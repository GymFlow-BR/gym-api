package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.AuthenticatedUserResponse;
import br.com.gymflow.api.auth.dto.LoginRequest;
import br.com.gymflow.api.auth.dto.LoginResult;
import br.com.gymflow.api.auth.dto.RegisterOrganizationRequest;
import br.com.gymflow.api.auth.dto.RegisterOrganizationResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gymflow.api.auth.dto.ChangePasswordRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @PostMapping("/register")
    public ResponseEntity<RegisterOrganizationResponse> register(
            @RequestBody @Valid RegisterOrganizationRequest request
    ) {
        RegisterOrganizationResponse response = authService.registerOrganization(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult loginResult = authService.login(request);

        authCookieService.addAuthCookie(response, loginResult.token());

        return ResponseEntity.ok(loginResult.user());
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me() {
        return ResponseEntity.ok(authService.getAuthenticatedUser());
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        authService.changePassword(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);

        return ResponseEntity.noContent().build();
    }
}