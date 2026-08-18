package kr.lawhan.market.listing.dto;

import kr.lawhan.market.listing.Listing;

import java.time.Instant;

public record ListingSummaryResponse(
        Long id,
        String title,
        String category,
        Long price,
        String status,
        String thumbnailUrl,
        Instant createdAt
) {

    public static ListingSummaryResponse from(Listing listing) {
        return new ListingSummaryResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getCategory(),
                listing.getPrice(),
                listing.getStatus(),
                listing.getThumbnailUrl(),
                listing.getCreatedAt()
        );
    }
}
