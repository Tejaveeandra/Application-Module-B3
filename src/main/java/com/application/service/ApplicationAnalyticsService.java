package com.application.service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.application.dto.CombinedAnalyticsDTO;
import com.application.dto.GraphBarDTO;
import com.application.dto.GraphDTO;
import com.application.dto.GraphSoldSummaryDTO;
import com.application.dto.MetricDTO;
import com.application.dto.MetricsAggregateDTO;
import com.application.dto.MetricsDataDTO;
import com.application.dto.YearlyGraphPointDTO;
import com.application.entity.AcademicYear;
import com.application.entity.Campus;
import com.application.entity.Dgm;
import com.application.entity.SCEmployeeEntity;
import com.application.entity.ZonalAccountant;
import com.application.repository.AcademicYearRepository;
import com.application.repository.AppStatusTrackRepository;
import com.application.repository.CampusRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.DgmRepository;
import com.application.repository.SCEmployeeRepository;
import com.application.repository.UserAppSoldRepository;
import com.application.repository.ZonalAccountantRepository;
import com.application.repository.ZoneRepository;
import com.application.entity.Zone;
import com.application.dto.GenericDropdownDTO_Dgm;
import com.application.dto.GenericDropdownDTO;
@Service
public class ApplicationAnalyticsService {
    @Autowired
    private UserAppSoldRepository userAppSoldRepository;
    @Autowired
    private AppStatusTrackRepository appStatusTrackRepository;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private SCEmployeeRepository scEmployeeRepository;
    @Autowired
    private ZonalAccountantRepository zonalAccountantRepository;
    @Autowired
    private DgmRepository dgmRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private DistributionRepository distributionRepository;
@Autowired
private ZoneRepository zoneRepository;
public CombinedAnalyticsDTO getRollupAnalytics(Integer empId) {
System.out.println("========================================");
System.out.println("DEBUG: getRollupAnalytics called for empId: " + empId);
        // 1. Get Basic Employee Details
        List<SCEmployeeEntity> employeeList = scEmployeeRepository.findByEmpId(empId);
        if (employeeList.isEmpty()) {
System.out.println("❌ ERROR: Employee " + empId + " not found in SCEmployeeEntity");
            return createEmptyAnalytics("Invalid Employee", empId, "Employee not found", "N/A");
        }
        SCEmployeeEntity employee = employeeList.get(0);
        String role = employee.getEmpStudApplicationRole();
        String designation = employee.getDesignationName();
System.out.println("✓ Employee found: ID=" + empId + ", Role=" + role + ", Designation=" + designation);
if (role == null) {
System.out.println("❌ ERROR: Employee " + empId + " has null role");
return createEmptyAnalytics("Null Role", empId, "No Role", designation);
}
        // 2. Route based on Role
        String trimmedRole = role.trim();
System.out.println("Routing to analytics method for role: " + trimmedRole);
        if (trimmedRole.equalsIgnoreCase("DGM")) {
            return getDgmDirectAnalytics(employee); // NEW METHOD
        }
        else if (trimmedRole.equalsIgnoreCase("ZONAL ACCOUNTANT")) {
            return getZonalDirectAnalytics(employee); // NEW METHOD
        }
        else {
// For PRO and other roles, show campus data if employee has a campus
System.out.println("Employee campusId: " + employee.getEmpCampusId() + ", campusName: " + employee.getCampusName());
return getCampusDirectAnalytics(employee);
        }
    }
    // --- "NORMAL" ROUTER METHOD (Unchanged) ---
    /**
     * This is the original "normal" view for DGM, Zonal, or PRO.
     * It shows data for *only* their direct entity.
     */
   public CombinedAnalyticsDTO getAnalyticsForEmployee(Integer empId) {
    List<SCEmployeeEntity> employeeList = scEmployeeRepository.findByEmpId(empId);
    // Employee not found
    if (employeeList.isEmpty()) {
        System.err.println("No employee found with ID: " + empId);
        // Pass "N/A" for designation
        return createEmptyAnalytics("Invalid Employee", empId, "Employee not found", "N/A");
    }
    SCEmployeeEntity employee = employeeList.get(0);
    String role = employee.getEmpStudApplicationRole();
    String designation = employee.getDesignationName(); // <--- GET DESIGNATION HERE
    // Null role
    if (role == null) {
         System.err.println("Employee " + empId + " has a null role.");
         return createEmptyAnalytics("Null Role", empId, "Employee has no role", designation); // <--- PASS DESIGNATION
    }
    String trimmedRole = role.trim();
    CombinedAnalyticsDTO analytics;
    if (trimmedRole.equalsIgnoreCase("DGM")) {
        analytics = getDgmAnalytics(empId);
        analytics.setRole("DGM");
        analytics.setEntityName(employee.getFirstName() + " " + employee.getLastName());
        analytics.setEntityId(empId);
    } else if (trimmedRole.equalsIgnoreCase("ZONAL ACCOUNTANT")) {
        int zoneId = employee.getZoneId();
        analytics = getZoneAnalytics((long) zoneId);
        analytics.setRole("Zonal Account");
        analytics.setEntityName(employee.getZoneName());
        analytics.setEntityId(zoneId);
    } else if (trimmedRole.equalsIgnoreCase("PRO")) {
        int campusId = employee.getEmpCampusId();
        analytics = getCampusAnalytics((long) campusId);
        analytics.setRole("PRO");
        analytics.setEntityName(employee.getCampusName());
        analytics.setEntityId(campusId);
    } else {
        System.err.println("Unrecognized role '" + role + "' for empId: " + empId);
        return createEmptyAnalytics(role, empId, "Unrecognized role", designation); // <--- PASS DESIGNATION
    }
    // <--- SET DESIGNATION BEFORE RETURNING --->
    analytics.setDesignationName(designation);
    return analytics;
}
    private CombinedAnalyticsDTO createEmptyAnalytics(String role, Integer id, String name, String designationName) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        analytics.setRole(role);
        analytics.setDesignationName(designationName); // <--- Set designation here
        analytics.setEntityId(id);
        analytics.setEntityName(name);
        return analytics;
    }
    // --- CORE ANALYTICS METHODS (Unchanged) ---
    public CombinedAnalyticsDTO getZoneAnalytics(Long id) {
        // System.out.println("========================================");
        // System.out.println("🔍 DEBUG: getZoneAnalytics called");
        // System.out.println("Zone ID (Long): " + id);
        Integer zoneIdInt = id.intValue();
        // System.out.println("Zone ID (Integer): " + zoneIdInt);
        // System.out.println("========================================");
        
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();

        // 1. Graph Data (ALIGNED WITH CARDS)
        // System.out.println("📊 Getting Graph Data for Zone (Aligned with Cards): " + zoneIdInt);
        analytics.setGraphData(getGraphData(
            (yearId) -> {
                // Get AppStatusTrack metrics (Type 2 - Zone only for issued)
                MetricsAggregateDTO statusMetrics = appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneIdInt, yearId)
                    .orElse(new MetricsAggregateDTO());
                
                // Add Admin→DGM and Admin→Campus distributions to issued count
                Integer adminToDgmDist = distributionRepository.sumAdminToDgmDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                Integer adminToCampusDist = distributionRepository.sumAdminToCampusDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                
                long totalIssued = statusMetrics.appIssued() + adminToDgmDist + adminToCampusDist;
                
                return Optional.of(new GraphSoldSummaryDTO(
                    totalIssued, 
                    statusMetrics.appSold()
                ));
            },
            () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt)
        ));

        // 2. Metrics Data
        // System.out.println("📈 Getting Metrics Data for Zone: " + zoneIdInt);
        // System.out.println("🔍 Checking years from AppStatusTrack (filtered by is_active = 1)...");
        // List<Integer> years = appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt);
        // System.out.println("✅ Found " + years.size() + " year(s) for Zone " + zoneIdInt + ": " + years);
        
        analytics.setMetricsData(
            getMetricsData(
                (yearId) -> {
                    // System.out.println("🔍 getMetricsByZoneIdAndYear called - Zone: " + zoneIdInt + ", Year: " + yearId);
                    // Get AppStatusTrack metrics (covers Zone to DGM, DGM to Campus, Zone to Campus)
                    // AppStatusTrack already includes all flows with app_issued_type_id IN (2, 3, 4)
                    // This already captures: Zone→DGM, DGM→Campus, Zone→Campus flows
                    MetricsAggregateDTO statusMetrics = appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneIdInt, yearId)
                        .orElse(new MetricsAggregateDTO());
                    
                    // Get issued breakdown by app_issued_type_id
                    List<Object[]> issuedBreakdown = appStatusTrackRepository.getIssuedBreakdownByZoneIdAndYear(zoneIdInt, yearId);
                    
                    // Get distribution counts
                    Integer adminToZoneDist = distributionRepository.sumAdminToZoneDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                    Integer adminToDgmDist = distributionRepository.sumAdminToDgmDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                    Integer adminToCampusDist = distributionRepository.sumAdminToCampusDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                    
                    // Display detailed breakdown for Zone Issued Calculation
                    System.out.println("========================================");
                    System.out.println("📊 ZONE ISSUED CALCULATION BREAKDOWN");
                    System.out.println("========================================");
                    System.out.println("Zone ID: " + zoneIdInt);
                    System.out.println("Academic Year ID: " + yearId);
                    System.out.println("----------------------------------------");
                    System.out.println("📈 ISSUED COUNT FROM AppStatusTrack (Type 2 - Zone ONLY):");
                    
                    long issuedType2 = 0; // Zone
                    
                    for (Object[] row : issuedBreakdown) {
                        Integer typeId = ((Number) row[0]).intValue();
                        Long issued = ((Number) row[1]).longValue();
                        
                        if (typeId == 2) {
                            issuedType2 = issued;
                            System.out.println("   app_issued_type_id = 2 (Zone): " + issued);
                        }
                    }
                    
                    System.out.println("----------------------------------------");
                    System.out.println("📊 TOTAL APPLICATIONS FROM AppStatusTrack:");
                    System.out.println("   totalApp (app_issued_type_id = 2 - Zone): " + statusMetrics.totalApp());
                    System.out.println("   appIssued (app_issued_type_id = 2 - Zone): " + statusMetrics.appIssued());
                    System.out.println("----------------------------------------");
                    System.out.println("📦 DISTRIBUTION TABLE COUNTS (Direct Admin Distributions):");
                    System.out.println("   Admin → Zone: " + adminToZoneDist + " (NOT added - already in AppStatusTrack)");
                    System.out.println("   Admin → DGM (Direct): " + adminToDgmDist + " (ADDED to totalApp and issued)");
                    System.out.println("   Admin → Campus (Direct): " + adminToCampusDist + " (ADDED to totalApp and issued)");
                    System.out.println("----------------------------------------");
                    
                    // Add Admin→DGM and Admin→Campus distributions to issued and totalApp
                    // Note: Admin→Zone is NOT added because it's already reflected in AppStatusTrack Type 2
                    long totalIssued = statusMetrics.appIssued() + adminToDgmDist + adminToCampusDist;
                    long totalApp = statusMetrics.totalApp() + adminToDgmDist + adminToCampusDist;
                    
                    System.out.println("✅ FINAL ZONE CALCULATION:");
                    System.out.println("   Total Applications:");
                    System.out.println("     - From AppStatusTrack (Type 2 - Zone): " + statusMetrics.totalApp());
                    System.out.println("     - Admin → DGM Distribution (Direct): " + adminToDgmDist);
                    System.out.println("     - Admin → Campus Distribution (Direct): " + adminToCampusDist);
                    System.out.println("     - TOTAL APPLICATIONS: " + totalApp);
                    System.out.println("   Issued Count:");
                    System.out.println("     - Zone (Type 2 from AppStatusTrack): " + issuedType2);
                    System.out.println("     - Admin → DGM Distribution (Direct): " + adminToDgmDist);
                    System.out.println("     - Admin → Campus Distribution (Direct): " + adminToCampusDist);
                    System.out.println("     - TOTAL ISSUED: " + totalIssued);
                    System.out.println("----------------------------------------");
                    System.out.println("🔍 VERIFICATION:");
                    System.out.println("   If DGM Total = 610, Zone Total should include:");
                    System.out.println("     - DGM's AppStatusTrack Type 3 (310) should NOT be in Zone");
                    System.out.println("     - DGM's Admin→DGM (100) SHOULD be in Zone: " + adminToDgmDist);
                    System.out.println("     - DGM's Admin→Campus (200) SHOULD be in Zone: " + adminToCampusDist);
                    System.out.println("     - Zone's own AppStatusTrack Type 2: " + statusMetrics.totalApp());
                    System.out.println("     - Expected Zone Total: " + statusMetrics.totalApp() + " + " + adminToDgmDist + " + " + adminToCampusDist + " = " + totalApp);
                    System.out.println("========================================");
                    
                    // Return metrics with added distribution counts
                    return Optional.of(new MetricsAggregateDTO(
                        totalApp, // totalApp + distributions
                        statusMetrics.appSold(),
                        statusMetrics.appConfirmed(),
                        statusMetrics.appAvailable(),
                        statusMetrics.appUnavailable(),
                        statusMetrics.appDamaged(),
                        totalIssued // issued + distributions
                    ));
                },
                (yearId) -> {
                    // System.out.println("🔍 getProMetricByZoneId_FromStatus called - Zone: " + zoneIdInt + ", Year: " + yearId);
                    Optional<Long> result = appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneIdInt, yearId);
                    // System.out.println("✅ PRO Metric: " + (result.isPresent() ? result.get() : "Not found"));
                    return result;
                },
                () -> {
                    // System.out.println("🔍 findDistinctYearIdsByZoneId called for Zone: " + zoneIdInt);
                    List<Integer> yearList = appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt);
                    // System.out.println("✅ Years returned: " + yearList);
                    return yearList;
                }
            )
        );

        // System.out.println("========================================");
        return analytics;
    }
    public CombinedAnalyticsDTO getDgmAnalytics(Integer dgmEmpId) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        analytics.setGraphData(getGraphData(
            (yearId) -> userAppSoldRepository.getSalesSummaryByDgm(dgmEmpId, yearId),
            () -> userAppSoldRepository.findDistinctYearIdsByDgm(dgmEmpId)
        ));
        analytics.setMetricsData(
            getMetricsData(
                (yearId) -> appStatusTrackRepository.getMetricsByEmployeeAndYear(dgmEmpId, yearId),
                (yearId) -> userAppSoldRepository.getProMetricByDgm(dgmEmpId, yearId),
                () -> appStatusTrackRepository.findDistinctYearIdsByEmployee(dgmEmpId)
            )
        );
        return analytics;
    }
 // In AnalyticsService.java
public CombinedAnalyticsDTO getEmployeeAnalytics(Long empId) {
    Integer empIdInt = empId.intValue();
    // 1. Get all Campus IDs associated with this Employee in the DGM table
    List<Integer> campusIds = dgmRepository.findCampusIdsByEmployeeId(empIdInt);
    if (campusIds.isEmpty()) {
        throw new RuntimeException("No active DGM records found for Employee ID: " + empId);
    }
    // 2. Get Zone ID for this DGM employee
    Integer zoneId = dgmRepository.findZoneIdByEmpId(empIdInt).orElse(null);
    if (zoneId == null) {
        throw new RuntimeException("No zone found for Employee ID: " + empId);
    }
    
    CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
    // 3. Use AppStatusTrack for graph data with app_issued_type_id = 4 (Campus/PRO)
    // Calculate issued as totalApp - appAvailable, then add distributions with issued_to_type_id = 4
    analytics.setGraphData(getGraphData(
        (yearId) -> {
            // Get totalApp and appAvailable from AppStatusTrack with app_issued_type_id = 4 (Campus/PRO, filtered by campusIds and zoneId)
            Optional<Object[]> totalAppAndAvailable = appStatusTrackRepository.getTotalAppAndAvailableByCampusIdsAndYearForDgmGraph(campusIds, zoneId, yearId);
            long totalApp = 0L;
            long appAvailable = 0L;
            if (totalAppAndAvailable.isPresent()) {
                Object[] result = totalAppAndAvailable.get();
                totalApp = ((Number) result[0]).longValue();
                appAvailable = ((Number) result[1]).longValue();
            }
            // Calculate issued as totalApp - appAvailable (entity_id = 4)
            long issuedFromStatus = totalApp - appAvailable;
            // Get admin-to-campus distribution count (Admin→Campus: issued_by_type_id = 1, issued_to_type_id = 4, filtered by campusIds)
            Integer adminToCampusDist = distributionRepository.sumAdminToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                .orElse(0);
            // Get zone-to-campus distribution count (Zone→Campus: issued_by_type_id = 2, issued_to_type_id = 4, filtered by campusIds)
            Integer zoneToCampusDist = distributionRepository.sumZoneToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                .orElse(0);
            // Add distribution counts to issued (ONLY issued_to_type_id = 4: Admin→Campus + Zone→Campus)
            // Note: Admin→DGM (issued_to_type_id = 3) is NOT included in issued count
            long totalIssued = issuedFromStatus + adminToCampusDist + zoneToCampusDist;
            // Get sold count from AppStatusTrack with app_issued_type_id = 4
            MetricsAggregateDTO proMetrics = appStatusTrackRepository.getSoldConfirmedUnavailableDamagedByCampusIdsAndYearForDgm(campusIds, zoneId, yearId)
                .orElse(new MetricsAggregateDTO());
            return Optional.of(new GraphSoldSummaryDTO(totalIssued, proMetrics.appSold()));
        },
        () -> {
            // Find distinct years from AppStatusTrack with app_issued_type_id = 4 (Campus/PRO) for graph data
            return appStatusTrackRepository.findDistinctYearIdsByCampusIdsForDgmGraph(campusIds, zoneId);
        }
    ));
    // 4. Get metrics data with distribution count added to issued count
    analytics.setMetricsData(
        getMetricsData(
            (yearId) -> {
                // Get AppStatusTrack metrics with app_issued_type_id = 3 (DGM→Campus: for totalApp, appIssued, appAvailable, filtered by campusIds and zoneId)
                // totalApp is taken from AppStatusTrack where app_issued_type_id = 3 and campus_id IN campusIds (e.g., 932)
                MetricsAggregateDTO statusMetrics = appStatusTrackRepository.getMetricsByCampusIdsAndYearForDgm(campusIds, zoneId, yearId)
                    .orElse(new MetricsAggregateDTO());
                // Get sold, confirmed, unavailable, damaged with app_issued_type_id = 4 (Campus/PRO, filtered by campusIds and zoneId)
                MetricsAggregateDTO proMetrics = appStatusTrackRepository.getSoldConfirmedUnavailableDamagedByCampusIdsAndYearForDgm(campusIds, zoneId, yearId)
                    .orElse(new MetricsAggregateDTO());
                // Get admin-to-campus distribution count (Admin→Campus: issued_by_type_id = 1, issued_to_type_id = 4, filtered by campusIds)
                Integer adminToCampusDist = distributionRepository.sumAdminToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                    .orElse(0);
                // Get zone-to-campus distribution count (Zone→Campus: issued_by_type_id = 2, issued_to_type_id = 4, filtered by campusIds)
                Integer zoneToCampusDist = distributionRepository.sumZoneToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                    .orElse(0);
                
                // Display detailed breakdown for DGM Total Applications Calculation
                System.out.println("========================================");
                System.out.println("📊 DGM TOTAL APPLICATIONS CALCULATION");
                System.out.println("========================================");
                System.out.println("Employee ID: " + empIdInt);
                System.out.println("Zone ID: " + zoneId);
                System.out.println("Campus IDs: " + campusIds);
                System.out.println("Academic Year ID: " + yearId);
                System.out.println("----------------------------------------");
                System.out.println("📈 TOTAL APPLICATIONS FROM AppStatusTrack:");
                System.out.println("   totalApp (app_issued_type_id = 3, campusIds): " + statusMetrics.totalApp());
                System.out.println("----------------------------------------");
                System.out.println("📦 DISTRIBUTION TABLE COUNTS (ONLY issued_to_type_id = 4):");
                System.out.println("   Admin → Campus: " + adminToCampusDist + " (ADDED to totalApp AND issued)");
                System.out.println("   Zone → Campus: " + zoneToCampusDist + " (ADDED to totalApp AND issued)");
                System.out.println("   Note: Admin→DGM is NOT added (data comes from AppStatusTrack)");
                System.out.println("----------------------------------------");
                
                // Combine: totalApp includes AppStatusTrack + distributions with issued_to_type_id = 4 (Admin→Campus + Zone→Campus)
                // Issued count includes AppStatusTrack + distributions with issued_to_type_id = 4 (Admin→Campus + Zone→Campus)
                // Note: Admin→DGM is NOT added because it's already included in AppStatusTrack data
                long totalDistCount = adminToCampusDist + zoneToCampusDist; // Only issued_to_type_id = 4
                long finalTotalApp = statusMetrics.totalApp() + totalDistCount;
                
                System.out.println("✅ FINAL DGM CALCULATION:");
                System.out.println("   Total Applications:");
                System.out.println("     - From AppStatusTrack (Type 3): " + statusMetrics.totalApp());
                System.out.println("     - Admin → Campus Distribution: " + adminToCampusDist + " (issued_to_type_id = 4)");
                System.out.println("     - Zone → Campus Distribution: " + zoneToCampusDist + " (issued_to_type_id = 4)");
                System.out.println("     - TOTAL APPLICATIONS: " + finalTotalApp);
                System.out.println("   Issued Count (ONLY issued_to_type_id = 4):");
                System.out.println("     - From AppStatusTrack (Type 3): " + statusMetrics.appIssued());
                System.out.println("     - Admin → Campus Distribution: " + adminToCampusDist + " (issued_to_type_id = 4)");
                System.out.println("     - Zone → Campus Distribution: " + zoneToCampusDist + " (issued_to_type_id = 4)");
                System.out.println("     - TOTAL ISSUED: " + (statusMetrics.appIssued() + totalDistCount));
                System.out.println("========================================");
                
                return Optional.of(new MetricsAggregateDTO(
                    finalTotalApp, // Add ONLY issued_to_type_id = 4 distributions (Admin→Campus + Zone→Campus) to grand total
                    proMetrics.appSold(), // From app_issued_type_id = 4
                    proMetrics.appConfirmed(), // From app_issued_type_id = 4
                    statusMetrics.appAvailable(), // From app_issued_type_id = 3
                    proMetrics.appUnavailable(), // From app_issued_type_id = 4
                    proMetrics.appDamaged(), // From app_issued_type_id = 4
                    statusMetrics.appIssued() + totalDistCount // Add ONLY issued_to_type_id = 4 distributions (Admin→Campus + Zone→Campus) to issued
                ));
            },
            (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
            () -> appStatusTrackRepository.findDistinctYearIdsByCampusIdsForDgm(campusIds, zoneId)
        )
    );
    return analytics;
}
    public CombinedAnalyticsDTO getCampusAnalytics(Long campusId) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        // Use AppStatusTrack with app_issued_type_id = 4 for campus analytics
        analytics.setGraphData(getGraphData(
            (yearId) -> appStatusTrackRepository.getSalesSummaryByCampusAndYearWithType4(campusId, yearId),
            () -> appStatusTrackRepository.findDistinctYearIdsByCampusWithType4(campusId)
        ));
        analytics.setMetricsData(
            getMetricsData(
                (yearId) -> appStatusTrackRepository.getMetricsByCampusAndYearWithType4(campusId, yearId),
                // Use AppStatusTrack repo with app_issued_type_id = 4
                (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusId.intValue(), yearId),
                () -> appStatusTrackRepository.findDistinctYearIdsByCampusWithType4(campusId)
            )
        );
        return analytics;
    }
public GraphDTO getGraphDataByZoneIdAndAmount(Integer zoneId, Float amount) {
        if (zoneId == null || amount == null) {
            GraphDTO emptyGraph = new GraphDTO();
            emptyGraph.setTitle("Error: Zone ID and Amount must be provided.");
            emptyGraph.setYearlyData(new ArrayList<>());
            return emptyGraph;
        }
        // This leverages the generic getGraphData helper with new repository functions
        return getGraphData(
            // Data Fetcher: Function<Integer, Optional<GraphSoldSummaryDTO>> (takes yearId)
            (yearId) -> userAppSoldRepository.getSalesSummaryByZoneAndAmount(zoneId, yearId, amount),
            // Year Fetcher: Supplier<List<Integer>> (takes no arguments)
            () -> userAppSoldRepository.findDistinctYearIdsByZoneAndAmount(zoneId, amount)
        );
    }
public GraphDTO getGraphDataByCampusIdAndAmount(Integer campusId, Float amount) {
    if (campusId == null || amount == null) {
        GraphDTO emptyGraph = new GraphDTO();
        emptyGraph.setTitle("Error: Campus ID and Amount must be provided.");
        emptyGraph.setYearlyData(new ArrayList<>());
        return emptyGraph;
    }
    // This leverages the generic getGraphData helper with new repository functions
    return getGraphData(
        // Data Fetcher: Function<Integer, Optional<GraphSoldSummaryDTO>> (takes yearId)
        (yearId) -> userAppSoldRepository.getSalesSummaryByCampusAndAmount(campusId, yearId, amount),
        // Year Fetcher: Supplier<List<Integer>> (takes no arguments)
        () -> userAppSoldRepository.findDistinctYearIdsByCampusAndAmount(campusId, amount)
    );
}
//private CombinedAnalyticsDTO getDgmDirectAnalytics(SCEmployeeEntity employee) {
// int empId = employee.getEmpId();
//
// // 1. Fetch DGM Record to get Campus ID
// // Assuming findByEmployee_EmpId returns List or Optional. Taking first for safety.
// Dgm dgmRecord = dgmRepository.lookupByEmpId(empId).orElse(null);
//
// if (dgmRecord == null || dgmRecord.getCampus() == null) {
// return createEmptyAnalytics("DGM", empId, "DGM not mapped to a Campus", employee.getDesignationName());
// }
//
// int campusId = dgmRecord.getCampus().getCampusId(); // Pick Campus ID
// String campusName = dgmRecord.getCampus().getCampusName(); // Assuming you have name in Campus entity
//
// // 2. Get Data using Campus ID directly
// CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//        
// // Use the new Repo methods created in Step 1
// analytics.setGraphData(getGraphDataForCampus(campusId));
// analytics.setMetricsData(getMetricsDataForCampus(campusId));
//
// // 3. Set Header Info
// analytics.setRole("DGM");
// analytics.setDesignationName(employee.getDesignationName());
// analytics.setEntityName(campusName); // Showing Campus Name
// analytics.setEntityId(campusId);
//
// return analytics;
// }
private CombinedAnalyticsDTO getDgmDirectAnalytics(SCEmployeeEntity employee) {
    int empId = employee.getEmpId();
System.out.println("DEBUG: getDgmDirectAnalytics for empId: " + empId);
    // 1. Fetch ALL DGM Records for this employee to get ALL Campus IDs
    // Assuming your dgmRepository has: List<Dgm> findByEmployee_EmpId(int empId)
    List<Dgm> dgmRecords = dgmRepository.findAllByEmployeeId(empId);
System.out.println("DEBUG: Found " + dgmRecords.size() + " DGM records for employee " + empId);
    if (dgmRecords.isEmpty()) {
System.out.println("❌ ERROR: No DGM records found for employee " + empId);
        return createEmptyAnalytics("DGM", empId, "No Campuses mapped to this DGM", employee.getDesignationName());
    }
    // Extract all Campus IDs into a List
    List<Integer> campusIds = dgmRecords.stream()
            .map(d -> d.getCampus().getCampusId())
            .collect(Collectors.toList());
System.out.println("DEBUG: Campus IDs for DGM: " + campusIds);
    // 2. Get Aggregated Data using the List of IDs
    CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
GraphDTO graphData = getGraphDataForCampuses(campusIds);
MetricsDataDTO metricsData = getMetricsDataForCampuses(campusIds);
analytics.setGraphData(graphData);
analytics.setMetricsData(metricsData);
System.out.println("DEBUG: Graph data - yearlyData size: " + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
System.out.println("DEBUG: Metrics data - metrics size: " + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
    // 3. Set Header Info
    analytics.setRole("DGM");
    analytics.setDesignationName(employee.getDesignationName());
    // For entity name, you can show a count or join names: "3 Campuses" or "Campus A, Campus B..."
    analytics.setEntityName(dgmRecords.size() + " Campuses Managed");
    analytics.setEntityId(empId); // Using EmpId as the identifier for the group
System.out.println("========================================");
    return analytics;
}
    /**
     * PRIVATE: Gets analytics for a Zonal Accountant's *managed DGMs*.
     */
private CombinedAnalyticsDTO getZonalDirectAnalytics(SCEmployeeEntity employee) {
        int empId = employee.getEmpId();
System.out.println("DEBUG: getZonalDirectAnalytics for empId: " + empId);
        // 1. Fetch ZonalAccountant Record to get Zone ID (handle multiple results by taking first)
        List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
System.out.println("DEBUG: Found " + (zonalRecords != null ? zonalRecords.size() : 0) + " ZonalAccountant records for employee " + empId);
        if (zonalRecords == null || zonalRecords.isEmpty()) {
System.out.println("❌ ERROR: No ZonalAccountant records found for employee " + empId);
            return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone", employee.getDesignationName());
        }
        // Get the first active record (most recent based on zone_acct_id DESC if needed)
        ZonalAccountant zonalRecord = zonalRecords.get(0);
        if (zonalRecord.getZone() == null) {
            return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone", employee.getDesignationName());
        }
        int zoneId = zonalRecord.getZone().getZoneId(); // Pick Zone ID
        String zoneName = zonalRecord.getZone().getZoneName();
System.out.println("DEBUG: Zone ID: " + zoneId + ", Zone Name: " + zoneName);
        // 2. Get Data using Zone ID directly
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        // Use the new Repo methods created in Step 1
GraphDTO graphData = getGraphDataForZone(zoneId);
MetricsDataDTO metricsData = getMetricsDataForZone(zoneId);
analytics.setGraphData(graphData);
analytics.setMetricsData(metricsData);
System.out.println("DEBUG: Graph data - yearlyData size: " + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
System.out.println("DEBUG: Metrics data - metrics size: " + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
        // 3. Set Header Info
        analytics.setRole("Zonal Accountant");
        analytics.setDesignationName(employee.getDesignationName());
        analytics.setEntityName(zoneName); // Showing Zone Name
        analytics.setEntityId(zoneId);
System.out.println("========================================");
        return analytics;
    }
/**
* PRIVATE: Gets analytics for PRO and other roles - shows campus data for the employee's campus.
*/
private CombinedAnalyticsDTO getCampusDirectAnalytics(SCEmployeeEntity employee) {
int empId = employee.getEmpId();
int campusId = employee.getEmpCampusId();
String campusName = employee.getCampusName();
String role = employee.getEmpStudApplicationRole();
System.out.println("DEBUG: getCampusDirectAnalytics for empId: " + empId + ", campusId: " + campusId + ", campusName: " + campusName);
// Check if employee has a valid campus ID
if (campusId <= 0) {
System.out.println("❌ ERROR: Employee " + empId + " has invalid campusId: " + campusId);
return createEmptyAnalytics(role != null ? role : "Unknown", empId, "Employee not mapped to a Campus", employee.getDesignationName());
}
System.out.println("✓ Fetching analytics data for campusId: " + campusId);
// Get Data using Campus ID directly
CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
GraphDTO graphData = getGraphDataForCampus(campusId);
MetricsDataDTO metricsData = getMetricsDataForCampus(campusId);
analytics.setGraphData(graphData);
analytics.setMetricsData(metricsData);
System.out.println("DEBUG: Graph data - yearlyData size: " + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
System.out.println("DEBUG: Metrics data - metrics size: " + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
// Set Header Info
analytics.setRole(role != null ? role : "Employee");
analytics.setDesignationName(employee.getDesignationName());
analytics.setEntityName(campusName != null ? campusName : "Campus " + campusId);
analytics.setEntityId(campusId);
System.out.println("========================================");
return analytics;
}
    // --- PRIVATE HELPER METHODS for ROLLUPS (Unchanged) ---
//=========================================================================
// DGM / CAMPUS DIRECT HELPERS
// =========================================================================
private GraphDTO getGraphDataForCampus(Integer campusId) {
    return getGraphData(
        (yearId) -> {
            Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository.getMetricsByCampusIdAndYear(campusId, yearId);
            return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
        },
        () -> appStatusTrackRepository.findDistinctYearIdsByCampusId(campusId)
    );
}
private MetricsDataDTO getMetricsDataForCampus(Integer campusId) {
    return getMetricsData(
        // 1. Main Metrics (Total, Issued, Damaged, etc.)
        (yearId) -> appStatusTrackRepository.getMetricsByCampusIdAndYear(campusId, yearId),
        // 2. Pro Metric (Sold count specifically for the card)
        (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusId, yearId),
        // 3. Distinct Years
        () -> appStatusTrackRepository.findDistinctYearIdsByCampusId(campusId)
    );
}
private GraphDTO getGraphDataForCampuses(List<Integer> campusIds) {
    return getGraphData(
        (yearId) -> {
            Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository.getMetricsByCampusIdsAndYear(campusIds, yearId);
            return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
        },
        () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds)
    );
}
private MetricsDataDTO getMetricsDataForCampuses(List<Integer> campusIds) {
    return getMetricsData(
        // 1. Aggregated Metrics for the list of campuses
        (yearId) -> appStatusTrackRepository.getMetricsByCampusIdsAndYear(campusIds, yearId),
        // 2. Pro Metric for the list of campuses
        (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
        // 3. Distinct Years across all campuses
        () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds)
    );
}
// =========================================================================
// ZONAL / ZONE DIRECT HELPERS
// =========================================================================
private GraphDTO getGraphDataForZone(Integer zoneId) {
    return getGraphData(
        (yearId) -> {
            Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneId, yearId);
            return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
        },
        () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneId)
    );
}
private MetricsDataDTO getMetricsDataForZone(Integer zoneId) {
    return getMetricsData(
        // 1. Main Metrics
        (yearId) -> appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneId, yearId),
        // 2. Pro Metric
        (yearId) -> appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneId, yearId),
        // 3. Distinct Years
        () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneId)
    );
}
    // --- Private Graph Data Helper (Unchanged) ---
    private GraphDTO getGraphData(
            Function<Integer, Optional<GraphSoldSummaryDTO>> dataFetcher,
            Supplier<List<Integer>> yearFetcher) {
        GraphDTO graphData = new GraphDTO();
        List<YearlyGraphPointDTO> yearlyDataList = new ArrayList<>();
        try {
            List<Integer> existingYearIds = yearFetcher.get();
            List<AcademicYear> academicYears = academicYearRepository.findByAcdcYearIdIn(existingYearIds)
                    .stream()
                    .sorted(Comparator.comparingInt(AcademicYear::getAcdcYearId))
                    .toList();
            for (AcademicYear year : academicYears) {
                int acdcYearId = year.getAcdcYearId();
                String yearLabel = year.getAcademicYear();
                GraphSoldSummaryDTO summary = dataFetcher.apply(acdcYearId)
                        .orElse(new GraphSoldSummaryDTO(0L, 0L));
                long issued = summary.totalApplications();
                long sold = summary.totalSold();
                double issuedPercent = issued > 0 ? 100.0 : 0.0;
                double soldPercent = (issued > 0)
                        ? Math.min(100.0, ((double) sold / issued) * 100.0)
                        : 0.0;
                yearlyDataList.add(new YearlyGraphPointDTO(
                        yearLabel, issuedPercent, soldPercent, issued, sold
                ));
            }
            if (!academicYears.isEmpty()) {
                graphData.setTitle("Application Sales Percentage (" +
                        academicYears.get(0).getAcademicYear() + "–" +
                        academicYears.get(academicYears.size() - 1).getAcademicYear() + ")");
            } else {
                graphData.setTitle("Application Sales Percentage (No Data)");
            }
        } catch (Exception e) {
            System.err.println("Error fetching graph data: " + e.getMessage());
            e.printStackTrace();
        }
        graphData.setYearlyData(yearlyDataList);
        return graphData;
    }
    // --- Private Metrics Data Helper (Unchanged) ---
    private MetricsDataDTO getMetricsData(
            Function<Integer, Optional<MetricsAggregateDTO>> dataFetcher,
            Function<Integer, Optional<Long>> proFetcher,
            Supplier<List<Integer>> yearFetcher) {
        System.out.println("🔍 getMetricsData called");
        MetricsDataDTO dto = new MetricsDataDTO();
        try {
            List<Integer> yearIds = yearFetcher.get();
            System.out.println("📅 Year IDs from fetcher: " + yearIds);
            if (yearIds.isEmpty()) {
                System.out.println("❌ No years found - returning empty metrics");
                dto.setMetrics(new ArrayList<>());
                return dto;
            }
            // Sort yearIds ascending → last one is current year
            yearIds.sort(Integer::compare);
            int currentYearId = yearIds.get(yearIds.size() - 1);
            int previousYearId = (yearIds.size() > 1)
                    ? yearIds.get(yearIds.size() - 2)
                    : currentYearId;
            System.out.println("📅 Current Year ID: " + currentYearId);
            System.out.println("📅 Previous Year ID: " + previousYearId);
            
            AcademicYear cy = academicYearRepository.findById(currentYearId).orElse(null);
            AcademicYear py = academicYearRepository.findById(previousYearId).orElse(null);
            System.out.println("📅 Current Year Entity: " + (cy != null ? cy.getAcademicYear() : "NULL"));
            System.out.println("📅 Previous Year Entity: " + (py != null ? py.getAcademicYear() : "NULL"));
            
            dto.setCurrentYear(cy != null ? cy.getYear() : 0);
            dto.setPreviousYear(py != null ? py.getYear() : 0);
            
            System.out.println("🔍 Fetching current year metrics...");
            MetricsAggregateDTO curr = dataFetcher.apply(currentYearId)
                    .orElse(new MetricsAggregateDTO());
            System.out.println("🔍 Fetching previous year metrics...");
            MetricsAggregateDTO prev = dataFetcher.apply(previousYearId)
                    .orElse(new MetricsAggregateDTO());
            
            System.out.println("📊 Current Metrics - TotalApp: " + curr.totalApp() + ", Sold: " + curr.appSold() + ", Confirmed: " + curr.appConfirmed() + ", Available: " + curr.appAvailable() + ", Issued: " + curr.appIssued());
            System.out.println("📊 Previous Metrics - TotalApp: " + prev.totalApp() + ", Sold: " + prev.appSold() + ", Confirmed: " + prev.appConfirmed() + ", Available: " + prev.appAvailable() + ", Issued: " + prev.appIssued());
            
            System.out.println("🔍 Fetching PRO metrics...");
            long proCurr = proFetcher.apply(currentYearId).orElse(0L);
            long proPrev = proFetcher.apply(previousYearId).orElse(0L);
            System.out.println("📊 PRO Current: " + proCurr + ", PRO Previous: " + proPrev);
MetricsAggregateDTO totalMetrics = curr; // instead of summing every year
            long totalPro = proCurr;
            // ------------------------------------------------------
            List<MetricDTO> cards = buildMetricsList(curr, prev, totalMetrics, proCurr, proPrev, totalPro);
            System.out.println("✅ Built " + cards.size() + " metric cards");
            dto.setMetrics(cards);
            System.out.println("========================================");
        } catch (Exception ex) {
            System.out.println("🔥 METRICS ERROR: " + ex.getMessage());
            ex.printStackTrace();
            dto.setMetrics(new ArrayList<>());
        }
        return dto;
    }
    /**
     * Builds the metrics list.
     */
    private List<MetricDTO> buildMetricsList(
            MetricsAggregateDTO current, MetricsAggregateDTO previous, MetricsAggregateDTO total,
            long proCurrent, long proPrevious, long totalPro) {
        List<MetricDTO> metrics = new ArrayList<>();
        metrics.add(createMetric("Total Applications",
            total.totalApp(),
            current.totalApp(), previous.totalApp()));
        double soldPercentCurrent = calculatePercentage(current.appSold(), current.totalApp());
        double soldPercentPrevious = calculatePercentage(previous.appSold(), previous.totalApp());
        metrics.add(createMetricWithPercentage("Sold",
            total.appSold(),
            soldPercentCurrent, soldPercentPrevious));
        double confirmedPercentCurrent = calculatePercentage(current.appConfirmed(), current.totalApp());
        double confirmedPercentPrevious = calculatePercentage(previous.appConfirmed(), previous.totalApp());
        metrics.add(createMetricWithPercentage("Confirmed",
            total.appConfirmed(),
            confirmedPercentCurrent, confirmedPercentPrevious));
        // Calculate Available as Total - Issued (not from appAvailable field)
        long availableTotal = Math.max(0, total.totalApp() - total.appIssued());
        long availableCurrent = Math.max(0, current.totalApp() - current.appIssued());
        long availablePrevious = Math.max(0, previous.totalApp() - previous.appIssued());
        metrics.add(createMetric("Available",
            availableTotal,
            availableCurrent, availablePrevious));
        long validIssuedCurrent = Math.max(0, current.appIssued());
        long validIssuedPrevious = Math.max(0, previous.appIssued());
        double issuedPercentCurrent = calculatePercentage(validIssuedCurrent, current.totalApp());
        double issuedPercentPrevious = calculatePercentage(validIssuedPrevious, previous.totalApp());
        metrics.add(createMetricWithPercentage("Issued",
            total.appIssued(),
            issuedPercentCurrent, issuedPercentPrevious));
        metrics.add(createMetric("Damaged",
            total.appDamaged(),
            current.appDamaged(), previous.appDamaged()));
        metrics.add(createMetric("Unavailable",
            total.appUnavailable(),
            current.appUnavailable(), previous.appUnavailable()));
        metrics.add(createMetric("With PRO",
            totalPro,
            proCurrent, proPrevious));
        return metrics;
    }
    // --- UTILITY METHODS ---
    private MetricDTO createMetric(String title, long totalValue, long currentValue, long previousValue) {
        double change = calculatePercentageChange(currentValue, previousValue);
        return new MetricDTO(title, totalValue, change, getChangeDirection(change));
    }
    private MetricDTO createMetricWithPercentage(String title, long totalValue, double currentPercent, double previousPercent) {
        double change = calculatePercentageChange(currentPercent, previousPercent);
        return new MetricDTO(title, totalValue, change, getChangeDirection(change));
    }
    private double calculatePercentage(long numerator, long denominator) {
        if (denominator == 0) return 0.0;
        return (double) Math.max(0, numerator) * 100.0 / denominator;
    }
    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0) return (current > 0) ? 100 : 0;
        double change = ((current - previous) / previous) * 100;
        return Math.round(change);
    }
    private String getChangeDirection(double change) {
        if (change > 0) return "up";
        if (change < 0) return "down";
        return "neutral";
    }
    private int getAcdcYearId(int year) {
        return academicYearRepository.findByYear(year)
                .map(AcademicYear::getAcdcYearId)
                .orElse(0);
    }
    // --- NEW: Flexible Graph Data Method with Optional Filters ---
    /**
     * Get year-wise graph data (GraphBarDTO) with optional filters for zoneId, campusIds, campusId, and amount.
     * All parameters are optional. Always returns data for the past 4 years (current + 3 previous).
     * If data doesn't exist for a year, returns 0 values for that year.
     *
     * IMPORTANT:
     * - campusId (singular) uses entity_id = 4 (single campus/PRO role)
     * - campusIds (plural) uses entity_id = 3 (DGM rollup with multiple campuses)
     *
     * @param zoneId Optional zone ID filter
     * @param campusIds Optional list of campus IDs filter (uses entity_id = 3 for DGM rollup)
     * @param campusId Optional single campus ID filter (uses entity_id = 4 for single campus)
     * @param amount Optional amount filter
     * @return List of GraphBarDTO containing year-wise issued and sold data for past 4 years
     */
    public List<GraphBarDTO> getFlexibleGraphData(Integer zoneId, List<Integer> campusIds, Integer campusId, Float amount, Integer employeeId) {
        // Get current year (latest year) from AppStatusTrackRepository
        Integer currentYearId = appStatusTrackRepository.findLatestYearId();
        if (currentYearId == null) {
            return new ArrayList<>();
        }
        // Get previous 4 years (current year + 3 previous years)
        List<Integer> yearIds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            yearIds.add(currentYearId - i);
        }
        List<Object[]> rows;
        // IMPORTANT: 
        // - campusId (singular) uses entity_id = 4 (single campus methods)
        // - campusIds (plural) uses entity_id = 3 (list methods, even for single element)
        // If employeeId is provided, fetch associated campus IDs for that DGM
        if (employeeId != null) {
            List<Integer> dgmCampusIds = dgmRepository.findCampusIdsByEmployeeId(employeeId);
            if (campusIds == null) {
                campusIds = new ArrayList<>();
            }
            for (Integer dgmCampusId : dgmCampusIds) {
                if (!campusIds.contains(dgmCampusId)) {
                    campusIds.add(dgmCampusId);
                }
            }
        }
        
        // IMPORTANT: Keep campusId (singular) and campusIds (plural) separate
        // - campusId (singular) uses entity_id = 4 (single campus/PRO)
        // - campusIds (plural) uses entity_id = 3 (DGM rollup)
        // Do NOT merge them - handle separately
        boolean hasCampusId = campusId != null;
        boolean hasCampusIds = campusIds != null && !campusIds.isEmpty();
        
        // Only merge for campusIds list (DGM rollup), not for single campusId
        List<Integer> effectiveCampusIds = new ArrayList<>();
        if (campusIds != null) effectiveCampusIds.addAll(campusIds);
        
        boolean hasCampuses = hasCampusIds; // Only true if campusIds (plural) is provided
        
        // DEBUG: Check individual campus data before aggregation (for multiple campuses from campusIds)
        if (hasCampuses && effectiveCampusIds.size() > 1) {
            System.out.println("=== DEBUG: CHECKING INDIVIDUAL CAMPUS DATA FOR AGGREGATION (campusIds) ===");
            System.out.println("Effective Campus IDs: " + effectiveCampusIds);
            for (Integer id : effectiveCampusIds) {
                List<Object[]> individualCampusRows;
                if (amount != null) {
                    individualCampusRows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(id, amount);
                } else {
                    individualCampusRows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(id);
                }
                // Filter by yearIds
                individualCampusRows = individualCampusRows.stream()
                        .filter(row -> yearIds.contains((Integer) row[0]))
                        .collect(java.util.stream.Collectors.toList());
                System.out.println("Campus ID " + id + " - Individual data (filtered by years, entity_id=4):");
                if (individualCampusRows.isEmpty()) {
                    System.out.println(" ⚠️ NO DATA FOUND for Campus ID " + id + " (for the past 4 years with current filters)");
                } else {
                    for (Object[] row : individualCampusRows) {
                        Integer yearId = (Integer) row[0];
                        Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                        Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                        System.out.println("  Year ID: " + yearId + " | Issued: " + totalAppCount + " | Sold: " + sold);
                    }
                }
            }
            System.out.println("=== END INDIVIDUAL CAMPUS DATA DEBUG ===");
        }
        
        // DEBUG: Log single campusId usage
        if (hasCampusId && !hasCampuses) {
            System.out.println("=== DEBUG: SINGLE CAMPUS (campusId) MODE ===");
            System.out.println("Campus ID: " + campusId + " (entity_id=4, issued = total - available)");
            System.out.println("=== END SINGLE CAMPUS DEBUG ===");
        }

        // Determine which repository method to call based on provided parameters
        // Priority: Check campusId (singular, entity_id=4) first, then campusIds (plural, entity_id=3)
        if (zoneId != null && hasCampusId && amount != null) {
            // Zone + Single Campus (campusId) + Amount - use single campus method with entity_id = 4
            System.out.println("Filter: Zone + Single Campus (campusId) + Amount (zone=" + zoneId + ", campusId=" + campusId + ", amt=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusAndAmount(zoneId, campusId, amount);
        } else if (zoneId != null && hasCampuses && amount != null) {
            // Zone + Campuses (campusIds) + Amount - use NEW method with entity_id = 4
            System.out.println("Filter: Zone + Campuses (campusIds) + Amount (zone=" + zoneId + ", camps=" + effectiveCampusIds + ", amt=" + amount + ") - Using entity_id = 4");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListAndAmountWithEntity4(zoneId, effectiveCampusIds, amount);
        } else if (hasCampusId && amount != null) {
            // Single Campus (campusId) + Amount - use single campus method with entity_id = 4
            System.out.println("Filter: Single Campus (campusId) + Amount (campusId=" + campusId + ", amt=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(campusId, amount);
        } else if (hasCampuses && amount != null) {
            // Campuses (campusIds) + Amount - use NEW method with entity_id = 4
            System.out.println("Filter: Campuses (campusIds) + Amount (camps=" + effectiveCampusIds + ", amt=" + amount + ") - Using entity_id = 4");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListAndAmountWithEntity4(effectiveCampusIds, amount);
        } else if (zoneId != null && amount != null) {
            System.out.println("Filter: Zone + Amount (zone=" + zoneId + ", amt=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneAndAmount(zoneId, amount);
        } else if (zoneId != null && hasCampusId) {
            // Zone + Single Campus (campusId) - use single campus method with entity_id = 4
            System.out.println("Filter: Zone + Single Campus (campusId) (zone=" + zoneId + ", campusId=" + campusId + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampus(zoneId, campusId);
        } else if (zoneId != null && hasCampuses) {
            // Zone + Campuses (campusIds) - use NEW method with entity_id = 4
            System.out.println("Filter: Zone + Campuses (campusIds) (zone=" + zoneId + ", camps=" + effectiveCampusIds + ") - Using entity_id = 4");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListWithEntity4(zoneId, effectiveCampusIds);
        } else if (hasCampusId) {
            // Single Campus (campusId) only - use single campus method with entity_id = 4
            System.out.println("Filter: Single Campus (campusId) (campusId=" + campusId + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(campusId);
        } else if (hasCampuses) {
            // Campuses (campusIds) only - use NEW method with entity_id = 4
            System.out.println("Filter: Campuses (campusIds) (camps=" + effectiveCampusIds + ") - Using entity_id = 4");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListWithEntity4(effectiveCampusIds);
        } else if (zoneId != null) {
            System.out.println("Filter: Zone (zone=" + zoneId + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZone(zoneId);
        } else if (amount != null) {
            System.out.println("Filter: Amount (amt=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
        } else {
            System.out.println("Filter: All Time Aggregate");
            // switch to AppStatusTrack
            rows = appStatusTrackRepository.getYearWiseMetricsAllTime();
        }

        // Apply year filtering
        System.out.println("Raw rows from DB before year filtering: " + rows.size());
        rows = rows.stream()
                .filter(row -> yearIds.contains((Integer) row[0]))
                .collect(java.util.stream.Collectors.toList());
        System.out.println("Rows after year filtering (for years: " + yearIds + "): " + rows.size());

        // Get AcademicYear entities for year labels
        List<AcademicYear> academicYears = academicYearRepository.findByAcdcYearIdIn(yearIds);
        java.util.Map<Integer, AcademicYear> yearMap = academicYears.stream()
                .collect(java.util.stream.Collectors.toMap(AcademicYear::getAcdcYearId, y -> y));
        // Log the raw data retrieved from database
        if (campusIds != null && campusIds.size() > 1) {
            System.out.println("=== MULTIPLE CAMPUSES DATA CALCULATION ===");
            System.out.println("Campuses being aggregated: " + campusIds);
            System.out.println("Amount filter: " + (amount != null ? amount : "None"));
            System.out.println("Zone filter: " + (zoneId != null ? zoneId : "None"));
            System.out.println("Year IDs being queried: " + yearIds);
            System.out.println("Total rows retrieved from aggregated query: " + rows.size());
            if (rows.isEmpty()) {
System.out.println("⚠️ WARNING: Aggregated query returned NO DATA for campuses: " + campusIds);
                System.out.println("This could mean:");
System.out.println(" 1. None of the campuses have data for the specified filters");
System.out.println(" 2. The amount filter (" + amount + ") doesn't match any records");
System.out.println(" 3. The data exists but not in the past 4 years");
            } else {
                System.out.println("Raw aggregated data from database (summed across all campuses):");
                for (Object[] row : rows) {
                    Integer yearId = (Integer) row[0];
                    Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
System.out.println(" Year ID: " + yearId + " | Issued (totalAppCount): " + totalAppCount + " | Sold: " + sold);
                }
            }
        }
        // Create a map of yearId -> [totalAppCount, sold] for quick lookup
        java.util.Map<Integer, long[]> yearDataMap = new java.util.HashMap<>();
        for (Object[] row : rows) {
            Integer yearId = (Integer) row[0];
            Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            yearDataMap.put(yearId, new long[]{totalAppCount, sold});
        }
        
        // If zoneId is present, add Admin→DGM and Admin→Campus distributions to issued count for each year
        if (zoneId != null) {
            for (Integer yearId : yearIds) {
                // Get distribution counts for this year
                Integer adminToDgmDist = distributionRepository.sumAdminToDgmDistributionByZoneAndYear(zoneId, yearId).orElse(0);
                Integer adminToCampusDist = distributionRepository.sumAdminToCampusDistributionByZoneAndYear(zoneId, yearId).orElse(0);
                
                // Add distributions to issued count
                long[] data = yearDataMap.getOrDefault(yearId, new long[]{0L, 0L});
                long updatedIssued = data[0] + adminToDgmDist + adminToCampusDist;
                yearDataMap.put(yearId, new long[]{updatedIssued, data[1]});
                
                if (adminToDgmDist > 0 || adminToCampusDist > 0) {
                    System.out.println("Zone " + zoneId + " Year " + yearId + ": Added Admin→DGM: " + adminToDgmDist + 
                                     ", Admin→Campus: " + adminToCampusDist + " to issued count");
                }
            }
        }
        
        // If campusIds is present (DGM rollup), add Admin→Campus and Zone→Campus distributions to issued count for each year
        // NOTE: Only distributions with issued_to_type_id = 4 (Campus/PRO) are added to issued count
        // Admin→DGM (issued_to_type_id = 3) is NOT included in issued count
        if (hasCampuses && effectiveCampusIds != null && !effectiveCampusIds.isEmpty()) {
            for (Integer yearId : yearIds) {
                // Get Admin→Campus distribution (filtered by campusIds, issued_to_type_id = 4)
                Integer adminToCampusDist = distributionRepository.sumAdminToCampusDistributionByCampusIdsAndYear(effectiveCampusIds, yearId).orElse(0);
                
                // Get Zone→Campus distribution (filtered by campusIds, issued_to_type_id = 4)
                Integer zoneToCampusDist = distributionRepository.sumZoneToCampusDistributionByCampusIdsAndYear(effectiveCampusIds, yearId).orElse(0);
                
                // Add ONLY issued_to_type_id = 4 distributions to issued count (Admin→Campus + Zone→Campus)
                long[] data = yearDataMap.getOrDefault(yearId, new long[]{0L, 0L});
                long issuedDistCount = adminToCampusDist + zoneToCampusDist; // Only issued_to_type_id = 4
                long updatedIssued = data[0] + issuedDistCount;
                yearDataMap.put(yearId, new long[]{updatedIssued, data[1]});
                
                if (issuedDistCount > 0) {
                    System.out.println("CampusIds " + effectiveCampusIds + " Year " + yearId + ": Added Admin→Campus: " + adminToCampusDist + 
                                     ", Zone→Campus: " + zoneToCampusDist + " (issued_to_type_id = 4 only, Total: " + issuedDistCount + ") to issued count");
                }
            }
        }
        // Log aggregated data summary
        if (campusIds != null && campusIds.size() > 1) {
            System.out.println("Aggregated data by year (summed across all campuses):");
            for (Integer yearId : yearIds) {
                long[] data = yearDataMap.getOrDefault(yearId, new long[]{0L, 0L});
System.out.println(" Year ID: " + yearId + " | Total Issued: " + data[0] + " | Total Sold: " + data[1]);
            }
        }
        // Build GraphBarDTO list for all 4 years (always return 4 years)
        List<GraphBarDTO> barList = new ArrayList<>();
        for (Integer yearId : yearIds) {
            long[] data = yearDataMap.getOrDefault(yearId, new long[]{0L, 0L});
            long issuedCount = data[0]; // totalAppCount from table
long soldCount = data[1]; // sold from table
            AcademicYear year = yearMap.get(yearId);
            String yearLabel = year != null ? year.getAcademicYear() : "Year " + yearId;
            // Calculate percentages
            int issuedPercent;
            int soldPercent;
            // If data is missing (issuedCount = 0), both percentages are 0
            // If data exists (issuedCount > 0), issuedPercent is 100% (baseline) and calculate sold percentage
            if (issuedCount > 0) {
                issuedPercent = 100; // 100% as baseline when data exists
                soldPercent = (int) Math.round((soldCount * 100.0) / issuedCount);
            } else {
                // No data exists - both percentages are 0
                issuedPercent = 0;
                soldPercent = 0;
            }
            // Log calculation details for multiple campuses
            if (campusIds != null && campusIds.size() > 1) {
                System.out.println("Year: " + yearLabel + " | Issued: " + issuedCount + " | Sold: " + soldCount +
                                 " | Issued %: " + issuedPercent + " | Sold %: " + soldPercent);
            }
            GraphBarDTO dto = new GraphBarDTO();
            dto.setYear(yearLabel);
            dto.setIssuedPercent(issuedPercent);
            dto.setSoldPercent(soldPercent);
            dto.setIssuedCount((int) issuedCount);
            dto.setSoldCount((int) soldCount);
            barList.add(dto);
        }
        if (campusIds != null && campusIds.size() > 1) {
            System.out.println("=== END MULTIPLE CAMPUSES DATA CALCULATION ===");
        }
        return barList;
    }
/**
* Get all campuses with optional category filter (school/college)
* @param category Optional category filter: "school" or "college"
* @return List of GenericDropdownDTO containing campus ID and name
*/
public List<GenericDropdownDTO> getAllCampuses(String category) {
// Get all active campuses
List<GenericDropdownDTO> allCampuses = campusRepository.findAllActiveCampusesForDropdown();
// If no category provided, return all campuses
if (category == null || category.trim().isEmpty()) {
return allCampuses;
}
// Filter by category (case-insensitive)
String cat = category.trim().toLowerCase();
return allCampuses.stream()
.filter(campus -> {
// Get campus entity to check business type
Campus campusEntity = campusRepository.findById(campus.getId()).orElse(null);
if (campusEntity == null || campusEntity.getBusinessType() == null) {
return false;
}
String businessTypeName = campusEntity.getBusinessType().getBusinessTypeName().toLowerCase();
// Match category
if (cat.equals("school")) {
return businessTypeName.contains("school");
} else if (cat.equals("college")) {
return businessTypeName.contains("college");
}
// If category doesn't match known types, return true (no filter)
return true;
})
.collect(Collectors.toList());
}
/**
* Get all zones with optional category filter (school/college)
* @param category Optional category filter: "school" or "college"
* @return List of GenericDropdownDTO containing zone ID and name
*/
public List<GenericDropdownDTO> getAllZones(String category) {
// Get all zones
List<GenericDropdownDTO> allZones = zoneRepository.findAllActiveZonesForDropdown();
// If no category provided, return all zones
if (category == null || category.trim().isEmpty()) {
return allZones;
}
// Filter by category (case-insensitive)
// A zone is included if it has at least one campus matching the category
String cat = category.trim().toLowerCase();
return allZones.stream()
.filter(zone -> {
// Get zone entity to check campuses
Zone zoneEntity = zoneRepository.findById(zone.getId()).orElse(null);
if (zoneEntity == null) {
return false;
}
// Get all campuses for this zone
List<Campus> campuses = campusRepository.findByZoneZoneId(zoneEntity.getZoneId());
// Check if any campus matches the category
return campuses.stream()
.anyMatch(campus -> {
if (campus.getBusinessType() == null) {
return false;
}
String businessTypeName = campus.getBusinessType()
.getBusinessTypeName().toLowerCase();
// Case-insensitive category matching
if (cat.equals("school")) {
return businessTypeName.contains("school");
} else if (cat.equals("college")) {
return businessTypeName.contains("college");
}
// If category doesn't match known types, return true (no filter)
return true;
});
})
.collect(Collectors.toList());
}
/**
* Get all DGM employees with optional category filter (school/college)
* @param category Optional category filter: "school" or "college"
* @return List of GenericDropdownDTO_Dgm containing employee ID, name, and associated campus IDs
*/
public List<GenericDropdownDTO_Dgm> getAllDgmEmployees(String category) {
// Get all DGM employees with their campus IDs
List<Object[]> rows = dgmRepository.findAllDgmEmployeesWithCampusId();
if (rows == null || rows.isEmpty()) {
return new ArrayList<>();
}

// Map to group by Employee ID
java.util.Map<Integer, GenericDropdownDTO_Dgm> groupedMap = new java.util.LinkedHashMap<>();

for (Object[] row : rows) {
Integer id = (Integer) row[0];
String name = (String) row[1];
Integer campusId = (Integer) row[2];

// Get existing DTO or create new
GenericDropdownDTO_Dgm dto = groupedMap.get(id);
if (dto == null) {
dto = new GenericDropdownDTO_Dgm();
dto.setId(id);
dto.setName(name);
dto.setCmpsId(new ArrayList<>()); // Initialize the list
groupedMap.put(id, dto);
}

// Add campusId to the list if not already present
if (campusId != null && !dto.getCmpsId().contains(campusId)) {
dto.getCmpsId().add(campusId);
}
}

List<GenericDropdownDTO_Dgm> allDgmEmployees = new ArrayList<>(groupedMap.values());
// If no category provided, return all DGM employees
if (category == null || category.trim().isEmpty()) {
return allDgmEmployees;
}
// Filter by category (case-insensitive)
// A DGM employee is included if they have at least one campus matching the category
String cat = category.trim().toLowerCase();
return allDgmEmployees.stream()
.filter(dgm -> {
// Check if any of the DGM's campuses match the category
return dgm.getCmpsId().stream()
.anyMatch(campusId -> {
Campus campus = campusRepository.findById(campusId).orElse(null);
if (campus == null || campus.getBusinessType() == null) {
return false;
}
String businessTypeName = campus.getBusinessType()
.getBusinessTypeName().toLowerCase();
// Case-insensitive category matching
if (cat.equals("school")) {
return businessTypeName.contains("school");
} else if (cat.equals("college")) {
return businessTypeName.contains("college");
}
// If category doesn't match known types, return true (no filter)
return true;
});
})
.collect(Collectors.toList());
}
}