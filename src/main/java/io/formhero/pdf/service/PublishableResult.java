package io.formhero.pdf.service;

import java.io.Serializable;

/**
 * Created by ryankimber on 2016-03-13.
 */
public interface PublishableResult extends Serializable
{
    public String getRequestId();
    public String getSessionId();
    public String getType();
}
