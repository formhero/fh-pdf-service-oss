package io.formhero.storage.azure;

import io.formhero.storage.StorageException;

/**
 * Created by stuart.thompson on 2021-10-11.
 */
public class AzureBlobStorageException extends StorageException {
    public AzureBlobStorageException() {
    }

    public AzureBlobStorageException(String message) {
        super(message);
    }

    public AzureBlobStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public AzureBlobStorageException(Throwable cause) {
        super(cause);
    }
}
