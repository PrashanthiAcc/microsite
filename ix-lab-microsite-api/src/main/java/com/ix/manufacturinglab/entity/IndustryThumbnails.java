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
@Table(name = "industry_thumbnails", schema = "mfg")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndustryThumbnails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_thumbnail_id")
    private Long industryThumbnailId;

    @Column(name = "industry_id", nullable = false)
    private Long industryId;

    @Column(name = "industry_thumbnail_url", nullable = false, length = 500)
    private String industryThumbnailUrl;
}
