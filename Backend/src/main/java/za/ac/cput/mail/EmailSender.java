/*
 EmailSender.java

 Outbound email abstraction. Two implementations exist and exactly one is active:

   SmtpEmailSender    - when spring.mail.host is configured, sends for real.
   LoggingEmailSender - otherwise, writes the message to the log.

 The logging fallback is deliberate, not a stub. It means every teammate can run
 and test the full signup + OTP flow without SMTP credentials, and the test suite
 never sends mail to a real inbox.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.mail;

public interface EmailSender {

    void send(String to, String subject, String body);

}
