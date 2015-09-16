# Spring Cloud Netflix: Service Discovery

<!-- TOC depth:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Spring Cloud Netflix: Service Discovery](#spring-cloud-netflix-service-discovery)
	- [Requirements](#requirements)
	- [Exercises](#exercises)
		- [Setup the `app-config` Repo](#setup-the-app-config-repo)
		- [Setup `config-server`](#setup-config-server)
		- [Setup `service-registry`](#setup-service-registry)
		- [Setup `fortune-service`](#setup-fortune-service)
		- [Setup `greeting-service`](#setup-greeting-service)
		- [Deploy the `service-registry` to PWS](#deploy-the-service-registry-to-pws)
		- [Update App Config for `fortune-service` and `greeting-service` to run on PWS](#update-app-config-for-fortune-service-and-greeting-service-to-run-on-pws)
		- [Deploy the `fortune-service` to PWS](#deploy-the-fortune-service-to-pws)
		- [Deploy the `greeting-service` app to PWS](#deploy-the-greeting-service-app-to-pws)
		- [Scale the `fortune-service`](#scale-the-fortune-service)
<!-- /TOC -->

## Requirements

[Lab Requirements](https://github.com/pivotal-enablement/lab-instructions/blob/master/requirements.md)

## Exercises


### Setup the `app-config` Repo

1) Create an `$APP_CONFIG_REPO_HOME/application.yml` in your fork of the `app-config` repo with the following contents:

```yml
 logging:
   level:
     io:
       pivotal: DEBUG
```
Then commit and push back to Github.



### Setup `config-server`

1) Start the `config-server` in a terminal window.  You may have a terminal window still open from the Spring Cloud Config Lab.

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/config-server
$ mvn clean spring-boot:run
```

2) Verify the `config-server` started correctly
```bash
curl -i http://localhost:8888/myapp/default

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
X-Application-Context: bootstrap:8888
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 27 Aug 2015 02:33:20 GMT

{
  "name": "myapp",
  "profiles": [
    "default"
  ],
  "label": "master",
  "propertySources": [
    {
      "name": "https://github.com/d4v3r/app-config.git/application.yml",
      "source": {
        "logging.level.io.pivotal": "DEBUG"
      }
    }
  ]
}
```

### Setup `service-registry`

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/service-registry/pom.xml` file.  By adding `spring-cloud-starter-eureka-server` to the classpath this application is eligible to embed an Eureka server.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>
```

2) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/service-registry/src/main/java/io/pivotal/ServiceRegistryApplication.java`.  Note the use of the ` @EnableEurekaServer` annotation that makes this application a Eureka server.

```java
 @SpringBootApplication
 @EnableEurekaServer
 public class ServiceRegistryApplication {

     public static void main(String[] args) {
         SpringApplication.run(ServiceRegistryApplication.class, args);
     }
 }
```

3). Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/service-registry/src/main/resources/application.yml`

```yml
 server:
   port: 8761

 eureka:
   instance:
     hostname: localhost
   client:
     registerWithEureka: false
     fetchRegistry: false
     serviceUrl:
       defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

4) Open a new terminal window.  Start the `service-registry`.

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/service-registry
$ mvn clean spring-boot:run
```

5) Verify the `service-registry` is up.  Browse to [http://localhost:8761/](http://localhost:8761/)
![eureka](resources/images/eureka.png "eureka")

### Setup `fortune-service`

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/fortune-service/src/main/resources/bootstrap.yml` file.  The name of this app is `fortune-service`.  It also uses the `config-server`.

```yml
 server:
   port: 8787
 spring:
   application:
     name: fortune-service
   cloud:
     config:
       uri: ${vcap.services.config-server.credentials.uri:http://localhost:8888}
```

2) Review the `$CLOUD_NATIVE_APP_LABS_HOME/fortune-service/pom.xml` file.  By adding `spring-cloud-starter-eureka` to the classpath this application is eligible to register and discover services with the `service-registry`.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

3) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/fortune-service/src/main/java/io/pivotal/FortuneServiceApplication.java`.  Notice the `@EnableDiscoveryClient`.  This registers the `fortune-service` with the `service-registry` application.

```java
@SpringBootApplication
@EnableDiscoveryClient
public class FortuneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FortuneServiceApplication.class, args);
    }
}
```

4). In your fork of the `app-config` repo.  Edit the `application.yml` file and add the following contents:

```yml
logging:
  level:
    io:
      pivotal: DEBUG
eureka: # <--- ADD NEW SECTION
  instance:
    metadataMap:
      instanceId: ${vcap.application.instance_id:${spring.application.name}:${server.port:8080}}
```
The expression above creates a unique `instanceId` when running locally or in PWS.  By default a eureka instance is registered with an ID that is equal to its hostname (i.e. only one service per host).  The expression above allows for multiple instances in the given environment.  Also note that there is no `eureka.client.serviceUrl.defaultZone` defined.  It defaults to `http://localhost:8761/eureka/`.

5) Open a new terminal window.  Start the `fortune-service`

 ```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/fortune-service
$ mvn clean spring-boot:run
```

6) After the a few moments, check the `service-registry` dashboard.  Confirm the `fortune-service` is registered.
![fortune-service](resources/images/fortune-service.png "fortune-service")

### Setup `greeting-service`

1) Review the `$CLOUD_NATIVE_APP_LABS_HOME/greeting-service/src/main/resources/bootstrap.yml` file.  The name of this app is `greeting-service`.  It also uses the `config-server`

```yml
 server:
   port: 8080
 spring:
   application:
     name: greeting-service
   cloud:
     config:
       uri: ${vcap.services.config-server.credentials.uri:http://localhost:8888}
```

2) Review the `$CLOUD_NATIVE_APP_LABS_HOME/greeting-service/pom.xml` file.  By adding `spring-cloud-starter-eureka` to the classpath this application is eligible to register and discover services with the `service-registry`.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```


3) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/greeting-service/src/main/java/io/pivotal/GreetingServiceApplication.java`.  Notice the `@EnableDiscoveryClient`.  This registers the `greeting-service` app with the `service-registry`.

 ```java
 @SpringBootApplication
 @EnableDiscoveryClient
 public class GreetingServiceApplication {


     public static void main(String[] args) {
         SpringApplication.run(GreetingServiceApplication.class, args);
     }

 }
```

4) Review the the following file: `$CLOUD_NATIVE_APP_LABS_HOME/greeting-service/src/main/java/io/pivotal/greeting/GreetingController.java`.  Notice the `DiscoveryClient`.  It is used to discovery services registered with the `service-registry`.

```java
@Controller
public class GreetingController {

	Logger logger = LoggerFactory
			.getLogger(GreetingController.class);




	@Autowired
	private DiscoveryClient discoveryClient;

	@RequestMapping("/")
	String getGreeting(Model model){

		logger.debug("Adding greeting");
		model.addAttribute("msg", "Greetings!!!");


		RestTemplate restTemplate = new RestTemplate();
        String fortune = restTemplate.getForObject(fetchFortuneServiceUrl(), String.class);

		logger.debug("Adding fortune");
		model.addAttribute("fortune", fortune);

		//resolves to the greeting.vm velocity template
		return "greeting";
	}

	private String fetchFortuneServiceUrl() {
	    InstanceInfo instance = discoveryClient.getNextServerFromEureka("FORTUNE-SERVICE", false);
	    logger.debug("instanceID: {}", instance.getId());

	    String fortuneServiceUrl = instance.getHomePageUrl();
		logger.debug("fortune service url: {}", fortuneServiceUrl);

	    return fortuneServiceUrl;
	}

}
```

5) Open a new terminal window.  Start the `greeting-service` app

 ```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/greeting-service
$ mvn clean spring-boot:run
```

6) After the a few moments, check the `service-registry` [dashboard](http://localhost:8761).  Confirm the `greeting-service` app is registered.
![greeting](resources/images/greeting.png "greeting")

7) [Browse](http://localhost:8080/) to the `greeting-service` application.  Confirm you are seeing fortunes.  Refresh as desired.  Also review the terminal output for the `greeting-service`.  See the `fortune-service` url being logged.

### Deploy the `service-registry` to PWS

1) Package `service-registry`

```bash
$ mvn clean package
```

2) Deploy `service-registry`.  Confirm it came up correctly.

```bash
$ cf push service-registry -p target/service-registry-0.0.1-SNAPSHOT.jar -m 512M --random-route
```

3) Create an user provided service for the `service-registry`.  Substitute your uri.  Do not use the literal below.

```bash
$ cf cups service-registry -p uri
$ uri> http://service-registry-unfluctuant-billionaire.cfapps.io
```

### Update App Config for `fortune-service` and `greeting-service` to run on PWS

1) In the `app-config` repo add the following to the `$APP_CONFIG_REPO_HOME/application.yml`

```yml
 logging:
   level:
     io:
       pivotal: DEBUG
 eureka:
   instance:
     metadataMap:
       instanceId: ${vcap.application.instance_id:${spring.application.name}:${server.port:8080}}
   client:    # <--- ADD THE CLIENT SECTION!!!
     serviceUrl:
       defaultZone: ${vcap.services.service-registry.credentials.uri:http://localhost:8761}/eureka/
```

2) Add a second yaml document to `application.yml`.  When using the `cloud` profile override the hostname and port to communicate with the given service.

```yml
logging:
  level:
    io:
      pivotal: DEBUG
eureka:
  instance:
    metadataMap:
      instanceId: ${vcap.application.instance_id:${spring.application.name}:${server.port:8080}}
  client:
    serviceUrl:
      defaultZone: ${vcap.services.service-registry.credentials.uri:http://localhost:8761}/eureka/
---  # <-- ADD THIS SECTION
spring:
  profiles: cloud
eureka:
  instance:
    hostname: ${vcap.application.uris[0]}
    nonSecurePort: 80
```


### Deploy the `fortune-service` to PWS

1) Package `fortune-service`

```bash
$ mvn clean package
```

2) Deploy `fortune-service`.

```bash
$ cf push fortune-service -p target/fortune-service-0.0.1-SNAPSHOT.jar -m 512M --random-route --no-start
```

3) Bind services to `fortune-service` and start the app.

```bash
$ cf bind-service fortune-service config-server
$ cf bind-service fortune-service service-registry
$ cf start fortune-service
```

4) Confirm `fortune-service` registered the the `service-registry`
![fortune-service](resources/images/cf-fortune-service.png "fortune-service")

### Deploy the `greeting-service` app to PWS

1) Package `greeting-service`

```bash
$ mvn clean package
```

2) Deploy `greeting-service`.

```bash
$ cf push greeting-service -p target/greeting-service-0.0.1-SNAPSHOT.jar -m 512M --random-route --no-start
```

3) Bind services to `greeting` and start the app.

```bash
$ cf bind-service greeting-service config-server
$ cf bind-service greeting-service service-registry
$ cf start greeting-service
```

4) Confirm `greeting-service` registered with the `service-registry`
![greeting](resources/images/cf-greeting.png "greeting")

5) Browse to the `greeting-service` application.  Confirm you are seeing fortunes.  Refresh as desired.  Also review the terminal output for the `greeting-service`.  See the `fortune-service` url being logged.

### Scale the `fortune-service`

1) Scale the `fortune-service` app instances to 3

```
$ cf scale fortune-service -i 3
```

2) Wait for the new instances to register with the `service-registry`

3) Tail the logs for the `greeting-service` application

```
$ cf logs greeting-service | grep GreetingController
```

4) Refresh the `greeting-service` `/` endpoint

5) Observe the effects in the log output.  The `discoveryClient` round robins the `fortune-service` instances.  However, PWS does not allow cross container communication, so we override the hostname and send traffic back through the PWS routers.

```
2015-09-01T09:54:58.57-0500 [App/2]      OUT 2015-09-01 14:54:58.574 DEBUG 33 --- [io-63979-exec-8] io.pivotal.greeting.GreetingController   : Adding greeting
2015-09-01T09:54:58.58-0500 [App/2]      OUT 2015-09-01 14:54:58.589 DEBUG 33 --- [io-63979-exec-8] io.pivotal.greeting.GreetingController   : instanceID: fortune-service-tympanic-nonvoter.cfapps.io:ab631fe181724ecb8c065ae6e1de8ee9
2015-09-01T09:54:58.58-0500 [App/2]      OUT 2015-09-01 14:54:58.589 DEBUG 33 --- [io-63979-exec-8] io.pivotal.greeting.GreetingController   : fortune service url: http://fortune-service-tympanic-nonvoter.cfapps.io:80/
2015-09-01T09:54:58.60-0500 [App/2]      OUT 2015-09-01 14:54:58.606 DEBUG 33 --- [io-63979-exec-8] io.pivotal.greeting.GreetingController   : Adding fortune
2015-09-01T09:55:42.73-0500 [App/2]      OUT 2015-09-01 14:55:42.739 DEBUG 33 --- [io-63979-exec-2] io.pivotal.greeting.GreetingController   : Adding greeting
2015-09-01T09:55:42.75-0500 [App/2]      OUT 2015-09-01 14:55:42.750 DEBUG 33 --- [io-63979-exec-2] io.pivotal.greeting.GreetingController   : instanceID: fortune-service-tympanic-nonvoter.cfapps.io:0ec2298e890a45bf81932ec0792a64e2
2015-09-01T09:55:42.75-0500 [App/2]      OUT 2015-09-01 14:55:42.750 DEBUG 33 --- [io-63979-exec-2] io.pivotal.greeting.GreetingController   : fortune service url: http://fortune-service-tympanic-nonvoter.cfapps.io:80/
2015-09-01T09:55:42.76-0500 [App/2]      OUT 2015-09-01 14:55:42.761 DEBUG 33 --- [io-63979-exec-2] io.pivotal.greeting.GreetingController   : Adding fortune
2015-09-01T09:55:57.30-0500 [App/1]      OUT 2015-09-01 14:55:57.308 DEBUG 34 --- [io-61136-exec-4] io.pivotal.greeting.GreetingController   : Adding greeting
2015-09-01T09:55:57.33-0500 [App/1]      OUT 2015-09-01 14:55:57.335 DEBUG 34 --- [io-61136-exec-4] io.pivotal.greeting.GreetingController   : instanceID: fortune-service-tympanic-nonvoter.cfapps.io:3131d8b3598c4093a599b05e881c0063
2015-09-01T09:55:57.33-0500 [App/1]      OUT 2015-09-01 14:55:57.336 DEBUG 34 --- [io-61136-exec-4] io.pivotal.greeting.GreetingController   : fortune service url: http://fortune-service-tympanic-nonvoter.cfapps.io:80/
2015-09-01T09:55:57.35-0500 [App/1]      OUT 2015-09-01 14:55:57.355 DEBUG 34 --- [io-61136-exec-4] io.pivotal.greeting.GreetingController   : Adding fortune
^C
```
