# Projeto CDK de Criação de recursos na AWS 

Curso da Udemy do Paulo Siecola

Este projeto irá criar o seguinte:
1 Cluster ECS
1 VPC
1 Instancia RDS MySQL
1 Serviço SNS
1 Instancia DynamoDB
2 Serviços no ECS
1 Fila SQS


The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation
 * `cdk deploy --all --parameters Rds:databasePassword=senha123` initial command to run the application
 
Enjoy!
