package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.AuthenticatedUserResponse;
import br.com.gymflow.api.auth.dto.LoginRequest;
import br.com.gymflow.api.auth.dto.LoginResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);

        return ResponseEntity.noContent().build();
    }
}