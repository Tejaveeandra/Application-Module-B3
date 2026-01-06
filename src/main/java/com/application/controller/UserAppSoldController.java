package com.application.controller;

import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;
 
import com.application.dto.RateResponseDTO;

import com.application.service.UserAppSoldService;

@RestController

@RequestMapping("/api/performance")

public class UserAppSoldController {

	@Autowired

	private UserAppSoldService userAppSoldService;

//	 @GetMapping("/top_drop_rate")//used

//	    public ResponseEntity<List<RateResponseDTO>> getAllRateData() {

//	        return ResponseEntity.ok(userAppSoldService.getAllRateData());

//	    }

	@GetMapping("/top_drop_rate")

	public ResponseEntity<List<RateResponseDTO>> getAllRateData(

	        @RequestParam(required = false) String campusCategory,

	        @RequestParam(required = false) Integer empId) {

	    // We pass the empId, and the Service handles the role check

	    return ResponseEntity.ok(userAppSoldService.getAllRateData(campusCategory, empId));

	}

//	 @GetMapping("/zonal_accountant/{empId}")

//	    public ResponseEntity<RateResponseDTO> getZonalAccountantPerformance(

//	            @PathVariable Integer empId) {

//

//	        RateResponseDTO response = userAppSoldService.getZoneAccountantPerformance(empId);

//

//	        return ResponseEntity.ok(response);

//	    }

//	 

//	 @GetMapping("/dgm/{empId}")

//	 public ResponseEntity<RateResponseDTO> getDgmAnalytics(@PathVariable Integer empId) {

//	     return ResponseEntity.ok(userAppSoldService.getDgmPerformance(empId));

//	 }

	 @GetMapping("/performance/{empId}")

	 public ResponseEntity<?> getPerformanceByRole(@PathVariable Integer empId) {

	     return ResponseEntity.ok(userAppSoldService.getRoleBasedPerformance(empId));

	 }
 
 
	 

}

 