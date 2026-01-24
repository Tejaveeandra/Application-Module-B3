package com.application.controller;
 
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.dto.AppStatusDTO;
import com.application.entity.AppStatusTrackView;
import com.application.service.ApplicationStatusViewService;
 
@RestController
@RequestMapping("/api/application-status")
public class ApplicationStatusViewController {
 
    @Autowired

    private ApplicationStatusViewService applicationStatusViewService;
 
    @GetMapping("/getview/{cmpsId}")
    public ResponseEntity<List<AppStatusTrackView>> getStatusByCampusId(@PathVariable int cmpsId) {
        List<AppStatusTrackView> statusRecords = applicationStatusViewService.getApplicationStatusByCampus(cmpsId);

        if (statusRecords.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(statusRecords);
    }
    
    @GetMapping("/getview/employee-campus/{empId}")
    public ResponseEntity<List<AppStatusTrackView>> getStatusByEmployeeCampusId(@PathVariable int empId) {
        try {
            List<AppStatusTrackView> statusRecords = applicationStatusViewService.getApplicationStatusByEmployeeCampus(empId);
            return ResponseEntity.ok(statusRecords);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/status-by-role")
    public ResponseEntity<List<AppStatusTrackView>> getStatusByRole(
            @RequestParam("empId") int empId,
            @RequestParam("category") String category,
            @RequestParam(value = "yearIds", required = false) List<Integer> yearIds) {
        
        List<AppStatusTrackView> result = applicationStatusViewService.getApplicationStatusByRole(empId, category, yearIds);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/all_status_list") // used/c
    public ResponseEntity<List<AppStatusDTO>> getAllStatus(
            @RequestParam(value = "category", required = false) String category) {
        
        // Pass the category (which might be null) to the service
        return ResponseEntity.ok(applicationStatusViewService.getStatusByCategory(category));
    }
    
    @GetMapping("/basedOnCategory/application-status_list")
    public List<AppStatusDTO> getApplicationStatus(
            @RequestParam String category,
            @RequestParam(required = false) Integer zoneId) {
 
        return applicationStatusViewService.fetchApplicationStatus(category, zoneId);
    }
    
    @GetMapping("/latest")
    public ResponseEntity<List<AppStatusTrackView>> getAllLatestStatus() {
        List<AppStatusTrackView> list = applicationStatusViewService.getAllLatestStatus();
        return ResponseEntity.ok(list);
    }
}

 