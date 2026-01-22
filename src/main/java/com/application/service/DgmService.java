// package com.application.service;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import com.application.dto.AppSeriesDTO;
// import com.application.dto.FormSubmissionDTO;
// import com.application.dto.GenericDropdownDTO;
// import com.application.dto.LocationAutoFillDTO;
// import com.application.entity.AdminApp;
// import com.application.entity.BalanceTrack;
// import com.application.entity.Campus;
// import com.application.entity.City;
// import com.application.entity.Dgm;
// import com.application.entity.Distribution;
// import com.application.entity.District;
// import com.application.entity.UserAdminView;
// import com.application.repository.AcademicYearRepository;
// import com.application.repository.AdminAppRepository;
// import com.application.repository.AppIssuedTypeRepository;
// import com.application.repository.BalanceTrackRepository;
// import com.application.repository.CampusRepository;
// import com.application.repository.CityRepository;
// import com.application.repository.DgmRepository;
// import com.application.repository.DistributionRepository;
// import com.application.repository.EmployeeRepository;
// import com.application.repository.UserAdminViewRepository;
// import com.application.repository.ZonalAccountantRepository;
// import com.application.repository.ZoneRepository;
// import com.application.repository.SCEmployeeRepository;
// import com.application.dto.EmployeeLocationDTO;
// import com.application.dto.DgmWithCampusesDTO;
// import com.application.entity.SCEmployeeEntity;
// import com.application.entity.State;
// import com.application.entity.Zone;

// import lombok.NonNull;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class DgmService {

//     private final AcademicYearRepository academicYearRepository;
//     private final CityRepository cityRepository;
//     private final ZoneRepository zoneRepository;
//     private final CampusRepository campusRepository;
//     private final AppIssuedTypeRepository appIssuedTypeRepository;
//     private final DistributionRepository distributionRepository;
//     private final EmployeeRepository employeeRepository;
//     private final BalanceTrackRepository balanceTrackRepository;
//     private final DgmRepository dgmRepository;
//     private final UserAdminViewRepository userAdminViewRepository;
//     private final ZonalAccountantRepository zonalAccountantRepository;
//     private final AdminAppRepository adminAppRepository;
//     private final SCEmployeeRepository scEmployeeRepository;

//     // --- Dropdown and Helper Methods with Caching ---
//     // @Cacheable("academicYears")
//     public List<GenericDropdownDTO> getAllAcademicYears() {
//         return academicYearRepository.findAll().stream()
//                 .map(year -> new GenericDropdownDTO(year.getAcdcYearId(), year.getAcademicYear()))
//                 .collect(Collectors.toList());
//     }

//     @Cacheable("cities")
//     public List<GenericDropdownDTO> getAllCities() {
//         final int ACTIVE_STATUS = 1;

//         return cityRepository.findByStatus(ACTIVE_STATUS).stream()
//                 .map(city -> new GenericDropdownDTO(city.getCityId(), city.getCityName())).collect(Collectors.toList());
//     }

//     // @Cacheable(cacheNames = "zonesByCity", key = "#cityId")
//     public List<GenericDropdownDTO> getZonesByCityId(int cityId) {
//         return zoneRepository.findByCityCityId(cityId).stream()
//                 .map(zone -> new GenericDropdownDTO(zone.getZoneId(), zone.getZoneName())).collect(Collectors.toList());
//     }

//     @Cacheable(cacheNames = "campusesByZone", key = "#zoneId")
//     public List<GenericDropdownDTO> getCampusesByZoneId(int zoneId) {
//         // Call the new repository method
//         return campusRepository.findActiveCampusesByZoneId(zoneId).stream()
//                 // .distinct() might be useful here to ensure unique campuses if a campus is
//                 // linked to multiple active zonal accountants in the same zone
//                 .distinct().map(campus -> new GenericDropdownDTO(campus.getCampusId(), campus.getCampusName()))
//                 .collect(Collectors.toList());
//     }

//     @Cacheable(cacheNames = "campusforzonalaccountant", key = "#empId")
//     public List<GenericDropdownDTO> getCampusesByEmployeeId(int empId) {
//         List<Integer> zoneIds = zonalAccountantRepository.findZoneIdByEmployeeId(empId);
//         if (zoneIds == null || zoneIds.isEmpty()) {
//             return Collections.emptyList();
//         }

//         return zoneIds.stream().flatMap(zoneId -> getCampusesByZoneId(zoneId).stream()).distinct()
//                 .collect(Collectors.toList());
//     }

//     public List<GenericDropdownDTO> getCampusesByEmployeeIdAndCategory(int empId, String category) {

//         List<Integer> zoneIds = zonalAccountantRepository.findZoneIdByEmployeeId(empId);

//         if (zoneIds == null || zoneIds.isEmpty()) {
//             return Collections.emptyList();
//         }

//         List<GenericDropdownDTO> allCampuses = zoneIds.stream()
//                 .flatMap(zoneId -> getCampusesByZoneId(zoneId).stream())
//                 .distinct()
//                 .collect(Collectors.toList());

//         // ============================
//         // 🔥 CATEGORY FILTERING LOGIC
//         // ============================
//         if (category == null || category.isEmpty()) {
//             return allCampuses; // no filter applied
//         }

//         String cat = category.trim().toLowerCase();

//         return allCampuses.stream()
//                 .filter(c -> {
//                     Campus campus = campusRepository.findById(c.getId()).orElse(null);
//                     if (campus == null || campus.getBusinessType() == null)
//                         return false;

//                     String type = campus.getBusinessType().getBusinessTypeName().toLowerCase();

//                     if (cat.equals("school")) {
//                         return type.contains("school");
//                     } else if (cat.equals("college")) {
//                         return type.contains("college");
//                     }
//                     return true;
//                 })
//                 .collect(Collectors.toList());
//     }

//     public List<GenericDropdownDTO> getActiveCampusesByEmpId(Integer empId) {
//         List<Dgm> dgms = dgmRepository.findByEmpId(empId);

//         // Map to GenericDropdownDTO — filtering only active campuses
//         return dgms.stream().filter(
//                 d -> d.getCampus() != null && d.getCampus().getIsActive() != null && d.getCampus().getIsActive() == 1)
//                 .map(d -> new GenericDropdownDTO(d.getCampus().getCampusId(), d.getCampus().getCampusName()))
//                 .collect(Collectors.toList());
//     }

//     public List<GenericDropdownDTO> getActiveCampusesByEmpIdAndCategory(Integer empId, String category) {

//         List<Dgm> dgms = dgmRepository.findByEmpId(empId);

//         // Step 1 → Get all active campuses for this DGM
//         List<GenericDropdownDTO> campusList = dgms.stream()
//                 .filter(d -> d.getCampus() != null &&
//                         d.getCampus().getIsActive() != null &&
//                         d.getCampus().getIsActive() == 1)
//                 .map(d -> new GenericDropdownDTO(
//                         d.getCampus().getCampusId(),
//                         d.getCampus().getCampusName()))
//                 .collect(Collectors.toList());

//         // Step 2 → If no category, return same as before
//         if (category == null || category.trim().isEmpty()) {
//             return campusList;
//         }

//         // Step 3 → Filter by category
//         String cat = category.trim().toLowerCase();

//         return campusList.stream()
//                 .filter(c -> matchBusinessType(c.getId(), cat)) // reused helper from earlier
//                 .collect(Collectors.toList());
//     }

//     public List<GenericDropdownDTO> getActiveCampusesByEmployeeId(int empId) {
//         List<Integer> zoneIds = zonalAccountantRepository.findZoneIdByEmployeeId(empId);
//         if (zoneIds == null || zoneIds.isEmpty()) {
//             return Collections.emptyList();
//         }

//         // Step 2: Get campus IDs for all zones
//         List<Integer> campusIds = dgmRepository.findCampusIdsByZoneIds(zoneIds);
//         if (campusIds == null || campusIds.isEmpty()) {
//             return Collections.emptyList();
//         }

//         // Step 3: Get only active campuses
//         return campusRepository.findActiveCampusesByIds(campusIds);
//     }

//     public List<GenericDropdownDTO> getActiveCampusesByEmployeeIdAndCategory(int empId, String category) {

//         // Step 1 → Find zones for this zonal accountant
//         List<Integer> zoneIds = zonalAccountantRepository.findZoneIdByEmployeeId(empId);
//         if (zoneIds == null || zoneIds.isEmpty()) {
//             return Collections.emptyList();
//         }

//         // Step 2 → Get campuses mapped under DGM for those zones
//         List<Integer> campusIds = dgmRepository.findCampusIdsByZoneIds(zoneIds);
//         if (campusIds == null || campusIds.isEmpty()) {
//             return Collections.emptyList();
//         }

//         // Step 3 → Get only active campuses (already returns GenericDropdownDTO)
//         List<GenericDropdownDTO> campuses = campusRepository.findActiveCampusesByIds(campusIds);

//         // Step 4 → If category not provided → return as is
//         if (category == null || category.trim().isEmpty()) {
//             return campuses;
//         }

//         // Step 5 → Filter by business type (school/college)
//         String cat = category.trim().toLowerCase();

//         return campuses.stream()
//                 .filter(c -> matchBusinessType(c.getId(), cat))
//                 .collect(Collectors.toList());
//     }

//     public List<GenericDropdownDTO> getDgmCampusesByZoneAndCategory(Integer zoneId, String category) {
//         Integer valid = zonalAccountantRepository.validateZone(zoneId);
//         if (valid == null || valid == 0) {
//             return Collections.emptyList();
//         }
//         List<Campus> campuses = dgmRepository.findCampusesByZone(zoneId);
//         if (campuses == null || campuses.isEmpty()) {
//             return Collections.emptyList();
//         }
//         List<Campus> filtered = campuses.stream()
//                 .filter(c -> filterByCategory(c, category))
//                 .collect(Collectors.toList());
//         return filtered.stream()
//                 .map(c -> new GenericDropdownDTO(c.getCampusId(), c.getCampusName()))
//                 .collect(Collectors.toList());
//     }

//     private boolean filterByCategory(Campus campus, String category) {

//         if (category == null || category.isBlank()) {
//             return true;
//         }

//         if (campus.getBusinessType() == null) {
//             return false;
//         }

//         String type = campus.getBusinessType().getBusinessTypeName().toLowerCase();

//         switch (category.toLowerCase()) {
//             case "school":
//                 return type.contains("school");
//             case "college":
//                 return type.contains("college");
//             default:
//                 return true;
//         }
//     }

//     private boolean matchBusinessType(Integer campusId, String category) {

//         Campus campus = campusRepository.findById(campusId).orElse(null);
//         if (campus == null || campus.getBusinessType() == null) {
//             return false;
//         }

//         String businessName = campus.getBusinessType().getBusinessTypeName().toLowerCase();

//         switch (category) {
//             case "school":
//                 return businessName.contains("school");
//             case "college":
//                 return businessName.contains("college");
//             default:
//                 return true; // Others → no filter
//         }
//     }

//     // @Cacheable("issuedToTypes")
//     public List<GenericDropdownDTO> getAllIssuedToTypes() {
//         return appIssuedTypeRepository.findAll().stream()
//                 .map(type -> new GenericDropdownDTO(type.getAppIssuedId(), type.getTypeName()))
//                 .collect(Collectors.toList());
//     }

//     @Cacheable(value = "mobileNumberByEmpId", key = "#empId")
//     public String getMobileNumberByEmpId(int empId) {
//         return employeeRepository.findMobileNoByEmpId(empId);
//     }

//     @Cacheable(cacheNames = "getDgmforCampus", key = "#campusId")
//     public List<GenericDropdownDTO> getDgmEmployeesForCampus(int campusId) {
//         // Find distinct DGM employees for that Campus, checking isActive = 1
//         // Filter by campusId directly with d.isActive = 1 and e.isActive = 1
//         return dgmRepository.findDistinctActiveEmployeesByCampusId(campusId);
//     }

//     public List<Double> getApplicationFees(int empId, int academicYearId) { // UPDATED SIGNATURE

//         // 1. Check AdminApp table first (UPDATED CALL)
//         List<Double> adminFees = adminAppRepository.findAmountsByEmpIdAndAcademicYear(empId, academicYearId);

//         // 2. If AdminApp has data, convert to Double and return
//         if (adminFees != null && !adminFees.isEmpty()) {
//             return adminFees.stream()
//                     .map(Double::valueOf)
//                     .collect(Collectors.toList());
//         }

//         // 3. If AdminApp is empty, check BalanceTrack table (UPDATED CALL)
//         List<Float> balanceFees = balanceTrackRepository.findAmountsByEmpIdAndAcademicYear(empId, academicYearId);

//         if (balanceFees != null && !balanceFees.isEmpty()) {
//             return balanceFees.stream()
//                     .map(Double::valueOf)
//                     .collect(Collectors.toList());
//         }

//         // 4. If both are empty, return an empty list
//         return Collections.emptyList();
//     }

//     public LocationAutoFillDTO getAutoPopulateData(int empId, String category) {

//         // 1️⃣ Only apply logic when category = "school"
//         if (!"school".equalsIgnoreCase(category)) {
//             return null;
//         }

//         // 2️⃣ Get active DGM record for employee
//         Dgm dgm = dgmRepository
//                 .findActiveDgm(empId, 1)
//                 .orElse(null);

//         if (dgm == null) {
//             return null;
//         }

//         // 3️⃣ DISTRICT (direct from Dgm table)
//         District district = dgm.getDistrict();

//         Integer districtId = district != null ? district.getDistrictId() : null;
//         String districtName = district != null ? district.getDistrictName() : null;

//         // 4️⃣ CITY (via Campus)
//         Campus campus = dgm.getCampus();
//         City city = (campus != null) ? campus.getCity() : null;

//         Integer cityId = city != null ? city.getCityId() : null;
//         String cityName = city != null ? city.getCityName() : null;

//         // 5️⃣ Return final DTO
//         return new LocationAutoFillDTO(cityId, cityName, districtId, districtName);
//     }

//     /**
//      * Get employee location details based on employee ID and campus category
//      * Checks if employee's cmps_category matches the provided category
//      * If match, returns campus, city, state, zone, and district information
//      */
//     public EmployeeLocationDTO getEmployeeLocationByCategory(int employeeId, String cmpsCategory) {
//         // 1. Get employee from view
//         List<SCEmployeeEntity> employees = scEmployeeRepository.findByEmpId(employeeId);
//         if (employees == null || employees.isEmpty()) {
//             return null;
//         }

//         SCEmployeeEntity employee = employees.get(0);

//         // 2. Check if cmps_category matches (case-insensitive)
//         String employeeCategory = employee.getCmpsCategory();
//         if (employeeCategory == null || !employeeCategory.equalsIgnoreCase(cmpsCategory)) {
//             return null; // Category doesn't match
//         }

//         // 3. Get campus ID and name from employee view
//         int campusId = employee.getEmpCampusId();
//         String campusName = employee.getCampusName();

//         if (campusId <= 0) {
//             return null; // Invalid campus ID
//         }

//         // 4. Get campus from database
//         Optional<Campus> campusOptional = campusRepository.findById(campusId);
//         if (campusOptional.isEmpty()) {
//             return null;
//         }

//         Campus campus = campusOptional.get();

//         // 5. Get city, state, zone from campus
//         City city = campus.getCity();
//         State state = campus.getState();
//         Zone zone = campus.getZone();

//         Integer cityId = city != null ? city.getCityId() : null;
//         String cityName = city != null ? city.getCityName() : null;

//         Integer stateId = state != null ? state.getStateId() : null;
//         String stateName = state != null ? state.getStateName() : null;

//         Integer zoneId = zone != null ? zone.getZoneId() : null;
//         String zoneName = zone != null ? zone.getZoneName() : null;

//         // 6. Get district from city
//         District district = city != null ? city.getDistrict() : null;
//         Integer districtId = district != null ? district.getDistrictId() : null;
//         String districtName = district != null ? district.getDistrictName() : null;

//         // 7. Return DTO with all information
//         return new EmployeeLocationDTO(
//                 campusId,
//                 campusName,
//                 cityId,
//                 cityName,
//                 stateId,
//                 stateName,
//                 zoneId,
//                 zoneName,
//                 districtId,
//                 districtName);
//     }

//     /**
//      * Get DGM names and their associated campuses based on employee ID
//      * Returns a list of DGM records with their campuses for the given employee
//      */
//     public List<DgmWithCampusesDTO> getDgmWithCampusesByEmployeeId(Integer employeeId) {
//         // 1. Check if the employee is a Zonal Accountant (Has mapped zones)
//         List<Integer> zoneIds = zonalAccountantRepository.findZoneIdByEmployeeId(employeeId);

//         List<Dgm> dgmRecords;

//         if (zoneIds != null && !zoneIds.isEmpty()) {
//             // CASE A: Zonal Accountant - Fetch all Active DGMs in those Zones
//             dgmRecords = dgmRepository.findActiveDgmByZoneIds(zoneIds);
//         } else {
//             // CASE B: DGM (or other) - Fetch DGM records for this employee specifically
//             dgmRecords = dgmRepository.findDgmWithCampusesByEmployeeId(employeeId);
//         }

//         if (dgmRecords == null || dgmRecords.isEmpty()) {
//             return new ArrayList<>();
//         }

//         // Group by DGM employee (since one employee can have multiple DGM records for
//         // different campuses)
//         // We'll create a map to group campuses by DGM employee
//         java.util.Map<Integer, DgmWithCampusesDTO> dgmMap = new java.util.HashMap<>();

//         for (Dgm dgm : dgmRecords) {
//             if (dgm.getEmployee() == null)
//                 continue;

//             Integer empId = dgm.getEmployee().getEmp_id();
//             String dgmName = dgm.getEmployee().getFirst_name() + " " + dgm.getEmployee().getLast_name();

//             // Get or create DGM entry
//             DgmWithCampusesDTO dto = dgmMap.get(empId);
//             if (dto == null) {
//                 dto = new DgmWithCampusesDTO();
//                 dto.setId(empId);
//                 dto.setName(dgmName);
//                 dto.setCmpsId(new ArrayList<>());
//                 dgmMap.put(empId, dto);
//             }

//             // Add campus ID to the list (avoid duplicates)
//             if (dgm.getCampus() != null) {
//                 Integer campusId = dgm.getCampus().getCampusId();

//                 // Check if campus ID already exists in the list
//                 if (!dto.getCmpsId().contains(campusId)) {
//                     dto.getCmpsId().add(campusId);
//                 }
//             }
//         }

//         return new ArrayList<>(dgmMap.values());
//     }

//     // public Optional<AppFromDTO> getAppFromByEmployeeAndYear(int employeeId, int
//     // academicYearId) {
//     // return balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(employeeId,
//     // academicYearId);
//     //
//     // // OR if using the @Query method:
//     // // return
//     // balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(employeeId,
//     // // academicYearId);
//     // }
//     //
//     //// @Cacheable(cacheNames = "getAppRange", key = "{#academicYearId,
//     // #employeeId}")
//     // public AppRangeDTO getAppRange(int empId, int academicYearId) {
//     // // Fetch distribution data
//     // AppDistributionDTO distDTO = distributionRepository
//     // .findActiveAppRangeByEmployeeAndAcademicYear(empId,
//     // academicYearId).orElse(null);
//     //
//     // // Fetch balance track data (now returns AppFromDTO with the ID)
//     // AppFromDTO fromDTO =
//     // balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(empId,
//     // academicYearId)
//     // .orElse(null);
//     //
//     // if (distDTO == null && fromDTO == null) {
//     // return null;
//     // }
//     //
//     // // Merge results into a single DTO
//     // Integer appStartNo = distDTO != null ? distDTO.getAppStartNo() : null;
//     // Integer appEndNo = distDTO != null ? distDTO.getAppEndNo() : null;
//     //
//     // // Extract fields from the updated AppFromDTO
//     // Integer appFrom = fromDTO != null ? fromDTO.getAppFrom() : null;
//     // Integer appBalanceTrkId = fromDTO != null ? fromDTO.getAppBalanceTrkId() :
//     // null; // Extracted new ID
//     //
//     // // Use the updated AppRangeDTO constructor
//     // return new AppRangeDTO(appStartNo, appEndNo, appFrom, appBalanceTrkId);
//     // }
//     //
//     // public AppRangeDTO getAppRange(int empId, int academicYearId, Integer cityId)
//     // { // Updated signature
//     // // Fetch distribution data
//     // AppDistributionDTO distDTO = distributionRepository
//     // .findActiveAppRangeByEmployeeAndAcademicYear(empId, academicYearId, cityId)
//     // // Pass cityId
//     // .orElse(null);
//     //
//     // // Fetch balance track data (now returns AppFromDTO with the ID)
//     // AppFromDTO fromDTO =
//     // balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(empId,
//     // academicYearId)
//     // .orElse(null);
//     //
//     // if (distDTO == null && fromDTO == null) {
//     // return null;
//     // }
//     //
//     // // Merge results into a single DTO
//     // Integer appStartNo = distDTO != null ? distDTO.getAppStartNo() : null;
//     // Integer appEndNo = distDTO != null ? distDTO.getAppEndNo() : null;
//     //
//     // // Extract fields from the updated AppFromDTO
//     // Integer appFrom = fromDTO != null ? fromDTO.getAppFrom() : null;
//     // Integer appBalanceTrkId = fromDTO != null ? fromDTO.getAppBalanceTrkId() :
//     // null; // Extracted new ID
//     //
//     // // Use the updated AppRangeDTO constructor
//     // return new AppRangeDTO(appStartNo, appEndNo, appFrom, appBalanceTrkId);
//     // }

//     // ... inside DgmService class ...

//     // Updated Method to use Double - First check BalanceTrack, then AdminApp (for
//     // employees only)
//     public List<AppSeriesDTO> getActiveSeriesForReceiver(int receiverId, int academicYearId, Double amount,
//             boolean isPro) {

//         // 1. FIRST: Try to fetch Series List from BalanceTrack with academicYearId
//         // filter
//         List<AppSeriesDTO> seriesList;
//         if (isPro) {
//             // For PRO: Only check BalanceTrack (AdminApp doesn't have PRO data)
//             seriesList = balanceTrackRepository.findSeriesByProIdYearAndAmount(receiverId, academicYearId, amount);

//             // If BalanceTrack has data, enrich with master info and return
//             if (seriesList != null && !seriesList.isEmpty()) {
//                 // Fetch Master Info from AdminApp
//                 List<AdminApp> masterRecords = adminAppRepository.findMasterRecordByYearAndAmount(
//                         academicYearId, amount);

//                 // Enrich DTOs with master info
//                 if (!masterRecords.isEmpty()) {
//                     AdminApp master = masterRecords.get(0);
//                     int mStart = master.getAppFromNo();
//                     int mEnd = master.getAppToNo();

//                     for (AppSeriesDTO dto : seriesList) {
//                         dto.setMasterStartNo(mStart);
//                         dto.setMasterEndNo(mEnd);
//                     }
//                 }
//             }
//             // Return BalanceTrack data or empty list for PRO
//             return seriesList != null ? seriesList : new ArrayList<>();

//         } else {
//             // For Employee: Check BalanceTrack first, then fallback to AdminApp
//             seriesList = balanceTrackRepository.findSeriesByEmpIdYearAndAmount(receiverId, academicYearId, amount);

//             // 2. If BalanceTrack has data, enrich with master info and return
//             if (seriesList != null && !seriesList.isEmpty()) {
//                 Integer mStart = null;
//                 Integer mEnd = null;

//                 // STEP 1: Check AdminApp first (only for Admins/CO)
//                 List<AdminApp> masterRecords = adminAppRepository.findMasterRecordByYearAndAmountAndEmployee(
//                         receiverId, academicYearId, amount);

//                 if (!masterRecords.isEmpty()) {
//                     // Admin/CO: Get master from AdminApp table
//                     AdminApp master = masterRecords.get(0);
//                     mStart = master.getAppFromNo();
//                     mEnd = master.getAppToNo();

//                     if (mStart == null || mEnd == null) {
//                         System.out.println("WARNING: AdminApp master record found for empId=" + receiverId +
//                                 ", yearId=" + academicYearId + ", amount=" + amount +
//                                 " but appFromNo or appToNo is null. appFromNo=" + mStart + ", appToNo=" + mEnd);
//                         mStart = null;
//                         mEnd = null;
//                     } else {
//                         System.out.println("INFO: Admin/CO employee - Master range from AdminApp: " +
//                                 mStart + " to " + mEnd + " (receiverId=" + receiverId + ")");
//                     }
//                 }

//                 // STEP 2: If AdminApp not found, get from Distribution table (for Zone/DGM
//                 // level)
//                 // Zone/DGM employees don't have AdminApp records, so match each series to its
//                 // distribution
//                 if (mStart == null || mEnd == null) {
//                     // Get ALL distributions (including inactive) to find original distribution
//                     // before split
//                     // Original distributions are marked inactive when split, but we need them for
//                     // master range
//                     List<Distribution> receivedDistributions = distributionRepository
//                             .findAllByIssuedToEmpIdAndAmountIncludingInactive(
//                                     receiverId, academicYearId, amount);

//                     if (!receivedDistributions.isEmpty()) {
//                         // For each series, find the ORIGINAL distribution that contains this series
//                         // range
//                         // (Not the remainder - the original distribution before it was split)
//                         for (AppSeriesDTO dto : seriesList) {
//                             // Find ALL distributions that contain this series range
//                             List<Distribution> containingDists = new ArrayList<>();
//                             for (Distribution dist : receivedDistributions) {
//                                 // Check if the series range falls within this distribution
//                                 // Series: dto.startNo to dto.endNo
//                                 // Distribution: dist.appStartNo to dist.appEndNo
//                                 if (dto.getStartNo() >= dist.getAppStartNo() && dto.getEndNo() <= dist.getAppEndNo()
//                                         && dist.getIsActive() == 1) {
//                                     containingDists.add(dist);
//                                     System.out.println("DEBUG: Series " + dto.getStartNo() + "-" + dto.getEndNo() +
//                                             " is contained in Distribution " + dist.getAppStartNo() + "-"
//                                             + dist.getAppEndNo() +
//                                             " (distId=" + dist.getAppDistributionId() + ", isActive="
//                                             + dist.getIsActive() +
//                                             ", range=" + (dist.getAppEndNo() - dist.getAppStartNo()) + ")");
//                                 }
//                             }

//                             System.out.println(
//                                     "DEBUG: Found " + containingDists.size() + " distributions containing series " +
//                                             dto.getStartNo() + "-" + dto.getEndNo());

//                             Distribution matchingDist = null;
//                             if (!containingDists.isEmpty()) {
//                                 // Among all distributions that contain this series, pick the ORIGINAL one
//                                 // Strategy: Find the one with the WIDEST range (largest end - start)
//                                 // Original distributions have wider ranges, remainders are smaller
//                                 // If ranges are equal, pick the one with LOWEST ID (earliest created)
//                                 matchingDist = containingDists.stream()
//                                         .max((d1, d2) -> {
//                                             // First compare by range width (end - start)
//                                             int range1 = d1.getAppEndNo() - d1.getAppStartNo();
//                                             int range2 = d2.getAppEndNo() - d2.getAppStartNo();
//                                             int rangeCompare = Integer.compare(range1, range2);
//                                             if (rangeCompare != 0) {
//                                                 return rangeCompare; // Wider range wins
//                                             }
//                                             // If ranges are equal, pick lower ID (earliest)
//                                             int id1 = d1.getAppDistributionId() != null ? d1.getAppDistributionId()
//                                                     : Integer.MAX_VALUE;
//                                             int id2 = d2.getAppDistributionId() != null ? d2.getAppDistributionId()
//                                                     : Integer.MAX_VALUE;
//                                             return Integer.compare(id2, id1); // Lower ID wins (reverse compare)
//                                         })
//                                         .orElse(null);
//                             }

//                             if (matchingDist != null) {
//                                 // Use the ORIGINAL distribution's range as master range for this series
//                                 dto.setMasterStartNo(matchingDist.getAppStartNo());
//                                 dto.setMasterEndNo(matchingDist.getAppEndNo());
//                                 System.out.println("INFO: Series " + dto.getStartNo() + "-" + dto.getEndNo() +
//                                         " matched to ORIGINAL Distribution " + matchingDist.getAppStartNo() + "-"
//                                         + matchingDist.getAppEndNo() +
//                                         " (distId=" + matchingDist.getAppDistributionId() + ")");
//                             } else {
//                                 // If no match found, use the first distribution as fallback
//                                 Distribution firstDist = receivedDistributions.get(0);
//                                 dto.setMasterStartNo(firstDist.getAppStartNo());
//                                 dto.setMasterEndNo(firstDist.getAppEndNo());
//                                 System.out.println("WARNING: No matching distribution found for series "
//                                         + dto.getStartNo() + "-" + dto.getEndNo() +
//                                         ", using first distribution as fallback: " + firstDist.getAppStartNo() + "-"
//                                         + firstDist.getAppEndNo());
//                             }
//                         }
//                     } else {
//                         System.out.println("WARNING: No distributions found for receiverId=" + receiverId +
//                                 ", academicYearId=" + academicYearId + ", amount=" + amount);
//                     }
//                 } else {
//                     // Admin/CO: Use same master range for all series
//                     for (AppSeriesDTO dto : seriesList) {
//                         dto.setMasterStartNo(mStart);
//                         dto.setMasterEndNo(mEnd);
//                     }
//                 }

//                 return seriesList;
//             }

//             // 3. If BalanceTrack is empty, fetch from AdminApp table (for employee)
//             List<AdminApp> adminAppRecords = adminAppRepository.findAllByEmpAndYearAndAmount(
//                     receiverId, academicYearId, amount);

//             // 4. Convert AdminApp records to AppSeriesDTO format
//             List<AppSeriesDTO> adminAppSeriesList = new ArrayList<>();
//             if (adminAppRecords != null && !adminAppRecords.isEmpty()) {
//                 for (AdminApp adminApp : adminAppRecords) {
//                     if (adminApp.getAppFromNo() != null && adminApp.getAppToNo() != null) {
//                         String displaySeries = adminApp.getAppFromNo() + " - " + adminApp.getAppToNo();
//                         int availableCount = adminApp.getTotalApp() != null ? adminApp.getTotalApp() : 0;

//                         AppSeriesDTO dto = new AppSeriesDTO(
//                                 displaySeries,
//                                 adminApp.getAppFromNo(),
//                                 adminApp.getAppToNo(),
//                                 availableCount);

//                         // Set master info (same as start/end for AdminApp)
//                         dto.setMasterStartNo(adminApp.getAppFromNo());
//                         dto.setMasterEndNo(adminApp.getAppToNo());

//                         adminAppSeriesList.add(dto);
//                     }
//                 }
//             }

//             return adminAppSeriesList;
//         }
//     }

//     public Integer getDistributionIdBySeries(int receiverId, int start, int end, Double amount, boolean isPro) {
//         if (isPro) {
//             return distributionRepository.findIdByProAndRange(receiverId, start, end, amount)
//                     .orElseThrow(() -> new RuntimeException("No Active Distribution found for this PRO range."));
//         } else {
//             return distributionRepository.findIdByEmpAndRange(receiverId, start, end, amount)
//                     .orElseThrow(() -> new RuntimeException("No Active Distribution found for this Employee range."));
//         }
//     }

//     // Generic helper to find the highest priority Role ID for ANY employee (Issuer
//     // or Receiver)
//     private int getRoleTypeIdByEmpId(int empId) {
//         List<UserAdminView> userRoles = userAdminViewRepository.findRolesByEmpId(empId);

//         if (userRoles.isEmpty()) {
//             // If the receiver has no role, they might be a basic employee.
//             // You might want a default ID (e.g., 4) or throw an error.
//             // For DGM Service, we expect them to be DGM (3).
//             throw new RuntimeException("No valid roles found for Employee ID: " + empId);
//         }

//         int highestPriorityTypeId = Integer.MAX_VALUE;
//         for (UserAdminView userView : userRoles) {
//             String roleName = userView.getRole_name().trim().toUpperCase();
//             int currentTypeId = switch (roleName) {
//                 case "ADMIN" -> 1;
//                 case "ZONAL ACCOUNTANT" -> 2;
//                 case "DGM" -> 3;
//                 // Add "PRO" or "AGENT" -> 4 if needed
//                 default -> -1;
//             };
//             if (currentTypeId != -1 && currentTypeId < highestPriorityTypeId) {
//                 highestPriorityTypeId = currentTypeId;
//             }
//         }

//         if (highestPriorityTypeId == Integer.MAX_VALUE) {
//             // Fallback or Error. If DGM service, maybe default to 3?
//             return 3; // Defaulting to DGM if role logic is strict
//         }
//         return highestPriorityTypeId;
//     }

//     private int getIssuedTypeByUserId(int userId) {
//         List<UserAdminView> userRoles = userAdminViewRepository.findRolesByEmpId(userId);
//         if (userRoles.isEmpty())
//             throw new RuntimeException("No roles found for ID: " + userId);

//         int highestPriorityTypeId = Integer.MAX_VALUE;
//         for (UserAdminView userView : userRoles) {
//             String roleName = userView.getRole_name().trim().toUpperCase();
//             int currentTypeId = switch (roleName) {
//                 case "ADMIN" -> 1;
//                 case "ZONAL ACCOUNTANT" -> 2;
//                 case "DGM" -> 3;
//                 default -> -1;
//             };
//             if (currentTypeId != -1 && currentTypeId < highestPriorityTypeId) {
//                 highestPriorityTypeId = currentTypeId;
//             }
//         }
//         return highestPriorityTypeId;
//     }

//     @Transactional
//     public synchronized void submitForm(@NonNull FormSubmissionDTO formDto) {
//         int issuerUserId = formDto.getUserId();
//         int receiverEmpId = formDto.getDgmEmployeeId();

//         // NEW VALIDATION: Check if sender actually owns the requested application range
//         // and amount
//         validateSenderHasAvailableRange(formDto);

//         // Validate Receiver is Active
//         com.application.entity.Employee receiverEmp = employeeRepository.findById(receiverEmpId)
//                 .orElseThrow(() -> new RuntimeException("Receiver Employee not found"));

//         if (receiverEmp.getIsActive() != 1) {
//             throw new RuntimeException("Transaction Failed: The selected Receiver is Inactive.");
//         }

//         // 1. AUTO-DETECT TYPES (Backend Logic)
//         int issuedById = getRoleTypeIdByEmpId(issuerUserId); // Who is sending?
//         int issuedToId = getRoleTypeIdByEmpId(receiverEmpId); // Who is receiving?

//         // 2. Check Overlaps
//         int startNo = Integer.parseInt(formDto.getApplicationNoFrom());
//         int endNo = Integer.parseInt(formDto.getApplicationNoTo());

//         // --- DUPLICATE CHECK START ---
//         // Prevents double submission for EXACT range
//         // List<Distribution> existing =
//         // distributionRepository.findOverlappingDistributions(
//         // formDto.getAcademicYearId(), startNo, endNo);
//         // If overlapping dists exist, we normally allow logic to handle splits.
//         // BUT strict duplicate check:
//         // logic handled by 'validateSenderHasAvailableRange' if synchronized.
//         // If we are synchronized, the first thread will move the apps. The second will
//         // fail validation.
//         // We will rely on synchronization + validation.

//         List<Distribution> overlappingDists = distributionRepository.findOverlappingDistributions(
//                 formDto.getAcademicYearId(), startNo, endNo);

//         if (!overlappingDists.isEmpty()) {
//             handleOverlappingDistributions(overlappingDists, formDto);
//         }

//         // 3. Create & Map (Pass BOTH types now)
//         Distribution distribution = new Distribution();
//         mapDtoToDistribution(distribution, formDto, issuedById, issuedToId);

//         distribution.setIssued_to_emp_id(receiverEmpId);
//         distribution.setIssued_to_pro_id(null);

//         // 4. Save & Flush
//         System.out.println("=== DISTRIBUTION SAVE (NEW) ===");
//         System.out.println("Operation: CREATE NEW DISTRIBUTION");
//         System.out.println("Range: " + distribution.getAppStartNo() + " - " + distribution.getAppEndNo());
//         System.out.println("Receiver EmpId: " + distribution.getIssued_to_emp_id());
//         System.out.println("Receiver ProId: " + distribution.getIssued_to_pro_id());
//         System.out.println("Amount: " + distribution.getAmount());
//         System.out.println("Created By: " + distribution.getCreated_by());
//         System.out.println("Academic Year: " + distribution.getAcademicYear().getAcdcYearId());
//         Distribution savedDist = distributionRepository.saveAndFlush(distribution);
//         System.out.println("Saved Distribution ID: " + savedDist.getAppDistributionId());
//         System.out.println("=================================");

//         // CRITICAL: Flush any pending remainders created in
//         // handleOverlappingDistributions
//         // This ensures they are visible when rebuilding the sender's balance
//         distributionRepository.flush();

//         // 5. Recalculate Balances
//         int stateId = savedDist.getState().getStateId();
//         Float amount = formDto.getApplication_Amount();

//         // A. Update Issuer (Sender) - CRITICAL: Always recalculate sender's balance
//         System.out.println("DEBUG DGM: Recalculating sender balance for issuer: " + issuerUserId);
//         recalculateBalanceForEmployee(issuerUserId, formDto.getAcademicYearId(), stateId, issuedById, issuerUserId,
//                 amount);

//         // CRITICAL: Flush balance updates to ensure they're persisted
//         balanceTrackRepository.flush();

//         // B. Update Receiver (Pass the calculated issuedToId)
//         addStockToReceiver(savedDist, formDto.getAcademicYearId(), issuedToId, issuerUserId, amount);
//     }

//     @Transactional
//     public void updateForm(@NonNull Integer distributionId, @NonNull FormSubmissionDTO formDto) {

//         // 1. Fetch Existing Record
//         Distribution existingDistribution = distributionRepository.findById(distributionId)
//                 .orElseThrow(() -> new RuntimeException("Distribution record not found with ID: " + distributionId));

//         // 2. Extract Immutable Data (Preserve Amount & State!)
//         Float originalAmount = existingDistribution.getAmount();
//         int issuerId = formDto.getUserId();
//         int academicYearId = formDto.getAcademicYearId();
//         int stateId = existingDistribution.getState().getStateId();

//         // 3. Identify Changes
//         int oldReceiverId = existingDistribution.getIssued_to_emp_id();
//         int newReceiverId = formDto.getDgmEmployeeId();
//         boolean isRecipientChanging = oldReceiverId != newReceiverId;

//         int oldStart = (int) existingDistribution.getAppStartNo();
//         int oldEnd = (int) existingDistribution.getAppEndNo();
//         int newStart = Integer.parseInt(formDto.getApplicationNoFrom());
//         int newEnd = Integer.parseInt(formDto.getApplicationNoTo());
//         boolean isRangeChanging = oldStart != newStart || oldEnd != newEnd;

//         // 4. AUTO-DETECT TYPES (Backend Logic)
//         // We calculate these from the UserAdminView, we do NOT trust the frontend.
//         int issuedById = getRoleTypeIdByEmpId(issuerId);
//         int issuedToId = getRoleTypeIdByEmpId(newReceiverId); // <--- Calculated Here

//         // -------------------------------------------------------
//         // STEP 5: ARCHIVE OLD & SAVE NEW
//         // -------------------------------------------------------

//         // A. Inactivate Old (Flush to ensure DB sees it as inactive immediately)
//         System.out.println("=== DISTRIBUTION UPDATE (INACTIVATE) ===");
//         System.out.println("Operation: INACTIVATE EXISTING DISTRIBUTION");
//         System.out.println("Distribution ID: " + existingDistribution.getAppDistributionId());
//         System.out.println(
//                 "Old Range: " + existingDistribution.getAppStartNo() + " - " + existingDistribution.getAppEndNo());
//         System.out.println("Old Receiver EmpId: " + existingDistribution.getIssued_to_emp_id());
//         System.out.println("Setting isActive = 0");
//         existingDistribution.setIsActive(0);
//         // Update timestamp when deactivating distribution
//         existingDistribution.setIssueDate(LocalDateTime.now());
//         distributionRepository.saveAndFlush(existingDistribution);
//         System.out.println("==========================================");

//         // B. Create New Record
//         Distribution newDist = new Distribution();
//         // Map basic fields (Date, Zone, etc.)
//         mapDtoToDistribution(newDist, formDto, issuedById, issuedToId);

//         // OVERRIDE with correct Logic:
//         newDist.setIssued_to_emp_id(newReceiverId); // DGM is always Employee
//         newDist.setIssued_to_pro_id(null);
//         newDist.setAmount(originalAmount); // CRITICAL: Preserve Original Amount

//         System.out.println("=== DISTRIBUTION UPDATE (CREATE NEW) ===");
//         System.out.println("Operation: CREATE NEW DISTRIBUTION (UPDATE)");
//         System.out.println("New Range: " + newDist.getAppStartNo() + " - " + newDist.getAppEndNo());
//         System.out.println("New Receiver EmpId: " + newDist.getIssued_to_emp_id());
//         System.out.println("Amount: " + newDist.getAmount());
//         System.out.println("Created By: " + newDist.getCreated_by());
//         distributionRepository.saveAndFlush(newDist); // Flush new record
//         System.out.println("New Distribution ID: " + newDist.getAppDistributionId());
//         System.out.println("==========================================");

//         // -------------------------------------------------------
//         // STEP 6: HANDLE REMAINDERS (If Range Shrank)
//         // -------------------------------------------------------
//         if (isRangeChanging) {
//             // Leftover BEFORE the new range -> Stays with OLD Receiver
//             if (oldStart < newStart) {
//                 createAndSaveRemainder(existingDistribution, oldStart, newStart - 1);
//             }
//             // Leftover AFTER the new range -> Stays with OLD Receiver
//             if (oldEnd > newEnd) {
//                 createAndSaveRemainder(existingDistribution, newEnd + 1, oldEnd);
//             }
//         }

//         // CRITICAL: Flush any pending remainders to ensure they are visible
//         distributionRepository.flush();

//         // -------------------------------------------------------
//         // STEP 7: RECALCULATE BALANCES
//         // -------------------------------------------------------

//         // A. Update Issuer (Zone Officer or CO) - CRITICAL: Always recalculate sender's
//         // balance
//         System.out.println("DEBUG DGM UPDATE: Recalculating sender balance for issuer: " + issuerId);
//         recalculateBalanceForEmployee(issuerId, academicYearId, stateId, issuedById, issuerId, originalAmount);

//         // CRITICAL: Flush balance updates to ensure they're persisted
//         balanceTrackRepository.flush();

//         // B. Update New Receiver (DGM)
//         // We use the calculated 'issuedToId' variable here
//         recalculateBalanceForEmployee(
//                 newReceiverId,
//                 academicYearId,
//                 stateId,
//                 issuedToId, // <--- FIX: Using local variable
//                 issuerId,
//                 originalAmount);

//         // C. Update Old Receiver (If changed)
//         if (isRecipientChanging) {
//             // We must find the Type ID of the old receiver to call the method correctly
//             // Use LIST check to avoid crashes
//             java.util.List<BalanceTrack> oldBalances = balanceTrackRepository
//                     .findActiveBalancesByEmpAndAmount(academicYearId, oldReceiverId, originalAmount);

//             if (!oldBalances.isEmpty()) {
//                 BalanceTrack oldBalance = oldBalances.get(0);
//                 int oldTypeId = oldBalance.getIssuedByType().getAppIssuedId();

//                 recalculateBalanceForEmployee(oldReceiverId, academicYearId, stateId, oldTypeId, issuerId,
//                         originalAmount);
//             }
//         }
//     }

//     // ---------------------------------------------------------
//     // Smart Gap Detection Logic
//     // ---------------------------------------------------------
//     private void addStockToReceiver(Distribution savedDist, int academicYearId, int typeId, int createdBy,
//             Float amount) {
//         int newStart = savedDist.getAppStartNo();
//         int newEnd = savedDist.getAppEndNo();
//         int newCount = savedDist.getTotalAppCount();
//         int targetEnd = newStart - 1;
//         int receiverId = savedDist.getIssued_to_emp_id(); // DGM is Employee

//         Optional<BalanceTrack> mergeableRow = balanceTrackRepository.findMergeableRowForEmployee(
//                 academicYearId, receiverId, amount, targetEnd);

//         if (mergeableRow.isPresent()) {
//             // MERGE
//             BalanceTrack existing = mergeableRow.get();
//             existing.setAppTo(newEnd);
//             existing.setAppAvblCnt(existing.getAppAvblCnt() + newCount);
//             balanceTrackRepository.save(existing);
//         } else {
//             // NEW ROW
//             BalanceTrack newRow = createNewBalanceTrack(receiverId, academicYearId, typeId, createdBy);
//             newRow.setAmount(amount);
//             newRow.setAppFrom(newStart);
//             newRow.setAppTo(newEnd);
//             newRow.setAppAvblCnt(newCount);
//             balanceTrackRepository.save(newRow);
//         }
//     }

//     private BalanceTrack createNewBalanceTrack(int id, int acYear, int typeId, int createdBy, boolean isPro) {
//         BalanceTrack nb = new BalanceTrack();
//         nb.setAcademicYear(academicYearRepository.findById(acYear).orElseThrow());
//         nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
//         nb.setIsActive(1);
//         nb.setCreatedBy(createdBy);

//         // Strict Validation logic
//         if (isPro) {
//             nb.setIssuedToProId(id);
//             nb.setEmployee(null); // DB allows null now
//         } else {
//             nb.setEmployee(employeeRepository.findById(id).orElseThrow());
//             nb.setIssuedToProId(null);
//         }
//         return nb;
//     }

//     private void createAndSaveRemainder(Distribution originalDist, int start, int end) {
//         Distribution remainder = new Distribution();
//         mapExistingToNewDistribution(remainder, originalDist);
//         remainder.setIssued_to_emp_id(originalDist.getIssued_to_emp_id());
//         remainder.setAppStartNo(start);
//         remainder.setAppEndNo(end);
//         remainder.setTotalAppCount((end - start) + 1);
//         remainder.setIsActive(1);
//         remainder.setAmount(originalDist.getAmount());
//         System.out.println("=== DISTRIBUTION SAVE (REMAINDER) ===");
//         System.out.println("Operation: CREATE REMAINDER DISTRIBUTION");
//         System.out.println("Remainder Range: " + remainder.getAppStartNo() + " - " + remainder.getAppEndNo());
//         System.out.println("Original Dist ID: " + originalDist.getAppDistributionId());
//         System.out.println("Original Range: " + originalDist.getAppStartNo() + " - " + originalDist.getAppEndNo());
//         System.out.println("Receiver EmpId: " + remainder.getIssued_to_emp_id());
//         System.out.println("Amount: " + remainder.getAmount());
//         distributionRepository.saveAndFlush(remainder);
//         System.out.println("Remainder Distribution ID: " + remainder.getAppDistributionId());
//         System.out.println("======================================");
//     }

//     // --- PRIVATE HELPER METHODS ---

//     /**
//      * Revised to use inactivation and insertion instead of update/delete.
//      */
//     private void handleOverlappingDistributions(List<Distribution> overlappingDists, FormSubmissionDTO request) {
//         int newStart = Integer.parseInt(request.getApplicationNoFrom());
//         int newEnd = Integer.parseInt(request.getApplicationNoTo());

//         for (Distribution oldDist : overlappingDists) {
//             int oldReceiverId = oldDist.getIssued_to_emp_id();
//             if (oldReceiverId == request.getDgmEmployeeId())
//                 continue;

//             int oldStart = oldDist.getAppStartNo();
//             int oldEnd = oldDist.getAppEndNo();

//             // NEW LOGIC: Keep Distribution record completely unchanged (no inactivation, no
//             // remainders)
//             // Distribution table: Keep original record (1-100) as-is, active, unchanged
//             // BalanceTrack table: Will be updated via recalculateBalanceForEmployee to show
//             // remaining (51-100)
//             System.out.println("=== DISTRIBUTION (KEEP COMPLETELY UNCHANGED) ===");
//             System.out.println("Operation: KEEP DISTRIBUTION RECORD COMPLETELY UNCHANGED");
//             System.out.println("Distribution ID: " + oldDist.getAppDistributionId());
//             System.out.println("Original Range: " + oldStart + " - " + oldEnd + " (KEEPING AS-IS, NO CHANGES)");
//             System.out.println("New Distribution Range: " + newStart + " - " + newEnd);
//             System.out.println("Receiver EmpId: " + oldDist.getIssued_to_emp_id());
//             System.out
//                     .println("NOTE: Distribution record will NOT be modified at all (no inactivation, no remainders).");
//             System.out.println("NOTE: Only BalanceTrack will be updated to show remaining range (51-100).");

//             // Keep original distribution record completely unchanged - no modifications at
//             // all
//             // No inactivation, no remainder creation
//             // BalanceTrack will be calculated based on what's actually available

//             System.out.println("==============================================");

//             // CRITICAL: Flush remainders before recalculating balance
//             distributionRepository.flush();

//             // Recalculate Balance for Victim
//             recalculateBalanceForEmployee(
//                     oldReceiverId,
//                     request.getAcademicYearId(),
//                     oldDist.getState().getStateId(),
//                     oldDist.getIssuedToType().getAppIssuedId(),
//                     request.getUserId(),
//                     oldDist.getAmount());

//             // CRITICAL: Flush balance updates
//             balanceTrackRepository.flush();
//         }
//     }

//     // 2. Recalculate Logic (Updated for Amount & AdminApp)
//     private void recalculateBalanceForEmployee(int employeeId, int academicYearId, int stateId, int typeId,
//             int createdBy, Float amount) {

//         // 1. CHECK: Is this a CO/Admin? (Check Master Table)
//         Optional<AdminApp> adminApp = adminAppRepository.findByEmpAndYearAndAmount(
//                 employeeId, academicYearId, amount);

//         if (adminApp.isPresent()) {
//             // --- CASE A: CO / ADMIN (The Source) ---
//             AdminApp master = adminApp.get();

//             // FIX: Handle LIST return type
//             List<BalanceTrack> balances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(
//                     academicYearId, employeeId, amount);

//             BalanceTrack balance;
//             if (balances.isEmpty()) {
//                 balance = createNewBalanceTrack(employeeId, academicYearId, typeId, createdBy);
//                 balance.setAmount(amount);
//             } else {
//                 // Admins act as a single bucket, so we pick the first one
//                 balance = balances.get(0);
//             }

//             int totalDistributed = distributionRepository.sumTotalAppCountByCreatedByAndAmount(
//                     employeeId, academicYearId, amount).orElse(0);

//             // CRITICAL FIX: Calculate app_from based on what they've distributed
//             // If they've distributed from the beginning, app_from should be the next
//             // available number
//             // Otherwise, use the master start
//             Optional<Integer> maxDistributedEnd = distributionRepository
//                     .findMaxAppEndNoByCreatedByAndAmount(employeeId, academicYearId, amount);

//             int calculatedAppFrom;
//             if (maxDistributedEnd.isPresent() && maxDistributedEnd.get() >= master.getAppFromNo()) {
//                 // They've distributed from the beginning - app_from should be next available
//                 calculatedAppFrom = maxDistributedEnd.get() + 1;
//                 System.out.println("DEBUG DGM: Admin/CO distributed up to " + maxDistributedEnd.get() +
//                         ", setting app_from to " + calculatedAppFrom);
//             } else {
//                 // They haven't distributed from the beginning yet - use master start
//                 calculatedAppFrom = master.getAppFromNo();
//                 System.out.println("DEBUG DGM: Admin/CO hasn't distributed from beginning, using master start: "
//                         + calculatedAppFrom);
//             }

//             // Ensure app_from doesn't exceed master end
//             if (calculatedAppFrom > master.getAppToNo()) {
//                 calculatedAppFrom = master.getAppToNo() + 1; // All apps distributed
//             }

//             int availableCount = master.getTotalApp() - totalDistributed;
//             balance.setAppAvblCnt(availableCount);

//             // If available count is 0, set app_from = 0, app_to = 0, and is_active = 1
//             if (availableCount <= 0) {
//                 balance.setAppFrom(0);
//                 balance.setAppTo(0);
//             } else {
//                 balance.setAppFrom(calculatedAppFrom); // Use calculated start (next available or master start)
//                 balance.setAppTo(master.getAppToNo());
//             }
//             // Keep is_active = 1 even when available count is 0 (show as available 0)
//             balance.setIsActive(1);

//             balanceTrackRepository.saveAndFlush(balance);

//         } else {
//             // --- CASE B: INTERMEDIARIES (Zone/DGM) ---
//             // Rebuilds rows to match gaps exactly
//             rebuildBalancesFromDistributions(
//                     employeeId,
//                     academicYearId,
//                     typeId,
//                     createdBy,
//                     amount // <--- Ensure you convert Float to Double here
//             );
//         }
//     }

//     // --- HELPER: Rebuild Balance Rows (Calculates Available by Subtracting Given
//     // Away) ---
//     // NEW LOGIC: Calculate available balance by subtracting what was given away
//     // from what was received
//     private void rebuildBalancesFromDistributions(int empId, int acYearId, int typeId, int createdBy, Float amount) {

//         // CRITICAL: Clear persistence context
//         distributionRepository.flush();

//         // 1. Get ALL Active Distributions RECEIVED by this user
//         List<Distribution> allReceived = distributionRepository.findActiveHoldingsForEmp(empId, acYearId);

//         // 2. Filter by Amount
//         List<Distribution> received = allReceived.stream()
//                 .filter(d -> d.getAmount() != null && Math.abs(d.getAmount() - amount) < 0.01)
//                 .sorted((d1, d2) -> Long.compare(d1.getAppStartNo(), d2.getAppStartNo()))
//                 .toList();

//         // 3. Get ALL Distributions GIVEN AWAY by this user
//         List<Distribution> allGivenAway = distributionRepository.findByCreatedByAndYear(empId, acYearId);

//         // 4. Filter given away by amount
//         List<Distribution> givenAway = allGivenAway.stream()
//                 .filter(d -> d.getAmount() != null && Math.abs(d.getAmount() - amount) < 0.01)
//                 .sorted((d1, d2) -> Long.compare(d1.getAppStartNo(), d2.getAppStartNo()))
//                 .toList();

//         // 5. Get CURRENT Active Balance Rows for Reuse
//         // REUSE STRATEGY: Instead of deactivating all, we keep them in a list/queue
//         List<BalanceTrack> currentBalances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(acYearId, empId,
//                 amount);
//         // Use a LinkedList for easy removal/popping
//         java.util.LinkedList<BalanceTrack> reusePool = new java.util.LinkedList<>(currentBalances);

//         boolean atLeastOneActiveRowCreated = false;

//         // 7. Calculate remaining ranges
//         if (received.isEmpty()) {
//             System.out.println("WARNING DGM: No received distributions found for Employee " + empId + " with amount "
//                     + amount);
//             // We need a zero-balance active record
//             BalanceTrack nb;
//             if (!reusePool.isEmpty()) {
//                 nb = reusePool.poll(); // Reuse existing
//             } else {
//                 nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy);
//                 nb.setAmount(amount);
//             }
//             nb.setAppFrom(0);
//             nb.setAppTo(0);
//             nb.setAppAvblCnt(0);
//             nb.setIsActive(1); // CORRECT: Keep it active
//             balanceTrackRepository.saveAndFlush(nb);
//             atLeastOneActiveRowCreated = true;
//         } else {
//             for (Distribution receivedDist : received) {
//                 int receivedStart = (int) receivedDist.getAppStartNo();
//                 int receivedEnd = (int) receivedDist.getAppEndNo();

//                 // Find overlapping given away ranges
//                 List<int[]> givenAwayRanges = new java.util.ArrayList<>();
//                 for (Distribution given : givenAway) {
//                     int givenStart = (int) given.getAppStartNo();
//                     int givenEnd = (int) given.getAppEndNo();
//                     if (givenStart <= receivedEnd && givenEnd >= receivedStart) {
//                         int overlapStart = Math.max(givenStart, receivedStart);
//                         int overlapEnd = Math.min(givenEnd, receivedEnd);
//                         givenAwayRanges.add(new int[] { overlapStart, overlapEnd });
//                     }
//                 }

//                 // Calculate remaining ranges
//                 List<int[]> remainingRanges = calculateRemainingRanges(receivedStart, receivedEnd, givenAwayRanges);

//                 // Create/Reuse balance tracks for remaining ranges
//                 for (int[] range : remainingRanges) {
//                     int remainingStart = range[0];
//                     int remainingEnd = range[1];
//                     int remainingCount = remainingEnd - remainingStart + 1;

//                     BalanceTrack nb;
//                     if (!reusePool.isEmpty()) {
//                         nb = reusePool.poll(); // Reuse
//                     } else {
//                         nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy);
//                         nb.setAmount(amount);
//                     }

//                     nb.setAppAvblCnt(remainingCount);
//                     if (remainingCount <= 0) {
//                         nb.setAppFrom(0);
//                         nb.setAppTo(0);
//                     } else {
//                         nb.setAppFrom(remainingStart);
//                         nb.setAppTo(remainingEnd);
//                     }
//                     nb.setIsActive(1);

//                     balanceTrackRepository.saveAndFlush(nb);
//                     atLeastOneActiveRowCreated = true;

//                     System.out.println("DEBUG DGM: Updated/Created balance row - ID: " + nb.getAppBalanceTrkId() +
//                             ", Range: " + nb.getAppFrom() + "-" + nb.getAppTo());
//                 }
//             }
//         }

//         // FINAL CHECK: If after processing everything, we have NO active rows (all
//         // stock given away)
//         if (!atLeastOneActiveRowCreated) {
//             System.out.println("DEBUG DGM: All stock distributed. Ensuring one active zero-balance row.");
//             BalanceTrack nb;
//             if (!reusePool.isEmpty()) {
//                 nb = reusePool.poll();
//             } else {
//                 nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy);
//                 nb.setAmount(amount);
//             }
//             nb.setAppAvblCnt(0);
//             nb.setAppFrom(0);
//             nb.setAppTo(0);
//             nb.setIsActive(1);
//             balanceTrackRepository.saveAndFlush(nb);
//         }

//         // DEACTIVATE REMAINING POOL (Clean up unused rows)
//         while (!reusePool.isEmpty()) {
//             BalanceTrack unused = reusePool.poll();
//             unused.setIsActive(0);
//             balanceTrackRepository.saveAndFlush(unused);
//             System.out.println("DEBUG DGM: Deactivating unused old balance row ID: " + unused.getAppBalanceTrkId());
//         }

//         // Final flush
//         balanceTrackRepository.flush();
//         System.out.println("DEBUG DGM: Rebuilt balance rows for employee " + empId + " with amount " + amount);
//     }

//     // Helper method to calculate remaining ranges after subtracting given away
//     // ranges
//     private List<int[]> calculateRemainingRanges(int start, int end, List<int[]> givenAwayRanges) {
//         List<int[]> remaining = new java.util.ArrayList<>();

//         if (givenAwayRanges.isEmpty()) {
//             // Nothing given away, entire range remains
//             remaining.add(new int[] { start, end });
//             return remaining;
//         }

//         // Sort given away ranges by start
//         givenAwayRanges.sort((a, b) -> Integer.compare(a[0], b[0]));

//         // Merge overlapping given away ranges
//         List<int[]> merged = new java.util.ArrayList<>();
//         for (int[] range : givenAwayRanges) {
//             if (merged.isEmpty()) {
//                 merged.add(range);
//             } else {
//                 int[] last = merged.get(merged.size() - 1);
//                 if (range[0] <= last[1] + 1) {
//                     // Merge overlapping or adjacent ranges
//                     last[1] = Math.max(last[1], range[1]);
//                 } else {
//                     merged.add(range);
//                 }
//             }
//         }

//         // Calculate remaining ranges
//         int currentStart = start;
//         for (int[] given : merged) {
//             if (currentStart < given[0]) {
//                 // There's a gap before this given away range
//                 remaining.add(new int[] { currentStart, given[0] - 1 });
//             }
//             currentStart = Math.max(currentStart, given[1] + 1);
//         }

//         // Add remaining range after last given away
//         if (currentStart <= end) {
//             remaining.add(new int[] { currentStart, end });
//         }

//         return remaining;
//     }

//     /**
//      * Helper to create a new active Distribution record based on an existing one
//      * but setting the new IssuedToEmpId (the old receiver) and setting IsActive=1.
//      */
//     private Distribution createRemainderDistribution(Distribution originalDist, int receiverId) {
//         Distribution remainderDistribution = new Distribution();
//         // Copy most fields from the original distribution
//         mapExistingToNewDistribution(remainderDistribution, originalDist);

//         // Set specific fields for the remainder
//         remainderDistribution.setIssued_to_emp_id(receiverId); // Stays with the OLD receiver
//         remainderDistribution.setIsActive(1);

//         // Note: The range and count will be set by the caller
//         return remainderDistribution;
//     }

//     // FIX: Added 4th parameter 'int issuedToId'
//     private void mapDtoToDistribution(Distribution distribution, FormSubmissionDTO formDto, int issuedById,
//             int issuedToId) {

//         int appNoFrom = Integer.parseInt(formDto.getApplicationNoFrom());
//         int appNoTo = Integer.parseInt(formDto.getApplicationNoTo());

//         // Map Basic Fields
//         academicYearRepository.findById(formDto.getAcademicYearId()).ifPresent(distribution::setAcademicYear);
//         zoneRepository.findById(formDto.getZoneId()).ifPresent(distribution::setZone);
//         campusRepository.findById(formDto.getCampusId()).ifPresent(distribution::setCampus);

//         cityRepository.findById(formDto.getCityId()).ifPresent(city -> {
//             distribution.setCity(city);
//             if (city.getDistrict() != null) {
//                 distribution.setDistrict(city.getDistrict());
//                 if (city.getDistrict().getState() != null) {
//                     distribution.setState(city.getDistrict().getState());
//                 }
//             }
//         });

//         // --- FIX: Use the passed arguments for Types ---
//         appIssuedTypeRepository.findById(issuedById).ifPresent(distribution::setIssuedByType);
//         appIssuedTypeRepository.findById(issuedToId).ifPresent(distribution::setIssuedToType);
//         // ----------------------------------------------

//         distribution.setAppStartNo(appNoFrom);
//         distribution.setAppEndNo(appNoTo);
//         distribution.setTotalAppCount(formDto.getRange());
//         distribution.setAmount(formDto.getApplication_Amount());

//         // Fix for Date (Always use Now to prevent nulls)
//         distribution.setIssueDate(LocalDateTime.now());

//         distribution.setIsActive(1);
//         distribution.setCreated_by(formDto.getUserId());

//         // Note: We set issued_to_emp_id in the main method, not here.
//     }

//     private void mapExistingToNewDistribution(Distribution newDist, Distribution oldDist) {
//         // ... (Copy standard fields) ...
//         newDist.setAcademicYear(oldDist.getAcademicYear());
//         newDist.setState(oldDist.getState());
//         newDist.setDistrict(oldDist.getDistrict());
//         newDist.setCity(oldDist.getCity());
//         newDist.setZone(oldDist.getZone());
//         newDist.setCampus(oldDist.getCampus());
//         newDist.setIssuedByType(oldDist.getIssuedByType());
//         newDist.setIssuedToType(oldDist.getIssuedToType());
//         newDist.setIssued_to_emp_id(oldDist.getIssued_to_emp_id());
//         newDist.setAppStartNo(oldDist.getAppStartNo());
//         newDist.setAppEndNo(oldDist.getAppEndNo());
//         newDist.setTotalAppCount(oldDist.getTotalAppCount());
//         // Set current timestamp for new distribution
//         newDist.setIssueDate(LocalDateTime.now());
//         newDist.setIsActive(1);
//         newDist.setCreated_by(oldDist.getCreated_by());
//     }

//     private BalanceTrack createNewBalanceTrack(int employeeId, int academicYearId, int typeId, int createdBy) {
//         BalanceTrack nb = new BalanceTrack();
//         nb.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
//         nb.setAcademicYear(academicYearRepository.findById(academicYearId).orElseThrow());
//         nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
//         nb.setAppAvblCnt(0);
//         nb.setIsActive(1);
//         nb.setCreatedBy(createdBy);
//         nb.setIssuedToProId(null); // Strict Validation for Employee
//         return nb;
//     }

//     /**
//      * Validates that the sender (userId) actually owns the requested application
//      * range.
//      * Checks BalanceTrack for Zone/DGM employees or AdminApp for Admin/CO
//      * employees.
//      * Also validates that the amount matches available balances.
//      * 
//      * @param formDto The form submission DTO containing the range to validate
//      * @throws RuntimeException if the requested range is not available, with
//      *                          details of available ranges
//      */
//     private void validateSenderHasAvailableRange(@NonNull FormSubmissionDTO formDto) {
//         int senderId = formDto.getUserId();
//         int academicYearId = formDto.getAcademicYearId();
//         Float amount = formDto.getApplication_Amount();
//         int requestedStart = Integer.parseInt(formDto.getApplicationNoFrom());
//         int requestedEnd = Integer.parseInt(formDto.getApplicationNoTo());

//         // Validate amount is provided and positive
//         if (amount == null) {
//             throw new RuntimeException("Validation Failed: Application Amount cannot be null.");
//         }
//         if (amount < 0) {
//             throw new RuntimeException("Validation Failed: Application Amount cannot be negative. Provided: " + amount);
//         }

//         // Validate range is valid
//         if (requestedStart > requestedEnd) {
//             throw new RuntimeException("Invalid range: Start number (" + requestedStart +
//                     ") cannot be greater than End number (" + requestedEnd + ")");
//         }

//         // Check if sender is Admin/CO (has AdminApp record)
//         Optional<AdminApp> adminApp = adminAppRepository.findByEmpAndYearAndAmount(
//                 senderId, academicYearId, amount);

//         if (adminApp.isPresent()) {
//             // CASE A: Admin/CO - Check against AdminApp master record
//             AdminApp master = adminApp.get();

//             // Validate amount matches AdminApp record
//             Double masterAmount = master.getApp_amount();
//             Integer masterFee = master.getApp_fee();
//             boolean amountMatches = false;

//             if (masterAmount != null && Math.abs(masterAmount - amount) < 0.01) {
//                 amountMatches = true;
//             } else if (masterFee != null && Math.abs(masterFee - amount) < 0.01) {
//                 amountMatches = true;
//             }

//             if (!amountMatches) {
//                 throw new RuntimeException(
//                         "Validation Failed: Requested amount (" + amount +
//                                 ") does not match Admin/CO master record. " +
//                                 "Master Amount: " + (masterAmount != null ? masterAmount : "null") +
//                                 ", Master Fee: " + (masterFee != null ? masterFee : "null"));
//             }

//             int masterStart = master.getAppFromNo();
//             int masterEnd = master.getAppToNo();

//             // Calculate what they've already distributed
//             int totalDistributed = distributionRepository
//                     .sumTotalAppCountByCreatedByAndAmount(senderId, academicYearId, amount).orElse(0);

//             // Calculate next available number
//             Optional<Integer> maxDistributedEnd = distributionRepository
//                     .findMaxAppEndNoByCreatedByAndAmount(senderId, academicYearId, amount);

//             int availableStart;
//             if (maxDistributedEnd.isPresent() && maxDistributedEnd.get() >= masterStart) {
//                 availableStart = maxDistributedEnd.get() + 1;
//             } else {
//                 availableStart = masterStart;
//             }
//             int availableEnd = masterEnd;

//             // Validate requested range is within available range
//             if (requestedStart < availableStart || requestedEnd > availableEnd) {
//                 throw new RuntimeException(
//                         "Validation Failed: You cannot distribute range " + requestedStart + "-" + requestedEnd +
//                                 ". Available range for distribution: " + availableStart + "-" + availableEnd +
//                                 " (Master: " + masterStart + "-" + masterEnd + ", Already distributed: "
//                                 + totalDistributed + " applications)");
//             }

//             // Check if range count matches
//             int requestedCount = requestedEnd - requestedStart + 1;
//             int availableCount = availableEnd - availableStart + 1;
//             if (requestedCount > availableCount) {
//                 throw new RuntimeException(
//                         "Validation Failed: Requested count (" + requestedCount +
//                                 ") exceeds available count (" + availableCount +
//                                 "). Available range: " + availableStart + "-" + availableEnd);
//             }

//             System.out.println("DEBUG: Admin/CO validation passed - Requested: " + requestedStart + "-" + requestedEnd +
//                     ", Available: " + availableStart + "-" + availableEnd);
//             return;
//         }

//         // CASE B: Zone/DGM - Check against BalanceTrack
//         List<BalanceTrack> availableBalances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(
//                 academicYearId, senderId, amount);

//         if (availableBalances.isEmpty()) {
//             // Check if employee has balances with different amounts
//             List<Float> availableAmounts = balanceTrackRepository.findAmountsByEmpIdAndAcademicYear(senderId,
//                     academicYearId);
//             if (availableAmounts != null && !availableAmounts.isEmpty()) {
//                 StringBuilder amountsList = new StringBuilder();
//                 for (Float amt : availableAmounts) {
//                     if (amountsList.length() > 0) {
//                         amountsList.append(", ");
//                     }
//                     amountsList.append(amt);
//                 }
//                 throw new RuntimeException(
//                         "Validation Failed: You have no available applications with amount " + amount +
//                                 " to distribute. Available amounts for this employee: " + amountsList.toString() +
//                                 ". Employee ID: " + senderId + ", Academic Year: " + academicYearId);
//             } else {
//                 throw new RuntimeException(
//                         "Validation Failed: You have no available applications to distribute. " +
//                                 "Employee ID: " + senderId + ", Academic Year: " + academicYearId + ", Amount: "
//                                 + amount);
//             }
//         }

//         // Validate amount matches the balance track amounts
//         boolean amountMatches = false;
//         for (BalanceTrack balance : availableBalances) {
//             if (balance.getAmount() != null && Math.abs(balance.getAmount() - amount) < 0.01) {
//                 amountMatches = true;
//                 break;
//             }
//         }

//         if (!amountMatches) {
//             StringBuilder amountsList = new StringBuilder();
//             for (BalanceTrack balance : availableBalances) {
//                 if (amountsList.length() > 0) {
//                     amountsList.append(", ");
//                 }
//                 amountsList.append(balance.getAmount());
//             }
//             throw new RuntimeException(
//                     "Validation Failed: Requested amount (" + amount +
//                             ") does not match any available balance. Available amounts: " + amountsList.toString());
//         }

//         // Check if requested range falls within any available balance range
//         boolean rangeFound = false;
//         StringBuilder availableRanges = new StringBuilder();

//         for (BalanceTrack balance : availableBalances) {
//             int balanceStart = balance.getAppFrom();
//             int balanceEnd = balance.getAppTo();
//             int balanceCount = balance.getAppAvblCnt();

//             if (availableRanges.length() > 0) {
//                 availableRanges.append(", ");
//             }
//             availableRanges.append(balanceStart).append("-").append(balanceEnd).append(" (Count: ").append(balanceCount)
//                     .append(")");

//             // Check if entire requested range is within this balance range
//             if (requestedStart >= balanceStart && requestedEnd <= balanceEnd) {
//                 // Check if there's enough count available
//                 if (requestedEnd - requestedStart + 1 <= balanceCount) {
//                     rangeFound = true;
//                     System.out.println(
//                             "DEBUG: Range validation passed - Requested: " + requestedStart + "-" + requestedEnd +
//                                     ", Found in Balance: " + balanceStart + "-" + balanceEnd + " (Available: "
//                                     + balanceCount + ")");
//                     break;
//                 }
//             }
//         }

//         if (!rangeFound) {
//             throw new RuntimeException(
//                     "Validation Failed: You cannot distribute range " + requestedStart + "-" + requestedEnd +
//                             ". Available ranges: " + availableRanges.toString() +
//                             ". Please select a range from the available ranges above.");
//         }
//     }

// }

package com.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.application.dto.AppSeriesDTO;
import com.application.dto.FormSubmissionDTO;
import com.application.dto.GenericDropdownDTO;
import com.application.dto.LocationAutoFillDTO;
import com.application.entity.AdminApp;
import com.application.entity.BalanceTrack;
import com.application.entity.Campus;
import com.application.entity.City;
import com.application.entity.Dgm;
import com.application.entity.Distribution;
import com.application.entity.District;
import com.application.entity.UserAdminView;
import com.application.repository.AcademicYearRepository;
import com.application.repository.AdminAppRepository;
import com.application.repository.AppIssuedTypeRepository;
import com.application.repository.BalanceTrackRepository;
import com.application.repository.CampusRepository;
import com.application.repository.CityRepository;
import com.application.repository.DgmRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.EmployeeRepository;
import com.application.repository.UserAdminViewRepository;
import com.application.repository.ZonalAccountantRepository;
import com.application.repository.ZoneRepository;
import com.application.repository.SCEmployeeRepository;
import com.application.dto.EmployeeLocationDTO;
import com.application.dto.DgmWithCampusesDTO;
import com.application.entity.SCEmployeeEntity;
import com.application.entity.State;
import com.application.entity.Zone;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DgmService {

    private final AcademicYearRepository academicYearRepository;
    private final CityRepository cityRepository;
    private final ZoneRepository zoneRepository;
    private final CampusRepository campusRepository;
    private final AppIssuedTypeRepository appIssuedTypeRepository;
    private final DistributionRepository distributionRepository;
    private final EmployeeRepository employeeRepository;
    private final BalanceTrackRepository balanceTrackRepository;
    private final DgmRepository dgmRepository;
    private final UserAdminViewRepository userAdminViewRepository;
    private final ZonalAccountantRepository zonalAccountantRepository;
    private final AdminAppRepository adminAppRepository;
    private final SCEmployeeRepository scEmployeeRepository;

    // --- Dropdown and Helper Methods with Caching ---
    // @Cacheable("academicYears")
    public List<GenericDropdownDTO> getAllAcademicYears() {
        return academicYearRepository.findAll().stream()
                .map(year -> new GenericDropdownDTO(year.getAcdcYearId(), year.getAcademicYear()))
                .collect(Collectors.toList());
    }

    @Cacheable("cities")
    public List<GenericDropdownDTO> getAllCities() {
        final int ACTIVE_STATUS = 1;

        return cityRepository.findByStatus(ACTIVE_STATUS).stream()
                .map(city -> new GenericDropdownDTO(city.getCityId(), city.getCityName())).collect(Collectors.toList());
    }

    // @Cacheable(cacheNames = "zonesByCity", key = "#cityId")
    public List<GenericDropdownDTO> getZonesByCityId(int cityId) {
        return zoneRepository.findByCityCityId(cityId).stream()
                .map(zone -> new GenericDropdownDTO(zone.getZoneId(), zone.getZoneName())).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "campusesByZone", key = "#zoneId")
    public List<GenericDropdownDTO> getCampusesByZoneId(int zoneId) {
        // Query directly from Campus table where zone_id matches and is_active = 1
        // This ensures we get all active campuses in the zone, not just those linked to ZonalAccountant records
        return campusRepository.findByZoneZoneId(zoneId).stream()
                .distinct()
                .map(campus -> new GenericDropdownDTO(campus.getCampusId(), campus.getCampusName()))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to get zone IDs for an employee
     * Priority: 1. Employee view (direct source of truth), 2. ZonalAccountant table (fallback)
     */
    private List<Integer> getZoneIdsForEmployee(int empId) {
        // First, check employee view directly (source of truth)
        List<SCEmployeeEntity> employees = scEmployeeRepository.findByEmpId(empId);
        if (employees != null && !employees.isEmpty()) {
            SCEmployeeEntity employee = employees.get(0);
            // If employee is a ZONAL ACCOUNTANT and has zone_id, use it directly
            if (employee.getDesignationName() != null && 
                employee.getDesignationName().trim().equalsIgnoreCase("ZONAL ACCOUNTANT") &&
                employee.getZoneId() > 0) {
                return java.util.Collections.singletonList(employee.getZoneId());
            }
        }
        
        // Fallback: Check ZonalAccountant table (for cases where employee view might not have the data)
        List<Integer> zoneIds = zonalAccountantRepository.findZoneIdByEmployeeId(empId);
        return (zoneIds != null && !zoneIds.isEmpty()) ? zoneIds : Collections.emptyList();
    }

    @Cacheable(cacheNames = "campusforzonalaccountant", key = "#empId")
    public List<GenericDropdownDTO> getCampusesByEmployeeId(int empId) {
        List<Integer> zoneIds = getZoneIdsForEmployee(empId);
        
        if (zoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        return zoneIds.stream()
                .flatMap(zoneId -> getCampusesByZoneId(zoneId).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<GenericDropdownDTO> getCampusesByEmployeeIdAndCategory(int empId, String category) {
        List<Integer> zoneIds = getZoneIdsForEmployee(empId);
        
        if (zoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<GenericDropdownDTO> allCampuses = zoneIds.stream()
                .flatMap(zoneId -> getCampusesByZoneId(zoneId).stream())
                .distinct()
                .collect(Collectors.toList());

        // ============================
        // 🔥 CATEGORY FILTERING LOGIC
        // ============================
        if (category == null || category.isEmpty()) {
            return allCampuses; // no filter applied
        }

        String cat = category.trim().toLowerCase();
        
        // Validate category - ignore invalid values like "[object object]" from frontend errors
        if (cat.contains("[object") || cat.equals("object") || cat.length() > 20) {
            // Invalid category value, return all campuses without filtering
            return allCampuses;
        }

        return allCampuses.stream()
                .filter(c -> {
                    Campus campus = campusRepository.findById(c.getId()).orElse(null);
                    if (campus == null || campus.getBusinessType() == null)
                        return false;

                    String type = campus.getBusinessType().getBusinessTypeName().toLowerCase();

                    if (cat.equals("school")) {
                        return type.contains("school");
                    } else if (cat.equals("college")) {
                        return type.contains("college");
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<GenericDropdownDTO> getActiveCampusesByEmpId(Integer empId) {
        List<Dgm> dgms = dgmRepository.findByEmpId(empId);

        // Map to GenericDropdownDTO — filtering only active campuses
        return dgms.stream().filter(
                d -> d.getCampus() != null && d.getCampus().getIsActive() != null && d.getCampus().getIsActive() == 1)
                .map(d -> new GenericDropdownDTO(d.getCampus().getCampusId(), d.getCampus().getCampusName()))
                .collect(Collectors.toList());
    }

    public List<GenericDropdownDTO> getActiveCampusesByEmpIdAndCategory(Integer empId, String category) {

        List<Dgm> dgms = dgmRepository.findByEmpId(empId);

        // Step 1 → Get all active campuses for this DGM
        List<GenericDropdownDTO> campusList = dgms.stream()
                .filter(d -> d.getCampus() != null &&
                        d.getCampus().getIsActive() != null &&
                        d.getCampus().getIsActive() == 1)
                .map(d -> new GenericDropdownDTO(
                        d.getCampus().getCampusId(),
                        d.getCampus().getCampusName()))
                .collect(Collectors.toList());

        // Step 2 → If no category, return same as before
        if (category == null || category.trim().isEmpty()) {
            return campusList;
        }

        // Step 3 → Filter by category
        String cat = category.trim().toLowerCase();

        return campusList.stream()
                .filter(c -> matchBusinessType(c.getId(), cat)) // reused helper from earlier
                .collect(Collectors.toList());
    }

    public List<GenericDropdownDTO> getActiveCampusesByEmployeeId(int empId) {
        List<Integer> zoneIds = getZoneIdsForEmployee(empId);
        
        if (zoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 2: Get campus IDs for all zones
        List<Integer> campusIds = dgmRepository.findCampusIdsByZoneIds(zoneIds);
        if (campusIds == null || campusIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 3: Get only active campuses
        return campusRepository.findActiveCampusesByIds(campusIds);
    }

    public List<GenericDropdownDTO> getActiveCampusesByEmployeeIdAndCategory(int empId, String category) {
        // Step 1 → Find zones for this zonal accountant
        List<Integer> zoneIds = getZoneIdsForEmployee(empId);
        
        if (zoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 2 → Get campuses mapped under DGM for those zones
        List<Integer> campusIds = dgmRepository.findCampusIdsByZoneIds(zoneIds);
        if (campusIds == null || campusIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 3 → Get only active campuses (already returns GenericDropdownDTO)
        List<GenericDropdownDTO> campuses = campusRepository.findActiveCampusesByIds(campusIds);

        // Step 4 → If category not provided → return as is
        if (category == null || category.trim().isEmpty()) {
            return campuses;
        }

        // Step 5 → Filter by business type (school/college)
        String cat = category.trim().toLowerCase();
        
        // Validate category - ignore invalid values like "[object object]" from frontend errors
        if (cat.contains("[object") || cat.equals("object") || cat.length() > 20) {
            // Invalid category value, return all campuses without filtering
            return campuses;
        }

        return campuses.stream()
                .filter(c -> matchBusinessType(c.getId(), cat))
                .collect(Collectors.toList());
    }

    public List<GenericDropdownDTO> getDgmCampusesByZoneAndCategory(Integer zoneId, String category) {
        Integer valid = zonalAccountantRepository.validateZone(zoneId);
        if (valid == null || valid == 0) {
            return Collections.emptyList();
        }
        List<Campus> campuses = dgmRepository.findCampusesByZone(zoneId);
        if (campuses == null || campuses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Campus> filtered = campuses.stream()
                .filter(c -> filterByCategory(c, category))
                .collect(Collectors.toList());
        return filtered.stream()
                .map(c -> new GenericDropdownDTO(c.getCampusId(), c.getCampusName()))
                .collect(Collectors.toList());
    }

    private boolean filterByCategory(Campus campus, String category) {

        if (category == null || category.isBlank()) {
            return true;
        }

        String cat = category.trim().toLowerCase();
        
        // Validate category - ignore invalid values like "[object object]" from frontend errors
        if (cat.contains("[object") || cat.equals("object") || cat.length() > 20) {
            // Invalid category value, return true to include all campuses
            return true;
        }

        if (campus.getBusinessType() == null) {
            return false;
        }

        String type = campus.getBusinessType().getBusinessTypeName().toLowerCase();

        switch (cat) {
            case "school":
                return type.contains("school");
            case "college":
                return type.contains("college");
            default:
                return true;
        }
    }

    private boolean matchBusinessType(Integer campusId, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }

        String cat = category.trim().toLowerCase();
        
        // Validate category - ignore invalid values like "[object object]" from frontend errors
        if (cat.contains("[object") || cat.equals("object") || cat.length() > 20) {
            // Invalid category value, return true to include all campuses
            return true;
        }

        Campus campus = campusRepository.findById(campusId).orElse(null);
        if (campus == null || campus.getBusinessType() == null) {
            return false;
        }

        String businessName = campus.getBusinessType().getBusinessTypeName().toLowerCase();

        switch (cat) {
            case "school":
                return businessName.contains("school");
            case "college":
                return businessName.contains("college");
            default:
                return true; // Others → no filter
        }
    }

    // @Cacheable("issuedToTypes")
    public List<GenericDropdownDTO> getAllIssuedToTypes() {
        return appIssuedTypeRepository.findAll().stream()
                .map(type -> new GenericDropdownDTO(type.getAppIssuedId(), type.getTypeName()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "mobileNumberByEmpId", key = "#empId")
    public String getMobileNumberByEmpId(int empId) {
        return employeeRepository.findMobileNoByEmpId(empId);
    }

    @Cacheable(cacheNames = "getDgmforCampus", key = "#campusId")
    public List<GenericDropdownDTO> getDgmEmployeesForCampus(int campusId) {
        // Find distinct DGM employees for that Campus, checking isActive = 1
        // Filter by campusId directly with d.isActive = 1 and e.isActive = 1
        return dgmRepository.findDistinctActiveEmployeesByCampusId(campusId);
    }

    public List<Double> getApplicationFees(int empId, int academicYearId) { // UPDATED SIGNATURE

        // 1. Check AdminApp table first (UPDATED CALL)
        List<Double> adminFees = adminAppRepository.findAmountsByEmpIdAndAcademicYear(empId, academicYearId);

        // 2. If AdminApp has data, convert to Double and return
        if (adminFees != null && !adminFees.isEmpty()) {
            return adminFees.stream()
                    .map(Double::valueOf)
                    .collect(Collectors.toList());
        }

        // 3. If AdminApp is empty, check BalanceTrack table (UPDATED CALL)
        List<Float> balanceFees = balanceTrackRepository.findAmountsByEmpIdAndAcademicYear(empId, academicYearId);

        if (balanceFees != null && !balanceFees.isEmpty()) {
            return balanceFees.stream()
                    .map(Double::valueOf)
                    .collect(Collectors.toList());
        }

        // 4. If both are empty, return an empty list
        return Collections.emptyList();
    }

    public LocationAutoFillDTO getAutoPopulateData(int empId, String category) {

        // 1️⃣ Only apply logic when category = "school"
        if (!"school".equalsIgnoreCase(category)) {
            return null;
        }

        // 2️⃣ Get active DGM record for employee
        Dgm dgm = dgmRepository
                .findActiveDgm(empId, 1)
                .orElse(null);

        if (dgm == null) {
            return null;
        }

        // 3️⃣ DISTRICT (direct from Dgm table)
        District district = dgm.getDistrict();

        Integer districtId = district != null ? district.getDistrictId() : null;
        String districtName = district != null ? district.getDistrictName() : null;

        // 4️⃣ CITY (via Campus)
        Campus campus = dgm.getCampus();
        City city = (campus != null) ? campus.getCity() : null;

        Integer cityId = city != null ? city.getCityId() : null;
        String cityName = city != null ? city.getCityName() : null;

        // 5️⃣ Return final DTO
        return new LocationAutoFillDTO(cityId, cityName, districtId, districtName);
    }

    /**
     * Get employee location details based on employee ID and campus category
     * Checks if employee's cmps_category matches the provided category
     * If match, returns campus, city, state, zone, and district information
     */
    public EmployeeLocationDTO getEmployeeLocationByCategory(int employeeId, String cmpsCategory) {
        // 1. Get employee from view
        List<SCEmployeeEntity> employees = scEmployeeRepository.findByEmpId(employeeId);
        if (employees == null || employees.isEmpty()) {
            return null;
        }

        SCEmployeeEntity employee = employees.get(0);

        // 2. Check if cmps_category matches (case-insensitive)
        String employeeCategory = employee.getCmpsCategory();
        if (employeeCategory == null || !employeeCategory.equalsIgnoreCase(cmpsCategory)) {
            return null; // Category doesn't match
        }

        // 3. Get campus ID and name from employee view
        int campusId = employee.getEmpCampusId();
        String campusName = employee.getCampusName();

        if (campusId <= 0) {
            return null; // Invalid campus ID
        }

        // 4. Get campus from database
        Optional<Campus> campusOptional = campusRepository.findById(campusId);
        if (campusOptional.isEmpty()) {
            return null;
        }

        Campus campus = campusOptional.get();

        // 5. Get city, state, zone from campus
        City city = campus.getCity();
        State state = campus.getState();
        Zone zone = campus.getZone();

        Integer cityId = city != null ? city.getCityId() : null;
        String cityName = city != null ? city.getCityName() : null;

        Integer stateId = state != null ? state.getStateId() : null;
        String stateName = state != null ? state.getStateName() : null;

        Integer zoneId = zone != null ? zone.getZoneId() : null;
        String zoneName = zone != null ? zone.getZoneName() : null;

        // 6. Get district from city
        District district = city != null ? city.getDistrict() : null;
        Integer districtId = district != null ? district.getDistrictId() : null;
        String districtName = district != null ? district.getDistrictName() : null;

        // 7. Return DTO with all information
        return new EmployeeLocationDTO(
                campusId,
                campusName,
                cityId,
                cityName,
                stateId,
                stateName,
                zoneId,
                zoneName,
                districtId,
                districtName);
    }

    /**
     * Get DGM names and their associated campuses based on employee ID
     * Returns a list of DGM records with their campuses for the given employee
     */
    public List<DgmWithCampusesDTO> getDgmWithCampusesByEmployeeId(Integer employeeId) {
        // 1. Get zone IDs for this employee (checks employee view first, then ZonalAccountant table)
        List<Integer> zoneIds = getZoneIdsForEmployee(employeeId);

        List<Dgm> dgmRecords;

        if (zoneIds != null && !zoneIds.isEmpty()) {
            // CASE A: Zonal Accountant - Fetch all Active DGMs in those Zones
            System.out.println("DEBUG: Fetching active DGMs for zones: " + zoneIds);
            dgmRecords = dgmRepository.findActiveDgmByZoneIds(zoneIds);
            System.out.println("DEBUG: Found " + (dgmRecords != null ? dgmRecords.size() : 0) + " active DGM records");
        } else {
            // CASE B: DGM (or other) - Fetch DGM records for this employee specifically
            System.out.println("DEBUG: Fetching DGM records for employee: " + employeeId);
            dgmRecords = dgmRepository.findDgmWithCampusesByEmployeeId(employeeId);
            System.out.println("DEBUG: Found " + (dgmRecords != null ? dgmRecords.size() : 0) + " DGM records for employee");
        }

        if (dgmRecords == null || dgmRecords.isEmpty()) {
            System.out.println("DEBUG: No DGM records found, returning empty list");
            return new ArrayList<>();
        }

        // Group by DGM employee (since one employee can have multiple DGM records for
        // different campuses)
        // We'll create a map to group campuses by DGM employee
        java.util.Map<Integer, DgmWithCampusesDTO> dgmMap = new java.util.HashMap<>();

        for (Dgm dgm : dgmRecords) {
            if (dgm.getEmployee() == null)
                continue;

            Integer empId = dgm.getEmployee().getEmp_id();
            String dgmName = dgm.getEmployee().getFirst_name() + " " + dgm.getEmployee().getLast_name();

            // Get or create DGM entry
            DgmWithCampusesDTO dto = dgmMap.get(empId);
            if (dto == null) {
                dto = new DgmWithCampusesDTO();
                dto.setId(empId);
                dto.setName(dgmName);
                dto.setCmpsId(new ArrayList<>());
                dgmMap.put(empId, dto);
            }

            // Add campus ID to the list (avoid duplicates)
            if (dgm.getCampus() != null) {
                Integer campusId = dgm.getCampus().getCampusId();

                // Check if campus ID already exists in the list
                if (!dto.getCmpsId().contains(campusId)) {
                    dto.getCmpsId().add(campusId);
                }
            }
        }

        return new ArrayList<>(dgmMap.values());
    }

    // public Optional<AppFromDTO> getAppFromByEmployeeAndYear(int employeeId, int
    // academicYearId) {
    // return balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(employeeId,
    // academicYearId);
    //
    // // OR if using the @Query method:
    // // return
    // balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(employeeId,
    // // academicYearId);
    // }
    //
    //// @Cacheable(cacheNames = "getAppRange", key = "{#academicYearId,
    // #employeeId}")
    // public AppRangeDTO getAppRange(int empId, int academicYearId) {
    // // Fetch distribution data
    // AppDistributionDTO distDTO = distributionRepository
    // .findActiveAppRangeByEmployeeAndAcademicYear(empId,
    // academicYearId).orElse(null);
    //
    // // Fetch balance track data (now returns AppFromDTO with the ID)
    // AppFromDTO fromDTO =
    // balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(empId,
    // academicYearId)
    // .orElse(null);
    //
    // if (distDTO == null && fromDTO == null) {
    // return null;
    // }
    //
    // // Merge results into a single DTO
    // Integer appStartNo = distDTO != null ? distDTO.getAppStartNo() : null;
    // Integer appEndNo = distDTO != null ? distDTO.getAppEndNo() : null;
    //
    // // Extract fields from the updated AppFromDTO
    // Integer appFrom = fromDTO != null ? fromDTO.getAppFrom() : null;
    // Integer appBalanceTrkId = fromDTO != null ? fromDTO.getAppBalanceTrkId() :
    // null; // Extracted new ID
    //
    // // Use the updated AppRangeDTO constructor
    // return new AppRangeDTO(appStartNo, appEndNo, appFrom, appBalanceTrkId);
    // }
    //
    // public AppRangeDTO getAppRange(int empId, int academicYearId, Integer cityId)
    // { // Updated signature
    // // Fetch distribution data
    // AppDistributionDTO distDTO = distributionRepository
    // .findActiveAppRangeByEmployeeAndAcademicYear(empId, academicYearId, cityId)
    // // Pass cityId
    // .orElse(null);
    //
    // // Fetch balance track data (now returns AppFromDTO with the ID)
    // AppFromDTO fromDTO =
    // balanceTrackRepository.getAppFromByEmployeeAndAcademicYear(empId,
    // academicYearId)
    // .orElse(null);
    //
    // if (distDTO == null && fromDTO == null) {
    // return null;
    // }
    //
    // // Merge results into a single DTO
    // Integer appStartNo = distDTO != null ? distDTO.getAppStartNo() : null;
    // Integer appEndNo = distDTO != null ? distDTO.getAppEndNo() : null;
    //
    // // Extract fields from the updated AppFromDTO
    // Integer appFrom = fromDTO != null ? fromDTO.getAppFrom() : null;
    // Integer appBalanceTrkId = fromDTO != null ? fromDTO.getAppBalanceTrkId() :
    // null; // Extracted new ID
    //
    // // Use the updated AppRangeDTO constructor
    // return new AppRangeDTO(appStartNo, appEndNo, appFrom, appBalanceTrkId);
    // }

    // ... inside DgmService class ...

    // Updated Method to use Double - First check BalanceTrack, then AdminApp (for
    // employees only)
    public List<AppSeriesDTO> getActiveSeriesForReceiver(int receiverId, int academicYearId, Double amount,
            boolean isPro) {

        // 1. FIRST: Try to fetch Series List from BalanceTrack with academicYearId
        // filter
        List<AppSeriesDTO> seriesList;
        if (isPro) {
            // For PRO: Only check BalanceTrack (AdminApp doesn't have PRO data)
            seriesList = balanceTrackRepository.findSeriesByProIdYearAndAmount(receiverId, academicYearId, amount);

            // If BalanceTrack has data, enrich with master info and return
            if (seriesList != null && !seriesList.isEmpty()) {
                // Fetch Master Info from AdminApp
                List<AdminApp> masterRecords = adminAppRepository.findMasterRecordByYearAndAmount(
                        academicYearId, amount);

                // Enrich DTOs with master info
                if (!masterRecords.isEmpty()) {
                    AdminApp master = masterRecords.get(0);
                    int mStart = master.getAppFromNo();
                    int mEnd = master.getAppToNo();

                    for (AppSeriesDTO dto : seriesList) {
                        dto.setMasterStartNo(mStart);
                        dto.setMasterEndNo(mEnd);
                    }
                }
            }
            // Return BalanceTrack data or empty list for PRO
            return seriesList != null ? seriesList : new ArrayList<>();

        } else {
            // For Employee: Check BalanceTrack first, then fallback to AdminApp
            seriesList = balanceTrackRepository.findSeriesByEmpIdYearAndAmount(receiverId, academicYearId, amount);

            // 2. If BalanceTrack has data, enrich with master info and return
            if (seriesList != null && !seriesList.isEmpty()) {
                Integer mStart = null;
                Integer mEnd = null;

                // STEP 1: Check AdminApp first (only for Admins/CO)
                List<AdminApp> masterRecords = adminAppRepository.findMasterRecordByYearAndAmountAndEmployee(
                        receiverId, academicYearId, amount);

                if (!masterRecords.isEmpty()) {
                    // Admin/CO: Get master from AdminApp table
                    AdminApp master = masterRecords.get(0);
                    mStart = master.getAppFromNo();
                    mEnd = master.getAppToNo();

                    if (mStart == null || mEnd == null) {
                        System.out.println("WARNING: AdminApp master record found for empId=" + receiverId +
                                ", yearId=" + academicYearId + ", amount=" + amount +
                                " but appFromNo or appToNo is null. appFromNo=" + mStart + ", appToNo=" + mEnd);
                        mStart = null;
                        mEnd = null;
                    } else {
                        System.out.println("INFO: Admin/CO employee - Master range from AdminApp: " +
                                mStart + " to " + mEnd + " (receiverId=" + receiverId + ")");
                    }
                }

                // STEP 2: If AdminApp not found, get from Distribution table (for Zone/DGM
                // level)
                // Zone/DGM employees don't have AdminApp records, so match each series to its
                // distribution
                if (mStart == null || mEnd == null) {
                    // Get ALL distributions (including inactive) to find original distribution
                    // before split
                    // Original distributions are marked inactive when split, but we need them for
                    // master range
                    List<Distribution> receivedDistributions = distributionRepository
                            .findAllByIssuedToEmpIdAndAmountIncludingInactive(
                                    receiverId, academicYearId, amount);

                    if (!receivedDistributions.isEmpty()) {
                        // For each series, find the ORIGINAL distribution that contains this series
                        // range
                        // (Not the remainder - the original distribution before it was split)
                        for (AppSeriesDTO dto : seriesList) {
                            // Find ALL distributions that contain this series range
                            List<Distribution> containingDists = new ArrayList<>();
                            for (Distribution dist : receivedDistributions) {
                                // Check if the series range falls within this distribution
                                // Series: dto.startNo to dto.endNo
                                // Distribution: dist.appStartNo to dist.appEndNo
                                if (dto.getStartNo() >= dist.getAppStartNo() && dto.getEndNo() <= dist.getAppEndNo()
                                        && dist.getIsActive() == 1) {
                                    containingDists.add(dist);
                                    System.out.println("DEBUG: Series " + dto.getStartNo() + "-" + dto.getEndNo() +
                                            " is contained in Distribution " + dist.getAppStartNo() + "-"
                                            + dist.getAppEndNo() +
                                            " (distId=" + dist.getAppDistributionId() + ", isActive="
                                            + dist.getIsActive() +
                                            ", range=" + (dist.getAppEndNo() - dist.getAppStartNo()) + ")");
                                }
                            }

                            System.out.println(
                                    "DEBUG: Found " + containingDists.size() + " distributions containing series " +
                                            dto.getStartNo() + "-" + dto.getEndNo());

                            Distribution matchingDist = null;
                            if (!containingDists.isEmpty()) {
                                // Among all distributions that contain this series, pick the ORIGINAL one
                                // Strategy: Find the one with the WIDEST range (largest end - start)
                                // Original distributions have wider ranges, remainders are smaller
                                // If ranges are equal, pick the one with LOWEST ID (earliest created)
                                matchingDist = containingDists.stream()
                                        .max((d1, d2) -> {
                                            // First compare by range width (end - start)
                                            int range1 = d1.getAppEndNo() - d1.getAppStartNo();
                                            int range2 = d2.getAppEndNo() - d2.getAppStartNo();
                                            int rangeCompare = Integer.compare(range1, range2);
                                            if (rangeCompare != 0) {
                                                return rangeCompare; // Wider range wins
                                            }
                                            // If ranges are equal, pick lower ID (earliest)
                                            int id1 = d1.getAppDistributionId() != null ? d1.getAppDistributionId()
                                                    : Integer.MAX_VALUE;
                                            int id2 = d2.getAppDistributionId() != null ? d2.getAppDistributionId()
                                                    : Integer.MAX_VALUE;
                                            return Integer.compare(id2, id1); // Lower ID wins (reverse compare)
                                        })
                                        .orElse(null);
                            }

                            if (matchingDist != null) {
                                // Use the ORIGINAL distribution's range as master range for this series
                                dto.setMasterStartNo(matchingDist.getAppStartNo());
                                dto.setMasterEndNo(matchingDist.getAppEndNo());
                                System.out.println("INFO: Series " + dto.getStartNo() + "-" + dto.getEndNo() +
                                        " matched to ORIGINAL Distribution " + matchingDist.getAppStartNo() + "-"
                                        + matchingDist.getAppEndNo() +
                                        " (distId=" + matchingDist.getAppDistributionId() + ")");
                            } else {
                                // If no match found, use the first distribution as fallback
                                Distribution firstDist = receivedDistributions.get(0);
                                dto.setMasterStartNo(firstDist.getAppStartNo());
                                dto.setMasterEndNo(firstDist.getAppEndNo());
                                System.out.println("WARNING: No matching distribution found for series "
                                        + dto.getStartNo() + "-" + dto.getEndNo() +
                                        ", using first distribution as fallback: " + firstDist.getAppStartNo() + "-"
                                        + firstDist.getAppEndNo());
                            }
                        }
                    } else {
                        System.out.println("WARNING: No distributions found for receiverId=" + receiverId +
                                ", academicYearId=" + academicYearId + ", amount=" + amount);
                    }
                } else {
                    // Admin/CO: Use same master range for all series
                    for (AppSeriesDTO dto : seriesList) {
                        dto.setMasterStartNo(mStart);
                        dto.setMasterEndNo(mEnd);
                    }
                }

                return seriesList;
            }

            // 3. If BalanceTrack is empty, fetch from AdminApp table (for employee)
            List<AdminApp> adminAppRecords = adminAppRepository.findAllByEmpAndYearAndAmount(
                    receiverId, academicYearId, amount);

            // 4. Convert AdminApp records to AppSeriesDTO format
            List<AppSeriesDTO> adminAppSeriesList = new ArrayList<>();
            if (adminAppRecords != null && !adminAppRecords.isEmpty()) {
                for (AdminApp adminApp : adminAppRecords) {
                    if (adminApp.getAppFromNo() != null && adminApp.getAppToNo() != null) {
                        String displaySeries = adminApp.getAppFromNo() + " - " + adminApp.getAppToNo();
                        int availableCount = adminApp.getTotalApp() != null ? adminApp.getTotalApp() : 0;

                        AppSeriesDTO dto = new AppSeriesDTO(
                                displaySeries,
                                adminApp.getAppFromNo(),
                                adminApp.getAppToNo(),
                                availableCount);

                        // Set master info (same as start/end for AdminApp)
                        dto.setMasterStartNo(adminApp.getAppFromNo());
                        dto.setMasterEndNo(adminApp.getAppToNo());

                        adminAppSeriesList.add(dto);
                    }
                }
            }

            return adminAppSeriesList;
        }
    }

    public Integer getDistributionIdBySeries(int receiverId, int start, int end, Double amount, boolean isPro) {
        if (isPro) {
            return distributionRepository.findIdByProAndRange(receiverId, start, end, amount)
                    .orElseThrow(() -> new RuntimeException("No Active Distribution found for this PRO range."));
        } else {
            return distributionRepository.findIdByEmpAndRange(receiverId, start, end, amount)
                    .orElseThrow(() -> new RuntimeException("No Active Distribution found for this Employee range."));
        }
    }

    // Generic helper to find the highest priority Role ID for ANY employee (Issuer
    // or Receiver)
    private int getRoleTypeIdByEmpId(int empId) {
        List<UserAdminView> userRoles = userAdminViewRepository.findRolesByEmpId(empId);

        if (userRoles.isEmpty()) {
            // If the receiver has no role, they might be a basic employee.
            // You might want a default ID (e.g., 4) or throw an error.
            // For DGM Service, we expect them to be DGM (3).
            throw new RuntimeException("No valid roles found for Employee ID: " + empId);
        }

        int highestPriorityTypeId = Integer.MAX_VALUE;
        for (UserAdminView userView : userRoles) {
            String roleName = userView.getRole_name().trim().toUpperCase();
            int currentTypeId = switch (roleName) {
                case "ADMIN" -> 1;
                case "ZONAL ACCOUNTANT" -> 2;
                case "DGM" -> 3;
                // Add "PRO" or "AGENT" -> 4 if needed
                default -> -1;
            };
            if (currentTypeId != -1 && currentTypeId < highestPriorityTypeId) {
                highestPriorityTypeId = currentTypeId;
            }
        }

        if (highestPriorityTypeId == Integer.MAX_VALUE) {
            // Fallback or Error. If DGM service, maybe default to 3?
            return 3; // Defaulting to DGM if role logic is strict
        }
        return highestPriorityTypeId;
    }

    private int getIssuedTypeByUserId(int userId) {
        List<UserAdminView> userRoles = userAdminViewRepository.findRolesByEmpId(userId);
        if (userRoles.isEmpty())
            throw new RuntimeException("No roles found for ID: " + userId);

        int highestPriorityTypeId = Integer.MAX_VALUE;
        for (UserAdminView userView : userRoles) {
            String roleName = userView.getRole_name().trim().toUpperCase();
            int currentTypeId = switch (roleName) {
                case "ADMIN" -> 1;
                case "ZONAL ACCOUNTANT" -> 2;
                case "DGM" -> 3;
                default -> -1;
            };
            if (currentTypeId != -1 && currentTypeId < highestPriorityTypeId) {
                highestPriorityTypeId = currentTypeId;
            }
        }
        return highestPriorityTypeId;
    }

    @Transactional
    public synchronized void submitForm(@NonNull FormSubmissionDTO formDto) {
        int issuerUserId = formDto.getUserId();
        int receiverEmpId = formDto.getDgmEmployeeId();

        // NEW VALIDATION: Check if sender actually owns the requested application range
        // and amount
        validateSenderHasAvailableRange(formDto);

        // Validate Receiver is Active
        com.application.entity.Employee receiverEmp = employeeRepository.findById(receiverEmpId)
                .orElseThrow(() -> new RuntimeException("Receiver Employee not found"));

        if (receiverEmp.getIsActive() != 1) {
            throw new RuntimeException("Transaction Failed: The selected Receiver is Inactive.");
        }

        // 1. AUTO-DETECT TYPES (Backend Logic)
        int issuedById = getRoleTypeIdByEmpId(issuerUserId); // Who is sending?
        int issuedToId = getRoleTypeIdByEmpId(receiverEmpId); // Who is receiving?

        // 2. Check Overlaps
        int startNo = Integer.parseInt(formDto.getApplicationNoFrom());
        int endNo = Integer.parseInt(formDto.getApplicationNoTo());
        List<Distribution> overlappingDists = distributionRepository.findOverlappingDistributions(
                formDto.getAcademicYearId(), startNo, endNo);

        if (!overlappingDists.isEmpty()) {
            handleOverlappingDistributions(overlappingDists, formDto);
        }

        // 3. Create & Map (Pass BOTH types now)
        Distribution distribution = new Distribution();
        mapDtoToDistribution(distribution, formDto, issuedById, issuedToId);

        distribution.setIssued_to_emp_id(receiverEmpId);
        distribution.setIssued_to_pro_id(null);

        // --- REMAINDER CREATION LOGIC DISABLED FOR POST ---
        // DISABLED: Remainder creation should only happen during PUT (update) operations, not POST (create)
        // When Zone distributes to DGM via POST, the parent distribution (Admin→Zone) remains unchanged
        // The original 100 count stays as-is. Remainders are only created when Admin updates the distribution.
        // 
        // Original logic commented out:
        // List<Distribution> parentCandidates = distributionRepository.findParentContainingRange(
        //         issuerUserId, formDto.getAcademicYearId(), formDto.getApplication_Amount(), startNo, endNo);
        // Optional<Distribution> parentOpt = parentCandidates.stream().findFirst();
        // if (parentOpt.isPresent()) {
        //     Distribution parent = parentOpt.get();
        //     parent.setIsActive(0);
        //     // Create remainders...
        // }
        // --- REMAINDER CREATION LOGIC DISABLED FOR POST ---

        // 4. Save & Flush
        System.out.println("=== DISTRIBUTION SAVE (NEW) ===");
        System.out.println("Operation: CREATE NEW DISTRIBUTION");
        System.out.println("Range: " + distribution.getAppStartNo() + " - " + distribution.getAppEndNo());
        System.out.println("Receiver EmpId: " + distribution.getIssued_to_emp_id());
        System.out.println("Receiver ProId: " + distribution.getIssued_to_pro_id());
        System.out.println("Amount: " + distribution.getAmount());
        System.out.println("Created By: " + distribution.getCreated_by());
        System.out.println("Academic Year: " + distribution.getAcademicYear().getAcdcYearId());
        Distribution savedDist = distributionRepository.saveAndFlush(distribution);
        System.out.println("Saved Distribution ID: " + savedDist.getAppDistributionId());
        System.out.println("=================================");

        // CRITICAL: Flush any pending remainders created in
        // handleOverlappingDistributions
        // This ensures they are visible when rebuilding the sender's balance
        distributionRepository.flush();

        // 5. Recalculate Balances
        int stateId = savedDist.getState().getStateId();
        Float amount = formDto.getApplication_Amount();

        // A. Update Issuer (Sender) - CRITICAL: Always recalculate sender's balance
        System.out.println("DEBUG DGM: Recalculating sender balance for issuer: " + issuerUserId);
        recalculateBalanceForEmployee(issuerUserId, formDto.getAcademicYearId(), stateId, issuedById, issuerUserId,
                amount);

        // CRITICAL: Flush balance updates to ensure they're persisted
        balanceTrackRepository.flush();

        // B. Update Receiver (Pass the calculated issuedToId)
        addStockToReceiver(savedDist, formDto.getAcademicYearId(), issuedToId, issuerUserId, amount);
    }

    @Transactional
    public void updateForm(@NonNull Integer distributionId, @NonNull FormSubmissionDTO formDto) {

        // 1. Fetch Existing Record
        Distribution existingDistribution = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Distribution record not found with ID: " + distributionId));

        // 2. Extract Immutable Data (Preserve Amount & State!)
        Float originalAmount = existingDistribution.getAmount();
        int issuerId = formDto.getUserId();
        int academicYearId = formDto.getAcademicYearId();
        int stateId = existingDistribution.getState().getStateId();

        // 3. Identify Changes
        int oldReceiverId = existingDistribution.getIssued_to_emp_id();
        int newReceiverId = formDto.getDgmEmployeeId();
        boolean isRecipientChanging = oldReceiverId != newReceiverId;
        
        // VALIDATION: Cannot update if:
        // 1. Same campus + same employee (no change at all)
        // 2. Different campus + same employee (DGM can handle this themselves, no Zone/Admin intervention needed)
        // Get campus IDs for validation
        // IMPORTANT: Use campus from distribution/formDto, NOT from employee's campus
        Integer oldCampusId = existingDistribution.getCampus() != null ? 
                existingDistribution.getCampus().getCampusId() : null;
        
        // Get new campus ID from the form (this is the campus being selected in the update)
        Integer newCampusId = formDto.getCampusId() > 0 ? formDto.getCampusId() : null;
        
        // Block if same employee (DGM) - regardless of campus change
        // Because DGM has access to both campuses and can distribute themselves
        if (oldReceiverId == newReceiverId) {
            if (newCampusId != null && oldCampusId != null && newCampusId.equals(oldCampusId)) {
                // Same campus + same employee = no change at all
                throw new RuntimeException(
                        "Update Denied: Cannot update to the same campus and same employee. " +
                        "Please select a different employee or different campus.");
            } else if (newCampusId != null && oldCampusId != null && !newCampusId.equals(oldCampusId)) {
                // Different campus + same employee = DGM can handle this themselves
                throw new RuntimeException(
                        "Update Denied: Cannot update to a different campus for the same DGM. " +
                        "The DGM has access to both campuses and can distribute themselves. " +
                        "Please select a different employee.");
            }
        }

        int oldStart = (int) existingDistribution.getAppStartNo();
        int oldEnd = (int) existingDistribution.getAppEndNo();
        int newStart = Integer.parseInt(formDto.getApplicationNoFrom());
        int newEnd = Integer.parseInt(formDto.getApplicationNoTo());
        boolean isRangeChanging = oldStart != newStart || oldEnd != newEnd;

        // 4. AUTO-DETECT TYPES (Backend Logic)
        // We calculate these from the UserAdminView, we do NOT trust the frontend.
        int issuedById = getRoleTypeIdByEmpId(issuerId);
        int issuedToId = getRoleTypeIdByEmpId(newReceiverId); // <--- Calculated Here

        // -------------------------------------------------------
        // STEP 5: ARCHIVE OLD & SAVE NEW
        // -------------------------------------------------------

        // A. Inactivate Old (Flush to ensure DB sees it as inactive immediately)
        System.out.println("=== DISTRIBUTION UPDATE (INACTIVATE) ===");
        System.out.println("Operation: INACTIVATE EXISTING DISTRIBUTION");
        System.out.println("Distribution ID: " + existingDistribution.getAppDistributionId());
        System.out.println(
                "Old Range: " + existingDistribution.getAppStartNo() + " - " + existingDistribution.getAppEndNo());
        System.out.println("Old Receiver EmpId: " + existingDistribution.getIssued_to_emp_id());
        System.out.println("Setting isActive = 0");
        existingDistribution.setIsActive(0);
        // Update timestamp when deactivating distribution
        existingDistribution.setIssueDate(LocalDateTime.now());
        distributionRepository.saveAndFlush(existingDistribution);
        System.out.println("==========================================");

        // B. Create New Record
        Distribution newDist = new Distribution();
        // Map basic fields (Date, Zone, etc.)
        mapDtoToDistribution(newDist, formDto, issuedById, issuedToId);

        // OVERRIDE with correct Logic:
        newDist.setIssued_to_emp_id(newReceiverId); // DGM is always Employee
        newDist.setIssued_to_pro_id(null);
        newDist.setAmount(originalAmount); // CRITICAL: Preserve Original Amount

        System.out.println("=== DISTRIBUTION UPDATE (CREATE NEW) ===");
        System.out.println("Operation: CREATE NEW DISTRIBUTION (UPDATE)");
        System.out.println("New Range: " + newDist.getAppStartNo() + " - " + newDist.getAppEndNo());
        System.out.println("New Receiver EmpId: " + newDist.getIssued_to_emp_id());
        System.out.println("Amount: " + newDist.getAmount());
        System.out.println("Created By: " + newDist.getCreated_by());
        distributionRepository.saveAndFlush(newDist); // Flush new record
        System.out.println("New Distribution ID: " + newDist.getAppDistributionId());
        System.out.println("==========================================");

        // -------------------------------------------------------
        // STEP 6: HANDLE REMAINDERS (If Range Shrank)
        // IMPORTANT: Campus (Type 4) is UNDER DGM, so apps with Campus are still part of DGM's count
        // Only transfers to OTHER entities reduce DGM's count
        // COMBINE all remainder ranges into a SINGLE record
        // -------------------------------------------------------
        if (isRangeChanging) {
            System.out.println("📊 DGM Remainder Creation Analysis:");
            System.out.println("  Old Range: " + oldStart + " - " + oldEnd);
            System.out.println("  New Range: " + newStart + " - " + newEnd);
            System.out.println("  DGM Employee ID: " + existingDistribution.getIssued_to_emp_id());
            System.out.println("  Note: Campus distributions (Type 4) are still part of DGM's count");
            System.out.println("  Note: Only transfers to other entities reduce DGM's count");
            
            // Collect all remainder ranges that should be created
            java.util.List<int[]> remainderRanges = new java.util.ArrayList<>();
            
            // Check for portion BEFORE the new range
            if (oldStart < newStart) {
                int remainderStart = oldStart;
                int remainderEnd = newStart - 1;
                
                // Check if DGM distributed this range to ANOTHER entity (not Campus/Type 4)
                List<Distribution> subDists = distributionRepository.findSubDistributionsInRange(
                        existingDistribution.getIssued_to_emp_id(),
                        existingDistribution.getAcademicYear().getAcdcYearId(),
                        originalAmount,
                        remainderStart,
                        remainderEnd);
                
                // Filter: Only skip if distributed to another DGM (Type 3) or Zone (Type 2) or other entities
                // Campus (Type 4) distributions are still part of DGM's count
                List<Distribution> dgmToOtherDists = subDists.stream()
                        .filter(sub -> sub.getIssuedToType() != null && 
                                sub.getIssuedToType().getAppIssuedId() != 4) // Not Campus
                        .collect(java.util.stream.Collectors.toList());
                
                if (dgmToOtherDists.isEmpty()) {
                    // No transfers to other entities, add to remainder ranges (includes Campus distributions)
                    remainderRanges.add(new int[]{remainderStart, remainderEnd});
                    System.out.println("  ✅ Adding remainder (before): " + remainderStart + " - " + remainderEnd);
                    if (!subDists.isEmpty()) {
                        System.out.println("    (Includes " + subDists.size() + " Campus distribution(s) - still part of DGM's count)");
                    }
                } else {
                    System.out.println("  ⏭️ Skipping remainder (before): " + remainderStart + " - " + remainderEnd + 
                            " (DGM transferred " + dgmToOtherDists.size() + " sub-distribution(s) to other entity/ies)");
                }
            }
            
            // Check for portion AFTER the new range
            if (oldEnd > newEnd) {
                int remainderStart = newEnd + 1;
                int remainderEnd = oldEnd;
                
                // Check if DGM distributed this range to ANOTHER entity (not Campus/Type 4)
                List<Distribution> subDists = distributionRepository.findSubDistributionsInRange(
                        existingDistribution.getIssued_to_emp_id(),
                        existingDistribution.getAcademicYear().getAcdcYearId(),
                        originalAmount,
                        remainderStart,
                        remainderEnd);
                
                // Filter: Only skip if distributed to another DGM (Type 3) or Zone (Type 2) or other entities
                List<Distribution> dgmToOtherDists = subDists.stream()
                        .filter(sub -> sub.getIssuedToType() != null && 
                                sub.getIssuedToType().getAppIssuedId() != 4) // Not Campus
                        .collect(java.util.stream.Collectors.toList());
                
                if (dgmToOtherDists.isEmpty()) {
                    // No transfers to other entities, add to remainder ranges (includes Campus distributions)
                    remainderRanges.add(new int[]{remainderStart, remainderEnd});
                    System.out.println("  ✅ Adding remainder (after): " + remainderStart + " - " + remainderEnd);
                    if (!subDists.isEmpty()) {
                        System.out.println("    (Includes " + subDists.size() + " Campus distribution(s) - still part of DGM's count)");
                    }
                } else {
                    System.out.println("  ⏭️ Skipping remainder (after): " + remainderStart + " - " + remainderEnd + 
                            " (DGM transferred " + dgmToOtherDists.size() + " sub-distribution(s) to other entity/ies)");
                }
            }
            
            // Combine all remainder ranges into a SINGLE record
            if (!remainderRanges.isEmpty()) {
                // Sort by start number
                remainderRanges.sort((a, b) -> Integer.compare(a[0], b[0]));
                
                // Find the overall range: from first start to last end
                int combinedStart = remainderRanges.get(0)[0];
                int combinedEnd = remainderRanges.get(remainderRanges.size() - 1)[1];
                
                // Calculate total count across all remainder ranges
                int totalCount = remainderRanges.stream()
                        .mapToInt(range -> range[1] - range[0] + 1)
                        .sum();
                
                System.out.println("  📦 Combining " + remainderRanges.size() + " remainder range(s) into SINGLE record:");
                for (int[] range : remainderRanges) {
                    System.out.println("    - Range: " + range[0] + " - " + range[1] + " (" + (range[1] - range[0] + 1) + " apps)");
                }
                System.out.println("  ✅ Creating SINGLE combined remainder: " + combinedStart + " - " + combinedEnd + " (" + totalCount + " apps)");
                
                // Create a single remainder record with the full range and total count
                Distribution combinedRemainder = new Distribution();
                combinedRemainder.setAcademicYear(existingDistribution.getAcademicYear());
                combinedRemainder.setState(existingDistribution.getState());
                combinedRemainder.setCity(existingDistribution.getCity());
                combinedRemainder.setZone(existingDistribution.getZone());
                combinedRemainder.setDistrict(existingDistribution.getDistrict());
                combinedRemainder.setCampus(existingDistribution.getCampus());
                combinedRemainder.setIssuedByType(existingDistribution.getIssuedByType());
                combinedRemainder.setIssuedToType(existingDistribution.getIssuedToType());
                combinedRemainder.setCreated_by(existingDistribution.getCreated_by());
                combinedRemainder.setIssueDate(java.time.LocalDateTime.now());
                combinedRemainder.setAmount(existingDistribution.getAmount());
                combinedRemainder.setIssued_to_emp_id(existingDistribution.getIssued_to_emp_id());
                combinedRemainder.setIssued_to_pro_id(existingDistribution.getIssued_to_pro_id());
                
                // Set the combined range and count
                combinedRemainder.setAppStartNo(combinedStart);
                combinedRemainder.setAppEndNo(combinedEnd);
                combinedRemainder.setTotalAppCount(totalCount); // Use actual count, not range size
                combinedRemainder.setIsActive(1);
                
                System.out.println("=== DISTRIBUTION SAVE (COMBINED REMAINDER) - DgmService ===");
                System.out.println("Operation: CREATE SINGLE COMBINED REMAINDER");
                System.out.println("Combined Range: " + combinedRemainder.getAppStartNo() + " - " + combinedRemainder.getAppEndNo());
                System.out.println("Total Count: " + combinedRemainder.getTotalAppCount() + " apps (sum of all remainder ranges)");
                System.out.println("Receiver EmpId: " + combinedRemainder.getIssued_to_emp_id());
                System.out.println("Amount: " + combinedRemainder.getAmount());
                distributionRepository.saveAndFlush(combinedRemainder);
                System.out.println("Combined Remainder Distribution ID: " + combinedRemainder.getAppDistributionId());
                System.out.println("============================================================");
            }
        }

        // CRITICAL: Flush any pending remainders to ensure they are visible
        distributionRepository.flush();

        // -------------------------------------------------------
        // STEP 7: RECALCULATE BALANCES
        // -------------------------------------------------------

        // A. Update Issuer (Zone Officer or CO) - CRITICAL: Always recalculate sender's
        // balance
        System.out.println("DEBUG DGM UPDATE: Recalculating sender balance for issuer: " + issuerId);
        recalculateBalanceForEmployee(issuerId, academicYearId, stateId, issuedById, issuerId, originalAmount);

        // CRITICAL: Flush balance updates to ensure they're persisted
        balanceTrackRepository.flush();

        // B. Update New Receiver (DGM)
        // We use the calculated 'issuedToId' variable here
        recalculateBalanceForEmployee(
                newReceiverId,
                academicYearId,
                stateId,
                issuedToId, // <--- FIX: Using local variable
                issuerId,
                originalAmount);

        // C. Update Old Receiver (If changed)
        if (isRecipientChanging) {
            // We must find the Type ID of the old receiver to call the method correctly
            // Use LIST check to avoid crashes
            java.util.List<BalanceTrack> oldBalances = balanceTrackRepository
                    .findActiveBalancesByEmpAndAmount(academicYearId, oldReceiverId, originalAmount);

            if (!oldBalances.isEmpty()) {
                BalanceTrack oldBalance = oldBalances.get(0);
                int oldTypeId = oldBalance.getIssuedByType().getAppIssuedId();

                recalculateBalanceForEmployee(oldReceiverId, academicYearId, stateId, oldTypeId, issuerId,
                        originalAmount);
            }
        }
    }

    // ---------------------------------------------------------
    // Smart Gap Detection Logic
    // ---------------------------------------------------------
    private void addStockToReceiver(Distribution savedDist, int academicYearId, int typeId, int createdBy,
            Float amount) {
        int newStart = savedDist.getAppStartNo();
        int newEnd = savedDist.getAppEndNo();
        int newCount = savedDist.getTotalAppCount();
        int targetEnd = newStart - 1;
        int receiverId = savedDist.getIssued_to_emp_id(); // DGM is Employee

        Optional<BalanceTrack> mergeableRow = balanceTrackRepository.findMergeableRowForEmployee(
                academicYearId, receiverId, amount, targetEnd);

        if (mergeableRow.isPresent()) {
            // MERGE
            BalanceTrack existing = mergeableRow.get();
            existing.setAppTo(newEnd);
            existing.setAppAvblCnt(existing.getAppAvblCnt() + newCount);
            balanceTrackRepository.save(existing);
        } else {
            // NEW ROW
            BalanceTrack newRow = createNewBalanceTrack(receiverId, academicYearId, typeId, createdBy);
            newRow.setAmount(amount);
            newRow.setAppFrom(newStart);
            newRow.setAppTo(newEnd);
            newRow.setAppAvblCnt(newCount);
            balanceTrackRepository.save(newRow);
        }
    }

    private BalanceTrack createNewBalanceTrack(int id, int acYear, int typeId, int createdBy, boolean isPro) {
        BalanceTrack nb = new BalanceTrack();
        nb.setAcademicYear(academicYearRepository.findById(acYear).orElseThrow());
        nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
        nb.setIsActive(1);
        nb.setCreatedBy(createdBy);

        // Strict Validation logic
        if (isPro) {
            nb.setIssuedToProId(id);
            nb.setEmployee(null); // DB allows null now
        } else {
            nb.setEmployee(employeeRepository.findById(id).orElseThrow());
            nb.setIssuedToProId(null);
        }
        return nb;
    }

    private void createAndSaveRemainder(Distribution originalDist, int start, int end) {
        Distribution remainder = new Distribution();
        mapExistingToNewDistribution(remainder, originalDist);
        remainder.setIssued_to_emp_id(originalDist.getIssued_to_emp_id());
        remainder.setAppStartNo(start);
        remainder.setAppEndNo(end);
        remainder.setTotalAppCount((end - start) + 1);
        remainder.setIsActive(1);
        remainder.setAmount(originalDist.getAmount());
        System.out.println("=== DISTRIBUTION SAVE (REMAINDER) ===");
        System.out.println("Operation: CREATE REMAINDER DISTRIBUTION");
        System.out.println("Remainder Range: " + remainder.getAppStartNo() + " - " + remainder.getAppEndNo());
        System.out.println("Original Dist ID: " + originalDist.getAppDistributionId());
        System.out.println("Original Range: " + originalDist.getAppStartNo() + " - " + originalDist.getAppEndNo());
        System.out.println("Receiver EmpId: " + remainder.getIssued_to_emp_id());
        System.out.println("Amount: " + remainder.getAmount());
        distributionRepository.saveAndFlush(remainder);
        System.out.println("Remainder Distribution ID: " + remainder.getAppDistributionId());
        System.out.println("======================================");
    }

    // --- PRIVATE HELPER METHODS ---

    /**
     * Revised to use inactivation and insertion instead of update/delete.
     */
    private void handleOverlappingDistributions(List<Distribution> overlappingDists, FormSubmissionDTO request) {
        int newStart = Integer.parseInt(request.getApplicationNoFrom());
        int newEnd = Integer.parseInt(request.getApplicationNoTo());

        for (Distribution oldDist : overlappingDists) {
            int oldReceiverId = oldDist.getIssued_to_emp_id();
            if (oldReceiverId == request.getDgmEmployeeId())
                continue;

            int oldStart = oldDist.getAppStartNo();
            int oldEnd = oldDist.getAppEndNo();

            // NEW LOGIC: Keep Distribution record completely unchanged (no inactivation, no
            // remainders)
            // Distribution table: Keep original record (1-100) as-is, active, unchanged
            // BalanceTrack table: Will be updated via recalculateBalanceForEmployee to show
            // remaining (51-100)
            System.out.println("=== DISTRIBUTION (KEEP COMPLETELY UNCHANGED) ===");
            System.out.println("Operation: KEEP DISTRIBUTION RECORD COMPLETELY UNCHANGED");
            System.out.println("Distribution ID: " + oldDist.getAppDistributionId());
            System.out.println("Original Range: " + oldStart + " - " + oldEnd + " (KEEPING AS-IS, NO CHANGES)");
            System.out.println("New Distribution Range: " + newStart + " - " + newEnd);
            System.out.println("Receiver EmpId: " + oldDist.getIssued_to_emp_id());
            System.out
                    .println("NOTE: Distribution record will NOT be modified at all (no inactivation, no remainders).");
            System.out.println("NOTE: Only BalanceTrack will be updated to show remaining range (51-100).");

            // Keep original distribution record completely unchanged - no modifications at
            // all
            // No inactivation, no remainder creation
            // BalanceTrack will be calculated based on what's actually available

            System.out.println("==============================================");

            // CRITICAL: Flush remainders before recalculating balance
            distributionRepository.flush();

            // Recalculate Balance for Victim
            recalculateBalanceForEmployee(
                    oldReceiverId,
                    request.getAcademicYearId(),
                    oldDist.getState().getStateId(),
                    oldDist.getIssuedToType().getAppIssuedId(),
                    request.getUserId(),
                    oldDist.getAmount());

            // CRITICAL: Flush balance updates
            balanceTrackRepository.flush();
        }
    }

    // 2. Recalculate Logic (Updated for Amount & AdminApp)
    private void recalculateBalanceForEmployee(int employeeId, int academicYearId, int stateId, int typeId,
            int createdBy, Float amount) {

        // 1. CHECK: Is this a CO/Admin? (Check Master Table)
        Optional<AdminApp> adminApp = adminAppRepository.findByEmpAndYearAndAmount(
                employeeId, academicYearId, amount);

        if (adminApp.isPresent()) {
            // --- CASE A: CO / ADMIN (The Source) ---
            AdminApp master = adminApp.get();

            // FIX: Handle LIST return type
            List<BalanceTrack> balances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(
                    academicYearId, employeeId, amount);

            BalanceTrack balance;
            if (balances.isEmpty()) {
                balance = createNewBalanceTrack(employeeId, academicYearId, typeId, createdBy);
                balance.setAmount(amount);
            } else {
                // Admins act as a single bucket, so we pick the first one
                balance = balances.get(0);
            }

            int totalDistributed = distributionRepository.sumTotalAppCountByCreatedByAndAmount(
                    employeeId, academicYearId, amount).orElse(0);

            // CRITICAL FIX: Calculate app_from based on what they've distributed
            // If they've distributed from the beginning, app_from should be the next
            // available number
            // Otherwise, use the master start
            Optional<Integer> maxDistributedEnd = distributionRepository
                    .findMaxAppEndNoByCreatedByAndAmount(employeeId, academicYearId, amount);

            int calculatedAppFrom;
            if (maxDistributedEnd.isPresent() && maxDistributedEnd.get() >= master.getAppFromNo()) {
                // They've distributed from the beginning - app_from should be next available
                calculatedAppFrom = maxDistributedEnd.get() + 1;
                System.out.println("DEBUG DGM: Admin/CO distributed up to " + maxDistributedEnd.get() +
                        ", setting app_from to " + calculatedAppFrom);
            } else {
                // They haven't distributed from the beginning yet - use master start
                calculatedAppFrom = master.getAppFromNo();
                System.out.println("DEBUG DGM: Admin/CO hasn't distributed from beginning, using master start: "
                        + calculatedAppFrom);
            }

            // Ensure app_from doesn't exceed master end
            if (calculatedAppFrom > master.getAppToNo()) {
                calculatedAppFrom = master.getAppToNo() + 1; // All apps distributed
            }

            int availableCount = master.getTotalApp() - totalDistributed;
            balance.setAppAvblCnt(availableCount);

            // If available count is 0, set app_from = 0, app_to = 0, and is_active = 1
            if (availableCount <= 0) {
                balance.setAppFrom(0);
                balance.setAppTo(0);
            } else {
                balance.setAppFrom(calculatedAppFrom); // Use calculated start (next available or master start)
                balance.setAppTo(master.getAppToNo());
            }
            // Keep is_active = 1 even when available count is 0 (show as available 0)
            balance.setIsActive(1);

            balanceTrackRepository.saveAndFlush(balance);

        } else {
            // --- CASE B: INTERMEDIARIES (Zone/DGM) ---
            // Rebuilds rows to match gaps exactly
            rebuildBalancesFromDistributions(
                    employeeId,
                    academicYearId,
                    typeId,
                    createdBy,
                    amount // <--- Ensure you convert Float to Double here
            );
        }
    }

    // --- HELPER: Rebuild Balance Rows (Calculates Available by Subtracting Given
    // Away) ---
    // NEW LOGIC: Calculate available balance by subtracting what was given away
    // from what was received
    private void rebuildBalancesFromDistributions(int empId, int acYearId, int typeId, int createdBy, Float amount) {

        // CRITICAL: Clear persistence context
        distributionRepository.flush();

        // 1. Get ALL Active Distributions RECEIVED by this user
        List<Distribution> allReceived = distributionRepository.findActiveHoldingsForEmp(empId, acYearId);
        
        System.out.println("DEBUG DGM: rebuildBalancesFromDistributions - Employee: " + empId + 
                ", All Received Distributions (before amount filter): " + allReceived.size());
        for (Distribution d : allReceived) {
            System.out.println("  - Distribution ID: " + d.getAppDistributionId() + 
                    ", Range: " + d.getAppStartNo() + "-" + d.getAppEndNo() + 
                    ", Amount: " + d.getAmount() + 
                    ", IsActive: " + d.getIsActive());
        }

        // 2. Filter by Amount
        List<Distribution> received = allReceived.stream()
                .filter(d -> d.getAmount() != null && Math.abs(d.getAmount() - amount) < 0.01)
                .sorted((d1, d2) -> Long.compare(d1.getAppStartNo(), d2.getAppStartNo()))
                .toList();
        
        System.out.println("DEBUG DGM: rebuildBalancesFromDistributions - Employee: " + empId + 
                ", Received Distributions (after amount filter): " + received.size() + 
                ", Filter Amount: " + amount);
        for (Distribution d : received) {
            System.out.println("  - Distribution ID: " + d.getAppDistributionId() + 
                    ", Range: " + d.getAppStartNo() + "-" + d.getAppEndNo() + 
                    ", Count: " + d.getTotalAppCount());
        }

        // 3. Get ALL Distributions GIVEN AWAY by this user
        List<Distribution> allGivenAway = distributionRepository.findByCreatedByAndYear(empId, acYearId);

        // 3b. Also find distributions that OVERLAP with received ranges but have different issued_to_emp_id
        // This catches cases where Admin/Zone updated and transferred apps away from this DGM
        // Example: Admin/Zone updates DGM's distribution, taking apps and giving to another DGM/Campus
        // The new distribution has created_by = Admin/Zone, but it overlaps with DGM's received range
        // CRITICAL: Only count as "taken away" if the overlapping distribution was created AFTER the received distribution
        // OR if it represents apps that were actually taken from this DGM's original range
        List<Distribution> overlappingTakenAway = new java.util.ArrayList<>();
        for (Distribution receivedDist : received) {
            int receivedStart = (int) receivedDist.getAppStartNo();
            int receivedEnd = (int) receivedDist.getAppEndNo();
            
            // Find all active distributions that overlap with this received range
            List<Distribution> overlapping = distributionRepository.findOverlappingDistributions(
                    acYearId, receivedStart, receivedEnd);
            
            for (Distribution overlap : overlapping) {
                // Only include if:
                // 1. Same amount
                // 2. Different issued_to_emp_id (apps were given to someone else)
                // 3. Active
                // 4. Not already in allGivenAway (avoid duplicates)
                // 5. CRITICAL: The overlap must be WITHIN the received range (not just overlapping)
                //    AND it must represent apps that were actually taken from this DGM
                int overlapStart = (int) overlap.getAppStartNo();
                int overlapEnd = (int) overlap.getAppEndNo();
                
                // Check if this overlap is actually WITHIN the received range (overlap is subset of received)
                // This means: overlap.start >= receivedStart AND overlap.end <= receivedEnd
                boolean isWithinReceivedRange = overlapStart >= receivedStart && overlapEnd <= receivedEnd;
                
                // CRITICAL: EXCLUDE if the overlap CONTAINS the received range (remainder distributions)
                // Example: Received: 2875052-2875076, Overlap: 2875002-2875101 (remainder for another DGM)
                // This should NOT be counted as "given away" for this DGM
                boolean overlapContainsReceived = overlapStart <= receivedStart && overlapEnd >= receivedEnd;
                
                // Check if it was created by Admin/Zone (represents a transfer/update)
                boolean isAdminOrZoneTransfer = overlap.getCreated_by() != empId &&
                        overlap.getIssuedByType() != null &&
                        (overlap.getIssuedByType().getAppIssuedId() == 1 || // Admin/CO type
                         overlap.getIssuedByType().getAppIssuedId() == 2); // Zone type
                
                // Only count as "taken away" if:
                // 1. The overlap is WITHIN the received range (subset), AND
                // 2. It was created by Admin/Zone (transfer), AND
                // 3. It does NOT contain the received range (exclude remainders)
                boolean shouldCountAsTakenAway = isWithinReceivedRange && 
                        isAdminOrZoneTransfer && 
                        !overlapContainsReceived;
                
                if (Math.abs(overlap.getAmount() - amount) < 0.01 &&
                    overlap.getIssued_to_emp_id() != null &&
                    !overlap.getIssued_to_emp_id().equals(empId) &&
                    overlap.getIsActive() == 1 &&
                    !allGivenAway.contains(overlap) &&
                    shouldCountAsTakenAway) {
                    overlappingTakenAway.add(overlap);
                    System.out.println("DEBUG DGM: Found overlapping distribution taken away - ID: " + 
                            overlap.getAppDistributionId() + ", Range: " + 
                            overlap.getAppStartNo() + "-" + overlap.getAppEndNo() + 
                            ", Issued to: " + overlap.getIssued_to_emp_id() + 
                            ", Created by: " + overlap.getCreated_by() +
                            ", IsWithinReceived: " + isWithinReceivedRange +
                            ", IsAdminOrZoneTransfer: " + isAdminOrZoneTransfer +
                            ", OverlapContainsReceived: " + overlapContainsReceived);
                } else {
                    System.out.println("DEBUG DGM: Excluding overlapping distribution - ID: " + 
                            overlap.getAppDistributionId() + ", Range: " + 
                            overlap.getAppStartNo() + "-" + overlap.getAppEndNo() + 
                            ", Issued to: " + overlap.getIssued_to_emp_id() + 
                            ", Created by: " + overlap.getCreated_by() +
                            ", IsWithinReceived: " + isWithinReceivedRange +
                            ", IsAdminOrZoneTransfer: " + isAdminOrZoneTransfer +
                            ", OverlapContainsReceived: " + overlapContainsReceived +
                            " (not taken from this DGM)");
                }
            }
        }

        // 4. Combine both lists and filter by amount
        // NOTE: BalanceTrack represents AVAILABLE stock for distribution
        // So ALL distributions (including Campus Type 4) ARE subtracted
        // because they're no longer available for the DGM to distribute
        // The Distribution table shows total holdings (including what's with Campus)
        // But BalanceTrack shows what's actually available to distribute
        List<Distribution> givenAway = new java.util.ArrayList<>(allGivenAway);
        givenAway.addAll(overlappingTakenAway);
        givenAway = givenAway.stream()
                .filter(d -> d.getAmount() != null && Math.abs(d.getAmount() - amount) < 0.01)
                .sorted((d1, d2) -> Long.compare(d1.getAppStartNo(), d2.getAppStartNo()))
                .toList();
        
        System.out.println("DEBUG DGM: rebuildBalancesFromDistributions - Employee: " + empId
                + ", Given Away Distributions: " + givenAway.size());

        // 5. Get CURRENT Active Balance Rows for Reuse
        // REUSE STRATEGY: Instead of deactivating all, we keep them in a list/queue
        List<BalanceTrack> currentBalances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(acYearId, empId,
                amount);
        // Use a LinkedList for easy removal/popping
        java.util.LinkedList<BalanceTrack> reusePool = new java.util.LinkedList<>(currentBalances);

        boolean atLeastOneActiveRowCreated = false;

        // 7. Calculate remaining ranges
        if (received.isEmpty()) {
            System.out.println("⚠️ WARNING DGM: No received distributions found for Employee " + empId + 
                    " with amount " + amount + " in academic year " + acYearId);
            System.out.println("  This might indicate:");
            System.out.println("    1. The distribution was not created yet (timing issue)");
            System.out.println("    2. The amount doesn't match (expected: " + amount + ")");
            System.out.println("    3. The distribution is inactive");
            System.out.println("    4. The employee ID doesn't match");
            // We need a zero-balance active record
            BalanceTrack nb;
            if (!reusePool.isEmpty()) {
                nb = reusePool.poll(); // Reuse existing
            } else {
                nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy);
                nb.setAmount(amount);
            }
            nb.setAppFrom(0);
            nb.setAppTo(0);
            nb.setAppAvblCnt(0);
            nb.setIsActive(1); // CORRECT: Keep it active
            balanceTrackRepository.saveAndFlush(nb);
            atLeastOneActiveRowCreated = true;
        } else {
            for (Distribution receivedDist : received) {
                int receivedStart = (int) receivedDist.getAppStartNo();
                int receivedEnd = (int) receivedDist.getAppEndNo();

                System.out.println("DEBUG DGM: Processing received distribution: " + receivedStart + " - " + receivedEnd);

                // Find overlapping given away ranges
                List<int[]> givenAwayRanges = new java.util.ArrayList<>();
                for (Distribution given : givenAway) {
                    int givenStart = (int) given.getAppStartNo();
                    int givenEnd = (int) given.getAppEndNo();
                    
                    // Check if given away range overlaps with received range
                    if (givenStart <= receivedEnd && givenEnd >= receivedStart) {
                        // Calculate overlap
                        int overlapStart = Math.max(givenStart, receivedStart);
                        int overlapEnd = Math.min(givenEnd, receivedEnd);
                        givenAwayRanges.add(new int[] { overlapStart, overlapEnd });
                        System.out.println("DEBUG DGM: Found overlap - Given away: " + givenStart + "-" + givenEnd +
                                ", Overlaps with received: " + overlapStart + "-" + overlapEnd);
                    }
                }

                // Calculate remaining ranges
                List<int[]> remainingRanges = calculateRemainingRanges(receivedStart, receivedEnd, givenAwayRanges);

                // Create/Reuse balance tracks for remaining ranges
                for (int[] range : remainingRanges) {
                    int remainingStart = range[0];
                    int remainingEnd = range[1];
                    int remainingCount = remainingEnd - remainingStart + 1;

                    BalanceTrack nb;
                    if (!reusePool.isEmpty()) {
                        nb = reusePool.poll(); // Reuse
                    } else {
                        nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy);
                        nb.setAmount(amount);
                    }

                    nb.setAppAvblCnt(remainingCount);
                    if (remainingCount <= 0) {
                        nb.setAppFrom(0);
                        nb.setAppTo(0);
                    } else {
                        nb.setAppFrom(remainingStart);
                        nb.setAppTo(remainingEnd);
                    }
                    nb.setIsActive(1);

                    balanceTrackRepository.saveAndFlush(nb);
                    atLeastOneActiveRowCreated = true;

                    System.out.println("DEBUG DGM: Updated/Created balance row - ID: " + nb.getAppBalanceTrkId() +
                            ", Range: " + nb.getAppFrom() + "-" + nb.getAppTo());
                }
            }
        }

        // FINAL CHECK: If after processing everything, we have NO active rows (all
        // stock given away)
        if (!atLeastOneActiveRowCreated) {
            System.out.println("DEBUG DGM: All stock distributed. Ensuring one active zero-balance row.");
            BalanceTrack nb;
            if (!reusePool.isEmpty()) {
                nb = reusePool.poll();
            } else {
                nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy);
                nb.setAmount(amount);
            }
            nb.setAppAvblCnt(0);
            nb.setAppFrom(0);
            nb.setAppTo(0);
            nb.setIsActive(1);
            balanceTrackRepository.saveAndFlush(nb);
        }

        // DEACTIVATE REMAINING POOL (Clean up unused rows)
        while (!reusePool.isEmpty()) {
            BalanceTrack unused = reusePool.poll();
            unused.setIsActive(0);
            balanceTrackRepository.saveAndFlush(unused);
            System.out.println("DEBUG DGM: Deactivating unused old balance row ID: " + unused.getAppBalanceTrkId());
        }

        // Final flush
        balanceTrackRepository.flush();
        System.out.println("DEBUG DGM: Rebuilt balance rows for employee " + empId + " with amount " + amount);
    }

    // Helper method to calculate remaining ranges after subtracting given away
    // ranges
    private List<int[]> calculateRemainingRanges(int start, int end, List<int[]> givenAwayRanges) {
        List<int[]> remaining = new java.util.ArrayList<>();

        if (givenAwayRanges.isEmpty()) {
            // Nothing given away, entire range remains
            remaining.add(new int[] { start, end });
            return remaining;
        }

        // Sort given away ranges by start
        givenAwayRanges.sort((a, b) -> Integer.compare(a[0], b[0]));

        // Merge overlapping given away ranges
        List<int[]> merged = new java.util.ArrayList<>();
        for (int[] range : givenAwayRanges) {
            if (merged.isEmpty()) {
                merged.add(range);
            } else {
                int[] last = merged.get(merged.size() - 1);
                if (range[0] <= last[1] + 1) {
                    // Merge overlapping or adjacent ranges
                    last[1] = Math.max(last[1], range[1]);
                } else {
                    merged.add(range);
                }
            }
        }

        // Calculate remaining ranges
        int currentStart = start;
        for (int[] given : merged) {
            if (currentStart < given[0]) {
                // There's a gap before this given away range
                remaining.add(new int[] { currentStart, given[0] - 1 });
            }
            currentStart = Math.max(currentStart, given[1] + 1);
        }

        // Add remaining range after last given away
        if (currentStart <= end) {
            remaining.add(new int[] { currentStart, end });
        }

        return remaining;
    }

    /**
     * Helper to create a new active Distribution record based on an existing one
     * but setting the new IssuedToEmpId (the old receiver) and setting IsActive=1.
     */
    private Distribution createRemainderDistribution(Distribution originalDist, int receiverId) {
        Distribution remainderDistribution = new Distribution();
        // Copy most fields from the original distribution
        mapExistingToNewDistribution(remainderDistribution, originalDist);

        // Set specific fields for the remainder
        remainderDistribution.setIssued_to_emp_id(receiverId); // Stays with the OLD receiver
        remainderDistribution.setIsActive(1);

        // Note: The range and count will be set by the caller
        return remainderDistribution;
    }

    // FIX: Added 4th parameter 'int issuedToId'
    private void mapDtoToDistribution(Distribution distribution, FormSubmissionDTO formDto, int issuedById,
            int issuedToId) {

        int appNoFrom = Integer.parseInt(formDto.getApplicationNoFrom());
        int appNoTo = Integer.parseInt(formDto.getApplicationNoTo());

        // Map Basic Fields
        academicYearRepository.findById(formDto.getAcademicYearId()).ifPresent(distribution::setAcademicYear);
        zoneRepository.findById(formDto.getZoneId()).ifPresent(distribution::setZone);
        campusRepository.findById(formDto.getCampusId()).ifPresent(distribution::setCampus);

        cityRepository.findById(formDto.getCityId()).ifPresent(city -> {
            distribution.setCity(city);
            if (city.getDistrict() != null) {
                distribution.setDistrict(city.getDistrict());
                if (city.getDistrict().getState() != null) {
                    distribution.setState(city.getDistrict().getState());
                }
            }
        });

        // --- FIX: Use the passed arguments for Types ---
        appIssuedTypeRepository.findById(issuedById).ifPresent(distribution::setIssuedByType);
        appIssuedTypeRepository.findById(issuedToId).ifPresent(distribution::setIssuedToType);
        // ----------------------------------------------

        distribution.setAppStartNo(appNoFrom);
        distribution.setAppEndNo(appNoTo);
        distribution.setTotalAppCount(formDto.getRange());
        distribution.setAmount(formDto.getApplication_Amount());

        // Fix for Date (Always use Now to prevent nulls)
        distribution.setIssueDate(LocalDateTime.now());

        distribution.setIsActive(1);
        distribution.setCreated_by(formDto.getUserId());

        // Note: We set issued_to_emp_id in the main method, not here.
    }

    private void mapExistingToNewDistribution(Distribution newDist, Distribution oldDist) {
        // ... (Copy standard fields) ...
        newDist.setAcademicYear(oldDist.getAcademicYear());
        newDist.setState(oldDist.getState());
        newDist.setDistrict(oldDist.getDistrict());
        newDist.setCity(oldDist.getCity());
        newDist.setZone(oldDist.getZone());
        newDist.setCampus(oldDist.getCampus());
        newDist.setIssuedByType(oldDist.getIssuedByType());
        newDist.setIssuedToType(oldDist.getIssuedToType());
        newDist.setIssued_to_emp_id(oldDist.getIssued_to_emp_id());
        newDist.setAppStartNo(oldDist.getAppStartNo());
        newDist.setAppEndNo(oldDist.getAppEndNo());
        newDist.setTotalAppCount(oldDist.getTotalAppCount());
        // Set current timestamp for new distribution
        newDist.setIssueDate(LocalDateTime.now());
        newDist.setIsActive(1);
        newDist.setCreated_by(oldDist.getCreated_by());
    }

    private BalanceTrack createNewBalanceTrack(int employeeId, int academicYearId, int typeId, int createdBy) {
        BalanceTrack nb = new BalanceTrack();
        nb.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
        nb.setAcademicYear(academicYearRepository.findById(academicYearId).orElseThrow());
        nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
        nb.setAppAvblCnt(0);
        nb.setIsActive(1);
        nb.setCreatedBy(createdBy);
        nb.setIssuedToProId(null); // Strict Validation for Employee
        return nb;
    }

    /**
     * Validates that the sender (userId) actually owns the requested application
     * range.
     * Checks BalanceTrack for Zone/DGM employees or AdminApp for Admin/CO
     * employees.
     * Also validates that the amount matches available balances.
     *
     * @param formDto The form submission DTO containing the range to validate
     * @throws RuntimeException if the requested range is not available, with
     *                          details of available ranges
     */
    private void validateSenderHasAvailableRange(@NonNull FormSubmissionDTO formDto) {
        int senderId = formDto.getUserId();
        int academicYearId = formDto.getAcademicYearId();
        Float amount = formDto.getApplication_Amount();
        int requestedStart = Integer.parseInt(formDto.getApplicationNoFrom());
        int requestedEnd = Integer.parseInt(formDto.getApplicationNoTo());

        // Validate amount is provided and positive
        if (amount == null) {
            throw new RuntimeException("Validation Failed: Application Amount cannot be null.");
        }
        if (amount < 0) {
            throw new RuntimeException("Validation Failed: Application Amount cannot be negative. Provided: " + amount);
        }

        // Validate range is valid
        if (requestedStart > requestedEnd) {
            throw new RuntimeException("Invalid range: Start number (" + requestedStart +
                    ") cannot be greater than End number (" + requestedEnd + ")");
        }

        // Check if sender is Admin/CO (has AdminApp record)
        Optional<AdminApp> adminApp = adminAppRepository.findByEmpAndYearAndAmount(
                senderId, academicYearId, amount);

        if (adminApp.isPresent()) {
            // CASE A: Admin/CO - Check against AdminApp master record
            AdminApp master = adminApp.get();

            // Validate amount matches AdminApp record
            Double masterAmount = master.getApp_amount();
            Integer masterFee = master.getApp_fee();
            boolean amountMatches = false;

            if (masterAmount != null && Math.abs(masterAmount - amount) < 0.01) {
                amountMatches = true;
            } else if (masterFee != null && Math.abs(masterFee - amount) < 0.01) {
                amountMatches = true;
            }

            if (!amountMatches) {
                throw new RuntimeException(
                        "Validation Failed: Requested amount (" + amount +
                                ") does not match Admin/CO master record. " +
                                "Master Amount: " + (masterAmount != null ? masterAmount : "null") +
                                ", Master Fee: " + (masterFee != null ? masterFee : "null"));
            }

            int masterStart = master.getAppFromNo();
            int masterEnd = master.getAppToNo();

            // Calculate what they've already distributed
            int totalDistributed = distributionRepository
                    .sumTotalAppCountByCreatedByAndAmount(senderId, academicYearId, amount).orElse(0);

            // Calculate next available number
            Optional<Integer> maxDistributedEnd = distributionRepository
                    .findMaxAppEndNoByCreatedByAndAmount(senderId, academicYearId, amount);

            int availableStart;
            if (maxDistributedEnd.isPresent() && maxDistributedEnd.get() >= masterStart) {
                availableStart = maxDistributedEnd.get() + 1;
            } else {
                availableStart = masterStart;
            }
            int availableEnd = masterEnd;

            // Validate requested range is within available range
            if (requestedStart < availableStart || requestedEnd > availableEnd) {
                throw new RuntimeException(
                        "Validation Failed: You cannot distribute range " + requestedStart + "-" + requestedEnd +
                                ". Available range for distribution: " + availableStart + "-" + availableEnd +
                                " (Master: " + masterStart + "-" + masterEnd + ", Already distributed: "
                                + totalDistributed + " applications)");
            }

            // Check if range count matches
            int requestedCount = requestedEnd - requestedStart + 1;
            int availableCount = availableEnd - availableStart + 1;
            if (requestedCount > availableCount) {
                throw new RuntimeException(
                        "Validation Failed: Requested count (" + requestedCount +
                                ") exceeds available count (" + availableCount +
                                "). Available range: " + availableStart + "-" + availableEnd);
            }

            System.out.println("DEBUG: Admin/CO validation passed - Requested: " + requestedStart + "-" + requestedEnd +
                    ", Available: " + availableStart + "-" + availableEnd);
            return;
        }

        // CASE B: Zone/DGM - Check against BalanceTrack
        List<BalanceTrack> availableBalances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(
                academicYearId, senderId, amount);

        if (availableBalances.isEmpty()) {
            // Check if employee has balances with different amounts
            List<Float> availableAmounts = balanceTrackRepository.findAmountsByEmpIdAndAcademicYear(senderId,
                    academicYearId);
            if (availableAmounts != null && !availableAmounts.isEmpty()) {
                StringBuilder amountsList = new StringBuilder();
                for (Float amt : availableAmounts) {
                    if (amountsList.length() > 0) {
                        amountsList.append(", ");
                    }
                    amountsList.append(amt);
                }
                throw new RuntimeException(
                        "Validation Failed: You have no available applications with amount " + amount +
                                " to distribute. Available amounts for this employee: " + amountsList.toString() +
                                ". Employee ID: " + senderId + ", Academic Year: " + academicYearId);
            } else {
                throw new RuntimeException(
                        "Validation Failed: You have no available applications to distribute. " +
                                "Employee ID: " + senderId + ", Academic Year: " + academicYearId + ", Amount: "
                                + amount);
            }
        }

        // Validate amount matches the balance track amounts
        boolean amountMatches = false;
        for (BalanceTrack balance : availableBalances) {
            if (balance.getAmount() != null && Math.abs(balance.getAmount() - amount) < 0.01) {
                amountMatches = true;
                break;
            }
        }

        if (!amountMatches) {
            StringBuilder amountsList = new StringBuilder();
            for (BalanceTrack balance : availableBalances) {
                if (amountsList.length() > 0) {
                    amountsList.append(", ");
                }
                amountsList.append(balance.getAmount());
            }
            throw new RuntimeException(
                    "Validation Failed: Requested amount (" + amount +
                            ") does not match any available balance. Available amounts: " + amountsList.toString());
        }

        // Check if requested range falls within any available balance range
        boolean rangeFound = false;
        StringBuilder availableRanges = new StringBuilder();

        for (BalanceTrack balance : availableBalances) {
            int balanceStart = balance.getAppFrom();
            int balanceEnd = balance.getAppTo();
            int balanceCount = balance.getAppAvblCnt();

            if (availableRanges.length() > 0) {
                availableRanges.append(", ");
            }
            availableRanges.append(balanceStart).append("-").append(balanceEnd).append(" (Count: ").append(balanceCount)
                    .append(")");

            // Check if entire requested range is within this balance range
            if (requestedStart >= balanceStart && requestedEnd <= balanceEnd) {
                // Check if there's enough count available
                if (requestedEnd - requestedStart + 1 <= balanceCount) {
                    rangeFound = true;
                    System.out.println(
                            "DEBUG: Range validation passed - Requested: " + requestedStart + "-" + requestedEnd +
                                    ", Found in Balance: " + balanceStart + "-" + balanceEnd + " (Available: "
                                    + balanceCount + ")");
                    break;
                }
            }
        }

        if (!rangeFound) {
            throw new RuntimeException(
                    "Validation Failed: You cannot distribute range " + requestedStart + "-" + requestedEnd +
                            ". Available ranges: " + availableRanges.toString() +
                            ". Please select a range from the available ranges above.");
        }
    }

}