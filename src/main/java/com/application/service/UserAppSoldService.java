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

	public List<RateResponseDTO> getAllRateData() {
		System.out.println("--- LOG: STARTING NATIVE RATE CALCULATION ---");
		List<RateResponseDTO> responseList = new ArrayList<>();

		// 1. ZONES
		List<Object[]> zoneRaw = userAppSoldRepository.findZonePerformanceNative();
		List<PerformanceDTO> zoneData = mapToPerformanceDTO(zoneRaw);
		responseList.add(processAnalytics("zone", "DISTRIBUTE_ZONE", "Application Drop Rate Zone Wise",
				"Top Rated Zones", zoneData));

		// 2. DGMS
		List<Object[]> dgmRaw = userAppSoldRepository.findDgmPerformanceNative();
		List<PerformanceDTO> dgmData = mapToPerformanceDTO(dgmRaw);
		responseList.add(
				processAnalytics("dgm", "DISTRIBUTE_DGM", "Application Drop Rate DGM Wise", "Top Rated DGMs", dgmData));

		// 3. CAMPUSES
		List<Object[]> campusRaw = userAppSoldRepository.findCampusPerformanceNative();
		List<PerformanceDTO> campusData = mapToPerformanceDTO(campusRaw);
		responseList.add(processAnalytics("campus", "DISTRIBUTE_CAMPUS", "Application Drop Rate Campus Wise",
				"Top Rated Campuses", campusData));

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
		return processAnalytics("campus", "DGM_CAMPUS", "Application Drop Rate Campus Wise", "Top Rated Campuses",
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
                    null
            );
    }
}



}
