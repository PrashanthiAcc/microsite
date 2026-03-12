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
 * Entity representing a Tag assigned to a Use Case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usecase_tags", schema = "mfg")
public class UseCaseTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usecase_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UseCase useCase;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;
}
