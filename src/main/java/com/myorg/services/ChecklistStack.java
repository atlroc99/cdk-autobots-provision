package com.myorg.services;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;

import java.util.Arrays;

public class ChecklistStack extends Stack {
    public ChecklistStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ChecklistStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create VPC
        Vpc myVPC = Vpc.Builder.create(this, "myVPC").maxAzs(2).build();
       /* Cluster cluster = Cluster.Builder.create(this, "autobotsClusterID")
                .vpc(myVPC)
                .clusterName("CDK-AUTOBOTS-ECS-CLUSTER")
                .build();*/

        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, "cdkAutobotsClusterID", "sg-005e39b20967671f6");
        ClusterAttributes clusterAttributes = ClusterAttributes.builder()
                .clusterName("CDK-AUTOBOTS-ECS-CLUSTER")
                .securityGroups(Arrays.asList(iSecurityGroup))
                .vpc(myVPC)
                .build();

        ICluster iCluster = Cluster.fromClusterAttributes(this, "cdkAutobotsCluster", clusterAttributes);

        //DO NOT add <AccountNO>.dkr.ecr.us-east-1.amazonaws.com/ with the ECR repository name
        //429506819373.dkr.ecr.us-east-1.amazonaws.com/alc-autobots-migration:checklist-service
        final String ECR_REPO_NAME = "alc-autobots-migration";
        final String CHECKLIST_TAG_NAME = "checklist-service";

        // Image from the Docker Hub
//        final String UI_DOCKER_IMAGE = "thiethaa/alc-autobots-ui";
        Repository checklistRepositoryBuilder = Repository.Builder.create(this, "checklistRepositoryBuilderID").build();
        IRepository iRepository_checklist = checklistRepositoryBuilder.fromRepositoryName(this, "ecrRepoChecklistID", ECR_REPO_NAME);
//        IRepository iRepository_checklist = Repository.fromRepositoryName(this, "ecrChecklistRepoID", ECR_REPO_NAME);
        System.out.println("repository name: " + iRepository_checklist.getRepositoryName());

        // create ApplicationLoadBalanced Fargate Service from the ECS_Patter: AWS gives us a load balancer url for our microservices
        ApplicationLoadBalancedFargateService checklistServiceALB = ApplicationLoadBalancedFargateService.Builder.create(this, "checklistApplicationLoadBalancedFargateService")
                .cluster(iCluster)
                .cpu(512)
                .serviceName("checklist-service")
                .desiredCount(2)
                .memoryLimitMiB(1024)
                .publicLoadBalancer(true)
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromEcrRepository(iRepository_checklist, CHECKLIST_TAG_NAME))
                        .enableLogging(true)
                        .containerPort(7070)
                        .build())
                .openListener(true)
                .listenerPort(80)
                .build();

        ContainerDefinition containerDefinition = checklistServiceALB.getTaskDefinition().getDefaultContainer();
        FargateService fargateService = checklistServiceALB.getService();

        System.out.println("container name: " + containerDefinition.getContainerName());


        // prints the App Load Balancer URL in teh Cloud Formations output tab
        CfnOutput checklist_output = CfnOutput.Builder.create(this, "checklistCloudFormationOutputID")
                .description("Application Load balancer URL for the Fargate Service")
                .value(checklistServiceALB.getLoadBalancer().getLoadBalancerDnsName())
                .build();

        // Create Loggroup to track the container internal logs
        LogGroup checklist_lg = LogGroup.Builder.create(this, "checklistLogGroupId")
                .logGroupName(checklistServiceALB.getService().getServiceName())
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }
}
