package kr.lawhan.market.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * prod 전용 구현체 — AWS SES로 실제 이메일을 발송한다. 자격증명은 SDK 기본 자격증명 체인
 * (환경변수/EC2 인스턴스 프로파일 등)에서 가져오며, 이번 범위에서는 실제 SES 계정/자격증명
 * 설정은 하지 않는다(§3 인프라 — 배포 단계에서 구성).
 */
@Component
@Profile("prod")
public class SesNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SesNotificationSender.class);

    private final SesClient sesClient;
    private final String senderEmail;
    private final String adminEmail;

    public SesNotificationSender(
            @Value("${app.notification.sender-email}") String senderEmail,
            @Value("${app.notification.admin-email}") String adminEmail) {
        this.sesClient = SesClient.builder().region(Region.AP_NORTHEAST_2).build();
        this.senderEmail = senderEmail;
        this.adminEmail = adminEmail;
    }

    @Override
    public void sendInquiryReceived(InquiryNotification n) {
        String subject = "[lawhan-market] 새 문의 접수 - " + n.listingTitle();
        String body = """
                매물: [%d] %s
                유형: %s
                문의자: %s <%s>%s
                연락처: %s
                희망 가격: %s

                문의 내용:
                %s
                """.formatted(
                n.listingId(), n.listingTitle(),
                n.type(),
                n.name(), n.email(), n.company() != null && !n.company().isBlank() ? " / " + n.company() : "",
                n.phone() != null ? n.phone() : "-",
                n.priceHope() != null ? n.priceHope() : "-",
                n.content() != null ? n.content() : "-"
        );

        SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(adminEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder().text(Content.builder().data(body).charset("UTF-8").build()).build())
                        .build())
                .build();

        sesClient.sendEmail(request);
        log.info("SES notification sent for inquiry id={} to {}", n.inquiryId(), adminEmail);
    }
}
