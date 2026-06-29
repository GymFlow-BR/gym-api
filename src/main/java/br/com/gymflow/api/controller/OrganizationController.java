package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.organization.CreateOrganizationRequest;
import br.com.gymflow.api.dto.organization.OrganizationResponse;
import br.com.gymflow.api.dto.organization.UpdateOrganizationRequest;
import br.com.gymflow.api.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Organizations",
        description = "Endpoints para cadastro, listagem e gerenciamento de organizações"
)
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;


    @Operation(
            summary = "Criar organização",
            description = "Cadastra uma nova organização no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organização criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponse> create(
            @RequestBody @Valid CreateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Listar organizações",
            description = "Retorna todas as organizações cadastradas no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizações encontradas")
    })
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> findAll() {
        List<OrganizationResponse> response = organizationService.findAll();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Buscar organização por ID",
            description = "Retorna uma organização específica pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organização encontrada"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> findById(
            @PathVariable Long id
    ) {
        OrganizationResponse response = organizationService.findById(id);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Atualizar organização",
            description = "Atualiza parcialmente os dados de uma organização cadastrada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organização atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<OrganizationResponse> patch(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.patch(id, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Remover organização",
            description = "Remove uma organização cadastrada pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Organização removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        organizationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}