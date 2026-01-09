package com.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLocationDTO {
    private Integer campusId;
    private String campusName;
    private Integer cityId;
    private String cityName;
    private Integer stateId;
    private String stateName;
    private Integer zoneId;
    private String zoneName;
    private Integer districtId;
    private String districtName;
}

