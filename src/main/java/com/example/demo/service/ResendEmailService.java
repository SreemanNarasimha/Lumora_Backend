package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResendEmailService {

    private final WebClient webClient;
    
    @Value("${resend.api-key}")
    private String apiKey;
    
    @Value("${resend.from-email}")
    private String fromEmail;

    public ResendEmailService(WebClient resendWebClient) {
        this.webClient = resendWebClient;
    }

    public void sendOtpEmail(String to, String otp) {
        Map<String, Object> request = new HashMap<>();
        request.put("from", fromEmail);
        request.put("to", List.of(to));
        request.put("subject", "Lumora • Your Password Reset Code");
        request.put("html", buildHtmlTemplate(otp));

        webClient.post()
            .uri("/emails")
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private String buildHtmlTemplate(String otp) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset=\"UTF-8\">" +
               "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
               "</head>" +
               "<body style=\"margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #fcfbf9; color: #333333;\">" +
               "  <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #fcfbf9; padding: 40px 0;\">" +
               "    <tr>" +
               "      <td align=\"center\">" +
               "        <table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden;\">" +
               "          <tr>" +
               "            <td style=\"padding: 40px 40px 20px 40px; text-align: center; border-bottom: 1px solid #f0eee6;\">" +
               "              <h1 style=\"margin: 0; font-size: 28px; font-weight: 300; letter-spacing: 2px; color: #8c7e6c; text-transform: uppercase;\">LUMORA</h1>" +
               "            </td>" +
               "          </tr>" +
               "          <tr>" +
               "            <td style=\"padding: 40px;\">" +
               "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #555555;\">Hello,</p>" +
               "              <p style=\"margin: 0 0 30px 0; font-size: 16px; line-height: 1.6; color: #555555;\">We received a request to reset the password for your Lumora account. Please use the verification code below to proceed.</p>" +
               "              <div style=\"text-align: center; margin: 30px 0;\">" +
               "                <span style=\"display: inline-block; padding: 15px 30px; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #333333; background-color: #f9f8f5; border: 1px solid #e5e0d8; border-radius: 4px;\">" +
               "                  " + otp +
               "                </span>" +
               "              </div>" +
               "              <p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.5; color: #888888; text-align: center;\">This code is valid for <strong>5 minutes</strong>.</p>" +
               "              <p style=\"margin: 0; font-size: 14px; line-height: 1.5; color: #999999; text-align: center;\">If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>" +
               "            </td>" +
               "          </tr>" +
               "          <tr>" +
               "            <td style=\"padding: 20px 40px; background-color: #fcfbf9; text-align: center;\">" +
               "              <p style=\"margin: 0; font-size: 12px; color: #aaaaaa;\">&copy; " + java.time.Year.now().getValue() + " Lumora. All rights reserved.</p>" +
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
