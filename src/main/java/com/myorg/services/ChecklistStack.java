package com.myorg.services;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;

public class ChecklistStack extends Stack {
    public ChecklistStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ChecklistStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create VPC
        Vpc myVPC = Vpc.Builder.create(this, "myVPC").build();
        Cluster cluster = Cluster.Builder.create(this, "FargateCluster_java").vpc(myVPC).build();

        //DO NOT add <AccountNO>.dkr.ecr.us-east-1.amazonaws.com/ with the ECR repository name
        //429506819373.dkr.ecr.us-east-1.amazonaws.com/alc-autobots-migration:questionnaire-service
        final String ECR_REPO_NAME = "alc-autobots-migration";
        final String CHECKLIST_TAG_NAME = "checklist-service";

        // Image from the Docker Hub
//        final String UI_DOCKER_IMAGE = "thiethaa/alc-autobots-ui";

        Repository checklistRepositoryBuilder = Repository.Builder.create(this, "checklistRepositoryBuilderID").build();
        IRepository iRepository_checklist = checklistRepositoryBuilder.fromRepositoryName(this, "ecrRepoChecklistID", ECR_REPO_NAME);

        // create ApplicationLoadBalanced Fargate Service from the ECS_Patter: AWS gives us a load balancer url for our microservices
        ApplicationLoadBalancedFargateService checklistServiceALB = ApplicationLoadBalancedFargateService.Builder.create(this, "checklistApplicationLaodBalancedFargateService")
                .cluster(cluster)
                .cpu(256)
                .serviceName("checklist-service")
                .desiredCount(2)
                .memoryLimitMiB(512)
                .publicLoadBalancer(true)
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        // .image(ContainerImage.fromRegistry(CHECKLIST_IMAGE)) // Image read from repos other than ECR
                        .image(ContainerImage.fromEcrRepository(iRepository_checklist, CHECKLIST_TAG_NAME))
                        .enableLogging(true)
                        .build())
                .build();

        // prints the App Load Balancer URL in teh Cloud Formations output tab
        CfnOutput checklist_cfnOutput = CfnOutput.Builder.create(this, "checklistCloudFormationOutputID")
                .description("Application Load balancer URL for the Fargate Service")
                .value(checklistServiceALB.getLoadBalancer().getLoadBalancerDnsName())
                .build();

        // Create Loggroup to track the container internal logs
        LogGroup checklist_logGroup = LogGroup.Builder.create(this, "checklistLogGroupId")
                .removalPolicy(RemovalPolicy.DESTROY)
                .logGroupName(checklistServiceALB.getService().getServiceName())
                .build();
    }
}
