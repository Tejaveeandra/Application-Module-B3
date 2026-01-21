package com.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.dto.DistributionRequestDTO;
import com.application.dto.EmployeesDto;
import com.application.entity.AcademicYear;
import com.application.entity.AdminApp;
import com.application.entity.BalanceTrack;
import com.application.entity.City;
import com.application.entity.Distribution;
import com.application.entity.State;
import com.application.entity.ZonalAccountant;
import com.application.entity.Zone;
import com.application.repository.AcademicYearRepository;
import com.application.repository.AdminAppRepository;
import com.application.repository.AppIssuedTypeRepository;
import com.application.repository.BalanceTrackRepository;
import com.application.repository.CampusProViewRepository;
import com.application.repository.CityRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.EmployeeRepository;
import com.application.repository.StateRepository;
import com.application.repository.ZonalAccountantRepository;
import com.application.repository.ZoneRepository;
import com.application.repository.CampusRepository;
import com.application.entity.Campus;

import lombok.NonNull;

@Service
public class ZoneService {

    private final AcademicYearRepository academicYearRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final ZoneRepository zoneRepository;
    private final AppIssuedTypeRepository appIssuedTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final BalanceTrackRepository balanceTrackRepository;
    private final DistributionRepository distributionRepository;
    private final ZonalAccountantRepository zonalAccountantRepository;
    @Autowired
    private AdminAppRepository adminAppRepository;
    @Autowired
    private CampusProViewRepository campusProViewRepository;
    @Autowired
    private CampusRepository campusRepository;

    public ZoneService(AcademicYearRepository academicYearRepository, StateRepository stateRepository,
            CityRepository cityRepository, ZoneRepository zoneRepository,
            AppIssuedTypeRepository appIssuedTypeRepository, EmployeeRepository employeeRepository,
            BalanceTrackRepository balanceTrackRepository,
            DistributionRepository distributionRepository, ZonalAccountantRepository zonalAccountantRepository) {
        this.academicYearRepository = academicYearRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.zoneRepository = zoneRepository;
        this.appIssuedTypeRepository = appIssuedTypeRepository;
        this.employeeRepository = employeeRepository;
        this.balanceTrackRepository = balanceTrackRepository;
        this.distributionRepository = distributionRepository;
        this.zonalAccountantRepository = zonalAccountantRepository;
    }

    // --- Dropdown/Helper Methods with Caching ---
    @Cacheable("academicYears")
    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAll();
    }

    @Cacheable("states")
    public List<State> getAllStates() {
        // Assumption: State entity has a field named 'is_active' or similar.
        return stateRepository.findByStatus(1);
    }

    @Cacheable(cacheNames = "citiesByState", key = "#stateId")
    public List<City> getCitiesByState(int stateId) {
        final int ACTIVE_STATUS = 1;
        return cityRepository.findByDistrictStateStateIdAndStatus(stateId, ACTIVE_STATUS);
    }

    @Cacheable(cacheNames = "zonesByCity", key = "#cityId")
    public List<Zone> getZonesByCity(int cityId) {
        return getZonesByCity(cityId, null);
    }

    public List<Zone> getZonesByCity(int cityId, String category) {
        // Get all zones for the city
        List<Zone> zones = zoneRepository.findByCityCityId(cityId);

        // Apply category filter if provided (case-insensitive)
        if (category != null && !category.trim().isEmpty()) {
            String categoryLower = category.trim().toLowerCase();

            zones = zones.stream()
                    .filter(zone -> {
                        // Get all active campuses for this zone
                        List<Campus> campuses = campusRepository.findByZoneZoneId(zone.getZoneId());

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

        return zones;
    }

    @Cacheable(cacheNames = "employeesByZone", key = "#zoneId")
    @Transactional(readOnly = true) // Recommended for lazy loading
    public List<EmployeesDto> getEmployeesByZone(int zoneId) {
        return getEmployeesByZone(zoneId, null);
    }

    @Transactional(readOnly = true) // Recommended for lazy loading
    public List<EmployeesDto> getEmployeesByZone(int zoneId, String category) {
        // Fetch only active zonal accountants (ZonalAccountant.isActive == 1)
        List<ZonalAccountant> activeAccountants = zonalAccountantRepository.findByZoneZoneIdAndIsActive(zoneId, 1);

        // Apply category filter if provided (case-insensitive) - filter at
        // ZonalAccountant level
        if (category != null && !category.trim().isEmpty()) {
            String categoryLower = category.trim().toLowerCase();
            activeAccountants = activeAccountants.stream()
                    .filter(accountant -> {
                        // Check Employee's Campus
                        Campus empCampus = (accountant.getEmployee() != null) ? accountant.getEmployee().getCampus()
                                : null;
                        if (empCampus != null && empCampus.getBusinessType() != null) {
                            String type = empCampus.getBusinessType().getBusinessTypeName().toLowerCase();
                            boolean matches = false;
                            if (categoryLower.equals("school"))
                                matches = type.contains("school");
                            else if (categoryLower.equals("college"))
                                matches = type.contains("college");
                            else
                                matches = true; // Unknown category -> no filter

                            if (matches)
                                return true;
                        }

                        // Fallback: Check Accountant's Campus (if employee campus didn't match or
                        // wasn't present)
                        Campus acctCampus = accountant.getCampus();
                        if (acctCampus != null && acctCampus.getBusinessType() != null) {
                            String type = acctCampus.getBusinessType().getBusinessTypeName().toLowerCase();
                            boolean matches = false;
                            if (categoryLower.equals("school"))
                                matches = type.contains("school");
                            else if (categoryLower.equals("college"))
                                matches = type.contains("college");
                            else
                                matches = true;

                            if (matches)
                                return true;
                        }

                        // If neither explicitly matches the category, but we had campuss...
                        // If one was checked and failed, do we fail?
                        // Original logic: "If targetCampus found, check it. If fail, return false."
                        // New logic: "If ANY valid campus matches, return true."

                        // If NO campus found at all?
                        if (empCampus == null && acctCampus == null)
                            return false;

                        // If we are here, it means we found campuses but none matched the specific
                        // "school" or "college" category.
                        // However, if category is active "school" and we only found "college", we
                        // should return false.
                        // Returns false if no match found.
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // Map and filter: Only include if Employee.isActive == 1
        return activeAccountants.stream()
                .map(this::mapToEmployeeDto)
                .filter(Objects::nonNull) // Skip null/inactive employees
                .collect(Collectors.toList());
    }

    // Helper: Maps only if employee is active (filters but doesn't include in DTO)
    private EmployeesDto mapToEmployeeDto(ZonalAccountant accountant) {
        var employee = accountant.getEmployee();
        // Filter: Skip if employee null or inactive
        if (employee == null || employee.getIsActive() == null || employee.getIsActive() != 1) {
            return null;
        }
        // Map without isActive
        return new EmployeesDto(employee.getEmp_id(), employee.getFirst_name(), employee.getLast_name(),
                employee.getPrimary_mobile_no());
    }

    @Transactional
    public synchronized void saveDistribution(@NonNull DistributionRequestDTO request) {

        validateEmployeeExists(request.getCreatedBy(), "Issuer");

        // NEW VALIDATION: Check if sender actually owns the requested application range
        validateSenderHasAvailableRange(request);

        // --- DUPLICATE CHECK START ---
        // Prevents double submission for EXACT range
        // List<Distribution> existing =
        // distributionRepository.findOverlappingDistributions(
        // request.getAcademicYearId(), request.getAppStartNo(), request.getAppEndNo());

        // If overlapping dists exist, normally logic handles splits.
        // But strict duplicate check relying on synchronization + validation:

        // If we find an overlap that looks like a DUPLICATE (same creation time approx?
        // No).
        // If we find a distribution that covers this range and was just created, we
        // should block.
        // However, 'overlappingDists' logic below handles partial overlaps by creating
        // NEW logic.
        // We need to stop EXACT duplicates from creating a MESS.

        // If this exact range is already fully covered by an active distribution that
        // was issued BY this user
        // to the SAME target type (e.g. Zone->DGM), we can flag it.
        // For simplicity: If we find an overlap, we usually proceed to split.
        // But if the user clicks TWICE, the first click creates a split/new Record.
        // The second click sees THAT new record as an "overlap".
        // If we proceed, we might try to split the SPLIT.

        // Simple Fix: Check if range is available in Sender's Balance.
        // validateSenderHasAvailableRange(request) ALREADY checks if the sender HAS the
        // apps.
        // If they click once -> apps move from Sender to Receiver. Sender no longer has
        // them.
        // If they click twice -> validateSenderHasAvailableRange SHOULD fail the second
        // time because they gave them away.
        // Let's rely on validateSenderHasAvailableRange, but ensure it's tight.

        // The issue is concurrency. 'synchronized' ensures we enter one by one.
        // 1st enters: calls validate..Range. Has apps. Moves apps. Commits.
        // 2nd enters: calls validate..Range. Apps are gone. Should Throw Exception.

        // So just adding 'synchronized' to the method + @Transactional is likely enough
        // IF validateSenderHasAvailableRange checks the DB *current state*.
        // validateSenderHasAvailableRange uses 'balanceTrackRepository'.
        // We need to make sure we are reading committed data.
        // @Transactional isolation level?

        // Let's inspect validateSenderHasAvailableRange to be sure.
        // But proceed with adding 'synchronized' now.

        List<Distribution> overlappingDists = distributionRepository.findOverlappingDistributions(
                request.getAcademicYearId(), request.getAppStartNo(), request.getAppEndNo());

        System.out.println("DEBUG: saveDistribution - Sender ID: " + request.getCreatedBy() +
                ", Overlapping Distributions Found: " + overlappingDists.size() +
                ", Range: " + request.getAppStartNo() + "-" + request.getAppEndNo());
        for (Distribution d : overlappingDists) {
            System.out.println("DEBUG: Overlap - Dist ID: " + d.getAppDistributionId() +
                    ", IssuedToEmpId: " + d.getIssued_to_emp_id() +
                    ", Range: " + d.getAppStartNo() + "-" + d.getAppEndNo());
        }

        // Track if sender's balance was already recalculated in
        // handleOverlappingDistributions
        boolean senderBalanceRecalculated = false;
        if (!overlappingDists.isEmpty()) {
            senderBalanceRecalculated = handleOverlappingDistributions(overlappingDists, request);
            System.out.println(
                    "DEBUG: handleOverlappingDistributions returned senderRecalculated: " + senderBalanceRecalculated);
        }

        // CRITICAL FIX: Check if SENDER's own distribution overlaps with the new
        // distribution
        // The sender might have a distribution where they are the receiver
        // (issued_to_emp_id = sender)
        // that overlaps with what they're giving away. This needs to be split!
        // Query ALL overlapping distributions again (in case some were inactivated) and
        // filter for sender
        List<Distribution> allOverlaps = distributionRepository.findOverlappingDistributions(
                request.getAcademicYearId(), request.getAppStartNo(), request.getAppEndNo());

        List<Distribution> senderOverlaps = allOverlaps.stream()
                .filter(d -> {
                    // Find distributions where the SENDER is the receiver (they hold these apps)
                    Integer holderId = d.getIssued_to_emp_id() != null ? d.getIssued_to_emp_id()
                            : d.getIssued_to_pro_id();
                    boolean isSender = holderId != null && holderId.equals(request.getCreatedBy());
                    if (isSender) {
                        System.out.println("DEBUG: Found SENDER's distribution - Dist ID: " + d.getAppDistributionId() +
                                ", Range: " + d.getAppStartNo() + "-" + d.getAppEndNo() +
                                ", IssuedToEmpId: " + d.getIssued_to_emp_id());
                    }
                    return isSender;
                })
                .toList();

        if (!senderOverlaps.isEmpty()) {
            System.out.println("DEBUG: Found " + senderOverlaps.size()
                    + " SENDER's own distributions that overlap - will split them");

            // Handle sender's overlapping distributions - this will create remainders and
            // recalculate balance
            boolean senderRecalc = handleOverlappingDistributions(senderOverlaps, request);
            if (senderRecalc) {
                senderBalanceRecalculated = true;
                System.out.println("DEBUG: Sender's distribution was split and balance recalculated");
            }
        } else {
            System.out.println(
                    "DEBUG: No SENDER's distributions found that overlap - sender might be Admin/CO or have no Distribution records");
        }

        // Handle multiple ZonalAccountant records for the same employee
        List<ZonalAccountant> receiverList = zonalAccountantRepository.findByEmployeeEmpId(request.getIssuedToEmpId());
        if (receiverList.isEmpty()) {
            throw new RuntimeException("Receiver not found for Employee ID: " + request.getIssuedToEmpId());
        }

        // Prefer the one matching the requested zone_id, otherwise take the first (most
        // recent)
        ZonalAccountant receiver = receiverList.stream()
                .filter(za -> za.getZone() != null && za.getZone().getZoneId() == request.getZoneId())
                .findFirst()
                .orElse(receiverList.get(0));

        if (receiverList.size() > 1) {
            System.out.println("WARNING: Multiple active ZonalAccountant records found for Employee ID: " +
                    request.getIssuedToEmpId() + ". Total records: " + receiverList.size()
                    + ". Selected one with zone_id: " +
                    (receiver.getZone() != null ? receiver.getZone().getZoneId() : "null"));
        }

        // Validate receiver has required relationships loaded
        if (receiver.getZone() == null) {
            throw new RuntimeException(
                    "Receiver Zone information is missing for Employee ID: " + request.getIssuedToEmpId());
        }

        // Optional: You can log a warning if the Zone doesn't match what the UI sent
        if (receiver.getZone().getZoneId() != request.getZoneId()) {
            System.out.println("WARNING: UI sent Zone " + request.getZoneId() + " but User "
                    + request.getIssuedToEmpId() + " is actually in Zone " + receiver.getZone().getZoneId());
        }

        if (receiver.getIsActive() != 1) {
            throw new RuntimeException("Transaction Failed: The selected Receiver is Inactive.");
        }

        Distribution newDistribution = new Distribution();
        mapDtoToDistribution(newDistribution, request); // Helper to map basic fields (State, Zone, Dates, etc.)

        // LOGIC: Determine where to save the ID (Employee Column vs PRO Column)
        if (receiver.getEmployee() != null) {
            // It's an Employee (e.g., DGM)
            newDistribution.setIssued_to_emp_id(receiver.getEmployee().getEmp_id());
            newDistribution.setIssued_to_pro_id(null);
        } else if (receiver.getCampus() != null) {
            // It's a PRO (Branch)
            newDistribution.setIssued_to_pro_id(receiver.getCampus().getCampusId());
            newDistribution.setIssued_to_emp_id(null);
        } else {
            throw new RuntimeException("Invalid Receiver: No Employee or Campus linked to this Zonal Accountant.");
        }

        // Set Fee/Amount
        newDistribution.setAmount(request.getApplication_Amount());

        // Save to DB
        System.out.println("=== DISTRIBUTION SAVE (NEW) - ZoneService ===");
        System.out.println("Operation: CREATE NEW DISTRIBUTION");
        System.out.println("Range: " + newDistribution.getAppStartNo() + " - " + newDistribution.getAppEndNo());
        System.out.println("Receiver EmpId: " + newDistribution.getIssued_to_emp_id());
        System.out.println("Receiver ProId: " + newDistribution.getIssued_to_pro_id());
        System.out.println("Amount: " + newDistribution.getAmount());
        System.out.println("Created By: " + newDistribution.getCreated_by());
        Distribution savedDist = distributionRepository.saveAndFlush(newDistribution);
        System.out.println("Saved Distribution ID: " + savedDist.getAppDistributionId());
        System.out.println("=============================================");

        // CRITICAL: Flush any pending remainders created in
        // handleOverlappingDistributions
        // This ensures they are visible when rebuilding the sender's balance
        distributionRepository.flush();

        // A. Update SENDER's Balance (Reduce - because they gave away applications)
        // CRITICAL: ALWAYS recalculate sender's balance, regardless of overlaps
        // The sender gave away apps, so their balance MUST be updated
        // Even if there was an overlap for the receiver, the sender's balance still
        // needs updating
        System.out.println("DEBUG: ALWAYS recalculating SENDER's balance - Employee: " + request.getCreatedBy() +
                ", senderBalanceRecalculated: " + senderBalanceRecalculated);

        // Flush first to ensure we see latest data including any remainders
        distributionRepository.flush();

        // Find what the sender actually holds to get correct type/amount
        List<Distribution> senderHoldings = distributionRepository.findActiveHoldingsForEmp(
                request.getCreatedBy(), request.getAcademicYearId());

        System.out.println(
                "DEBUG: Sender holdings found: " + senderHoldings.size() + " for Employee: " + request.getCreatedBy());

        if (senderHoldings.isEmpty()) {
            // Sender has no holdings as receiver - they might be Admin/CO distributing from
            // master allocation
            // OR they gave away all their apps
            // CRITICAL: Still recalculate balance - this will check AdminApp and
            // create/update balance accordingly
            System.out.println("DEBUG: Sender has no holdings - checking if Admin/CO or recalculating balance");
            recalculateBalanceForEmployee(request.getCreatedBy(), request.getAcademicYearId(),
                    request.getStateId(), request.getIssuedByTypeId(),
                    request.getCreatedBy(), request.getApplication_Amount());
        } else {
            // Group by amount and recalculate for each amount the sender holds
            senderHoldings.stream()
                    .filter(d -> d.getAmount() != null)
                    .collect(java.util.stream.Collectors.groupingBy(Distribution::getAmount))
                    .forEach((amount, dists) -> {
                        // Use the type from the first distribution with this amount
                        Distribution firstDist = dists.get(0);
                        System.out.println("DEBUG: Recalculating sender balance for amount: " + amount +
                                ", Type: " + firstDist.getIssuedToType().getAppIssuedId() +
                                ", Holdings count: " + dists.size());
                        recalculateBalanceForEmployee(request.getCreatedBy(), request.getAcademicYearId(),
                                request.getStateId(), firstDist.getIssuedToType().getAppIssuedId(),
                                request.getCreatedBy(), amount);
                    });
        }

        // CRITICAL: Flush balance updates to ensure they're persisted
        balanceTrackRepository.flush();
        System.out.println("DEBUG: Sender balance recalculation completed for Employee: " + request.getCreatedBy());

        // B. Update Receiver's Balance (Increase - because they received applications)
        if (savedDist.getIssued_to_emp_id() != null) {
            addStockToReceiver(savedDist.getIssued_to_emp_id(), request.getAcademicYearId(),
                    request.getIssuedToTypeId(), request.getCreatedBy(), request.getApplication_Amount(),
                    request.getAppStartNo(), // Pass Start
                    request.getAppEndNo(), // Pass End
                    request.getRange() // Pass Count
            );
        }
    }

    @Transactional
    public void updateDistribution(int distributionId, @NonNull DistributionRequestDTO request) {

        // 1. Fetch Existing
        validateEmployeeExists(request.getCreatedBy(), "Issuer");
        Distribution existingDist = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        Float originalAmount = existingDist.getAmount(); // Keep amount!

        // 2. Resolve New Receiver (Zone Service always targets Employee column)
        // Handle multiple ZonalAccountant records for the same employee
        List<ZonalAccountant> receiverList = zonalAccountantRepository.findByEmployeeEmpId(request.getIssuedToEmpId());
        if (receiverList.isEmpty()) {
            throw new RuntimeException("New Receiver not found for Employee ID: " + request.getIssuedToEmpId());
        }

        // Prefer the one matching the requested zone_id, otherwise take the first (most
        // recent)
        ZonalAccountant newReceiver = receiverList.stream()
                .filter(za -> za.getZone() != null && za.getZone().getZoneId() == request.getZoneId())
                .findFirst()
                .orElse(receiverList.get(0));

        if (receiverList.size() > 1) {
            System.out.println("WARNING: Multiple active ZonalAccountant records found for Employee ID: " +
                    request.getIssuedToEmpId() + ". Total records: " + receiverList.size()
                    + ". Selected one with zone_id: " +
                    (newReceiver.getZone() != null ? newReceiver.getZone().getZoneId() : "null"));
        }

        // Determine Target ID (Logic: always grab the emp_id, even for campuses)
        Integer newTargetId;
        if (newReceiver.getEmployee() != null) {
            newTargetId = newReceiver.getEmployee().getEmp_id();
        } else {
            newTargetId = campusProViewRepository.findDropdownByCampusId(newReceiver.getCampus().getCampusId())
                    .stream()
                    .findFirst()
                    .map(dto -> dto.getId())
                    .orElseThrow(() -> new RuntimeException("No valid ID found for Campus"));
        }

        // 3. Inactivate Old
        System.out.println("=== DISTRIBUTION UPDATE (INACTIVATE) - ZoneService ===");
        System.out.println("Operation: INACTIVATE EXISTING DISTRIBUTION");
        System.out.println("Distribution ID: " + existingDist.getAppDistributionId());
        System.out.println("Old Range: " + existingDist.getAppStartNo() + " - " + existingDist.getAppEndNo());
        System.out.println("Old Receiver EmpId: " + existingDist.getIssued_to_emp_id());
        existingDist.setIsActive(0);
        // Update timestamp when deactivating distribution
        existingDist.setIssueDate(LocalDateTime.now());
        distributionRepository.saveAndFlush(existingDist);
        System.out.println("=====================================================");

        // 4. Create New
        Distribution newDist = new Distribution();
        mapDtoToDistribution(newDist, request);

        newDist.setIssued_to_emp_id(newTargetId);
        newDist.setIssued_to_pro_id(null); // Zone service keeps this null
        newDist.setAmount(originalAmount); // Preserve Amount
        
        // IMPORTANT: For UPDATES, use the EXACT values from the request
        // This allows overwriting the count even if it was previously consumed
        // The consumable stock logic only applies to NEW distributions, not updates
        newDist.setAppStartNo(request.getAppStartNo());
        newDist.setAppEndNo(request.getAppEndNo());
        newDist.setTotalAppCount(request.getRange()); // Use exact count from request (can overwrite consumed values)

        System.out.println("=== DISTRIBUTION UPDATE (CREATE NEW) - ZoneService ===");
        System.out.println("Operation: CREATE NEW DISTRIBUTION (UPDATE)");
        System.out.println("New Range: " + newDist.getAppStartNo() + " - " + newDist.getAppEndNo());
        System.out.println("New Total Count: " + newDist.getTotalAppCount() + " (from request, overwrites any consumed values)");
        System.out.println("New Receiver EmpId: " + newDist.getIssued_to_emp_id());
        System.out.println("Amount: " + newDist.getAmount());
        distributionRepository.saveAndFlush(newDist);
        System.out.println("New Distribution ID: " + newDist.getAppDistributionId());
        System.out.println("======================================================");

        // 5. Handle Remainders: Create remainders for dropped ranges
        // IMPORTANT: DGM (Type 3) is UNDER Zone, so apps with DGM are still part of Zone's count
        // Only transfers to OTHER ZONES (Type 2) reduce Zone's count
        // COMBINE adjacent remainders into a single record
        int oldStart = (int) existingDist.getAppStartNo();
        int oldEnd = (int) existingDist.getAppEndNo();
        int newStart = request.getAppStartNo();
        int newEnd = request.getAppEndNo();

        if (oldStart != newStart || oldEnd != newEnd) {
            System.out.println("📊 Remainder Creation Analysis:");
            System.out.println("  Old Range: " + oldStart + " - " + oldEnd);
            System.out.println("  New Range: " + newStart + " - " + newEnd);
            System.out.println("  Zone Employee ID: " + existingDist.getIssued_to_emp_id());
            System.out.println("  Note: DGM distributions (Type 3) are still part of Zone's count");
            System.out.println("  Note: Only transfers to other Zones (Type 2) reduce Zone's count");
            
            // Collect all remainder ranges that should be created
            java.util.List<int[]> remainderRanges = new java.util.ArrayList<>();
            
            // Check for portion BEFORE the new range
            if (oldStart < newStart) {
                int remainderStart = oldStart;
                int remainderEnd = Math.min(oldEnd, newStart - 1);
                
                // Check if Zone distributed this range to ANOTHER ZONE (Type 2)
                List<Distribution> subDists = distributionRepository.findSubDistributionsInRange(
                        existingDist.getIssued_to_emp_id(),
                        existingDist.getAcademicYear().getAcdcYearId(),
                        originalAmount,
                        remainderStart,
                        remainderEnd);
                
                // Filter: Only skip if distributed to another Zone (Type 2)
                List<Distribution> zoneToZoneDists = subDists.stream()
                        .filter(sub -> sub.getIssuedToType() != null && sub.getIssuedToType().getAppIssuedId() == 2)
                        .collect(java.util.stream.Collectors.toList());
                
                if (zoneToZoneDists.isEmpty()) {
                    // No transfers to other Zones, add to remainder ranges
                    remainderRanges.add(new int[]{remainderStart, remainderEnd});
                    System.out.println("  ✅ Adding remainder (before): " + remainderStart + " - " + remainderEnd);
                    if (!subDists.isEmpty()) {
                        System.out.println("    (Includes " + subDists.size() + " DGM distribution(s) - still part of Zone's count)");
                    }
                } else {
                    System.out.println("  ⏭️ Skipping remainder (before): " + remainderStart + " - " + remainderEnd + 
                            " (Zone transferred " + zoneToZoneDists.size() + " sub-distribution(s) to other Zone(s))");
                }
            }
            
            // Check for portion AFTER the new range
            if (oldEnd > newEnd) {
                int remainderStart = Math.max(oldStart, newEnd + 1);
                int remainderEnd = oldEnd;
                
                // Check if Zone distributed this range to ANOTHER ZONE (Type 2)
                List<Distribution> subDists = distributionRepository.findSubDistributionsInRange(
                        existingDist.getIssued_to_emp_id(),
                        existingDist.getAcademicYear().getAcdcYearId(),
                        originalAmount,
                        remainderStart,
                        remainderEnd);
                
                // Filter: Only skip if distributed to another Zone (Type 2)
                List<Distribution> zoneToZoneDists = subDists.stream()
                        .filter(sub -> sub.getIssuedToType() != null && sub.getIssuedToType().getAppIssuedId() == 2)
                        .collect(java.util.stream.Collectors.toList());
                
                if (zoneToZoneDists.isEmpty()) {
                    // No transfers to other Zones, add to remainder ranges
                    remainderRanges.add(new int[]{remainderStart, remainderEnd});
                    System.out.println("  ✅ Adding remainder (after): " + remainderStart + " - " + remainderEnd);
                    if (!subDists.isEmpty()) {
                        System.out.println("    (Includes " + subDists.size() + " DGM distribution(s) - still part of Zone's count)");
                    }
                } else {
                    System.out.println("  ⏭️ Skipping remainder (after): " + remainderStart + " - " + remainderEnd + 
                            " (Zone transferred " + zoneToZoneDists.size() + " sub-distribution(s) to other Zone(s))");
                }
            }
            
            // Combine all remainder ranges into a SINGLE record
            // Even if there are gaps (e.g., apps given to other zones), create one record
            // covering the full range from first remainder start to last remainder end
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
                combinedRemainder.setAcademicYear(existingDist.getAcademicYear());
                combinedRemainder.setState(existingDist.getState());
                combinedRemainder.setCity(existingDist.getCity());
                combinedRemainder.setZone(existingDist.getZone());
                combinedRemainder.setDistrict(existingDist.getDistrict());
                combinedRemainder.setIssuedByType(existingDist.getIssuedByType());
                combinedRemainder.setIssuedToType(existingDist.getIssuedToType());
                combinedRemainder.setCreated_by(existingDist.getCreated_by());
                combinedRemainder.setIssueDate(java.time.LocalDateTime.now());
                combinedRemainder.setAmount(existingDist.getAmount());
                combinedRemainder.setIssued_to_emp_id(existingDist.getIssued_to_emp_id());
                combinedRemainder.setIssued_to_pro_id(existingDist.getIssued_to_pro_id());
                
                // Set the combined range and count
                combinedRemainder.setAppStartNo(combinedStart);
                combinedRemainder.setAppEndNo(combinedEnd);
                combinedRemainder.setTotalAppCount(totalCount); // Use actual count, not range size
                combinedRemainder.setIsActive(1);
                
                System.out.println("=== DISTRIBUTION SAVE (COMBINED REMAINDER) - ZoneService ===");
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
        
        // 6. Check if the new range overlaps with what Zone already distributed
        // If it does, we should NOT create the new distribution record for that overlapping part
        List<Distribution> overlappingSubDists = distributionRepository.findSubDistributionsInRange(
                existingDist.getIssued_to_emp_id(),
                existingDist.getAcademicYear().getAcdcYearId(),
                originalAmount,
                newStart,
                newEnd);
        
        if (!overlappingSubDists.isEmpty()) {
            System.out.println("⚠️ WARNING: New range " + newStart + " - " + newEnd + 
                    " overlaps with " + overlappingSubDists.size() + " existing sub-distribution(s)");
            System.out.println("  The new distribution record will be created, but Zone already distributed parts of this range");
            for (Distribution sub : overlappingSubDists) {
                System.out.println("    - Sub-distribution ID: " + sub.getAppDistributionId() + 
                        ", Range: " + sub.getAppStartNo() + " - " + sub.getAppEndNo());
            }
        }

        // CRITICAL: Flush all distribution changes (inactivation, new record, remainders) 
        // before recalculating balances to ensure balance calculation sees the latest data
        distributionRepository.flush();
        System.out.println("DEBUG: Flushed all distribution changes before balance recalculation");

        // 6. Recalculate Balances
        int acYear = existingDist.getAcademicYear().getAcdcYearId();
        int stateId = existingDist.getState().getStateId();

        // A. Issuer
        recalculateBalanceForEmployee(request.getCreatedBy(), acYear, stateId, request.getIssuedByTypeId(),
                request.getCreatedBy(), originalAmount);

        // B. New Receiver
        System.out.println("DEBUG: Recalculating balance for NEW receiver: " + newTargetId + 
                ", Amount: " + originalAmount + ", Type: " + request.getIssuedToTypeId());
        recalculateBalanceForEmployee(newTargetId, acYear, stateId, request.getIssuedToTypeId(), request.getCreatedBy(),
                originalAmount);

        // C. Old Receiver - ALWAYS recalculate if range changed or receiver changed
        // This ensures BalanceTrack reflects the updated distribution (including remainders)
        Integer oldId = existingDist.getIssued_to_emp_id();
        boolean rangeChanged = (oldStart != request.getAppStartNo() || oldEnd != request.getAppEndNo());
        boolean receiverChanged = (oldId != null && !Objects.equals(oldId, newTargetId));
        
        if (oldId != null && (receiverChanged || rangeChanged)) {
            System.out.println("DEBUG: Recalculating balance for old receiver: " + oldId + 
                    " (receiverChanged: " + receiverChanged + ", rangeChanged: " + rangeChanged + ")");
            recalculateBalanceForEmployee(oldId, acYear, stateId, existingDist.getIssuedToType().getAppIssuedId(),
                    request.getCreatedBy(), originalAmount);
        } else if (oldId != null) {
            System.out.println("DEBUG: Skipping balance recalculation for old receiver: " + oldId + 
                    " (no changes detected)");
        }
        
        // CRITICAL: Flush balance updates to ensure they're persisted
        balanceTrackRepository.flush();
    }

    private void createAndSaveRemainder(Distribution originalDist, int start, int end) {
        Distribution remainder = new Distribution();

        // Copy standard fields
        remainder.setAcademicYear(originalDist.getAcademicYear());
        remainder.setState(originalDist.getState());
        remainder.setCity(originalDist.getCity());
        remainder.setZone(originalDist.getZone());
        remainder.setDistrict(originalDist.getDistrict());
        remainder.setIssuedByType(originalDist.getIssuedByType());
        remainder.setIssuedToType(originalDist.getIssuedToType());
        remainder.setCreated_by(originalDist.getCreated_by());
        // Set current timestamp for new remainder distribution
        remainder.setIssueDate(LocalDateTime.now());
        remainder.setAmount(originalDist.getAmount());
        remainder.setIssued_to_emp_id(originalDist.getIssued_to_emp_id());
        remainder.setIssued_to_pro_id(originalDist.getIssued_to_pro_id());

        // Set New Range
        remainder.setAppStartNo(start);
        remainder.setAppEndNo(end);
        remainder.setTotalAppCount((end - start) + 1);
        remainder.setIsActive(1); // Active

        System.out.println("=== DISTRIBUTION SAVE (REMAINDER) - ZoneService ===");
        System.out.println("Operation: CREATE REMAINDER DISTRIBUTION");
        System.out.println("Remainder Range: " + remainder.getAppStartNo() + " - " + remainder.getAppEndNo());
        System.out.println("Original Dist ID: " + originalDist.getAppDistributionId());
        System.out.println("Original Range: " + originalDist.getAppStartNo() + " - " + originalDist.getAppEndNo());
        System.out.println("Receiver EmpId: " + remainder.getIssued_to_emp_id());
        System.out.println("Amount: " + remainder.getAmount());
        distributionRepository.saveAndFlush(remainder);
        System.out.println("Remainder Distribution ID: " + remainder.getAppDistributionId());
        System.out.println("===================================================");
    }

    private void handleSmartRecallForRange(Distribution originalDist, int dropStart, int dropEnd) {
        System.out.println("  Checking range: " + dropStart + " - " + dropEnd);

        // Find all active sub-distributions originating from the receiver within this
        // dropped range
        List<Distribution> subDists = distributionRepository.findSubDistributionsInRange(
                originalDist.getIssued_to_emp_id(),
                originalDist.getAcademicYear().getAcdcYearId(),
                originalDist.getAmount(),
                dropStart,
                dropEnd);

        if (subDists.isEmpty()) {
            System.out.println("  - Status: FREE (No sub-distributions found. Returns to Issuer.)");
        } else {
            System.out.println("  - Status: OCCUPIED (Found " + subDists.size() + " sub-distributions)");
            for (Distribution sub : subDists) {
                // Ensure we only create remainders for the overlapping part
                int overlapStart = Math.max(dropStart, (int) sub.getAppStartNo());
                int overlapEnd = Math.min(dropEnd, (int) sub.getAppEndNo());

                System.out.println("  - Creating remainder for occupied sub-range: " + overlapStart + " - " + overlapEnd);
                createAndSaveRemainder(originalDist, overlapStart, overlapEnd);
            }
        }
    }

    private boolean handleOverlappingDistributions(List<Distribution> overlappingDists,
            DistributionRequestDTO request) {
        int reqStart = request.getAppStartNo();
        int reqEnd = request.getAppEndNo();
        boolean senderRecalculated = false;

        for (Distribution oldDist : overlappingDists) {

            // 1. Identify the "Old Holder" (The Victim)
            Integer oldHolderId;
            boolean isPro = false;

            if (oldDist.getIssued_to_pro_id() != null) {
                oldHolderId = oldDist.getIssued_to_pro_id();
                isPro = true;
            } else if (oldDist.getIssued_to_emp_id() != null) {
                oldHolderId = oldDist.getIssued_to_emp_id();
                isPro = false;
            } else {
                continue;
            }

            int oldStart = oldDist.getAppStartNo();
            int oldEnd = oldDist.getAppEndNo();

            // NEW LOGIC: Keep Distribution record completely unchanged (no inactivation, no
            // remainders)
            // Distribution table: Keep original record (1-100) as-is, active, unchanged
            // BalanceTrack table: Will be updated via recalculateBalanceForEmployee to show
            // remaining (51-100)
            System.out.println("=== DISTRIBUTION (KEEP COMPLETELY UNCHANGED) - ZoneService ===");
            System.out.println("Operation: KEEP DISTRIBUTION RECORD COMPLETELY UNCHANGED");
            System.out.println("Distribution ID: " + oldDist.getAppDistributionId());
            System.out.println("Original Range: " + oldStart + " - " + oldEnd + " (KEEPING AS-IS, NO CHANGES)");
            System.out.println("New Distribution Range: " + reqStart + " - " + reqEnd);
            System.out.println("Receiver EmpId: " + oldDist.getIssued_to_emp_id());
            System.out.println("Receiver ProId: " + oldDist.getIssued_to_pro_id());
            System.out
                    .println("NOTE: Distribution record will NOT be modified at all (no inactivation, no remainders).");
            System.out.println("NOTE: Only BalanceTrack will be updated to show remaining range (51-100).");

            // Keep original distribution record completely unchanged - no modifications at
            // all
            // No inactivation, no remainder creation
            // BalanceTrack will be calculated based on what's actually available

            System.out.println("===============================================================");

            // 5. Recalculate Balance for the OLD HOLDER (The Victim)
            // We use the Old Distribution's metadata (State, Type, Amount)
            int acYear = request.getAcademicYearId();
            int stateId = oldDist.getState().getStateId();
            int typeId = oldDist.getIssuedToType().getAppIssuedId();
            int modifierId = request.getCreatedBy();
            Float amount = oldDist.getAmount(); // Keep Original Amount

            System.out.println("DEBUG: Recalculating balance for Employee: " + oldHolderId +
                    ", Type: " + typeId + ", Amount: " + amount + ", Sender: " + request.getCreatedBy());

            // CRITICAL: Flush remainders before recalculating balance to ensure they're
            // visible
            distributionRepository.flush();

            recalculateBalanceForEmployee(oldHolderId, acYear, stateId, typeId, modifierId, amount);

            // CRITICAL: Flush balance updates to ensure they're persisted
            balanceTrackRepository.flush();

            // Track if sender's balance was recalculated
            if (oldHolderId != null && oldHolderId.equals(request.getCreatedBy())) {
                senderRecalculated = true;
                System.out.println("DEBUG: Sender's balance was recalculated in handleOverlappingDistributions");
            }
        }

        return senderRecalculated;
    }

    private void recalculateBalanceForEmployee(int employeeId, int academicYearId, int stateId, int typeId,
            int createdBy, Float amount) {

        // 1. CHECK: Is this a CO/Admin? (Check Master Table)
        // We convert Float to Float for the repo call
        Optional<AdminApp> adminApp = adminAppRepository.findByEmpAndYearAndAmount(employeeId, academicYearId, amount);

        if (adminApp.isPresent()) {
            // --- CASE A: CO / ADMIN (The Source) ---
            // Logic: Master Allocation - Total Distributed

            AdminApp master = adminApp.get();

            // Admins usually have 1 giant balance row, so we fetch/create just one.
            List<BalanceTrack> balances = balanceTrackRepository.findActiveBalancesByEmpAndAmount(academicYearId,
                    employeeId, amount);

            BalanceTrack balance;
            if (balances.isEmpty()) {
                balance = createNewBalanceTrack(employeeId, academicYearId, typeId, createdBy);
                balance.setAmount(amount);
            } else {
                balance = balances.get(0); // Use the existing one
            }

            // Calculate Total Distributed by Admin
            int totalDistributed = distributionRepository
                    .sumTotalAppCountByCreatedByAndAmount(employeeId, academicYearId, amount).orElse(0);

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
                System.out.println("DEBUG: Admin/CO distributed up to " + maxDistributedEnd.get() +
                        ", setting app_from to " + calculatedAppFrom);
            } else {
                // They haven't distributed from the beginning yet - use master start
                calculatedAppFrom = master.getAppFromNo();
                System.out.println(
                        "DEBUG: Admin/CO hasn't distributed from beginning, using master start: " + calculatedAppFrom);
            }

            // Ensure app_from doesn't exceed master end
            if (calculatedAppFrom > master.getAppToNo()) {
                calculatedAppFrom = master.getAppToNo() + 1; // All apps distributed
            }

            // Update Logic
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

            // CRITICAL: Save and flush to ensure the balance update is persisted
            balanceTrackRepository.saveAndFlush(balance);

        } else {
            // --- CASE B: ZONE & DGM (The Intermediaries) ---
            // They do NOT have a master table. They rely purely on what they HOLD.
            // We call the helper to rebuild their balance rows to match their holdings.

            rebuildBalancesFromDistributions(employeeId, academicYearId, typeId, createdBy, amount);
        }
    }

    private void rebuildBalancesFromDistributions(int empId, int acYearId, int typeId, int createdBy, Float amount) {

        // CRITICAL: Clear persistence context to ensure we see the latest data
        distributionRepository.flush();

        // 1. Get ALL Active Distributions RECEIVED by this user (what they hold)
        List<Distribution> allReceived = distributionRepository.findActiveHoldingsForEmp(empId, acYearId);
        
        System.out.println("DEBUG: rebuildBalancesFromDistributions - Employee: " + empId + 
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

        System.out.println("DEBUG: rebuildBalancesFromDistributions - Employee: " + empId + 
                ", Received Distributions (after amount filter): " + received.size() + 
                ", Filter Amount: " + amount);
        for (Distribution d : received) {
            System.out.println("  - Distribution ID: " + d.getAppDistributionId() + 
                    ", Range: " + d.getAppStartNo() + "-" + d.getAppEndNo() + 
                    ", Count: " + d.getTotalAppCount());
        }

        // 3. Get ALL Distributions GIVEN AWAY by this user (what they distributed)
        List<Distribution> allGivenAway = distributionRepository.findByCreatedByAndYear(empId, acYearId);

        // 3b. Also find distributions that OVERLAP with received ranges but have different issued_to_emp_id
        // This catches cases where Admin/others updated and transferred apps away from this employee
        // Example: Admin updates Zone 4004's distribution, taking 25 apps and giving to Zone 4011
        // The new distribution has created_by = Admin, but it overlaps with Zone 4004's received range
        // CRITICAL: Only count as "taken away" if the overlapping distribution was created AFTER the received distribution
        // OR if it represents apps that were actually taken from this employee's original range
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
                //    AND it must represent apps that were actually taken from this employee
                //    This means: overlap.start >= receivedStart AND overlap.end <= receivedEnd
                //    OR the overlap was created by someone else (Admin) and represents a transfer
                int overlapStart = (int) overlap.getAppStartNo();
                int overlapEnd = (int) overlap.getAppEndNo();
                
                // Check if this overlap is actually WITHIN the received range (overlap is subset of received)
                // This means: overlap.start >= receivedStart AND overlap.end <= receivedEnd
                boolean isWithinReceivedRange = overlapStart >= receivedStart && overlapEnd <= receivedEnd;
                
                // CRITICAL: EXCLUDE if the overlap CONTAINS the received range (remainder distributions)
                // Example: Received: 2875052-2875076, Overlap: 2875002-2875101 (remainder for Zone 4004)
                // This should NOT be counted as "given away" for Zone 4011
                boolean overlapContainsReceived = overlapStart <= receivedStart && overlapEnd >= receivedEnd;
                
                // Check if it was created by Admin/CO (represents a transfer/update)
                boolean isAdminTransfer = overlap.getCreated_by() != empId &&
                        overlap.getIssuedByType() != null &&
                        overlap.getIssuedByType().getAppIssuedId() == 1; // Admin/CO type
                
                // Only count as "taken away" if:
                // 1. The overlap is WITHIN the received range (subset), AND
                // 2. It was created by Admin (transfer), AND
                // 3. It does NOT contain the received range (exclude remainders)
                boolean shouldCountAsTakenAway = isWithinReceivedRange && 
                        isAdminTransfer && 
                        !overlapContainsReceived;
                
                if (Math.abs(overlap.getAmount() - amount) < 0.01 &&
                    overlap.getIssued_to_emp_id() != null &&
                    !overlap.getIssued_to_emp_id().equals(empId) &&
                    overlap.getIsActive() == 1 &&
                    !allGivenAway.contains(overlap) &&
                    shouldCountAsTakenAway) {
                    overlappingTakenAway.add(overlap);
                    System.out.println("DEBUG: Found overlapping distribution taken away - ID: " + 
                            overlap.getAppDistributionId() + ", Range: " + 
                            overlap.getAppStartNo() + "-" + overlap.getAppEndNo() + 
                            ", Issued to: " + overlap.getIssued_to_emp_id() + 
                            ", Created by: " + overlap.getCreated_by() +
                            ", IsWithinReceived: " + isWithinReceivedRange +
                            ", IsAdminTransfer: " + isAdminTransfer);
                } else {
                    System.out.println("DEBUG: Excluding overlapping distribution - ID: " + 
                            overlap.getAppDistributionId() + ", Range: " + 
                            overlap.getAppStartNo() + "-" + overlap.getAppEndNo() + 
                            ", Issued to: " + overlap.getIssued_to_emp_id() + 
                            ", Created by: " + overlap.getCreated_by() +
                            " (not taken from this employee)");
                }
            }
        }

        // 4. Combine both lists and filter by amount
        // NOTE: BalanceTrack represents AVAILABLE stock for distribution
        // So ALL distributions (including DGM Type 3 and Campus Type 4) ARE subtracted
        // because they're no longer available for the Zone/DGM to distribute
        // The Distribution table shows total holdings (including what's with DGM/Campus)
        // But BalanceTrack shows what's actually available to distribute
        List<Distribution> givenAway = new java.util.ArrayList<>(allGivenAway);
        givenAway.addAll(overlappingTakenAway);
        givenAway = givenAway.stream()
                .filter(d -> Math.abs(d.getAmount() - amount) < 0.01)
                .sorted((d1, d2) -> Long.compare(d1.getAppStartNo(), d2.getAppStartNo()))
                .toList();

        System.out.println("DEBUG: rebuildBalancesFromDistributions - Employee: " + empId
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
            System.out.println("⚠️ WARNING: No received distributions found for Employee " + empId + 
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

                System.out.println("DEBUG: Processing received distribution: " + receivedStart + " - " + receivedEnd);

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
                        System.out.println("DEBUG: Found overlap - Given away: " + givenStart + "-" + givenEnd +
                                ", Overlaps with received: " + overlapStart + "-" + overlapEnd);
                    }
                }

                // Calculate remaining ranges (received minus given away)
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

                    System.out.println("DEBUG: Updated/Created balance row - AppFrom: " + saved.getAppFrom() +
                            ", AppTo: " + saved.getAppTo() + ", Count: " + saved.getAppAvblCnt() +
                            ", IsActive: " + saved.getIsActive());
                }
            }
        }

        // FINAL CHECK: If no active balance rows were created, create a dummy active
        // one with 0 balance
        // This happens when everything received has been given away.
        if (!atLeastOneActiveRowCreated) {
            System.out.println("DEBUG: All stock distributed. Ensuring one active zero-balance row.");
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
            System.out.println("DEBUG: Deactivating unused old balance row ID: " + unused.getAppBalanceTrkId());
        }

        // Final flush
        balanceTrackRepository.flush();
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

    private Distribution createRemainderDistribution(Distribution originalDist, int receiverId) {
        Distribution remainderDistribution = new Distribution();
        // Copy most fields from the original distribution
        mapDtoToDistribution(remainderDistribution, createDtoFromDistribution(originalDist));

        // Set specific fields for the remainder
        remainderDistribution.setIssued_to_emp_id(receiverId); // Stays with the OLD receiver
        remainderDistribution.setIsActive(1);

        // Note: The range and count will be set by the caller (updateDistribution)
        return remainderDistribution;
    }

    private void mapDtoToDistribution(Distribution d, DistributionRequestDTO req) {
        d.setAcademicYear(academicYearRepository.findById(req.getAcademicYearId()).orElseThrow());
        d.setState(stateRepository.findById(req.getStateId()).orElseThrow());
        d.setZone(zoneRepository.findById(req.getZoneId()).orElseThrow());
        d.setIssuedByType(appIssuedTypeRepository.findById(req.getIssuedByTypeId()).orElseThrow());
        d.setIssuedToType(appIssuedTypeRepository.findById(req.getIssuedToTypeId()).orElseThrow());
        City city = cityRepository.findById(req.getCityId()).orElseThrow();
        d.setCity(city);
        d.setDistrict(city.getDistrict());
        d.setAmount(req.getApplication_Amount());
        d.setIssueDate(LocalDateTime.now());
        d.setIssued_to_emp_id(req.getIssuedToEmpId());
        d.setCreated_by(req.getCreatedBy());
        d.setAppStartNo(req.getAppStartNo());
        d.setAppEndNo(req.getAppEndNo());
        d.setTotalAppCount(req.getRange());
        d.setIsActive(1);
    }

    private DistributionRequestDTO createDtoFromDistribution(Distribution dist) {
        DistributionRequestDTO dto = new DistributionRequestDTO();
        dto.setAcademicYearId(dist.getAcademicYear().getAcdcYearId());
        dto.setStateId(dist.getState().getStateId());
        dto.setCityId(dist.getCity().getCityId());
        dto.setZoneId(dist.getZone().getZoneId());
        dto.setIssuedByTypeId(dist.getIssuedByType().getAppIssuedId());
        dto.setIssuedToTypeId(dist.getIssuedToType().getAppIssuedId());
        dto.setIssuedToEmpId(dist.getIssued_to_emp_id());
        dto.setApplication_Amount(dist.getAmount());
        dto.setAppStartNo(dist.getAppStartNo());
        dto.setAppEndNo(dist.getAppEndNo());
        dto.setRange(dist.getTotalAppCount());
        // dto.setIssueDate(dist.getIssueDate());
        dto.setCreatedBy(dist.getCreated_by());
        return dto;
    }

    private BalanceTrack createNewBalanceTrack(int employeeId, int academicYearId, int typeId, int createdBy) {
        BalanceTrack nb = new BalanceTrack();
        nb.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
        nb.setAcademicYear(academicYearRepository.findById(academicYearId).orElseThrow());
        nb.setIssuedByType(appIssuedTypeRepository.findById(typeId).orElseThrow());

        nb.setIssuedToProId(null); // Strict Validation: It's an Employee
        nb.setAppAvblCnt(0);
        nb.setIsActive(1);
        nb.setCreatedBy(createdBy);
        return nb;
    }

    private void validateEmployeeExists(int employeeId, String role) {
        if (employeeId <= 0 || !employeeRepository.existsById(employeeId)) {
            throw new IllegalArgumentException(role + " employee not found or invalid ID: " + employeeId);
        }
    }

    /**
     * Validates that the sender (createdBy) actually owns the requested application
     * range.
     * Checks BalanceTrack for Zone/DGM employees or AdminApp for Admin/CO
     * employees.
     * Also validates that the amount matches available balances.
     * 
     * @param request The distribution request containing the range to validate
     * @throws RuntimeException if the requested range is not available, with
     *                          details of available ranges
     */
    private void validateSenderHasAvailableRange(@NonNull DistributionRequestDTO request) {
        int senderId = request.getCreatedBy();
        int academicYearId = request.getAcademicYearId();
        Float amount = request.getApplication_Amount();
        int requestedStart = request.getAppStartNo();
        int requestedEnd = request.getAppEndNo();

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

    // This is SPECIFICALLY for adding new stock to a Receiver (Zone/DGM)
    private void addStockToReceiver(int employeeId, int academicYearId, int typeId, int createdBy, Float amount,
            int newStart, int newEnd, int newCount) {

        // 1. Calculate the "Target End" (The number immediately before the new batch)
        int targetEnd = newStart - 1;

        // 2. Check if we can MERGE with an existing row
        Optional<BalanceTrack> mergeableRow = balanceTrackRepository.findMergeableRowForEmployee(academicYearId,
                employeeId, amount, targetEnd);

        if (mergeableRow.isPresent()) {
            // SCENARIO: CONTIGUOUS (1-50 exists, adding 51-100)
            BalanceTrack existing = mergeableRow.get();

            // Update the existing row
            existing.setAppTo(newEnd); // Extend the range (50 -> 100)
            existing.setAppAvblCnt(existing.getAppAvblCnt() + newCount); // Add count

            balanceTrackRepository.save(existing);
        } else {
            // SCENARIO: DISTURBED / GAP (1-50 exists, adding 101-150)
            // Create a BRAND NEW row
            BalanceTrack newRow = createNewBalanceTrack(employeeId, academicYearId, typeId, createdBy);
            newRow.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
            newRow.setIssuedToProId(null);
            newRow.setAmount(amount);
            newRow.setIsActive(1);

            // Set the specific range for this packet
            newRow.setAppFrom(newStart);
            newRow.setAppTo(newEnd);
            newRow.setAppAvblCnt(newCount);

            balanceTrackRepository.save(newRow);
        }
    }
}
