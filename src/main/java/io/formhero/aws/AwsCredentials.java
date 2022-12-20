package io.formhero.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.formhero.util.FhConfigException;
import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * Created by ryan.kimber on 2018-03-27.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwsCredentials implements Serializable {

    private String accessKeyId;
    private String secretAccessKey;
    private String region;

    public AwsCredentials()
    {

    }

    public static AwsCredentials loadFromFile(String path) throws FhConfigException
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Trying to load " + (new File(path)).getAbsolutePath());
            InputStream is = new FileInputStream(new File(path).getAbsolutePath());
            AwsCredentials fhCreds = mapper.readValue(is, AwsCredentials.class);
            return fhCreds;
        }
        catch(IOException ioe)
        {
            throw new FhConfigException("Unable to read AwsCredentials from file " + path + ": ", ioe);
        }
    }

    public AWSStaticCredentialsProvider getAwsCredentials()
    {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(this.getAccessKeyId(), this.getSecretAccessKey());
        return new AWSStaticCredentialsProvider(awsCreds);
    }
}
