package amazon.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class auto-configures a {@link AWSLambdaConfigurerAdapter} bean.
 *
 * @author kbastani
 */
@Configuration
@ConditionalOnMissingBean(AWSLambdaConfigurerAdapter.class)
@EnableConfigurationProperties(AmazonProperties.class)
public class AmazonAutoConfiguration {

    @Autowired
    private AmazonProperties amazonProperties;

    @Bean
    protected AWSLambdaConfigurerAdapter lambdaAdapter() {
        return new AWSLambdaConfigurerAdapter(
                amazonProperties.getAws().getAccessKeyId(),
                amazonProperties.getAws().getAccessKeySecret());
    }
}
