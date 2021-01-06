package com.myorg.jenkins;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

public class EC2JenkinsStack extends Stack {

    public EC2JenkinsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public EC2JenkinsStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);
    }
}
