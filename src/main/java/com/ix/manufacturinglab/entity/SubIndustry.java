package com.ix.manufacturinglab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a Sub-Industry within an Industry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sub_industry", schema = "mfg")
public class SubIndustry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_industry_id")
    private Long subIndustryId;

    @Column(name = "sub_industry_name", nullable = false, length = 255)
    private String subIndustryName;

    @Column(name = "industry_id", nullable = false)
    private Long industryId;

    @Column(name = "updated_by_id")
    private Integer updatedById;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}
