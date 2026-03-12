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

/**
 * Entity representing a Value Chain mapped to an Industry and Sub-Industry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "value_chain", schema = "mfg")
public class ValueChain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_chain_id")
    private Long valueChainId;

    @Column(name = "value_chain_name", nullable = false, length = 255)
    private String valueChainName;

    @Column(name = "industry_id", nullable = false)
    private Long industryId;

    @Column(name = "sub_industry_id", nullable = false)
    private Long subIndustryId;
}
