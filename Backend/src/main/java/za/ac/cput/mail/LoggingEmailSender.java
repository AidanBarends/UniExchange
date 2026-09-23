/*
 LoggingEmailSender.java

 Development fallback: writes the email to the log instead of sending it.

 Registered by MailConfig only when no SmtpEmailSender exists (i.e. when
 spring.mail.host is unset). Logs at WARN so the code is easy to find in the
 console and so nobody mistakes this for production behaviour.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.warn("""

                ======================= EMAIL (NOT SENT) =======================
                To      : {}
                Subject : {}
                ----------------------------------------------------------------
                {}
                ================================================================""",
                to, subject, body);
    }

}
