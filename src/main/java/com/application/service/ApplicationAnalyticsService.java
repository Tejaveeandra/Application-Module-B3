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
    public CombinedAnalyticsDTO getZoneAnalytics(Long zoneId) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        // Convert Long to Integer for the Repo calls that need it
        Integer zoneIdInt = zoneId.intValue();
        analytics.setGraphData(getGraphData(
            (yearId) -> userAppSoldRepository.getSalesSummaryByZone(zoneIdInt, yearId),
            () -> userAppSoldRepository.findDistinctYearIdsByZone(zoneIdInt)
        ));
        analytics.setMetricsData(
            getMetricsData(
                (yearId) -> appStatusTrackRepository.getMetricsByZoneAndYear(zoneId, yearId),
                // CHANGE IS HERE: Use AppStatusTrack repo (filter by appIssuedId=4)
                (yearId) -> appStatusTrackRepository.getProMetricByZoneId_FromStatus(zoneIdInt, yearId),
                () -> appStatusTrackRepository.findDistinctYearIdsByZone(zoneId)
            )
        );
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
    CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
    // 2. Pass the list of campusIds to your repositories
    // Ensure your repository methods are updated to use "IN :campusIds" instead of "= :campusId"
    analytics.setGraphData(getGraphData(
        (yearId) -> userAppSoldRepository.getSalesSummaryByCampusIdsAndYear(campusIds, yearId),
        () -> userAppSoldRepository.findDistinctYearIdsByCampusIds(campusIds)
    ));
    analytics.setMetricsData(
        getMetricsData(
            (yearId) -> appStatusTrackRepository.getMetricsByCampusIdsAndYear(campusIds, yearId),
            (yearId) -> appStatusTrackRepository.getProMetricByCampusIds_FromStatus(campusIds, yearId),
            () -> appStatusTrackRepository.findDistinctYearIdsByCampusIds(campusIds)
        )
    );
    return analytics;
}
    public CombinedAnalyticsDTO getCampusAnalytics(Long campusId) {
        CombinedAnalyticsDTO analytics = new CombinedAnalyticsDTO();
        // Convert Long to Integer for the Repo calls
        Integer campusIdInt = campusId.intValue();
        analytics.setGraphData(getGraphData(
            (yearId) -> userAppSoldRepository.getSalesSummaryByCampus(campusIdInt, yearId),
            () -> userAppSoldRepository.findDistinctYearIdsByCampus(campusIdInt)
        ));
        analytics.setMetricsData(
            getMetricsData(
                (yearId) -> appStatusTrackRepository.getMetricsByCampusAndYear(campusId, yearId),
                // CHANGE IS HERE: Use AppStatusTrack repo
                (yearId) -> appStatusTrackRepository.getProMetricByCampusId_FromStatus(campusIdInt, yearId),
                () -> appStatusTrackRepository.findDistinctYearIdsByCampus(campusId)
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
        // Lambda passes 'yearId' to the repo method
        (yearId) -> userAppSoldRepository.getSalesSummaryByCampusIdAndYear(campusId, yearId),
        // Supplier gets distinct years
        () -> userAppSoldRepository.findDistinctYearIdsByCampusId(campusId)
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
        // Lambda passes 'yearId' and the LIST of campusIds
        (yearId) -> userAppSoldRepository.getSalesSummaryByCampusIdsAndYear(campusIds, yearId),
        // Supplier gets distinct years for ALL campuses in the list
        () -> userAppSoldRepository.findDistinctYearIdsByCampusIds(campusIds)
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
        // Lambda passes 'yearId' to the repo method
        (yearId) -> userAppSoldRepository.getSalesSummaryByZoneIdAndYear(zoneId, yearId),
        // Supplier gets distinct years
        () -> userAppSoldRepository.findDistinctYearIdsByZoneId(zoneId)
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
        MetricsDataDTO dto = new MetricsDataDTO();
        try {
            List<Integer> yearIds = yearFetcher.get();
            if (yearIds.isEmpty()) {
                dto.setMetrics(new ArrayList<>());
                return dto;
            }
            // Sort yearIds ascending → last one is current year
            yearIds.sort(Integer::compare);
            int currentYearId = yearIds.get(yearIds.size() - 1);
            int previousYearId = (yearIds.size() > 1)
                    ? yearIds.get(yearIds.size() - 2)
                    : currentYearId;
            AcademicYear cy = academicYearRepository.findById(currentYearId).orElse(null);
            AcademicYear py = academicYearRepository.findById(previousYearId).orElse(null);
            dto.setCurrentYear(cy != null ? cy.getYear() : 0);
            dto.setPreviousYear(py != null ? py.getYear() : 0);
            MetricsAggregateDTO curr = dataFetcher.apply(currentYearId)
                    .orElse(new MetricsAggregateDTO());
            MetricsAggregateDTO prev = dataFetcher.apply(previousYearId)
                    .orElse(new MetricsAggregateDTO());
            long proCurr = proFetcher.apply(currentYearId).orElse(0L);
            long proPrev = proFetcher.apply(previousYearId).orElse(0L);
MetricsAggregateDTO totalMetrics = curr; // instead of summing every year
            long totalPro = proCurr;
            // ------------------------------------------------------
            List<MetricDTO> cards = buildMetricsList(curr, prev, totalMetrics, proCurr, proPrev, totalPro);
            dto.setMetrics(cards);
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
     * Get year-wise graph data (GraphBarDTO) with optional filters for zoneId, campusIds, and amount.
     * All parameters are optional. Always returns data for the past 4 years (current + 3 previous).
     * If data doesn't exist for a year, returns 0 values for that year.
     *
     * Note: campusIds can contain one or more campus IDs. For single campus, pass a list with one element.
     * For multiple campuses, pass a list with multiple elements.
     *
     * @param zoneId Optional zone ID filter
     * @param campusIds Optional list of campus IDs filter (can be single or multiple campuses)
     * @param amount Optional amount filter
     * @return List of GraphBarDTO containing year-wise issued and sold data for past 4 years
     */
    public List<GraphBarDTO> getFlexibleGraphData(Integer zoneId, List<Integer> campusIds, Float amount) {
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
        // Check if we have multiple campuses or single campus
        boolean hasMultipleCampuses = campusIds != null && campusIds.size() > 1;
        boolean hasSingleCampus = campusIds != null && campusIds.size() == 1;
        Integer singleCampusId = hasSingleCampus ? campusIds.get(0) : null;
        // DEBUG: Check individual campus data before aggregation (for multiple campuses)
        if (hasMultipleCampuses) {
            System.out.println("=== CHECKING INDIVIDUAL CAMPUS DATA ===");
            for (Integer campusId : campusIds) {
                List<Object[]> individualCampusRows;
                if (amount != null) {
                    individualCampusRows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(campusId, amount);
                } else {
                    individualCampusRows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(campusId);
                }
                // Filter by yearIds
                individualCampusRows = individualCampusRows.stream()
                        .filter(row -> yearIds.contains((Integer) row[0]))
                        .collect(java.util.stream.Collectors.toList());
                System.out.println("Campus ID " + campusId + " - Individual data:");
                if (individualCampusRows.isEmpty()) {
System.out.println(" ⚠️ NO DATA FOUND for Campus ID " + campusId + " (for the past 4 years)");
                } else {
                    for (Object[] row : individualCampusRows) {
                        Integer yearId = (Integer) row[0];
                        Long totalAppCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                        Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
System.out.println(" Year ID: " + yearId + " | Issued: " + totalAppCount + " | Sold: " + sold);
                    }
                }
            }
            System.out.println("=== END INDIVIDUAL CAMPUS DATA CHECK ===");
        }
        // Determine which repository method to call based on provided parameters
        if (zoneId != null && hasMultipleCampuses && amount != null) {
            // Zone + Multiple Campuses + Amount
            System.out.println("Using filter: Zone + Multiple Campuses + Amount (zoneId=" + zoneId + ", campusIds=" + campusIds + ", amount=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusListAndAmount(zoneId, campusIds, amount);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (zoneId != null && hasSingleCampus && amount != null) {
            // Zone + Single Campus + Amount
            System.out.println("Using filter: Zone + Campus + Amount");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneCampusAndAmount(zoneId, singleCampusId, amount);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (hasMultipleCampuses && amount != null) {
            // Multiple Campuses + Amount
            System.out.println("Using filter: Multiple Campuses + Amount (campusIds=" + campusIds + ", amount=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusListAndAmount(campusIds, amount);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (hasSingleCampus && amount != null) {
            // Single Campus + Amount
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusAndAmount(singleCampusId, amount);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (zoneId != null && amount != null) {
            // Zone + Amount - need to filter by yearIds manually
            System.out.println("Using filter: Zone + Amount (zoneId=" + zoneId + ", amount=" + amount + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZoneAndAmount(zoneId, amount);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (hasMultipleCampuses) {
            // Multiple Campuses only
            System.out.println("Using filter: Multiple Campuses (campusIds=" + campusIds + ")");
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampusList(campusIds);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (hasSingleCampus) {
            // Single Campus only - need to filter by yearIds manually
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByCampus(singleCampusId);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (zoneId != null) {
            // Zone only - need to filter by yearIds manually
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByZone(zoneId);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else if (amount != null) {
            // Amount only - need to filter by yearIds manually
            rows = userAppSoldRepository.getYearWiseIssuedAndSoldByAmount(amount);
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // No filters - get all data and filter by yearIds
            rows = userAppSoldRepository.getYearWiseIssuedAndSold();
            rows = rows.stream()
                    .filter(row -> yearIds.contains((Integer) row[0]))
                    .collect(java.util.stream.Collectors.toList());
        }
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