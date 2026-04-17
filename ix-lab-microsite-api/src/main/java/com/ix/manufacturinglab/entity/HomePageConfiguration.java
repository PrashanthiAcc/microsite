package com.ix.manufacturinglab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a Home page configuration in the Manufacturing Lab Microsite.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "home_page_configuration", schema = "mfg")
public class HomePageConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_name", nullable = false)
    private String applicationName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subtitle", nullable = false)
    private String subtitle;

    @Column(name = "hero_image_url", nullable = false, length = 500)
    private String heroImageUrl;

    @Column(name = "mes_mom_sol_delivered", nullable = false)
    private String mesMomSolDelivered;

    @Column(name = "prod_site_critical_support", nullable = false)
    private String prodSiteCriticalSupport;

    @Column(name = "sap_ewm_prg_delivered", nullable = false)
    private String sapEwmPrgDelivered;

    @Column(name = "key_cap_conf_url", nullable = false, length = 500)
    private String keyCapConfUrl;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "updated_by_id", nullable = false)
    private Integer updatedById;

}