package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor

@Data
public class ArtifactDTO {

    private String artifactType;
    private String url;

}