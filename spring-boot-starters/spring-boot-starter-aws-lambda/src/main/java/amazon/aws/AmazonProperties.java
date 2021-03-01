package amazon.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a configuration properties model for authenticating with AWS.
 *
 * @author kbastani
 */
@Configuration
@ConfigurationProperties(prefix = "amazon")
public class AmazonProperties {

    @NestedConfigurationProperty
    private Aws aws;

    /**
     * A property group for Amazon Web Service (AWS) configurations
     *
     * @return a property group for AWS configurations
     */
    public Aws getAws() {
        return aws;
    }

    /**
     * A property group for Amazon Web Service (AWS) configurations
     *
     * @param aws is a property group for AWS configurations
     */
    public void setAws(Aws aws) {
        this.aws = aws;
    }

    /**
     * A property group for Amazon Web Service (AWS) configurations
     */
    public static class Aws {

        private String accessKeyId;
        private String accessKeySecret;

        /**
         * A valid AWS account's access key id.
         *
         * @return an AWS access key id
         */
        public String getAccessKeyId() {
            return accessKeyId;
        }

        /**
         * A valid AWS account's access key id.
         *
         * @param accessKeyId is a valid AWS account's access key id.
         */
        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        /**
         * A valid AWS account's secret access token.
         *
         * @return an AWS account's secret access key
         */
        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        /**
         * A valid AWS account's secret access token.
         *
         * @param accessKeySecret is a valid AWS account's secret access token.
         */
        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        @Override
        public String toString() {
            return "Aws{" +
                    "accessKeyId='" + accessKeyId + '\'' +
                    ", accessKeySecret='" + accessKeySecret + '\'' +
                    '}';
        }
    }
}
