package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.organization.CreateOrganizationRequest;
import br.com.gymflow.api.dto.organization.OrganizationResponse;
import br.com.gymflow.api.dto.organization.UpdateOrganizationRequest;
import br.com.gymflow.api.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(
            @RequestBody @Valid CreateOrganizationRequest request
            ) {
        OrganizationResponse response = organizationService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> findById(
            @PathVariable Long id
    ) {
        OrganizationResponse response = organizationService.findById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> findAll() {
        List<OrganizationResponse> response = organizationService.findAll();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrganizationResponse> patch(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrganizationRequest request
            ) {
        OrganizationResponse response = organizationService.patch(id, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        organizationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}