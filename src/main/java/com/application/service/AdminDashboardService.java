 
package com.application.service;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.application.dto.DashboardResponseDTO;
import com.application.dto.GraphBarDTO;
import com.application.dto.GraphResponseDTO;
import com.application.dto.MetricCardDTO;
import com.application.entity.AcademicYear;
import com.application.repository.AdminAppRepository;
import com.application.repository.AcademicYearRepository;
import com.application.repository.AppStatusTrackRepository;
import com.application.repository.AppStatusTrackViewRepository;
import com.application.repository.BalanceTrackRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.UserAppSoldRepository;
import com.application.entity.Distribution;
import com.application.entity.AppStatusTrackView;
 
@Service
public class AdminDashboardService {
 
    @Autowired
    private AdminAppRepository adminAppRepository;
   
    @Autowired
    private AppStatusTrackRepository appStatusTrackRepository;
   
    @Autowired
    private BalanceTrackRepository balanceTrackRepository;
   
    @Autowired
    private UserAppSoldRepository userAppSoldRepository;
   
    @Autowired
    private AcademicYearRepository academicYearRepository;
   
    @Autowired
    private DistributionRepository distributionRepository;
   
    @Autowired
    private AppStatusTrackViewRepository appStatusTrackViewRepository;
 
    public DashboardResponseDTO getDashboardData(Integer employeeId) {
       
        // Get current year (latest year) from AdminApp for this specific employee
        // This ensures we get the year where the employee actually has allocations
        Integer currentYearId = adminAppRepository.findLatestYearIdByEmployee(employeeId);
        
        // If no year found for this employee, try to get latest year from AdminApp in general
        if (currentYearId == null) {
            currentYearId = adminAppRepository.findLatestYearIdFromAdminApp();
        }
        
        // If still no year found, return zeros for all data
        if (currentYearId == null) {
            return createEmptyDashboardResponse();
        }
        Integer previousYearId = currentYearId - 1;
       
        // Sum total_app from AdminApp table for given employee and current academic year
        Long currentYearTotal = adminAppRepository.sumTotalAppByEmployeeAndAcademicYear(employeeId, currentYearId);
        int currTotalApplications = currentYearTotal != null ? currentYearTotal.intValue() : 0;
       
        // Sum total_app from AdminApp table for given employee and previous academic year
        Long previousYearTotal = adminAppRepository.sumTotalAppByEmployeeAndAcademicYear(employeeId, previousYearId);
        int prevTotalApplications = previousYearTotal != null ? previousYearTotal.intValue() : 0;
       
        // Calculate percentage change (same logic as AppStatusTrackService)
        int percentageChange = clampChange(prevTotalApplications, currTotalApplications);
       
        // Get metrics data using new logic: Check Distribution ranges and count by status from AppStatusTrackView
        long currSold = countApplicationsByStatus(employeeId, currentYearId, "sold");
        long currConfirmed = countApplicationsByStatus(employeeId, currentYearId, "confirmed");
        long currDamaged = countApplicationsByStatus(employeeId, currentYearId, "damaged");
        long currUnavailable = countApplicationsByStatus(employeeId, currentYearId, "unavailable");
        long currWithPro = countApplicationsByStatus(employeeId, currentYearId, "with pro");
       
        // Get metrics data for previous year
        long prevSold = countApplicationsByStatus(employeeId, previousYearId, "sold");
        long prevConfirmed = countApplicationsByStatus(employeeId, previousYearId, "confirmed");
        long prevDamaged = countApplicationsByStatus(employeeId, previousYearId, "damaged");
        long prevUnavailable = countApplicationsByStatus(employeeId, previousYearId, "unavailable");
        long prevWithPro = countApplicationsByStatus(employeeId, previousYearId, "with pro");
       
        // Get available data using new logic: AdminApp.total_app - Distribution.total_app_count
        int currAvailable = calculateAvailableByAmount(employeeId, currentYearId);
        int prevAvailable = calculateAvailableByAmount(employeeId, previousYearId);
       
        // Calculate Issued = Total App (AdminApp) - Available (AdminApp - Distribution)
        // This equals: Total App - (Total App - Distributed) = Distributed
        int currIssued = currTotalApplications - currAvailable;
        int prevIssued = prevTotalApplications - prevAvailable;
       
        // With PRO is already calculated above using countApplicationsByStatus
       
        // Calculate percentage changes
        int soldPercentageChange = clampChange((int) prevSold, (int) currSold);
        int confirmedPercentageChange = clampChange((int) prevConfirmed, (int) currConfirmed);
        int damagedPercentageChange = clampChange((int) prevDamaged, (int) currDamaged);
        int unavailablePercentageChange = clampChange((int) prevUnavailable, (int) currUnavailable);
        int availablePercentageChange = clampChange(prevAvailable, currAvailable);
        int issuedPercentageChange = clampChange(prevIssued, currIssued);
        int withProPercentageChange = clampChange((int) prevWithPro, (int) currWithPro);
 
        // Create metric cards
        List<MetricCardDTO> metricCards = new ArrayList<>();
        metricCards.add(new MetricCardDTO("Total Applications", currTotalApplications, percentageChange, "total_applications"));
        metricCards.add(new MetricCardDTO("Sold", (int) currSold, soldPercentageChange, "sold"));
        metricCards.add(new MetricCardDTO("Confirmed", (int) currConfirmed, confirmedPercentageChange, "confirmed"));
        metricCards.add(new MetricCardDTO("Available", currAvailable, availablePercentageChange, "available"));
        metricCards.add(new MetricCardDTO("Issued", currIssued, issuedPercentageChange, "issued"));
        metricCards.add(new MetricCardDTO("Damaged", (int) currDamaged, damagedPercentageChange, "damaged"));
        metricCards.add(new MetricCardDTO("Unavailable", (int) currUnavailable, unavailablePercentageChange, "unavailable"));
        metricCards.add(new MetricCardDTO("With PRO", (int) currWithPro, withProPercentageChange, "with_pro"));
 
        // Generate graph data for previous 4 years (current year + 3 previous years)
        GraphResponseDTO graphData = generateGraphData(employeeId, currentYearId);
 
        // Create response
        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setMetricCards(metricCards);
        response.setGraphData(graphData);
 
        return response;
    }
   
    // Helper method to create empty dashboard response with all zeros
    private DashboardResponseDTO createEmptyDashboardResponse() {
        List<MetricCardDTO> metricCards = new ArrayList<>();
        metricCards.add(new MetricCardDTO("Total Applications", 0, 0, "total_applications"));
        metricCards.add(new MetricCardDTO("Sold", 0, 0, "sold"));
        metricCards.add(new MetricCardDTO("Confirmed", 0, 0, "confirmed"));
        metricCards.add(new MetricCardDTO("Available", 0, 0, "available"));
        metricCards.add(new MetricCardDTO("Issued", 0, 0, "issued"));
        metricCards.add(new MetricCardDTO("Damaged", 0, 0, "damaged"));
        metricCards.add(new MetricCardDTO("Unavailable", 0, 0, "unavailable"));
        metricCards.add(new MetricCardDTO("With PRO", 0, 0, "with_pro"));
       
        // Create empty graph data for 4 years with zeros
        List<GraphBarDTO> barList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            GraphBarDTO dto = new GraphBarDTO();
            dto.setYear("Year " + (2025 - i));
            dto.setIssuedPercent(0);
            dto.setSoldPercent(0);
            dto.setIssuedCount(0);
            dto.setSoldCount(0);
            barList.add(dto);
        }
       
        GraphResponseDTO graphData = new GraphResponseDTO();
        graphData.setGraphBarData(barList);
       
        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setMetricCards(metricCards);
        response.setGraphData(graphData);
       
        return response;
    }
   
    // Helper methods for percentage calculation (same as AppStatusTrackService)
    private int clampChange(int prev, int curr) {
        if (prev == 0) {
            // If previous was 0, any increase is considered 100% growth, but if current is also 0, return 0
            return curr > 0 ? 100 : 0;
        }
        double raw = ((double) (curr - prev) / prev) * 100;
        return clamp(raw);
    }
 
    private int clamp(double value) {
        if (value > 100) return 100;
        if (value < -100) return -100;
        return (int) Math.round(value);
    }
 
    private int calculateAvailableByAmount(Integer employeeId, Integer yearId) {
        // Get total_app from AdminApp table for this employee and year
        Long adminAppTotal = adminAppRepository.sumTotalAppByEmployeeAndAcademicYear(employeeId, yearId);
        int totalApp = (adminAppTotal != null) ? adminAppTotal.intValue() : 0;
       
        // Get sum of total_app_count from Distribution table (all distributions created by this admin)
        Integer distributedCount = distributionRepository.sumTotalAppCountByCreatedByAndYear(employeeId, yearId);
        int totalDistributed = (distributedCount != null) ? distributedCount : 0;
       
        // Available = AdminApp.total_app - Distribution.total_app_count
        int available = totalApp - totalDistributed;
       
        // Ensure available is not negative
        return Math.max(0, available);
    }
   
    private long countApplicationsByStatus(Integer employeeId, Integer yearId, String statusType) {
        // Get all distributions created by this employee for the year
        List<Distribution> distributions = distributionRepository.findByCreatedByAndYear(employeeId, yearId);
       
        if (distributions == null || distributions.isEmpty()) {
            System.out.println("DEBUG: No distributions found for employeeId=" + employeeId + ", yearId=" + yearId);
            return 0L;
        }
       
        System.out.println("DEBUG: Found " + distributions.size() + " distributions for employeeId=" + employeeId + ", yearId=" + yearId);
       
        long totalCount = 0L;
        long totalApplicationsChecked = 0L;
       
        // For each distribution range
        for (Distribution dist : distributions) {
            int startNo = dist.getAppStartNo();
            int endNo = dist.getAppEndNo();
           
            System.out.println("DEBUG: Checking range " + startNo + " to " + endNo);
           
            // Get all applications in this range from AppStatusTrackView
            List<AppStatusTrackView> applications = appStatusTrackViewRepository.findByApplicationNumberRange(startNo, endNo);
           
            if (applications == null || applications.isEmpty()) {
                System.out.println("DEBUG: No applications found in range " + startNo + " to " + endNo);
                continue;
            }
           
            System.out.println("DEBUG: Found " + applications.size() + " applications in range " + startNo + " to " + endNo);
            totalApplicationsChecked += applications.size();
           
            // Count by status type
            for (AppStatusTrackView app : applications) {
                String status = app.getStatus();
                if (status == null) {
                    System.out.println("DEBUG: Application " + app.getNum() + " has null status");
                    continue;
                }
               
                // Normalize status to lowercase for comparison
                String normalizedStatus = status.toLowerCase().trim();
               
                switch (statusType.toLowerCase()) {
                    case "sold":
                        // Sold includes: "not confirmed" and "fast sale" (with all variations)
                        // Handle variations: "fast sale", "fastsale", "fast_sale", "Fast Sale", "FAST SALE", etc.
                        // Remove all spaces, underscores, and hyphens for flexible matching
                        String normalizedStatusNoSpace = normalizedStatus.replaceAll("[\\s_\\-]+", "");
                        if (normalizedStatus.equals("not confirmed") ||
                            normalizedStatus.equals("fast sale") ||
                            normalizedStatusNoSpace.equals("fastsale")) {
                            totalCount++;
                            System.out.println("DEBUG: Application " + app.getNum() + " counted as SOLD (status: " + status + ")");
                        }
                        break;
                    case "confirmed":
                        if (normalizedStatus.equals("confirmed")) {
                            totalCount++;
                        }
                        break;
                    case "damaged":
                        if (normalizedStatus.equals("damaged")) {
                            totalCount++;
                        }
                        break;
                    case "unavailable":
                        // Handle variations: "unavailable", "un available", "un_available"
                        // Remove all spaces, underscores, and hyphens for flexible matching
                        String normalizedStatusNoSpaceUnavailable = normalizedStatus.replaceAll("[\\s_\\-]+", "");
                        if (normalizedStatus.equals("unavailable") ||
                            normalizedStatus.equals("un available") ||
                            normalizedStatus.equals("un_available") ||
                            normalizedStatusNoSpaceUnavailable.equals("unavailable")) {
                            totalCount++;
                            System.out.println("DEBUG: Application " + app.getNum() + " counted as UNAVAILABLE (original: " + status + ")");
                        }
                        break;
                    case "with pro":
                        // Handle variations: "with pro", "withpro", "with_pro"
                        // Also include "Payment Pending" (from database) and its variations
                        // Note: normalizedStatus is already lowercase, so "Payment Pending" becomes "payment pending"
                        String normalizedStatusNoSpaceForPro = normalizedStatus.replaceAll("[\\s_\\-]+", "");
                        if (normalizedStatus.equals("with pro") ||
                            normalizedStatus.equals("withpro") ||
                            normalizedStatus.equals("with_pro") ||
                            normalizedStatus.equals("payment pending") ||  // handles "Payment Pending", "payment pending", "PAYMENT PENDING"
                            normalizedStatus.equals("paymentpending") ||
                            normalizedStatus.equals("payment_pending") ||
                            normalizedStatusNoSpaceForPro.equals("paymentpending")) {
                            totalCount++;
                            System.out.println("DEBUG: Application " + app.getNum() + " counted as WITH PRO (original status: '" + status + "', normalized: '" + normalizedStatus + "')");
                        }
                        break;
                }
            }
        }
       
        System.out.println("DEBUG: StatusType=" + statusType + ", TotalCount=" + totalCount + ", TotalApplicationsChecked=" + totalApplicationsChecked);
        return totalCount;
    }
   
    private GraphResponseDTO generateGraphData(Integer employeeId, Integer currentYearId) {
        // Handle null currentYearId
        if (currentYearId == null) {
            currentYearId = 0; // Default to 0 if null
        }
       
        // Get previous 4 years (current year + 3 previous years)
        List<Integer> yearIds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            yearIds.add(currentYearId - i);
        }
       
        // Get AcademicYear entities for year labels
        List<AcademicYear> academicYears = academicYearRepository.findByAcdcYearIdIn(yearIds);
        Map<Integer, AcademicYear> yearMap = new HashMap<>();
        if (academicYears != null) {
            yearMap = academicYears.stream()
                .collect(Collectors.toMap(AcademicYear::getAcdcYearId, y -> y));
        }
       
        // Build graph bar data for all 4 years - always return 4 years with zeros if no data
        List<GraphBarDTO> barList = new ArrayList<>();
        for (Integer yearId : yearIds) {
            // Calculate Issued the SAME way as metrics card: Total App (AdminApp) - Available (BalanceTrack)
            Long yearTotal = adminAppRepository.sumTotalAppByEmployeeAndAcademicYear(employeeId, yearId);
            int totalApplications = yearTotal != null ? yearTotal.intValue() : 0;
            int available = calculateAvailableByAmount(employeeId, yearId);
            int issued = totalApplications - available; // Same calculation as metrics card
            
            // Get Sold and Confirmed counts separately for this employee and year
            long sold = countApplicationsByStatus(employeeId, yearId, "sold");
            long confirmed = countApplicationsByStatus(employeeId, yearId, "confirmed");
            long soldAndConfirmed = sold + confirmed; // Combine Sold + Confirmed
           
            AcademicYear year = yearMap.get(yearId);
            String yearLabel = year != null ? year.getAcademicYear() : "Year " + yearId;
           
            // Calculate percentages - default to 0 if no data
            int issuedPercent = 0;
            int soldPercent = 0;
            if (issued > 0) {
                issuedPercent = 100; // 100% as baseline when data exists
                soldPercent = (int) Math.round((soldAndConfirmed * 100.0) / issued);
            }
           
            GraphBarDTO dto = new GraphBarDTO();
            dto.setYear(yearLabel);
            dto.setIssuedPercent(issuedPercent);
            dto.setSoldPercent(soldPercent);
            dto.setIssuedCount((int) issued);
            dto.setSoldCount((int) soldAndConfirmed); // Store Sold + Confirmed combined
           
            barList.add(dto);
        }
       
        GraphResponseDTO response = new GraphResponseDTO();
        response.setGraphBarData(barList);
       
        return response;
    }
}