package com.ln.stack;

import software.amazon.awscdk.*;

public class LocalStack extends Stack {
   public LocalStack(final App scope, final String id, final StackProps props) {
      super(scope, id, props);
   }

   public static void main(final String[] args) {
App app = new App(AppProps.builder()
        .outdir("./cdk.out")
        .build());
StackProps stackProps=StackProps.builder()
        .synthesizer(new BootstraplessSynthesizer())
        .build();
   new LocalStack(app,"LocalStack", stackProps);
   app.synth();
      System.out.println("App synthesizing is in progress...");
   }
}
