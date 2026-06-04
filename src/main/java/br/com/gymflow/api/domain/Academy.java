package br.com.gymflow.api.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "academies")
public class Academy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_id")
    private Long id;

    @Column(name = "academy_name", nullable = false, length = 150)
    private String academyName;

    @Column(name = "academy_email", nullable = false, unique = true, length = 150)
    private String academyEmail;

    @Column(name = "academy_phone", length = 30)
    private String academyPhone;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
