package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class FaqDTO {

    private Integer usecaseFaqId;
    private Integer usecaseId;
    private String question;
    private String answer;
    private Integer updatedBy;
    private LocalDateTime lastUpdated;

}
