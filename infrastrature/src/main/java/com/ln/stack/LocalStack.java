package com.ln.stack;


import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateServiceProps;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStack extends Stack {

   final private Vpc vpc;
   final private software.amazon.awscdk.services.ecs.Cluster cluster;
   public LocalStack(final App scope, final String id, final StackProps props) {

      super(scope, id, props);
      this.vpc=createVpc();
      DatabaseInstance authServiceDb=
              createDatabaseInstance("AuthServiceDb","auth-service-db");
      DatabaseInstance patientServiceDb=
              createDatabaseInstance("PatientServiceDb","patient-service-db");
      CfnHealthCheck authServiceDbHealthCheck=
              createCfnHealthCheck(authServiceDb,"AuthServiceDbHealthCheck");
      CfnHealthCheck patientServiceDbHealthCheck=
              createCfnHealthCheck(patientServiceDb,"PatientServiceDbHealthCheck");
      CfnCluster mskCluster=createMskCluster();
      this.cluster=createEcsCluster();
      FargateService authService=createFargateService("AuthService","auth-service",
              List.of(4005)
      ,authServiceDb
      ,Map.of("JWT_SECRET","w7Xn4C2QGbKEpXZf8NZoYlR9tMAJ5vU2q6LW3Re9O1g="));

      authService.getNode().addDependency(authServiceDbHealthCheck);
      authService.getNode().addDependency(authServiceDb);

      FargateService billingService=createFargateService("BillingService",
              "billing-service"
      ,List.of(4001,9001)
              ,null
              ,null

      );
      FargateService analyticsService=createFargateService("AnalyticsService"
      ,"analytics-service"
                      ,List.of(4002)
      ,null
      ,null);

      analyticsService.getNode().addDependency(mskCluster);

      FargateService patientService=createFargateService("PatientService"
      ,"patient-service"
      ,List.of(4001)
              ,patientServiceDb
              ,Map.of("BILLING_SERVICE_ADDRESS","host.docker.internal"
                      ,"BILLING_SERVICE_GRPC_PORT","9001"
              ));
      patientService.getNode().addDependency(patientServiceDb);
      patientService.getNode().addDependency(patientServiceDbHealthCheck);
      patientService.getNode().addDependency(billingService);
      patientService.getNode().addDependency(mskCluster);
      createApiGatewayService();

   }

   private Vpc createVpc() {
    return  Vpc.Builder.create(this, "PatientManagementVPC")
              .vpcName("Patient Management VPC")
              .maxAzs(2)
              .build();
   }

   private DatabaseInstance createDatabaseInstance(String id,String dbName) {
        return DatabaseInstance.Builder
                .create(this,id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps
                                .builder()
                                .version(PostgresEngineVersion.VER_17_2).build() ))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
   }
   private CfnHealthCheck createCfnHealthCheck(DatabaseInstance db,String id) {
      return CfnHealthCheck.Builder.create(this,id)
              .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                      .type("TCP")
                      .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                      .ipAddress(db.getDbInstanceEndpointAddress())
                      .requestInterval(30)
                      .failureThreshold(3)
                      .build())
              .build();
   }
   private CfnCluster createMskCluster() {
      return CfnCluster.Builder.create(this,"MskCluster")
              .clusterName("Kafka-cluster")
              .kafkaVersion("2.8.0")
              .numberOfBrokerNodes(2)
              .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                      .instanceType("kafka.m5.xlarge")
                      .clientSubnets(vpc.getPrivateSubnets().stream().map(ISubnet::getSubnetId).collect(Collectors.toList()))
                      .brokerAzDistribution("DEFAULT")
                      .build())
              .build();

   }
   //auth-service.patient-management.local
   private Cluster createEcsCluster() {
      return software.amazon.awscdk.services.ecs.Cluster.Builder
              .create(this,"PatientManagementCluster")
              .vpc(vpc)
              .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                      .name("patient-management.local")
                      .build())
              .build();
   }

   private FargateService createFargateService(
           String id,
           String imageName,
           List<Integer> ports,
           DatabaseInstance db,
           Map<String,String> additionalEnvVari) {
      FargateTaskDefinition fargateTaskDefinition=FargateTaskDefinition.Builder
              .create(this,id+"Task")
              .cpu(256)
              .memoryLimitMiB(512)
              .build();
      ContainerDefinitionOptions.Builder containerOptions=
              ContainerDefinitionOptions.builder()
                      .image(ContainerImage.fromRegistry(imageName))
                      .portMappings(ports.stream()
                              .map(port->PortMapping.builder()
                                      .containerPort(port)
                                      .hostPort(port)
                                      .protocol(Protocol.TCP)
                                      .build()).toList())
                      .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                      .logGroup(LogGroup.Builder.create(this,id+"LogGroup")
                                              .logGroupName("/ecs/"+imageName)
                                              .removalPolicy(RemovalPolicy.DESTROY)
                                              .retention(RetentionDays.ONE_DAY)
                                              .build())
                                      .streamPrefix(imageName)
                              .build()));

      Map<String,String> envVariables=new HashMap<>();
      envVariables.put("SPRING_KAFKA_BOOTSTRAP_SERVERS","localhost.localstack.cloud:4510,localhost.localstack.cloud:4511,localhost.localstack.cloud:4512");
      if(additionalEnvVari!=null) {
         envVariables.putAll(additionalEnvVari);
      }
      if(db!=null) {
         envVariables.put("SPRING_DATASOURCE_URL","jdbc:postgresql://%s:%s/%s-db"
                 .formatted(
                         db.getDbInstanceEndpointAddress()
                         ,db.getDbInstanceEndpointPort()
                         ,imageName
                 ));
         envVariables.put("SPRING_DATASOURCE_USERNAME","admin_user");
         envVariables.put("SPRING_DATASOURCE_PASSWORD",db.getSecret().secretValueFromJson("password").toString());
         envVariables.put("SPRING_JPA_HIBERNATE_DDL_AUTO","update");
         envVariables.put("SPRING_SQL_INIT_MODE","always");
      }
      containerOptions.environment(envVariables);
      fargateTaskDefinition.addContainer(imageName+"container", containerOptions.build());
      return FargateService.Builder.create(this,id)
              .cluster(cluster)
              .taskDefinition(fargateTaskDefinition)
              .assignPublicIp(false)
              .serviceName(imageName)
              .build();

   }
   private void createApiGatewayService(){
      FargateTaskDefinition fargateTaskDefinition=FargateTaskDefinition.Builder
              .create(this,"ApiGateWayTaskDefinition")
              .cpu(256)
              .memoryLimitMiB(512)
              .build();

      ContainerDefinitionOptions containerOptions=
              ContainerDefinitionOptions.builder()
                      .image(ContainerImage.fromRegistry("api-gateway"))
                      .environment(Map.of(
                              "SPRING_PROFILES_ACTIVE","prod",
                              "AUTH_SERVICE_URL","http://host.docker.internal:4005"
                      ))
                      .portMappings(List.of(4004).stream()
                              .map(port->PortMapping.builder()
                                      .containerPort(port)
                                      .hostPort(port)
                                      .protocol(Protocol.TCP)
                                      .build()).toList())
                      .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                              .logGroup(LogGroup.Builder.create(this,"ApiGateWayLogGroup")
                                      .logGroupName("/ecs/"+"api-gateway")
                                      .removalPolicy(RemovalPolicy.DESTROY)
                                      .retention(RetentionDays.ONE_DAY)
                                      .build())
                              .streamPrefix("api-gateway")
                              .build()))
                      .build();
      fargateTaskDefinition.addContainer("ApiGateWayContainer",containerOptions);

      ApplicationLoadBalancedFargateService balancedFargateService=
              ApplicationLoadBalancedFargateService.Builder
                      .create(this,"ApiGatewayService")
                      .cluster(cluster)
                      .serviceName("api-gateway")
                      .taskDefinition(fargateTaskDefinition)
                      .desiredCount(1)
                      .healthCheckGracePeriod(Duration.seconds(60))
                      .build();
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
