/*
 MailConfig.java

 Chooses how outbound email leaves the application.

 SmtpEmailSender registers itself when spring.mail.host is set. When it is not,
 the fallback below takes over and logs each message instead. That is what lets
 the whole signup + OTP flow be run and tested with no mail credentials at all.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import za.ac.cput.mail.EmailSender;
import za.ac.cput.mail.LoggingEmailSender;

@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    EmailSender fallbackEmailSender() {
        log.warn("No spring.mail.host configured - verification emails will be logged, not sent.");
        return new LoggingEmailSender();
    }

}
