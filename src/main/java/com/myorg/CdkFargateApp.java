package com.myorg;

import com.myorg.services.ChecklistStack;
import com.myorg.services.GeneralStack;
import com.myorg.services.QuestionnaireStack;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

public class CdkFargateApp {
    public static void main(final String[] args) {
        App app = new App();

//        create stack prop and pass it to the stack (containing stack props such as account no, region, env, etc)
        StackProps stackProps = StackProps.builder()
                .env(Environment.builder()
                        .account("429506819373")
                        .region("us-east-1")
                        .build())
                .terminationProtection(false)
                .build();

        new GeneralStack(app, "general-stack", stackProps);
        new QuestionnaireStack(app, "questionnaire-stack", stackProps);
        new ChecklistStack(app, "checklist-stack", stackProps);
        app.synth();
    }
}
