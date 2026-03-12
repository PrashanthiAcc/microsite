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
 * Entity representing a Speaker assigned to a Use Case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usecase_speakers", schema = "mfg")
public class UseCaseSpeaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "speaker_id")
    private Integer speakerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usecase_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UseCase useCase;

    @Column(name = "speaker_eid", nullable = false, length = 100)
    private String speakerEid;

    @Column(name = "speaker_type", nullable = false, length = 20)
    private String speakerType;
}
