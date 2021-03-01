# Spring Boot Starter AWS Lambda

This starter project provides auto-configuration support classes to easily invoke _AWS Lambda Functions_ from a Spring Boot application.

* Configuration property classes for externalizing AWS credentials as IAM access tokens
* Automatic authenticated session management for securely invoking Lambda functions
* Provides a Spring Boot friendly config adapter for easily registering Lambda function invocation interfaces

## Usage

In your Spring Boot project, add the starter project dependency to your class path. For Maven, add the following dependency to your `pom.xml`.

```xml
<dependencies>
    <dependency>
        <groupId>org.kbastani</groupId>
        <artifactId>spring-boot-starter-aws-lambda</artifactId>
        <version>${spring-boot-starter-aws-lambda.version}</version>
    </dependency>
    
    ...
</dependencies>
```

Next, inject your AWS IAM credentials safely into the `application.properties|yaml` file for your Spring Boot application. The snippet below shows an example of how to source the access credentials from the environment.

```yaml
spring:
  profiles: development
server:
  port: 8081
amazon:
  aws:
    access-key-id: ${AWS_ACCESS_KEY_ID}
    access-key-secret: ${AWS_ACCESS_KEY_SECRET}
```

You can also set the properties using command line arguments with the Maven Spring Boot plugin, shown in the snippet below.

```bash
$ mvn spring-boot:run -Drun.arguments="--amazon.aws.access-key-id=ABCDEFG,--amazon.aws.access-key-secret=ZYXKGFWG"
```

You can now begin to invoke AWS Lambda functions from your AWS account. The next thing you'll need to do is to define an interface of lambda function references to invoke.

```java
public interface LambdaFunctions {
    
    @LambdaFunction(functionName="account-created-13P0EDGLDE399", logType = LogType.Tail)
    Account accountCreated(AccountEvent event);

    @LambdaFunction(functionName="account-activated-1P0I6FTFCMHKH", logType = LogType.Tail)
    Account accountActivated(AccountEvent event);
}
```

To start invoking your AWS Lambda functions, you can define a new bean that creates a proxy instance of your lambda interface.

```java
@Configuration
public class AwsLambdaConfig {

    @Bean
    public LambdaFunctions lambdaFunctions(AWSLambdaConfigurerAdapter configurerAdapter) {
        return configurerAdapter.getFunctionInstance(LambdaFunctions.class);
    }
}
```

In the example above, we inject the auto-configured `AWSLambdaConfigurerAdapter` dependency from `spring-boot-starter-aws-lambda` into a new Spring bean definition named `lambdaFunctions`. The configurer adapter will create an instance of an interface that contains `@LambdaFunction` annotated methods—like in the example snippet for the `LambdaFunctions` interface.

We can now inject the `LambdaFunctions` as a dependency into other Spring components in our application in order to easily invoke remote Lambda functions on AWS.
 
 ```java
 @Service
 public class AccountService {
    
    final private LambdaFunctions lambdaFunctions;
    
    public AccountService(LambdaFunctions lambdaFunctions) {
        this.lambdaFunctions = lambdaFunctions;
    }
    
    public Account createAccount(Account account) {
        // Trigger the new event by invoking AWS lambda
        Account result = lambdaFunctions
            .accountCreated(new AccountEvent(account, EventType.ACCOUNT_CREATED));
            
        return result;
    }
 }
 ```