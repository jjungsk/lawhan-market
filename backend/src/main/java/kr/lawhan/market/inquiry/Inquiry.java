package kr.lawhan.market.inquiry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(nullable = false)
    private String type;

    private String company;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "price_hope")
    private Long priceHope;

    private String content;

    @Column(name = "agreed_at")
    private Instant agreedAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Inquiry() {
    }

    public Inquiry(Long listingId, String type, String company, String name, String email,
            String phone, Long priceHope, String content, Instant agreedAt) {
        this.listingId = listingId;
        this.type = type;
        this.company = company;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.priceHope = priceHope;
        this.content = content;
        this.agreedAt = agreedAt;
        this.status = "신규";
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getListingId() {
        return listingId;
    }

    public String getType() {
        return type;
    }

    public String getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getPriceHope() {
        return priceHope;
    }

    public String getContent() {
        return content;
    }

    public Instant getAgreedAt() {
        return agreedAt;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
