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
 * Entity representing a Favourite Use Case in the Manufacturing Lab Microsite.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favourites", schema = "mfg")
public class Favourite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favourite_id")
    private Integer favouriteId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "usecase_id", nullable = false)
    private Integer usecaseId;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}