package com.application.dto;
 
import java.time.LocalDate;
import java.util.Date;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrientationFeeAndDatesDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private float fee;
}