# Spring Cloud Config

<!-- TOC depth:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Spring Cloud Config](#spring-cloud-config)
	- [Requirements](#requirements)
	- [Exercises](#exercises)
		- [Setup the `app-config` Repo](#setup-the-app-config-repo)
		- [Setup the `cloud-native-app-labs` Repo](#setup-the-cloud-native-app-labs-repo)
		- [`config-server` Setup](#config-server-setup)
		- [`greeting-config` Setup](#greeting-config-setup)
		- [Deploy the `config-server` and `greeting-config` apps to PWS](#deploy-the-config-server-and-greeting-config-apps-to-pws)
		- [Changing Logging Levels](#changing-logging-levels)
		- [`@ConfigurationProperties`](#configurationproperties)
		- [`@RefreshScope`](#refreshscope)
		- [Override Configuration Values By Profile](#override-configuration-values-by-profile)
		- [Cloud Bus](#cloud-bus)
<!-- /TOC -->

## Requirements

[Lab Requirements](https://github.com/pivotal-enablement/cloud-native-app-labs/blob/master/lab-instructions/requirements.md)

## Exercises

### Setup the `app-config` Repo
To start, we need a repository to hold our configuration.

1) Fork the configuration repo to your account.  Browse to: https://github.com/pivotal-enablement/app-config.  Then fork the repo.
![fork](resources/images/fork.png "fork")

2) Open a new terminal window and clone the fork you just created

```bash
$ git clone <Your fork of the app-config repo>
$ cd app-config
```

### Setup the `cloud-native-app-labs` Repo
1) Fork the labs repo to your account.  Browse to: https://github.com/pivotal-enablement/cloud-native-app-labs.  Then fork the repo.

2) Open a new terminal window.  Clone the following repo.  This contains several applications used to demonstrate cloud native architectures.  Get familiar with the sub directories.

```bash
$ git clone <Your fork of the cloud-native-app-labs repo>
$ cd cloud-native-app-labs
```

2) OPTIONAL STEP - Import applications into your IDE such as SpringSource Tool Suite.  Importing projects at the `cloud-native-app-labs` level is recommended because there are several projects. Otherwise, use your favorite editor.

### `config-server` Setup

1) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/config-server/pom.xml`
By adding `spring-cloud-config-server` to the classpath this application is eligible to embed a config-server.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```
2) Review the following file:`$CLOUD_NATIVE_APP_LABS_HOME/config-server/src/main/java/io/pivotal/ConfigServerApplication.java`

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```
Note the `@EnableConfigServer` annotation.  That embeds the config-server.

3) Set the Github repository for the `config-server`.  Edit the `$CLOUD_NATIVE_APP_LABS_HOME/config-server/src/main/resources/application.yml` file.

```yml
 server:
   port: 8888

 spring:
   cloud:
     config:
       server:
         git:
           uri: https://github.com/d4v3r/app-config.git #<-- CHANGE ME
```
Make sure to substitute your forked repository.  Do not use the literal above.

4) Start the `config-server`.

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/config-server
$ mvn clean spring-boot:run
```

5) Confirm the config-server is working properly.

```bash
$ curl -i http://localhost:8888/greeting-config/default
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Fri, 21 Aug 2015 19:55:42 GMT
Server: Apache-Coyote/1.1
X-Application-Context: config-server:cloud:0
X-Cf-Requestid: fe5d6055-274f-405b-481b-3a4455e58c38
Content-Length: 308
Connection: keep-alive

{
  "name": "greeting-config",
  "profiles": [
    "default"
  ],
  "label": "master",
  "propertySources": []
}
```
This can also be done via the Chrome [JSON Formatter](https://chrome.google.com/webstore/detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) plug-in.

### `greeting-config` Setup

1) Review the following file: `$CLOUD_NATIVE_APP_LABS_HOME/geeting-config/pom.xml`
By adding `spring-cloud-starter-config` to the classpath this application will consume configuration from the config-server.  `greeting-config` is a config client.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

2) Review the `$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/resources/bootstrap.yml`

```yml
spring:
  application:
    name: greeting-config
```
Note there is no `spring.cloud.config.uri` defined. It defaults to `http://localhost:8888`.

3) Open a new terminal window.  Start the `greeting-config` application.

```bash
$ cd $CLOUD_NATIVE_APP_LABS_HOME/greeting-config
$ mvn clean spring-boot:run
```

4) Confirm the `greeting-config` app is working properly.  You should see a "Greetings!!!" message.

```bash
$ curl http://localhost:8080
<!DOCTYPE html>
<html>
 <body>
   <h1>Greetings!!!</h1>
 </body>
</html>
```

5) Stop the `config-server` and `greeting-config` applications


### Deploy the `config-server` and `greeting-config` apps to PWS
The exercises below can all be run locally, but we will deploy them to PWS.

1) Add the following to `$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/resources/bootstrap.yml`.

```yml
spring:
  application:
    name: greeting-config
  cloud:  # <-- ADD NEW SECTION
    config:
      uri: ${vcap.services.config-server.credentials.uri:http://localhost:8888}
```
When defining the `spring.cloud.config.uri` our app will first look for an environment variable (`vcap.services.config-server.credentials.uri`), if not present then try to connect to a local config-server.

2) Create a user provided service.  This is the environment variable our application will read when running in PWS.  Make sure to use your config-server uri not the literal below.

```bash
$ cf cups config-server -p uri
$ uri> http://config-server-sectarian-flasket.cfapps.io
```

3) Package and deploy the `config-server` to PWS:

```bash
$ mvn clean package
$ cf push config-server -p target/config-server-0.0.1-SNAPSHOT.jar -m 512M --random-route
```

4) Package the `greeting-config` application with Maven

```bash
$ mvn clean package
```

5) Deploy the `greeting-config` to PWS & bind services:

```bash
$ cf push greeting-config -p target/greeting-config-0.0.1-SNAPSHOT.jar -m 512M --random-route --no-start
$ cf bind-service greeting-config config-server
$ cf start greeting-config
```

### Changing Logging Levels
Logging levels are reset automatically when the environment changes.

1) Review the logout put when hitting the `/` endpoint
```bash
$ cf logs greeting-config
```
Nothing application specific.  Just log messages from the router.  How can we see application debug logs?

2) Review `$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/java/io/pivotal/greeting/GreetingController.java`
 ```java
@RequestMapping("/")
String getGreeting(Model model){

  logger.debug("Adding greeting");
  model.addAttribute("msg", "Greetings!!!");

  if(greetingProperties.isDisplayFortune()){
    logger.debug("Adding fortune");
    model.addAttribute("fortune", fortuneService.getFortune());
  }

  //resolves to the greeting.vm velocity template
  return "greeting";
}
```
We want to see these debug messages.

3) Review what the config-server is serving up.  Use your `config-server` url, not the literal below.

```bash
$ curl http://config-server-sectarian-flasket.cfapps.io/greeting-config/cloud
{
  "name": "greeting-config",
  "profiles": [
    "cloud"
  ],
  "label": "master",
  "propertySources": []
}
```

4) Edit your fork of the `app-config` repo.  Create a file called `greeting-config.yml`.  Add the content below to the file and push the changes back to GitHub.
```yml
logging:
  level:
    io:
      pivotal: DEBUG

greeting:
  displayFortune: false

quoteServiceURL: http://quote-service-dev.cfapps.io/quote
```

5) While tailing the application logs, refresh the `/` endpoint.  No changes in out application logs yet.

```
$ cf logs greeting-config
```

6) Review what the config-server is serving up.  Use your `config-server` url, not the literal below.

```bash
$ curl http://config-server-sectarian-flasket.cfapps.io/greeting-config/cloud
{
  "name": "greeting-config",
  "profiles": [
    "cloud"
  ],
  "label": "master",
  "propertySources": [
    {
      "name": "https://github.com/d4v3r/app-config.git/greeting-config.yml",
      "source": {
        "logging.level.io.pivotal": "DEBUG",
        "greeting.displayFortune": false,
        "quoteServiceURL": "http://quote-service-dev.cfapps.io/quote"
      }
    }
  ]
}
```
The value has changed!

7) Notify `greeting-config` app to pick up the new config by POSTing to the `/refresh` endpoint.  Make sure to use your url not the literal below and that you are tailing the application logs.

```bash
$ curl -X POST http://greeting-config-hypodermal-subcortex.cfapps.io/refresh
```

8) Refresh the `/` endpoint while tailing the logs.  You should see the debug line "Adding greeting"


### `@ConfigurationProperties`

`@ConfigurationProperties` are re-bound automatically when the environment changes.

1) Review `$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/java/io/pivotal/greeting/GreetingProperties.java` &
`$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/java/io/pivotal/greeting/GreetingController.java`
Note how the `greeting.displayFortune` is used to turn a feature on/off.
There are times when you want to turn features on/off on demand.  In this case, we want the fortune feature "on" with our greeting.  In this case, we will use `@ConfigurationProperties` to achieve this.

2) Edit your fork of the `app-config` repo.   Change `greeting.displayFortune` from `false` to `true` in the `greeting-config.yml` and push the changes back to GitHub.
```yml
logging:
  level:
    io:
      pivotal: DEBUG

greeting:
  displayFortune: true

quoteServiceURL: http://quote-service-dev.cfapps.io/quote
```
3) Notify `greeting-config` app to pick up the new config by POSTing to the `/refresh` endpoint.  Make sure to use your url not the literal below and that you are tailing the application logs.

```bash
$ curl -X POST http://greeting-config-hypodermal-subcortex.cfapps.io/refresh
```

4) Then refresh the `/` endpoint and see the fortune included.

### `@RefreshScope`
The `@ResfreshScope` annotation is used to recreate beans so they can pickup new config values.

1) Review `$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/java/io/pivotal/quote/QuoteController.java` & `$CLOUD_NATIVE_APP_LABS_HOME/greeting-config/src/main/java/io/pivotal/quote/QuoteService.java`.  `QuoteService.java` uses the `@RefreshScope` annotation.  In this case, we are using a third party service to get quotes.  We want to keep our environments aligned with the third party.  So we are going to override configuration values by profile (next section).

2) In your browser, hit the `/quote-of-the-day` endpoint.  
Note where the data is being served from: `http://quote-service-dev.cfapps.io/quote`

### Override Configuration Values By Profile
1) Set the active profile - qa

```bash
$ cf set-env greeting-config SPRING_PROFILES_ACTIVE qa
$ cf restart greeting-config
```
2) Make sure the profile is set by checking the `/env` endpoint.  Under profiles `qa` should be listed.  This can be done with curl or your browser.

```bash
$ curl -i http://greeting-config-hypodermal-subcortex.cfapps.io/env
```

3) In your fork of the `app-config` repository, create a new file: `greeting-config-qa.yml`. Fill it in with the following content:

```yml
quoteServiceURL: http://quote-service-qa.cfapps.io/quote
```
Make sure to commit and push to GitHub.

4) Refresh the application configuration values

```bash
$ curl -X POST http://greeting-config-hypodermal-subcortex.cfapps.io/refresh
```

5) Refresh the `/quote-of-the-day` endpoint.  Quotes are now being served from QA.

### Cloud Bus
Refreshing multiple instances can be a challenge by  hitting the `/refresh` endpoint for multiple app instances.

Cloud Bus allows for a pub/sub notification mechanism to refresh configuration.

1) Scale the number of config client instances to 3

```
$ cf scale greeting-config -i 3
```

2) Create a RabbitMQ service instance, bind it to `greeting-config`
```bash
$ cf cs cloudamqp lemur cloud-bus
$ cf bs greeting-config cloud-bus
$ cf restart greeting-config
```

3) Include the cloud bus dependency in the  `$CLOUD_NATIVE_APP_LABS_HOME/geeting-config/pom.xml`.  _You will need to paste this in your file._
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

4) Package the new artifact:
```bash
$ mvn clean package
```

5) Deploy the application
```bash
$ cf push greeting-config -p target/greeting-config-0.0.1-SNAPSHOT.jar
```

6) Observe the logs that are generated by refreshing the `/` endpoint several times in your browser.
```bash
$ cf logs greeting-config | grep GreetingController
```
All app instances are creating debug statements

7) Turn logging down.  In your fork of the `app-config` repo edit the `greeting-config.yml`
```yml
logging:
  level:
    io:
      pivotal: INFO
```

8) Nofify applications to pickup the change.  Send a POST to `/bus/refresh`
```bash
$ curl -X POST http://greeting-config-hypodermal-subcortex.cfapps.io/bus/refresh
```

9) Refresh the `/` endpoint several times in your browser.  No more logs!
