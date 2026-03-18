package com.ix.manufacturinglab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing an Artifact associated with a Use Case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usecase_artifacts", schema = "mfg")
public class UseCaseArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artifact_id")
    private Integer artifactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usecase_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UseCase useCase;

    @Column(name = "artifact_type", nullable = false, length = 30)
    private String artifactType;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "artifact_name", nullable = false, length = 100)
    private String artifactName;

}
