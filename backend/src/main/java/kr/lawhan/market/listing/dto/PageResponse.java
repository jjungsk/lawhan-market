package kr.lawhan.market.listing.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> items, long total, int page, int size) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        List<T> items = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
