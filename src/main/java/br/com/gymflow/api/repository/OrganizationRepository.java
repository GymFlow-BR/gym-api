package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByOrganizationEmail(String organizationEmail);
}