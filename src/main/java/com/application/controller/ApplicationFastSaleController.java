package com.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.dto.ApplicationFastDetailsGet;
import com.application.dto.StudentApplicationSaleColegeDTO;
import com.application.dto.StudentApplicationSingleDTO;
import com.application.dto.StudentApplicationUpdateDTO;
import com.application.dto.StudentCollegeConfirmationDto;
import com.application.dto.StudentFastSaleDTO;
import com.application.service.ApplicationFastSale;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/student_fast_sale")
public class ApplicationFastSaleController {
	
	@Autowired private ApplicationFastSale applicationFastSale;
	
	@PostMapping("/fast-sale")
    public ResponseEntity<String> createFastSaleAdmission(@RequestBody StudentFastSaleDTO formData) {
		System.out.println("Received studAdmsNo in Controller: " + formData.getStudAdmsNo());
        try {
        	applicationFastSale.createFastSaleAdmission(formData);
            return ResponseEntity.ok("Fast sale admission created successfully!");
        } catch (Exception e) {
            // Log the full stack trace for debugging purposes
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to save fast sale admission: " + e.getMessage());
        }
    }
	
	
	@GetMapping("/fast-sale-get-colleges/{admsNo}")
    public ResponseEntity<ApplicationFastDetailsGet> getFastSaleDetails(@PathVariable Long admsNo) {
        try {
            ApplicationFastDetailsGet details = applicationFastSale.getFastSaleDetailsByAdmissionNo(admsNo);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            // Log the full stack trace for debugging purposes
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null); // Or custom error DTO
        }
    }
	
	@PostMapping("/college-application-sale")
	public ResponseEntity<String> createApplicationSale(@Valid @RequestBody StudentApplicationSaleColegeDTO formDto) {
        try {
        	applicationFastSale.createApplicationSale(formDto);
            return ResponseEntity.ok("Sale admission created successfully!");
        } catch (Exception e) {
            // Log the full stack trace for debugging purposes
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to save sale admission: " + e.getMessage());
        }
    }
	
	@GetMapping("/saleget-college/{studAdmsId}")
    public ResponseEntity<?> getApplicationDetails(@PathVariable Long studAdmsId) {
        try {
            // 1. Call the service method
            StudentApplicationSingleDTO dto = applicationFastSale.getSingleApplicationDetails(studAdmsId);
            
            // 2. Return the DTO with an OK status (HTTP 200)
            return ResponseEntity.ok(dto);
            
        } catch (EntityNotFoundException e) {
            // 3. Handle specific exceptions (like the one thrown in the service layer)
            // Return a NOT FOUND status (HTTP 404)
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            
        } catch (Exception e) {
            // 4. Handle any other unexpected errors
            // Log the error for debugging
            System.err.println("Error in getApplicationDetails: " + e.getMessage());
            e.printStackTrace();
            // Return an Internal Server Error (HTTP 500) with more details
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while fetching application details: " + e.getMessage());
        }
    }
	
	@PutMapping("/update/{studAdmsId}")  // or @PatchMapping if you prefer
	public ResponseEntity<String> updateApplication(
	        @PathVariable Long studAdmsId,
	        @RequestBody StudentApplicationUpdateDTO formData) {
	    
	    try {
	        // Call the updated service method with the new DTO
	        String successMessage = applicationFastSale.updateApplicationSale(studAdmsId, formData);

	        // Return success message with HTTP 200
	        return ResponseEntity.ok(successMessage);

	    } catch (EntityNotFoundException e) {
	        // 404 - Resource not found
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	        
	    } catch (Exception e) {
	        // 500 - Any other unexpected error
	        // Log the full exception for debugging
	        System.out.println("===========================================");
	        System.out.println("ERROR updating application sale for studAdmsId: " + studAdmsId);
	        System.out.println("Exception Type: " + e.getClass().getName());
	        System.out.println("Exception Message: " + e.getMessage());
	        System.out.println("===========================================");
	        e.printStackTrace();
	        
	        // Build detailed error message
	        String errorDetails = String.format(
	            "Error Type: %s | Message: %s | Cause: %s",
	            e.getClass().getSimpleName(),
	            e.getMessage() != null ? e.getMessage() : "No message",
	            e.getCause() != null ? e.getCause().getMessage() : "No cause"
	        );
	        
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(errorDetails);
	    }
	}
	
	@PostMapping("/college-confirmation")
    public ResponseEntity<String> confirmCollegeEnrollment(@RequestBody StudentCollegeConfirmationDto formData) {
        try {
            // Call the service method, which returns a String success message
            String successMessage = applicationFastSale.confirmCollegeEnrollment(formData);
            
            // Return 200 OK with the success message
            return ResponseEntity.ok(successMessage);
            
        } catch (EntityNotFoundException e) {
            // Return 404 Not Found if the studAdmsId or a required lookup ID is invalid
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            // Log the error
            System.err.println("Enrollment confirmation error: " + e.getMessage());
            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to confirm enrollment due to an internal error.");
        }
    }
	
}
