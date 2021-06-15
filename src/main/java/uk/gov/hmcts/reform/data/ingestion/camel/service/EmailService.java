package uk.gov.hmcts.reform.data.ingestion.camel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.EmailFailureException;
import uk.gov.hmcts.reform.data.ingestion.camel.model.Email;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

/**
 * This EmailServiceImpl send emails to intended recipients for failure cases
 * with detailed reason of failure.
 *
 * @since 2020-10-27
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Value("${logging-component-name:data_ingestion}")
    private String logComponentName;

    public boolean sendEmail(Email email) {
        try {
            final var message = mailSender.createMimeMessage();
            var mimeMsgHelperObj = new MimeMessageHelper(message, true);
            mimeMsgHelperObj.setTo(populateEmailAddresses(email.getTo()));
            mimeMsgHelperObj.setCc(populateEmailAddresses(email.getCc()));
            mimeMsgHelperObj.setBcc(populateEmailAddresses(email.getBcc()));
            mimeMsgHelperObj.setSubject(email.getSubject());
            mimeMsgHelperObj.setText(email.getContent());
            mimeMsgHelperObj.setFrom(email.getFrom());

            mailSender.send(mimeMsgHelperObj.getMimeMessage());
            return true;

        } catch (MailException | MessagingException e) {
            log.error("{}:: Exception  while  sending mail  {}", logComponentName, getStackTrace(e));
            throw new EmailFailureException(e);
        }
    }

    private String[] populateEmailAddresses(List<String> toAddresses) {
        Optional<List<String>> toAddressesOptional = Optional.ofNullable(toAddresses);
        return toAddressesOptional.isPresent()
                ? toAddresses.toArray(String[]::new)
                : new String[0];
    }

}
