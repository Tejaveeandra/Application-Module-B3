package com.application.dto;

import java.util.List;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericDropdownDTO_Dgm {
    private Integer id;
    private String name;
    
    // This MUST be List<Integer>. 
    // Initializing it here ensures the "bucket" exists immediately.
    private List<Integer> cmpsId = new ArrayList<>(); 
}