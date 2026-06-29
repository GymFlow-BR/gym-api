package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.LoginResponse;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnOkWhenLoginRequestIsValid() throws Exception {
        LoginResponse response = new LoginResponse(
                "jwt-token",
                1L,
                100L,
                "Professor Dev",
                "teacher.dev@gymflow.com",
                UserRole.TEACHER
        );;

        Mockito.when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "teacher.dev@gymflow.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.organizationId").value(100L))
                .andExpect(jsonPath("$.name").value("Professor Dev"))
                .andExpect(jsonPath("$.email").value("teacher.dev@gymflow.com"))
                .andExpect(jsonPath("$.role").value("TEACHER"));

        Mockito.verify(authService).login(any());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "",
                              "password": "123456"
                            }
                            """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "invalid-email",
                              "password": "123456"
                            }
                            """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "teacher.dev@gymflow.com",
                              "password": ""
                            }
                            """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "teacher.dev@gymflow.com",
                              "password": "123456"
                            """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(authService);
    }
}