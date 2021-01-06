package com.myorg;

import javafx.concurrent.Task;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;

import java.util.Arrays;

public class CdkFargateStack2 extends Stack {
    public CdkFargateStack2(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkFargateStack2(Construct scope, String id, StackProps props) {
        super(scope, id, props);

//        Vpc _myVpc = Vpc.Builder.create(this,"myVPC").maxAzs(2).natGateways(1).build();
//        ICluster iCluster = Cluster.Builder.create(this, "myCluster2").vpc(_myVpc).build();
        ICluster iCluster = Cluster.Builder.create(this, "myCluster2").build();
        //429506819373.dkr.ecr.us-east-1.amazonaws.com/
        final String ECR_REPO_NAME = "alc-autobots-migration";
        final String TAG_NAME = "checklist-service";
        Repository repositoryBuilder = Repository.Builder.create(this, "repositoryBuilderID").build();
        IRepository iRepository = repositoryBuilder.fromRepositoryName(this, "ecrRepoID", ECR_REPO_NAME);

      /*  VpcLookupOptions vpcLookupOptions = VpcLookupOptions.builder().vpcId("vpc-d1c1dcab").build();
        IVpc iVpc = Vpc.fromLookup(this, "vpcID", vpcLookupOptions);
        Vpc iVpc = Vpc.Builder.create(this, "myVpcID").build();

        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "ecsSecurityGroupID")
                .securityGroupName("autobots-checklist-service")
                .vpc(iVpc)
                .build();
*/
         //429506819373.dkr.ecr.us-east-1.amazonaws.com/
        // 429506819373.dkr.ecr.us-east-1.amazonaws.com/alc-autobots-migration:checklist-service
        ContainerDefinitionOptions containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromEcrRepository(iRepository, TAG_NAME))
                .memoryReservationMiB(512)
                .build();

        TaskDefinition taskDefinition = TaskDefinition.Builder.create(this, "taskDefID")
                .cpu("256")
                .memoryMiB("512")
                .compatibility(Compatibility.EC2_AND_FARGATE)
                .build();

        taskDefinition.addContainer("containerDefID", containerDefinitionOptions);

        FargateService iFargateService = FargateService.Builder.create(this, "fargateServiceID")
                .cluster(iCluster)
                .serviceName("checklist-service-2")
                .desiredCount(2)
                .maxHealthyPercent(100)
                .minHealthyPercent(0)
//                .securityGroups(Arrays.asList(securityGroup))
                .taskDefinition(taskDefinition)
                .build();
    }
}
