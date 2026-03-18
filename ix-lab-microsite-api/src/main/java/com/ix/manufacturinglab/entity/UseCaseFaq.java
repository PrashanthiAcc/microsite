package com.ix.manufacturinglab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing FAQ entries for a Use Case in the Manufacturing Lab Microsite.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usecase_faq", schema = "mfg")
public class UseCaseFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usecase_faq_id")
    private Integer usecaseFaqId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usecase_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UseCase useCase;

    @Column(name = "question", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String answer;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}