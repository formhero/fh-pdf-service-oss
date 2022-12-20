package io.formhero.storage;

import java.io.IOException;

/**
 * Created by ryan.kimber on 2017-10-03.
 */
public class StorageException extends IOException
{
    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }
}
