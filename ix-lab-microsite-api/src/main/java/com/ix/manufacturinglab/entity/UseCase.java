package com.ix.manufacturinglab.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing a Use Case in the Manufacturing Lab Microsite.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usecase", schema = "mfg")
public class UseCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usecase_id")
    private Integer usecaseId;

    @Column(name = "industry_id", nullable = false)
    private Long industryId;

    @Column(name = "sub_industry_id", nullable = false)
    private Long subIndustryId;

    @Column(name = "value_chain_id", nullable = false)
    private Integer valueChainId;

    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "owner_eid", nullable = false, length = 100)
    private String ownerEid;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "approver_id")
    private Integer approverId;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "parent_usecase_id")
    private Integer parentUsecaseId;

    @Column(name = "is_updated_usecase", nullable = false)
    private Boolean isUpdatedUsecase;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder.Default
    @OneToMany(mappedBy = "useCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<UseCaseSpeaker> speakers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "useCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<UseCaseTag> tags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "useCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<UseCaseArtifact> artifacts = new ArrayList<>();
}
