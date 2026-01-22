package com.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.dto.AppNumberRangeDTO;
import com.application.dto.DgmToCampusFormDTO;
import com.application.dto.GenericDropdownDTO;
import com.application.entity.AdminApp;
import com.application.entity.BalanceTrack;
import com.application.entity.Campus;
import com.application.entity.CampusProView;
import com.application.entity.Distribution;
import com.application.entity.District;
import com.application.entity.UserAdminView;
import com.application.repository.AcademicYearRepository;
import com.application.repository.AdminAppRepository;
import com.application.repository.AppIssuedTypeRepository;
import com.application.repository.BalanceTrackRepository;
import com.application.repository.CampaignRepository;
import com.application.repository.CampusProViewRepository;
import com.application.repository.CampusRepository;
import com.application.repository.CityRepository;
import com.application.repository.DgmRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.DistrictRepository;
import com.application.repository.EmployeeRepository;
import com.application.repository.StateRepository;
import com.application.repository.UserAdminViewRepository;
import com.application.repository.ZonalAccountantRepository;

import lombok.NonNull;

@Service
public class CampusService {

    private final AcademicYearRepository academicYearRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final CampusRepository campusRepository;
    private final CampaignRepository campaignRepository;
    private final AppIssuedTypeRepository appIssuedTypeRepository;
    private final DistributionRepository distributionRepository;
    private final EmployeeRepository employeeRepository;
    private final BalanceTrackRepository balanceTrackRepository;
    private final CampusProViewRepository campusProViewRepository;

    @Autowired
    UserAdminViewRepository userAdminViewRepository;

    @Autowired
    private AdminAppRepository adminAppRepository;

    @Autowired
    private ZonalAccountantRepository zonalAccountantRepository;

    @Autowired
    private DgmRepository dgmRepository;

    public CampusService(AcademicYearRepository academicYearRepository, StateRepository stateRepository,
            DistrictRepository districtRepository, CityRepository cityRepository, CampusRepository campusRepository,
            CampaignRepository campaignRepository, AppIssuedTypeRepository appIssuedTypeRepository,
            DistributionRepository distributionRepository, EmployeeRepository employeeRepository,
            BalanceTrackRepository balanceTrackRepository, CampusProViewRepository campusProViewRepository) {
        this.academicYearRepository = academicYearRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.cityRepository = cityRepository;
        this.campusRepository = campusRepository;
        this.campaignRepository = campaignRepository;
        this.appIssuedTypeRepository = appIssuedTypeRepository;
        this.distributionRepository = distributionRepository;
        this.employeeRepository = employeeRepository;
        this.balanceTrackRepository = balanceTrackRepository;
        this.campusProViewRepository = campusProViewRepository;
    }

    // --- Dropdowns & Helpers with caching ---
    @Cacheable("academicYears")
    public List<GenericDropdownDTO> getAllAcademicYears() {
        return academicYearRepository.findAll().stream()
                .map(y -> new GenericDropdownDTO(y.getAcdcYearId(), y.getAcademicYear())).collect(Collectors.toList());
    }

    @Cacheable("states")
    public List<GenericDropdownDTO> getAllStates() {
        return stateRepository.findAll().stream().map(s -> new GenericDropdownDTO(s.getStateId(), s.getStateName()))
                .collect(Collectors.toList());
    }

    @Cacheable("districts")
    public List<District> getAllDistricts() {
        return districtRepository.findAll();
    }

    @Cacheable(cacheNames = "districtsByState", key = "#stateId")
    public List<GenericDropdownDTO> getDistrictsByStateId(int stateId) {
        return districtRepository.findByStateStateId(stateId).stream()
                .map(d -> new GenericDropdownDTO(d.getDistrictId(), d.getDistrictName())).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "citiesByDistrict", key = "#districtId")
    public List<GenericDropdownDTO> getCitiesByDistrictId(int districtId) {
        return getCitiesByDistrictId(districtId, null);
    }

    public List<GenericDropdownDTO> getCitiesByDistrictId(int districtId, String category) {
        final int ACTIVE_STATUS = 1;
        List<GenericDropdownDTO> cities = cityRepository.findByDistrictDistrictIdAndStatus(districtId, ACTIVE_STATUS)
                .stream()
                .map(c -> new GenericDropdownDTO(c.getCityId(), c.getCityName())).collect(Collectors.toList());

        // Apply category filter if provided (case-insensitive)
        if (category != null && !category.trim().isEmpty()) {
            String categoryLower = category.trim().toLowerCase();

            cities = cities.stream()
                    .filter(city -> {
                        // Get all active campuses for this city
                        List<Campus> campuses = campusRepository.findByCityCityId(city.getId());

                        // Check if any campus matches the category
                        return campuses.stream()
                                .anyMatch(campus -> {
                                    if (campus.getBusinessType() == null) {
                                        return false;
                                    }

                                    String businessTypeName = campus.getBusinessType()
                                            .getBusinessTypeName().toLowerCase();

                                    // Case-insensitive category matching
                                    if (categoryLower.equals("school")) {
                                        return businessTypeName.contains("school");
                                    } else if (categoryLower.equals("college")) {
                                        return businessTypeName.contains("college");
                                    }
                                    // If category doesn't match known types, return true (no filter)
                                    return true;
                                });
                    })
                    .collect(Collectors.toList());
        }

        return cities;
    }

    @Cacheable(cacheNames = "campusesByCity", key = "#cityId")
    public List<GenericDropdownDTO> getCampusesByCityId(int cityId) {
        return campusRepository.findByCityCityId(cityId).stream()
                .map(c -> new GenericDropdownDTO(c.getCampusId(), c.getCampusName())).collect(Collectors.toList());
    }

    @Cacheable("campaignAreas")
    public List<GenericDropdownDTO> getAllCampaignAreas() {
        return campaignRepository.findAll().stream()
                .map(c -> new GenericDropdownDTO(c.getCampaignId(), c.getAreaName())).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "prosByCampus", key = "#campusId")
    public List<GenericDropdownDTO> getProsByCampusId(int campusId) {
        List<Integer> employeeIds = campusProViewRepository.findByCampusId(campusId).stream()
                .map(CampusProView::getCmps_emp_id).toList();
        if (employeeIds.isEmpty())
            return List.of();
        return employeeRepository.findAllById(employeeIds).stream()
                .map(e -> new GenericDropdownDTO(e.getEmp_id(), e.getFirst_name() + " " + e.getLast_name()))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "prosByCampus", key = "#campusId")
    public List<GenericDropdownDTO> getEmployeeDropdownByCampus(int campusId) {

        // Fetch from view (includes both our emp = 1 and external emp = 0) where active
        List<GenericDropdownDTO> dropdown = campusProViewRepository.findDropdownByCampusId(campusId);
        return dropdown == null ? List.of() : dropdown;
    }

    @Cacheable("issuedToTypes")
    public List<GenericDropdownDTO> getAllIssuedToTypes() {
        return appIssuedTypeRepository.findAll().stream()
                .map(t -> new GenericDropdownDTO(t.getAppIssuedId(), t.getTypeName())).collect(Collectors.toList());
    }

    // @Cacheable(cacheNames = "availableAppNumberRanges", key = "{#employeeId,
    // #academicYearId}")
    public List<AppNumberRangeDTO> getAvailableAppNumberRanges(int employeeId, int academicYearId) {
        return balanceTrackRepository.findAppNumberRanges(academicYearId, employeeId).stream()
                .map(r -> new AppNumberRangeDTO(r.getAppBalanceTrkId(), r.getAppFrom(), r.getAppTo()))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "mobileNumberByEmpId", key = "#empId")
    public String getMobileNumberByEmpId(int empId) {
        return employeeRepository.findMobileNoByEmpId(empId);
    }

    @Cacheable(cacheNames = "campusByCampaign", key = "#campaignId")
    public List<GenericDropdownDTO> getCampusByCampaignId(int campaignId) {
        return campusRepository.findCampusByCampaignId(campaignId).stream()
                .map(c -> new GenericDropdownDTO(c.getCampusId(), c.getCampusName())).toList();
    }

    @Cacheable(cacheNames = "campaignsByCity", key = "#cityId")
    public List<GenericDropdownDTO> getCampaignsByCityId(int cityId) {
        return campaignRepository.findByCity_CityId(cityId).stream()
                .map(c -> new GenericDropdownDTO(c.getCampaignId(), c.getAreaName())).toList();
    }

    private int getDgmUserTypeId(int userId) {
        // 1. Fetch all roles for this user
        List<UserAdminView> userRoles = userAdminViewRepository.findRolesByEmpId(userId);

        if (userRoles.isEmpty()) {
            // Fallback: If no roles found (rare for internal staff), throw error or default
            throw new RuntimeException("No roles found for Employee ID: " + userId);
        }

        int highestPriorityTypeId = Integer.MAX_VALUE;

        // 2. Loop through roles to find the "Highest Rank" (Smallest ID number)
        for (UserAdminView userView : userRoles) {
            if (userView.getRole_name() == null)
                continue;

            String normalizedRoleName = userView.getRole_name().trim().toUpperCase();

            int currentTypeId = switch (normalizedRoleName) {
                case "ADMIN" -> 1;
                case "ZONAL ACCOUNTANT" -> 2;
                case "DGM" -> 3;
                // Add other roles here if needed
                default -> -1;
            };

            // Logic: If we found a valid role (not -1) AND it is higher rank (smaller
            // number)
            if (currentTypeId != -1 && currentTypeId < highestPriorityTypeId) {
                highestPriorityTypeId = currentTypeId;
            }
        }

        if (highestPriorityTypeId == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "User " + userId + " does not have a valid Issuer Role (Admin/Zone/DGM).");
        }

        return highestPriorityTypeId;
    }

    @Transactional
    public synchronized void submitDgmToCampusForm(@NonNull DgmToCampusFormDTO formDto) {
        int dgmUserId = formDto.getUserId();
        int dgmUserTypeId = getDgmUserTypeId(dgmUserId);

        // NEW VALIDATION: Check if sender actually owns the requested application range
        // and amount
        validateSenderHasAvailableRange(formDto);

        // -------------------------------------------------------
        // STEP 1: CHECK FOR OVERLAPS (This was missing!)
        // -------------------------------------------------------
        int startNo = Integer.parseInt(formDto.getApplicationNoFrom());
        int endNo = Integer.parseInt(formDto.getApplicationNoTo());

        // --- DUPLICATE CHECK START ---
        // Prevents double submission for EXACT range
        // List<Distribution> existing =
        // distributionRepository.findOverlappingDistributions(
        // formDto.getAcademicYearId(), startNo, endNo);
        // If overlapping dists exist, normally logic handles splits.
        // But strict duplicate check relying on synchronization + validation:

        // 1. Find conflicts
        List<Distribution> overlappingDists = distributionRepository.findOverlappingDistributions(
                formDto.getAcademicYearId(), startNo, endNo);

        // 2. Execute the Split/Recall Logic
        if (!overlappingDists.isEmpty()) {
            // This call removes the warning and fixes the logic!
            handleOverlappingDistributions(overlappingDists, formDto);
        }
        // -------------------------------------------------------

        // 2. Create and Map Basic Fields
        Distribution distribution = new Distribution();
        mapDtoToDistribution(distribution, formDto, dgmUserTypeId);

        // 3. Lookup Receiver
        CampusProView receiver = campusProViewRepository.findByEmp_id(formDto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Validate Receiver is Active
        if (receiver.getIs_active() != 1) {
            throw new RuntimeException("Transaction Failed: The selected Receiver is Inactive.");
        }

        // 4. Traffic Switch
        if (receiver.getIsOurEmp() == 1) {
            distribution.setIssued_to_emp_id(receiver.getCmps_emp_id());
            distribution.setIssued_to_pro_id(null);
        } else {
            distribution.setIssued_to_pro_id(receiver.getCmps_emp_id());
            distribution.setIssued_to_emp_id(null);
        }

        // --- CONSUMABLE STOCK LOGIC DISABLED ---
        // DISABLED: The consumable stock logic was modifying parent distribution records
        // when DGM distributes to Campus. This caused parent records to be overwritten/updated
        // when they should remain as static history.
        // 
        // If consumable stock is needed in the future, it should only apply to specific flows
        // and should not modify parent distributions during POST (create) operations.
        // 
        // Original logic commented out:
        // int consumedCount = endNo - startNo + 1;
        // Optional<Distribution> parentOpt = distributionRepository.findParentToConsume(
        //         dgmUserId, formDto.getAcademicYearId(), formDto.getApplication_Amount(), startNo);
        // if (parentOpt.isPresent()) {
        //     Distribution parent = parentOpt.get();
        //     parent.setAppStartNo(endNo + 1);
        //     parent.setTotalAppCount(parent.getTotalAppCount() - consumedCount);
        //     if (parent.getTotalAppCount() <= 0) {
        //         parent.setIsActive(0);
        //     } else {
        //         distributionRepository.save(parent);
        //     }
        // }
        // --- CONSUMABLE STOCK LOGIC DISABLED ---

        // 5. Save and Flush
        System.out.println("=== DISTRIBUTION SAVE (NEW) - CampusService ===");
        System.out.println("Operation: CREATE NEW DISTRIBUTION");
        System.out.println("Range: " + distribution.getAppStartNo() + " - " + distribution.getAppEndNo());
        System.out.println("Receiver EmpId: " + distribution.getIssued_to_emp_id());
        System.out.println("Receiver ProId: " + distribution.getIssued_to_pro_id());
        System.out.println("Amount: " + distribution.getAmount());
        System.out.println("Created By: " + distribution.getCreated_by());
        Distribution savedDist = distributionRepository.saveAndFlush(distribution);
        System.out.println("Saved Distribution ID: " + savedDist.getAppDistributionId());
        System.out.println("===============================================");

        // CRITICAL: Flush any pending remainders created in
        // handleOverlappingDistributions
        // This ensures they are visible when rebuilding the sender's balance
        distributionRepository.flush();

        // 6. Recalculate Balances
        int stateId = savedDist.getState().getStateId();
        Float amount = formDto.getApplication_Amount();

        // A. Update Issuer (Sender) - CRITICAL: Always recalculate sender's balance
        System.out.println("DEBUG CAMPUS: Recalculating sender balance for issuer: " + dgmUserId);
        recalculateBalanceForEmployee(dgmUserId, formDto.getAcademicYearId(), stateId, dgmUserTypeId, dgmUserId,
                amount);

        // CRITICAL: Flush balance updates to ensure they're persisted
        balanceTrackRepository.flush();

        // B. Update Receiver
        addStockToReceiver(savedDist, formDto.getAcademicYearId(), formDto.getIssuedToTypeId(), dgmUserId, amount);
    }

    private Integer getIssuerZoneId(int issuerId, int issuerTypeId) {
        // If Issuer is Zonal Accountant (Type 2)
        if (issuerTypeId == 2) {
            // Use ZonalAccountantRepository to find the Zone
            return zonalAccountantRepository.findZoneIdByEmployeeId(issuerId).stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Issuer is a Zone Officer but not mapped to any Zone"));
        }

        // If Issuer is DGM (Type 3)
        if (issuerTypeId == 3) {
            // DGM -> Campus -> Zone
            // Assuming you have a method in DgmRepository or similar
            // OR we can check ZonalAccountant repo if DGMs are listed there too.
            // Let's assume DgmRepository has this:
            /*
             * return dgmRepository.findZoneIdByDgmEmpId(issuerId);
             */

            // WORKAROUND if you don't have that specific query yet:
            // We can use the 'branchId' from the form context IF the DGM is operating from
            // their own branch.
            // But safer to look up the DGM entity:
            // Dgm dgm = dgmRepository.findByEmpId(issuerId).get(0);
            // return dgm.getCampus().getZone().getZoneId();

            // Placeholder: You need to ensure you can get the Zone ID for the DGM
            return dgmRepository.findZoneIdByEmpId(issuerId)
                    .orElseThrow(() -> new RuntimeException("Issuer is a DGM but not mapped to any Zone"));
        }

        return null;
    }

    private void recalculateBalanceForPro(int proId, int academicYearId, int stateId, int typeId, int createdBy,
            Float amount) {

        // 1. Find existing PRO balance
        BalanceTrack balance = balanceTrackRepository.findActiveBalanceByProAndAmount(academicYearId, proId, amount)
                .orElseGet(() -> {
                    BalanceTrack nb = new BalanceTrack();
                    nb.setIssuedToProId(proId);
                    nb.setEmployee(null);
                    nb.setAcademicYear(academicYearRepository.findById(academicYearId).orElseThrow());
                    nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
                    nb.setIsActive(1);
                    nb.setCreatedBy(createdBy);
                    nb.setAmount(amount);
                    return nb;
                });

        // 2. Calculate Totals from Distribution Table
        // This query returns '7' correctly
        Integer totalReceived = distributionRepository
                .sumTotalAppCountByIssuedToProIdAndAmount(proId, academicYearId, amount).orElse(0);

        // 3. Set Ranges AND Count
        if (totalReceived > 0) {
            // These two lines worked (that's why you see 111-117)
            balance.setAppFrom(
                    distributionRepository.findMinAppStartNoByIssuedToProId(proId, academicYearId).orElse(0));
            balance.setAppTo(
                    distributionRepository.findMaxAppEndNoByIssuedToProId(proId, academicYearId).orElse(0));

            // --- THIS IS THE MISSING LINE ---
            balance.setAppAvblCnt(totalReceived);
            // --------------------------------

        } else {
            balance.setAppFrom(0);
            balance.setAppTo(0);
            balance.setAppAvblCnt(0);
        }

        balanceTrackRepository.save(balance);
    }

    @Transactional
    public void updateDgmToCampusForm(@NonNull Integer distributionId, @NonNull DgmToCampusFormDTO formDto) {
        System.out.println("========================================");
        System.out.println("--- LOG: START updateDgmToCampusForm ---");
        System.out.println("--- LOG: Dist ID: " + distributionId + ", User ID: " + formDto.getUserId() +
                ", Target Range: " + formDto.getApplicationNoFrom() + "-" + formDto.getApplicationNoTo());
        System.out.println("--- LOG: CRITICAL - This method should create ONLY ONE new distribution record");
        System.out.println("========================================");

        // 1. Fetch Existing Record
        Distribution existingDistribution = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Distribution record not found for ID: " + distributionId));

        // 2. Extract Critical Data
        Float originalAmount = existingDistribution.getAmount();
        int stateId = existingDistribution.getState().getStateId();
        int dgmUserId = formDto.getUserId();
        int academicYearId = formDto.getAcademicYearId();
        int dgmUserTypeId = getDgmUserTypeId(dgmUserId);

        // 3. Resolve New Receiver
        CampusProView newReceiver = campusProViewRepository.findByEmp_id(formDto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("New Receiver not found"));

        // School Validation
        if (dgmUserTypeId != 1) {
            String category = formDto.getCategory();
            if (category != null && category.trim().equalsIgnoreCase("SCHOOL")) {
                Integer targetZoneId = newReceiver.getZoneId();
                Integer myZoneId = getIssuerZoneId(dgmUserId, dgmUserTypeId);
                if (targetZoneId != null && myZoneId != null && !targetZoneId.equals(myZoneId)) {
                    throw new RuntimeException(
                            "Update Denied: For SCHOOL category, cannot transfer to a different Zone.");
                }
            }
        }

        // Determine New IDs
        Integer newEmpId = (newReceiver.getIsOurEmp() == 1) ? newReceiver.getCmps_emp_id() : null;
        Integer newProId = (newReceiver.getIsOurEmp() == 0) ? newReceiver.getCmps_emp_id() : null;
        
        // Get campus IDs for validation
        Integer newCampusId = newReceiver.getCmps_id();
        Integer oldCampusId = existingDistribution.getCampus() != null ? 
                existingDistribution.getCampus().getCampusId() : null;

        // Identify Changes
        Integer oldEmpId = existingDistribution.getIssued_to_emp_id();
        Integer oldProId = existingDistribution.getIssued_to_pro_id();
        boolean isRecipientChanging = !java.util.Objects.equals(oldEmpId, newEmpId)
                || !java.util.Objects.equals(oldProId, newProId);
        
        // VALIDATION: Cannot update to same campus + same PRO
        // Only allow update if campus is different OR PRO is different
        if (newCampusId != null && oldCampusId != null && newCampusId.equals(oldCampusId)) {
            // Same campus - check if PRO is also the same
            if (java.util.Objects.equals(oldProId, newProId) && newProId != null) {
                throw new RuntimeException(
                        "Update Denied: Cannot update to the same campus and same PRO. " +
                        "Please select a different PRO or different campus.");
            }
        }

        int oldStart = (int) existingDistribution.getAppStartNo();
        int oldEnd = (int) existingDistribution.getAppEndNo();
        int newStart = Integer.parseInt(formDto.getApplicationNoFrom());
        int newEnd = Integer.parseInt(formDto.getApplicationNoTo());
        boolean isRangeChanging = oldStart != newStart || oldEnd != newEnd;

        System.out.println("--- LOG: Range Changing? " + isRangeChanging + " (" + oldStart + "->" + newStart + ")");
        System.out.println("--- LOG: Recipient Changing? " + isRecipientChanging);

        // -------------------------------------------------------
        // STEP 6: CHECK OVERLAPS (THE FIX LOGIC)
        // -------------------------------------------------------
        boolean isIssuerRebuilt = false; // Flag to track if we already fixed 4818

        if (isRangeChanging || isRecipientChanging) {
            System.out.println("--- LOG: Checking for overlaps...");
            List<Distribution> overlappingDists = distributionRepository.findOverlappingDistributions(
                    academicYearId, newStart, newEnd);

            // Filter out self
            List<Distribution> others = overlappingDists.stream()
                    .filter(d -> !d.getAppDistributionId().equals(distributionId))
                    .toList();

            System.out.println("--- LOG: Found " + others.size() + " overlapping distributions (excluding self).");

            if (!others.isEmpty()) {
                // FIX: Capture the boolean return value!
                isIssuerRebuilt = handleOverlappingDistributions(others, formDto);
            }
        }

        System.out.println("--- LOG: isIssuerRebuilt flag is: " + isIssuerRebuilt);

        // -------------------------------------------------------
        // STEP 7: EXECUTE UPDATE
        // -------------------------------------------------------

        // A. Inactivate Old
        System.out.println("=== DISTRIBUTION UPDATE (INACTIVATE) - CampusService ===");
        System.out.println("Operation: INACTIVATE EXISTING DISTRIBUTION");
        System.out.println("Distribution ID: " + existingDistribution.getAppDistributionId());
        System.out.println(
                "Old Range: " + existingDistribution.getAppStartNo() + " - " + existingDistribution.getAppEndNo());
        System.out.println("Old Receiver EmpId: " + existingDistribution.getIssued_to_emp_id());
        System.out.println("Old Receiver ProId: " + existingDistribution.getIssued_to_pro_id());
        existingDistribution.setIsActive(0);
        // Update timestamp when deactivating distribution
        existingDistribution.setIssueDate(LocalDateTime.now());
        distributionRepository.saveAndFlush(existingDistribution);
        System.out.println("========================================================");

        // B. Create New - ONLY ONE RECORD
        // CRITICAL: Ensure we only create ONE distribution record, not multiple
        // The formDto should contain a single continuous range
        Distribution newDist = new Distribution();
        mapDtoToDistribution(newDist, formDto, dgmUserTypeId);
        newDist.setIssued_to_emp_id(newEmpId);
        newDist.setIssued_to_pro_id(newProId);
        newDist.setAmount(originalAmount);
        
        // VALIDATION: Ensure the range is valid (start <= end)
        if (newDist.getAppStartNo() > newDist.getAppEndNo()) {
            throw new RuntimeException("Invalid range: Start (" + newDist.getAppStartNo() + 
                    ") cannot be greater than End (" + newDist.getAppEndNo() + ")");
        }
        
        // Calculate total count from the range
        int calculatedCount = (int)(newDist.getAppEndNo() - newDist.getAppStartNo() + 1);
        newDist.setTotalAppCount(calculatedCount);
        
        System.out.println("=== DISTRIBUTION UPDATE (CREATE NEW) - CampusService ===");
        System.out.println("Operation: CREATE SINGLE NEW DISTRIBUTION (UPDATE)");
        System.out.println("New Range: " + newDist.getAppStartNo() + " - " + newDist.getAppEndNo());
        System.out.println("New Total Count: " + newDist.getTotalAppCount() + " apps");
        System.out.println("New Receiver EmpId: " + newDist.getIssued_to_emp_id());
        System.out.println("New Receiver ProId: " + newDist.getIssued_to_pro_id());
        System.out.println("Amount: " + newDist.getAmount());
        System.out.println("NOTE: Only ONE record should be created for this update");
        distributionRepository.saveAndFlush(newDist);
        System.out.println("New Distribution ID: " + newDist.getAppDistributionId());
        System.out.println("========================================================");
        
        // CRITICAL CHECK: Verify only ONE record was created for this update
        // Check if any other active records exist with the same receiver, same amount, and overlapping range
        // This helps detect if the update is creating duplicate records
        List<Distribution> duplicateCheck = distributionRepository.findOverlappingDistributions(
                academicYearId, (int)newDist.getAppStartNo(), (int)newDist.getAppEndNo());
        List<Distribution> duplicates = duplicateCheck.stream()
                .filter(d -> d.getAppDistributionId() != null && 
                        !d.getAppDistributionId().equals(newDist.getAppDistributionId()) &&
                        d.getIsActive() == 1 &&
                        java.util.Objects.equals(d.getIssued_to_emp_id(), newDist.getIssued_to_emp_id()) &&
                        java.util.Objects.equals(d.getIssued_to_pro_id(), newDist.getIssued_to_pro_id()) &&
                        d.getAmount() != null && Math.abs(d.getAmount() - newDist.getAmount()) < 0.01 &&
                        d.getCreated_by() == dgmUserId) // Same creator (same update operation)
                .collect(java.util.stream.Collectors.toList());
        
        if (!duplicates.isEmpty()) {
            System.out.println("⚠️ WARNING: Found " + duplicates.size() + " potential duplicate record(s) created in the same update:");
            for (Distribution dup : duplicates) {
                System.out.println("  - Distribution ID: " + dup.getAppDistributionId() + 
                        ", Range: " + dup.getAppStartNo() + "-" + dup.getAppEndNo() + 
                        ", Count: " + dup.getTotalAppCount() +
                        ", IsActive: " + dup.getIsActive());
            }
            System.out.println("  ⚠️ This should not happen - only ONE record should be created per update");
            System.out.println("  ⚠️ Please check if the update method is being called multiple times");
        } else {
            System.out.println("✅ VERIFIED: Only ONE new distribution record created (ID: " + newDist.getAppDistributionId() + ")");
        }

        // 8. Handle Remainders (For Shrinking/Shifting)
        // COMBINE all remainder ranges into a SINGLE record (like DGM and Zone updates)
        if (isRangeChanging) {
            System.out.println("📊 Campus Remainder Creation Analysis:");
            System.out.println("  Old Range: " + oldStart + " - " + oldEnd);
            System.out.println("  New Range: " + newStart + " - " + newEnd);
            System.out.println("  Old Receiver EmpId: " + existingDistribution.getIssued_to_emp_id());
            System.out.println("  Old Receiver ProId: " + existingDistribution.getIssued_to_pro_id());
            
            // Collect all remainder ranges that should be created
            java.util.List<int[]> remainderRanges = new java.util.ArrayList<>();
            
            // Check for portion BEFORE the new range
            if (oldStart < newStart) {
                int remainderStart = oldStart;
                int remainderEnd = newStart - 1;
                remainderRanges.add(new int[]{remainderStart, remainderEnd});
                System.out.println("  ✅ Adding remainder (before): " + remainderStart + " - " + remainderEnd);
            }
            
            // Check for portion AFTER the new range
            if (oldEnd > newEnd) {
                int remainderStart = newEnd + 1;
                int remainderEnd = oldEnd;
                remainderRanges.add(new int[]{remainderStart, remainderEnd});
                System.out.println("  ✅ Adding remainder (after): " + remainderStart + " - " + remainderEnd);
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
                
                System.out.println("=== DISTRIBUTION SAVE (COMBINED REMAINDER) - CampusService ===");
                System.out.println("Operation: CREATE SINGLE COMBINED REMAINDER");
                System.out.println("Combined Range: " + combinedRemainder.getAppStartNo() + " - " + combinedRemainder.getAppEndNo());
                System.out.println("Total Count: " + combinedRemainder.getTotalAppCount() + " apps (sum of all remainder ranges)");
                System.out.println("Receiver EmpId: " + combinedRemainder.getIssued_to_emp_id());
                System.out.println("Receiver ProId: " + combinedRemainder.getIssued_to_pro_id());
                System.out.println("Amount: " + combinedRemainder.getAmount());
                distributionRepository.saveAndFlush(combinedRemainder);
                System.out.println("Combined Remainder Distribution ID: " + combinedRemainder.getAppDistributionId());
                System.out.println("============================================================");
            }
        }

        // CRITICAL: Flush any pending remainders to ensure they are visible
        distributionRepository.flush();

        // -------------------------------------------------------
        // STEP 9: RECALCULATE BALANCES
        // -------------------------------------------------------

        // A. Issuer (Sender) - CRITICAL: Always recalculate sender's balance
        if (!isIssuerRebuilt) {
            System.out.println("--- LOG: Performing Standard Issuer Recalculation (Flag was FALSE)");
            System.out.println("DEBUG CAMPUS UPDATE: Recalculating sender balance for issuer: " + dgmUserId);
            recalculateBalanceForEmployee(dgmUserId, academicYearId, stateId, dgmUserTypeId, dgmUserId, originalAmount);
        } else {
            System.out.println("--- LOG: SKIPPING Issuer Recalculation (Flag was TRUE) - Balance 4818 is safe.");
        }

        // CRITICAL: Flush balance updates to ensure they're persisted
        balanceTrackRepository.flush();

        // B. New Receiver - ALWAYS recalculate
        if (newProId != null) {
            System.out.println("DEBUG CAMPUS UPDATE: Recalculating new receiver balance for PRO: " + newProId);
            recalculateBalanceForPro(newProId, academicYearId, stateId, formDto.getIssuedToTypeId(), dgmUserId,
                    originalAmount);
        } else if (newEmpId != null) {
            System.out.println("DEBUG CAMPUS UPDATE: Recalculating new receiver balance for Employee: " + newEmpId);
            // Use rebuildBalancesFromDistributions for employees (like DGM update)
            recalculateBalanceForEmployee(newEmpId, academicYearId, stateId, formDto.getIssuedToTypeId(), dgmUserId,
                    originalAmount);
        }

        // C. Old Receiver (If changed OR range changed) - ALWAYS recalculate if changed
        if (isRecipientChanging || isRangeChanging) {
            System.out.println("DEBUG CAMPUS UPDATE: Recalculating old receiver balance - RecipientChanged: " + 
                    isRecipientChanging + ", RangeChanged: " + isRangeChanging);
            
            if (oldEmpId != null) {
                // Get the type ID from existing balance or distribution
                int oldTypeId = existingDistribution.getIssuedToType() != null ? 
                        existingDistribution.getIssuedToType().getAppIssuedId() : formDto.getIssuedToTypeId();
                
                // Try to get type from balance track if available
                java.util.List<BalanceTrack> oldBalances = balanceTrackRepository
                        .findActiveBalancesByEmpAndAmount(academicYearId, oldEmpId, originalAmount);
                if (!oldBalances.isEmpty() && oldBalances.get(0).getIssuedByType() != null) {
                    oldTypeId = oldBalances.get(0).getIssuedByType().getAppIssuedId();
                }
                
                System.out.println("DEBUG CAMPUS UPDATE: Recalculating old receiver Employee: " + oldEmpId + 
                        ", TypeId: " + oldTypeId);
                recalculateBalanceForEmployee(oldEmpId, academicYearId, stateId, oldTypeId, dgmUserId, originalAmount);
            }
            if (oldProId != null) {
                System.out.println("DEBUG CAMPUS UPDATE: Recalculating old receiver PRO: " + oldProId);
                int oldTypeId = existingDistribution.getIssuedToType() != null ? 
                        existingDistribution.getIssuedToType().getAppIssuedId() : formDto.getIssuedToTypeId();
                recalculateBalanceForPro(oldProId, academicYearId, stateId, oldTypeId, dgmUserId, originalAmount);
            }
        }
        
        // CRITICAL: Flush all balance updates after all recalculations
        balanceTrackRepository.flush();

        System.out.println("--- LOG: END updateDgmToCampusForm ---");
    }

    // --- PRIVATE HELPER METHODS ---

    // CHANGE: Return type must be boolean
    private boolean handleOverlappingDistributions(List<Distribution> overlappingDists, DgmToCampusFormDTO request) {
        System.out.println("--- LOG: Inside handleOverlappingDistributions ---");
        System.out.println("--- LOG: Request User ID: " + request.getUserId());

        boolean issuerRebuilt = false;
        int reqStart = Integer.parseInt(request.getApplicationNoFrom());
        int reqEnd = Integer.parseInt(request.getApplicationNoTo());

        for (Distribution oldDist : overlappingDists) {
            Integer oldHolderId = null;
            boolean isPro = false;

            if (oldDist.getIssued_to_emp_id() != null) {
                oldHolderId = oldDist.getIssued_to_emp_id();
            } else if (oldDist.getIssued_to_pro_id() != null) {
                oldHolderId = oldDist.getIssued_to_pro_id();
                isPro = true;
            }

            System.out.println("--- LOG: Processing Overlap. Old Holder ID: " + oldHolderId + ", IsPro: " + isPro);

            int oldStart = oldDist.getAppStartNo();
            int oldEnd = oldDist.getAppEndNo();

            // NEW LOGIC: Keep Distribution record completely unchanged (no inactivation, no
            // remainders)
            // Distribution table: Keep original record as-is, active, unchanged
            // BalanceTrack table: Will be updated via recalculateBalanceForEmployee to show
            // remaining
            System.out.println("=== DISTRIBUTION (KEEP COMPLETELY UNCHANGED) - CampusService ===");
            System.out.println("Operation: KEEP DISTRIBUTION RECORD COMPLETELY UNCHANGED");
            System.out.println("Distribution ID: " + oldDist.getAppDistributionId());
            System.out.println("Original Range: " + oldStart + " - " + oldEnd + " (KEEPING AS-IS, NO CHANGES)");
            System.out.println("New Distribution Range: " + reqStart + " - " + reqEnd);
            System.out.println("Receiver EmpId: " + oldDist.getIssued_to_emp_id());
            System.out.println("Receiver ProId: " + oldDist.getIssued_to_pro_id());
            System.out
                    .println("NOTE: Distribution record will NOT be modified at all (no inactivation, no remainders).");
            System.out.println("NOTE: Only BalanceTrack will be updated to show remaining range.");

            // Keep original distribution record completely unchanged - no modifications at
            // all
            // No inactivation, no remainder creation
            // BalanceTrack will be calculated based on what's actually available

            System.out.println("================================================================");

            // 4. Recalculate Victim's Balance
            if (isPro) {
                recalculateBalanceForPro(oldHolderId, request.getAcademicYearId(),
                        oldDist.getState().getStateId(), oldDist.getIssuedToType().getAppIssuedId(),
                        request.getUserId(), oldDist.getAmount());
            } else {
                System.out.println("--- LOG: Rebuilding Balance for Emp ID: " + oldHolderId);

                rebuildBalancesFromDistributions(oldHolderId, request.getAcademicYearId(),
                        oldDist.getIssuedToType().getAppIssuedId(), request.getUserId(), oldDist.getAmount());

                // CRITICAL: Flush balance updates
                balanceTrackRepository.flush();

                // --- CRITICAL FIX HERE: Use .equals() instead of == ---
                if (oldHolderId != null && oldHolderId.equals(request.getUserId())) {
                    System.out.println("--- LOG: MATCH FOUND! Issuer " + oldHolderId + " was rebuilt.");
                    issuerRebuilt = true;
                } else {
                    System.out.println(
                            "--- LOG: No Match. OldHolder: " + oldHolderId + " vs RequestUser: " + request.getUserId());
                }
            }
        }

        System.out.println("--- LOG: Exiting handleOverlappingDistributions. Returns: " + issuerRebuilt);
        return issuerRebuilt;
    }

    // REVISED recalculateBalanceForEmployee for CampusService
    // REVISED recalculateBalanceForEmployee in DgmService
    private void recalculateBalanceForEmployee(int employeeId, int academicYearId, int stateId, int typeId,
            int createdBy, Float amount) {

        // 1. CHECK: Is this a CO/Admin? (Check Master Table)
        Optional<AdminApp> adminApp = adminAppRepository.findByEmpAndYearAndAmount(
                employeeId, academicYearId, amount);

        if (adminApp.isPresent()) {
            // CASE A: CO / ADMIN (Source Logic)
            AdminApp master = adminApp.get();

            List<BalanceTrack> balances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(
                    academicYearId, employeeId, amount);

            BalanceTrack balance;
            if (balances.isEmpty()) {
                balance = createNewBalanceTrack(employeeId, academicYearId, typeId, createdBy);
                balance.setAmount(amount);
                balance.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
                balance.setIssuedToProId(null);
            } else {
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
                System.out.println("DEBUG CAMPUS: Admin/CO distributed up to " + maxDistributedEnd.get() +
                        ", setting app_from to " + calculatedAppFrom);
            } else {
                // They haven't distributed from the beginning yet - use master start
                calculatedAppFrom = master.getAppFromNo();
                System.out.println("DEBUG CAMPUS: Admin/CO hasn't distributed from beginning, using master start: "
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
            // CASE B: INTERMEDIARY (DGM/Zone) -> REBUILD LOGIC
            // This fixes the issue where sender balance wasn't updating correctly for gaps
            rebuildBalancesFromDistributions(employeeId, academicYearId, typeId, createdBy, amount);
        }
    }

    private void rebuildBalancesFromDistributions(int empId, int acYearId, int typeId, int createdBy, Float amount) {
        System.out.println("DEBUG CAMPUS: rebuildBalancesFromDistributions - Employee: " + empId);

        // CRITICAL: Clear persistence context
        distributionRepository.flush();

        // 1. Get ALL Active Distributions RECEIVED by this user
        List<Distribution> allReceived = distributionRepository.findActiveHoldingsForEmp(empId, acYearId);
        
        System.out.println("DEBUG CAMPUS: rebuildBalancesFromDistributions - Employee: " + empId + 
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
        
        System.out.println("DEBUG CAMPUS: rebuildBalancesFromDistributions - Employee: " + empId + 
                ", Received Distributions (after amount filter): " + received.size() + 
                ", Filter Amount: " + amount);
        for (Distribution d : received) {
            System.out.println("  - Distribution ID: " + d.getAppDistributionId() + 
                    ", Range: " + d.getAppStartNo() + "-" + d.getAppEndNo() + 
                    ", Count: " + d.getTotalAppCount());
        }

        // 3. Get ALL Distributions GIVEN AWAY by this user
        List<Distribution> allGivenAway = distributionRepository.findByCreatedByAndYear(empId, acYearId);

        // 3b. Also find distributions that OVERLAP with received ranges but have different issued_to_emp_id/issued_to_pro_id
        // This catches cases where Admin/DGM updated and transferred apps away from this Campus/PRO
        // Example: Admin/DGM updates Campus's distribution, taking apps and giving to another Campus/PRO
        // The new distribution has created_by = Admin/DGM, but it overlaps with Campus's received range
        // CRITICAL: Only count as "taken away" if the overlapping distribution was created AFTER the received distribution
        // OR if it represents apps that were actually taken from this Campus's original range
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
                // 2. Different issued_to_emp_id or issued_to_pro_id (apps were given to someone else)
                // 3. Active
                // 4. Not already in allGivenAway (avoid duplicates)
                // 5. CRITICAL: The overlap must be WITHIN the received range (not just overlapping)
                //    AND it must represent apps that were actually taken from this Campus
                int overlapStart = (int) overlap.getAppStartNo();
                int overlapEnd = (int) overlap.getAppEndNo();
                
                // Check if this overlap is actually WITHIN the received range (overlap is subset of received)
                // This means: overlap.start >= receivedStart AND overlap.end <= receivedEnd
                boolean isWithinReceivedRange = overlapStart >= receivedStart && overlapEnd <= receivedEnd;
                
                // CRITICAL: EXCLUDE if the overlap CONTAINS the received range (remainder distributions)
                // Example: Received: 2875052-2875076, Overlap: 2875002-2875101 (remainder for another Campus)
                // This should NOT be counted as "given away" for this Campus
                boolean overlapContainsReceived = overlapStart <= receivedStart && overlapEnd >= receivedEnd;
                
                // Check if it was created by Admin/DGM (represents a transfer/update)
                boolean isAdminOrDgmTransfer = overlap.getCreated_by() != empId &&
                        overlap.getIssuedByType() != null &&
                        (overlap.getIssuedByType().getAppIssuedId() == 1 || // Admin/CO type
                         overlap.getIssuedByType().getAppIssuedId() == 3); // DGM type
                
                // Check if receiver is different (either emp_id or pro_id)
                boolean isDifferentReceiver = false;
                if (receivedDist.getIssued_to_emp_id() != null) {
                    isDifferentReceiver = !java.util.Objects.equals(receivedDist.getIssued_to_emp_id(), 
                            overlap.getIssued_to_emp_id());
                } else if (receivedDist.getIssued_to_pro_id() != null) {
                    isDifferentReceiver = !java.util.Objects.equals(receivedDist.getIssued_to_pro_id(), 
                            overlap.getIssued_to_pro_id());
                }
                
                // Only count as "taken away" if:
                // 1. The overlap is WITHIN the received range (subset), AND
                // 2. It was created by Admin/DGM (transfer), AND
                // 3. It does NOT contain the received range (exclude remainders), AND
                // 4. Receiver is different
                boolean shouldCountAsTakenAway = isWithinReceivedRange && 
                        isAdminOrDgmTransfer && 
                        !overlapContainsReceived &&
                        isDifferentReceiver;
                
                if (Math.abs(overlap.getAmount() - amount) < 0.01 &&
                    overlap.getIsActive() == 1 &&
                    !allGivenAway.contains(overlap) &&
                    shouldCountAsTakenAway) {
                    overlappingTakenAway.add(overlap);
                    System.out.println("DEBUG CAMPUS: Found overlapping distribution taken away - ID: " + 
                            overlap.getAppDistributionId() + ", Range: " + 
                            overlap.getAppStartNo() + "-" + overlap.getAppEndNo() + 
                            ", Issued to Emp: " + overlap.getIssued_to_emp_id() + 
                            ", Issued to Pro: " + overlap.getIssued_to_pro_id() + 
                            ", Created by: " + overlap.getCreated_by() +
                            ", IsWithinReceived: " + isWithinReceivedRange +
                            ", IsAdminOrDgmTransfer: " + isAdminOrDgmTransfer +
                            ", OverlapContainsReceived: " + overlapContainsReceived);
                } else {
                    System.out.println("DEBUG CAMPUS: Excluding overlapping distribution - ID: " + 
                            overlap.getAppDistributionId() + ", Range: " + 
                            overlap.getAppStartNo() + "-" + overlap.getAppEndNo() + 
                            ", Issued to Emp: " + overlap.getIssued_to_emp_id() + 
                            ", Issued to Pro: " + overlap.getIssued_to_pro_id() + 
                            ", Created by: " + overlap.getCreated_by() +
                            ", IsWithinReceived: " + isWithinReceivedRange +
                            ", IsAdminOrDgmTransfer: " + isAdminOrDgmTransfer +
                            ", OverlapContainsReceived: " + overlapContainsReceived +
                            " (not taken from this Campus)");
                }
            }
        }

        // 4. Combine both lists and filter by amount
        // NOTE: BalanceTrack represents AVAILABLE stock for distribution
        // So ALL distributions ARE subtracted because they're no longer available
        List<Distribution> givenAway = new java.util.ArrayList<>(allGivenAway);
        givenAway.addAll(overlappingTakenAway);
        givenAway = givenAway.stream()
                .filter(d -> d.getAmount() != null && Math.abs(d.getAmount() - amount) < 0.01)
                .sorted((d1, d2) -> Long.compare(d1.getAppStartNo(), d2.getAppStartNo()))
                .toList();
        
        System.out.println("DEBUG CAMPUS: rebuildBalancesFromDistributions - Employee: " + empId
                + ", Given Away Distributions: " + givenAway.size());

        // 5. Get CURRENT Active Balance Rows for Reuse
        // REUSE STRATEGY: Instead of deactivating all, we keep them in a list/queue
        List<BalanceTrack> currentBalances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(acYearId, empId,
                amount);
        // Use a LinkedList for easy removal/popping
        java.util.LinkedList<BalanceTrack> reusePool = new java.util.LinkedList<>(currentBalances);

        boolean atLeastOneActiveRowCreated = false;

        // 7. Calculate remaining ranges by subtracting given away from received
        if (received.isEmpty()) {
            System.out.println(
                    "--- LOG: WARNING! No received distributions found. Creating balance row with is_active = 0.");
            // We need a zero-balance active record
            BalanceTrack nb;
            if (!reusePool.isEmpty()) {
                nb = reusePool.poll(); // Reuse existing
            } else {
                nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy, false);
                nb.setAmount(amount);
            }
            nb.setAppFrom(0);
            nb.setAppTo(0);
            nb.setAppAvblCnt(0);
            nb.setIsActive(1); // CORRECT: Keep it active
            balanceTrackRepository.saveAndFlush(nb);
            atLeastOneActiveRowCreated = true;
            System.out.println("--- LOG: Created/Reused active balance row for employee " + empId + " with zero count");
        } else {
            // COLLECT ALL remaining ranges from ALL received distributions first
            List<int[]> allRemainingRanges = new java.util.ArrayList<>();
            
            for (Distribution receivedDist : received) {
                int receivedStart = (int) receivedDist.getAppStartNo();
                int receivedEnd = (int) receivedDist.getAppEndNo();

                System.out.println("--- LOG: Processing received distribution: " + receivedStart + " - " + receivedEnd);

                // Find all given away ranges that overlap with this received range
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
                        System.out.println("--- LOG: Found overlap - Given away: " + givenStart + "-" + givenEnd +
                                ", Overlaps with received: " + overlapStart + "-" + overlapEnd);
                    }
                }

                // Calculate remaining ranges (received minus given away)
                List<int[]> remainingRanges = calculateRemainingRanges(receivedStart, receivedEnd, givenAwayRanges);
                
                // Add all remaining ranges to the master list
                allRemainingRanges.addAll(remainingRanges);
            }
            
            // Create separate balance track records for each remaining range
            // This allows tracking of separate series even if there are gaps
            if (!allRemainingRanges.isEmpty()) {
                // Sort by start number
                allRemainingRanges.sort((a, b) -> Integer.compare(a[0], b[0]));
                
                System.out.println("--- LOG: Creating " + allRemainingRanges.size() + 
                        " separate balance track record(s) for remaining ranges:");
                
                // Create/Reuse balance tracks for EACH remaining range separately
                for (int[] range : allRemainingRanges) {
                    int remainingStart = range[0];
                    int remainingEnd = range[1];
                    int remainingCount = remainingEnd - remainingStart + 1;

                    System.out.println(
                            "--- LOG: Creating/Reusing Balance Track for range: " + remainingStart + "-"
                                    + remainingEnd + " (" + remainingCount + " apps)");

                    BalanceTrack nb;
                    if (!reusePool.isEmpty()) {
                        nb = reusePool.poll(); // Reuse
                    } else {
                        nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy, false);
                        nb.setAmount(amount);
                    }

                    nb.setAppAvblCnt(remainingCount);

                    // If available count is 0, set app_from = 0, app_to = 0, and is_active = 1
                    if (remainingCount <= 0) {
                        nb.setAppFrom(0);
                        nb.setAppTo(0);
                    } else {
                        nb.setAppFrom(remainingStart);
                        nb.setAppTo(remainingEnd);
                    }
                    // Keep is_active = 1 even when available count is 0 (show as available 0)
                    nb.setIsActive(1);

                    BalanceTrack saved = balanceTrackRepository.saveAndFlush(nb);
                    atLeastOneActiveRowCreated = true;
                    System.out.println(
                            "--- LOG: Updated/Created Balance Track ID: " + saved.getAppBalanceTrkId() +
                            ", Range: " + saved.getAppFrom() + "-" + saved.getAppTo() + 
                            ", Count: " + saved.getAppAvblCnt());
                }
            }
        }

        // FINAL CHECK: If no active balance rows were created, create a dummy active
        // one with 0 balance
        // This happens when everything received has been given away.
        if (!atLeastOneActiveRowCreated) {
            System.out.println("--- LOG: All stock distributed. Ensuring one active zero-balance row.");
            BalanceTrack nb;
            if (!reusePool.isEmpty()) {
                nb = reusePool.poll();
            } else {
                nb = createNewBalanceTrack(empId, acYearId, typeId, createdBy, false);
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
            System.out.println("--- LOG: Deactivating unused old balance row ID: " + unused.getAppBalanceTrkId());
        }

        // CRITICAL: Flush all balance updates to ensure they're persisted
        balanceTrackRepository.flush();
        System.out.println("DEBUG CAMPUS: Rebuilt balance rows for employee " + empId + " with amount " + amount);
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

    private void mapDtoToDistribution(Distribution distribution, DgmToCampusFormDTO formDto, int issuedById) {
        int appNoFrom = Integer.parseInt(formDto.getApplicationNoFrom());
        int appNoTo = Integer.parseInt(formDto.getApplicationNoTo());

        // Basic Mappings
        academicYearRepository.findById(formDto.getAcademicYearId()).ifPresent(distribution::setAcademicYear);

        cityRepository.findById(formDto.getCityId()).ifPresent(city -> {
            distribution.setCity(city);
            if (city.getDistrict() != null) {
                distribution.setDistrict(city.getDistrict());
                if (city.getDistrict().getState() != null) {
                    distribution.setState(city.getDistrict().getState());
                }
            }
        });

        // Set Branch Context (Location)
        campusRepository.findById(formDto.getBranchId()).ifPresent(c -> {
            distribution.setCampus(c);
            distribution.setZone(c.getZone());
        });

        // Types
        appIssuedTypeRepository.findById(issuedById).ifPresent(distribution::setIssuedByType);

        // USE FRONTEND VALUE
        appIssuedTypeRepository.findById(formDto.getIssuedToTypeId()).ifPresent(distribution::setIssuedToType);

        distribution.setAppStartNo(appNoFrom);
        distribution.setAppEndNo(appNoTo);
        // Calculate total count from the range to ensure accuracy
        int calculatedRange = appNoTo - appNoFrom + 1;
        // Use the calculated range, but validate against formDto.getRange() if provided
        if (formDto.getRange() > 0 && formDto.getRange() != calculatedRange) {
            System.out.println("⚠️ WARNING: Form range (" + formDto.getRange() + 
                    ") doesn't match calculated range (" + calculatedRange + "). Using calculated range.");
        }
        distribution.setTotalAppCount(calculatedRange);
        distribution.setAmount(formDto.getApplication_Amount());

        // Date Logic: Use Frontend Date, fallback to Now
        if (formDto.getIssueDate() != null) {
            distribution.setIssueDate(formDto.getIssueDate().atTime(java.time.LocalTime.now())); // Convert to
                                                                                                 // LocalDateTime with
                                                                                                 // current time
        } else {
            distribution.setIssueDate(LocalDateTime.now());
        }

        distribution.setIsActive(1);
        distribution.setCreated_by(formDto.getUserId());
    }

    private void mapExistingToNewDistribution(Distribution newDist, Distribution oldDist) {
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
        // Basic setup only, specific IDs set by caller
        nb.setAcademicYear(academicYearRepository.findById(academicYearId).orElseThrow());
        nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
        nb.setAppAvblCnt(0);
        nb.setIsActive(1);
        nb.setCreatedBy(createdBy);
        return nb;
    }

    // FIX: Renamed to match the loop call, added start/end args, added saveAndFlush
    private void createAndSaveRemainder(Distribution originalDist, int start, int end) {
        Distribution remainder = new Distribution();
        mapExistingToNewDistribution(remainder, originalDist);

        // CRITICAL: Copy EXACTLY who had it before (whether Employee OR Pro)
        remainder.setIssued_to_emp_id(originalDist.getIssued_to_emp_id());
        remainder.setIssued_to_pro_id(originalDist.getIssued_to_pro_id());

        // Set the specific split range
        remainder.setAppStartNo(start);
        remainder.setAppEndNo(end);
        remainder.setTotalAppCount((end - start) + 1);

        remainder.setIsActive(1);
        remainder.setAmount(originalDist.getAmount()); // Keep Amount
        // Ensure timestamp is set for new remainder distribution
        remainder.setIssueDate(LocalDateTime.now());

        System.out.println("=== DISTRIBUTION SAVE (REMAINDER) - CampusService ===");
        System.out.println("Operation: CREATE REMAINDER DISTRIBUTION");
        System.out.println("Remainder Range: " + remainder.getAppStartNo() + " - " + remainder.getAppEndNo());
        System.out.println("Original Dist ID: " + originalDist.getAppDistributionId());
        System.out.println("Original Range: " + originalDist.getAppStartNo() + " - " + originalDist.getAppEndNo());
        System.out.println("Receiver EmpId: " + remainder.getIssued_to_emp_id());
        System.out.println("Receiver ProId: " + remainder.getIssued_to_pro_id());
        System.out.println("Amount: " + remainder.getAmount());
        // CRITICAL: Force DB to write NOW so recalculateBalance sees it
        distributionRepository.saveAndFlush(remainder);
        System.out.println("Remainder Distribution ID: " + remainder.getAppDistributionId());
        System.out.println("=====================================================");
    }

    // Smart Method to Add Stock (Handles Gaps & Receiver Type)
    private void addStockToReceiver(Distribution savedDist, int academicYearId, int typeId, int createdBy,
            Float amount) {
        int newStart = savedDist.getAppStartNo();
        int newEnd = savedDist.getAppEndNo();
        int newCount = savedDist.getTotalAppCount();
        int targetEnd = newStart - 1;

        boolean isPro = (savedDist.getIssued_to_pro_id() != null);
        Integer receiverId = isPro ? savedDist.getIssued_to_pro_id() : savedDist.getIssued_to_emp_id();

        if (receiverId == null)
            return;

        List<BalanceTrack> existingDuplicates;

        // FIX: Distinguish between Pro and Emp repo calls to prevent ID collisions
        if (isPro) {
            // Assuming you have this method or a generic find that works for both by column
            existingDuplicates = balanceTrackRepository.findActiveBalancesByProAndAmount(academicYearId, receiverId,
                    amount);
        } else {
            existingDuplicates = balanceTrackRepository.findActiveBalancesByEmpAndAmount(academicYearId, receiverId,
                    amount);
        }

        for (BalanceTrack b : existingDuplicates) {
            if (b.getAppFrom() == newStart && b.getAppTo() == newEnd) {
                System.out.println("--- LOG: Duplicate balance detected. Skipping creation.");
                return;
            }
        }

        Optional<BalanceTrack> mergeableRow;
        if (isPro) {
            mergeableRow = balanceTrackRepository.findMergeableRowForPro(academicYearId, receiverId, amount, targetEnd);
        } else {
            mergeableRow = balanceTrackRepository.findMergeableRowForEmployee(academicYearId, receiverId, amount,
                    targetEnd);
        }

        if (mergeableRow.isPresent()) {
            BalanceTrack existing = mergeableRow.get();
            existing.setAppTo(newEnd);
            existing.setAppAvblCnt(existing.getAppAvblCnt() + newCount);
            balanceTrackRepository.save(existing);
        } else {
            BalanceTrack newRow = createNewBalanceTrack(receiverId, academicYearId, typeId, createdBy, isPro);
            newRow.setAmount(amount);
            newRow.setAppFrom(newStart);
            newRow.setAppTo(newEnd);
            newRow.setAppAvblCnt(newCount);
            balanceTrackRepository.save(newRow);
        }
    }

    // Helper to create the empty object correctly based on type
    private BalanceTrack createNewBalanceTrack(int id, int acYear, int typeId, int createdBy, boolean isPro) {
        BalanceTrack nb = new BalanceTrack();
        nb.setAcademicYear(academicYearRepository.findById(acYear).orElseThrow());
        nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());
        nb.setIsActive(1);
        nb.setCreatedBy(createdBy);

        if (isPro) {
            nb.setIssuedToProId(id);
            nb.setEmployee(null); // DB Constraint satisfied (nullable)
        } else {
            nb.setEmployee(employeeRepository.findById(id).orElseThrow());
            nb.setIssuedToProId(null);
        }
        return nb;
    }

    // Overload for Employees (Defaults isPro = false)
    private BalanceTrack createNewBalanceTrack_(int employeeId, int academicYearId, int typeId, int createdBy) {
        return createNewBalanceTrack(employeeId, academicYearId, typeId, createdBy, false);
    }

    public List<GenericDropdownDTO> fetchCampusesByCityAndCategory(String category, Integer cityId) {

        String cat = category == null ? "" : category.toLowerCase();

        switch (cat) {
            case "school":
                return campusRepository.findSchoolCampusesByCity(cityId);

            case "college":
            case "clg":
            case "clge":
                return campusRepository.findCollegeCampusesByCity(cityId);

            default:
                return campusRepository.findAllCampusesByCity(cityId);
        }
    }

    /**
     * Validates that the sender (userId) actually owns the requested application
     * range.
     * Checks BalanceTrack for Zone/DGM employees or AdminApp for Admin/CO
     * employees.
     * Also validates that the amount matches available balances.
     * 
     * @param formDto The DGM to Campus form DTO containing the range to validate
     * @throws RuntimeException if the requested range is not available, with
     *                          details of available ranges
     */
    private void validateSenderHasAvailableRange(@NonNull DgmToCampusFormDTO formDto) {
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
