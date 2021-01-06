package com.myorg.services;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;

public class QuestionnaireStack extends Stack {
    public QuestionnaireStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public QuestionnaireStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);


        // Create VPC
        Vpc myVPC = Vpc.Builder.create(this, "myVPC").build();
        Cluster cluster = Cluster.Builder.create(this, "FargateCluster_java").vpc(myVPC).build();

        //DO NOT add <AccountNO>.dkr.ecr.us-east-1.amazonaws.com/ with the ECR repository name
        //429506819373.dkr.ecr.us-east-1.amazonaws.com/alc-autobots-migration:questionnaire-service
        final String ECR_REPO_NAME = "alc-autobots-migration";
        final String QUESTIONNAIRE_TAG_NAME = "questionnaire-service";

        // Image from the Docker Hub
//        final String UI_DOCKER_IMAGE = "thiethaa/alc-autobots-ui";

        Repository questionnaireRepositoryBuilder = Repository.Builder.create(this, "questionnaireRepositoryBuilderID").build();
        IRepository iRepository_questionnaire = questionnaireRepositoryBuilder.fromRepositoryName(this, "ecrRepoQuestionnaireID", ECR_REPO_NAME);

        ApplicationLoadBalancedFargateService questionnaireServiceALB = ApplicationLoadBalancedFargateService.Builder.create(this, "questionnaireApplicationLaodBalancedFargateService")
                .cluster(cluster)
                .cpu(256)
                .serviceName("questionnaire-service")
                .desiredCount(2)
                .memoryLimitMiB(512)
                .publicLoadBalancer(true)
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromEcrRepository(iRepository_questionnaire, QUESTIONNAIRE_TAG_NAME))
                        .enableLogging(true)
                        .build())
                .build();

        CfnOutput questionnaire_cfnOutput = CfnOutput.Builder.create(this, "questionnaireCloudFormationOutputID")
                .description("Application Load balacner URL for the Fargate Service")
                .value(questionnaireServiceALB.getLoadBalancer().getLoadBalancerDnsName())
                .build();

        // Create Loggroup to track the container internal logs
        LogGroup questionnaire_logGroup = LogGroup.Builder.create(this, "questionnaireLogGroupId")
                .removalPolicy(RemovalPolicy.DESTROY)
                .logGroupName(questionnaireServiceALB.getService().getServiceName())
                .build();
    }
}
