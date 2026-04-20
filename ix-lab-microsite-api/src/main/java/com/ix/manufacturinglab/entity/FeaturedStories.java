package com.ix.manufacturinglab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Table(name = "featured_stories", schema = "mfg")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedStories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_story_id")
    private Long featureStoryId;

    @Column(name = "usecase_id", nullable = false)
    private Long usecaseId;
}
