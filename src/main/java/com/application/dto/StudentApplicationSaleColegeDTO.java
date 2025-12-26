package com.application.dto; // Assuming your DTO package; adjust if needed

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class StudentApplicationSaleColegeDTO {
    // Essential for lookup
    private Long studAdmsNo;
    private Integer createdBy;

    private String hallTicketNumber; // "Hall Ticket Number"
    private String preHallTicketNo;
    private Integer schoolStateId;        // "School State"
    private Integer schoolDistrictId;     // "School District"
    private String schoolName;            // "School Name" (Assuming text or value)
    private String scoreAppNo;            // "Score App No"
    private Integer scoreMarks;           // "Score Marks"
    private Integer schoolType;
    private Long proReceiptNo;
    private Integer foodTypeId;           // "Food Type"
    private Integer bloodGroupId;         // "Blood Group"
    private Integer religionId;           // "Religion"
    private Integer casteId;
    // Personal Information
    private String firstName;
    private String lastName;
    private Integer genderId; // From Male/Female select
    private Date dob; // mm/dd/yyyy string
    private Long aadharCardNo;
    private String apaarNo;
    private Integer appTypeId; // Admission Type
    private Integer quotaId; // Quota/Admission Referred By
    private Date appSaleDate;
    private String admissionReferedBy;

    // Parent Information
    private String fatherName;
    private Long fatherMobileNo;
    private String fatherEmail;
    private Integer fatherSectorId; // Select Sector
    private Integer fatherOccupationId; // Select Occupation
    private String motherName;
    private Long motherMobileNo;
    private String motherEmail;
    private Integer motherSectorId; // Select Sector
    private Integer motherOccupationId; // Select Occupation

    // Orientation Information
    private Integer academicYearId;
//    private Integer cityId; // Select City
    private Integer branchId; // Select Branch
    private Integer classId; // Joining Class
    private Integer orientationId; // Course Name (assuming ID)
    private Integer studentTypeId; // Student Type
    
    // ========================================
    // 🔵 PREVIOUS COLLEGE DETAILS (Added Now)
    // ========================================
    private String preCollegeName;
    private Integer preCollegeTypeId;      // Maps to pre_college_type_id
    private Integer preCollegeStateId;     // Maps to pre_college_state_id
    private Integer preCollegeDistrictId;  // Maps to pre_college_district_id
 

    // Address Information (fields as per image)
    private AddressDetailsDTO addressDetails;
    
    private PaymentDetailsDTO paymentDetails; 
    
    private List<SiblingDTO> siblings;

    // --- Concession Info (List) ---
    private List<ConcessionConfirmationDTO> concessions;

    // Optional: Image upload handled separately
}