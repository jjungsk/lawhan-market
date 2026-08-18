package kr.lawhan.market.listing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import kr.lawhan.market.user.User;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * listings.deleted_at IS NULL is enforced here via {@link SQLRestriction} so
 * every query issued through this entity (findById, JPQL, derived queries)
 * automatically excludes soft-deleted rows — callers cannot forget the
 * filter. Admin screens that need to see deleted listings (M5) will need a
 * separate query path that bypasses this restriction (e.g. a native query),
 * since this entity is intentionally "public view only".
 */
@Entity
@Table(name = "listings")
@SQLRestriction("deleted_at IS NULL")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    private Long price;

    @Column(nullable = false)
    private String status;

    @Column(name = "app_number")
    private String appNumber;

    @Column(name = "reg_number")
    private String regNumber;

    private String summary;

    private String content;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "listing", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ListingImage> images = new ArrayList<>();

    protected Listing() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public Long getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getAppNumber() {
        return appNumber;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ListingImage> getImages() {
        return images;
    }
}
