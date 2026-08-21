package br.com.gymflow.api.security;

import br.com.gymflow.api.auth.JwtService;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.repository.OrganizationRepository;
import br.com.gymflow.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private OrganizationRepository organizationRepository;

    @Test
    void shouldBlockPrivateEndpointWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/workouts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/workouts"));
    }

    @Test
    void shouldBlockPrivateEndpointWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/workouts")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/workouts"));
    }

    @Test
    void shouldAllowTeacherToAccessExercisesEndpoint() throws Exception {
        User teacher = createUser(1L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldBlockStudentFromAccessingExercisesEndpoint() throws Exception {
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/exercises"));
    }

    @Test
    void shouldAllowStudentToAccessStudentsGetEndpoint() throws Exception {
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", student.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockStudentFromPostingToStudentsEndpoint() throws Exception {
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(post("/api/students/{studentId}/workouts", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "workoutId": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/students/" + student.getId() + "/workouts"));
    }

    @Test
    void shouldBlockStudentFromPatchingStudentsEndpoint() throws Exception {
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(patch("/api/students/{studentId}/workouts/{studentWorkoutId}", student.getId(), 1L)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldBlockStudentFromDeletingStudentsEndpoint() throws Exception {
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(delete("/api/students/{studentId}/workouts/{studentWorkoutId}", student.getId(), 1L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessUsersGetEndpoint() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldBlockTeacherFromAccessingUsersGetEndpoint() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void shouldBlockStudentFromAccessingUsersGetEndpoint() throws Exception {
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowTeacherToAccessUsersPostEndpoint() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email",
                                  "password": "123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBlockStudentFromAccessingUsersPostEndpoint() throws Exception {
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Aluno Teste",
                                  "email": "student.test@gymflow.com",
                                  "password": "123456",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessUsersPatchEndpoint() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        mockMvc.perform(patch("/api/users/{id}", 2L)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowTeacherToAccessUsersPatchEndpoint() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(patch("/api/users/{id}", 3L)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Aluno Atualizado"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockStudentFromAccessingUsersPatchEndpoint() throws Exception {
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(patch("/api/users/{id}", 3L)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Aluno Atualizado"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessUsersDeleteEndpoint() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        mockMvc.perform(delete("/api/users/{id}", 999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockTeacherFromAccessingUsersDeleteEndpoint() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(delete("/api/users/{id}", 3L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldBlockStudentFromAccessingUsersDeleteEndpoint() throws Exception {
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(delete("/api/users/{id}", 3L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldBlockStudentFromAccessingAnotherStudentCurrentWorkoutEndpoint() throws Exception {
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(student);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", 99L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/students/99/workouts/current"));
    }

    @Test
    void shouldAllowAdminToAccessStudentCurrentWorkoutEndpointFromSameOrganization() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);

        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", student.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowTeacherToAccessStudentCurrentWorkoutEndpointFromSameOrganization() throws Exception {
        User teacher = createUser(1L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);

        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", student.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockTeacherFromAccessingStudentCurrentWorkoutEndpointFromAnotherOrganization() throws Exception {
        User teacher = createUser(1L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);

        Organization anotherOrganization = new Organization();
        anotherOrganization.setId(2L);
        anotherOrganization.setOrganizationName("Another Organization");
        student.setOrganization(anotherOrganization);

        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", student.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/students/" + student.getId() + "/workouts/current"));
    }

    @Test
    void shouldBlockAdminFromAccessingStudentCurrentWorkoutEndpointFromAnotherOrganization() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        User student = createUser(2L, "student.dev@gymflow.com", UserRole.STUDENT);

        Organization anotherOrganization = new Organization();
        anotherOrganization.setId(2L);
        anotherOrganization.setOrganizationName("Another Organization");
        student.setOrganization(anotherOrganization);

        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", student.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value(
                        "/api/students/" + student.getId() + "/workouts/current"
                ));
    }

    @Test
    void shouldAllowAdminToCreateTeacherUser() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("teacher.new@gymflow.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            return savedUser;
        });
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(admin.getOrganization()));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Professor Novo",
                                  "email": "teacher.new@gymflow.com",
                                  "password": "123456",
                                  "role": "TEACHER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.organizationId").value(1L))
                .andExpect(jsonPath("$.name").value("Professor Novo"))
                .andExpect(jsonPath("$.email").value("teacher.new@gymflow.com"))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldAllowTeacherToCreateStudentUser() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(userRepository.existsByEmail("student.new@gymflow.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(11L);
            return savedUser;
        });
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(teacher.getOrganization()));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Aluno Novo",
                                  "email": "student.new@gymflow.com",
                                  "password": "123456",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.organizationId").value(1L))
                .andExpect(jsonPath("$.name").value("Aluno Novo"))
                .andExpect(jsonPath("$.email").value("student.new@gymflow.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldBlockTeacherFromCreatingTeacherUser() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Professor Indevido",
                                  "email": "teacher.invalid@gymflow.com",
                                  "password": "123456",
                                  "role": "TEACHER"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void shouldBlockTeacherFromCreatingAdminUser() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Admin Indevido",
                                  "email": "admin.invalid@gymflow.com",
                                  "password": "123456",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void shouldBlockUnauthenticatedUserFromCreatingUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Aluno Teste",
                                  "email": "student.test@gymflow.com",
                                  "password": "123456",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void shouldAllowTeacherToPatchStudentUserWithoutChangingActive() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(userRepository.existsByEmailAndIdNot("student.updated@gymflow.com", student.getId())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/users/{id}", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Aluno Atualizado",
                                  "email": "student.updated@gymflow.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.name").value("Aluno Atualizado"))
                .andExpect(jsonPath("$.email").value("student.updated@gymflow.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void shouldBlockTeacherFromPatchingStudentActiveStatus() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        mockMvc.perform(patch("/api/users/{id}", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users/" + student.getId()));
    }

    @Test
    void shouldBlockTeacherFromPatchingTeacherUser() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        User anotherTeacher = createUser(4L, "another.teacher@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(userRepository.findById(anotherTeacher.getId())).thenReturn(Optional.of(anotherTeacher));

        mockMvc.perform(patch("/api/users/{id}", anotherTeacher.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Professor Atualizado"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users/" + anotherTeacher.getId()));
    }

    @Test
    void shouldAllowTeacherToFindUsersByRoleStudent() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        User student = createUser(3L, "student.dev@gymflow.com", UserRole.STUDENT);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(teacher.getOrganization()));
        when(userRepository.findByOrganizationIdAndRole(1L, UserRole.STUDENT)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/users/by-organization/{organizationId}/by-role", 1L)
                        .param("role", "STUDENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(student.getId()))
                .andExpect(jsonPath("$[0].role").value("STUDENT"));
    }

    @Test
    void shouldAllowAdminToFindUsersByRoleTeacher() throws Exception {
        User admin = createUser(1L, "admin.dev@gymflow.com", UserRole.ADMIN);
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);

        String token = jwtService.generateToken(admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(admin.getOrganization()));
        when(userRepository.findByOrganizationIdAndRole(1L, UserRole.TEACHER)).thenReturn(List.of(teacher));

        mockMvc.perform(get("/api/users/by-organization/{organizationId}/by-role", 1L)
                        .param("role", "TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(teacher.getId()))
                .andExpect(jsonPath("$[0].organizationId").value(1L))
                .andExpect(jsonPath("$[0].organizationName").value("GymFlow Academy Dev"))
                .andExpect(jsonPath("$[0].email").value("teacher.dev@gymflow.com"))
                .andExpect(jsonPath("$[0].role").value("TEACHER"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldBlockTeacherFromFindingUsersByRoleTeacher() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(teacher.getOrganization()));

        mockMvc.perform(get("/api/users/by-organization/{organizationId}/by-role", 1L)
                        .param("role", "TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users/by-organization/1/by-role"));
    }

    @Test
    void shouldBlockTeacherFromFindingUsersByRoleAdmin() throws Exception {
        User teacher = createUser(2L, "teacher.dev@gymflow.com", UserRole.TEACHER);
        String token = jwtService.generateToken(teacher);

        when(userRepository.findByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(teacher.getOrganization()));

        mockMvc.perform(get("/api/users/by-organization/{organizationId}/by-role", 1L)
                        .param("role", "ADMIN")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/users/by-organization/1/by-role"));
    }

    private User createUser(Long id, String email, UserRole role) {
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setOrganizationName("GymFlow Academy Dev");

        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setRole(role);
        user.setActive(true);
        user.setOrganization(organization);

        return user;
    }
}