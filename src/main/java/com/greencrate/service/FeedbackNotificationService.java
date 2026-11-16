package com.greencrate.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class FeedbackNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String emailUser;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Value("${FEEDBACK_NOTIFICATION_EMAIL:}")
    private String notificationEmail;

    @Value("${DEFAULT_FROM_EMAIL:}")
    private String fromEmail;

    public FeedbackNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(Map<String, Object> feedback) {
        if (emailUser.isBlank() || emailPassword.isBlank()
                || emailUser.startsWith("your-") || emailPassword.startsWith("your-")) {
            logger.info("Skipping feedback email because SMTP credentials are not configured");
            return;
        }
        String recipient = notificationEmail.isBlank() ? emailUser : notificationEmail;
        int rating = ((Number) feedback.get("rating")).intValue();
        String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail.isBlank() ? emailUser : fromEmail);
        message.setTo(recipient);
        message.setSubject("[GreenCrate] New Customer Feedback (" + rating
                + " Stars) from " + feedback.get("email"));
        message.setText("""
                Hello Admin,

                You have received new customer feedback on GreenCrate!

                Customer Email : %s
                Rating         : %d/5 (%s)
                Submitted At   : %s

                Feedback Comments:
                %s

                Live Dashboard : http://localhost:8081/admin-feedback.html
                """.formatted(feedback.get("email"), rating, stars, feedback.get("createdAt"),
                feedback.get("comments") == null ? "No additional comments provided."
                        : feedback.get("comments")));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.warn("Could not send feedback notification email", exception);
        }
    }
}
