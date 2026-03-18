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
 * Entity representing the content details of a Use Case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usecase_content", schema = "mfg")
public class UseCaseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usecase_content_id")
    private Integer usecaseContentId;

    @Column(name = "usecase_id", nullable = false)
    private Integer usecaseId;

    @Column(name = "description" )
    private String description;

    @Column(name = "business_problem" )
    private String businessProblem;

    @Column(name = "solution")
    private String solution;

    @Column(name = "tools_and_technologies")
    private String toolsAndTechnologies;

    @Column(name = "key_results")
    private String keyResults;

    @Column(name = "value_delivered")
    private String valueDelivered;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "narration_guide")
    private String narrationGuide;

    @Column(name = "banner_url", nullable = false, length = 500)
    private String bannerUrl;

}
