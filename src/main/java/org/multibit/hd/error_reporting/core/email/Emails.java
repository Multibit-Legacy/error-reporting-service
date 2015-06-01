package org.multibit.hd.error_reporting.core.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>Factory to provide the following to applications:</p>
 * <ul>
 * <li>Provision of simple SMTP based email over SSL/TLS</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class Emails {

  private static final Logger log = LoggerFactory.getLogger(Emails.class);

  private static final String SUBJECT_SUPPORT = "[Error-report]";
  private static final String SUBJECT_NO_REPLY = "Confirmation of delivery from Bitcoin Solutions - do not reply";

  /**
   * The response email template (the email address may be spoofed so cannot leak either name or message)
   */
  private static final String TEXT_RESPONSE = "Thank you for getting in touch.\n\n" +
    "We have received your message and we will get back to you as soon as possible.\n\n" +
    "Please do not reply to this message as it has been automatically generated.\n\n" +
    "If you did not expect this message, please contact our support department (support@bitcoin-solutions.co.uk) so that we can investigate the error.\n\n" +
    "Kind regards,\n\n" +
    "The Bitcoin Solutions support team";

  private static final String TO_SUPPORT = "support@bitcoin-solutions.co.uk";

  private static final String FROM_NO_REPLY = "noreply@bitcoin-solutions.co.uk";

  private static final String SMTP_USER = "noreply+bitcoin-solutions.co.uk";
  private static final String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD");

  /**
   * Sends an internal support email
   *
   * @param text    The internal text
   * @throws java.lang.IllegalStateException If something goes wrong
   */
  public static void sendSupportEmail(String text) {

    sendEmail(TO_SUPPORT, FROM_NO_REPLY, SUBJECT_SUPPORT, text);

  }

  /**
   * Sends an external do not reply email (usually for confirmation)
   *
   * @param to The recipient
   * @throws java.lang.IllegalStateException If something goes wrong
   */
  public static void sendNoReplyEmail(String to) {

    sendEmail(to, FROM_NO_REPLY, SUBJECT_NO_REPLY, TEXT_RESPONSE);

  }

  /**
   * Sends a generic email. Made private to encourage use of dedicated factory methods.
   *
   * @param to      The recipient
   * @param from    The Reply-To (e.g. "noreply@example.org")
   * @param subject The subject line
   * @param text    The internal text
   */
  private static void sendEmail(final String to, final String from, final String subject, final String text) {

    // SSL/TLS not available without certificate import
    final Properties props = new Properties();
    props.put("mail.smtp.host", "just26.justhost.com");
    props.put("mail.smtp.port", "26");
    props.put("mail.smtp.auth", "true");

    final Session mailSession = Session.getDefaultInstance(props,
      new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
        }
      }
    );

    final InternetAddress fromAddress;
    final InternetAddress toAddress;
    try {

      fromAddress = new InternetAddress(from);
      toAddress = new InternetAddress(to);
    } catch (AddressException e) {
      throw new IllegalStateException(e);
    }

    // Sending an email could take forever if SMTP fails so we use a separate thread
    // with a timeout
    ExecutorService service = Executors.newSingleThreadExecutor();
    Future result = service.submit(new Runnable() {

      @Override
      public void run() {
        try {

          log.debug("Preparing to send email. To '{}', from '{}' ", toAddress, fromAddress);

          // Configure the message
          final Message message = new MimeMessage(mailSession);
          message.setFrom(fromAddress);
          message.setRecipient(RecipientType.TO, toAddress);
          message.setSubject(subject);
          message.setText(text);

          // This could block forever if SMTP fails
          // (ideally we want a message queue to a separate email service)
          Transport.send(message);

          log.debug("Email sent successfully");

        } catch (MessagingException e) {
          throw new IllegalStateException(e);
        }
      }
    });

    try {
      result.get(20, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new IllegalStateException(e);
    } finally {
      service.shutdownNow();
    }

  }

}