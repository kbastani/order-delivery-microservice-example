package amazon.aws;

import com.amazonaws.auth.*;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Provides a configurer for invoking remote AWS Lambda functions using {@link LambdaInvokerFactory}.
 * This component also manages the authenticated session for an IAM user that has provided valid
 * access keys to access AWS resources.
 *
 * @author kbastani
 */
@Component
public class AWSLambdaConfigurerAdapter {

    private String accessKeyId;
    private String accessKeySecret;
    private Credentials sessionCredentials;

    /**
     * Create a new instance of the {@link AWSLambdaConfigurerAdapter} with the bucket name and access credentials
     *
     * @param accessKeyId     is the access key id credential for the specified bucket name
     * @param accessKeySecret is the access key secret for the specified bucket name
     */
    public AWSLambdaConfigurerAdapter(String accessKeyId,
                                      String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
    }

    /**
     * Creates a proxy instance of a supplied interface that contains methods annotated with
     * {@link LambdaFunction}. Provides automatic credential support to authenticate with an IAM
     * access keys using {@link BasicSessionCredentials} auto-configured from Spring Boot
     * configuration properties in {@link AmazonProperties}.
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getFunctionInstance(Class<T> type) {
        return LambdaInvokerFactory.builder()
                .lambdaClient(AWSLambdaClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(
                                getBasicSessionCredentials()))
                        .build())
                .build(type);
    }

    /**
     * Get the basic session credentials for the template's configured IAM authentication keys
     *
     * @return a {@link BasicSessionCredentials} instance with a valid authenticated session token
     */
    private BasicSessionCredentials getBasicSessionCredentials() {

        // Create a new session token if the session is expired or not initialized
        if (sessionCredentials == null || sessionCredentials.getExpiration().before(new Date()))
            sessionCredentials = getSessionCredentials();

        // Create basic session credentials using the generated session token
        return new BasicSessionCredentials(sessionCredentials.getAccessKeyId(),
                sessionCredentials.getSecretAccessKey(),
                sessionCredentials.getSessionToken());
    }

    /**
     * Creates a new session credential that is valid for 12 hours
     *
     * @return an authenticated {@link Credentials} for the new session token
     */
    private Credentials getSessionCredentials() {
        // Create a new session with the user credentials for the service instance
        AWSSecurityTokenServiceClient stsClient =
                new AWSSecurityTokenServiceClient(new BasicAWSCredentials(accessKeyId, accessKeySecret));

        // Start a new session for managing a service instance's bucket
        GetSessionTokenRequest getSessionTokenRequest =
                new GetSessionTokenRequest().withDurationSeconds(43200);

        // Get the session token for the service instance's bucket
        sessionCredentials = stsClient.getSessionToken(getSessionTokenRequest).getCredentials();

        return sessionCredentials;
    }
}
