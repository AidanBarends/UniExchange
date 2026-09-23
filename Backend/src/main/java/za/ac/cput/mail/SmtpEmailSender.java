/*
 SmtpEmailSender.java

 Real SMTP delivery. Only registered when spring.mail.host is set, which is what
 lets LoggingEmailSender take over in development.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.mail.host")
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender,
                           @Value("${app.otp.from:no-reply@uniexchange.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(this.from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            this.mailSender.send(message);
            log.info("Sent '{}' to {}", subject, to);
        }
        catch (MailException ex) {
            // Surfaced to the caller so registration can report a real failure
            // rather than leaving the student waiting for a code that never comes.
            log.error("Failed to send '{}' to {}", subject, to, ex);
            throw ex;
        }
    }

}
