package io.formhero.util;

/**
 * Created by ryankimber on 2016-03-15.
 */
public class FhCacheException extends Exception {

    public FhCacheException() {
        super();
    }

    public FhCacheException(String message) {
        super(message);
    }

    public FhCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public FhCacheException(Throwable cause) {
        super(cause);
    }

    protected FhCacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
