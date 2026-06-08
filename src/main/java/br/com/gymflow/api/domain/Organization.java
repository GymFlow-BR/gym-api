package br.com.gymflow.api.domain;

import br.com.gymflow.api.domain.enums.OrganizationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "organization_id")
   private Long id;

   @Column(name = "organization_name", nullable = false, length = 150)
   private String organizationName;

   @Enumerated(EnumType.STRING)
   @Column(name = "organization_type", nullable = false, length = 30)
   private OrganizationType organizationType;

   @Column(name = "organization_email", unique = true, length = 150)
   private String organizationEmail;

   @Column(name = "organization_phone", length = 30)
   private String organizationPhone;

   @Column(name = "active", nullable = false)
   private Boolean active = true;

   @CreationTimestamp
   @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @UpdateTimestamp
   @Column(name = "updated_at", nullable = false)
   private LocalDateTime updatedAt;
}