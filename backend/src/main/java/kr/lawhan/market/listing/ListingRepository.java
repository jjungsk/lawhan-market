package kr.lawhan.market.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("""
            SELECT l FROM Listing l
            WHERE (:category IS NULL OR l.category = :category)
              AND (:keyword = ''
                   OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(l.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Listing> search(@Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.images WHERE l.id = :id")
    Optional<Listing> findDetailById(@Param("id") Long id);

    List<Listing> findTop4ByCategoryAndIdNotOrderByCreatedAtDesc(String category, Long id);
}
