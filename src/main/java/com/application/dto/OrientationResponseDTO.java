package com.application.dto;

import java.time.LocalDate;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrientationResponseDTO {
    private int cmps_id;
    private String cmps_name;
    private int orientation_id;
    private String orientation_name;
    private int orientation_batch_id;
    private String orientation_batch_name;
    private LocalDate orientation_start_date;
    private LocalDate orientation_end_date;
    private float orientation_fee;
    private String section_name;
    private Integer sectionId;
}