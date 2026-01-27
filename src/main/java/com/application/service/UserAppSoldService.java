package com.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.dto.GraphBarDTO;
import com.application.dto.GraphResponseDTO;
import com.application.dto.PerformanceDTO;
import com.application.dto.RateItemDTO;
import com.application.dto.RateResponseDTO;
import com.application.dto.RateSectionDTO;
import com.application.dto.UserAppSoldDTO;
import com.application.entity.AcademicYear;
import com.application.entity.Dgm;
import com.application.entity.SCEmployeeEntity;
import com.application.entity.UserAppSold;
import com.application.entity.ZonalAccountant;
import com.application.repository.AcademicYearRepository;
import com.application.repository.DgmRepository;
import com.application.repository.SCEmployeeRepository;
import com.application.repository.UserAppSoldRepository;
import com.application.repository.ZonalAccountantRepository;

@Service
public class UserAppSoldService {

	@Autowired
	private UserAppSoldRepository userAppSoldRepository;
	@Autowired
	private AcademicYearRepository academicYearRepository;
	@Autowired
	private DgmRepository dgmRepository;
	@Autowired
	private ZonalAccountantRepository zonalAccountantRepository;
	@Autowired
	private SCEmployeeRepository scEmployeeRepository;
	@Autowired
	private com.application.repository.CampusRepository campusRepository;

	private UserAppSoldDTO convertToDto(UserAppSold userAppSold) {
		UserAppSoldDTO dto = new UserAppSoldDTO();
		dto.setEmpId(userAppSold.getEmpId());
		dto.setEntityId(userAppSold.getEntityId());
		dto.setAcdcYearId(userAppSold.getAcdcYearId());
		dto.setTotalAppCount(userAppSold.getTotalAppCount());
		dto.setSold(userAppSold.getSold());
		return dto;
	}

	private List<PerformanceDTO> mapToPerformanceDto(List<Object[]> rawData) {
		return rawData.stream().map(result -> {
			PerformanceDTO dto = new PerformanceDTO();
			dto.setName((String) result[0]);
			dto.setPerformancePercentage(((Number) result[1]).doubleValue());
			return dto;
		}).collect(Collectors.toList());
	}

	public List<RateResponseDTO> getAllRateData(String campusCategory, Integer empId) {
		System.out.println("--- LOG: STARTING RATE CALCULATION FOR EMP_ID: " + empId + " ---");

		// ============================================================
		// Calculate Academic Year based on March 1st transition
		// ============================================================
		// Academic year transitions on March 1st:
		// - Before March 1st: Show 26-27 (acdcYearId = 27, year = 2026)
		// - On/After March 1st: Show 27-28 (acdcYearId = 28, year = 2027)
		java.time.LocalDate today = java.time.LocalDate.now();
		int currentCalendarYear = today.getYear();
		int currentMonth = today.getMonthValue();

		// Calculate the academic year value for performance data
		// Before March 1st: Use current year (e.g., 2026 → shows 26-27, acdcYearId 27)
		// On/After March 1st: Use current year + 1 (e.g., 2027 → shows 27-28,
		// acdcYearId 28)
		int performanceAcademicYearValue = (currentMonth >= 3) ? (currentCalendarYear + 1) : currentCalendarYear;

		System.out.println("========================================");
		System.out.println("PERFORMANCE DATA ACADEMIC YEAR CALCULATION");
		System.out.println("Current Calendar Year: " + currentCalendarYear);
		System.out.println("Current Month: " + currentMonth);
		System.out.println("Performance Academic Year Value (year field to search): " + performanceAcademicYearValue);
		System.out.println("Expected: " + (currentMonth >= 3 ? "After March 1st → year=2027 → acdcYearId=28 (2027-28)"
				: "Before March 1st → year=2026 → acdcYearId=27 (2026-27)"));
		System.out.println("========================================");

		// Find the academic year entity for performance data
		java.util.Optional<com.application.entity.AcademicYear> yearEntityOpt = academicYearRepository
				.findByYearAndIsActive(performanceAcademicYearValue, 1);

		Integer yearId = null;
		if (yearEntityOpt.isPresent()) {
			yearId = yearEntityOpt.get().getAcdcYearId();
			System.out.println("✅ PERFORMANCE DATA: Found academic year - acdcYearId: " + yearId +
					", year: " + yearEntityOpt.get().getYear() +
					", academicYear: " + yearEntityOpt.get().getAcademicYear());
		} else {
			// If academic year not found, try to find the latest active year as fallback
			System.out.println("PERFORMANCE DATA: Academic year with year=" + performanceAcademicYearValue
					+ " not found, trying fallback...");
			java.util.List<com.application.entity.AcademicYear> allActiveYears = academicYearRepository
					.findAllActiveOrderedByYearDesc();
			if (allActiveYears != null && !allActiveYears.isEmpty()) {
				yearId = allActiveYears.get(0).getAcdcYearId();
				System.out.println("PERFORMANCE DATA: Using fallback - acdcYearId: " + yearId);
			} else {
				System.out.println("PERFORMANCE DATA: No active academic years found, returning empty response");
				return new ArrayList<>();
			}
		}

		List<Integer> dgmIds = null;
		List<Integer> campusIds = null;
		String userRole = null;

		// 1. Determine User Role and Fetch Filter IDs
		if (empId != null && empId != 0) {
			// Use findByEmpId which returns List (handles multiple results in view)
			SCEmployeeEntity employee = scEmployeeRepository.findByEmpId(empId)
					.stream()
					.findFirst()
					.orElse(null);

			if (employee != null) {
				userRole = employee.getEmpStudApplicationRole();
				System.out.println("User Role: " + userRole);

				// LOGIC: IF ZONAL ACCOUNTANT -> Filter DGMs and Campuses by Zone
				if ("ZONAL ACCOUNTANT".equalsIgnoreCase(userRole) || "ZONE".equalsIgnoreCase(userRole)
						|| "ZONAL_OFFICER".equalsIgnoreCase(userRole)) {
					// Fetch the Zone ID from ZonalAccountant entity (same as
					// getZoneAccountantPerformance)
					List<ZonalAccountant> zonalRecords = zonalAccountantRepository.findActiveByEmployee(empId);
					if (zonalRecords != null && !zonalRecords.isEmpty() && zonalRecords.get(0).getZone() != null) {
						Integer zoneId = zonalRecords.get(0).getZone().getZoneId();
						System.out.println("ZONAL ACCOUNTANT Logged In (empId: " + empId + ", zoneId: " + zoneId + ")");

						// Get all DGMs under this Zone
						List<Dgm> dgmList = dgmRepository.findByZoneZoneIdAndIsActive(zoneId, 1);
						dgmIds = dgmList.stream()
								.map(d -> d.getEmployee().getEmp_id())
								.collect(java.util.stream.Collectors.toList());
						System.out.println("Zone (" + zoneId + "). Fetched DGM IDs: " + dgmIds);

						// Get all Campuses under this Zone (to filter campus performance)
						List<com.application.entity.Campus> campuses = campusRepository.findByZoneZoneId(zoneId);
						campusIds = campuses.stream()
								.map(com.application.entity.Campus::getCampusId)
								.collect(java.util.stream.Collectors.toList());
						System.out.println("Zone (" + zoneId + "). Fetched Campus IDs: " + campusIds);
					} else {
						System.out.println("⚠️ ZONAL ACCOUNTANT " + empId + " not mapped to zone, showing no data");
						dgmIds = new ArrayList<>();
						campusIds = new ArrayList<>();
					}
				}
				// LOGIC: IF DGM -> Filter Campuses by DGM entity (can have multiple campuses)
				else if ("DGM".equalsIgnoreCase(userRole) || "DIVISIONAL_OFFICER".equalsIgnoreCase(userRole)) {
					// Fetch all DGM records for this employee (can have multiple campuses)
					List<Dgm> dgmList = dgmRepository.findByEmpId(empId);
					if (dgmList != null && !dgmList.isEmpty()) {
						// Filter for active DGMs and collect all campus IDs
						campusIds = dgmList.stream()
								.filter(dgm -> dgm.getIsActive() == 1) // isActive is int, not Integer
								.filter(dgm -> dgm.getCampus() != null)
								.map(dgm -> dgm.getCampus().getCampusId())
								.distinct() // Remove duplicates
								.collect(java.util.stream.Collectors.toList());

						if (!campusIds.isEmpty()) {
							System.out.println("DGM Logged In (empId: " + empId + ", campusIds: " + campusIds + ")");
						} else {
							System.out.println("⚠️ DGM " + empId + " has no active campuses, showing no data");
							campusIds = new ArrayList<>();
						}
					} else {
						System.out.println("⚠️ DGM " + empId + " not found, showing no data");
						campusIds = new ArrayList<>();
					}
				}
				// LOGIC: IF ADMIN -> Filter by Category (COLLEGE ADMIN or SCHOOL ADMIN)
				else if ("ADMIN".equalsIgnoreCase(userRole) ||
						"COLLEGE ADMIN".equalsIgnoreCase(userRole) ||
						"SCHOOL ADMIN".equalsIgnoreCase(userRole)) {
					// Determine category based on role or employee category field
					String adminCategory = null;
					if ("COLLEGE ADMIN".equalsIgnoreCase(userRole)) {
						adminCategory = "college";
						System.out.println("COLLEGE ADMIN Logged In (" + empId + "). Filtering by COLLEGE category.");
					} else if ("SCHOOL ADMIN".equalsIgnoreCase(userRole)) {
						adminCategory = "school";
						System.out.println("SCHOOL ADMIN Logged In (" + empId + "). Filtering by SCHOOL category.");
					} else if ("ADMIN".equalsIgnoreCase(userRole)) {
						// For generic ADMIN, check employee category field as fallback
						String empCategory = employee.getCategory();
						if (empCategory != null && !empCategory.trim().isEmpty()) {
							String categoryLower = empCategory.trim().toLowerCase();
							if (categoryLower.contains("college")) {
								adminCategory = "college";
								System.out.println("ADMIN Logged In (" + empId + "). Using employee category: COLLEGE");
							} else if (categoryLower.contains("school")) {
								adminCategory = "school";
								System.out.println("ADMIN Logged In (" + empId + "). Using employee category: SCHOOL");
							} else {
								System.out.println(
										"ADMIN Logged In (" + empId + "). No category filter - showing ALL data.");
							}
						} else {
							System.out
									.println("ADMIN Logged In (" + empId + "). No category found - showing ALL data.");
						}
					}
					// Override campusCategory parameter if admin has specific category
					if (adminCategory != null) {
						campusCategory = adminCategory;
						System.out.println("Setting campusCategory to: " + campusCategory);
					}
				}
			}
		}

		// 2. Pass the calculated IDs and Role to the core logic
		// Determine visibility based on role
		boolean showZone = true;
		boolean showDgm = true;
		boolean showCampus = true;

		if (empId != null && empId != 0) {
			// Utilize the userRole determined above
			if (userRole != null) {
				if ("ZONAL ACCOUNTANT".equalsIgnoreCase(userRole) || "ZONE".equalsIgnoreCase(userRole)
						|| "ZONAL_OFFICER".equalsIgnoreCase(userRole)) {
					showZone = false; // Zonal Accountant/Officer: DGMs + Campuses
					showDgm = true;
					showCampus = true;
				} else if ("DGM".equalsIgnoreCase(userRole) || "DIVISIONAL_OFFICER".equalsIgnoreCase(userRole)) {
					showZone = false; // DGM/Divisional Officer: Campuses only
					showDgm = false;
					showCampus = true;
				} else if ("ADMIN".equalsIgnoreCase(userRole) ||
						"COLLEGE ADMIN".equalsIgnoreCase(userRole) ||
						"SCHOOL ADMIN".equalsIgnoreCase(userRole)) {
					showZone = true;
					showDgm = true;
					showCampus = true;
				}
			}
		}

		return calculateRateDataInternal(campusCategory, dgmIds, campusIds, showZone, showDgm, showCampus, yearId);
	}

	private List<RateResponseDTO> calculateRateDataInternal(
			String campusCategory,
			List<Integer> dgmIds,
			List<Integer> campusIds,
			boolean showZone,
			boolean showDgm,
			boolean showCampus,
			Integer yearId) {

		String category = (campusCategory != null && !campusCategory.trim().isEmpty()) ? campusCategory.trim() : null;
		List<RateResponseDTO> responseList = new ArrayList<>();

		// --- 1. ZONES (Always Global / Category based) ---
		if (showZone) {
			List<Object[]> zoneRaw = (category != null)
					? userAppSoldRepository.findZonePerformanceNativeByCategoryAndYear(category, yearId)
					: userAppSoldRepository.findZonePerformanceNativeByYear(yearId);

			responseList.add(processAnalytics("zone", "DISTRIBUTE_ZONE", "Application Drop Rated Zone Wise",
					"Application Top Rated Zone Wise", mapToPerformanceDTO(zoneRaw)));
		}

		// --- 2. DGMS (Filtered if Zone Logged In) ---
		if (showDgm) {
			List<Object[]> dgmRaw;
			if (dgmIds != null && !dgmIds.isEmpty()) {
				dgmRaw = userAppSoldRepository.findDgmPerformanceForZoneAndYear(dgmIds, yearId);
			} else {
				// If filtering by IDs is NOT requested but showDgm IS true (e.g. Admin), fetch
				// all
				dgmRaw = (category != null)
						? userAppSoldRepository.findDgmPerformanceNativeByCategoryAndYear(category, yearId)
						: userAppSoldRepository.findDgmPerformanceNativeByYear(yearId);
			}
			responseList.add(processAnalytics("dgm", "DISTRIBUTE_DGM", "Application Drop Rated DGM Wise",
					"Application Top Rated DGM Wise", mapToPerformanceDTO(dgmRaw)));
		}

		// --- 3. CAMPUSES (Filtered if DGM Logged In) ---
		if (showCampus) {
			List<Object[]> campusRaw;
			if (campusIds != null && !campusIds.isEmpty()) {
				campusRaw = userAppSoldRepository.findCampusPerformanceForDgmAndYear(campusIds, yearId);
			} else {
				campusRaw = (category != null)
						? userAppSoldRepository.findCampusPerformanceNativeByCategoryAndYear(category, yearId)
						: userAppSoldRepository.findCampusPerformanceNativeByYear(yearId);
			}
			responseList.add(processAnalytics("campus", "DISTRIBUTE_CAMPUS", "Application Drop Rated Branch Wise",
					"Application Top Rated Branch Wise", mapToPerformanceDTO(campusRaw)));
		}

		return responseList;
	}

	// --- Helper to convert Native Query Object[] to DTO ---
	private List<PerformanceDTO> mapToPerformanceDTO(List<Object[]> rawData) {
		List<PerformanceDTO> list = new ArrayList<>();
		if (rawData == null)
			return list;

		for (Object[] row : rawData) {
			String name = (String) row[0];
			// Handle BigDecimal vs Double conversion safely
			Double rate = 0.0;
			if (row[1] != null) {
				rate = ((Number) row[1]).doubleValue(); // Works for BigDecimal, Double, Float
			}
			list.add(new PerformanceDTO(name, rate));

			// LOGGING TO SEE VALUES
			System.out.println("Mapped: " + name + " -> " + rate);
		}
		return list;
	}

	private RateResponseDTO processAnalytics(String type, String permKey, String dropTitle, String topTitle,
			List<PerformanceDTO> data) {

		if (data == null || data.isEmpty()) {
			return new RateResponseDTO(type, permKey, new RateSectionDTO(dropTitle, new ArrayList<>()),
					new RateSectionDTO(topTitle, new ArrayList<>()));
		}

		List<PerformanceDTO> all = new ArrayList<>(data);

		boolean allZero = all.stream().allMatch(d -> d.getPerformancePercentage() == 0.0);

		List<RateItemDTO> top4;
		List<RateItemDTO> drop4;

		if (allZero) {
			// ⭐ CASE 1 — ALL rates are zero → alphabetic sorting only
			List<PerformanceDTO> alphaSorted = all.stream().sorted(Comparator.comparing(PerformanceDTO::getName))
					.collect(Collectors.toList());

			// Top 4 = First 4 alphabetically
			top4 = alphaSorted.stream().limit(4).map(p -> new RateItemDTO(p.getName(), p.getPerformancePercentage()))
					.collect(Collectors.toList());

			// Drop 4 = Last 4 alphabetically
			drop4 = alphaSorted.stream().skip(Math.max(alphaSorted.size() - 4, 0))
					.map(p -> new RateItemDTO(p.getName(), p.getPerformancePercentage())).collect(Collectors.toList());

		} else {

			// ⭐ CASE 2 — Mixed percentages → Sort by percentage + alphabetical tie-break
			top4 = all.stream()
					.sorted(Comparator.comparingDouble(PerformanceDTO::getPerformancePercentage).reversed()
							.thenComparing(PerformanceDTO::getName))
					.limit(4).map(p -> new RateItemDTO(p.getName(), p.getPerformancePercentage()))
					.collect(Collectors.toList());

			drop4 = all.stream()
					.sorted(Comparator.comparingDouble(PerformanceDTO::getPerformancePercentage)
							.thenComparing(PerformanceDTO::getName))
					.limit(4).map(p -> new RateItemDTO(p.getName(), p.getPerformancePercentage()))
					.collect(Collectors.toList());
		}

		return new RateResponseDTO(type, permKey, new RateSectionDTO(dropTitle, drop4),
				new RateSectionDTO(topTitle, top4));
	}

	private RateResponseDTO buildResponse(String type, String permission, String dropTitle, String topTitle,
			List<Object[]> raw) {

		List<RateItemDTO> items = new ArrayList<>();

		for (Object[] row : raw) {
			String name = (String) row[0];
			long issued = (Long) row[1];
			long sold = (Long) row[2];

			double percent = 0;

			if (issued > 0) {
				percent = ((double) sold / issued) * 100;
			}

			items.add(new RateItemDTO(name, percent));
		}

		List<RateItemDTO> topRated = items.stream().sorted((a, b) -> Double.compare(b.getRate(), a.getRate())).limit(4)
				.toList();

		List<RateItemDTO> dropRated = items.stream().sorted(Comparator.comparingDouble(RateItemDTO::getRate)).limit(4)
				.toList();

		return new RateResponseDTO(type, permission, new RateSectionDTO(dropTitle, dropRated),
				new RateSectionDTO(topTitle, topRated));
	}

	public GraphResponseDTO generateYearWiseIssuedSoldPercentage() {

		List<Object[]> rows = userAppSoldRepository.getYearWiseIssuedAndSold();
		// rows → [acdcYearId, SUM(totalAppCount), SUM(sold)]

		Map<Integer, AcademicYear> yearMap = academicYearRepository.findAll().stream()
				.collect(Collectors.toMap(AcademicYear::getAcdcYearId, y -> y));

		List<GraphBarDTO> barList = new ArrayList<>();

		for (Object[] row : rows) {
			Integer yearId = (Integer) row[0];
			Long issued = row[1] != null ? ((Number) row[1]).longValue() : 0L;
			Long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;

			AcademicYear y = yearMap.get(yearId);
			String yearLabel = y != null ? y.getAcademicYear() : "Unknown Year";

			// Calculate sold percentage relative to issued
			int issuedPercent = 100;
			int soldPercent = 0;

			if (issued > 0) {
				soldPercent = (int) Math.round((sold.doubleValue() / issued.doubleValue()) * 100);
			}

			// ✅ Include both percentage and actual count in the DTO
			GraphBarDTO dto = new GraphBarDTO();
			dto.setYear(yearLabel);
			dto.setIssuedPercent(issuedPercent);
			dto.setSoldPercent(soldPercent);
			dto.setIssuedCount(issued.intValue());
			dto.setSoldCount(sold.intValue());

			barList.add(dto);
		}

		GraphResponseDTO response = new GraphResponseDTO();
		response.setGraphBarData(barList);

		return response;
	}

	public RateResponseDTO getZoneAccountantPerformance(Integer empId) {

		// 1️⃣ Fetch ALL Zonal Accountant records for this employee
		List<ZonalAccountant> zaList = zonalAccountantRepository.findActiveByEmployee(empId);

		if (zaList.isEmpty()) {
			throw new RuntimeException("Zonal Accountant not found or inactive");
		}

		// 2️⃣ Pick the FIRST record (all rows have same zone_id)
		Integer zoneId = zaList.get(0).getZone().getZoneId();

		// 3️⃣ Fetch ALL DGMs under this zone
		List<Dgm> dgmList = dgmRepository.findByZoneZoneIdAndIsActive(zoneId, 1);

		List<Integer> dgmEmpIds = dgmList.stream().map(d -> d.getEmployee().getEmp_id()) // FIX: getEmpId() not
																							// get_emp_id()
				.collect(Collectors.toList());

		if (dgmEmpIds.isEmpty()) {
			return new RateResponseDTO("dgm", "ZONE_DGM",
					new RateSectionDTO("Application Drop Rate DGM Wise", new ArrayList<>()),
					new RateSectionDTO("Top Rated DGMs", new ArrayList<>()));
		}

		// 4️⃣ Fetch DGM performance from UserAppSold table
		List<Object[]> raw = userAppSoldRepository.findDgmPerformanceForZone(dgmEmpIds);

		// 5️⃣ Convert raw SQL output → DTO list
		List<PerformanceDTO> performanceList = mapToPerformanceDTO(raw);

		// 6️⃣ Run Top/Drop logic (already perfect for admin)
		return processAnalytics("dgm", "ZONE_DGM", "Application Drop Rate DGM Wise", "Top Rated DGMs", performanceList);
	}

	public RateResponseDTO getDgmPerformance(Integer empId) {

		// 1️⃣ Fetch DGM record
		Dgm dgm = dgmRepository.findActiveByEmpId(empId)
				.orElseThrow(() -> new RuntimeException("No active DGM record found"));

		Integer campusId = dgm.getCampus().getCampusId();

		// 2️⃣ Campus list (DGM has 1 campus, but using list for consistency)
		List<Integer> campusIds = List.of(campusId);

		// 3️⃣ Fetch performance from UserAppSold
		List<Object[]> raw = userAppSoldRepository.findCampusPerformanceForDgm(campusIds);

		// 4️⃣ Convert raw to DTO
		List<PerformanceDTO> performanceList = mapToPerformanceDTO(raw);

		// 5️⃣ Apply Top/Drop logic
		return processAnalytics("campus", "DGM_CAMPUS", "Application Drop Rate Branch Wise",
				"Application Top Rate Branch Wise",
				performanceList);
	}

	public String getRole(Integer empId) {
		SCEmployeeEntity emp = scEmployeeRepository.findByEmpId(empId)
				.stream()
				.findFirst()
				.orElse(null);

		if (emp == null || emp.getEmpStudApplicationRole() == null) {
			return null;
		}

		return emp.getEmpStudApplicationRole().trim().toUpperCase();
	}

	public RateResponseDTO getRoleBasedPerformance(Integer empId) {

		String role = getRole(empId);

		if (role == null) {
			throw new RuntimeException("Employee role not found");
		}

		switch (role) {

			case "ZONAL ACCOUNTANT":
				System.out.println("LOGGED ROLE = ZONAL ACCOUNTANT");
				return getZoneAccountantPerformance(empId);

			case "DGM":
				System.out.println("LOGGED ROLE = DGM");
				return getDgmPerformance(empId);

			default:
				System.out.println("LOGGED ROLE = NOT SUPPORTED");
				return new RateResponseDTO(
						role,
						"NO_PERMISSION",
						null,
						null);
		}
	}

}