package br.com.gymflow.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must have at least 6 characters")
        String newPassword,

        @NotBlank(message = "Confirm new password is required")
        String confirmNewPassword
) {
}