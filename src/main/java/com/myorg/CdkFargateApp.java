package com.myorg;

import software.amazon.awscdk.core.App;

import java.util.Arrays;

public class CdkFargateApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkFargateStack(app, "CdkFargateStack");

        app.synth();
    }
}
