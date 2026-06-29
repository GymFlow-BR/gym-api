package br.com.gymflow.api.controller;

import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.dto.user.CreateUserRequest;
import br.com.gymflow.api.dto.user.UserResponse;
import br.com.gymflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gymflow.api.dto.user.UpdateUserRequest;

import java.util.List;

@Tag(
        name = "Users",
        description = "Endpoints para criação, listagem e gerenciamento de usuários"
)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Criar usuário",
            description = "Cria um novo usuário vinculado a uma organização."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @RequestBody @Valid CreateUserRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Listar usuários",
            description = "Retorna todos os usuários cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários encontrados")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> response = userService.findAll();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar usuários por organização",
            description = "Retorna todos os usuários vinculados a uma organização específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários encontrados"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @GetMapping("/by-organization/{organizationId}")
    public ResponseEntity<List<UserResponse>> findAllByOrganizationId(
            @PathVariable Long organizationId
    ) {
        List<UserResponse> response = userService.findAllByOrganizationId(organizationId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar usuários por organização e perfil",
            description = "Retorna usuários de uma organização filtrando por perfil."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários encontrados"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @GetMapping("/by-organization/{organizationId}/by-role")
    public ResponseEntity<List<UserResponse>> findAllByOrganizationIdAndRole(
            @PathVariable Long organizationId,
            @RequestParam UserRole role
    ) {
        List<UserResponse> response = userService.findAllByOrganizationIdAndRole(organizationId, role);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza parcialmente os dados de um usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patch(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        UserResponse response = userService.patch(id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Inativar usuário",
            description = "Inativa um usuário, alterando seu status para inactive."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário inativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna um usuário específico pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable Long id
    ) {
        UserResponse response = userService.findById(id);

        return ResponseEntity.ok(response);
    }
}