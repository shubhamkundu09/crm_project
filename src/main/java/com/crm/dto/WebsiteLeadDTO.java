// WebsiteLeadDTO.java
package com.crm.dto;

import com.crm.entity.MainService;
import com.crm.entity.ServiceSubcategory;
import com.crm.entity.ServiceSubSubcategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteLeadDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    private MainService interestedService;
    private ServiceSubcategory serviceSubcategory;
    private ServiceSubSubcategory serviceSubSubcategory;
    private String serviceDescription;
    private String remarks;
}
