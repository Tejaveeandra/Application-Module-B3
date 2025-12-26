package com.application.dto;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 @AllArgsConstructor
 @NoArgsConstructor
@Data
public class PerformanceDTO {
    // A generic field to hold the name of the Zone, DGM, or Campus
    private String name;
    private Double performancePercentage;
}