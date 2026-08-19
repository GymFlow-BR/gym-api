package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.AuthenticatedUserResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.auth.dto.LoginResult;
import br.com.gymflow.api.auth.dto.RegisterOrganizationResponse;
import br.com.gymflow.api.domain.enums.OrganizationType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthCookieService authCookieService;

    @Test
    void shouldReturnCreatedWhenRegisterRequestIsValid() throws Exception {
        RegisterOrganizationResponse response = new RegisterOrganizationResponse(
                1L,
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                10L,
                "Samuel Gomes",
                "samuel@gymflowacademy.com"
        );

        Mockito.when(authService.registerOrganization(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "organizationName": "GymFlow Academy",
                              "organizationType": "ACADEMY",
                              "organizationEmail": "contato@gymflowacademy.com",
                              "organizationPhone": "11999999999",
                              "adminName": "Samuel Gomes",
                              "adminEmail": "samuel@gymflowacademy.com",
                              "password": "123456"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizationId").value(1L))
                .andExpect(jsonPath("$.organizationName").value("GymFlow Academy"))
                .andExpect(jsonPath("$.organizationType").value("ACADEMY"))
                .andExpect(jsonPath("$.adminUserId").value(10L))
                .andExpect(jsonPath("$.adminName").value("Samuel Gomes"))
                .andExpect(jsonPath("$.adminEmail").value("samuel@gymflowacademy.com"));

        Mockito.verify(authService).registerOrganization(any());
        Mockito.verifyNoInteractions(authCookieService);
    }

    @Test
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "organizationName": "",
                              "organizationType": "ACADEMY",
                              "organizationEmail": "invalid-email",
                              "adminName": "",
                              "adminEmail": "invalid-email",
                              "password": "123"
                            }
                            """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(authService);
        Mockito.verifyNoInteractions(authCookieService);
    }

    @Test
    void shouldReturnOkWhenLoginRequestIsValid() throws Exception {
        AuthenticatedUserResponse userResponse = new AuthenticatedUserResponse(
                1L,
                100L,
                "Professor Dev",
                "teacher.dev@gymflow.com",
                UserRole.TEACHER
        );

        LoginResult loginResult = new LoginResult(
                "jwt-token",
                userResponse
        );

        Mockito.when(authService.login(any())).thenReturn(loginResult);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "teacher.dev@gymflow.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.organizationId").value(100L))
                .andExpect(jsonPath("$.name").value("Professor Dev"))
                .andExpect(jsonPath("$.email").value("teacher.dev@gymflow.com"))
                .andExpect(jsonPath("$.role").value("TEACHER"));

        Mockito.verify(authService).login(any());
        Mockito.verify(authCookieService).addAuthCookie(any(), Mockito.eq("jwt-token"));
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

    @Test
    void shouldReturnAuthenticatedUserWhenMeEndpointIsCalled() throws Exception {
        AuthenticatedUserResponse userResponse = new AuthenticatedUserResponse(
                1L,
                100L,
                "Professor Dev",
                "teacher.dev@gymflow.com",
                UserRole.TEACHER
        );

        Mockito.when(authService.getAuthenticatedUser()).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.organizationId").value(100L))
                .andExpect(jsonPath("$.name").value("Professor Dev"))
                .andExpect(jsonPath("$.email").value("teacher.dev@gymflow.com"))
                .andExpect(jsonPath("$.role").value("TEACHER"));

        Mockito.verify(authService).getAuthenticatedUser();
    }

    @Test
    void shouldClearAuthCookieWhenLogoutIsCalled() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());

        Mockito.verify(authCookieService).clearAuthCookie(any());
        Mockito.verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnNoContentWhenChangePasswordRequestIsValid() throws Exception {
        doNothing().when(authService).changePassword(any());

        mockMvc.perform(patch("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "currentPassword": "123456",
                          "newPassword": "654321",
                          "confirmNewPassword": "654321"
                        }
                        """))
                .andExpect(status().isNoContent());

        Mockito.verify(authService).changePassword(any());
        Mockito.verifyNoInteractions(authCookieService);
    }

    @Test
    void shouldReturnBadRequestWhenChangePasswordRequestIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "currentPassword": "",
                          "newPassword": "123",
                          "confirmNewPassword": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(authService);
        Mockito.verifyNoInteractions(authCookieService);
    }

    @Test
    void shouldReturnBadRequestWhenCurrentPasswordIsInvalid() throws Exception {
        doThrow(new BusinessRuleException("Current password is invalid"))
                .when(authService)
                .changePassword(any());

        mockMvc.perform(patch("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "currentPassword": "wrong-password",
                          "newPassword": "654321",
                          "confirmNewPassword": "654321"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Current password is invalid"));

        Mockito.verify(authService).changePassword(any());
        Mockito.verifyNoInteractions(authCookieService);
    }

    @Test
    void shouldReturnBadRequestWhenNewPasswordConfirmationDoesNotMatch() throws Exception {
        doThrow(new BusinessRuleException("New password confirmation does not match"))
                .when(authService)
                .changePassword(any());

        mockMvc.perform(patch("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "currentPassword": "123456",
                          "newPassword": "654321",
                          "confirmNewPassword": "different"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("New password confirmation does not match"));

        Mockito.verify(authService).changePassword(any());
        Mockito.verifyNoInteractions(authCookieService);
    }
}