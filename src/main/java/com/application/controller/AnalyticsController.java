package com.application.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.List;
 
import com.application.dto.CombinedAnalyticsDTO;
import com.application.dto.DashboardResponseDTO;
import com.application.dto.GenericDropdownDTO;
import com.application.dto.GenericDropdownDTO_Dgm;
import com.application.dto.GraphBarDTO;
import com.application.dto.GraphDTO;
import com.application.dto.AcademicYearInfoDTO;
import com.application.service.AdminDashboardService;
import com.application.service.ApplicationAnalyticsService;
 
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
 
    @Autowired
    private ApplicationAnalyticsService analyticsService;
   
    @Autowired
    private AdminDashboardService adminDashboardService;
 
    @GetMapping("/zone/{id}")
    public ResponseEntity<CombinedAnalyticsDTO> getZoneAnalytics(@PathVariable Long id) {
        try {
            CombinedAnalyticsDTO data = analyticsService.getZoneAnalytics(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("Error in getZoneAnalytics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
 
    @GetMapping("/campus/{id}")
    public ResponseEntity<CombinedAnalyticsDTO> getCampusAnalytics(@PathVariable Long id) {
        try {
            CombinedAnalyticsDTO data = analyticsService.getCampusAnalytics(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("Error in getCampusAnalytics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
   
    @GetMapping("/{empId}")
    public ResponseEntity<CombinedAnalyticsDTO> getRollupAnalytics(
            @PathVariable("empId") Integer empId) {
       
        try {
            // Call the "master rollup" method in the service
            CombinedAnalyticsDTO analytics = analyticsService.getRollupAnalytics(empId);
 
            // Check if the service returned empty data (only for invalid employees or null roles)
            if (analytics.getGraphData() == null && analytics.getMetricsData() == null) {
                // This returns the JSON for invalid employees or employees without roles/campuses
                return ResponseEntity.badRequest().body(analytics);
            }
            return ResponseEntity.ok(analytics);
           
        } catch (Exception e) {
            System.err.println("Error in getRollupAnalytics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
   
    @GetMapping("/zone/graph-by-amount")
    public ResponseEntity<GraphDTO> getZoneGraphByAmount(
            @RequestParam("zoneId") Integer zoneId,
            @RequestParam("amount") Float amount) {
 
        GraphDTO graphData = analyticsService.getGraphDataByZoneIdAndAmount(zoneId, amount);
        return ResponseEntity.ok(graphData);
    }
   
    @GetMapping("/campus/graph-by-amount")
    public ResponseEntity<GraphDTO> getCampusGraphByAmount(
            @RequestParam("campusId") Integer campusId,
            @RequestParam("amount") Float amount) {
 
        GraphDTO graphData = analyticsService.getGraphDataByCampusIdAndAmount(campusId, amount);
        return ResponseEntity.ok(graphData);
    }
   
    @GetMapping("/cards_graph")
    public DashboardResponseDTO getAdminDashboardData(
            @RequestParam Integer employeeId) {
        return adminDashboardService.getDashboardData(employeeId);
    }
   
    @GetMapping("/flexible-graph")
    public ResponseEntity<List<GraphBarDTO>> getFlexibleGraphData(
            @RequestParam(required = false) Integer zoneId,
            @RequestParam(required = false) List<Integer> campusIds,
            @RequestParam(required = false) Integer campusId,
            @RequestParam(required = false) Float amount,
            @RequestParam(required = false) Integer empId) {
       
        try {
            // IMPORTANT: campusId (singular) uses entity_id = 4, campusIds (plural) uses entity_id = 3
            // Don't convert campusId to campusIds - pass them separately to service
            System.out.println("========================================");
            System.out.println("FLEXIBLE GRAPH REQUEST");
            System.out.println("Zone ID: " + zoneId);
            if (campusId != null) {
                System.out.println("Campus ID (singular, entity_id=4): " + campusId);
            }
            if (campusIds != null && !campusIds.isEmpty()) {
                System.out.println("Campus IDs (plural, entity_id=3): " + campusIds + " (Total: " + campusIds.size() + " campus/es)");
            }
            if (campusId == null && (campusIds == null || campusIds.isEmpty())) {
                System.out.println("Campus IDs: None");
            }
            System.out.println("Amount: " + amount);
            System.out.println("Employee ID: " + empId);
            System.out.println("========================================");
           
            List<GraphBarDTO> graphData = analyticsService.getFlexibleGraphData(zoneId, campusIds, campusId, amount, empId);
           
            // Log the final response summary
            if (campusIds != null && !campusIds.isEmpty()) {
                System.out.println("========================================");
                System.out.println("FLEXIBLE GRAPH RESPONSE SUMMARY");
                System.out.println("Total data points returned: " + graphData.size());
                System.out.println("Data for each year:");
                for (GraphBarDTO dto : graphData) {
                    System.out.println("  " + dto.getYear() + ": Issued=" + dto.getIssuedCount() +
                                     ", Sold=" + dto.getSoldCount() +
                                     ", Issued%=" + dto.getIssuedPercent() +
                                     ", Sold%=" + dto.getSoldPercent());
                }
                System.out.println("========================================");
            } else {
                System.out.println("Flexible Graph Response - Returned " + graphData.size() + " data points");
            }
           
            return ResponseEntity.ok(graphData);
        } catch (Exception e) {
            System.err.println("Error in getFlexibleGraphData: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
   
 // In AnalyticsController.java
 
    @GetMapping("/dgm_employee/{id}")
    public ResponseEntity<CombinedAnalyticsDTO> getEmployeeAnalytics(@PathVariable Long id) {
        try {
            CombinedAnalyticsDTO data = analyticsService.getEmployeeAnalytics(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("Error in getEmployeeAnalytics: " + e.getMessage());
            e.printStackTrace();
            // Return a 500 Internal Server Error response
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/campuses")
    public ResponseEntity<List<GenericDropdownDTO>> getAllCampuses(
            @RequestParam(required = false) String category) {
        try {
            List<GenericDropdownDTO> campuses = analyticsService.getAllCampuses(category);
            return ResponseEntity.ok(campuses);
        } catch (Exception e) {
            System.err.println("Error in getAllCampuses: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/zones")
    public ResponseEntity<List<GenericDropdownDTO>> getAllZones(
            @RequestParam(required = false) String category) {
        try {
            List<GenericDropdownDTO> zones = analyticsService.getAllZones(category);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            System.err.println("Error in getAllZones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/dgm-employees")
    public ResponseEntity<List<GenericDropdownDTO_Dgm>> getAllDgmEmployees(
            @RequestParam(required = false) String category) {
        try {
            List<GenericDropdownDTO_Dgm> dgmEmployees = analyticsService.getAllDgmEmployees(category);
            return ResponseEntity.ok(dgmEmployees);
        } catch (Exception e) {
            System.err.println("Error in getAllDgmEmployees: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/academic-year-info")
    public ResponseEntity<AcademicYearInfoDTO> getAcademicYearInfo() {
        try {
            AcademicYearInfoDTO yearInfo = analyticsService.getAcademicYearInfo();
            return ResponseEntity.ok(yearInfo);
        } catch (Exception e) {
            System.err.println("Error in getAcademicYearInfo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/admin-app-amounts")
    public ResponseEntity<List<Double>> getDistinctAppAmounts() {
        try {
            List<Double> amounts = analyticsService.getDistinctAppAmounts();
            return ResponseEntity.ok(amounts);
        } catch (Exception e) {
            System.err.println("Error in getDistinctAppAmounts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
 