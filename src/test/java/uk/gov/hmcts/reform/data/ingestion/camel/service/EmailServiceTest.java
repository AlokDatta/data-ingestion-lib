package uk.gov.hmcts.reform.data.ingestion.camel.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.EmailFailureException;
import uk.gov.hmcts.reform.data.ingestion.camel.model.Email;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    EmailService emailService;

    @Mock
    JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    public void setUp() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    private Email getEmail() {
        return Email.builder()
                .subject("Test mail")
                .from("no-reply@reform.hmcts.net")
                .to(Arrays.asList("example1@hmcts.net", "example2@hmcts.net"))
                .content("Test")
                .build();
    }

    @Test
    void testSendEmail() {
        doNothing().when(mailSender).send(any(MimeMessage.class));
        assertTrue(emailService.sendEmail(getEmail()));
    }

    @Test
    void testSendEmailException() {
        final Email email = getEmail();
        EmailFailureException emailFailureException = new EmailFailureException(new Throwable());
        doThrow(emailFailureException).when(mailSender).send(any(MimeMessage.class));
        assertThrows(EmailFailureException.class, () -> emailService
            .sendEmail(email));
    }

    @Test
    void testMailException() {
        final Email email = getEmail();
        MailException emailFailureException = mock(MailException.class);
        doThrow(emailFailureException).when(mailSender).send(any(MimeMessage.class));
        assertThrows(EmailFailureException.class, () -> emailService
            .sendEmail(email));
    }
}