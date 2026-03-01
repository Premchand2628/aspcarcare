package com.carwash.mailnotification.service;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.carwash.mailnotification.dto.EmailRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingEmail(EmailRequest request) {
        try {
            String htmlContent = buildEmail(request);
            sendEmail(request.getToEmail(), getSubject(request.getAction()), htmlContent);
            logger.info("Email sent successfully to: {} for action: {}", request.getToEmail(), request.getAction());
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", request.getToEmail(), e);
        }
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(subject);
        try {
			helper.setFrom("noreply@aspcarcare.com", "ASP Car Care");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String getSubject(String action) {
        if (action == null) return "Booking Update - ASP Car Care";
        
        switch (action.trim().toUpperCase()) {
            case "BOOKED":
                return "Booking Confirmed - ASP Car Care ✓";
            case "RESCHEDULED":
                return "Booking Rescheduled - ASP Car Care 📅";
            case "CANCELLED":
                return "Booking Cancelled - ASP Car Care ✕";
            case "UPGRADED":
                return "Booking Upgraded - ASP Car Care 🚀";
            default:
                return "Booking Update - ASP Car Care";
        }
    }

    private String buildEmail(EmailRequest request) {
        if (request == null || request.getAction() == null) {
            return buildDefaultEmail(request);
        }
        
        switch (request.getAction().trim().toUpperCase()) {
            case "BOOKED":
                return buildBookingConfirmationEmail(request);
            case "RESCHEDULED":
                return buildRescheduleEmail(request);
            case "CANCELLED":
                return buildCancellationEmail(request);
            case "UPGRADED":
                return buildUpgradeEmail(request);
            default:
                return buildDefaultEmail(request);
        }
    }

    private String buildBookingConfirmationEmail(EmailRequest request) {
        String amount = request.getAmount() != null ? String.format("%.2f", request.getAmount()) : "0.00";
        
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial; line-height: 1.6; color: #333; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #5E4DB2 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".header p { margin: 5px 0 0 0; opacity: 0.9; }" +
                ".content { padding: 30px; }" +
                ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                ".details-box { background: #f9f9f9; border-left: 4px solid #5E4DB2; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e0e0e0; }" +
                ".detail-row:last-child { border-bottom: none; }" +
                ".detail-label { font-weight: 600; color: #5E4DB2; }" +
                ".detail-value { color: #555; text-align: right; }" +
                ".message { color: #666; line-height: 1.8; margin: 20px 0; }" +
                ".cta-button { display: inline-block; background: #5E4DB2; color: white; padding: 14px 32px; border-radius: 8px; text-decoration: none; margin-top: 20px; font-weight: 600; }" +
                ".footer { background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #e0e0e0; }" +
                ".emoji { font-size: 24px; margin-bottom: 10px; }" +
                "</style>" +
                "</head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='emoji'>✓</div>" +
                "<h1>Booking Confirmed!</h1>" +
                "<p>Your car wash is all set</p>" +
                "</div>" +
                "<div class='content'>" +
                "<p class='greeting'>Hi <strong>" + (request.getFirstName() != null ? request.getFirstName() : "Customer") + "</strong>,</p>" +
                "<p class='message'>Your car wash booking has been successfully confirmed. We're excited to serve you!</p>" +
                "<div class='details-box'>" +
                "<div class='detail-row'><span class='detail-label'>Booking ID</span><span class='detail-value'>#" + request.getBookingId() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Wash Type</span><span class='detail-value'>" + request.getWashType() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Car</span><span class='detail-value'>" + request.getCarNumber() + " (" + request.getCarType() + ")</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Date</span><span class='detail-value'>" + request.getBookingDate() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Time</span><span class='detail-value'>" + request.getTimeSlot() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Amount</span><span class='detail-value'><strong>₹" + amount + "</strong></span></div>" +
                "</div>" +
                "<p class='message'>Please arrive 5 minutes before your scheduled time. Our team will be ready to give your car the best care!</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Thank you for choosing ASP Car Care! 🚗</p>" +
                "<p>&copy; 2026 ASP Car Care. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildRescheduleEmail(EmailRequest request) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial; line-height: 1.6; color: #333; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #FF8C00 0%, #FFB347 100%); color: white; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { padding: 30px; }" +
                ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                ".details-box { background: #f9f9f9; border-left: 4px solid #FF8C00; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e0e0e0; }" +
                ".detail-row:last-child { border-bottom: none; }" +
                ".detail-label { font-weight: 600; color: #FF8C00; }" +
                ".detail-value { color: #555; text-align: right; }" +
                ".message { color: #666; line-height: 1.8; margin: 20px 0; }" +
                ".footer { background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #e0e0e0; }" +
                ".emoji { font-size: 24px; margin-bottom: 10px; }" +
                "</style>" +
                "</head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='emoji'>📅</div>" +
                "<h1>Booking Rescheduled</h1>" +
                "<p>Your new date and time are confirmed</p>" +
                "</div>" +
                "<div class='content'>" +
                "<p class='greeting'>Hi <strong>" + (request.getFirstName() != null ? request.getFirstName() : "Customer") + "</strong>,</p>" +
                "<p class='message'>Your car wash booking has been successfully rescheduled to the new date and time below:</p>" +
                "<div class='details-box'>" +
                "<div class='detail-row'><span class='detail-label'>Booking ID</span><span class='detail-value'>#" + request.getBookingId() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Wash Type</span><span class='detail-value'>" + request.getWashType() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Car</span><span class='detail-value'>" + request.getCarNumber() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'><strong>NEW Date</strong></span><span class='detail-value'><strong>" + request.getBookingDate() + "</strong></span></div>" +
                "<div class='detail-row'><span class='detail-label'><strong>NEW Time</strong></span><span class='detail-value'><strong>" + request.getTimeSlot() + "</strong></span></div>" +
                "</div>" +
                "<p class='message'>We look forward to serving you at your new scheduled time. Thank you for your flexibility!</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Thank you for choosing ASP Car Care!</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildCancellationEmail(EmailRequest request) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial; line-height: 1.6; color: #333; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #FF6B6B 0%, #FF8A80 100%); color: white; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { padding: 30px; }" +
                ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                ".details-box { background: #f9f9f9; border-left: 4px solid #FF6B6B; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e0e0e0; }" +
                ".detail-row:last-child { border-bottom: none; }" +
                ".detail-label { font-weight: 600; color: #FF6B6B; }" +
                ".detail-value { color: #555; text-align: right; }" +
                ".message { color: #666; line-height: 1.8; margin: 20px 0; }" +
                ".cta-button { display: inline-block; background: #5E4DB2; color: white; padding: 12px 28px; border-radius: 8px; text-decoration: none; margin-top: 15px; font-weight: 600; }" +
                ".footer { background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #e0e0e0; }" +
                ".emoji { font-size: 24px; margin-bottom: 10px; }" +
                "</style>" +
                "</head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='emoji'>✕</div>" +
                "<h1>Booking Cancelled</h1>" +
                "<p>We'll miss you!</p>" +
                "</div>" +
                "<div class='content'>" +
                "<p class='greeting'>Hi <strong>" + (request.getFirstName() != null ? request.getFirstName() : "Customer") + "</strong>,</p>" +
                "<p class='message'>Your car wash booking has been cancelled. Here are the details:</p>" +
                "<div class='details-box'>" +
                "<div class='detail-row'><span class='detail-label'>Booking ID</span><span class='detail-value'>#" + request.getBookingId() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Wash Type</span><span class='detail-value'>" + request.getWashType() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Original Date</span><span class='detail-value'>" + request.getBookingDate() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Refund Status</span><span class='detail-value'>Processing</span></div>" +
                "</div>" +
                "<p class='message'>If you'd like to rebook or have any questions, feel free to reach out to us. We're always here to help!</p>" +
                "<a href='https://yourapp.com/services' class='cta-button'>Book Again</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>We hope to see you soon! 🚗</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildUpgradeEmail(EmailRequest request) {
        String amount = request.getAmount() != null ? String.format("%.2f", request.getAmount()) : "0.00";
        
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial; line-height: 1.6; color: #333; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%); color: white; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { padding: 30px; }" +
                ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                ".details-box { background: #f9f9f9; border-left: 4px solid #4CAF50; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e0e0e0; }" +
                ".detail-row:last-child { border-bottom: none; }" +
                ".detail-label { font-weight: 600; color: #4CAF50; }" +
                ".detail-value { color: #555; text-align: right; }" +
                ".upgrade-highlight { background: #C8E6C9; padding: 15px; border-radius: 6px; margin: 15px 0; text-align: center; }" +
                ".upgrade-highlight strong { color: #2E7D32; font-size: 16px; }" +
                ".message { color: #666; line-height: 1.8; margin: 20px 0; }" +
                ".footer { background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #e0e0e0; }" +
                ".emoji { font-size: 24px; margin-bottom: 10px; }" +
                "</style>" +
                "</head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='emoji'>🚀</div>" +
                "<h1>Booking Upgraded!</h1>" +
                "<p>Your car will get premium treatment</p>" +
                "</div>" +
                "<div class='content'>" +
                "<p class='greeting'>Hi <strong>" + (request.getFirstName() != null ? request.getFirstName() : "Customer") + "</strong>,</p>" +
                "<p class='message'>Great choice! Your booking has been successfully upgraded to premium service.</p>" +
                "<div class='details-box'>" +
                "<div class='detail-row'><span class='detail-label'>Booking ID</span><span class='detail-value'>#" + request.getBookingId() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Car</span><span class='detail-value'>" + request.getCarNumber() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Original Service</span><span class='detail-value'>" + request.getOriginalWashType() + "</span></div>" +
                "<div class='upgrade-highlight'>" +
                "<strong>✓ Upgraded to: " + request.getUpgradedWashType() + "</strong>" +
                "</div>" +
                "<div class='detail-row'><span class='detail-label'>Date</span><span class='detail-value'>" + request.getBookingDate() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>Time</span><span class='detail-value'>" + request.getTimeSlot() + "</span></div>" +
                "<div class='detail-row'><span class='detail-label'>New Amount</span><span class='detail-value'><strong>₹" + amount + "</strong></span></div>" +
                "</div>" +
                "<p class='message'>Your car will receive the premium treatment it deserves! See you soon.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Thank you for choosing ASP Car Care!</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildDefaultEmail(EmailRequest request) {
        String bookingId = request != null && request.getBookingId() != null ? request.getBookingId().toString() : "N/A";
        return "<html><body><h1>Booking Update</h1><p>Your booking ID: " + bookingId + " has been updated.</p></body></html>";
    }
}