package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name}")
    private String senderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationOtp(String to, String otp) {
        String subject = "Lumora • Your Verification Code";
        String messageBody = "We received a request to register your Lumora account. Please use the verification code below to proceed.";
        sendHtmlEmail(to, subject, messageBody, otp);
    }

    public void sendForgotPasswordOtp(String to, String otp) {
        String subject = "Lumora • Your Password Reset Code";
        String messageBody = "We received a request to reset the password for your Lumora account. Please use the verification code below to proceed.";
        sendHtmlEmail(to, subject, messageBody, otp);
    }

    private void sendHtmlEmail(String to, String subject, String messageBody, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildLuxuryHtmlTemplate(messageBody, otp), true);

            mailSender.send(message);
            System.out.println("SMTP Status: Successfully sent OTP email to " + to + " at " + java.time.LocalDateTime.now());
        } catch (MailException e) {
            System.err.println("SMTP Error at " + java.time.LocalDateTime.now() + " - Recipient: " + to + " - Message: " + e.getMessage());
            throw new RuntimeException("Unable to send verification email. Please try again.");
        } catch (Exception e) {
            System.err.println("Unexpected Error at " + java.time.LocalDateTime.now() + " - Recipient: " + to + " - Message: " + e.getMessage());
            throw new RuntimeException("Unable to send verification email. Please try again.");
        }
    }

    private String buildLuxuryHtmlTemplate(String messageBody, String otp) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset=\"UTF-8\">" +
               "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
               "</head>" +
               "<body style=\"margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #FAF8F4; color: #333333;\">" +
               "  <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #FAF8F4; padding: 40px 0;\">" +
               "    <tr>" +
               "      <td align=\"center\">" +
               "        <table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #E8DCC8;\">" +
               "          <tr>" +
               "            <td style=\"padding: 40px 40px 20px 40px; text-align: center; border-bottom: 1px solid #E8DCC8;\">" +
               "              <h1 style=\"margin: 0; font-size: 28px; font-weight: 300; letter-spacing: 2px; color: #333333; text-transform: uppercase;\">LUMORA</h1>" +
               "            </td>" +
               "          </tr>" +
               "          <tr>" +
               "            <td style=\"padding: 40px;\">" +
               "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #333333;\">Hello,</p>" +
               "              <p style=\"margin: 0 0 30px 0; font-size: 16px; line-height: 1.6; color: #333333;\">" + messageBody + "</p>" +
               "              <div style=\"text-align: center; margin: 30px 0;\">" +
               "                <span style=\"display: inline-block; padding: 15px 30px; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #333333; background-color: #FAF8F4; border: 1px solid #C8A86B; border-radius: 8px;\">" +
               "                  " + otp +
               "                </span>" +
               "              </div>" +
               "              <p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.5; color: #555555; text-align: center;\">This code is valid for <strong>5 minutes</strong>.</p>" +
               "              <p style=\"margin: 0; font-size: 14px; line-height: 1.5; color: #555555; text-align: center;\">If you did not request this email, please ignore it or contact support if you have concerns.</p>" +
               "            </td>" +
               "          </tr>" +
               "          <tr>" +
               "            <td style=\"padding: 20px 40px; background-color: #FAF8F4; text-align: center;\">" +
               "              <p style=\"margin: 0; font-size: 12px; color: #888888;\">&copy; " + java.time.Year.now().getValue() + " Lumora. All rights reserved.</p>" +
               "            </td>" +
               "          </tr>" +
               "        </table>" +
               "      </td>" +
               "    </tr>" +
               "  </table>" +
               "</body>" +
               "</html>";
    }
}
