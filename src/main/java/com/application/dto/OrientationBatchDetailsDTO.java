package com.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrientationBatchDetailsDTO {
    private LocalDate orientationStartDate;
    private LocalDate orientationEndDate;
    private float orientationFee;
}
