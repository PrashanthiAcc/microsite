package com.ix.manufacturinglab.dto;

import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.ValueChain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated DTO combining Industry, SubIndustry, and ValueChain data
 * for the microsite hierarchy response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicrositeDataDTO {

    private List<Industry> industries;
    private List<SubIndustry> subIndustries;
    private List<ValueChain> valueChains;
}
