package com.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DgmWithCampusesDTO {
    private Integer empId;
    private String dgmName;
    private List<Integer> campusIds;
}

