package com.school.inventary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name must contain only letters, spaces, hyphens, and dots")
    private String name;

    @NotBlank(message = "School name is required")
    @Size(min = 2, max = 150, message = "School name must be between 2 and 150 characters")
    private String schoolName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must contain exactly 10 digits without alphabets, spaces, or special characters")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email format")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    private Integer role;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;
}
