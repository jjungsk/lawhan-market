package kr.lawhan.market.notification;

/**
 * Abstraction over "notify the admin that something happened". Local/dev
 * profiles bind {@link LogNotificationSender} (logs instead of sending);
 * prod binds {@link SesNotificationSender} (real AWS SES call).
 */
public interface NotificationSender {

    void sendInquiryReceived(InquiryNotification notification);
}
