package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.HashMap;
import java.util.Map;

public class Service02Stack extends Stack{
    public Service02Stack(final Construct scope, final String id,
                          Cluster cluster, SnsTopic topic, Table productEventsDynamo) {
        this(scope, id, null, cluster, topic, productEventsDynamo);
    }

    public Service02Stack(final Construct scope, final String id,
                          final StackProps props, Cluster cluster,
                          SnsTopic topic, Table productEventsDynamo) {
        super(scope, id, props);

        Queue productEventsDlq = Queue.Builder.create(this, "ProductEventsDlq")
                .queueName("product-events-dlq")
                .build();

        DeadLetterQueue dlq = DeadLetterQueue.builder()
                .queue(productEventsDlq)
                .maxReceiveCount(3)
                .build();

        Queue productEvents = Queue.Builder.create(this, "ProductEvents")
                .queueName("product-events")
                .deadLetterQueue(dlq)
                .build();
        SqsSubscription sqsSubscription = SqsSubscription.Builder.create(productEvents)
                .build();
        topic.getTopic().addSubscription(sqsSubscription);

        Map<String, String> envVariables = new HashMap<>();
        /*envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint")
                + ":3306/aws_project01?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));*/
        envVariables.put("AWS_REGION", "us-east-1");
        envVariables.put("AWS_SQS_QUEUE_PRODUCT_EVENTS_NAME", productEvents.getQueueName());
       // envVariables.put("AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN", topic.getTopic().getTopicArn());

        ApplicationLoadBalancedFargateService service02 = ApplicationLoadBalancedFargateService.Builder
                .create(this, "ALB02")
                .serviceName("service-02")
                .cluster(cluster)
                .cpu(512)
                .desiredCount(2)
                .listenerPort(9090)
                .memoryLimitMiB(1024)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project02")
                                .image(ContainerImage.fromRegistry("marconefreitas/curso_aws_repository2:1.0.10"))
                                .containerPort(9090)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "Service02LogGroup")
                                                .logGroupName("Service02")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("Service02")
                                        .build()))
                                .environment(envVariables)
                                .build())
                .publicLoadBalancer(true)
                .build();

        service02.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port("9090")
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTaskCount = service02.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());
        scalableTaskCount.scaleOnCpuUtilization("Service02AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

        productEvents.grantConsumeMessages(service02.getTaskDefinition().getTaskRole());
        productEventsDynamo.grantReadWriteData(service02.getTaskDefinition().getTaskRole());
    }
}
