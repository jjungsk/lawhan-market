package kr.lawhan.market.listing.dto;

import kr.lawhan.market.listing.Listing;
import kr.lawhan.market.listing.ListingImage;

import java.time.Instant;
import java.util.List;

public record ListingDetailResponse(
        Long id,
        String title,
        String category,
        Long price,
        String status,
        String appNumber,
        String regNumber,
        String summary,
        String content,
        String thumbnailUrl,
        List<String> images,
        Instant createdAt,
        Instant updatedAt
) {

    public static ListingDetailResponse from(Listing listing) {
        List<String> imageUrls = listing.getImages().stream()
                .map(ListingImage::getImageUrl)
                .toList();

        return new ListingDetailResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getCategory(),
                listing.getPrice(),
                listing.getStatus(),
                listing.getAppNumber(),
                listing.getRegNumber(),
                listing.getSummary(),
                listing.getContent(),
                listing.getThumbnailUrl(),
                imageUrls,
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
