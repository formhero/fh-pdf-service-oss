package io.formhero.storage.gae;

import io.formhero.storage.StorageException;

/**
 * Created by ryankimber on 2015-11-19.
 */
public class GoogleCloudStorageException extends StorageException
{
    public GoogleCloudStorageException() {
    }

    public GoogleCloudStorageException(String message) {
        super(message);
    }

    public GoogleCloudStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoogleCloudStorageException(Throwable cause) {
        super(cause);
    }

}

