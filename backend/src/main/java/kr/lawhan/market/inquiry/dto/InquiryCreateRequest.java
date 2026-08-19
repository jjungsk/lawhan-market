package kr.lawhan.market.inquiry.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InquiryCreateRequest(
        @NotBlank(message = "type is required")
        @Pattern(regexp = "매수|라이센싱|기타", message = "type must be one of 매수, 라이센싱, 기타")
        String type,

        String company,

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "phone is required")
        String phone,

        Long priceHope,

        @NotBlank(message = "content is required")
        String content,

        @AssertTrue(message = "agree must be true")
        boolean agree
) {
}
