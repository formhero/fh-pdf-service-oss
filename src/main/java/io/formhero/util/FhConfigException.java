package io.formhero.util;

/**
 * Created by ryankimber on 2016-03-11.
 */
public class FhConfigException extends Exception
{
    public FhConfigException() {
        super();
    }

    public FhConfigException(String message) {
        super(message);
    }

    public FhConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public FhConfigException(Throwable cause) {
        super(cause);
    }

    protected FhConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
