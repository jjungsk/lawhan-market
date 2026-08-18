package kr.lawhan.market.listing;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.lawhan.market.listing.dto.ListingDetailResponse;
import kr.lawhan.market.listing.dto.ListingSummaryResponse;
import kr.lawhan.market.listing.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingRepository listingRepository;

    public ListingController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @GetMapping
    public PageResponse<ListingSummaryResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<Listing> result = listingRepository.search(category, keyword, pageable);
        return PageResponse.of(result, ListingSummaryResponse::from);
    }

    @GetMapping("/{id}")
    public ListingDetailResponse detail(@PathVariable Long id) {
        Listing listing = listingRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "listing not found: " + id));
        return ListingDetailResponse.from(listing);
    }

    /** Same category, most recent first, capped at 4 (see {@link ListingRepository#findTop4ByCategoryAndIdNotOrderByCreatedAtDesc}). */
    @GetMapping("/{id}/related")
    public List<ListingSummaryResponse> related(@PathVariable Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "listing not found: " + id));

        return listingRepository.findTop4ByCategoryAndIdNotOrderByCreatedAtDesc(listing.getCategory(), id).stream()
                .map(ListingSummaryResponse::from)
                .toList();
    }

    private Sort resolveSort(String sort) {
        return switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "latest" -> Sort.by("createdAt").descending();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown sort: " + sort);
        };
    }
}
