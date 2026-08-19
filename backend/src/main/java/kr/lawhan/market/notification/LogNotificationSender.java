package kr.lawhan.market.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** local/dev 대체 구현체 — 실제 SES 호출 없이 발송될 내용을 로그로 남긴다. prod에서는 {@link SesNotificationSender}가 대신 활성화된다. */
@Component
@Profile("!prod")
public class LogNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationSender.class);

    @Override
    public void sendInquiryReceived(InquiryNotification n) {
        String message = """
                [알림] 관리자에게 문의 접수 이메일 발송 (local/dev 프로파일 — 실제 SES 미호출, 로그로 대체)
                문의 ID: %d / 매물 ID: %d (%s)
                유형: %s
                문의자: %s <%s>%s
                연락처: %s
                희망 가격: %s
                문의 내용: %s
                """.formatted(
                n.inquiryId(), n.listingId(), n.listingTitle(),
                n.type(),
                n.name(), n.email(), n.company() != null && !n.company().isBlank() ? " / " + n.company() : "",
                n.phone() != null ? n.phone() : "-",
                n.priceHope() != null ? n.priceHope() : "-",
                n.content() != null ? n.content() : "-"
        );
        log.info(message);
    }
}
