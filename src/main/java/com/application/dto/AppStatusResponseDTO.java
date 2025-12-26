package com.application.dto;
 
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppStatusResponseDTO {
 
    private Integer appNo;
    private String status;
    private String campusName;
    private String proName;
    private String zoneName;
    private String dgmName;
    private String reason;
    private LocalDateTime updatedDate;
}
 
 