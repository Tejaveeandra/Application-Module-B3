package com.application.controller;
 
import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.application.dto.ApiResponse;
import com.application.dto.AppSeriesDTO;
import com.application.dto.EmployeesDto;
import com.application.dto.GenericDropdownDTO;
import com.application.dto.LocationAutoFillDTO;
import com.application.dto.EmployeeLocationDTO;
import com.application.dto.DgmWithCampusesDTO;
import com.application.entity.AcademicYear;
import com.application.entity.BalanceTrack;
import com.application.entity.City;
import com.application.entity.District;
import com.application.entity.State;
import com.application.entity.Zone;
import com.application.repository.BalanceTrackRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.EmployeeRepository;
import com.application.repository.SchoolDetailsRepository;
import com.application.entity.Distribution;
import com.application.service.CampusService;
import com.application.service.DgmService;
import com.application.service.ZoneService;
import com.application.service.DistributionGetTableService;
 
@RestController
@RequestMapping("/distribution/gets")
//@CrossOrigin(origins = "*")
public class DistributionGet {
 
    @Autowired
    EmployeeRepository employeeRepository;
   
    @Autowired
    DistributionGetTableService distributionGetTableService;
   
    @Autowired
    CampusService campusService;
   
    @Autowired
    private ZoneService distributionService;
   
    @Autowired private BalanceTrackRepository balanceTrackRepository;
    
    @Autowired private DistributionRepository distributionRepository;
   
    DistributionGet(SchoolDetailsRepository schoolDetailsRepository) {
    }
   
    @GetMapping("/academic-years")//used/c
    public ResponseEntity<List<AcademicYear>> getAcademicYears() {
        return ResponseEntity.ok(distributionService.getAllAcademicYears());
    }
 
    @GetMapping("/states")//used/c
    public ResponseEntity<List<State>> getStates() {
        return ResponseEntity.ok(distributionService.getAllStates());
    }
 
    @GetMapping("/city/{stateId}")//used/c
    public ResponseEntity<List<City>> getCitiesByState(@PathVariable int stateId) {
        return ResponseEntity.ok(distributionService.getCitiesByState(stateId));
    }
 
    @GetMapping("/zones/{cityId}")//used/c
    public ResponseEntity<List<Zone>> getZonesByCity(
            @PathVariable int cityId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(distributionService.getZonesByCity(cityId, category));
    }
   
     @GetMapping("/{empId}/mobile")
        public String getMobileByEmpId(@PathVariable int empId) {
            return distributionGetTableService.getMobileByCmpsEmpId(empId);
        }
     
       // GET /api/zonal-accountants/zone/1/employees
        @GetMapping("/zone/{zoneId}/employees")//used/
        public List<EmployeesDto> getEmployeesByZone(
                @PathVariable int zoneId,
                @RequestParam(required = false) String category) {
            return distributionService.getEmployeesByZone(zoneId, category);
        }
     
        @Autowired
        private DgmService applicationService;
     
        @GetMapping("/cities")//used/c
        public List<GenericDropdownDTO> getCities() {
            return applicationService.getAllCities();
        }
     
        @GetMapping("/campus/{zoneId}")//used/c
        public List<GenericDropdownDTO> getCampusesByZone(@PathVariable int zoneId) {
            return applicationService.getCampusesByZoneId(zoneId);
        }
       
        @GetMapping("/campusesforzonal_accountant/{empId}")//used/c
        public List<GenericDropdownDTO> getCampusesByEmployee(@PathVariable int empId) {
            return applicationService.getCampusesByEmployeeId(empId);
        }
       
        @GetMapping("/campusesforzonal_accountant_with_category/{empId}")
        public List<GenericDropdownDTO> getCampusesByEmployee(
                @PathVariable int empId,
                @RequestParam(required = false) String category) {
 
            return applicationService.getCampusesByEmployeeIdAndCategory(empId, category);
        }
 
       
        @GetMapping("/campusesfordgm/{empId}")//used
        public List<GenericDropdownDTO> getActiveCampusesByEmpId(@PathVariable Integer empId) {
            return applicationService.getActiveCampusesByEmpId(empId);
        }
       
        @GetMapping("/campusesfordgm_with_category/{empId}")
        public List<GenericDropdownDTO> getActiveCampusesByEmpId(
                @PathVariable Integer empId,
                @RequestParam(required = false) String category) {
 
            return applicationService.getActiveCampusesByEmpIdAndCategory(empId, category);
        }
 
       
        @GetMapping("/dgmforzonal_accountant/{empId}")//used
        public List<GenericDropdownDTO> getActiveCampusesByEmployee(@PathVariable int empId) {
            return applicationService.getActiveCampusesByEmployeeId(empId);
        }
       
        @GetMapping("/dgmforzonal_accountant_with_category/{empId}")
        public List<GenericDropdownDTO> getActiveCampusesByEmployee(
                @PathVariable int empId,
                @RequestParam(required = false) String category) {
 
            return applicationService.getActiveCampusesByEmployeeIdAndCategory(empId, category);
        }
 
       
        @GetMapping("/issued-to")//used
        public List<GenericDropdownDTO> getIssuedToTypes() {
            return applicationService.getAllIssuedToTypes();
        }
       
        @GetMapping("/mobile-no/{empId}")//used/n
        public ResponseEntity<String> getMobileNo(@PathVariable int empId) {
            String mobileNumber = applicationService.getMobileNumberByEmpId(empId);
            if (mobileNumber != null) {
                return ResponseEntity.ok(mobileNumber);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
       
        @Autowired
        private CampusService dgmService;
       
     
        @GetMapping("/districts/{stateId}")//used
        public List<GenericDropdownDTO> getDistrictsByState(@PathVariable int stateId) {
            return dgmService.getDistrictsByStateId(stateId);
        }
     
        @GetMapping("/cities/{districtId}")//used/
        public List<GenericDropdownDTO> getCitiesByDistrict(
                @PathVariable int districtId,
                @RequestParam(required = false) String category) {
            return dgmService.getCitiesByDistrictId(districtId, category);
        }
       
        @GetMapping("/campuses/{cityId}")//used/
        public List<GenericDropdownDTO> getCampusesByCity(@PathVariable int cityId) {
            return dgmService.getCampusesByCityId(cityId);
        }
       
        @GetMapping("/campaign-areas")//used
        public List<GenericDropdownDTO> getAllCampaignAreas() {
            return dgmService.getAllCampaignAreas();
        }
         
        @GetMapping("/pros/{campusId}")//used/c
        public ResponseEntity<List<GenericDropdownDTO>> getEmployeeDropdown(
                @PathVariable int campusId) {

            // 1. Call the service layer method to execute the JPQL query
            // This method filters by campusId and returns only active PROs (is_active = 1)
            List<GenericDropdownDTO> employees = campusService.getEmployeeDropdownByCampus(campusId);

            // 2. Return the list with an HTTP 200 OK status
            if (employees.isEmpty()) {
                // Optional: Return 204 No Content or an empty list if no results
                return ResponseEntity.ok(employees);
            }
            return ResponseEntity.ok(employees);
        }
       
        @GetMapping("/getalldistricts")//used/
        public List<District> getAllDistricts()
        {
            return campusService.getAllDistricts();
        }
       
        @GetMapping("/{campaignId}/campus")
        public ResponseEntity<List<GenericDropdownDTO>> getCampusByCampaign(@PathVariable int campaignId) {
            List<GenericDropdownDTO> campus = campusService.getCampusByCampaignId(campaignId);
            return ResponseEntity.ok(campus);
        }
       
        @GetMapping("/getarea/{cityId}")
        public ResponseEntity<List<GenericDropdownDTO>> getCampaignsByCity(@PathVariable int cityId) {
            List<GenericDropdownDTO> campaigns = campusService.getCampaignsByCityId(cityId);
            return ResponseEntity.ok(campaigns);
        }
       
        @GetMapping("/dgm/{campusId}")//used/c
        public ResponseEntity<List<GenericDropdownDTO>> getActiveDgmEmployeesByCampus(
                @PathVariable int campusId) { // Use @PathVariable
 
            // Call the service method to fetch the data
            List<GenericDropdownDTO> dgmEmployees = applicationService.getDgmEmployeesForCampus(campusId);
 
            if (dgmEmployees.isEmpty()) {
                // Return 404 Not Found if no employees are found
                return ResponseEntity.notFound().build();
            }
 
            // Return the list with HTTP 200 OK
            return ResponseEntity.ok(dgmEmployees);
        }
       
        @GetMapping("/getallamounts/{empId}/{academicYearId}") // UPDATED PATH
        public ResponseEntity<ApiResponse<List<Double>>> getFeeDropdown(
            @PathVariable int empId,
            @PathVariable int academicYearId // NEW PATH VARIABLE
        ) {
            List<Double> fees = applicationService.getApplicationFees(empId, academicYearId);
           
            // Check if fees list is null or empty
            if (fees == null || fees.isEmpty()) {
                ApiResponse<List<Double>> response = ApiResponse.error(
                    "Application fees are not assigned to that particular employee"
                );
                return ResponseEntity.ok(response);
            }
           
            // Return success response with data
            ApiResponse<List<Double>> response = ApiResponse.success(
                fees,
                "Application fees retrieved successfully"
            );
            return ResponseEntity.ok(response);
        }
       
        @GetMapping("/district_city_autopopulate/{empId}/{category}")
        public ResponseEntity<LocationAutoFillDTO> autoFill(
                @PathVariable int empId,
                @PathVariable String category) {
 
            LocationAutoFillDTO dto = applicationService.getAutoPopulateData(empId, category);
 
            return ResponseEntity.ok(dto);
        }
       
        @GetMapping("/track")
         public ResponseEntity<BalanceTrack> getBalanceTrackByEmployeeAndYear(
                 @RequestParam int academicYearId,
                 @RequestParam int employeeId) {
 
             Optional<BalanceTrack> balanceTrackOptional = balanceTrackRepository.findBalanceTrack(academicYearId, employeeId);
 
             if (balanceTrackOptional.isPresent()) {
                 return ResponseEntity.ok(balanceTrackOptional.get());
             } else {
                 return ResponseEntity.notFound().build();
             }
         }
         
         @GetMapping("/get-series")
         public ResponseEntity<List<AppSeriesDTO>> getSeriesDropdown(
                 @RequestParam int receiverId,
                 @RequestParam int academicYearId,
                 @RequestParam Double amount,
                 @RequestParam boolean isPro) { // Frontend passes true if receiver is PRO
                 
             return ResponseEntity.ok(applicationService.getActiveSeriesForReceiver(receiverId,academicYearId, amount, isPro));
         }
 
         // 2. Get the Distribution ID (Call this when user selects a Series)
         @GetMapping("/get-distribution-id")
         public ResponseEntity<Integer> getDistributionId(
                 @RequestParam int receiverId,
                 @RequestParam int start,
                 @RequestParam int end,
                 @RequestParam Double amount,
                 @RequestParam boolean isPro) {
                 
             return ResponseEntity.ok(applicationService.getDistributionIdBySeries(receiverId, start, end, amount, isPro));
         }
         
         @GetMapping("/dgmforzone_with_category_college/{zoneId}/{category}")
         public List<GenericDropdownDTO> getCampusesForZone(
                 @PathVariable Integer zoneId,
                 @RequestParam(required = false) String category) {
 
             return applicationService.getDgmCampusesByZoneAndCategory(zoneId, category);
         }
         
         @GetMapping("/campuses/category/{cityId}")
            public ResponseEntity<List<GenericDropdownDTO>> getCampuses(
                    @RequestParam(required = true) String category,
                    @PathVariable Integer cityId) {
   
                return ResponseEntity.ok(
                   campusService.fetchCampusesByCityAndCategory(category, cityId)
                );
            }
         
         @GetMapping("/employee-location/{employeeId}")
         public ResponseEntity<?> getEmployeeLocation(
                 @PathVariable int employeeId,
                 @RequestParam(required = true) String cmpsCategory,
                 @RequestParam(required = false, defaultValue = "false") boolean returnMessage) {
             
            // If returnMessage is true, return an empty object
            if (returnMessage) {
                EmployeeLocationDTO emptyLocation = new EmployeeLocationDTO();
                return ResponseEntity.ok(emptyLocation);
            }
             
             // If returnMessage is false, return the location data
             EmployeeLocationDTO location = applicationService.getEmployeeLocationByCategory(employeeId, cmpsCategory);
             
             if (location == null) {
                 return ResponseEntity.notFound().build();
             }
             
            return ResponseEntity.ok(location);
        }
        
        @GetMapping("/dgm-with-campuses/{employeeId}")
        public ResponseEntity<List<DgmWithCampusesDTO>> getDgmWithCampuses(@PathVariable Integer employeeId) {
            List<DgmWithCampusesDTO> result = applicationService.getDgmWithCampusesByEmployeeId(employeeId);
            return ResponseEntity.ok(result);
        }

        @GetMapping("/next-available-no")
        @Transactional(readOnly = true)
        public ResponseEntity<Integer> getNextAvailableNo(
                @RequestParam int academicYearId,
                @RequestParam int employeeId,
                @RequestParam Float amount) {
            
            try {
                System.out.println("========================================");
                System.out.println("📋 NEXT AVAILABLE NUMBER API CALLED");
                System.out.println("========================================");
                System.out.println("Input Parameters:");
                System.out.println("  - Academic Year ID: " + academicYearId);
                System.out.println("  - Employee ID (Sender/Issuer): " + employeeId);
                System.out.println("  - Amount: " + amount);
                System.out.println("----------------------------------------");
                System.out.flush();
                
                // First, get all matching BalanceTrack records to show what data exists
                System.out.println("🔍 Step 1: Finding all matching BalanceTrack records...");
                System.out.println("Query: SELECT b FROM BalanceTrack b");
                System.out.println("  WHERE b.employee.emp_id = " + employeeId);
                System.out.println("    AND b.academicYear.acdcYearId = " + academicYearId);
                System.out.println("    AND b.amount = " + amount);
                System.out.println("    AND b.isActive = 1");
                System.out.println("    AND b.appAvblCnt > 0");
                System.out.flush();
                
                List<BalanceTrack> matchingRecords = balanceTrackRepository.findActiveBalancesByEmpAndAmountReadOnly(
                        academicYearId, employeeId, amount);
                
                System.out.println("----------------------------------------");
                System.out.println("📊 Matching BalanceTrack Records Found: " + matchingRecords.size());
                System.out.flush();
                
                if (matchingRecords.isEmpty()) {
                    System.out.println("  ❌ No records found in BalanceTrack table (sce_app_balance_trk)");
                    System.out.println("  Table: sce_application.sce_app_balance_trk");
                    System.out.println("  Filters:");
                    System.out.println("    - emp_id = " + employeeId);
                    System.out.println("    - acdc_year_id = " + academicYearId);
                    System.out.println("    - amount = " + amount);
                    System.out.println("    - is_active = 1");
                    System.out.println("    - app_avbl_cnt > 0");
                } else {
                    System.out.println("  ✅ Found " + matchingRecords.size() + " record(s):");
                    for (int i = 0; i < matchingRecords.size(); i++) {
                        BalanceTrack bt = matchingRecords.get(i);
                        System.out.println("  [" + (i + 1) + "] BalanceTrack ID: " + bt.getAppBalanceTrkId());
                        System.out.println("      - app_from: " + bt.getAppFrom());
                        System.out.println("      - app_to: " + bt.getAppTo());
                        System.out.println("      - app_avbl_cnt: " + bt.getAppAvblCnt());
                        System.out.println("      - amount: " + bt.getAmount());
                        System.out.println("      - is_active: " + bt.getIsActive());
                    }
                }
                System.out.flush();
                
                // Now get the MIN value
                System.out.println("----------------------------------------");
                System.out.println("🔍 Step 2: Finding MIN(appFrom) from matching records...");
                System.out.println("Query: SELECT MIN(b.appFrom) FROM BalanceTrack b");
                System.out.println("  WHERE b.employee.emp_id = " + employeeId);
                System.out.println("    AND b.academicYear.acdcYearId = " + academicYearId);
                System.out.println("    AND b.amount = " + amount);
                System.out.println("    AND b.isActive = 1");
                System.out.println("    AND b.appAvblCnt > 0");
                System.out.flush();
                
                Integer nextAvailable = balanceTrackRepository.findNextAvailableStart(academicYearId, employeeId, amount)
                        .orElse(0);
                
                System.out.println("----------------------------------------");
                System.out.println("✅ Final Result:");
                if (nextAvailable != null && nextAvailable > 0) {
                    System.out.println("  Next Available Start Number: " + nextAvailable);
                    System.out.println("  Source: BalanceTrack table (sce_app_balance_trk)");
                    System.out.println("  Calculation: MIN(appFrom) from " + matchingRecords.size() + " matching record(s)");
                    if (!matchingRecords.isEmpty()) {
                        System.out.println("  Selected from record(s) with appFrom values:");
                        matchingRecords.forEach(bt -> 
                            System.out.println("    - " + bt.getAppFrom() + " (BalanceTrack ID: " + bt.getAppBalanceTrkId() + ")")
                        );
                        System.out.println("  Minimum value: " + nextAvailable);
                    }
                } else {
                    System.out.println("  Next Available Start Number: 0");
                    System.out.println("  Reason: No matching records found or all have appFrom = 0");
                    System.out.println("  Check BalanceTrack table for:");
                    System.out.println("    - emp_id = " + employeeId);
                    System.out.println("    - acdc_year_id = " + academicYearId);
                    System.out.println("    - amount = " + amount);
                    System.out.println("    - is_active = 1");
                    System.out.println("    - app_avbl_cnt > 0");
                }
                System.out.println("========================================");
                System.out.flush();
                
                return ResponseEntity.ok(nextAvailable);
            } catch (Exception e) {
                System.out.println("❌ ERROR in next-available-no API:");
                System.out.println("  Exception: " + e.getClass().getName());
                System.out.println("  Message: " + e.getMessage());
                e.printStackTrace();
                System.out.flush();
                return ResponseEntity.ok(0);
            }
        }

        /**
         * Get next available number for the RECEIVER of a distribution record
         * This is useful when updating a distribution - you need to know what the receiver
         * actually has available after their sub-distributions
         * 
         * Example: Admin→Zone distribution, Zone has distributed some to DGM,
         * this returns what Zone has available now (from BalanceTrack)
         * 
         * @param distributionId The distribution ID to look up
         * @param appStartNo Optional: Filter BalanceTrack records to only those that overlap with this start number
         * @param appEndNo Optional: Filter BalanceTrack records to only those that overlap with this end number
         */
        @GetMapping("/next-available-no-by-distribution/{distributionId}")
        @Transactional(readOnly = true)
        public ResponseEntity<Integer> getNextAvailableNoByDistribution(
                @PathVariable int distributionId,
                @RequestParam(required = true) Integer appStartNo,
                @RequestParam(required = true) Integer appEndNo) {
            
            try {
                System.out.println("========================================");
                System.out.println("📋 NEXT AVAILABLE NUMBER BY DISTRIBUTION ID API CALLED");
                System.out.println("========================================");
                System.out.println("Input Parameter:");
                System.out.println("  - Distribution ID: " + distributionId);
                System.out.println("----------------------------------------");
                System.out.flush();
                
                // 1. Fetch the distribution record
                Optional<Distribution> distOpt = distributionRepository.findById(distributionId);
                if (distOpt.isEmpty()) {
                    System.out.println("❌ Distribution record not found with ID: " + distributionId);
                    System.out.println("========================================");
                    System.out.flush();
                    return ResponseEntity.ok(0);
                }
                
                Distribution distribution = distOpt.get();
                Integer receiverEmpId = distribution.getIssued_to_emp_id();
                Integer academicYearId = distribution.getAcademicYear() != null ? 
                        distribution.getAcademicYear().getAcdcYearId() : null;
                Float amount = distribution.getAmount();
                
                System.out.println("📊 Distribution Record Details:");
                System.out.println("  - Distribution ID: " + distribution.getAppDistributionId());
                System.out.println("  - Range: " + distribution.getAppStartNo() + " - " + distribution.getAppEndNo());
                System.out.println("  - Receiver Employee ID: " + receiverEmpId);
                System.out.println("  - Academic Year ID: " + academicYearId);
                System.out.println("  - Amount: " + amount);
                System.out.println("----------------------------------------");
                System.out.flush();
                
                if (receiverEmpId == null) {
                    System.out.println("❌ No receiver employee ID found in distribution record");
                    System.out.println("  This distribution may be for a PRO (issued_to_pro_id)");
                    System.out.println("========================================");
                    System.out.flush();
                    return ResponseEntity.ok(0);
                }
                
                if (academicYearId == null) {
                    System.out.println("❌ No academic year ID found in distribution record");
                    System.out.println("========================================");
                    System.out.flush();
                    return ResponseEntity.ok(0);
                }
                
                if (amount == null) {
                    System.out.println("❌ No amount found in distribution record");
                    System.out.println("========================================");
                    System.out.flush();
                    return ResponseEntity.ok(0);
                }
                
                // 2. Get all matching BalanceTrack records for the RECEIVER
                System.out.println("🔍 Step 1: Finding BalanceTrack records for RECEIVER...");
                System.out.println("Receiver Employee ID: " + receiverEmpId);
                System.out.println("Query: SELECT b FROM BalanceTrack b");
                System.out.println("  WHERE b.employee.emp_id = " + receiverEmpId);
                System.out.println("    AND b.academicYear.acdcYearId = " + academicYearId);
                System.out.println("    AND b.amount = " + amount);
                System.out.println("    AND b.isActive = 1");
                System.out.println("    AND b.appAvblCnt > 0");
                System.out.flush();
                
                List<BalanceTrack> matchingRecords = balanceTrackRepository.findActiveBalancesByEmpAndAmountReadOnly(
                        academicYearId, receiverEmpId, amount);
                
                System.out.println("----------------------------------------");
                System.out.println("📊 Matching BalanceTrack Records Found (before range filter): " + matchingRecords.size());
                System.out.flush();
                
                // Filter by range if provided
                if (appStartNo != null && appEndNo != null) {
                    System.out.println("🔍 Filtering BalanceTrack records by range: " + appStartNo + " - " + appEndNo);
                    matchingRecords = matchingRecords.stream()
                            .filter(bt -> {
                                // Check if BalanceTrack range overlaps with the requested range
                                int btStart = bt.getAppFrom();
                                int btEnd = bt.getAppTo();
                                boolean overlaps = btStart <= appEndNo && btEnd >= appStartNo;
                                if (overlaps) {
                                    System.out.println("  ✅ BalanceTrack ID: " + bt.getAppBalanceTrkId() + 
                                            ", Range: " + btStart + "-" + btEnd + " overlaps with " + 
                                            appStartNo + "-" + appEndNo);
                                } else {
                                    System.out.println("  ⏭️ BalanceTrack ID: " + bt.getAppBalanceTrkId() + 
                                            ", Range: " + btStart + "-" + btEnd + " does NOT overlap with " + 
                                            appStartNo + "-" + appEndNo);
                                }
                                return overlaps;
                            })
                            .collect(java.util.stream.Collectors.toList());
                    System.out.println("📊 Matching BalanceTrack Records Found (after range filter): " + matchingRecords.size());
                    System.out.flush();
                }
                
                // Get the minimum appFrom from the records we already fetched
                Integer nextAvailable = 0;
                if (!matchingRecords.isEmpty()) {
                    nextAvailable = matchingRecords.stream()
                            .mapToInt(BalanceTrack::getAppFrom)
                            .min()
                            .orElse(0);
                    
                    System.out.println("  ✅ Found " + matchingRecords.size() + " record(s) for RECEIVER:");
                    for (int i = 0; i < matchingRecords.size(); i++) {
                        BalanceTrack bt = matchingRecords.get(i);
                        System.out.println("  [" + (i + 1) + "] BalanceTrack ID: " + bt.getAppBalanceTrkId());
                        System.out.println("      - app_from: " + bt.getAppFrom());
                    }
                    System.out.println("----------------------------------------");
                    System.out.println("✅ Final Result: " + nextAvailable);
                    System.out.println("  Source: BalanceTrack table (sce_app_balance_trk)");
                    System.out.println("========================================");
                } else {
                    System.out.println("  ❌ No records found in BalanceTrack table for RECEIVER");
                    System.out.println("========================================");
                }
                System.out.flush();
                
                return ResponseEntity.ok(nextAvailable);
            } catch (Exception e) {
                System.out.println("❌ ERROR in next-available-no-by-distribution API:");
                System.out.println("  Exception: " + e.getClass().getName());
                System.out.println("  Message: " + e.getMessage());
                e.printStackTrace();
                System.out.flush();
                return ResponseEntity.ok(0);
            }
        }

        /**
         * Get continuous series of applications with status "WITH PRO" within a range
         * Splits the range into multiple series if there are gaps (non-WITH PRO statuses)
         * 
         * Example: Range 1-10, but app 5 is sold/unavailable/damaged/confirmed
         * Returns: [1-4] and [6-10] as separate series
         * 
         * @param distributionId The distribution ID to look up
         * @param appStartNo Start number of the range to check
         * @param appEndNo End number of the range to check
         * @param issuedToTypeId The issued to type ID (to filter by receiver type)
         * @return List of continuous series (ranges) where all apps have status "WITH PRO"
         */
        @GetMapping("/pro-series-by-distribution/{distributionId}")
        @Transactional(readOnly = true)
        public ResponseEntity<List<AppSeriesDTO>> getProSeriesByDistribution(
                @PathVariable int distributionId,
                @RequestParam(required = true) Integer appStartNo,
                @RequestParam(required = true) Integer appEndNo,
                @RequestParam(required = true) Integer issuedToTypeId) {
            
            List<AppSeriesDTO> series = distributionGetTableService.getProSeriesByDistribution(
                    distributionId, appStartNo, appEndNo, issuedToTypeId);
            return ResponseEntity.ok(series);
        }

       
}