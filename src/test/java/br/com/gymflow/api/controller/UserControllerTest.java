package br.com.gymflow.api.controller;

import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.dto.user.UserResponse;
import br.com.gymflow.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreateUserAndReturnCreated() throws Exception {
        UserResponse response = createUserResponse();

        Mockito.when(userService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aluno Teste",
                                  "email": "student.test@gymflow.com",
                                  "password": "123456",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.organizationId").value(1L))
                .andExpect(jsonPath("$.organizationName").value("GymFlow Academy Dev"))
                .andExpect(jsonPath("$.name").value("Aluno Teste"))
                .andExpect(jsonPath("$.email").value("student.test@gymflow.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        Mockito.verify(userService).create(any());
    }

    @Test
    void shouldFindAllUsersAndReturnOk() throws Exception {
        UserResponse response = createUserResponse();

        Mockito.when(userService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].organizationId").value(1L))
                .andExpect(jsonPath("$[0].organizationName").value("GymFlow Academy Dev"))
                .andExpect(jsonPath("$[0].name").value("Aluno Teste"))
                .andExpect(jsonPath("$[0].email").value("student.test@gymflow.com"))
                .andExpect(jsonPath("$[0].role").value("STUDENT"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0]", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$[0]", not(hasKey("password"))));

        Mockito.verify(userService).findAll();
    }

    @Test
    void shouldFindUserByIdAndReturnOk() throws Exception {
        UserResponse response = createUserResponse();

        Mockito.when(userService.findById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.organizationId").value(1L))
                .andExpect(jsonPath("$.name").value("Aluno Teste"))
                .andExpect(jsonPath("$.email").value("student.test@gymflow.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        Mockito.verify(userService).findById(2L);
    }

    @Test
    void shouldPatchUserAndReturnOk() throws Exception {
        UserResponse response = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Aluno Atualizado",
                "student.updated@gymflow.com",
                UserRole.STUDENT,
                true,
                LocalDateTime.of(2026, 6, 26, 10, 0)
        );

        Mockito.when(userService.patch(eq(2L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/users/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aluno Atualizado",
                                  "email": "student.updated@gymflow.com",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Aluno Atualizado"))
                .andExpect(jsonPath("$.email").value("student.updated@gymflow.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        Mockito.verify(userService).patch(eq(2L), any());
    }

    @Test
    void shouldDeleteUserAndReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 2L))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).delete(2L);
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aluno Teste",
                                  "email": "invalid-email",
                                  "password": "123456",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aluno Teste",
                                  "email": "student.test@gymflow.com",
                                  "password": "123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserRoleIsMissing() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aluno Teste",
                                  "email": "student.test@gymflow.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldReturnBadRequestWhenPatchUserEmailIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/users/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    private UserResponse createUserResponse() {
        return new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Aluno Teste",
                "student.test@gymflow.com",
                UserRole.STUDENT,
                true,
                LocalDateTime.of(2026, 6, 26, 10, 0)
        );
    }
}