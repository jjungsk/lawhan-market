package kr.lawhan.market.inquiry.dto;

import kr.lawhan.market.inquiry.Inquiry;

import java.time.Instant;

public record InquiryResponse(
        Long id,
        Long listingId,
        String type,
        String name,
        String email,
        String status,
        Instant createdAt
) {

    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getListingId(),
                inquiry.getType(),
                inquiry.getName(),
                inquiry.getEmail(),
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );
    }
}
