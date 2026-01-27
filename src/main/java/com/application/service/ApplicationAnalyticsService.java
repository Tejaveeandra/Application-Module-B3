// package com.application.service;

// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.function.Function;
// import java.util.function.Supplier;
// import java.util.stream.Collectors;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import com.application.dto.CombinedAnalyticsDTO;
// import com.application.dto.GraphBarDTO;
// import com.application.dto.GraphDTO;
// import com.application.dto.GraphSoldSummaryDTO;
// import com.application.dto.MetricDTO;
// import com.application.dto.MetricsAggregateDTO;
// import com.application.dto.MetricsDataDTO;
// import com.application.dto.YearlyGraphPointDTO;
// import com.application.entity.AcademicYear;
// import com.application.entity.Campus;
// import com.application.entity.Dgm;
// import com.application.entity.SCEmployeeEntity;
// import com.application.entity.ZonalAccountant;
// import com.application.repository.AcademicYearRepository;
// import com.application.repository.AppStatusTrackRepository;
// import com.application.repository.CampusRepository;
// import com.application.repository.DistributionRepository;
// import com.application.repository.DgmRepository;
// import com.application.repository.SCEmployeeRepository;
// import com.application.repository.UserAppSoldRepository;
// import com.application.repository.ZonalAccountantRepository;
// import com.application.repository.ZoneRepository;
// import com.application.repository.AppStatusTrackViewRepository;
// import com.application.entity.Zone;
// import com.application.dto.GenericDropdownDTO_Dgm;
// import com.application.dto.GenericDropdownDTO;
// import com.application.dto.AcademicYearDTO;
// import com.application.dto.AcademicYearInfoDTO;
// import com.application.repository.AdminAppRepository;

// @Service
// public class ApplicationAnalyticsService {
//     @Autowired
//     private UserAppSoldRepository userAppSoldRepository;
//     @Autowired
//     private AppStatusTrackRepository appStatusTrackRepository;
//     @Autowired
//     private AcademicYearRepository academicYearRepository;
//     @Autowired
//     private SCEmployeeRepository scEmployeeRepository;
//     @Autowired
//     private ZonalAccountantRepository zonalAccountantRepository;
//     @Autowired
//     private DgmRepository dgmRepository;
//     @Autowired
//     private CampusRepository campusRepository;
//     @Autowired
//     private DistributionRepository distributionRepository;
//     @Autowired
//     private ZoneRepository zoneRepository;
//     @Autowired
//     private AppStatusTrackViewRepository appStatusTrackViewRepository;
//     @Autowired
//     private AdminAppRepository adminAppRepository;

//     public CombinedAnalyticsDTO getRollupAnalytics(Integer empId) {
//         System.out.println("========================================");
//         System.out.println("DEBUG: getRollupAnalytics called for empId: " + empId);
//         // 1. Get Basic Employee Details
//         List<SCEmployeeEntity> employeeList = scEmployeeRepository.findByEmpId(empId);
//         if (employeeList.isEmpty()) {
//             System.out.println("❌ ERROR: Employee " + empId + " not found in SCEmployeeEntity");
//             return createEmptyAnalytics("Invalid Employee", empId, "Employee not found", "N/A");
//         }
//         SCEmployeeEntity employee = employeeList.get(0);
//         String role = employee.getEmpStudApplicationRole();
//         String designation = employee.getDesignationName();
//         System.out.println("✓ Employee found: ID=" + empId + ", Role=" + role + ", Designation=" + designation);
//         if (role == null) {
//             System.out.println("❌ ERROR: Employee " + empId + " has null role");
//             return createEmptyAnalytics("Null Role", empId, "No Role", designation);
//         }
//         // 2. Route based on Role - Use same methods as specific endpoints
//         String trimmedRole = role.trim();
//         System.out.println("Routing to analytics method for role: " + trimmedRole);
//         CombinedAnalyticsDTO analytics;
//         if (trimmedRole.equalsIgnoreCase("DGM")) {
//             // Use same method as /api/analytics/dgm_employee/{id}
//             System.out.println("Calling getEmployeeAnalytics (same as /dgm_employee/{id})");
//             analytics = getEmployeeAnalytics((long) empId);
//             // Set role and designation info
//             analytics.setRole("DGM");
//             analytics.setDesignationName(designation);
//             // Get campus info for entity name
//             List<Integer> campusIds = dgmRepository.findCampusIdsByEmployeeId(empId);
//             if (!campusIds.isEmpty()) {
//                 analytics.setEntityName(campusIds.size() + " Campuses Managed");
//             } else {
//                 analytics.setEntityName("DGM");
//             }
//             analytics.setEntityId(empId);
//             return analytics;
//         } else if (trimmedRole.equalsIgnoreCase("ZONAL ACCOUNTANT")) {
//             // Get Zone ID and use same method as /api/analytics/zone/{id}
//             List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
//             if (zonalRecords == null || zonalRecords.isEmpty()) {
//                 System.out.println("❌ ERROR: No ZonalAccountant records found for employee " + empId);
//                 return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone", designation);
//             }
//             ZonalAccountant zonalRecord = zonalRecords.get(0);
//             if (zonalRecord.getZone() == null) {
//                 return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone", designation);
//             }
//             Long zoneId = (long) zonalRecord.getZone().getZoneId();
//             String zoneName = zonalRecord.getZone().getZoneName();
//             System.out.println("Calling getZoneAnalytics (same as /zone/{id}) for zoneId: " + zoneId);
//             analytics = getZoneAnalytics(zoneId);
//             // Set role and designation info
//             analytics.setRole("Zonal Accountant");
//             analytics.setDesignationName(designation);
//             analytics.setEntityName(zoneName != null ? zoneName : "Zone " + zoneId);
//             analytics.setEntityId(zoneId.intValue());
//             return analytics;
//         } else {
//             // For PRO and other roles, get campus ID and use EXACT SAME method as
//             // /api/analytics/campus/{id}
//             // This ensures PRO role uses the same calculation logic as campus endpoint
//             int campusId = employee.getEmpCampusId();
//             if (campusId <= 0) {
//                 System.out.println("❌ ERROR: Employee " + empId + " has invalid campusId: " + campusId);
//                 return createEmptyAnalytics(role != null ? role : "Unknown", empId, "Employee not mapped to a Campus",
//                         designation);
//             }
//             String campusName = employee.getCampusName();
//             System.out.println("========================================");
//             System.out.println("📊 PRO ROLE - Using SAME logic as /api/analytics/campus/{id}");
//             System.out.println("========================================");
//             System.out.println("Employee ID: " + empId);
//             System.out.println("Campus ID: " + campusId);
//             System.out.println("Campus Name: " + campusName);
//             System.out.println("Method: getCampusAnalytics (EXACT SAME as /campus/{id})");
//             System.out.println("Uses: AppStatusTrack with app_issued_type_id = 4");
//             System.out.println("========================================");
//             analytics = getCampusAnalytics((long) campusId, empId);
//             // Set role and designation info
//             analytics.setRole(role != null ? role : "PRO");
//             analytics.setDesignationName(designation);
//             analytics.setEntityName(campusName != null ? campusName : "Campus " + campusId);
//             analytics.setEntityId(campusId);
//             return analytics;
//         }
//     }

//     // --- "NORMAL" ROUTER METHOD (Unchanged) ---
//     /**
//      * This is the original "normal" view for DGM, Zonal, or PRO.
//      * It shows data for *only* their direct entity.
//      */
//     public CombinedAnalyticsDTO getAnalyticsForEmployee(Integer empId) {
//         List<SCEmployeeEntity> employeeList = scEmployeeRepository.findByEmpId(empId);
//         // Employee not found
//         if (employeeList.isEmpty()) {
//             System.err.println("No employee found with ID: " + empId);
//             // Pass "N/A" for designation
//             return createEmptyAnalytics("Invalid Employee", empId, "Employee not found", "N/A");
//         }
//         SCEmployeeEntity employee = employeeList.get(0);
//         String role = employee.getEmpStudApplicationRole();
//         String designation = employee.getDesignationName(); // <--- GET DESIGNATION HERE
//         // Null role
//         if (role == null) {
//             System.err.println("Employee " + empId + " has a null role.");
//             return createEmptyAnalytics("Null Role", empId, "Employee has no role", designation); // <--- PASS
//                                                                                                   // DESIGNATION
//         }
//         String trimmedRole = role.trim();
//         CombinedAnalyticsDTO analytics;
//         if (trimmedRole.equalsIgnoreCase("DGM")) {
//             analytics = getDgmAnalytics(empId);
//             analytics.setRole("DGM");
//             analytics.setEntityName(employee.getFirstName() + " " + employee.getLastName());
//             analytics.setEntityId(empId);
//         } else if (trimmedRole.equalsIgnoreCase("ZONAL ACCOUNTANT")) {
//             int zoneId = employee.getZoneId();
//             analytics = getZoneAnalytics((long) zoneId);
//             analytics.setRole("Zonal Account");
//             analytics.setEntityName(employee.getZoneName());
//             analytics.setEntityId(zoneId);
//         } else if (trimmedRole.equalsIgnoreCase("PRO")) {
//             int campusId = employee.getEmpCampusId();
//             analytics = getCampusAnalytics((long) campusId, empId);
//             analytics.setRole("PRO");
//             analytics.setEntityName(employee.getCampusName());
//             analytics.setEntityId(campusId);
//         } else {
//             System.err.println("Unrecognized role '" + role + "' for empId: " + empId);
//             return createEmptyAnalytics(role, empId, "Unrecognized role", designation); // <--- PASS DESIGNATION
//         }
//         // <--- SET DESIGNATION BEFORE RETURNING --->
//         analytics.setDesignationName(designation);
//         return analytics;
//     }

//     private CombinedAnalyticsDTO createEmptyAnalytics(String role, Integer id, String name, String designationName) {
//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         analytics.setRole(role);
//         analytics.setDesignationName(designationName); // <--- Set designation here
//         analytics.setEntityId(id);
//         analytics.setEntityName(name);
//         return analytics;
//     }

//     // --- CORE ANALYTICS METHODS (Unchanged) ---
//     public CombinedAnalyticsDTO getZoneAnalytics(Long id) {
//         // System.out.println("========================================");
//         // System.out.println("🔍 DEBUG: getZoneAnalytics called");
//         // System.out.println("Zone ID (Long): " + id);
//         Integer zoneIdInt = id.intValue();
//         // System.out.println("Zone ID (Integer): " + zoneIdInt);
//         // System.out.println("========================================");

//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();

//         // 1. Graph Data (ALIGNED WITH CARDS)
//         // System.out.println("📊 Getting Graph Data for Zone (Aligned with Cards): " +
//         // zoneIdInt);
//         // Use UserAppSold for graph data: entity_id = 2 for issued, entity_id = 4 for
//         // sold
//         analytics.setGraphData(getGraphData(
//                 (yearId) -> {
//                     // Get issued from UserAppSold with entity_id = 2 (Zone)
//                     Optional<GraphSoldSummaryDTO> userAppSoldData = userAppSoldRepository
//                             .getSalesSummaryByZoneId(zoneIdInt, yearId);
//                     if (userAppSoldData.isEmpty()) {
//                         return Optional.of(new GraphSoldSummaryDTO(0L, 0L));
//                     }

//                     long issuedFromUserAppSold = userAppSoldData.get().totalApplications();
//                     long soldFromUserAppSold = userAppSoldData.get().totalSold();

//                     // Add Admin→DGM and Admin→Campus distributions to issued count
//                     Integer adminToDgmDist = distributionRepository
//                             .sumAdminToDgmDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
//                     Integer adminToCampusDist = distributionRepository
//                             .sumAdminToCampusDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);

//                     long totalIssued = issuedFromUserAppSold + adminToDgmDist + adminToCampusDist;

//                     System.out.println("Zone " + zoneIdInt + " Year " + yearId
//                             + ": UserAppSold totalAppCount (entity_id=2)=" + issuedFromUserAppSold +
//                             ", Sold (entity_id=4)=" + soldFromUserAppSold +
//                             ", Admin→DGM=" + adminToDgmDist + ", Admin→Campus=" + adminToCampusDist +
//                             ", Total issued=" + totalIssued);

//                     return Optional.of(new GraphSoldSummaryDTO(
//                             totalIssued,
//                             soldFromUserAppSold // Sold from entity_id = 4
//                     ));
//                 },
//                 () -> userAppSoldRepository.findDistinctYearIdsByZoneId(zoneIdInt)));

//         // 2. Metrics Data
//         // System.out.println("📈 Getting Metrics Data for Zone: " + zoneIdInt);
//         // System.out.println("🔍 Checking years from AppStatusTrack (filtered by
//         // is_active = 1)...");
//         // List<Integer> years =
//         // appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt);
//         // System.out.println("✅ Found " + years.size() + " year(s) for Zone " +
//         // zoneIdInt + ": " + years);

//         analytics.setMetricsData(
//                 getMetricsData(
//                         (yearId) -> {
//                             // System.out.println("🔍 getMetricsByZoneIdAndYear called - Zone: " + zoneIdInt
//                             // + ", Year: " + yearId);
//                             // Get AppStatusTrack metrics (covers Zone to DGM, DGM to Campus, Zone to
//                             // Campus)
//                             // AppStatusTrack already includes all flows with app_issued_type_id IN (2, 3,
//                             // 4)
//                             // This already captures: Zone→DGM, DGM→Campus, Zone→Campus flows
//                             MetricsAggregateDTO statusMetrics = appStatusTrackRepository
//                                     .getMetricsByZoneIdAndYear(zoneIdInt, yearId)
//                                     .orElse(new MetricsAggregateDTO());

//                             // Get issued breakdown by app_issued_type_id
//                             List<Object[]> issuedBreakdown = appStatusTrackRepository
//                                     .getIssuedBreakdownByZoneIdAndYear(zoneIdInt, yearId);

//                             // Get distribution counts
//                             Integer adminToZoneDist = distributionRepository
//                                     .sumAdminToZoneDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
//                             Integer adminToDgmDist = distributionRepository
//                                     .sumAdminToDgmDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
//                             Integer adminToCampusDist = distributionRepository
//                                     .sumAdminToCampusDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);

//                             // Display detailed breakdown for Zone Issued Calculation
//                             System.out.println("========================================");
//                             System.out.println("📊 ZONE ISSUED CALCULATION BREAKDOWN");
//                             System.out.println("========================================");
//                             System.out.println("Zone ID: " + zoneIdInt);
//                             System.out.println("Academic Year ID: " + yearId);
//                             System.out.println("----------------------------------------");
//                             System.out.println("📈 ISSUED COUNT FROM AppStatusTrack (Type 2 - Zone ONLY):");

//                             long issuedType2 = 0; // Zone

//                             for (Object[] row : issuedBreakdown) {
//                                 Integer typeId = ((Number) row[0]).intValue();
//                                 Long issued = ((Number) row[1]).longValue();

//                                 if (typeId == 2) {
//                                     issuedType2 = issued;
//                                     System.out.println("   app_issued_type_id = 2 (Zone): " + issued);
//                                 }
//                             }

//                             System.out.println("----------------------------------------");
//                             System.out.println("📊 TOTAL APPLICATIONS FROM AppStatusTrack:");
//                             System.out.println(
//                                     "   totalApp (app_issued_type_id = 2 - Zone): " + statusMetrics.totalApp());
//                             System.out.println(
//                                     "   appIssued (app_issued_type_id = 2 - Zone): " + statusMetrics.appIssued());
//                             System.out.println("----------------------------------------");
//                             System.out.println("📦 DISTRIBUTION TABLE COUNTS (Direct Admin Distributions):");
//                             System.out.println(
//                                     "   Admin → Zone: " + adminToZoneDist + " (NOT added - already in AppStatusTrack)");
//                             System.out.println(
//                                     "   Admin → DGM (Direct): " + adminToDgmDist + " (ADDED to totalApp and issued)");
//                             System.out.println("   Admin → Campus (Direct): " + adminToCampusDist
//                                     + " (ADDED to totalApp and issued)");
//                             System.out.println("----------------------------------------");

//                             // Add Admin→DGM and Admin→Campus distributions to issued and totalApp
//                             // Note: Admin→Zone is NOT added because it's already reflected in
//                             // AppStatusTrack Type 2
//                             long totalIssued = statusMetrics.appIssued() + adminToDgmDist + adminToCampusDist;
//                             long totalApp = statusMetrics.totalApp() + adminToDgmDist + adminToCampusDist;

//                             System.out.println("✅ FINAL ZONE CALCULATION:");
//                             System.out.println("   Total Applications:");
//                             System.out
//                                     .println("     - From AppStatusTrack (Type 2 - Zone): " + statusMetrics.totalApp());
//                             System.out.println("     - Admin → DGM Distribution (Direct): " + adminToDgmDist);
//                             System.out.println("     - Admin → Campus Distribution (Direct): " + adminToCampusDist);
//                             System.out.println("     - TOTAL APPLICATIONS: " + totalApp);
//                             System.out.println("   Issued Count:");
//                             System.out.println("     - Zone (Type 2 from AppStatusTrack): " + issuedType2);
//                             System.out.println("     - Admin → DGM Distribution (Direct): " + adminToDgmDist);
//                             System.out.println("     - Admin → Campus Distribution (Direct): " + adminToCampusDist);
//                             System.out.println("     - TOTAL ISSUED: " + totalIssued);
//                             System.out.println("----------------------------------------");
//                             System.out.println("🔍 VERIFICATION:");
//                             System.out.println("   If DGM Total = 610, Zone Total should include:");
//                             System.out.println("     - DGM's AppStatusTrack Type 3 (310) should NOT be in Zone");
//                             System.out.println("     - DGM's Admin→DGM (100) SHOULD be in Zone: " + adminToDgmDist);
//                             System.out
//                                     .println("     - DGM's Admin→Campus (200) SHOULD be in Zone: " + adminToCampusDist);
//                             System.out.println("     - Zone's own AppStatusTrack Type 2: " + statusMetrics.totalApp());
//                             System.out.println("     - Expected Zone Total: " + statusMetrics.totalApp() + " + "
//                                     + adminToDgmDist + " + " + adminToCampusDist + " = " + totalApp);
//                             System.out.println("========================================");

//                             // Return metrics with added distribution counts for totalApp
//                             // But use app_available and app_issued directly from AppStatusTrack
//                             // (app_issued_type_id = 2) without adding distributions
//                             return Optional.of(new MetricsAggregateDTO(
//                                     totalApp, // totalApp + distributions
//                                     statusMetrics.appSold(),
//                                     statusMetrics.appConfirmed(),
//                                     statusMetrics.appAvailable(), // Use app_available from AppStatusTrack
//                                                                   // (app_issued_type_id = 2)
//                                     statusMetrics.appUnavailable(),
//                                     statusMetrics.appDamaged(),
//                                     statusMetrics.appIssued() // Use app_issued from AppStatusTrack (app_issued_type_id
//                                                               // = 2), NOT totalIssued with distributions
//                             ));
//                         },
//                         (yearId) -> {
//                             // System.out.println("🔍 getProMetricByZoneId_FromStatus called - Zone: " +
//                             // zoneIdInt + ", Year: " + yearId);
//                             Optional<Long> result = appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneIdInt,
//                                     yearId);
//                             // System.out.println("✅ PRO Metric: " + (result.isPresent() ? result.get() :
//                             // "Not found"));
//                             return result;
//                         },
//                         () -> {
//                             // System.out.println("🔍 findDistinctYearIdsByZoneId called for Zone: " +
//                             // zoneIdInt);
//                             List<Integer> yearList = appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt);
//                             // System.out.println("✅ Years returned: " + yearList);
//                             return yearList;
//                         }));

//         // System.out.println("========================================");
//         return analytics;
//     }

//     public CombinedAnalyticsDTO getDgmAnalytics(Integer dgmEmpId) {
//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         analytics.setGraphData(getGraphData(
//                 (yearId) -> userAppSoldRepository.getSalesSummaryByDgm(dgmEmpId, yearId),
//                 () -> userAppSoldRepository.findDistinctYearIdsByDgm(dgmEmpId)));
//         analytics.setMetricsData(
//                 getMetricsData(
//                         (yearId) -> appStatusTrackRepository.getMetricsByEmployeeAndYear(dgmEmpId, yearId),
//                         (yearId) -> userAppSoldRepository.getProMetricByDgm(dgmEmpId, yearId),
//                         () -> appStatusTrackRepository.findDistinctYearIdsByEmployee(dgmEmpId)));
//         return analytics;
//     }

//     // In AnalyticsService.java
//     public CombinedAnalyticsDTO getEmployeeAnalytics(Long empId) {
//         Integer empIdInt = empId.intValue();
//         System.out.println("========================================");
//         System.out.println("📊 DGM EMPLOYEE ANALYTICS - AUTO FETCH CAMPUS IDs");
//         System.out.println("========================================");
//         System.out.println("Employee ID: " + empIdInt);

//         // 1. Get all Campus IDs associated with this Employee in the sce_dgm table
//         // (AUTOMATIC)
//         // This is done automatically - no need to pass campusIds manually
//         List<Integer> campusIds = dgmRepository.findCampusIdsByEmployeeId(empIdInt);
//         System.out.println("✅ Campus IDs fetched from sce_dgm table: " + campusIds);

//         if (campusIds.isEmpty()) {
//             System.out.println("❌ ERROR: No active DGM records found for Employee ID: " + empId);
//             throw new RuntimeException("No active DGM records found for Employee ID: " + empId);
//         }

//         // 2. Get Zone ID for this DGM employee
//         Integer zoneId = dgmRepository.findZoneIdByEmpId(empIdInt).orElse(null);
//         if (zoneId == null) {
//             System.out.println("❌ ERROR: No zone found for Employee ID: " + empId);
//             throw new RuntimeException("No zone found for Employee ID: " + empId);
//         }
//         System.out.println("✅ Zone ID: " + zoneId);

//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         // 3. Use UserAppSold for graph data: entity_id IN (1, 3) for issued, entity_id
//         // = 4 for sold
//         // Campus IDs are automatically fetched from sce_dgm table - no manual input
//         // needed
//         System.out.println("----------------------------------------");
//         System.out.println("📊 GRAPH DATA - Using UserAppSold (entity_id IN (1,3) for issued, entity_id = 4 for sold)");
//         System.out.println("Campus IDs from sce_dgm table: " + campusIds);
//         System.out.println("Zone ID: " + zoneId);

//         // Use UserAppSold for graph data - entity_id = 3 for issued (totalAppCount),
//         // entity_id = 4 for sold
//         // IMPORTANT: Ensure unique series - same series counted only once
//         analytics.setGraphData(getGraphData(
//                 (yearId) -> {
//                     // Get issued and sold from UserAppSold with unique series (entity_id = 3 for
//                     // issued, entity_id = 4 for sold)
//                     // This ensures same series appears only once even if it exists in multiple
//                     // entity_ids
//                     List<Object[]> userAppSoldData = userAppSoldRepository
//                             .getSalesSummaryByCampusListWithEntity4Raw(campusIds, yearId);
//                     long issuedFromUserAppSold = 0L;
//                     long soldFromUserAppSold = 0L;

//                     if (!userAppSoldData.isEmpty() && userAppSoldData.get(0) != null) {
//                         Object[] row = userAppSoldData.get(0);
//                         issuedFromUserAppSold = ((Number) row[0]).longValue();
//                         soldFromUserAppSold = ((Number) row[1]).longValue();
//                     }

//                     // Add distributions with issued_to_type_id = 4 to issued count
//                     Integer adminToCampusDist = distributionRepository
//                             .sumAdminToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
//                             .orElse(0);
//                     Integer zoneToCampusDist = distributionRepository
//                             .sumZoneToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
//                             .orElse(0);

//                     long totalIssued = issuedFromUserAppSold + adminToCampusDist + zoneToCampusDist;

//                     System.out.println("DGM Year " + yearId + ": UserAppSold totalAppCount (entity_id=3)="
//                             + issuedFromUserAppSold +
//                             ", Sold (entity_id=4)=" + soldFromUserAppSold +
//                             ", Admin→Campus=" + adminToCampusDist + ", Zone→Campus=" + zoneToCampusDist +
//                             ", Total issued=" + totalIssued);
//                     return Optional.of(new GraphSoldSummaryDTO(totalIssued, soldFromUserAppSold));
//                 },
//                 () -> {
//                     // Find distinct years from UserAppSold for campusIds
//                     List<Integer> years = userAppSoldRepository.findDistinctYearIdsByCampusIds(campusIds);
//                     System.out.println("✅ Returning " + years.size() + " years from UserAppSold: " + years);
//                     return years;
//                 }));
//         System.out.println("========================================");
//         // 4. Get metrics data with distribution count added to issued count
//         analytics.setMetricsData(
//                 getMetricsData(
//                         (yearId) -> {
//                             // Get AppStatusTrack metrics with app_issued_type_id = 3 (DGM→Campus: for
//                             // totalApp, appIssued, appAvailable, filtered by campusIds and zoneId)
//                             // totalApp is taken from AppStatusTrack where app_issued_type_id = 3 and
//                             // campus_id IN campusIds (e.g., 932)
//                             MetricsAggregateDTO statusMetrics = appStatusTrackRepository
//                                     .getMetricsByCampusIdsAndYearForDgm(campusIds, zoneId, yearId)
//                                     .orElse(new MetricsAggregateDTO());
//                             // Get sold, confirmed, unavailable, damaged with app_issued_type_id = 4
//                             // (Campus/PRO, filtered by campusIds and zoneId)
//                             MetricsAggregateDTO proMetrics = appStatusTrackRepository
//                                     .getSoldConfirmedUnavailableDamagedByCampusIdsAndYearForDgm(campusIds, zoneId,
//                                             yearId)
//                                     .orElse(new MetricsAggregateDTO());
//                             // Get admin-to-campus distribution count (Admin→Campus: issued_by_type_id = 1,
//                             // issued_to_type_id = 4, filtered by campusIds)
//                             Integer adminToCampusDist = distributionRepository
//                                     .sumAdminToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
//                                     .orElse(0);
//                             // Get zone-to-campus distribution count (Zone→Campus: issued_by_type_id = 2,
//                             // issued_to_type_id = 4, filtered by campusIds)
//                             Integer zoneToCampusDist = distributionRepository
//                                     .sumZoneToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
//                                     .orElse(0);

//                             // Display detailed breakdown for DGM Available and Issued Calculation
//                             System.out.println("========================================");
//                             System.out.println("📊 DGM AVAILABLE & ISSUED CALCULATION");
//                             System.out.println("========================================");
//                             System.out.println("Employee ID: " + empIdInt);
//                             System.out.println("Zone ID: " + zoneId);
//                             System.out.println("Campus IDs: " + campusIds);
//                             System.out.println("Academic Year ID: " + yearId);
//                             System.out.println("----------------------------------------");
//                             System.out.println(
//                                     "📈 FROM AppStatusTrack (app_issued_type_id = 3, campus_id IN campusIds):");
//                             System.out.println("   app_available: " + statusMetrics.appAvailable());
//                             System.out.println("   app_issued: " + statusMetrics.appIssued());
//                             System.out.println("   totalApp: " + statusMetrics.totalApp());
//                             System.out.println("----------------------------------------");
//                             System.out.println("📦 DISTRIBUTION TABLE COUNTS (for Total Applications only):");
//                             System.out.println("   Admin → Campus: " + adminToCampusDist + " (ADDED to totalApp only)");
//                             System.out.println("   Zone → Campus: " + zoneToCampusDist + " (ADDED to totalApp only)");
//                             System.out.println("----------------------------------------");

//                             // Combine: totalApp includes AppStatusTrack + distributions with
//                             // issued_to_type_id = 4 (Admin→Campus + Zone→Campus)
//                             // Available and Issued are taken DIRECTLY from AppStatusTrack
//                             // (app_issued_type_id = 3, campus_id IN campusIds)
//                             long totalDistCount = adminToCampusDist + zoneToCampusDist; // Only issued_to_type_id = 4
//                             long finalTotalApp = statusMetrics.totalApp() + totalDistCount;

//                             System.out.println("✅ FINAL DGM CALCULATION:");
//                             System.out.println("   Total Applications:");
//                             System.out.println("     - From AppStatusTrack (Type 3): " + statusMetrics.totalApp());
//                             System.out.println("     - Admin → Campus Distribution: " + adminToCampusDist
//                                     + " (issued_to_type_id = 4)");
//                             System.out.println("     - Zone → Campus Distribution: " + zoneToCampusDist
//                                     + " (issued_to_type_id = 4)");
//                             System.out.println("     - TOTAL APPLICATIONS: " + finalTotalApp);
//                             System.out.println(
//                                     "   Available (from AppStatusTrack, Type 3): " + statusMetrics.appAvailable());
//                             System.out.println("   Issued (from AppStatusTrack, Type 3): " + statusMetrics.appIssued());
//                             System.out.println("========================================");

//                             return Optional.of(new MetricsAggregateDTO(
//                                     finalTotalApp, // Add ONLY issued_to_type_id = 4 distributions (Admin→Campus +
//                                                    // Zone→Campus) to grand total
//                                     proMetrics.appSold(), // From app_issued_type_id = 4
//                                     proMetrics.appConfirmed(), // From app_issued_type_id = 4
//                                     statusMetrics.appAvailable(), // From app_issued_type_id = 3, campus_id IN campusIds
//                                                                   // (DIRECT from AppStatusTrack)
//                                     proMetrics.appUnavailable(), // From app_issued_type_id = 4
//                                     proMetrics.appDamaged(), // From app_issued_type_id = 4
//                                     statusMetrics.appIssued() // From app_issued_type_id = 3, campus_id IN campusIds
//                                                               // (DIRECT from AppStatusTrack, NO distributions added)
//                             ));
//                         },
//                         (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
//                         () -> appStatusTrackRepository.findDistinctYearIdsByCampusIdsForDgm(campusIds, zoneId)));
//         return analytics;
//     }

//     public CombinedAnalyticsDTO getCampusAnalytics(Long campusId) {
//         return getCampusAnalytics(campusId, null);
//     }

//     public CombinedAnalyticsDTO getCampusAnalytics(Long campusId, Integer proEmpId) {
//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         Integer campusIdInt = campusId.intValue();

//         System.out.println("========================================");
//         System.out.println("📊 CAMPUS ANALYTICS");
//         System.out.println("========================================");
//         System.out.println("Campus ID: " + campusIdInt);
//         if (proEmpId != null) {
//             System.out.println("PRO Employee ID: " + proEmpId);
//         }

//         // Use UserAppSold for graph data: entity_id = 4 for both issued (totalAppCount)
//         // and sold
//         System.out.println("Using UserAppSold - entity_id = 4 for issued (totalAppCount) and sold");
//         analytics.setGraphData(getGraphData(
//                 (yearId) -> {
//                     // Get data from UserAppSold with entity_id = 4 for the campus
//                     Optional<GraphSoldSummaryDTO> userAppSoldData = userAppSoldRepository
//                             .getSalesSummaryByCampusIdAndYear(campusIdInt, yearId);
//                     if (userAppSoldData.isEmpty()) {
//                         return Optional.of(new GraphSoldSummaryDTO(0L, 0L));
//                     }

//                     long issued = userAppSoldData.get().totalApplications(); // totalAppCount from entity_id = 4
//                     long sold = userAppSoldData.get().totalSold(); // sold from entity_id = 4

//                     System.out.println("Campus " + campusIdInt + " Year " + yearId
//                             + ": Issued (totalAppCount, entity_id=4)=" + issued + ", Sold (entity_id=4)=" + sold);
//                     return Optional.of(new GraphSoldSummaryDTO(issued, sold));
//                 },
//                 () -> {
//                     // Find distinct years from UserAppSold (entity_id = 4)
//                     return userAppSoldRepository.findDistinctYearIdsByCampusId(campusIdInt);
//                 }));
//         System.out.println("✅ Graph data created - using UserAppSold with entity_id = 4");
//         System.out.println("========================================");

//         // Metrics use AppStatusTrack with app_issued_type_id = 4
//         // For PRO role: if PRO has distributed, calculate issued and available based on
//         // distributions
//         analytics.setMetricsData(
//                 getMetricsData(
//                         (yearId) -> {
//                             MetricsAggregateDTO metrics = appStatusTrackRepository
//                                     .getMetricsByCampusAndYearWithType4(campusId, yearId)
//                                     .orElse(new MetricsAggregateDTO());

//                             // For direct campus endpoint (/api/analytics/campus/{id}): ALWAYS set available
//                             // = 0, issued = 0
//                             if (proEmpId == null) {
//                                 System.out.println("========================================");
//                                 System.out
//                                         .println("📊 CAMPUS ANALYTICS - Direct Endpoint (/api/analytics/campus/{id})");
//                                 System.out.println("========================================");
//                                 System.out.println("Campus ID: " + campusIdInt);
//                                 System.out.println("Year: " + yearId);
//                                 System.out.println("Total App (from AppStatusTrack): " + metrics.totalApp());
//                                 System.out.println(
//                                         "✅ Direct endpoint - FORCING available = 0, issued = 0 (in metrics card)");
//                                 System.out.println("========================================");

//                                 // ALWAYS return 0 for available and issued for direct campus endpoint
//                                 return Optional.of(new MetricsAggregateDTO(
//                                         metrics.totalApp(), // totalApp (unchanged)
//                                         metrics.appSold(), // appSold (unchanged)
//                                         metrics.appConfirmed(), // appConfirmed (unchanged)
//                                         0L, // appAvailable = 0 (FORCED for direct endpoint)
//                                         metrics.appUnavailable(), // appUnavailable (unchanged)
//                                         metrics.appDamaged(), // appDamaged (unchanged)
//                                         0L // appIssued = 0 (FORCED for direct endpoint)
//                                 ));
//                             }

//                             // For PRO role login (proEmpId != null): check distributions
//                             Integer proDistributed = distributionRepository
//                                     .sumProDistributionByEmpIdAndYear(proEmpId, yearId)
//                                     .orElse(0);

//                             System.out.println("========================================");
//                             System.out.println("📊 PRO DISTRIBUTION CHECK");
//                             System.out.println("========================================");
//                             System.out.println("PRO Employee ID: " + proEmpId);
//                             System.out.println("Year: " + yearId);
//                             System.out.println("Total App (from AppStatusTrack): " + metrics.totalApp());
//                             System.out.println("PRO Distributed: " + proDistributed);

//                             if (proDistributed > 0) {
//                                 // PRO has distributed: issued = distributed count, available = totalApp -
//                                 // distributed
//                                 long issued = (long) proDistributed;
//                                 long available = Math.max(0L, metrics.totalApp() - (long) proDistributed);

//                                 System.out.println("✅ PRO has distributed applications");
//                                 System.out.println("Issued (distributed): " + issued);
//                                 System.out.println("Available (totalApp - distributed): " + available);

//                                 // Return modified metrics with calculated issued and available
//                                 return Optional.of(new MetricsAggregateDTO(
//                                         metrics.totalApp(), // totalApp (unchanged)
//                                         metrics.appSold(), // appSold (unchanged)
//                                         metrics.appConfirmed(), // appConfirmed (unchanged)
//                                         available, // appAvailable (calculated)
//                                         metrics.appUnavailable(), // appUnavailable (unchanged)
//                                         metrics.appDamaged(), // appDamaged (unchanged)
//                                         issued // appIssued (calculated from distributions)
//                                 ));
//                             } else {
//                                 // PRO has NOT distributed: available = 0, issued = 0
//                                 System.out.println("ℹ️ PRO has NOT distributed - available = 0, issued = 0");
//                                 System.out.println("Issued: 0");
//                                 System.out.println("Available: 0");

//                                 return Optional.of(new MetricsAggregateDTO(
//                                         metrics.totalApp(), // totalApp (unchanged)
//                                         metrics.appSold(), // appSold (unchanged)
//                                         metrics.appConfirmed(), // appConfirmed (unchanged)
//                                         0L, // appAvailable = 0 (no distributions)
//                                         metrics.appUnavailable(), // appUnavailable (unchanged)
//                                         metrics.appDamaged(), // appDamaged (unchanged)
//                                         0L // appIssued = 0 (no distributions)
//                                 ));
//                             }
//                         },
//                         // Use AppStatusTrack repo with app_issued_type_id = 4
//                         (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusIdInt, yearId),
//                         () -> appStatusTrackRepository.findDistinctYearIdsByCampusWithType4(campusId)));
//         return analytics;
//     }

//     public GraphDTO getGraphDataByZoneIdAndAmount(Integer zoneId, Float amount) {
//         if (zoneId == null || amount == null) {
//             GraphDTO emptyGraph = new GraphDTO();
//             emptyGraph.setTitle("Error: Zone ID and Amount must be provided.");
//             emptyGraph.setYearlyData(new ArrayList<>());
//             return emptyGraph;
//         }
//         // This leverages the generic getGraphData helper with new repository functions
//         return getGraphData(
//                 // Data Fetcher: Function<Integer, Optional<GraphSoldSummaryDTO>> (takes yearId)
//                 (yearId) -> userAppSoldRepository.getSalesSummaryByZoneAndAmount(zoneId, yearId, amount),
//                 // Year Fetcher: Supplier<List<Integer>> (takes no arguments)
//                 () -> userAppSoldRepository.findDistinctYearIdsByZoneAndAmount(zoneId, amount));
//     }

//     public GraphDTO getGraphDataByCampusIdAndAmount(Integer campusId, Float amount) {
//         if (campusId == null || amount == null) {
//             GraphDTO emptyGraph = new GraphDTO();
//             emptyGraph.setTitle("Error: Campus ID and Amount must be provided.");
//             emptyGraph.setYearlyData(new ArrayList<>());
//             return emptyGraph;
//         }
//         // This leverages the generic getGraphData helper with new repository functions
//         return getGraphData(
//                 // Data Fetcher: Function<Integer, Optional<GraphSoldSummaryDTO>> (takes yearId)
//                 (yearId) -> userAppSoldRepository.getSalesSummaryByCampusAndAmount(campusId, yearId, amount),
//                 // Year Fetcher: Supplier<List<Integer>> (takes no arguments)
//                 () -> userAppSoldRepository.findDistinctYearIdsByCampusAndAmount(campusId, amount));
//     }

//     // private CombinedAnalyticsDTO getDgmDirectAnalytics(SCEmployeeEntity employee)
//     // {
//     // int empId = employee.getEmpId();
//     //
//     // // 1. Fetch DGM Record to get Campus ID
//     // // Assuming findByEmployee_EmpId returns List or Optional. Taking first for
//     // safety.
//     // Dgm dgmRecord = dgmRepository.lookupByEmpId(empId).orElse(null);
//     //
//     // if (dgmRecord == null || dgmRecord.getCampus() == null) {
//     // return createEmptyAnalytics("DGM", empId, "DGM not mapped to a Campus",
//     // employee.getDesignationName());
//     // }
//     //
//     // int campusId = dgmRecord.getCampus().getCampusId(); // Pick Campus ID
//     // String campusName = dgmRecord.getCampus().getCampusName(); // Assuming you
//     // have name in Campus entity
//     //
//     // // 2. Get Data using Campus ID directly
//     // CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//     //
//     // // Use the new Repo methods created in Step 1
//     // analytics.setGraphData(getGraphDataForCampus(campusId));
//     // analytics.setMetricsData(getMetricsDataForCampus(campusId));
//     //
//     // // 3. Set Header Info
//     // analytics.setRole("DGM");
//     // analytics.setDesignationName(employee.getDesignationName());
//     // analytics.setEntityName(campusName); // Showing Campus Name
//     // analytics.setEntityId(campusId);
//     //
//     // return analytics;
//     // }
//     private CombinedAnalyticsDTO getDgmDirectAnalytics(SCEmployeeEntity employee) {
//         int empId = employee.getEmpId();
//         System.out.println("DEBUG: getDgmDirectAnalytics for empId: " + empId);
//         // 1. Fetch ALL DGM Records for this employee to get ALL Campus IDs
//         // Assuming your dgmRepository has: List<Dgm> findByEmployee_EmpId(int empId)
//         List<Dgm> dgmRecords = dgmRepository.findAllByEmployeeId(empId);
//         System.out.println("DEBUG: Found " + dgmRecords.size() + " DGM records for employee " + empId);
//         if (dgmRecords.isEmpty()) {
//             System.out.println("❌ ERROR: No DGM records found for employee " + empId);
//             return createEmptyAnalytics("DGM", empId, "No Campuses mapped to this DGM", employee.getDesignationName());
//         }
//         // Extract all Campus IDs into a List
//         List<Integer> campusIds = dgmRecords.stream()
//                 .map(d -> d.getCampus().getCampusId())
//                 .collect(Collectors.toList());
//         System.out.println("DEBUG: Campus IDs for DGM: " + campusIds);
//         // 2. Get Aggregated Data using the List of IDs
//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         GraphDTO graphData = getGraphDataForCampuses(campusIds);
//         MetricsDataDTO metricsData = getMetricsDataForCampuses(campusIds);
//         analytics.setGraphData(graphData);
//         analytics.setMetricsData(metricsData);
//         System.out.println("DEBUG: Graph data - yearlyData size: "
//                 + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
//         System.out.println("DEBUG: Metrics data - metrics size: "
//                 + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
//         // 3. Set Header Info
//         analytics.setRole("DGM");
//         analytics.setDesignationName(employee.getDesignationName());
//         // For entity name, you can show a count or join names: "3 Campuses" or "Campus
//         // A, Campus B..."
//         analytics.setEntityName(dgmRecords.size() + " Campuses Managed");
//         analytics.setEntityId(empId); // Using EmpId as the identifier for the group
//         System.out.println("========================================");
//         return analytics;
//     }

//     /**
//      * PRIVATE: Gets analytics for a Zonal Accountant's *managed DGMs*.
//      */
//     private CombinedAnalyticsDTO getZonalDirectAnalytics(SCEmployeeEntity employee) {
//         int empId = employee.getEmpId();
//         System.out.println("DEBUG: getZonalDirectAnalytics for empId: " + empId);
//         // 1. Fetch ZonalAccountant Record to get Zone ID (handle multiple results by
//         // taking first)
//         List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
//         System.out.println("DEBUG: Found " + (zonalRecords != null ? zonalRecords.size() : 0)
//                 + " ZonalAccountant records for employee " + empId);
//         if (zonalRecords == null || zonalRecords.isEmpty()) {
//             System.out.println("❌ ERROR: No ZonalAccountant records found for employee " + empId);
//             return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone",
//                     employee.getDesignationName());
//         }
//         // Get the first active record (most recent based on zone_acct_id DESC if
//         // needed)
//         ZonalAccountant zonalRecord = zonalRecords.get(0);
//         if (zonalRecord.getZone() == null) {
//             return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone",
//                     employee.getDesignationName());
//         }
//         int zoneId = zonalRecord.getZone().getZoneId(); // Pick Zone ID
//         String zoneName = zonalRecord.getZone().getZoneName();
//         System.out.println("DEBUG: Zone ID: " + zoneId + ", Zone Name: " + zoneName);
//         // 2. Get Data using Zone ID directly
//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         // Use the new Repo methods created in Step 1
//         GraphDTO graphData = getGraphDataForZone(zoneId);
//         MetricsDataDTO metricsData = getMetricsDataForZone(zoneId);
//         analytics.setGraphData(graphData);
//         analytics.setMetricsData(metricsData);
//         System.out.println("DEBUG: Graph data - yearlyData size: "
//                 + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
//         System.out.println("DEBUG: Metrics data - metrics size: "
//                 + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
//         // 3. Set Header Info
//         analytics.setRole("Zonal Accountant");
//         analytics.setDesignationName(employee.getDesignationName());
//         analytics.setEntityName(zoneName); // Showing Zone Name
//         analytics.setEntityId(zoneId);
//         System.out.println("========================================");
//         return analytics;
//     }

//     /**
//      * PRIVATE: Gets analytics for PRO and other roles - shows campus data for the
//      * employee's campus.
//      */
//     private CombinedAnalyticsDTO getCampusDirectAnalytics(SCEmployeeEntity employee) {
//         int empId = employee.getEmpId();
//         int campusId = employee.getEmpCampusId();
//         String campusName = employee.getCampusName();
//         String role = employee.getEmpStudApplicationRole();
//         System.out.println("DEBUG: getCampusDirectAnalytics for empId: " + empId + ", campusId: " + campusId
//                 + ", campusName: " + campusName);
//         // Check if employee has a valid campus ID
//         if (campusId <= 0) {
//             System.out.println("❌ ERROR: Employee " + empId + " has invalid campusId: " + campusId);
//             return createEmptyAnalytics(role != null ? role : "Unknown", empId, "Employee not mapped to a Campus",
//                     employee.getDesignationName());
//         }
//         System.out.println("✓ Fetching analytics data for campusId: " + campusId);
//         // Get Data using Campus ID directly
//         CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
//         GraphDTO graphData = getGraphDataForCampus(campusId);
//         MetricsDataDTO metricsData = getMetricsDataForCampus(campusId);
//         analytics.setGraphData(graphData);
//         analytics.setMetricsData(metricsData);
//         System.out.println("DEBUG: Graph data - yearlyData size: "
//                 + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
//         System.out.println("DEBUG: Metrics data - metrics size: "
//                 + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
//         // Set Header Info
//         analytics.setRole(role != null ? role : "Employee");
//         analytics.setDesignationName(employee.getDesignationName());
//         analytics.setEntityName(campusName != null ? campusName : "Campus " + campusId);
//         analytics.setEntityId(campusId);
//         System.out.println("========================================");
//         return analytics;
//     }

//     // --- PRIVATE HELPER METHODS for ROLLUPS (Unchanged) ---
//     // =========================================================================
//     // DGM / CAMPUS DIRECT HELPERS
//     // =========================================================================
//     private GraphDTO getGraphDataForCampus(Integer campusId) {
//         return getGraphData(
//                 (yearId) -> {
//                     Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository
//                             .getMetricsByCampusIdAndYear(campusId, yearId);
//                     return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
//                 },
//                 () -> appStatusTrackRepository.findDistinctYearIdsByCampusId(campusId));
//     }

//     private MetricsDataDTO getMetricsDataForCampus(Integer campusId) {
//         return getMetricsData(
//                 // 1. Main Metrics (Total, Issued, Damaged, etc.)
//                 (yearId) -> appStatusTrackRepository.getMetricsByCampusIdAndYear(campusId, yearId),
//                 // 2. Pro Metric (Sold count specifically for the card)
//                 (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusId, yearId),
//                 // 3. Distinct Years
//                 () -> appStatusTrackRepository.findDistinctYearIdsByCampusId(campusId));
//     }

//     private GraphDTO getGraphDataForCampuses(List<Integer> campusIds) {
//         return getGraphData(
//                 (yearId) -> {
//                     Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository
//                             .getMetricsByCampusIdsAndYear(campusIds, yearId);
//                     return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
//                 },
//                 () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds));
//     }

//     private MetricsDataDTO getMetricsDataForCampuses(List<Integer> campusIds) {
//         return getMetricsData(
//                 // 1. Aggregated Metrics for the list of campuses
//                 (yearId) -> appStatusTrackRepository.getMetricsByCampusIdsAndYear(campusIds, yearId),
//                 // 2. Pro Metric for the list of campuses
//                 (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
//                 // 3. Distinct Years across all campuses
//                 () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds));
//     }

//     // =========================================================================
//     // ZONAL / ZONE DIRECT HELPERS
//     // =========================================================================
//     private GraphDTO getGraphDataForZone(Integer zoneId) {
//         return getGraphData(
//                 (yearId) -> {
//                     Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneId,
//                             yearId);
//                     return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
//                 },
//                 () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneId));
//     }

//     private MetricsDataDTO getMetricsDataForZone(Integer zoneId) {
//         return getMetricsData(
//                 // 1. Main Metrics
//                 (yearId) -> appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneId, yearId),
//                 // 2. Pro Metric
//                 (yearId) -> appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneId, yearId),
//                 // 3. Distinct Years
//                 () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneId));
//     }

//     // --- Private Graph Data Helper (Unchanged) ---
//     private GraphDTO getGraphData(
//             Function<Integer, Optional<GraphSoldSummaryDTO>> dataFetcher,
//             Supplier<List<Integer>> yearFetcher) {
//         GraphDTO graphData = new GraphDTO();
//         List<YearlyGraphPointDTO> yearlyDataList = new ArrayList<>();
//         try {
//             List<Integer> existingYearIds = yearFetcher.get();

//             // Get current year (latest year ID from the data)
//             int currentYearId;
//             if (!existingYearIds.isEmpty()) {
//                 existingYearIds.sort(Integer::compare);
//                 currentYearId = existingYearIds.get(existingYearIds.size() - 1);
//             } else {
//                 // If no data, get the latest year from all academic years
//                 List<AcademicYear> allYears = academicYearRepository.findAll();
//                 if (allYears.isEmpty()) {
//                     graphData.setTitle("Application Sales Percentage (No Data)");
//                     graphData.setYearlyData(yearlyDataList);
//                     return graphData;
//                 }
//                 currentYearId = allYears.stream()
//                         .max(Comparator.comparingInt(AcademicYear::getAcdcYearId))
//                         .map(AcademicYear::getAcdcYearId)
//                         .orElse(0);
//             }

//             // Create list of 4 years: current + 3 previous years
//             List<Integer> yearIds = new ArrayList<>();
//             for (int i = 0; i < 4; i++) {
//                 yearIds.add(currentYearId - i);
//             }

//             // Get AcademicYear entities for all 4 years
//             List<AcademicYear> academicYears = academicYearRepository.findByAcdcYearIdIn(yearIds)
//                     .stream()
//                     .sorted(Comparator.comparingInt(AcademicYear::getAcdcYearId))
//                     .toList();

//             // Create a map for quick lookup
//             java.util.Map<Integer, AcademicYear> yearMap = academicYears.stream()
//                     .collect(Collectors.toMap(AcademicYear::getAcdcYearId, y -> y));

//             // Build graph data for all 4 years (always return 4 years, even if some have 0
//             // values)
//             for (Integer yearId : yearIds) {
//                 AcademicYear year = yearMap.get(yearId);
//                 if (year == null) {
//                     // If year doesn't exist in database, skip it
//                     continue;
//                 }

//                 String yearLabel = year.getAcademicYear();
//                 GraphSoldSummaryDTO summary = dataFetcher.apply(yearId)
//                         .orElse(new GraphSoldSummaryDTO(0L, 0L));
//                 long issued = summary.totalApplications();
//                 long sold = summary.totalSold();
//                 double issuedPercent = issued > 0 ? 100.0 : 0.0;
//                 double soldPercent = (issued > 0)
//                         ? Math.min(100.0, ((double) sold / issued) * 100.0)
//                         : 0.0;
//                 yearlyDataList.add(new YearlyGraphPointDTO(
//                         yearLabel, issuedPercent, soldPercent, issued, sold));
//             }

//             if (!yearlyDataList.isEmpty()) {
//                 graphData.setTitle("Application Sales Percentage (" +
//                         yearlyDataList.get(0).getYear() + "–" +
//                         yearlyDataList.get(yearlyDataList.size() - 1).getYear() + ")");
//             } else {
//                 graphData.setTitle("Application Sales Percentage (No Data)");
//             }
//         } catch (Exception e) {
//             System.err.println("Error fetching graph data: " + e.getMessage());
//             e.printStackTrace();
//         }
//         graphData.setYearlyData(yearlyDataList);
//         return graphData;
//     }

//     // --- Private Metrics Data Helper (Unchanged) ---
//     private MetricsDataDTO getMetricsData(
//             Function<Integer, Optional<MetricsAggregateDTO>> dataFetcher,
//             Function<Integer, Optional<Long>> proFetcher,
//             Supplier<List<Integer>> yearFetcher) {
//         System.out.println("🔍 getMetricsData called");
//         MetricsDataDTO dto = new MetricsDataDTO();
//         try {
//             List<Integer> yearIds = yearFetcher.get();
//             System.out.println("📅 Year IDs from fetcher: " + yearIds);
//             if (yearIds.isEmpty()) {
//                 System.out.println("❌ No years found - returning empty metrics");
//                 dto.setMetrics(new ArrayList<>());
//                 return dto;
//             }
//             // Sort yearIds ascending → last one is current year
//             yearIds.sort(Integer::compare);
//             int currentYearId = yearIds.get(yearIds.size() - 1);
//             // Always use previous year (currentYearId - 1) for comparison, similar to graph
//             // logic
//             // This ensures we show percentage change even when only one year of data exists
//             int previousYearId = currentYearId - 1;
//             System.out.println("📅 Current Year ID: " + currentYearId);
//             System.out.println("📅 Previous Year ID: " + previousYearId + " (calculated as currentYearId - 1)");

//             AcademicYear cy = academicYearRepository.findById(currentYearId).orElse(null);
//             AcademicYear py = academicYearRepository.findById(previousYearId).orElse(null);

//             // If previous year doesn't exist by ID, try to find it by year (currentYear -
//             // 1)
//             if (py == null && cy != null) {
//                 int previousYearNumber = cy.getYear() - 1;
//                 py = academicYearRepository.findByYear(previousYearNumber).orElse(null);
//                 System.out.println("📅 Previous year not found by ID " + previousYearId + ", trying to find by year "
//                         + previousYearNumber);
//             }

//             System.out.println("📅 Current Year Entity: " + (cy != null ? cy.getAcademicYear() : "NULL"));
//             System.out.println("📅 Previous Year Entity: " + (py != null ? py.getAcademicYear() : "NULL"));

//             dto.setCurrentYear(cy != null ? cy.getYear() : 0);
//             dto.setPreviousYear(py != null ? py.getYear() : (cy != null ? cy.getYear() - 1 : 0));

//             System.out.println("🔍 Fetching current year metrics...");
//             MetricsAggregateDTO curr = dataFetcher.apply(currentYearId)
//                     .orElse(new MetricsAggregateDTO());
//             System.out.println("🔍 Fetching previous year metrics...");
//             MetricsAggregateDTO prev = dataFetcher.apply(previousYearId)
//                     .orElse(new MetricsAggregateDTO());

//             System.out.println("📊 Current Metrics - TotalApp: " + curr.totalApp() + ", Sold: " + curr.appSold()
//                     + ", Confirmed: " + curr.appConfirmed() + ", Available: " + curr.appAvailable() + ", Issued: "
//                     + curr.appIssued());
//             System.out.println("📊 Previous Metrics - TotalApp: " + prev.totalApp() + ", Sold: " + prev.appSold()
//                     + ", Confirmed: " + prev.appConfirmed() + ", Available: " + prev.appAvailable() + ", Issued: "
//                     + prev.appIssued());

//             System.out.println("🔍 Fetching PRO metrics...");
//             long proCurr = proFetcher.apply(currentYearId).orElse(0L);
//             long proPrev = proFetcher.apply(previousYearId).orElse(0L);
//             System.out.println("📊 PRO Current: " + proCurr + ", PRO Previous: " + proPrev);
//             MetricsAggregateDTO totalMetrics = curr; // instead of summing every year
//             long totalPro = proCurr;
//             // ------------------------------------------------------
//             List<MetricDTO> cards = buildMetricsList(curr, prev, totalMetrics, proCurr, proPrev, totalPro);
//             System.out.println("✅ Built " + cards.size() + " metric cards");
//             dto.setMetrics(cards);
//             System.out.println("========================================");
//         } catch (Exception ex) {
//             System.out.println("🔥 METRICS ERROR: " + ex.getMessage());
//             ex.printStackTrace();
//             dto.setMetrics(new ArrayList<>());
//         }
//         return dto;
//     }

//     /**
//      * Builds the metrics list.
//      */
//     private List<MetricDTO> buildMetricsList(
//             MetricsAggregateDTO current, MetricsAggregateDTO previous, MetricsAggregateDTO total,
//             long proCurrent, long proPrevious, long totalPro) {
//         List<MetricDTO> metrics = new ArrayList<>();
//         metrics.add(createMetric("Total Applications",
//                 total.totalApp(),
//                 current.totalApp(), previous.totalApp()));
//         double soldPercentCurrent = calculatePercentage(current.appSold(), current.totalApp());
//         double soldPercentPrevious = calculatePercentage(previous.appSold(), previous.totalApp());
//         metrics.add(createMetricWithPercentage("Sold",
//                 total.appSold(),
//                 soldPercentCurrent, soldPercentPrevious));
//         double confirmedPercentCurrent = calculatePercentage(current.appConfirmed(), current.totalApp());
//         double confirmedPercentPrevious = calculatePercentage(previous.appConfirmed(), previous.totalApp());
//         metrics.add(createMetricWithPercentage("Confirmed",
//                 total.appConfirmed(),
//                 confirmedPercentCurrent, confirmedPercentPrevious));
//         // Use appAvailable field directly from MetricsAggregateDTO (not calculated as
//         // totalApp - appIssued)
//         // This allows direct campus endpoint to show available = 0 as set in the data
//         // fetcher
//         metrics.add(createMetric("Available",
//                 total.appAvailable(),
//                 current.appAvailable(), previous.appAvailable()));
//         long validIssuedCurrent = Math.max(0, current.appIssued());
//         long validIssuedPrevious = Math.max(0, previous.appIssued());
//         double issuedPercentCurrent = calculatePercentage(validIssuedCurrent, current.totalApp());
//         double issuedPercentPrevious = calculatePercentage(validIssuedPrevious, previous.totalApp());
//         metrics.add(createMetricWithPercentage("Issued",
//                 total.appIssued(),
//                 issuedPercentCurrent, issuedPercentPrevious));
//         metrics.add(createMetric("Damaged",
//                 total.appDamaged(),
//                 current.appDamaged(), previous.appDamaged()));
//         metrics.add(createMetric("Unavailable",
//                 total.appUnavailable(),
//                 current.appUnavailable(), previous.appUnavailable()));
//         metrics.add(createMetric("With PRO",
//                 totalPro,
//                 proCurrent, proPrevious));
//         return metrics;
//     }

//     // --- UTILITY METHODS ---
//     private MetricDTO createMetric(String title, long totalValue, long currentValue, long previousValue) {
//         double change = calculatePercentageChange(currentValue, previousValue);
//         return new MetricDTO(title, totalValue, change, getChangeDirection(change));
//     }

//     private MetricDTO createMetricWithPercentage(String title, long totalValue, double currentPercent,
//             double previousPercent) {
//         double change = calculatePercentageChange(currentPercent, previousPercent);
//         return new MetricDTO(title, totalValue, change, getChangeDirection(change));
//     }

//     private double calculatePercentage(long numerator, long denominator) {
//         if (denominator == 0)
//             return 0.0;
//         return (double) Math.max(0, numerator) * 100.0 / denominator;
//     }

//     private double calculatePercentageChange(double current, double previous) {
//         // If previous year has no data (0) and current year has data (> 0): show 100%
//         // (like graph issued percentage is 100 when data exists)
//         if (previous == 0)
//             return (current > 0) ? 100.0 : 0.0;
//         // If current year has no data (0): show 0% (like graph when no data, not -100%)
//         if (current == 0)
//             return 0.0;
//         // If both have data: calculate normal percentage change
//         double change = ((current - previous) / previous) * 100;
//         return Math.round(change);
//     }

//     private String getChangeDirection(double change) {
//         if (change > 0)
//             return "up";
//         if (change < 0)
//             return "down";
//         return "neutral";
//     }

//     private int getAcdcYearId(int year) {
//         return academicYearRepository.findByYear(year)
//                 .map(AcademicYear::getAcdcYearId)
//                 .orElse(0);
//     }

//     // --- NEW: Flexible Graph Data Method with Optional Filters ---
//     /**
//      * Get year-wise graph data (GraphBarDTO) with optional filters for zoneId,
//      * campusIds, campusId, and amount.
//      * All parameters are optional. Always returns data for the past 4 years
//      * (current + 3 previous).
//      * If data doesn't exist for a year, returns 0 values for that year.
//      *
//      * IMPORTANT:
//      * - campusId (singular) uses entity_id = 4 (single campus/PRO role)
//      * - campusIds (plural) uses entity_id = 3 (DGM rollup with multiple campuses)
//      *
//      * @param zoneId    Optional zone ID filter
//      * @param campusIds Optional list of campus IDs filter (uses entity_id = 3 for
//      *                  DGM rollup)
//      * @param campusId  Optional single campus ID filter (uses entity_id = 4 for
//      *                  single campus)
//      * @param amount    Optional amount filter
//      * @return List of GraphBarDTO containing year-wise issued and sold data for
//      *         past 4 years
//      */
//     public List<GraphBarDTO> getFlexibleGraphData(Integer zoneId, List<Integer> campusIds, Integer campusId,
//             Float amount, Integer empId) {
//         // Get current year (latest year) from AppStatusTrackRepository
//         Integer currentYearId = appStatusTrackRepository.findLatestYearId();
//         if (currentYearId == null) {
//             return new ArrayList<>();
//         }
//         // Get previous 4 years (current year + 3 previous years)
//         List<Integer> yearIds = new ArrayList<>();
//         for (int i = 0; i < 4; i++) {
//             yearIds.add(currentYearId - i);
//         }
//         List<Object[]> rows;
//         // IMPORTANT:
//         // - campusId (singular) uses entity_id = 4 (single campus methods)
//         // - campusIds (plural) uses entity_id = 3 (list methods, even for single
//         // element)

//         // IMPORTANT: Keep campusId (singular) and campusIds (plural) separate
//         // - campusId (singular) uses entity_id = 4 (single campus/PRO)
//         // - campusIds (plural) uses entity_id = 3 (DGM rollup)
//         // Do NOT merge them - handle separately
//         boolean hasCampusId = campusId != null;
//         boolean hasCampusIds = campusIds != null && !campusIds.isEmpty();

//         // Only merge for campusIds list (DGM rollup), not for single campusId
//         List<Integer> effectiveCampusIds = new ArrayList<>();
//         if (campusIds != null)
//             effectiveCampusIds.addAll(campusIds);

//         boolean hasCampuses = hasCampusIds; // Only true if campusIds (plural) is provided

//         // DEBUG: Check individual campus data before aggregation (for multiple campuses
//         // from campusIds)
//         if (hasCampuses && effectiveCampusIds.size() > 1) {
//             System.out.println("=== DEBUG: CHECKING INDIVIDUAL CAMPUS DATA FOR AGGREGATION (campusIds) ===");
//             System.out.println("Effective Campus IDs: " + effectiveCampusIds);
//             for (Integer id : effectiveCampusIds) {
//                 List<Object[]> individualCampusRows;
//                 if (amount != null) {
//                     individualCampusRows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(id, amount);
//                 } else {
//                     individualCampusRows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(id);
//                 }
//                 // Filter by yearIds
//                 individualCampusRows = individualCampusRows.stream()
//                         .filter(row -> yearIds.contains((Integer) row[0]))
//                         .collect(java.util.stream.Collectors.toList());
//                 System.out.println("Campus ID " + id + " - Individual data (filtered by years, entity_id=4):");
//                 if (individualCampusRows.isEmpty()) {
//                     System.out.println(
//                             " ⚠️ NO DATA FOUND for Campus ID " + id + " (for the past 4 years with current filters)");
//                 } else {
//                     for (Object[] row : individualCampusRows) {
//                         Integer yearId = (Integer) row[0];
//                         Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
//                         Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
//                         System.out.println("  Year ID: " + yearId + " | Issued: " + totalAppCount + " | Sold: " + sold);
//                     }
//                 }
//             }
//             System.out.println("=== END INDIVIDUAL CAMPUS DATA DEBUG ===");
//         }

//         // DEBUG: Log single campusId usage
//         if (hasCampusId && !hasCampuses) {
//             System.out.println("=== DEBUG: SINGLE CAMPUS (campusId) MODE ===");
//             System.out.println("Campus ID: " + campusId + " (entity_id=4, issued = total - available)");
//             System.out.println("=== END SINGLE CAMPUS DEBUG ===");
//         }

//         // Determine which repository method to call based on provided parameters
//         // Priority: Check campusId (singular, entity_id=4) first, then campusIds
//         // (plural, entity_id=3)
//         if (zoneId != null && hasCampusId && amount != null) {
//             // Zone + Single Campus (campusId) + Amount - use single campus method with
//             // entity_id = 4
//             System.out.println("Filter: Zone + Single Campus (campusId) + Amount (zone=" + zoneId + ", campusId="
//                     + campusId + ", amt=" + amount + ")");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusAndAmount(zoneId, campusId, amount);
//         } else if (zoneId != null && hasCampuses && amount != null) {
//             // Zone + Campuses (campusIds) + Amount - use NEW method with entity_id = 4
//             System.out.println("Filter: Zone + Campuses (campusIds) + Amount (zone=" + zoneId + ", camps="
//                     + effectiveCampusIds + ", amt=" + amount + ") - Using entity_id = 4");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListAndAmountWithEntity4(zoneId,
//                     effectiveCampusIds, amount);
//         } else if (hasCampusId && amount != null) {
//             // Single Campus (campusId) + Amount - use single campus method with entity_id =
//             // 4
//             System.out.println(
//                     "Filter: Single Campus (campusId) + Amount (campusId=" + campusId + ", amt=" + amount + ")");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(campusId, amount);
//         } else if (hasCampuses && amount != null) {
//             // Campuses (campusIds) + Amount - use entity_id = 3 ONLY for issued (DGM
//             // level), entity_id = 4 for sold
//             System.out.println("Filter: Campuses (campusIds) + Amount (camps=" + effectiveCampusIds + ", amt=" + amount
//                     + ") - Using entity_id = 3 ONLY for issued, entity_id = 4 for sold");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListAndAmountWithEntity4(effectiveCampusIds,
//                     amount);
//         } else if (zoneId != null && amount != null) {
//             System.out.println("Filter: Zone + Amount (zone=" + zoneId + ", amt=" + amount
//                     + ") - Using distinct series with entity_id = 2 ONLY for issued, entity_id = 4 for sold");
//             // Use distinct series counting to avoid double counting when same series
//             // appears in multiple entity_ids
//             // For zone: entity_id = 2 ONLY for issued (Zone level), entity_id = 4 for sold
//             // Admin→DGM and Admin→Campus distributions will be added separately to avoid
//             // double counting
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneAndAmountWithDistinct(zoneId, amount);
//         } else if (zoneId != null && hasCampusId) {
//             // Zone + Single Campus (campusId) - use single campus method with entity_id = 4
//             System.out.println(
//                     "Filter: Zone + Single Campus (campusId) (zone=" + zoneId + ", campusId=" + campusId + ")");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampus(zoneId, campusId);
//         } else if (zoneId != null && hasCampuses) {
//             // Zone + Campuses (campusIds) - use NEW method with entity_id = 4
//             System.out.println("Filter: Zone + Campuses (campusIds) (zone=" + zoneId + ", camps=" + effectiveCampusIds
//                     + ") - Using entity_id = 4");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListWithEntity4(zoneId,
//                     effectiveCampusIds);
//         } else if (hasCampusId) {
//             // Single Campus (campusId) only - use single campus method with entity_id = 4
//             System.out.println("Filter: Single Campus (campusId) (campusId=" + campusId + ")");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(campusId);
//         } else if (hasCampuses) {
//             // Campuses (campusIds) only - use entity_id = 3 (totalAppCount) for issued,
//             // entity_id = 4 for sold
//             System.out.println("Filter: Campuses (campusIds) (camps=" + effectiveCampusIds
//                     + ") - Using entity_id = 3 (totalAppCount) for issued, entity_id = 4 for sold");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListWithEntity4(effectiveCampusIds);
//         } else if (zoneId != null) {
//             System.out.println("Filter: Zone (zone=" + zoneId + ") - Using UserAppSold with entity_id = 2 only");
//             // Use UserAppSold for zone, but fix the query to match zone analytics
//             // Zone analytics uses: entity_id = 2 (Zone) + Admin→DGM + Admin→Campus
//             // distributions
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZone(zoneId);
//         } else if (amount != null && empId != null) {
//             // NEW LOGIC: Role-based series matching from Distribution to UserAppSold
//             // Get employee role and apply role-specific logic
//             List<SCEmployeeEntity> employeeList = scEmployeeRepository.findByEmpId(empId);
//             if (employeeList.isEmpty()) {
//                 System.out.println("⚠️ Employee " + empId + " not found, using default amount filter");
//                 rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//             } else {
//                 SCEmployeeEntity employee = employeeList.get(0);
//                 String role = employee.getEmpStudApplicationRole();
//                 if (role == null) {
//                     System.out.println("⚠️ Employee " + empId + " has null role, using default amount filter");
//                     rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//                 } else {
//                     String trimmedRole = role.trim().toUpperCase();
//                     System.out.println("Filter: Amount + Employee Series Match (amt=" + amount + ", empId=" + empId
//                             + ", role=" + trimmedRole + ")");

//                     if (trimmedRole.equals("ADMIN")) {
//                         // ADMIN: Get issued from Distribution table (total_app_count), sold/fast
//                         // sale/confirmed from AppStatusTrackView
//                         System.out.println(
//                                 "Using ADMIN logic: Issued from Distribution table (total_app_count), Sold/FastSale/Confirmed from AppStatusTrackView");

//                         // Get issued from Distribution table (total_app_count) filtered by created_by
//                         // and amount
//                         System.out.println("========================================");
//                         System.out.println("ADMIN DISTRIBUTION QUERY DEBUG");
//                         System.out.println("Employee ID: " + empId);
//                         System.out.println("Amount: " + amount);
//                         System.out.println("========================================");
//                         List<Object[]> distributionRows = distributionRepository
//                                 .getYearWiseTotalAppCountByCreatedByAndAmount(empId, amount);
//                         System.out.println("Distribution rows returned: " + distributionRows.size());

//                         // Get sold, fast sale, and confirmed from AppStatusTrackView
//                         // This query matches distributions created_by = empId with amount filter
//                         // Then checks applications in AppStatusTrackView within those distribution
//                         // ranges
//                         System.out.println(
//                                 "Getting sold/fast sale/confirmed from AppStatusTrackView for distributions created_by="
//                                         + empId + ", amount=" + amount);
//                         List<Object[]> statusRows = appStatusTrackViewRepository
//                                 .getYearWiseSoldFastSaleConfirmedByAdminAndAmount(empId, amount);
//                         System.out.println("Status rows returned: " + statusRows.size());

//                         // Merge: issued from Distribution, sold = sold + fast sale + confirmed from
//                         // AppStatusTrackView
//                         java.util.Map<Integer, long[]> mergedMap = new java.util.HashMap<>();

//                         // Add issued data from Distribution table
//                         for (Object[] row : distributionRows) {
//                             Integer yearId = (Integer) row[0];
//                             Long issued = row[1] != null ? ((Number) row[1]).longValue() : 0L;
//                             mergedMap.put(yearId, new long[] { issued, 0L }); // sold will be updated below
//                             System.out.println("Admin Year " + yearId
//                                     + ": Issued (from Distribution total_app_count with DISTINCT ranges)=" + issued);
//                         }
//                         System.out.println("========================================");

//                         // Add sold/confirmed data from AppStatusTrackView
//                         // Query returns: [yearId, sold_count, fast_sale_count, confirmed_count]
//                         // sold_count = "not confirmed" OR "fast sale"
//                         // fast_sale_count = "fast sale" (subset of sold_count)
//                         // confirmed_count = "confirmed"
//                         // Total sold = sold_count + confirmed_count (since fast_sale is already in
//                         // sold_count)
//                         // OR if we want explicit: sold (not confirmed only) + fast_sale + confirmed
//                         // Based on user request: sold + fast sale + confirmed
//                         // We'll use: sold_count (which includes fast sale) + confirmed_count
//                         // This ensures: (not confirmed + fast sale) + confirmed = all sold applications
//                         for (Object[] row : statusRows) {
//                             Integer yearId = (Integer) row[0];
//                             Long soldCount = row[1] != null ? ((Number) row[1]).longValue() : 0L; // Includes "not
//                                                                                                   // confirmed" + "fast
//                                                                                                   // sale"
//                             Long fastSaleCount = row[2] != null ? ((Number) row[2]).longValue() : 0L; // "fast sale"
//                                                                                                       // (subset of
//                                                                                                       // soldCount)
//                             Long confirmedCount = row[3] != null ? ((Number) row[3]).longValue() : 0L; // "confirmed"

//                             // Calculate total sold: sold_count (includes fast sale) + confirmed_count
//                             // This gives us: (not confirmed + fast sale) + confirmed = all sold
//                             // applications
//                             long totalSold = soldCount + confirmedCount;

//                             long[] data = mergedMap.getOrDefault(yearId, new long[] { 0L, 0L });
//                             data[1] = totalSold; // Update sold count
//                             mergedMap.put(yearId, data);

//                             System.out.println("Admin Year " + yearId + ": SoldCount=" + soldCount
//                                     + " (includes not confirmed + fast sale), FastSaleCount=" + fastSaleCount
//                                     + ", ConfirmedCount=" + confirmedCount + ", TotalSold=" + totalSold);
//                         }
//                         System.out.println("========================================");

//                         // Convert merged map back to rows format [yearId, issued, sold]
//                         rows = new java.util.ArrayList<>();
//                         for (java.util.Map.Entry<Integer, long[]> entry : mergedMap.entrySet()) {
//                             rows.add(new Object[] { entry.getKey(), entry.getValue()[0], entry.getValue()[1] });
//                         }
//                     } else if (trimmedRole.equals("ZONAL ACCOUNTANT")) {
//                         // ZONAL ACCOUNTANT: Use entity_id = 2 from UserAppSold, then add Admin→DGM and
//                         // Admin→Campus distributions
//                         List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
//                         if (zonalRecords == null || zonalRecords.isEmpty() || zonalRecords.get(0).getZone() == null) {
//                             System.out.println("⚠️ Zonal Accountant " + empId
//                                     + " not mapped to zone, using default amount filter");
//                             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//                         } else {
//                             Integer empZoneId = zonalRecords.get(0).getZone().getZoneId();
//                             System.out.println("Using ZONAL ACCOUNTANT logic: entity_id = 2 from UserAppSold (zone: "
//                                     + empZoneId + ", amount: " + amount + ")");
//                             System.out.println("Will add Admin→DGM and Admin→Campus distributions to issued count");
//                             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneAndAmountWithDistinct(empZoneId,
//                                     amount);
//                             // Store zoneId for later distribution addition
//                             zoneId = empZoneId;
//                         }
//                     } else if (trimmedRole.equals("DGM")) {
//                         // DGM: Use entity_id = 3 directly from UserAppSold, then add Admin→Campus and
//                         // Zone→Campus distributions
//                         List<Integer> dgmCampusIds = dgmRepository.findCampusIdsByEmployeeId(empId);
//                         if (dgmCampusIds == null || dgmCampusIds.isEmpty()) {
//                             System.out.println("⚠️ DGM " + empId + " has no campuses, using default amount filter");
//                             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//                         } else {
//                             System.out.println("Using DGM logic: entity_id = 3 from UserAppSold (campuses: "
//                                     + dgmCampusIds + ", amount: " + amount + ")");
//                             System.out.println("Will add Admin→Campus and Zone→Campus distributions to issued count");
//                             rows = userAppSoldRepository
//                                     .getYearWiseIssuedAndSoldByCampusListAndAmountWithEntity4(dgmCampusIds, amount);
//                             // Store DGM campusIds for later distribution addition
//                             effectiveCampusIds = dgmCampusIds;
//                             hasCampuses = true;
//                         }
//                     } else if (trimmedRole.equals("PRO") || trimmedRole.contains("PRINCIPAL")
//                             || trimmedRole.contains("VICE")) {
//                         // PRO/PRINCIPAL/VICE PRINCIPAL: Use entity_id = 4 directly from UserAppSold for
//                         // campus calculation
//                         // Uses campusId from employee record and entity_id = 4 for both issued
//                         // (totalAppCount) and sold
//                         int empCampusId = employee.getEmpCampusId();
//                         if (empCampusId <= 0) {
//                             System.out.println("⚠️ " + trimmedRole + " " + empId
//                                     + " has invalid campusId, using default amount filter");
//                             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//                         } else {
//                             System.out.println(
//                                     "Using " + trimmedRole + " logic: entity_id = 4 from UserAppSold (campusId: "
//                                             + empCampusId + ", amount: " + amount + ")");
//                             System.out.println(
//                                     "Query uses entity_id = 4 for campus calculation - totalAppCount for issued, sold for sold count");
//                             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(empCampusId, amount);
//                         }
//                     } else {
//                         // Unknown role, use default
//                         System.out.println("⚠️ Unknown role '" + trimmedRole + "', using default amount filter");
//                         rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//                     }
//                 }
//             }
//         } else if (amount != null) {
//             System.out.println("Filter: Amount (amt=" + amount + ")");
//             rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
//         } else {
//             System.out.println("Filter: All Time Aggregate");
//             // switch to AppStatusTrack
//             rows = appStatusTrackRepository.getYearWiseMetricsAllTime();
//         }

//         // Apply year filtering
//         System.out.println("Raw rows from DB before year filtering: " + rows.size());
//         rows = rows.stream()
//                 .filter(row -> yearIds.contains((Integer) row[0]))
//                 .collect(java.util.stream.Collectors.toList());
//         System.out.println("Rows after year filtering (for years: " + yearIds + "): " + rows.size());

//         // Get AcademicYear entities for year labels
//         List<AcademicYear> academicYears = academicYearRepository.findByAcdcYearIdIn(yearIds);
//         java.util.Map<Integer, AcademicYear> yearMap = academicYears.stream()
//                 .collect(java.util.stream.Collectors.toMap(AcademicYear::getAcdcYearId, y -> y));
//         // Log the raw data retrieved from database
//         if (campusIds != null && campusIds.size() > 1) {
//             System.out.println("=== MULTIPLE CAMPUSES DATA CALCULATION ===");
//             System.out.println("Campuses being aggregated: " + campusIds);
//             System.out.println("Amount filter: " + (amount != null ? amount : "None"));
//             System.out.println("Zone filter: " + (zoneId != null ? zoneId : "None"));
//             System.out.println("Year IDs being queried: " + yearIds);
//             System.out.println("Total rows retrieved from aggregated query: " + rows.size());
//             if (rows.isEmpty()) {
//                 System.out.println("⚠️ WARNING: Aggregated query returned NO DATA for campuses: " + campusIds);
//                 System.out.println("This could mean:");
//                 System.out.println(" 1. None of the campuses have data for the specified filters");
//                 System.out.println(" 2. The amount filter (" + amount + ") doesn't match any records");
//                 System.out.println(" 3. The data exists but not in the past 4 years");
//             } else {
//                 System.out.println("Raw aggregated data from database (summed across all campuses):");
//                 for (Object[] row : rows) {
//                     Integer yearId = (Integer) row[0];
//                     Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
//                     Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
//                     System.out.println(
//                             " Year ID: " + yearId + " | Issued (totalAppCount): " + totalAppCount + " | Sold: " + sold);
//                 }
//             }
//         }
//         // Create a map of yearId -> [totalAppCount, sold] for quick lookup
//         java.util.Map<Integer, long[]> yearDataMap = new java.util.HashMap<>();
//         for (Object[] row : rows) {
//             Integer yearId = (Integer) row[0];
//             Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
//             Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
//             yearDataMap.put(yearId, new long[] { totalAppCount, sold });
//         }

//         // If zoneId is present, add Admin→DGM and Admin→Campus distributions to issued
//         // count for each year
//         // UserAppSold query returns entity_id = 2 (Zone) with totalAppCount (NOT
//         // totalAppCount - appAvlbCount)
//         // IMPORTANT: When amount filter is present (any value, including 0), use ONLY
//         // UserAppSold data
//         // Do NOT add distributions because distributions don't have amount info
//         if (zoneId != null && amount == null) {
//             // Only add distributions when NO amount filter is present
//             for (Integer yearId : yearIds) {
//                 // Get distribution counts for this year (Admin→DGM and Admin→Campus)
//                 Integer adminToDgmDist = distributionRepository.sumAdminToDgmDistributionByZoneAndYear(zoneId, yearId)
//                         .orElse(0);
//                 Integer adminToCampusDist = distributionRepository
//                         .sumAdminToCampusDistributionByZoneAndYear(zoneId, yearId).orElse(0);

//                 // Add distributions to issued count (UserAppSold query returns entity_id = 2
//                 // with totalAppCount, so we add distributions)
//                 long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
//                 long updatedIssued = data[0] + adminToDgmDist + adminToCampusDist;
//                 yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

//                 if (adminToDgmDist > 0 || adminToCampusDist > 0) {
//                     System.out.println("Zone " + zoneId + " Year " + yearId
//                             + ": UserAppSold (entity_id=2) totalAppCount=" + data[0] +
//                             ", Added Admin→DGM: " + adminToDgmDist + ", Admin→Campus: " + adminToCampusDist +
//                             ", Total issued=" + updatedIssued);
//                 }
//             }
//         } else if (zoneId != null && amount != null) {
//             // When amount filter is present, add Admin→DGM and Admin→Campus distributions
//             // to issued count
//             // This applies to both: (1) zoneId parameter passed directly, and (2) ZONAL
//             // ACCOUNTANT role with empId
//             // UserAppSold query returns entity_id = 2 ONLY (Zone level) with totalAppCount,
//             // so we add Admin→DGM and Admin→Campus distributions
//             // This avoids double counting because entity_id=3 (DGM) is excluded from
//             // UserAppSold
//             System.out.println("Adding Admin→DGM and Admin→Campus distributions for zoneId: " + zoneId
//                     + " with amount filter: " + amount);
//             for (Integer yearId : yearIds) {
//                 // Get distribution counts for this year (Admin→DGM and Admin→Campus) with
//                 // amount filter
//                 // Uses DISTINCT on ranges to avoid duplicate counting within Distribution table
//                 Integer adminToDgmDist = distributionRepository
//                         .sumAdminToDgmDistributionByZoneYearAndAmount(zoneId, yearId, amount).orElse(0);
//                 Integer adminToCampusDist = distributionRepository
//                         .sumAdminToCampusDistributionByZoneYearAndAmount(zoneId, yearId, amount).orElse(0);

//                 // Add distributions to issued count
//                 long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
//                 long updatedIssued = data[0] + adminToDgmDist + adminToCampusDist;
//                 yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

//                 if (adminToDgmDist > 0 || adminToCampusDist > 0) {
//                     System.out.println("Zone " + zoneId + " Year " + yearId + " Amount " + amount
//                             + ": UserAppSold (entity_id=2 ONLY) totalAppCount=" + data[0] +
//                             ", Added Admin→DGM: " + adminToDgmDist + ", Admin→Campus: " + adminToCampusDist +
//                             ", Total issued=" + updatedIssued);
//                 }
//             }
//         }

//         // If campusIds is present (DGM rollup), add Admin→Campus and Zone→Campus
//         // distributions to issued count for each year
//         // NOTE: Only distributions with issued_to_type_id = 4 (Campus/PRO) are added to
//         // issued count
//         // Admin→DGM (issued_to_type_id = 3) is NOT included in issued count
//         // IMPORTANT: When amount filter is present, use ONLY UserAppSold data, do NOT
//         // add distributions
//         if (hasCampuses && effectiveCampusIds != null && !effectiveCampusIds.isEmpty() && amount == null) {
//             // Only add distributions when NO amount filter is present
//             for (Integer yearId : yearIds) {
//                 // Get Admin→Campus distribution (filtered by campusIds, issued_to_type_id = 4)
//                 Integer adminToCampusDist = distributionRepository
//                         .sumAdminToCampusDistributionByCampusIdsAndYear(effectiveCampusIds, yearId).orElse(0);

//                 // Get Zone→Campus distribution (filtered by campusIds, issued_to_type_id = 4)
//                 Integer zoneToCampusDist = distributionRepository
//                         .sumZoneToCampusDistributionByCampusIdsAndYear(effectiveCampusIds, yearId).orElse(0);

//                 // Add ONLY issued_to_type_id = 4 distributions to issued count (Admin→Campus +
//                 // Zone→Campus)
//                 long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
//                 long issuedDistCount = adminToCampusDist + zoneToCampusDist; // Only issued_to_type_id = 4
//                 long updatedIssued = data[0] + issuedDistCount;
//                 yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

//                 if (issuedDistCount > 0) {
//                     System.out.println("CampusIds " + effectiveCampusIds + " Year " + yearId
//                             + ": UserAppSold totalAppCount (entity_id=3)=" + data[0] +
//                             ", Added Admin→Campus: " + adminToCampusDist + ", Zone→Campus: " + zoneToCampusDist +
//                             " (issued_to_type_id = 4 only, Total issued: " + updatedIssued + ")");
//                 }
//             }
//         } else if (hasCampuses && effectiveCampusIds != null && !effectiveCampusIds.isEmpty() && amount != null) {
//             // When amount filter is present, add Admin→Campus and Zone→Campus distributions
//             // to issued count
//             // This applies to both: (1) campusIds parameter passed directly, and (2) DGM
//             // role with empId
//             // UserAppSold query returns entity_id = 3 ONLY (DGM level) with totalAppCount,
//             // so we add Admin→Campus and Zone→Campus distributions
//             // This avoids double counting because entity_id=1 (Admin) and entity_id=4
//             // (Campus) are excluded from UserAppSold
//             System.out.println("Adding Admin→Campus and Zone→Campus distributions for campusIds: " + effectiveCampusIds
//                     + " with amount filter: " + amount);
//             for (Integer yearId : yearIds) {
//                 // Get distribution counts for this year (Admin→Campus and Zone→Campus) with
//                 // amount filter
//                 // Uses DISTINCT on ranges to avoid duplicate counting within Distribution table
//                 Integer adminToCampusDist = distributionRepository
//                         .sumAdminToCampusDistributionByCampusIdsYearAndAmount(effectiveCampusIds, yearId, amount)
//                         .orElse(0);
//                 Integer zoneToCampusDist = distributionRepository
//                         .sumZoneToCampusDistributionByCampusIdsYearAndAmount(effectiveCampusIds, yearId, amount)
//                         .orElse(0);

//                 // Add distributions to issued count
//                 long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
//                 long updatedIssued = data[0] + adminToCampusDist + zoneToCampusDist;
//                 yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

//                 if (adminToCampusDist > 0 || zoneToCampusDist > 0) {
//                     System.out.println("CampusIds " + effectiveCampusIds + " Year " + yearId + " Amount " + amount
//                             + ": UserAppSold (entity_id=3 ONLY) totalAppCount=" + data[0] +
//                             ", Added Admin→Campus: " + adminToCampusDist + ", Zone→Campus: " + zoneToCampusDist +
//                             ", Total issued=" + updatedIssued);
//                 }
//             }
//         }
//         // Log aggregated data summary
//         if (campusIds != null && campusIds.size() > 1) {
//             System.out.println("Aggregated data by year (summed across all campuses):");
//             for (Integer yearId : yearIds) {
//                 long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
//                 System.out.println(" Year ID: " + yearId + " | Total Issued: " + data[0] + " | Total Sold: " + data[1]);
//             }
//         }
//         // Build GraphBarDTO list for all 4 years (always return 4 years)
//         List<GraphBarDTO> barList = new ArrayList<>();
//         for (Integer yearId : yearIds) {
//             long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
//             long issuedCount = data[0]; // totalAppCount from table
//             long soldCount = data[1]; // sold from table
//             AcademicYear year = yearMap.get(yearId);
//             String yearLabel = year != null ? year.getAcademicYear() : "Year " + yearId;
//             // Calculate percentages
//             int issuedPercent;
//             int soldPercent;
//             // If data is missing (issuedCount = 0), both percentages are 0
//             // If data exists (issuedCount > 0), issuedPercent is 100% (baseline) and
//             // calculate sold percentage
//             if (issuedCount > 0) {
//                 issuedPercent = 100; // 100% as baseline when data exists
//                 soldPercent = (int) Math.round((soldCount * 100.0) / issuedCount);
//             } else {
//                 // No data exists - both percentages are 0
//                 issuedPercent = 0;
//                 soldPercent = 0;
//             }
//             // Log calculation details for multiple campuses
//             if (campusIds != null && campusIds.size() > 1) {
//                 System.out.println("Year: " + yearLabel + " | Issued: " + issuedCount + " | Sold: " + soldCount +
//                         " | Issued %: " + issuedPercent + " | Sold %: " + soldPercent);
//             }
//             GraphBarDTO dto = new GraphBarDTO();
//             dto.setYear(yearLabel);
//             dto.setIssuedPercent(issuedPercent);
//             dto.setSoldPercent(soldPercent);
//             dto.setIssuedCount((int) issuedCount);
//             dto.setSoldCount((int) soldCount);
//             barList.add(dto);
//         }
//         if (campusIds != null && campusIds.size() > 1) {
//             System.out.println("=== END MULTIPLE CAMPUSES DATA CALCULATION ===");
//         }
//         return barList;
//     }

//     /**
//      * Get all campuses with optional category filter (school/college)
//      * 
//      * @param category Optional category filter: "school" or "college"
//      * @return List of GenericDropdownDTO containing campus ID and name
//      */
//     public List<GenericDropdownDTO> getAllCampuses(String category) {
//         // Get all active campuses
//         List<GenericDropdownDTO> allCampuses = campusRepository.findAllActiveCampusesForDropdown();
//         // If no category provided, return all campuses
//         if (category == null || category.trim().isEmpty()) {
//             return allCampuses;
//         }
//         // Filter by category (case-insensitive)
//         String cat = category.trim().toLowerCase();
//         return allCampuses.stream()
//                 .filter(campus -> {
//                     // Get campus entity to check business type
//                     Campus campusEntity = campusRepository.findById(campus.getId()).orElse(null);
//                     if (campusEntity == null || campusEntity.getBusinessType() == null) {
//                         return false;
//                     }
//                     String businessTypeName = campusEntity.getBusinessType().getBusinessTypeName().toLowerCase();
//                     // Match category
//                     if (cat.equals("school")) {
//                         return businessTypeName.contains("school");
//                     } else if (cat.equals("college")) {
//                         return businessTypeName.contains("college");
//                     }
//                     // If category doesn't match known types, return true (no filter)
//                     return true;
//                 })
//                 .collect(Collectors.toList());
//     }

//     /**
//      * Get all zones with optional category filter (school/college)
//      * 
//      * @param category Optional category filter: "school" or "college"
//      * @return List of GenericDropdownDTO containing zone ID and name
//      */
//     public List<GenericDropdownDTO> getAllZones(String category) {
//         // Get all zones
//         List<GenericDropdownDTO> allZones = zoneRepository.findAllActiveZonesForDropdown();
//         // If no category provided, return all zones
//         if (category == null || category.trim().isEmpty()) {
//             return allZones;
//         }
//         // Filter by category (case-insensitive)
//         // A zone is included if it has at least one campus matching the category
//         String cat = category.trim().toLowerCase();
//         return allZones.stream()
//                 .filter(zone -> {
//                     // Get zone entity to check campuses
//                     Zone zoneEntity = zoneRepository.findById(zone.getId()).orElse(null);
//                     if (zoneEntity == null) {
//                         return false;
//                     }
//                     // Get all campuses for this zone
//                     List<Campus> campuses = campusRepository.findByZoneZoneId(zoneEntity.getZoneId());
//                     // Check if any campus matches the category
//                     return campuses.stream()
//                             .anyMatch(campus -> {
//                                 if (campus.getBusinessType() == null) {
//                                     return false;
//                                 }
//                                 String businessTypeName = campus.getBusinessType()
//                                         .getBusinessTypeName().toLowerCase();
//                                 // Case-insensitive category matching
//                                 if (cat.equals("school")) {
//                                     return businessTypeName.contains("school");
//                                 } else if (cat.equals("college")) {
//                                     return businessTypeName.contains("college");
//                                 }
//                                 // If category doesn't match known types, return true (no filter)
//                                 return true;
//                             });
//                 })
//                 .collect(Collectors.toList());
//     }

//     /**
//      * Get all DGM employees with optional category filter (school/college)
//      * 
//      * @param category Optional category filter: "school" or "college"
//      * @return List of GenericDropdownDTO_Dgm containing employee ID, name, and
//      *         associated campus IDs
//      */
//     public List<GenericDropdownDTO_Dgm> getAllDgmEmployees(String category) {
//         // Get all DGM employees with their campus IDs
//         List<Object[]> rows = dgmRepository.findAllDgmEmployeesWithCampusId();
//         if (rows == null || rows.isEmpty()) {
//             return new ArrayList<>();
//         }

//         // Map to group by Employee ID
//         java.util.Map<Integer, GenericDropdownDTO_Dgm> groupedMap = new java.util.LinkedHashMap<>();

//         for (Object[] row : rows) {
//             Integer id = (Integer) row[0];
//             String name = (String) row[1];
//             Integer campusId = (Integer) row[2];

//             // Get existing DTO or create new
//             GenericDropdownDTO_Dgm dto = groupedMap.get(id);
//             if (dto == null) {
//                 dto = new GenericDropdownDTO_Dgm();
//                 dto.setId(id);
//                 dto.setName(name);
//                 dto.setCmpsId(new ArrayList<>()); // Initialize the list
//                 groupedMap.put(id, dto);
//             }

//             // Add campusId to the list if not already present
//             if (campusId != null && !dto.getCmpsId().contains(campusId)) {
//                 dto.getCmpsId().add(campusId);
//             }
//         }

//         List<GenericDropdownDTO_Dgm> allDgmEmployees = new ArrayList<>(groupedMap.values());
//         // If no category provided, return all DGM employees
//         if (category == null || category.trim().isEmpty()) {
//             return allDgmEmployees;
//         }
//         // Filter by category (case-insensitive)
//         // A DGM employee is included if they have at least one campus matching the
//         // category
//         String cat = category.trim().toLowerCase();
//         return allDgmEmployees.stream()
//                 .filter(dgm -> {
//                     // Check if any of the DGM's campuses match the category
//                     return dgm.getCmpsId().stream()
//                             .anyMatch(campusId -> {
//                                 Campus campus = campusRepository.findById(campusId).orElse(null);
//                                 if (campus == null || campus.getBusinessType() == null) {
//                                     return false;
//                                 }
//                                 String businessTypeName = campus.getBusinessType()
//                                         .getBusinessTypeName().toLowerCase();
//                                 // Case-insensitive category matching
//                                 if (cat.equals("school")) {
//                                     return businessTypeName.contains("school");
//                                 } else if (cat.equals("college")) {
//                                     return businessTypeName.contains("college");
//                                 }
//                                 // If category doesn't match known types, return true (no filter)
//                                 return true;
//                             });
//                 })
//                 .collect(Collectors.toList());
//     }

//     /**
//      * Get Current, Next, and Previous Two Academic Years
//      * Returns academic year ID and academic year string for each
//      * Current year is determined by finding academic year where year field =
//      * current calendar year - 1
//      * (e.g., if calendar year is 2026, current academic year is "2025-26" where
//      * year=2025)
//      * Then uses that academic year's year field to calculate next and previous
//      * years
//      * All queries filter by is_active = 1
//      * 
//      * @return AcademicYearInfoDTO containing current, next, and previous two
//      *         academic years
//      */
//     public AcademicYearInfoDTO getAcademicYearInfo() {
//         // Get current calendar year (e.g., 2026)
//         int currentCalendarYear = java.time.Year.now().getValue();
//         // Current academic year is typically the one that started in the previous
//         // calendar year
//         // (e.g., if calendar year is 2026, current academic year is "2025-26" where
//         // year=2025)
//         int currentAcademicYearValue = currentCalendarYear - 1;
//         System.out.println("========================================");
//         System.out.println("ACADEMIC YEAR INFO CALCULATION");
//         System.out.println("Current Calendar Year: " + currentCalendarYear);
//         System.out.println(
//                 "Finding academic year where year field = " + currentAcademicYearValue + " (calendar year - 1)");
//         System.out.println("========================================");

//         // Find academic year where year field = current calendar year - 1 (filtered by
//         // is_active = 1)
//         // This gives us the current academic year (e.g., if calendar is 2026, year=2025
//         // → "2025-26")
//         Optional<AcademicYear> currentYearEntityOpt = academicYearRepository
//                 .findByYearAndIsActive(currentAcademicYearValue, 1);

//         AcademicYearDTO currentYear;
//         AcademicYearDTO nextYear;
//         AcademicYearDTO previousYear;

//         if (currentYearEntityOpt.isPresent()) {
//             AcademicYear currentYearEntity = currentYearEntityOpt.get();
//             currentYear = new AcademicYearDTO(
//                     currentYearEntity.getAcdcYearId(),
//                     currentYearEntity.getAcademicYear());
//             System.out.println("Current Academic Year Found: acdcYearId=" + currentYearEntity.getAcdcYearId() +
//                     ", year=" + currentYearEntity.getYear() +
//                     ", academicYear=" + currentYearEntity.getAcademicYear());

//             // Next year: current academic year's year value + 1 (filtered by is_active = 1)
//             Integer nextYearValue = currentYearEntity.getYear() + 1;
//             Optional<AcademicYear> nextYearEntityOpt = academicYearRepository.findByYearAndIsActive(nextYearValue, 1);
//             nextYear = nextYearEntityOpt.isPresent()
//                     ? new AcademicYearDTO(nextYearEntityOpt.get().getAcdcYearId(),
//                             nextYearEntityOpt.get().getAcademicYear())
//                     : new AcademicYearDTO(null, null);
//             if (nextYearEntityOpt.isPresent()) {
//                 System.out.println("Next Year Found: acdcYearId=" + nextYearEntityOpt.get().getAcdcYearId() +
//                         ", year=" + nextYearEntityOpt.get().getYear() +
//                         ", academicYear=" + nextYearEntityOpt.get().getAcademicYear());
//             } else {
//                 System.out.println("Next Year NOT Found for year=" + nextYearValue);
//             }

//             // Previous year: current academic year's year value - 1 (filtered by is_active
//             // = 1)
//             Integer previousYearValue = currentYearEntity.getYear() - 1;
//             Optional<AcademicYear> previousYearEntityOpt = academicYearRepository
//                     .findByYearAndIsActive(previousYearValue, 1);
//             previousYear = previousYearEntityOpt.isPresent()
//                     ? new AcademicYearDTO(previousYearEntityOpt.get().getAcdcYearId(),
//                             previousYearEntityOpt.get().getAcademicYear())
//                     : new AcademicYearDTO(null, null);
//             if (previousYearEntityOpt.isPresent()) {
//                 System.out.println("Previous Year Found: acdcYearId=" + previousYearEntityOpt.get().getAcdcYearId() +
//                         ", year=" + previousYearEntityOpt.get().getYear() +
//                         ", academicYear=" + previousYearEntityOpt.get().getAcademicYear());
//             } else {
//                 System.out.println("Previous Year NOT Found for year=" + previousYearValue);
//             }
//         } else {
//             // If current year not found, return empty DTOs
//             System.out.println("Current Academic Year NOT Found for calendar year=" + currentCalendarYear);
//             return new AcademicYearInfoDTO(
//                     new AcademicYearDTO(null, null),
//                     new AcademicYearDTO(null, null),
//                     new AcademicYearDTO(null, null));
//         }

//         System.out.println("========================================");

//         return new AcademicYearInfoDTO(currentYear, nextYear, previousYear);
//     }

//     /**
//      * Get distinct app_amount values from AdminApp table
//      * 
//      * @return List of distinct app_amount values (sorted in ascending order)
//      */
//     public List<Double> getDistinctAppAmounts() {
//         try {
//             List<Double> distinctAmounts = adminAppRepository.findDistinctAppAmounts();
//             System.out.println("Distinct App Amounts Found: " + distinctAmounts.size());
//             if (!distinctAmounts.isEmpty()) {
//                 System.out.println("Amounts: " + distinctAmounts);
//             }
//             return distinctAmounts;
//         } catch (Exception e) {
//             System.err.println("Error in getDistinctAppAmounts: " + e.getMessage());
//             e.printStackTrace();
//             return new ArrayList<>();
//         }
//     }
// }
package com.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import com.application.repository.AppStatusTrackViewRepository;
import com.application.entity.Zone;
import com.application.dto.GenericDropdownDTO_Dgm;
import com.application.dto.GenericDropdownDTO;
import com.application.dto.AcademicYearDTO;
import com.application.dto.AcademicYearInfoDTO;
import com.application.repository.AdminAppRepository;

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
    @Autowired
    private AppStatusTrackViewRepository appStatusTrackViewRepository;
    @Autowired
    private AdminAppRepository adminAppRepository;

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
        // 2. Route based on Role - Use same methods as specific endpoints
        String trimmedRole = role.trim();
        System.out.println("Routing to analytics method for role: " + trimmedRole);
        CombinedAnalyticsDTO analytics;
        if (trimmedRole.equalsIgnoreCase("DGM")) {
            // Use same method as /api/analytics/dgm_employee/{id}
            System.out.println("Calling getEmployeeAnalytics (same as /dgm_employee/{id})");
            analytics = getEmployeeAnalytics((long) empId);
            // Set role and designation info
            analytics.setRole("DGM");
            analytics.setDesignationName(designation);
            // Get campus info for entity name
            List<Integer> campusIds = dgmRepository.findCampusIdsByEmployeeId(empId);
            if (!campusIds.isEmpty()) {
                analytics.setEntityName(campusIds.size() + " Campuses Managed");
            } else {
                analytics.setEntityName("DGM");
            }
            analytics.setEntityId(empId);
            return analytics;
        } else if (trimmedRole.equalsIgnoreCase("ZONAL ACCOUNTANT")) {
            // Get Zone ID and use same method as /api/analytics/zone/{id}
            List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
            if (zonalRecords == null || zonalRecords.isEmpty()) {
                System.out.println("❌ ERROR: No ZonalAccountant records found for employee " + empId);
                return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone", designation);
            }
            ZonalAccountant zonalRecord = zonalRecords.get(0);
            if (zonalRecord.getZone() == null) {
                return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone", designation);
            }
            Long zoneId = (long) zonalRecord.getZone().getZoneId();
            String zoneName = zonalRecord.getZone().getZoneName();
            System.out.println("Calling getZoneAnalytics (same as /zone/{id}) for zoneId: " + zoneId);
            analytics = getZoneAnalytics(zoneId);
            // Set role and designation info
            analytics.setRole("Zonal Accountant");
            analytics.setDesignationName(designation);
            analytics.setEntityName(zoneName != null ? zoneName : "Zone " + zoneId);
            analytics.setEntityId(zoneId.intValue());
            return analytics;
        } else {
            // For PRO and other roles, get campus ID and use EXACT SAME method as
            // /api/analytics/campus/{id}
            // This ensures PRO role uses the same calculation logic as campus endpoint
            int campusId = employee.getEmpCampusId();
            if (campusId <= 0) {
                System.out.println("❌ ERROR: Employee " + empId + " has invalid campusId: " + campusId);
                return createEmptyAnalytics(role != null ? role : "Unknown", empId, "Employee not mapped to a Campus",
                        designation);
            }
            String campusName = employee.getCampusName();
            System.out.println("========================================");
            System.out.println("📊 PRO ROLE - Using SAME logic as /api/analytics/campus/{id}");
            System.out.println("========================================");
            System.out.println("Employee ID: " + empId);
            System.out.println("Campus ID: " + campusId);
            System.out.println("Campus Name: " + campusName);
            System.out.println("Method: getCampusAnalytics (EXACT SAME as /campus/{id})");
            System.out.println("Uses: AppStatusTrack with app_issued_type_id = 4");
            System.out.println("========================================");
            analytics = getCampusAnalytics((long) campusId, empId);
            // Set role and designation info
            analytics.setRole(role != null ? role : "PRO");
            analytics.setDesignationName(designation);
            analytics.setEntityName(campusName != null ? campusName : "Campus " + campusId);
            analytics.setEntityId(campusId);
            return analytics;
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
            return createEmptyAnalytics("Null Role", empId, "Employee has no role", designation); // <--- PASS
                                                                                                  // DESIGNATION
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
            analytics = getCampusAnalytics((long) campusId, empId);
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
        // System.out.println("📊 Getting Graph Data for Zone (Aligned with Cards): " +
        // zoneIdInt);
        // Use UserAppSold for graph data: entity_id = 2 for issued, entity_id = 4 for
        // sold
        // Filter future years before March 1st for /api/analytics/{empId}
        analytics.setGraphData(getGraphData(
                (yearId) -> {
                    // Get issued from UserAppSold with entity_id = 2 (Zone)
                    Optional<GraphSoldSummaryDTO> userAppSoldData = userAppSoldRepository
                            .getSalesSummaryByZoneId(zoneIdInt, yearId);
                    if (userAppSoldData.isEmpty()) {
                        return Optional.of(new GraphSoldSummaryDTO(0L, 0L));
                    }

                    long issuedFromUserAppSold = userAppSoldData.get().totalApplications();
                    long soldFromUserAppSold = userAppSoldData.get().totalSold();

                    // Add Admin→DGM and Admin→Campus distributions to issued count
                    Integer adminToDgmDist = distributionRepository
                            .sumAdminToDgmDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                    Integer adminToCampusDist = distributionRepository
                            .sumAdminToCampusDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);

                    long totalIssued = issuedFromUserAppSold + adminToDgmDist + adminToCampusDist;

                    System.out.println("Zone " + zoneIdInt + " Year " + yearId
                            + ": UserAppSold totalAppCount (entity_id=2)=" + issuedFromUserAppSold +
                            ", Sold (entity_id=4)=" + soldFromUserAppSold +
                            ", Admin→DGM=" + adminToDgmDist + ", Admin→Campus=" + adminToCampusDist +
                            ", Total issued=" + totalIssued);

                    return Optional.of(new GraphSoldSummaryDTO(
                            totalIssued,
                            soldFromUserAppSold // Sold from entity_id = 4
                    ));
                },
                () -> userAppSoldRepository.findDistinctYearIdsByZoneId(zoneIdInt),
                true)); // Filter future years before March 1st

        // 2. Metrics Data
        // System.out.println("📈 Getting Metrics Data for Zone: " + zoneIdInt);
        // System.out.println("🔍 Checking years from AppStatusTrack (filtered by
        // is_active = 1)...");
        // List<Integer> years =
        // appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt);
        // System.out.println("✅ Found " + years.size() + " year(s) for Zone " +
        // zoneIdInt + ": " + years);

        analytics.setMetricsData(
                getMetricsData(
                        (yearId) -> {
                            // System.out.println("🔍 getMetricsByZoneIdAndYear called - Zone: " + zoneIdInt
                            // + ", Year: " + yearId);
                            // Get AppStatusTrack metrics (covers Zone to DGM, DGM to Campus, Zone to
                            // Campus)
                            // AppStatusTrack already includes all flows with app_issued_type_id IN (2, 3,
                            // 4)
                            // This already captures: Zone→DGM, DGM→Campus, Zone→Campus flows
                            MetricsAggregateDTO statusMetrics = appStatusTrackRepository
                                    .getMetricsByZoneIdAndYear(zoneIdInt, yearId)
                                    .orElse(new MetricsAggregateDTO());

                            // Get issued breakdown by app_issued_type_id
                            List<Object[]> issuedBreakdown = appStatusTrackRepository
                                    .getIssuedBreakdownByZoneIdAndYear(zoneIdInt, yearId);

                            // Get distribution counts
                            Integer adminToZoneDist = distributionRepository
                                    .sumAdminToZoneDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                            Integer adminToDgmDist = distributionRepository
                                    .sumAdminToDgmDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);
                            Integer adminToCampusDist = distributionRepository
                                    .sumAdminToCampusDistributionByZoneAndYear(zoneIdInt, yearId).orElse(0);

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
                            System.out.println(
                                    "   totalApp (app_issued_type_id = 2 - Zone): " + statusMetrics.totalApp());
                            System.out.println(
                                    "   appIssued (app_issued_type_id = 2 - Zone): " + statusMetrics.appIssued());
                            System.out.println("----------------------------------------");
                            System.out.println("📦 DISTRIBUTION TABLE COUNTS (Direct Admin Distributions):");
                            System.out.println(
                                    "   Admin → Zone: " + adminToZoneDist + " (NOT added - already in AppStatusTrack)");
                            System.out.println(
                                    "   Admin → DGM (Direct): " + adminToDgmDist + " (ADDED to totalApp and issued)");
                            System.out.println("   Admin → Campus (Direct): " + adminToCampusDist
                                    + " (ADDED to totalApp and issued)");
                            System.out.println("----------------------------------------");

                            // Add Admin→DGM and Admin→Campus distributions to issued and totalApp
                            // Note: Admin→Zone is NOT added because it's already reflected in
                            // AppStatusTrack Type 2
                            long totalIssued = statusMetrics.appIssued() + adminToDgmDist + adminToCampusDist;
                            long totalApp = statusMetrics.totalApp() + adminToDgmDist + adminToCampusDist;

                            System.out.println("✅ FINAL ZONE CALCULATION:");
                            System.out.println("   Total Applications:");
                            System.out
                                    .println("     - From AppStatusTrack (Type 2 - Zone): " + statusMetrics.totalApp());
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
                            System.out
                                    .println("     - DGM's Admin→Campus (200) SHOULD be in Zone: " + adminToCampusDist);
                            System.out.println("     - Zone's own AppStatusTrack Type 2: " + statusMetrics.totalApp());
                            System.out.println("     - Expected Zone Total: " + statusMetrics.totalApp() + " + "
                                    + adminToDgmDist + " + " + adminToCampusDist + " = " + totalApp);
                            System.out.println("========================================");

                            // Return metrics with added distribution counts for totalApp
                            // But use app_available and app_issued directly from AppStatusTrack
                            // (app_issued_type_id = 2) without adding distributions
                            return Optional.of(new MetricsAggregateDTO(
                                    totalApp, // totalApp + distributions
                                    statusMetrics.appSold(),
                                    statusMetrics.appConfirmed(),
                                    statusMetrics.appAvailable(), // Use app_available from AppStatusTrack
                                                                  // (app_issued_type_id = 2)
                                    statusMetrics.appUnavailable(),
                                    statusMetrics.appDamaged(),
                                    statusMetrics.appIssued() // Use app_issued from AppStatusTrack (app_issued_type_id
                                                              // = 2), NOT totalIssued with distributions
                            ));
                        },
                        (yearId) -> {
                            // System.out.println("🔍 getProMetricByZoneId_FromStatus called - Zone: " +
                            // zoneIdInt + ", Year: " + yearId);
                            Optional<Long> result = appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneIdInt,
                                    yearId);
                            // System.out.println("✅ PRO Metric: " + (result.isPresent() ? result.get() :
                            // "Not found"));
                            return result;
                        },
                        () -> {
                            // System.out.println("🔍 findDistinctYearIdsByZoneId called for Zone: " +
                            // zoneIdInt);
                            List<Integer> yearList = appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneIdInt);
                            // System.out.println("✅ Years returned: " + yearList);
                            return yearList;
                        }));

        // System.out.println("========================================");
        return analytics;
    }

    public CombinedAnalyticsDTO getDgmAnalytics(Integer dgmEmpId) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        // Filter future years before March 1st for /api/analytics/{empId}
        analytics.setGraphData(getGraphData(
                (yearId) -> userAppSoldRepository.getSalesSummaryByDgm(dgmEmpId, yearId),
                () -> userAppSoldRepository.findDistinctYearIdsByDgm(dgmEmpId),
                true)); // Filter future years before March 1st
        analytics.setMetricsData(
                getMetricsData(
                        (yearId) -> appStatusTrackRepository.getMetricsByEmployeeAndYear(dgmEmpId, yearId),
                        (yearId) -> userAppSoldRepository.getProMetricByDgm(dgmEmpId, yearId),
                        () -> appStatusTrackRepository.findDistinctYearIdsByEmployee(dgmEmpId)));
        return analytics;
    }

    // In AnalyticsService.java
    public CombinedAnalyticsDTO getEmployeeAnalytics(Long empId) {
        Integer empIdInt = empId.intValue();
        System.out.println("========================================");
        System.out.println("📊 DGM EMPLOYEE ANALYTICS - AUTO FETCH CAMPUS IDs");
        System.out.println("========================================");
        System.out.println("Employee ID: " + empIdInt);

        // 1. Get all Campus IDs associated with this Employee in the sce_dgm table
        // (AUTOMATIC)
        // This is done automatically - no need to pass campusIds manually
        List<Integer> campusIds = dgmRepository.findCampusIdsByEmployeeId(empIdInt);
        System.out.println("✅ Campus IDs fetched from sce_dgm table: " + campusIds);

        if (campusIds.isEmpty()) {
            System.out.println("❌ ERROR: No active DGM records found for Employee ID: " + empId);
            throw new RuntimeException("No active DGM records found for Employee ID: " + empId);
        }

        // 2. Get Zone ID for this DGM employee
        Integer zoneId = dgmRepository.findZoneIdByEmpId(empIdInt).orElse(null);
        if (zoneId == null) {
            System.out.println("❌ ERROR: No zone found for Employee ID: " + empId);
            throw new RuntimeException("No zone found for Employee ID: " + empId);
        }
        System.out.println("✅ Zone ID: " + zoneId);

        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        // 3. Use UserAppSold for graph data: entity_id IN (1, 3) for issued, entity_id
        // = 4 for sold
        // Campus IDs are automatically fetched from sce_dgm table - no manual input
        // needed
        System.out.println("----------------------------------------");
        System.out.println("📊 GRAPH DATA - Using UserAppSold (entity_id IN (1,3) for issued, entity_id = 4 for sold)");
        System.out.println("Campus IDs from sce_dgm table: " + campusIds);
        System.out.println("Zone ID: " + zoneId);

        // Use UserAppSold for graph data - entity_id = 3 for issued (totalAppCount),
        // entity_id = 4 for sold
        // IMPORTANT: Ensure unique series - same series counted only once
        // Filter future years before March 1st for /api/analytics/{empId}
        analytics.setGraphData(getGraphData(
                (yearId) -> {
                    // Get issued and sold from UserAppSold with unique series (entity_id = 3 for
                    // issued, entity_id = 4 for sold)
                    // This ensures same series appears only once even if it exists in multiple
                    // entity_ids
                    List<Object[]> userAppSoldData = userAppSoldRepository
                            .getSalesSummaryByCampusListWithEntity4Raw(campusIds, yearId);
                    long issuedFromUserAppSold = 0L;
                    long soldFromUserAppSold = 0L;

                    if (!userAppSoldData.isEmpty() && userAppSoldData.get(0) != null) {
                        Object[] row = userAppSoldData.get(0);
                        issuedFromUserAppSold = ((Number) row[0]).longValue();
                        soldFromUserAppSold = ((Number) row[1]).longValue();
                    }

                    // Add distributions with issued_to_type_id = 4 to issued count
                    Integer adminToCampusDist = distributionRepository
                            .sumAdminToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                            .orElse(0);
                    Integer zoneToCampusDist = distributionRepository
                            .sumZoneToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                            .orElse(0);

                    long totalIssued = issuedFromUserAppSold + adminToCampusDist + zoneToCampusDist;

                    System.out.println("DGM Year " + yearId + ": UserAppSold totalAppCount (entity_id=3)="
                            + issuedFromUserAppSold +
                            ", Sold (entity_id=4)=" + soldFromUserAppSold +
                            ", Admin→Campus=" + adminToCampusDist + ", Zone→Campus=" + zoneToCampusDist +
                            ", Total issued=" + totalIssued);
                    return Optional.of(new GraphSoldSummaryDTO(totalIssued, soldFromUserAppSold));
                },
                () -> {
                    // Find distinct years from UserAppSold for campusIds
                    List<Integer> years = userAppSoldRepository.findDistinctYearIdsByCampusIds(campusIds);
                    System.out.println("✅ Returning " + years.size() + " years from UserAppSold: " + years);
                    return years;
                },
                true)); // Filter future years before March 1st
        System.out.println("========================================");
        // 4. Get metrics data with distribution count added to issued count
        analytics.setMetricsData(
                getMetricsData(
                        (yearId) -> {
                            // Get AppStatusTrack metrics with app_issued_type_id = 3 (DGM level: for
                            // appIssued, appAvailable, filtered by campusIds and zoneId)
                            // NOTE: totalApp from AppStatusTrack (Type 3) captures Zone-to-DGM and
                            // Admin-to-DGM flows
                            MetricsAggregateDTO statusMetrics = appStatusTrackRepository
                                    .getMetricsByCampusIdsAndYearForDgm(campusIds, zoneId, yearId)
                                    .orElse(new MetricsAggregateDTO());
                            // Get sold, confirmed, unavailable, damaged with app_issued_type_id = 4
                            // (Campus/PRO, filtered by campusIds and zoneId)
                            MetricsAggregateDTO proMetrics = appStatusTrackRepository
                                    .getSoldConfirmedUnavailableDamagedByCampusIdsAndYearForDgm(campusIds, zoneId,
                                            yearId)
                                    .orElse(new MetricsAggregateDTO());
                            // Get admin-to-campus distribution count (Admin→Campus: issued_by_type_id = 1,
                            // issued_to_type_id = 4, filtered by campusIds)
                            Integer adminToCampusDist = distributionRepository
                                    .sumAdminToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                                    .orElse(0);
                            // Get zone-to-campus distribution count (Zone→Campus: issued_by_type_id = 2,
                            // issued_to_type_id = 4, filtered by campusIds)
                            Integer zoneToCampusDist = distributionRepository
                                    .sumZoneToCampusDistributionByCampusIdsAndYear(campusIds, yearId)
                                    .orElse(0);

                            // Display detailed breakdown for DGM Available and Issued Calculation
                            System.out.println("========================================");
                            System.out.println("📊 DGM AVAILABLE & ISSUED CALCULATION");
                            System.out.println("========================================");
                            System.out.println("Employee ID: " + empIdInt);
                            System.out.println("Zone ID: " + zoneId);
                            System.out.println("Campus IDs: " + campusIds);
                            System.out.println("Academic Year ID: " + yearId);
                            System.out.println("----------------------------------------");
                            System.out.println(
                                    "📈 FROM AppStatusTrack (app_issued_type_id = 3, campus_id IN campusIds):");
                            System.out.println("   app_available: " + statusMetrics.appAvailable());
                            System.out.println("   app_issued: " + statusMetrics.appIssued());
                            System.out.println("   totalApp: " + statusMetrics.totalApp()
                                    + " (USED for final total - captures Zone-to-DGM & Admin-to-DGM flows)");
                            System.out.println("----------------------------------------");
                            System.out.println("📦 DISTRIBUTION TABLE COUNTS (for Direct Campus Distributions):");
                            System.out.println("   Admin → Campus: " + adminToCampusDist
                                    + " (issued_to_type_id = 4, ADDED to totalApp)");
                            System.out.println("   Zone → Campus: " + zoneToCampusDist
                                    + " (issued_to_type_id = 4, ADDED to totalApp)");
                            System.out.println("----------------------------------------");

                            // For DGM Flow (Zone-to-DGM & Admin-to-DGM): Use AppStatusTrack (Type 3)
                            // For Direct Campus Flow (Admin-to-Campus & Zone-to-Campus): Use Distribution
                            // table
                            long finalTotalApp = statusMetrics.totalApp() + adminToCampusDist + zoneToCampusDist;

                            System.out.println("✅ FINAL DGM CALCULATION:");
                            System.out.println("   Total Applications (Hybrid: AppStatusTrack Type 3 + Distribution):");
                            System.out.println("     - From AppStatusTrack (Type 3 - Zone-to-DGM & Admin-to-DGM): "
                                    + statusMetrics.totalApp());
                            System.out.println("     - Admin → Campus Distribution (Direct): " + adminToCampusDist
                                    + " (issued_to_type_id = 4, from Distribution table)");
                            System.out.println("     - Zone → Campus Distribution (Direct): " + zoneToCampusDist
                                    + " (issued_to_type_id = 4, from Distribution table)");
                            System.out.println("     - TOTAL APPLICATIONS: " + finalTotalApp);
                            System.out.println(
                                    "   Available (from AppStatusTrack, Type 3): " + statusMetrics.appAvailable());
                            System.out.println("   Issued (from AppStatusTrack, Type 3): " + statusMetrics.appIssued());
                            System.out.println("========================================");

                            return Optional.of(new MetricsAggregateDTO(
                                    finalTotalApp, // Hybrid: AppStatusTrack Type 3 (Zone-to-DGM & Admin-to-DGM) +
                                                   // Distribution table (Admin-to-Campus & Zone-to-Campus)
                                    proMetrics.appSold(), // From app_issued_type_id = 4
                                    proMetrics.appConfirmed(), // From app_issued_type_id = 4
                                    statusMetrics.appAvailable(), // From app_issued_type_id = 3, campus_id IN campusIds
                                                                  // (DIRECT from AppStatusTrack)
                                    proMetrics.appUnavailable(), // From app_issued_type_id = 4
                                    proMetrics.appDamaged(), // From app_issued_type_id = 4
                                    statusMetrics.appIssued() // From app_issued_type_id = 3, campus_id IN campusIds
                                                              // (DIRECT from AppStatusTrack, NO distributions added)
                            ));
                        },
                        (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
                        () -> {
                            // Combine years from AppStatusTrack (type 3) and Distribution table
                            // Since app_issued_type_id = 3 may not exist, Distribution table is the primary
                            // source
                            List<Integer> yearsFromAppStatusTrack = appStatusTrackRepository
                                    .findDistinctYearIdsByCampusIdsForDgm(campusIds, zoneId);
                            List<Integer> yearsFromDistribution = distributionRepository
                                    .findDistinctYearIdsForDgmCampuses(empIdInt, campusIds);

                            // Merge both lists and remove duplicates
                            Set<Integer> allYears = new HashSet<>();
                            allYears.addAll(yearsFromAppStatusTrack);
                            allYears.addAll(yearsFromDistribution);

                            List<Integer> combinedYears = new ArrayList<>(allYears);
                            System.out.println("📅 Years from AppStatusTrack (type 3): " + yearsFromAppStatusTrack);
                            System.out.println("📅 Years from Distribution table: " + yearsFromDistribution);
                            System.out.println("📅 Combined years: " + combinedYears);

                            return combinedYears;
                        }));
        return analytics;
    }

    public CombinedAnalyticsDTO getCampusAnalytics(Long campusId) {
        return getCampusAnalytics(campusId, null);
    }

    public CombinedAnalyticsDTO getCampusAnalytics(Long campusId, Integer proEmpId) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        Integer campusIdInt = campusId.intValue();

        System.out.println("========================================");
        System.out.println("📊 CAMPUS ANALYTICS");
        System.out.println("========================================");
        System.out.println("Campus ID: " + campusIdInt);
        if (proEmpId != null) {
            System.out.println("PRO Employee ID: " + proEmpId);
        }

        // Use UserAppSold for graph data: entity_id = 4 for both issued (totalAppCount)
        // and sold
        System.out.println("Using UserAppSold - entity_id = 4 for issued (totalAppCount) and sold");
        // Filter future years before March 1st for /api/analytics/{empId}
        analytics.setGraphData(getGraphData(
                (yearId) -> {
                    // Get data from UserAppSold with entity_id = 4 for the campus
                    Optional<GraphSoldSummaryDTO> userAppSoldData = userAppSoldRepository
                            .getSalesSummaryByCampusIdAndYear(campusIdInt, yearId);
                    if (userAppSoldData.isEmpty()) {
                        return Optional.of(new GraphSoldSummaryDTO(0L, 0L));
                    }

                    long issued = userAppSoldData.get().totalApplications(); // totalAppCount from entity_id = 4
                    long sold = userAppSoldData.get().totalSold(); // sold from entity_id = 4

                    System.out.println("Campus " + campusIdInt + " Year " + yearId
                            + ": Issued (totalAppCount, entity_id=4)=" + issued + ", Sold (entity_id=4)=" + sold);
                    return Optional.of(new GraphSoldSummaryDTO(issued, sold));
                },
                () -> {
                    // Find distinct years from UserAppSold (entity_id = 4)
                    return userAppSoldRepository.findDistinctYearIdsByCampusId(campusIdInt);
                },
                true)); // Filter future years before March 1st
        System.out.println("✅ Graph data created - using UserAppSold with entity_id = 4");
        System.out.println("========================================");

        // Metrics use AppStatusTrack with app_issued_type_id = 4
        // For PRO role: if PRO has distributed, calculate issued and available based on
        // distributions
        analytics.setMetricsData(
                getMetricsData(
                        (yearId) -> {
                            MetricsAggregateDTO metrics = appStatusTrackRepository
                                    .getMetricsByCampusAndYearWithType4(campusId, yearId)
                                    .orElse(new MetricsAggregateDTO());

                            // For direct campus endpoint (/api/analytics/campus/{id}): ALWAYS set available
                            // = 0, issued = 0
                            if (proEmpId == null) {
                                System.out.println("========================================");
                                System.out
                                        .println("📊 CAMPUS ANALYTICS - Direct Endpoint (/api/analytics/campus/{id})");
                                System.out.println("========================================");
                                System.out.println("Campus ID: " + campusIdInt);
                                System.out.println("Year: " + yearId);
                                System.out.println("Total App (from AppStatusTrack): " + metrics.totalApp());
                                System.out.println(
                                        "✅ Direct endpoint - FORCING available = 0, issued = 0 (in metrics card)");
                                System.out.println("========================================");

                                // ALWAYS return 0 for available and issued for direct campus endpoint
                                return Optional.of(new MetricsAggregateDTO(
                                        metrics.totalApp(), // totalApp (unchanged)
                                        metrics.appSold(), // appSold (unchanged)
                                        metrics.appConfirmed(), // appConfirmed (unchanged)
                                        0L, // appAvailable = 0 (FORCED for direct endpoint)
                                        metrics.appUnavailable(), // appUnavailable (unchanged)
                                        metrics.appDamaged(), // appDamaged (unchanged)
                                        0L // appIssued = 0 (FORCED for direct endpoint)
                                ));
                            }

                            // For PRO role login (proEmpId != null): check distributions
                            Integer proDistributed = distributionRepository
                                    .sumProDistributionByEmpIdAndYear(proEmpId, yearId)
                                    .orElse(0);

                            System.out.println("========================================");
                            System.out.println("📊 PRO DISTRIBUTION CHECK");
                            System.out.println("========================================");
                            System.out.println("PRO Employee ID: " + proEmpId);
                            System.out.println("Year: " + yearId);
                            System.out.println("Total App (from AppStatusTrack): " + metrics.totalApp());
                            System.out.println("PRO Distributed: " + proDistributed);

                            if (proDistributed > 0) {
                                // PRO has distributed: issued = distributed count, available = totalApp -
                                // distributed
                                long issued = (long) proDistributed;
                                long available = Math.max(0L, metrics.totalApp() - (long) proDistributed);

                                System.out.println("✅ PRO has distributed applications");
                                System.out.println("Issued (distributed): " + issued);
                                System.out.println("Available (totalApp - distributed): " + available);

                                // Return modified metrics with calculated issued and available
                                return Optional.of(new MetricsAggregateDTO(
                                        metrics.totalApp(), // totalApp (unchanged)
                                        metrics.appSold(), // appSold (unchanged)
                                        metrics.appConfirmed(), // appConfirmed (unchanged)
                                        available, // appAvailable (calculated)
                                        metrics.appUnavailable(), // appUnavailable (unchanged)
                                        metrics.appDamaged(), // appDamaged (unchanged)
                                        issued // appIssued (calculated from distributions)
                                ));
                            } else {
                                // PRO has NOT distributed: available = 0, issued = 0
                                System.out.println("ℹ️ PRO has NOT distributed - available = 0, issued = 0");
                                System.out.println("Issued: 0");
                                System.out.println("Available: 0");

                                return Optional.of(new MetricsAggregateDTO(
                                        metrics.totalApp(), // totalApp (unchanged)
                                        metrics.appSold(), // appSold (unchanged)
                                        metrics.appConfirmed(), // appConfirmed (unchanged)
                                        0L, // appAvailable = 0 (no distributions)
                                        metrics.appUnavailable(), // appUnavailable (unchanged)
                                        metrics.appDamaged(), // appDamaged (unchanged)
                                        0L // appIssued = 0 (no distributions)
                                ));
                            }
                        },
                        // Use AppStatusTrack repo with app_issued_type_id = 4
                        (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusIdInt, yearId),
                        () -> appStatusTrackRepository.findDistinctYearIdsByCampusWithType4(campusId)));
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
                () -> userAppSoldRepository.findDistinctYearIdsByZoneAndAmount(zoneId, amount));
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
                () -> userAppSoldRepository.findDistinctYearIdsByCampusAndAmount(campusId, amount));
    }

    // private CombinedAnalyticsDTO getDgmDirectAnalytics(SCEmployeeEntity employee)
    // {
    // int empId = employee.getEmpId();
    //
    // // 1. Fetch DGM Record to get Campus ID
    // // Assuming findByEmployee_EmpId returns List or Optional. Taking first for
    // safety.
    // Dgm dgmRecord = dgmRepository.lookupByEmpId(empId).orElse(null);
    //
    // if (dgmRecord == null || dgmRecord.getCampus() == null) {
    // return createEmptyAnalytics("DGM", empId, "DGM not mapped to a Campus",
    // employee.getDesignationName());
    // }
    //
    // int campusId = dgmRecord.getCampus().getCampusId(); // Pick Campus ID
    // String campusName = dgmRecord.getCampus().getCampusName(); // Assuming you
    // have name in Campus entity
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
        System.out.println("DEBUG: Graph data - yearlyData size: "
                + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
        System.out.println("DEBUG: Metrics data - metrics size: "
                + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
        // 3. Set Header Info
        analytics.setRole("DGM");
        analytics.setDesignationName(employee.getDesignationName());
        // For entity name, you can show a count or join names: "3 Campuses" or "Campus
        // A, Campus B..."
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
        // 1. Fetch ZonalAccountant Record to get Zone ID (handle multiple results by
        // taking first)
        List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
        System.out.println("DEBUG: Found " + (zonalRecords != null ? zonalRecords.size() : 0)
                + " ZonalAccountant records for employee " + empId);
        if (zonalRecords == null || zonalRecords.isEmpty()) {
            System.out.println("❌ ERROR: No ZonalAccountant records found for employee " + empId);
            return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone",
                    employee.getDesignationName());
        }
        // Get the first active record (most recent based on zone_acct_id DESC if
        // needed)
        ZonalAccountant zonalRecord = zonalRecords.get(0);
        if (zonalRecord.getZone() == null) {
            return createEmptyAnalytics("Zonal Accountant", empId, "Not mapped to a Zone",
                    employee.getDesignationName());
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
        System.out.println("DEBUG: Graph data - yearlyData size: "
                + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
        System.out.println("DEBUG: Metrics data - metrics size: "
                + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
        // 3. Set Header Info
        analytics.setRole("Zonal Accountant");
        analytics.setDesignationName(employee.getDesignationName());
        analytics.setEntityName(zoneName); // Showing Zone Name
        analytics.setEntityId(zoneId);
        System.out.println("========================================");
        return analytics;
    }

    /**
     * PRIVATE: Gets analytics for PRO and other roles - shows campus data for the
     * employee's campus.
     */
    private CombinedAnalyticsDTO getCampusDirectAnalytics(SCEmployeeEntity employee) {
        int empId = employee.getEmpId();
        int campusId = employee.getEmpCampusId();
        String campusName = employee.getCampusName();
        String role = employee.getEmpStudApplicationRole();
        System.out.println("DEBUG: getCampusDirectAnalytics for empId: " + empId + ", campusId: " + campusId
                + ", campusName: " + campusName);
        // Check if employee has a valid campus ID
        if (campusId <= 0) {
            System.out.println("❌ ERROR: Employee " + empId + " has invalid campusId: " + campusId);
            return createEmptyAnalytics(role != null ? role : "Unknown", empId, "Employee not mapped to a Campus",
                    employee.getDesignationName());
        }
        System.out.println("✓ Fetching analytics data for campusId: " + campusId);
        // Get Data using Campus ID directly
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        GraphDTO graphData = getGraphDataForCampus(campusId);
        MetricsDataDTO metricsData = getMetricsDataForCampus(campusId);
        analytics.setGraphData(graphData);
        analytics.setMetricsData(metricsData);
        System.out.println("DEBUG: Graph data - yearlyData size: "
                + (graphData != null && graphData.getYearlyData() != null ? graphData.getYearlyData().size() : 0));
        System.out.println("DEBUG: Metrics data - metrics size: "
                + (metricsData != null && metricsData.getMetrics() != null ? metricsData.getMetrics().size() : 0));
        // Set Header Info
        analytics.setRole(role != null ? role : "Employee");
        analytics.setDesignationName(employee.getDesignationName());
        analytics.setEntityName(campusName != null ? campusName : "Campus " + campusId);
        analytics.setEntityId(campusId);
        System.out.println("========================================");
        return analytics;
    }

    // --- PRIVATE HELPER METHODS for ROLLUPS (Unchanged) ---
    // =========================================================================
    // DGM / CAMPUS DIRECT HELPERS
    // =========================================================================
    private GraphDTO getGraphDataForCampus(Integer campusId) {
        return getGraphData(
                (yearId) -> {
                    Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository
                            .getMetricsByCampusIdAndYear(campusId, yearId);
                    return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
                },
                () -> appStatusTrackRepository.findDistinctYearIdsByCampusId(campusId));
    }

    private MetricsDataDTO getMetricsDataForCampus(Integer campusId) {
        return getMetricsData(
                // 1. Main Metrics (Total, Issued, Damaged, etc.)
                (yearId) -> appStatusTrackRepository.getMetricsByCampusIdAndYear(campusId, yearId),
                // 2. Pro Metric (Sold count specifically for the card)
                (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusId, yearId),
                // 3. Distinct Years
                () -> appStatusTrackRepository.findDistinctYearIdsByCampusId(campusId));
    }

    private GraphDTO getGraphDataForCampuses(List<Integer> campusIds) {
        return getGraphData(
                (yearId) -> {
                    Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository
                            .getMetricsByCampusIdsAndYear(campusIds, yearId);
                    return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
                },
                () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds));
    }

    private MetricsDataDTO getMetricsDataForCampuses(List<Integer> campusIds) {
        return getMetricsData(
                // 1. Aggregated Metrics for the list of campuses
                (yearId) -> appStatusTrackRepository.getMetricsByCampusIdsAndYear(campusIds, yearId),
                // 2. Pro Metric for the list of campuses
                (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
                // 3. Distinct Years across all campuses
                () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds));
    }

    // =========================================================================
    // ZONAL / ZONE DIRECT HELPERS
    // =========================================================================
    private GraphDTO getGraphDataForZone(Integer zoneId) {
        return getGraphData(
                (yearId) -> {
                    Optional<MetricsAggregateDTO> metrics = appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneId,
                            yearId);
                    return metrics.map(m -> new GraphSoldSummaryDTO(m.appIssued(), m.appSold()));
                },
                () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneId));
    }

    private MetricsDataDTO getMetricsDataForZone(Integer zoneId) {
        return getMetricsData(
                // 1. Main Metrics
                (yearId) -> appStatusTrackRepository.getMetricsByZoneIdAndYear(zoneId, yearId),
                // 2. Pro Metric
                (yearId) -> appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneId, yearId),
                // 3. Distinct Years
                () -> appStatusTrackRepository.findDistinctYearIdsByZoneId(zoneId));
    }

    // --- Private Graph Data Helper (Unchanged) ---
    private GraphDTO getGraphData(
            Function<Integer, Optional<GraphSoldSummaryDTO>> dataFetcher,
            Supplier<List<Integer>> yearFetcher) {
        return getGraphData(dataFetcher, yearFetcher, false);
    }

    // --- Private Graph Data Helper with future year filtering ---
    private GraphDTO getGraphData(
            Function<Integer, Optional<GraphSoldSummaryDTO>> dataFetcher,
            Supplier<List<Integer>> yearFetcher,
            boolean filterFutureYears) {
        GraphDTO graphData = new GraphDTO();
        List<YearlyGraphPointDTO> yearlyDataList = new ArrayList<>();
        try {
            // Determine current academic year based on March 1st logic
            java.time.LocalDate today = java.time.LocalDate.now();
            int currentCalendarYear = today.getYear();
            int currentMonth = today.getMonthValue();

            // Calculate current academic year value
            // Before March 1st: current academic year = (currentCalendarYear - 1) -
            // currentCalendarYear
            // On/After March 1st: current academic year = currentCalendarYear -
            // (currentCalendarYear + 1)
            int currentAcademicYearValue = (currentMonth >= 3) ? currentCalendarYear : (currentCalendarYear - 1);

            // Find the current academic year entity
            Optional<AcademicYear> currentYearEntityOpt = academicYearRepository
                    .findByYearAndIsActive(currentAcademicYearValue, 1);

            List<Integer> yearIds = new ArrayList<>();

            if (currentYearEntityOpt.isPresent()) {
                AcademicYear currentYearEntity = currentYearEntityOpt.get();
                int currentYearValue = currentYearEntity.getYear();
                int currentYearId = currentYearEntity.getAcdcYearId();

                // Always show 4 years: current + 3 past years
                yearIds.add(currentYearId); // Current year

                // Get 3 previous years
                for (int i = 1; i <= 3; i++) {
                    int previousYearValue = currentYearValue - i;
                    Optional<AcademicYear> previousYearEntity = academicYearRepository
                            .findByYearAndIsActive(previousYearValue, 1);
                    if (previousYearEntity.isPresent()) {
                        yearIds.add(previousYearEntity.get().getAcdcYearId());
                    }
                }

                System.out.println(
                        "Using academic year logic: Current year=" + currentYearValue + "-" + (currentYearValue + 1) +
                                " (acdcYearId=" + currentYearId + "), Year IDs: " + yearIds);
            } else {
                // Fallback: Use data from yearFetcher if academic year not found
                List<Integer> existingYearIds = yearFetcher.get();
                if (!existingYearIds.isEmpty()) {
                    existingYearIds.sort(Integer::compare);
                    int currentYearId = existingYearIds.get(existingYearIds.size() - 1);
                    // Create list of 4 years: current + 3 previous years (based on acdcYearId)
                    for (int i = 0; i < 4; i++) {
                        yearIds.add(currentYearId - i);
                    }
                    System.out.println("Fallback: Using data from yearFetcher, Year IDs: " + yearIds);
                } else {
                    // No data available
                    graphData.setTitle("Application Sales Percentage (No Data)");
                    graphData.setYearlyData(yearlyDataList);
                    return graphData;
                }
            }

            // Filter out future years before March 1st if flag is set
            if (filterFutureYears && currentMonth < 3) {
                // Before March 1st: Filter out years with year field > (currentCalendarYear -
                // 1)
                int maxAllowedYear = currentCalendarYear - 1;
                yearIds = yearIds.stream()
                        .filter(yearId -> {
                            Optional<AcademicYear> yearOpt = academicYearRepository.findById(yearId);
                            if (yearOpt.isPresent()) {
                                int yearValue = yearOpt.get().getYear();
                                boolean keep = yearValue <= maxAllowedYear;
                                if (!keep) {
                                    System.out
                                            .println("Filtering out future year: " + yearValue + "-" + (yearValue + 1) +
                                                    " (acdcYearId: " + yearId + ") - exceeds max allowed year "
                                                    + maxAllowedYear);
                                }
                                return keep;
                            }
                            return true; // Keep if year not found
                        })
                        .collect(Collectors.toList());

                // If after filtering we have fewer than 4 years, add more past years to make it
                // 4
                if (yearIds.size() < 4) {
                    // Find the oldest year in the list
                    int oldestYearValue = yearIds.stream()
                            .map(yearId -> academicYearRepository.findById(yearId))
                            .filter(Optional::isPresent)
                            .map(opt -> opt.get().getYear())
                            .min(Integer::compare)
                            .orElse(currentAcademicYearValue);

                    // Add more past years until we have 4 years
                    int yearsToAdd = 4 - yearIds.size();
                    for (int i = 1; i <= yearsToAdd; i++) {
                        int previousYearValue = oldestYearValue - i;
                        Optional<AcademicYear> previousYearEntity = academicYearRepository
                                .findByYearAndIsActive(previousYearValue, 1);
                        if (previousYearEntity.isPresent()) {
                            yearIds.add(previousYearEntity.get().getAcdcYearId());
                        }
                    }
                }

                System.out.println("Filtered future years (before March 1st, maxAllowedYear=" + maxAllowedYear +
                        "): Final yearIds = " + yearIds);
            }

            // Sort yearIds in descending order (newest first) for consistent display
            yearIds.sort(Comparator.reverseOrder());

            // Get AcademicYear entities for all years
            List<AcademicYear> academicYears = academicYearRepository.findByAcdcYearIdIn(yearIds)
                    .stream()
                    .sorted(Comparator.comparingInt(AcademicYear::getAcdcYearId))
                    .toList();

            // Create a map for quick lookup
            java.util.Map<Integer, AcademicYear> yearMap = academicYears.stream()
                    .collect(Collectors.toMap(AcademicYear::getAcdcYearId, y -> y));

            // Build graph data for all 4 years (always return 4 years, even if some have 0
            // values)
            for (Integer yearId : yearIds) {
                AcademicYear year = yearMap.get(yearId);
                if (year == null) {
                    // If year doesn't exist in database, skip it
                    continue;
                }

                String yearLabel = year.getAcademicYear();
                GraphSoldSummaryDTO summary = dataFetcher.apply(yearId)
                        .orElse(new GraphSoldSummaryDTO(0L, 0L));
                long issued = summary.totalApplications();
                long sold = summary.totalSold();
                double issuedPercent = issued > 0 ? 100.0 : 0.0;
                double soldPercent = (issued > 0)
                        ? Math.min(100.0, ((double) sold / issued) * 100.0)
                        : 0.0;
                yearlyDataList.add(new YearlyGraphPointDTO(
                        yearLabel, issuedPercent, soldPercent, issued, sold));
            }

            if (!yearlyDataList.isEmpty()) {
                graphData.setTitle("Application Sales Percentage (" +
                        yearlyDataList.get(0).getYear() + "–" +
                        yearlyDataList.get(yearlyDataList.size() - 1).getYear() + ")");
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
            // ============================================================
            // FOR METRIC CARDS: Show current year before March 1st, future year after March
            // 1st
            // ============================================================
            // Academic year for metric cards:
            // - Before March 1st: Show current academic year (e.g., Jan 2026 → shows
            // 2026-27, year = 2026)
            // - On/After March 1st: Show future academic year (e.g., March 2026 → shows
            // 2027-28, year = 2027)
            java.time.LocalDate today = java.time.LocalDate.now();
            int currentCalendarYear = today.getYear();
            int currentMonth = today.getMonthValue();

            // Calculate the academic year value for metric cards
            // Before March 1st: Use currentCalendarYear (e.g., Jan 2026 → year = 2026 →
            // shows 2026-27)
            // On/After March 1st: Use currentCalendarYear + 1 (e.g., March 2026 → year =
            // 2027 → shows 2027-28) - FUTURE YEAR
            int cardsAcademicYearValue = (currentMonth >= 3) ? (currentCalendarYear + 1) : currentCalendarYear;

            System.out.println("========================================");
            System.out.println("METRIC CARDS ACADEMIC YEAR CALCULATION");
            System.out.println("Current Calendar Year: " + currentCalendarYear);
            System.out.println("Current Month: " + currentMonth);
            System.out.println("Cards Academic Year Value (year field to search): " + cardsAcademicYearValue);
            System.out.println("Expected: " + (currentMonth >= 3
                    ? "After March 1st → year=" + (currentCalendarYear + 1) + " → shows " + (currentCalendarYear + 1)
                            + "-" + (currentCalendarYear + 2) + " (FUTURE YEAR)"
                    : "Before March 1st → year=" + currentCalendarYear + " → shows " + currentCalendarYear + "-"
                            + (currentCalendarYear + 1)));
            System.out.println("========================================");

            // Find the academic year entity for metric cards
            java.util.Optional<AcademicYear> cardsYearEntityOpt = academicYearRepository
                    .findByYearAndIsActive(cardsAcademicYearValue, 1);

            Integer currentYearId = null;
            if (cardsYearEntityOpt.isPresent()) {
                currentYearId = cardsYearEntityOpt.get().getAcdcYearId();
                System.out.println("✅ METRIC CARDS: Found academic year - acdcYearId: " + currentYearId +
                        ", year: " + cardsYearEntityOpt.get().getYear() +
                        ", academicYear: " + cardsYearEntityOpt.get().getAcademicYear());
            } else {
                // If academic year not found, try to find the latest active year as fallback
                System.out.println("METRIC CARDS: Academic year with year=" + cardsAcademicYearValue
                        + " not found, trying fallback...");
                List<Integer> yearIds = yearFetcher.get();
                if (yearIds != null && !yearIds.isEmpty()) {
                    // Use the latest year from data as fallback
                    yearIds.sort(Integer::compare);
                    currentYearId = yearIds.get(yearIds.size() - 1);
                    System.out.println("METRIC CARDS: Using fallback - acdcYearId: " + currentYearId);
                } else {
                    // If still no year found, return empty metrics
                    System.out.println("METRIC CARDS: No years found - returning empty metrics");
                    dto.setMetrics(new ArrayList<>());
                    return dto;
                }
            }

            // Always use previous year (currentYearId - 1) for comparison
            int previousYearId = currentYearId - 1;
            System.out.println("📅 Current Year ID: " + currentYearId);
            System.out.println("📅 Previous Year ID: " + previousYearId + " (calculated as currentYearId - 1)");

            AcademicYear cy = academicYearRepository.findById(currentYearId).orElse(null);
            AcademicYear py = academicYearRepository.findById(previousYearId).orElse(null);

            // If previous year doesn't exist by ID, try to find it by year (currentYear -
            // 1)
            if (py == null && cy != null) {
                int previousYearNumber = cy.getYear() - 1;
                py = academicYearRepository.findByYear(previousYearNumber).orElse(null);
                System.out.println("📅 Previous year not found by ID " + previousYearId + ", trying to find by year "
                        + previousYearNumber);
            }

            System.out.println("📅 Current Year Entity: " + (cy != null ? cy.getAcademicYear() : "NULL"));
            System.out.println("📅 Previous Year Entity: " + (py != null ? py.getAcademicYear() : "NULL"));

            dto.setCurrentYear(cy != null ? cy.getYear() : 0);
            dto.setPreviousYear(py != null ? py.getYear() : (cy != null ? cy.getYear() - 1 : 0));

            System.out.println("🔍 Fetching current year metrics...");
            MetricsAggregateDTO curr = dataFetcher.apply(currentYearId)
                    .orElse(new MetricsAggregateDTO());
            System.out.println("🔍 Fetching previous year metrics...");
            MetricsAggregateDTO prev = dataFetcher.apply(previousYearId)
                    .orElse(new MetricsAggregateDTO());

            System.out.println("📊 Current Metrics - TotalApp: " + curr.totalApp() + ", Sold: " + curr.appSold()
                    + ", Confirmed: " + curr.appConfirmed() + ", Available: " + curr.appAvailable() + ", Issued: "
                    + curr.appIssued());
            System.out.println("📊 Previous Metrics - TotalApp: " + prev.totalApp() + ", Sold: " + prev.appSold()
                    + ", Confirmed: " + prev.appConfirmed() + ", Available: " + prev.appAvailable() + ", Issued: "
                    + prev.appIssued());

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
        // Use appAvailable field directly from MetricsAggregateDTO (not calculated as
        // totalApp - appIssued)
        // This allows direct campus endpoint to show available = 0 as set in the data
        // fetcher
        metrics.add(createMetric("Available",
                total.appAvailable(),
                current.appAvailable(), previous.appAvailable()));
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

    private MetricDTO createMetricWithPercentage(String title, long totalValue, double currentPercent,
            double previousPercent) {
        double change = calculatePercentageChange(currentPercent, previousPercent);
        return new MetricDTO(title, totalValue, change, getChangeDirection(change));
    }

    private double calculatePercentage(long numerator, long denominator) {
        if (denominator == 0)
            return 0.0;
        return (double) Math.max(0, numerator) * 100.0 / denominator;
    }

    private double calculatePercentageChange(double current, double previous) {
        // If previous year has no data (0) and current year has data (> 0): show 100%
        // (like graph issued percentage is 100 when data exists)
        if (previous == 0)
            return (current > 0) ? 100.0 : 0.0;
        // If current year has no data (0) and previous had data: show -100% (actual
        // negative value)
        if (current == 0)
            return -100.0;
        // If both have data: calculate normal percentage change (can be negative)
        double change = ((current - previous) / previous) * 100;
        return Math.round(change);
    }

    // Helper method for percentage calculation with clamping (same as
    // AdminDashboardService)
    private int clampChange(int prev, int curr) {
        if (prev == 0) {
            // If previous was 0, any increase is considered 100% growth, but if current is
            // also 0, return 0
            return curr > 0 ? 100 : 0;
        }
        double raw = ((double) (curr - prev) / prev) * 100;
        return clamp(raw);
    }

    private int clamp(double value) {
        if (value > 100)
            return 100;
        if (value < -100)
            return -100;
        return (int) Math.round(value);
    }

    // Helper method for percentage calculation without clamping (for sold
    // percentage in graph)
    private int unclampedChange(int prev, int curr) {
        if (prev == 0) {
            // If previous was 0, any increase shows a high percentage to indicate growth
            // If current is also 0, return 0
            return curr > 0 ? 1000 : 0; // Show 1000% to indicate significant growth from 0
        }
        double raw = ((double) (curr - prev) / prev) * 100;
        // Don't clamp - show actual percentage even if it exceeds 100 or goes below
        // -100
        return (int) Math.round(raw);
    }

    private String getChangeDirection(double change) {
        if (change > 0)
            return "up";
        if (change < 0)
            return "down";
        return "neutral";
    }

    private int getAcdcYearId(int year) {
        return academicYearRepository.findByYear(year)
                .map(AcademicYear::getAcdcYearId)
                .orElse(0);
    }

    // --- NEW: Flexible Graph Data Method with Optional Filters ---
    /**
     * Get year-wise graph data (GraphBarDTO) with optional filters for zoneId,
     * campusIds, campusId, and amount.
     * All parameters are optional. Always returns data for the past 4 years
     * (current + 3 previous).
     * If data doesn't exist for a year, returns 0 values for that year.
     *
     * IMPORTANT:
     * - campusId (singular) uses entity_id = 4 (single campus/PRO role)
     * - campusIds (plural) uses entity_id = 3 (DGM rollup with multiple campuses)
     *
     * @param zoneId    Optional zone ID filter
     * @param campusIds Optional list of campus IDs filter (uses entity_id = 3 for
     *                  DGM rollup)
     * @param campusId  Optional single campus ID filter (uses entity_id = 4 for
     *                  single campus)
     * @param amount    Optional amount filter
     * @return List of GraphBarDTO containing year-wise issued and sold data for
     *         past 4 years
     */
    public List<GraphBarDTO> getFlexibleGraphData(Integer zoneId, List<Integer> campusIds, Integer campusId,
            Float amount, Integer empId) {
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
        // - campusIds (plural) uses entity_id = 3 (list methods, even for single
        // element)

        // IMPORTANT: Keep campusId (singular) and campusIds (plural) separate
        // - campusId (singular) uses entity_id = 4 (single campus/PRO)
        // - campusIds (plural) uses entity_id = 3 (DGM rollup)
        // Do NOT merge them - handle separately
        boolean hasCampusId = campusId != null;
        boolean hasCampusIds = campusIds != null && !campusIds.isEmpty();

        // Only merge for campusIds list (DGM rollup), not for single campusId
        List<Integer> effectiveCampusIds = new ArrayList<>();
        if (campusIds != null)
            effectiveCampusIds.addAll(campusIds);

        boolean hasCampuses = hasCampusIds; // Only true if campusIds (plural) is provided

        // DEBUG: Check individual campus data before aggregation (for multiple campuses
        // from campusIds)
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
                    System.out.println(
                            " ⚠️ NO DATA FOUND for Campus ID " + id + " (for the past 4 years with current filters)");
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
        // Priority: Check campusId (singular, entity_id=4) first, then campusIds
        // (plural, entity_id=3)
        if (zoneId != null && hasCampusId && amount != null) {
            // Zone + Single Campus (campusId) + Amount - use single campus method with
            // entity_id = 4
            System.out.println("Filter: Zone + Single Campus (campusId) + Amount (zone=" + zoneId + ", campusId="
                    + campusId + ", amt=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusAndAmount(zoneId, campusId, amount);
        } else if (zoneId != null && hasCampuses && amount != null) {
            // Zone + Campuses (campusIds) + Amount - use NEW method with entity_id = 4
            System.out.println("Filter: Zone + Campuses (campusIds) + Amount (zone=" + zoneId + ", camps="
                    + effectiveCampusIds + ", amt=" + amount + ") - Using entity_id = 4");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListAndAmountWithEntity4(zoneId,
                    effectiveCampusIds, amount);
        } else if (hasCampusId && amount != null) {
            // Single Campus (campusId) + Amount - use single campus method with entity_id =
            // 4
            System.out.println(
                    "Filter: Single Campus (campusId) + Amount (campusId=" + campusId + ", amt=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(campusId, amount);
        } else if (hasCampuses && amount != null) {
            // Campuses (campusIds) + Amount - use entity_id = 3 ONLY for issued (DGM
            // level), entity_id = 4 for sold
            System.out.println("Filter: Campuses (campusIds) + Amount (camps=" + effectiveCampusIds + ", amt=" + amount
                    + ") - Using entity_id = 3 ONLY for issued, entity_id = 4 for sold");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListAndAmountWithEntity4(effectiveCampusIds,
                    amount);
        } else if (zoneId != null && amount != null) {
            System.out.println("Filter: Zone + Amount (zone=" + zoneId + ", amt=" + amount
                    + ") - Using distinct series with entity_id = 2 ONLY for issued, entity_id = 4 for sold");
            // Use distinct series counting to avoid double counting when same series
            // appears in multiple entity_ids
            // For zone: entity_id = 2 ONLY for issued (Zone level), entity_id = 4 for sold
            // Admin→DGM and Admin→Campus distributions will be added separately to avoid
            // double counting
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneAndAmountWithDistinct(zoneId, amount);
        } else if (zoneId != null && hasCampusId) {
            // Zone + Single Campus (campusId) - use single campus method with entity_id = 4
            System.out.println(
                    "Filter: Zone + Single Campus (campusId) (zone=" + zoneId + ", campusId=" + campusId + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampus(zoneId, campusId);
        } else if (zoneId != null && hasCampuses) {
            // Zone + Campuses (campusIds) - use NEW method with entity_id = 4
            System.out.println("Filter: Zone + Campuses (campusIds) (zone=" + zoneId + ", camps=" + effectiveCampusIds
                    + ") - Using entity_id = 4");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListWithEntity4(zoneId,
                    effectiveCampusIds);
        } else if (hasCampusId) {
            // Single Campus (campusId) only - use single campus method with entity_id = 4
            System.out.println("Filter: Single Campus (campusId) (campusId=" + campusId + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(campusId);
        } else if (hasCampuses) {
            // Campuses (campusIds) only - use entity_id = 3 (totalAppCount) for issued,
            // entity_id = 4 for sold
            System.out.println("Filter: Campuses (campusIds) (camps=" + effectiveCampusIds
                    + ") - Using entity_id = 3 (totalAppCount) for issued, entity_id = 4 for sold");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListWithEntity4(effectiveCampusIds);
        } else if (zoneId != null) {
            System.out.println("Filter: Zone (zone=" + zoneId + ") - Using UserAppSold with entity_id = 2 only");
            // Use UserAppSold for zone, but fix the query to match zone analytics
            // Zone analytics uses: entity_id = 2 (Zone) + Admin→DGM + Admin→Campus
            // distributions
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZone(zoneId);
        } else if (amount != null && empId != null) {
            // NEW LOGIC: Role-based series matching from Distribution to UserAppSold
            // Get employee role and apply role-specific logic
            List<SCEmployeeEntity> employeeList = scEmployeeRepository.findByEmpId(empId);
            if (employeeList.isEmpty()) {
                System.out.println("⚠️ Employee " + empId + " not found, using default amount filter");
                rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
            } else {
                SCEmployeeEntity employee = employeeList.get(0);
                String role = employee.getEmpStudApplicationRole();
                if (role == null) {
                    System.out.println("⚠️ Employee " + empId + " has null role, using default amount filter");
                    rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
                } else {
                    String trimmedRole = role.trim().toUpperCase();
                    System.out.println("Filter: Amount + Employee Series Match (amt=" + amount + ", empId=" + empId
                            + ", role=" + trimmedRole + ")");

                    if (trimmedRole.equals("ADMIN")) {
                        // ADMIN: Get issued from Distribution table (total_app_count), sold/fast
                        // sale/confirmed from AppStatusTrackView
                        System.out.println(
                                "Using ADMIN logic: Issued from Distribution table (total_app_count), Sold/FastSale/Confirmed from AppStatusTrackView");

                        // Get issued from Distribution table (total_app_count) filtered by created_by
                        // and amount
                        System.out.println("========================================");
                        System.out.println("ADMIN DISTRIBUTION QUERY DEBUG");
                        System.out.println("Employee ID: " + empId);
                        System.out.println("Amount: " + amount);
                        System.out.println("========================================");
                        List<Object[]> distributionRows = distributionRepository
                                .getYearWiseTotalAppCountByCreatedByAndAmount(empId, amount);
                        System.out.println("Distribution rows returned: " + distributionRows.size());

                        // Get sold, fast sale, and confirmed from AppStatusTrackView
                        // This query matches distributions created_by = empId with amount filter
                        // Then checks applications in AppStatusTrackView within those distribution
                        // ranges
                        System.out.println(
                                "Getting sold/fast sale/confirmed from AppStatusTrackView for distributions created_by="
                                        + empId + ", amount=" + amount);
                        List<Object[]> statusRows = appStatusTrackViewRepository
                                .getYearWiseSoldFastSaleConfirmedByAdminAndAmount(empId, amount);
                        System.out.println("Status rows returned: " + statusRows.size());

                        // Merge: issued from Distribution, sold = sold + fast sale + confirmed from
                        // AppStatusTrackView
                        java.util.Map<Integer, long[]> mergedMap = new java.util.HashMap<>();

                        // Add issued data from Distribution table
                        for (Object[] row : distributionRows) {
                            Integer yearId = (Integer) row[0];
                            Long issued = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                            mergedMap.put(yearId, new long[] { issued, 0L }); // sold will be updated below
                            System.out.println("Admin Year " + yearId
                                    + ": Issued (from Distribution total_app_count with DISTINCT ranges)=" + issued);
                        }
                        System.out.println("========================================");

                        // Add sold/confirmed data from AppStatusTrackView
                        // Query returns: [yearId, sold_count, fast_sale_count, confirmed_count]
                        // sold_count = "not confirmed" OR "fast sale"
                        // fast_sale_count = "fast sale" (subset of sold_count)
                        // confirmed_count = "confirmed"
                        // Total sold = sold_count + confirmed_count (since fast_sale is already in
                        // sold_count)
                        // OR if we want explicit: sold (not confirmed only) + fast_sale + confirmed
                        // Based on user request: sold + fast sale + confirmed
                        // We'll use: sold_count (which includes fast sale) + confirmed_count
                        // This ensures: (not confirmed + fast sale) + confirmed = all sold applications
                        for (Object[] row : statusRows) {
                            Integer yearId = (Integer) row[0];
                            Long soldCount = row[1] != null ? ((Number) row[1]).longValue() : 0L; // Includes "not
                                                                                                  // confirmed" + "fast
                                                                                                  // sale"
                            Long fastSaleCount = row[2] != null ? ((Number) row[2]).longValue() : 0L; // "fast sale"
                                                                                                      // (subset of
                                                                                                      // soldCount)
                            Long confirmedCount = row[3] != null ? ((Number) row[3]).longValue() : 0L; // "confirmed"

                            // Calculate total sold: sold_count (includes fast sale) + confirmed_count
                            // This gives us: (not confirmed + fast sale) + confirmed = all sold
                            // applications
                            long totalSold = soldCount + confirmedCount;

                            long[] data = mergedMap.getOrDefault(yearId, new long[] { 0L, 0L });
                            data[1] = totalSold; // Update sold count
                            mergedMap.put(yearId, data);

                            System.out.println("Admin Year " + yearId + ": SoldCount=" + soldCount
                                    + " (includes not confirmed + fast sale), FastSaleCount=" + fastSaleCount
                                    + ", ConfirmedCount=" + confirmedCount + ", TotalSold=" + totalSold);
                        }
                        System.out.println("========================================");

                        // Convert merged map back to rows format [yearId, issued, sold]
                        rows = new java.util.ArrayList<>();
                        for (java.util.Map.Entry<Integer, long[]> entry : mergedMap.entrySet()) {
                            rows.add(new Object[] { entry.getKey(), entry.getValue()[0], entry.getValue()[1] });
                        }
                    } else if (trimmedRole.equals("ZONAL ACCOUNTANT")) {
                        // ZONAL ACCOUNTANT: Use entity_id = 2 from UserAppSold, then add Admin→DGM and
                        // Admin→Campus distributions
                        List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
                        if (zonalRecords == null || zonalRecords.isEmpty() || zonalRecords.get(0).getZone() == null) {
                            System.out.println("⚠️ Zonal Accountant " + empId
                                    + " not mapped to zone, using default amount filter");
                            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
                        } else {
                            Integer empZoneId = zonalRecords.get(0).getZone().getZoneId();
                            System.out.println("Using ZONAL ACCOUNTANT logic: entity_id = 2 from UserAppSold (zone: "
                                    + empZoneId + ", amount: " + amount + ")");
                            System.out.println("Will add Admin→DGM and Admin→Campus distributions to issued count");
                            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneAndAmountWithDistinct(empZoneId,
                                    amount);
                            // Store zoneId for later distribution addition
                            zoneId = empZoneId;
                        }
                    } else if (trimmedRole.equals("DGM")) {
                        // DGM: Use entity_id = 3 directly from UserAppSold, then add Admin→Campus and
                        // Zone→Campus distributions
                        List<Integer> dgmCampusIds = dgmRepository.findCampusIdsByEmployeeId(empId);
                        if (dgmCampusIds == null || dgmCampusIds.isEmpty()) {
                            System.out.println("⚠️ DGM " + empId + " has no campuses, using default amount filter");
                            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
                        } else {
                            System.out.println("Using DGM logic: entity_id = 3 from UserAppSold (campuses: "
                                    + dgmCampusIds + ", amount: " + amount + ")");
                            System.out.println("Will add Admin→Campus and Zone→Campus distributions to issued count");
                            rows = userAppSoldRepository
                                    .getYearWiseIssuedAndSoldByCampusListAndAmountWithEntity4(dgmCampusIds, amount);
                            // Store DGM campusIds for later distribution addition
                            effectiveCampusIds = dgmCampusIds;
                            hasCampuses = true;
                        }
                    } else if (trimmedRole.equals("PRO") || trimmedRole.contains("PRINCIPAL")
                            || trimmedRole.contains("VICE")) {
                        // PRO/PRINCIPAL/VICE PRINCIPAL: Use entity_id = 4 directly from UserAppSold for
                        // campus calculation
                        // Uses campusId from employee record and entity_id = 4 for both issued
                        // (totalAppCount) and sold
                        int empCampusId = employee.getEmpCampusId();
                        if (empCampusId <= 0) {
                            System.out.println("⚠️ " + trimmedRole + " " + empId
                                    + " has invalid campusId, using default amount filter");
                            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
                        } else {
                            System.out.println(
                                    "Using " + trimmedRole + " logic: entity_id = 4 from UserAppSold (campusId: "
                                            + empCampusId + ", amount: " + amount + ")");
                            System.out.println(
                                    "Query uses entity_id = 4 for campus calculation - totalAppCount for issued, sold for sold count");
                            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(empCampusId, amount);
                        }
                    } else {
                        // Unknown role, use default
                        System.out.println("⚠️ Unknown role '" + trimmedRole + "', using default amount filter");
                        rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
                    }
                }
            }
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
                    System.out.println(
                            " Year ID: " + yearId + " | Issued (totalAppCount): " + totalAppCount + " | Sold: " + sold);
                }
            }
        }
        // Create a map of yearId -> [totalAppCount, sold] for quick lookup
        java.util.Map<Integer, long[]> yearDataMap = new java.util.HashMap<>();
        for (Object[] row : rows) {
            Integer yearId = (Integer) row[0];
            Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            yearDataMap.put(yearId, new long[] { totalAppCount, sold });
        }

        // If zoneId is present, add Admin→DGM and Admin→Campus distributions to issued
        // count for each year
        // UserAppSold query returns entity_id = 2 (Zone) with totalAppCount (NOT
        // totalAppCount - appAvlbCount)
        // IMPORTANT: When amount filter is present (any value, including 0), use ONLY
        // UserAppSold data
        // Do NOT add distributions because distributions don't have amount info
        if (zoneId != null && amount == null) {
            // Only add distributions when NO amount filter is present
            for (Integer yearId : yearIds) {
                // Get distribution counts for this year (Admin→DGM and Admin→Campus)
                Integer adminToDgmDist = distributionRepository.sumAdminToDgmDistributionByZoneAndYear(zoneId, yearId)
                        .orElse(0);
                Integer adminToCampusDist = distributionRepository
                        .sumAdminToCampusDistributionByZoneAndYear(zoneId, yearId).orElse(0);

                // Add distributions to issued count (UserAppSold query returns entity_id = 2
                // with totalAppCount, so we add distributions)
                long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
                long updatedIssued = data[0] + adminToDgmDist + adminToCampusDist;
                yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

                if (adminToDgmDist > 0 || adminToCampusDist > 0) {
                    System.out.println("Zone " + zoneId + " Year " + yearId
                            + ": UserAppSold (entity_id=2) totalAppCount=" + data[0] +
                            ", Added Admin→DGM: " + adminToDgmDist + ", Admin→Campus: " + adminToCampusDist +
                            ", Total issued=" + updatedIssued);
                }
            }
        } else if (zoneId != null && amount != null) {
            // When amount filter is present, add Admin→DGM and Admin→Campus distributions
            // to issued count
            // This applies to both: (1) zoneId parameter passed directly, and (2) ZONAL
            // ACCOUNTANT role with empId
            // UserAppSold query returns entity_id = 2 ONLY (Zone level) with totalAppCount,
            // so we add Admin→DGM and Admin→Campus distributions
            // This avoids double counting because entity_id=3 (DGM) is excluded from
            // UserAppSold
            System.out.println("Adding Admin→DGM and Admin→Campus distributions for zoneId: " + zoneId
                    + " with amount filter: " + amount);
            for (Integer yearId : yearIds) {
                // Get distribution counts for this year (Admin→DGM and Admin→Campus) with
                // amount filter
                // Uses DISTINCT on ranges to avoid duplicate counting within Distribution table
                Integer adminToDgmDist = distributionRepository
                        .sumAdminToDgmDistributionByZoneYearAndAmount(zoneId, yearId, amount).orElse(0);
                Integer adminToCampusDist = distributionRepository
                        .sumAdminToCampusDistributionByZoneYearAndAmount(zoneId, yearId, amount).orElse(0);

                // Add distributions to issued count
                long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
                long updatedIssued = data[0] + adminToDgmDist + adminToCampusDist;
                yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

                if (adminToDgmDist > 0 || adminToCampusDist > 0) {
                    System.out.println("Zone " + zoneId + " Year " + yearId + " Amount " + amount
                            + ": UserAppSold (entity_id=2 ONLY) totalAppCount=" + data[0] +
                            ", Added Admin→DGM: " + adminToDgmDist + ", Admin→Campus: " + adminToCampusDist +
                            ", Total issued=" + updatedIssued);
                }
            }
        }

        // If campusIds is present (DGM rollup), add Admin→Campus and Zone→Campus
        // distributions to issued count for each year
        // NOTE: Only distributions with issued_to_type_id = 4 (Campus/PRO) are added to
        // issued count
        // Admin→DGM (issued_to_type_id = 3) is NOT included in issued count
        // IMPORTANT: When amount filter is present, use ONLY UserAppSold data, do NOT
        // add distributions
        if (hasCampuses && effectiveCampusIds != null && !effectiveCampusIds.isEmpty() && amount == null) {
            // Only add distributions when NO amount filter is present
            for (Integer yearId : yearIds) {
                // Get Admin→Campus distribution (filtered by campusIds, issued_to_type_id = 4)
                Integer adminToCampusDist = distributionRepository
                        .sumAdminToCampusDistributionByCampusIdsAndYear(effectiveCampusIds, yearId).orElse(0);

                // Get Zone→Campus distribution (filtered by campusIds, issued_to_type_id = 4)
                Integer zoneToCampusDist = distributionRepository
                        .sumZoneToCampusDistributionByCampusIdsAndYear(effectiveCampusIds, yearId).orElse(0);

                // Add ONLY issued_to_type_id = 4 distributions to issued count (Admin→Campus +
                // Zone→Campus)
                long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
                long issuedDistCount = adminToCampusDist + zoneToCampusDist; // Only issued_to_type_id = 4
                long updatedIssued = data[0] + issuedDistCount;
                yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

                if (issuedDistCount > 0) {
                    System.out.println("CampusIds " + effectiveCampusIds + " Year " + yearId
                            + ": UserAppSold totalAppCount (entity_id=3)=" + data[0] +
                            ", Added Admin→Campus: " + adminToCampusDist + ", Zone→Campus: " + zoneToCampusDist +
                            " (issued_to_type_id = 4 only, Total issued: " + updatedIssued + ")");
                }
            }
        } else if (hasCampuses && effectiveCampusIds != null && !effectiveCampusIds.isEmpty() && amount != null) {
            // When amount filter is present, add Admin→Campus and Zone→Campus distributions
            // to issued count
            // This applies to both: (1) campusIds parameter passed directly, and (2) DGM
            // role with empId
            // UserAppSold query returns entity_id = 3 ONLY (DGM level) with totalAppCount,
            // so we add Admin→Campus and Zone→Campus distributions
            // This avoids double counting because entity_id=1 (Admin) and entity_id=4
            // (Campus) are excluded from UserAppSold
            System.out.println("Adding Admin→Campus and Zone→Campus distributions for campusIds: " + effectiveCampusIds
                    + " with amount filter: " + amount);
            for (Integer yearId : yearIds) {
                // Get distribution counts for this year (Admin→Campus and Zone→Campus) with
                // amount filter
                // Uses DISTINCT on ranges to avoid duplicate counting within Distribution table
                Integer adminToCampusDist = distributionRepository
                        .sumAdminToCampusDistributionByCampusIdsYearAndAmount(effectiveCampusIds, yearId, amount)
                        .orElse(0);
                Integer zoneToCampusDist = distributionRepository
                        .sumZoneToCampusDistributionByCampusIdsYearAndAmount(effectiveCampusIds, yearId, amount)
                        .orElse(0);

                // Add distributions to issued count
                long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
                long updatedIssued = data[0] + adminToCampusDist + zoneToCampusDist;
                yearDataMap.put(yearId, new long[] { updatedIssued, data[1] });

                if (adminToCampusDist > 0 || zoneToCampusDist > 0) {
                    System.out.println("CampusIds " + effectiveCampusIds + " Year " + yearId + " Amount " + amount
                            + ": UserAppSold (entity_id=3 ONLY) totalAppCount=" + data[0] +
                            ", Added Admin→Campus: " + adminToCampusDist + ", Zone→Campus: " + zoneToCampusDist +
                            ", Total issued=" + updatedIssued);
                }
            }
        }
        // Log aggregated data summary
        if (campusIds != null && campusIds.size() > 1) {
            System.out.println("Aggregated data by year (summed across all campuses):");
            for (Integer yearId : yearIds) {
                long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
                System.out.println(" Year ID: " + yearId + " | Total Issued: " + data[0] + " | Total Sold: " + data[1]);
            }
        }
        // Build GraphBarDTO list for all 4 years (always return 4 years)
        // Calculate percentages by comparing each year with its previous year (like
        // metric cards)
        List<GraphBarDTO> barList = new ArrayList<>();
        for (int i = 0; i < yearIds.size(); i++) {
            Integer yearId = yearIds.get(i);
            Integer previousYearId = (i < yearIds.size() - 1) ? yearIds.get(i + 1) : null; // Previous year (next in
                                                                                           // list)

            long[] data = yearDataMap.getOrDefault(yearId, new long[] { 0L, 0L });
            long issuedCount = data[0]; // totalAppCount from table
            long soldCount = data[1]; // sold from table
            AcademicYear year = yearMap.get(yearId);
            // Format year label as "2026-27" (4-digit year - 2-digit next year)
            String yearLabel;
            if (year != null) {
                int yearValue = year.getYear();
                int nextYear = yearValue + 1;
                yearLabel = String.format("%d-%02d", yearValue, nextYear % 100);
            } else {
                yearLabel = "Year " + yearId;
            }

            // Calculate percentages by comparing with previous year
            int issuedPercent;
            int soldPercent;

            if (previousYearId != null) {
                // Get previous year data for comparison
                long[] prevData = yearDataMap.getOrDefault(previousYearId, new long[] { 0L, 0L });
                long prevIssuedCount = prevData[0];
                long prevSoldCount = prevData[1];

                // Calculate percentage change
                // Issued: Use clampChange (clamped to -100 to 100)
                issuedPercent = clampChange((int) prevIssuedCount, (int) issuedCount);
                // Sold: Use unclampedChange (show actual percentage even if exceeds 100)
                soldPercent = unclampedChange((int) prevSoldCount, (int) soldCount);
            } else {
                // For the oldest year (no previous year), use absolute percentages
                if (issuedCount > 0) {
                    issuedPercent = 100; // 100% as baseline when data exists
                    soldPercent = (int) Math.round((soldCount * 100.0) / issuedCount);
                } else {
                    // No data exists - both percentages are 0
                    issuedPercent = 0;
                    soldPercent = 0;
                }
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
     *
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
     *
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
     *
     * @param category Optional category filter: "school" or "college"
     * @return List of GenericDropdownDTO_Dgm containing employee ID, name, and
     *         associated campus IDs
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
        // A DGM employee is included if they have at least one campus matching the
        // category
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

    /**
     * Get Current, Next, and Previous Two Academic Years
     * Returns academic year ID and academic year string for each
     * 
     * Current year transitions on March 1st:
     * - Before March 1st: current academic year = (current calendar year - 1)
     * Example: Feb 28, 2026 → academic year 2025-26 (year = 2025)
     * - On/After March 1st: current academic year = current calendar year
     * Example: March 1, 2026 → academic year 2026-27 (year = 2026)
     * Then uses that academic year's year field to calculate next and previous
     * years
     * All queries filter by is_active = 1
     *
     * @return AcademicYearInfoDTO containing current, next, and previous two
     *         academic years
     */
    public AcademicYearInfoDTO getAcademicYearInfo() {
        // Get current calendar year (e.g., 2026)
        // Academic year transitions on March 1st:
        // - Before March 1st: current academic year = (current calendar year - 1)
        // Example: Feb 28, 2026 → academic year 2025-26 (year = 2025)
        // - On/After March 1st: current academic year = current calendar year
        // Example: March 1, 2026 → academic year 2026-27 (year = 2026)
        java.time.LocalDate today = java.time.LocalDate.now();
        int currentCalendarYear = today.getYear();
        int currentMonth = today.getMonthValue();
        int currentAcademicYearValue = (currentMonth >= 3) ? currentCalendarYear : (currentCalendarYear - 1);
        System.out.println("========================================");
        System.out.println("ACADEMIC YEAR INFO CALCULATION");
        System.out.println("Current Calendar Year: " + currentCalendarYear);
        System.out.println("Current Month: " + currentMonth);
        System.out.println(
                "Finding academic year where year field = " + currentAcademicYearValue +
                        (currentMonth >= 3 ? " (on/after March 1st)" : " (before March 1st)"));
        System.out.println("========================================");

        // Find academic year where year field matches the calculated current academic
        // year
        // (filtered by is_active = 1)
        Optional<AcademicYear> currentYearEntityOpt = academicYearRepository
                .findByYearAndIsActive(currentAcademicYearValue, 1);

        AcademicYearDTO currentYear;
        AcademicYearDTO nextYear;
        AcademicYearDTO previousYear;

        if (currentYearEntityOpt.isPresent()) {
            AcademicYear currentYearEntity = currentYearEntityOpt.get();
            currentYear = new AcademicYearDTO(
                    currentYearEntity.getAcdcYearId(),
                    currentYearEntity.getAcademicYear());
            System.out.println("Current Academic Year Found: acdcYearId=" + currentYearEntity.getAcdcYearId() +
                    ", year=" + currentYearEntity.getYear() +
                    ", academicYear=" + currentYearEntity.getAcademicYear());

            // Next year: current academic year's year value + 1 (filtered by is_active = 1)
            Integer nextYearValue = currentYearEntity.getYear() + 1;
            Optional<AcademicYear> nextYearEntityOpt = academicYearRepository.findByYearAndIsActive(nextYearValue, 1);
            nextYear = nextYearEntityOpt.isPresent()
                    ? new AcademicYearDTO(nextYearEntityOpt.get().getAcdcYearId(),
                            nextYearEntityOpt.get().getAcademicYear())
                    : new AcademicYearDTO(null, null);
            if (nextYearEntityOpt.isPresent()) {
                System.out.println("Next Year Found: acdcYearId=" + nextYearEntityOpt.get().getAcdcYearId() +
                        ", year=" + nextYearEntityOpt.get().getYear() +
                        ", academicYear=" + nextYearEntityOpt.get().getAcademicYear());
            } else {
                System.out.println("Next Year NOT Found for year=" + nextYearValue);
            }

            // Previous year: current academic year's year value - 1 (filtered by is_active
            // = 1)
            Integer previousYearValue = currentYearEntity.getYear() - 1;
            Optional<AcademicYear> previousYearEntityOpt = academicYearRepository
                    .findByYearAndIsActive(previousYearValue, 1);
            previousYear = previousYearEntityOpt.isPresent()
                    ? new AcademicYearDTO(previousYearEntityOpt.get().getAcdcYearId(),
                            previousYearEntityOpt.get().getAcademicYear())
                    : new AcademicYearDTO(null, null);
            if (previousYearEntityOpt.isPresent()) {
                System.out.println("Previous Year Found: acdcYearId=" + previousYearEntityOpt.get().getAcdcYearId() +
                        ", year=" + previousYearEntityOpt.get().getYear() +
                        ", academicYear=" + previousYearEntityOpt.get().getAcademicYear());
            } else {
                System.out.println("Previous Year NOT Found for year=" + previousYearValue);
            }
        } else {
            // If current year not found, return empty DTOs
            System.out.println("Current Academic Year NOT Found for calendar year=" + currentCalendarYear);
            return new AcademicYearInfoDTO(
                    new AcademicYearDTO(null, null),
                    new AcademicYearDTO(null, null),
                    new AcademicYearDTO(null, null));
        }

        System.out.println("========================================");

        return new AcademicYearInfoDTO(currentYear, nextYear, previousYear);
    }

    /**
     * Get distinct app_amount values from AdminApp table
     *
     * @return List of distinct app_amount values (sorted in ascending order)
     */
    public List<Double> getDistinctAppAmounts() {
        try {
            List<Double> distinctAmounts = adminAppRepository.findDistinctAppAmounts();
            System.out.println("Distinct App Amounts Found: " + distinctAmounts.size());
            if (!distinctAmounts.isEmpty()) {
                System.out.println("Amounts: " + distinctAmounts);
            }
            return distinctAmounts;
        } catch (Exception e) {
            System.err.println("Error in getDistinctAppAmounts: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}