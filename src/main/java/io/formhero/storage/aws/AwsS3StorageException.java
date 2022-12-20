package io.formhero.storage.aws;

import io.formhero.storage.StorageException;

/**
 * Created by ryan.kimber on 2017-10-03.
 */
public class AwsS3StorageException extends StorageException {
    public AwsS3StorageException() {
    }

    public AwsS3StorageException(String message) {
        super(message);
    }

    public AwsS3StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public AwsS3StorageException(Throwable cause) {
        super(cause);
    }
}
