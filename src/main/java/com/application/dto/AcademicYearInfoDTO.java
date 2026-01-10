package com.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearInfoDTO {
    private AcademicYearDTO currentYear;
    private AcademicYearDTO nextYear;
    private AcademicYearDTO previousYear;
}

