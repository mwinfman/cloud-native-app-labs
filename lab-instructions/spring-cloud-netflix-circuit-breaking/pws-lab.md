# Spring Cloud Netflix: Circuit Breaking

<!-- TOC depth:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Spring Cloud Netflix: Circuit Breaking](#spring-cloud-netflix-circuit-breaking)
	- [Requirements](#requirements)
	- [Exercises](#exercises)
		- [Start the  `config-server`,  `service-registry`, and `fortune-service`](#start-the-config-server-service-registry-and-fortune-service)
		- [Setup `greeting-hystrix`](#setup-greeting-hystrix)
		- [Setup `hystrix-dashboard`](#setup-hystrix-dashboard)
		- [Setup `turbine`](#setup-turbine)
		- [Deploying to PWS](#deploying-to-pws)
		- [Create a RabbitMQ Service Instance on PWS](#create-a-rabbitmq-service-instance-on-pws)
		- [Deploy `greeting-hystrix` to PWS](#deploy-greeting-hystrix-to-pws)
		- [Deploy `turbine-amqp` to PWS](#deploy-turbine-amqp-to-pws)
		- [Deploy `hystrix-dashboard` to PWS](#deploy-hystrix-dashboard-to-pws)
<!-- /TOC -->

## Requirements

[Lab Requirements](https://github.com/pivotal-enablement/lab-instructions/blob/master/requirements.md)

## Exercises


### Start the  `config-server`,  `service-registry`, and `fortune-service`

1) Start the `config-server` in a terminal window.  You may have a terminal windows still open from previous labs.  They may be reused for this lab.

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/config-server
$ mvn clean spring-boot:run
```

2) Start the `service-registry`

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/service-registry
$ mvn clean spring-boot:run
```

3) Start the `fortune-service`

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/fortune-service
$ mvn clean spring-boot:run
```


### Setup `greeting-hystrix`

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/greeting-hystrix/pom.xml` file.  By adding `spring-cloud-starter-hystrix` to the classpath this application is eligible to use circuit breakers via Hystrix.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix</artifactId>
</dependency>
```

2) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/greeting-hystrix/src/main/java/io/pivotal/GreetingHystrixApplication.java`.  Note the use of the `@EnableCircuitBreaker` annotation. This allows the application to create circuit breakers.

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class GreetingHystrixApplication {


    public static void main(String[] args) {
        SpringApplication.run(GreetingHystrixApplication.class, args);
    }

}
```

3). Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/greeting-hystrix/src/main/java/io/pivotal/fortune/FortuneService.java`.  Note the use of the `@HystrixCommand`.  If `getFortune()` fails, a fallback method `defaultFortune` will be invoked.

```java
@Service
public class FortuneService {

	Logger logger = LoggerFactory
			.getLogger(FortuneService.class);

	@Autowired
	private RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "defaultFortune")
	public String getFortune() {
    String fortune = restTemplate.getForObject("http://fortune-service", String.class);
		return fortune;
	}

	public String defaultFortune(){
		logger.debug("Default fortune used.");
		return "This fortune is no good. Try another.";
	}



}

```

4) Open a new terminal window. Start the `greeting-hystrix`

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/greeting-hystrix
$ mvn clean spring-boot:run
```

5) Refresh the `greeting-hystrix` `/` endpoint.  You should get fortunes from the `fortune-service`.

6) Stop the `fortune-service`.  And refresh the `greeting-hystrix` `/` endpoint again.  The default fortune is given.

7) Restart the `fortune-service`.  And refresh the `greeting-hystrix` `/` endpoint again.  Fortunes from the `fortune-service` are back.

### Setup `hystrix-dashboard`

Being able to monitor the state of our circuit breakers is highly valuable.  We can do this with the Hystrix Dashboard.

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/hystrix-dashboard/pom.xml` file.  By adding `spring-cloud-starter-hystrix-dashboard` to the classpath this application is exposes a Hystrix Dashboard.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
</dependency>
```

2) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/hystrix-dashboard/src/main/java/io/pivotal/HystrixDashboardApplication.java`.  Note the use of the `@EnableHystrixDashboard` annotation. This creates a Hystrix Dashboard.

```java
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixDashboardApplication.class, args);
    }
}
```

3) Open a new terminal window. Start the `hystrix-dashboard`

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/hystrix-dashboard
$ mvn clean spring-boot:run
```

4) Open a browser to [http://localhost:8686/hystrix](http://localhost:8686/hystrix)
![hystrix-dashboard](resources/images/hystrix-dashboard.png "hystrix-dashboard")

5) Link the `hystrix-dashboard` to the `greeting-hystrix` app.  Enter `http://localhost:8080/hystrix.stream` as the stream to monitor.

6) Experiment! Refresh the `greeting-hystrix` `/` endpoint several times.  Take down the `fortune-service` app.  What does the dashboard do?  Review the [dashboard doc](https://github.com/Netflix/Hystrix/wiki/Dashboard) for explanation on metrics.
![dashboard-activity](resources/images/dashboard-activity.png "dashboard-activity")

### Setup `turbine`

Looking at an individual instances Hystrix data is not very useful in terms of the overall health of the system. Turbine is an application that aggregates all of the relevant /hystrix.stream endpoints into a combined /turbine.stream for use in the Hystrix Dashboard.

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/turbine/pom.xml` file.  By adding `spring-cloud-starter-hystrix` to the classpath this application is eligible to use circuit breakers via Hystrix.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-turbine</artifactId>
</dependency>
```

2) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/turbine/src/main/java/io/pivotal/TurbineApplication.java`.  Note the use of the `@EnableTurbine` annotation. This creates a turbine application.

```java
@SpringBootApplication
@EnableTurbine
public class TurbineApplication {


    public static void main(String[] args) {
        SpringApplication.run(TurbineApplication.class, args);
    }

}
```

3). Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/turbine/src/main/resources/bootstrap.yml`.  `turbine.appConfig` is a list of eureka serviceIds that turbine will use to lookup instances.  `turbine.aggregator.clusterConfig` is the turbine cluster these services belong too.

```yml
spring:
  application:
    name: turbine
  cloud:
    config:
      uri: ${vcap.services.config-server.credentials.uri:http://localhost:8888}
turbine:
  aggregator:
    clusterConfig: GREETING-HYSTRIX
  appConfig: greeting-hystrix
```

4) Open a new terminal window. Start the `turbine` app

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/turbine
$ mvn clean spring-boot:run
```

5) Wait for the `turbine` application to register with [`service-registry`](http://localhost:8761/).

6) Configure the [`hystrix-dashboard`](http://localhost:8686/hystrix) to consume the turbine stream.  Enter `http://localhost:8585/turbine.stream?cluster=GREETING-HYSTRIX`

7) Experiment! Refresh the `greeting-hystrix` `/` endpoint several times.  Take down the `fortune-service` app.  What does the dashboard do?

### Deploying to PWS

In PWS the classic Turbine model of pulling metrics from all the distributed Hystrix commands doesn’t work.  This is because cross container communication is not allowed.  Every app instance has the same url.  The problem is solved with Turbine AMQP.  Metrics are published through a message broker.  We'll use RabbitMQ.

### Create a RabbitMQ Service Instance on PWS

```bash
$ cf create-service cloudamqp lemur turbine-broker
```

### Deploy `greeting-hystrix` to PWS

1) Add the following dependency to the $CLOUD_NATIVE_APP_LABS_HOME/greeting-hystrix/pom.xml file. _You must edit the file._

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-netflix-hystrix-amqp</artifactId>
</dependency>
```

2) Package, push and bind service for `greeting-hystrix`
```bash
$ mvn clean package
$ cf push greeting-hystrix -p target/greeting-hystrix-0.0.1-SNAPSHOT.jar -m 512M --random-route --no-start
$ cf bind-service greeting-hystrix config-server
$ cf bind-service greeting-hystrix service-registry
$ cf bind-service greeting-hystrix turbine-broker
$ cf start greeting-hystrix
```

### Deploy `turbine-amqp` to PWS

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/turbine-amqp/pom.xml` file.  By adding `spring-cloud-starter-turbine-amqp` to the classpath this application is eligible to use Turbine AMQP.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-turbine-amqp</artifactId>
</dependency>
```

2) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/turbine-amqp/src/main/java/io/pivotal/TurbineApplication.java`.  Note the use of the `@EnableTurbineAmqp` annotation. This creates a turbine application.  Turbine AMQP uses `com.netflix.turbine:turbine-core:2.0.0-DP.2` which leverages Netty, so we turn off our servlet container (Tomcat).

```java
@SpringBootApplication
@EnableTurbineAmqp
public class TurbineApplication {

    public static void main(String[] args) {
		new SpringApplicationBuilder(TurbineApplication.class).web(false).run(args);
	}


}
```

3). Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/turbine-amqp/src/main/resources/bootstrap.yml`.  `turbine.appConfig` and `turbine.aggregator.clusterConfig` no longer need to be configured.

```yml
spring:
  application:
    name: turbine-amqp
  cloud:
    config:
      uri: ${vcap.services.config-server.credentials.uri:http://localhost:8888}
```


4) Package, push and bind service for `turbine-amqp`
```bash
$ mvn clean package
$ cf push turbine-amqp -p target/turbine-amqp-0.0.1-SNAPSHOT.jar --random-route -m 512M --no-start
$ cf bind-service turbine-amqp turbine-broker
$ cf start turbine-amqp
```

### Deploy `hystrix-dashboard` to PWS

1) Package, and push `hystrix-dashboard`
```bash
$ mvn clean package
$ cf push hystrix-dashboard -p target/hystrix-dashboard-0.0.1-SNAPSHOT.jar -m 512M --random-route
```

2) Configure the `hystrix-dashboard` to consume the turbine stream.  Enter your `turbine-amqp` url.

3) Experiment! Refresh the `greeting-hystrix` `/` endpoint several times.  Take down the `fortune-service` app.  Scale the `greeting-hystrix` app.  What does the dashboard do?
