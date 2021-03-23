package com.myorg;

import software.amazon.awscdk.core.App;

import java.util.Arrays;

public class CursoAwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        VPCStack vpc = new VPCStack(app, "Vpc");
        ClusterStack cluster = new ClusterStack(app, "Cluster", vpc.getVpc());
        cluster.addDependency(vpc);


        RdsStack rdsStack = new RdsStack(app, "Rds", vpc.getVpc());
        rdsStack.addDependency(vpc);

        SnsStack snsStack = new SnsStack(app, "Sns");

        Service01Stack service01 = new Service01Stack(app, "Service01", cluster.getCluster(), snsStack.getProductEventTopic());
        service01.addDependency(cluster);
        service01.addDependency(rdsStack);
        service01.addDependency(snsStack);

        DynamoDbStack db = new DynamoDbStack(app, "DynamoDb");

        Service02Stack service02 = new Service02Stack(app, "Service02", cluster.getCluster(),
                snsStack.getProductEventTopic(), db.getProductEventsDynamo());

        service02.addDependency(snsStack);
        service02.addDependency(cluster);
        service02.addDependency(db);

        app.synth();
    }
}
