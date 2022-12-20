package io.formhero.pdf.email;

import io.formhero.pdf.service.requests.Destination;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by ryankimber on 2016-03-15.
 */
@Getter
@Setter
public class EmailDestination extends Destination
{
    private String subject;
    private String textMessage;
    private String htmlMessage;

    private EmailRecipient from;
    private EmailRecipient replyTo;
    private List<String> attachments;
    private List<EmailRecipient> recipients;
    private List<EmailRecipient> ccRecipients;
    private List<EmailRecipient> bccRecipients;
}
