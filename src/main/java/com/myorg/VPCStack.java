package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;

public class VPCStack extends Stack {

    private Vpc vpc;

    public VPCStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VPCStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        vpc = Vpc.Builder.create(this, "Vpc01")
                .maxAzs(3)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
