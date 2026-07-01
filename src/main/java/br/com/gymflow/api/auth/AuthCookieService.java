package br.com.gymflow.api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    @Value("${app.security.cookie.name}")
    private String cookieName;

    @Value("${app.security.cookie.secure}")
    private boolean secure;

    @Value("${app.security.cookie.max-age-seconds}")
    private int maxAgeSeconds;

    @Value("${app.security.cookie.same-site}")
    private String sameSite;

    public void addAuthCookie(HttpServletResponse response, String token) {
        String cookie = "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=%s%s"
                .formatted(
                        cookieName,
                        token,
                        maxAgeSeconds,
                        sameSite,
                        secure ? "; Secure" : ""
                );

        response.addHeader("Set-Cookie", cookie);
    }

    public void clearAuthCookie(HttpServletResponse response) {
        String cookie = "%s=; Path=/; Max-Age=0; HttpOnly; SameSite=%s%s"
                .formatted(
                        cookieName,
                        sameSite,
                        secure ? "; Secure" : ""
                );

        response.addHeader("Set-Cookie", cookie);
    }

    public String extractTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}