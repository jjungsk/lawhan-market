package kr.lawhan.market.notification;

/** Plain payload for an inquiry notification, decoupled from the inquiry/listing JPA entities. */
public record InquiryNotification(
        Long inquiryId,
        Long listingId,
        String listingTitle,
        String type,
        String company,
        String name,
        String email,
        String phone,
        Long priceHope,
        String content
) {
}
