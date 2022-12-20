package io.formhero.params;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import io.formhero.aws.AwsCredentials;
import io.formhero.util.FhConfigException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Ec2Params implements ServiceParams {
    @Getter
    private String environmentOwner;

    @Getter
    private String environment;

    private final String partialPath;

    private boolean initialized = false;

    private final Map<String, String> ec2ParamMap = new HashMap<>();

    public Ec2Params(String environmentOwner, String environment) throws FhConfigException {
        this.environmentOwner = environmentOwner;
        this.environment = environment;

        if (this.environmentOwner == null) this.environmentOwner = "FormHero";
        if (this.environment == null) this.environment = "test";

        this.partialPath = "/" + this.environmentOwner + "/" + this.environment + "/";
    }

    private AWSSimpleSystemsManagement loadAwsClient() throws FhConfigException {
        if (System.getenv("AWS_CONFIGURATION") != null) {
            AwsCredentials credentials = AwsCredentials.loadFromFile(System.getenv("AWS_CONFIGURATION"));
            AWSStaticCredentialsProvider awsCredentials = credentials.getAwsCredentials();
            AWSSimpleSystemsManagementClientBuilder ssmClientBuilder = AWSSimpleSystemsManagementClient.builder();
            ssmClientBuilder.withCredentials(awsCredentials);
            ssmClientBuilder.setRegion(credentials.getRegion());

            return ssmClientBuilder.build();
        } else {
            //Rely on the container or environment to provide the credentials.
            return AWSSimpleSystemsManagementClientBuilder.defaultClient();
        }
    }

    private void initialize() throws FhConfigException {
        AWSSimpleSystemsManagement client = loadAwsClient();
        GetParametersByPathRequest ssmRequest= new GetParametersByPathRequest();
        ssmRequest.withPath("/" + this.environmentOwner + "/" + this.environment);
        ssmRequest.setRecursive(true);
        ssmRequest.setWithDecryption(true);
        ssmRequest.setMaxResults(10);

        GetParametersByPathResult result = client.getParametersByPath(ssmRequest);
        while (result != null) {
            for (Parameter param : result.getParameters()) {
                ec2ParamMap.put(param.getName(), param.getValue());
            }

            if (result.getNextToken() != null) {
                ssmRequest= new GetParametersByPathRequest();
                ssmRequest.withPath("/" + this.environmentOwner + "/" + this.environment);
                ssmRequest.setRecursive(true);
                ssmRequest.setWithDecryption(true);
                ssmRequest.setMaxResults(10);
                ssmRequest.setNextToken(result.getNextToken());
                result = client.getParametersByPath(ssmRequest);
            } else {
                result = null;
            }
        }

        initialized = true;
    }

    @Override
    public String get(String key, String defaultValue) {
        if (!initialized) {
            try {
                this.initialize();
            } catch (FhConfigException e) {
                return null;
            }
        }

        String value = ec2ParamMap.get(this.partialPath + key);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    @Override
    public String get(String key) {
        return get(key, null);
    }
}
