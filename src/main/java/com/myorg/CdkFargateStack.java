package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;

public class CdkFargateStack extends Stack {
    public CdkFargateStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkFargateStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create VPC
        Vpc myVPC = Vpc.Builder.create(this, "myVPC").build();
        Cluster cluster = Cluster.Builder.create(this, "FargateCluster_java").vpc(myVPC).build();

        //DO NOT add <AccountNO>.dkr.ecr.us-east-1.amazonaws.com/ with the ECR repository name
        final String ECR_REPO_NAME = "alc-autobots-migration";
        final String TAG_NAME = "checklist-service";

        // Image from the Docker Hub
        final String UI_DOCKER_IMAGE = "thiethaa/alc-autobots-ui";

        Repository repositoryBuilder = Repository.Builder.create(this, "repositoryBuilderID").build();
        IRepository iRepository = repositoryBuilder.fromRepositoryName(this, "ecrRepoID", ECR_REPO_NAME);

        // create ApplicationLoadBalanced Fargate Service from the ECS_Patter: AWS gives us a load balancer url for our microservices
        ApplicationLoadBalancedFargateService appLoadBalFargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "myApplicationLaodBalancedFargateService")
                .cluster(cluster)
                .cpu(256)
                .serviceName("checklist-service-1")
                .desiredCount(2)
                .memoryLimitMiB(512)
                .publicLoadBalancer(true)
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        // .image(ContainerImage.fromRegistry(CHECKLIST_IMAGE)) // Image read from repos other than ECR
                        .image(ContainerImage.fromEcrRepository(iRepository, TAG_NAME))
                        .containerPort(7070)
                        .enableLogging(true)
                        .build())
                .build();

        // prints the App Load Balancer URL in teh Cloud Formations output tab
        CfnOutput cfnOutput = CfnOutput.Builder.create(this, "cloudFormationOutputID")
                .description("Application Load balacner URL for the Fargate Service")
                .value(appLoadBalFargateService.getLoadBalancer().getLoadBalancerDnsName())
                .build();

        // Create Loggroup to track the container internal logs
        LogGroup logGroup = LogGroup.Builder.create(this, "logGroupId")
                .removalPolicy(RemovalPolicy.DESTROY)
                .logGroupName(appLoadBalFargateService.getService().getServiceName())
                .build();
    }
}
