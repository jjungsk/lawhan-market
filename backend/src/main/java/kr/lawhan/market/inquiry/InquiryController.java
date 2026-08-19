package kr.lawhan.market.inquiry;

import jakarta.validation.Valid;
import kr.lawhan.market.inquiry.dto.InquiryCreateRequest;
import kr.lawhan.market.inquiry.dto.InquiryResponse;
import kr.lawhan.market.listing.Listing;
import kr.lawhan.market.listing.ListingRepository;
import kr.lawhan.market.notification.InquiryNotification;
import kr.lawhan.market.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/listings/{listingId}/inquiries")
public class InquiryController {

    private static final Logger log = LoggerFactory.getLogger(InquiryController.class);

    private final InquiryRepository inquiryRepository;
    private final ListingRepository listingRepository;
    private final NotificationSender notificationSender;

    public InquiryController(InquiryRepository inquiryRepository, ListingRepository listingRepository,
            NotificationSender notificationSender) {
        this.inquiryRepository = inquiryRepository;
        this.listingRepository = listingRepository;
        this.notificationSender = notificationSender;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryResponse create(@PathVariable Long listingId, @Valid @RequestBody InquiryCreateRequest request) {
        // listingRepository is scoped to non-soft-deleted listings (see Listing's @SQLRestriction),
        // so a deleted listing's id resolves here to empty just like a nonexistent one.
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "listing not found: " + listingId));

        Inquiry inquiry = new Inquiry(
                listingId,
                request.type(),
                request.company(),
                request.name(),
                request.email(),
                request.phone(),
                request.priceHope(),
                request.content(),
                Instant.now()
        );
        inquiry = inquiryRepository.save(inquiry);

        sendNotificationBestEffort(listing, inquiry);

        return InquiryResponse.from(inquiry);
    }

    /**
     * DB 저장은 이미 성공했으므로 사용자에게는 201을 그대로 반환한다. 알림 발송 실패는
     * 서버 로그에 ERROR로만 남기고 요청을 실패시키지 않는다(재시도 큐는 이번 범위 아님 — 백로그 참고).
     */
    private void sendNotificationBestEffort(Listing listing, Inquiry inquiry) {
        InquiryNotification notification = new InquiryNotification(
                inquiry.getId(),
                listing.getId(),
                listing.getTitle(),
                inquiry.getType(),
                inquiry.getCompany(),
                inquiry.getName(),
                inquiry.getEmail(),
                inquiry.getPhone(),
                inquiry.getPriceHope(),
                inquiry.getContent()
        );
        try {
            notificationSender.sendInquiryReceived(notification);
        } catch (Exception e) {
            log.error("Failed to send admin notification for inquiry id={}", inquiry.getId(), e);
        }
    }
}
