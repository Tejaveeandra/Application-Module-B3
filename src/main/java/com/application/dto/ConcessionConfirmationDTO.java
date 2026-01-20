// 3. NEW FILE (ConcessionConfirmationDTO.java) - PLEASE CREATE THIS
package com.application.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConcessionConfirmationDTO {
    private Integer concessionTypeId;
    private Float concessionAmount;
    private Integer givenById;
    private Integer authorizedById;
    private Integer reasonId;
    private String comments;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer concReferedBy;
    private Integer proConcessionAmount;
    private String proConcessionReason;
    private Integer proConcessionGivenBy;

}