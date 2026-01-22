// package com.application.repository;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import com.application.entity.Distribution;

// @Repository
// public interface DistributionRepository extends JpaRepository<Distribution, Integer> {

// 	@Query(value = "SELECT * FROM sce_application.sce_app_distrubution d WHERE d.created_by = :empId AND d.is_active = 1", nativeQuery = true)
// 	List<Distribution> findByCreatedBy(@Param("empId") int empId);

// 	@Query(value = "SELECT * FROM sce_application.sce_app_distrubution d WHERE d.created_by=:empId AND d.issued_to_type_id=:issuedToTypeId AND d.is_active=1 AND d.created_date <= CURRENT_TIMESTAMP ORDER BY d.created_date DESC", nativeQuery = true)
// 	List<Distribution> findByCreatedByAndIssuedToType(@Param("empId") int empId,
// 			@Param("issuedToTypeId") int issuedToTypeId);

// 	@Query("SELECT d FROM Distribution d WHERE d.academicYear.acdcYearId = :academicYearId AND d.appStartNo <= :endNo AND d.appEndNo >= :startNo AND d.isActive = 1")
// 	List<Distribution> findOverlappingDistributions(@Param("academicYearId") int academicYearId,
// 			@Param("startNo") int startNo, @Param("endNo") int endNo);

// 	@Query("SELECT d FROM Distribution d WHERE :admissionNo >= d.appStartNo AND :admissionNo <= d.appEndNo AND d.issuedToType.appIssuedId = 4 AND d.isActive = 1")
// 	Optional<Distribution> findProDistributionForAdmissionNumber(@Param("admissionNo") long admissionNo);

// 	@Query("SELECT SUM(d.totalAppCount) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1")
// 	Optional<Integer> sumTotalAppCountByCreatedByAndAmount(@Param("empId") int empId, @Param("yearId") int yearId,
// 			@Param("amount") Float amount);

// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1")
// 	Integer sumTotalAppCountByCreatedByAndYear(@Param("empId") int empId, @Param("yearId") int yearId);

// 	// Find minimum app start number distributed by Admin/CO
// 	@Query("SELECT MIN(d.appStartNo) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1")
// 	Optional<Integer> findMinAppStartNoByCreatedByAndAmount(@Param("empId") int empId, @Param("yearId") int yearId,
// 			@Param("amount") Float amount);

// 	// Find maximum app end number distributed by Admin/CO
// 	@Query("SELECT MAX(d.appEndNo) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1")
// 	Optional<Integer> findMaxAppEndNoByCreatedByAndAmount(@Param("empId") int empId, @Param("yearId") int yearId,
// 			@Param("amount") Float amount);

// 	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1 ORDER BY d.appStartNo ASC")
// 	List<Distribution> findActiveByIssuedToEmpIdAndAmountOrderByStart(
// 			@Param("empId") int empId,
// 			@Param("yearId") int yearId,
// 			@Param("amount") Float amount);

// 	@Query("SELECT d.appDistributionId FROM Distribution d WHERE " +
// 			"d.issued_to_emp_id = :empId " +
// 			"AND d.appStartNo = :start " +
// 			"AND d.appEndNo = :end " +
// 			"AND d.amount = :amount " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> findIdByEmpAndRange(
// 			@Param("empId") int empId,
// 			@Param("start") int start,
// 			@Param("end") int end,
// 			@Param("amount") Double amount);

// 	// 2. GET DISTRIBUTION ID FOR PRO
// 	// Finds the active transaction ID for a specific PRO and Range
// 	@Query("SELECT d.appDistributionId FROM Distribution d WHERE " +
// 			"d.issued_to_pro_id = :proId " +
// 			"AND d.appStartNo = :start " +
// 			"AND d.appEndNo = :end " +
// 			"AND d.amount = :amount " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> findIdByProAndRange(
// 			@Param("proId") int proId,
// 			@Param("start") int start,
// 			@Param("end") int end,
// 			@Param("amount") Double amount);

// 	@Query("SELECT SUM(d.totalAppCount) FROM Distribution d WHERE " +
// 			"d.issued_to_pro_id = :proId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			// "AND d.amount = :amount " + <--- COMMENTED OUT
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumTotalAppCountByIssuedToProIdAndAmount(
// 			@Param("proId") int proId,
// 			@Param("yearId") int yearId,
// 			@Param("amount") Float amount // Keep param to avoid changing Service code, but don't use it
// 	);

// 	@Query("SELECT MIN(d.appStartNo) FROM Distribution d WHERE d.issued_to_pro_id = :proId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1")
// 	Optional<Integer> findMinAppStartNoByIssuedToProId(@Param("proId") int proId, @Param("yearId") int yearId);

// 	// 3. Find Max End Number for PRO
// 	@Query("SELECT MAX(d.appEndNo) FROM Distribution d WHERE d.issued_to_pro_id = :proId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1")
// 	Optional<Integer> findMaxAppEndNoByIssuedToProId(@Param("proId") int proId, @Param("yearId") int yearId);

// 	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1 ORDER BY d.appStartNo ASC")
// 	List<Distribution> findActiveHoldingsForEmp(@Param("empId") Integer empId, @Param("yearId") Integer yearId);

// 	@Query("SELECT d FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1 ORDER BY d.appStartNo ASC")
// 	List<Distribution> findByCreatedByAndYear(@Param("empId") Integer empId, @Param("yearId") Integer yearId);

// 	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount ORDER BY d.appStartNo ASC")
// 	List<Distribution> findAllByIssuedToEmpIdAndAmountIncludingInactive(
// 			@Param("empId") int empId,
// 			@Param("yearId") int yearId,
// 			@Param("amount") Double amount);

// 	// Sum total_app_count for admin-to-campus distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 4) by zone and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 1 " +
// 			"AND d.issuedToType.appIssuedId = 4 " +
// 			"AND d.zone.zoneId = :zoneId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumAdminToCampusDistributionByZoneAndYear(
// 			@Param("zoneId") Integer zoneId,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for admin-to-campus distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 4) by zone, year, and amount
// 	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double counting same application ranges
// 	@Query(value = """
// 			SELECT COALESCE(SUM(unique_ranges.total_app_count), 0)
// 			FROM (
// 				SELECT DISTINCT
// 					d.acdc_year_id,
// 					d.app_start_no,
// 					d.app_end_no,
// 					MAX(d.total_app_count) as total_app_count
// 				FROM sce_application.sce_app_distrubution d
// 				WHERE d.issued_by_type_id = 1
// 					AND d.issued_to_type_id = 4
// 					AND d.zone_id = :zoneId
// 					AND d.acdc_year_id = :yearId
// 					AND d.amount = :amount
// 					AND d.is_active = 1
// 				GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
// 			) unique_ranges
// 			""", nativeQuery = true)
// 	Optional<Integer> sumAdminToCampusDistributionByZoneYearAndAmount(
// 			@Param("zoneId") Integer zoneId,
// 			@Param("yearId") Integer yearId,
// 			@Param("amount") Float amount);

// 	// Sum total_app_count for admin-to-zone distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 2) by zone and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 1 " +
// 			"AND d.issuedToType.appIssuedId = 2 " +
// 			"AND d.zone.zoneId = :zoneId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumAdminToZoneDistributionByZoneAndYear(
// 			@Param("zoneId") Integer zoneId,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for admin-to-DGM distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 3) by zone and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 1 " +
// 			"AND d.issuedToType.appIssuedId = 3 " +
// 			"AND d.zone.zoneId = :zoneId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumAdminToDgmDistributionByZoneAndYear(
// 			@Param("zoneId") Integer zoneId,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for admin-to-DGM distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 3) by zone, year, and amount
// 	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double counting same application ranges
// 	@Query(value = """
// 			SELECT COALESCE(SUM(unique_ranges.total_app_count), 0)
// 			FROM (
// 				SELECT DISTINCT
// 					d.acdc_year_id,
// 					d.app_start_no,
// 					d.app_end_no,
// 					MAX(d.total_app_count) as total_app_count
// 				FROM sce_application.sce_app_distrubution d
// 				WHERE d.issued_by_type_id = 1
// 					AND d.issued_to_type_id = 3
// 					AND d.zone_id = :zoneId
// 					AND d.acdc_year_id = :yearId
// 					AND d.amount = :amount
// 					AND d.is_active = 1
// 				GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
// 			) unique_ranges
// 			""", nativeQuery = true)
// 	Optional<Integer> sumAdminToDgmDistributionByZoneYearAndAmount(
// 			@Param("zoneId") Integer zoneId,
// 			@Param("yearId") Integer yearId,
// 			@Param("amount") Float amount);

// 	// Sum total_app_count for admin-to-campus distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 4) by campusIds and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 1 " +
// 			"AND d.issuedToType.appIssuedId = 4 " +
// 			"AND d.campus.campusId IN :campusIds " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumAdminToCampusDistributionByCampusIdsAndYear(
// 			@Param("campusIds") List<Integer> campusIds,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for zone-to-DGM distributions (issued_by_type_id = 2,
// 	// issued_to_type_id = 3) by DGM employee ID and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 2 " +
// 			"AND d.issuedToType.appIssuedId = 3 " +
// 			"AND d.issued_to_emp_id = :dgmEmpId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumZoneToDgmDistributionByEmpIdAndYear(
// 			@Param("dgmEmpId") Integer dgmEmpId,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for zone-to-campus distributions (issued_by_type_id = 2,
// 	// issued_to_type_id = 4) by campusIds and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 2 " +
// 			"AND d.issuedToType.appIssuedId = 4 " +
// 			"AND d.campus.campusId IN :campusIds " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumZoneToCampusDistributionByCampusIdsAndYear(
// 			@Param("campusIds") List<Integer> campusIds,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for admin-to-DGM distributions (issued_by_type_id = 1,
// 	// issued_to_type_id = 3) by DGM employee ID and year
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"WHERE d.issuedByType.appIssuedId = 1 " +
// 			"AND d.issuedToType.appIssuedId = 3 " +
// 			"AND d.issued_to_emp_id = :dgmEmpId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1")
// 	Optional<Integer> sumAdminToDgmDistributionByEmpIdAndYear(
// 			@Param("dgmEmpId") Integer dgmEmpId,
// 			@Param("yearId") Integer yearId);

// 	// Sum total_app_count for PRO distributions (issued_by_type_id = 4) by PRO
// 	// employee ID and year
// 	// This checks if PRO has distributed applications to others
// 	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
// 			"FROM Distribution d " +
// 			"INNER JOIN d.issuedByType ibt " +
// 			"WHERE d.created_by = :proEmpId " +
// 			"AND d.academicYear.acdcYearId = :yearId " +
// 			"AND d.isActive = 1 " +
// 			"AND ibt.appIssuedId = 4")
// 	Optional<Integer> sumProDistributionByEmpIdAndYear(
// 			@Param("proEmpId") Integer proEmpId,
// 			@Param("yearId") Integer yearId);

// 	// Get year-wise total_app_count from Distribution table for ADMIN
// 	// Filtered by created_by (employeeId) and amount
// 	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double counting same application ranges
// 	// For each unique range, uses the actual range calculation (app_end_no - app_start_no + 1) to ensure accuracy
// 	// Returns [acdc_year_id, total_app_count] grouped by year
// 	@Query(value = """
// 			SELECT
// 				unique_ranges.acdc_year_id,
// 				COALESCE(SUM(unique_ranges.app_count), 0) as total_app_count
// 			FROM (
// 				SELECT DISTINCT
// 					d.acdc_year_id,
// 					d.app_start_no,
// 					d.app_end_no,
// 					-- Use actual range calculation to ensure accuracy: (app_end_no - app_start_no + 1)
// 					-- This ensures we count actual applications in the range, not stored total_app_count which might be duplicated
// 					(d.app_end_no - d.app_start_no + 1) as app_count
// 				FROM sce_application.sce_app_distrubution d
// 				WHERE d.created_by = :employeeId
// 					AND d.amount = :amount
// 					AND d.is_active = 1
// 				GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
// 			) unique_ranges
// 			GROUP BY unique_ranges.acdc_year_id
// 			ORDER BY unique_ranges.acdc_year_id
// 			""", nativeQuery = true)
// 	List<Object[]> getYearWiseTotalAppCountByCreatedByAndAmount(
// 			@Param("employeeId") Integer employeeId,
// 			@Param("amount") Float amount);

// 	// Sum Admin→Campus distributions (issued_by_type_id = 1, issued_to_type_id = 4) by campusIds, year, and amount
// 	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double counting same application ranges
// 	@Query(value = """
// 			SELECT COALESCE(SUM(unique_ranges.app_count), 0)
// 			FROM (
// 				SELECT DISTINCT
// 					d.acdc_year_id,
// 					d.app_start_no,
// 					d.app_end_no,
// 					(d.app_end_no - d.app_start_no + 1) as app_count
// 				FROM sce_application.sce_app_distrubution d
// 				WHERE d.issued_by_type_id = 1
// 					AND d.issued_to_type_id = 4
// 					AND d.cmps_id IN :campusIds
// 					AND d.acdc_year_id = :yearId
// 					AND d.amount = :amount
// 					AND d.is_active = 1
// 				GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
// 			) unique_ranges
// 			""", nativeQuery = true)
// 	Optional<Integer> sumAdminToCampusDistributionByCampusIdsYearAndAmount(
// 			@Param("campusIds") List<Integer> campusIds,
// 			@Param("yearId") Integer yearId,
// 			@Param("amount") Float amount);

// 	// Sum Zone→Campus distributions (issued_by_type_id = 2, issued_to_type_id = 4) by campusIds, year, and amount
// 	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double counting same application ranges
// 	@Query(value = """
// 			SELECT COALESCE(SUM(unique_ranges.app_count), 0)
// 			FROM (
// 				SELECT DISTINCT
// 					d.acdc_year_id,
// 					d.app_start_no,
// 					d.app_end_no,
// 					(d.app_end_no - d.app_start_no + 1) as app_count
// 				FROM sce_application.sce_app_distrubution d
// 				WHERE d.issued_by_type_id = 2
// 					AND d.issued_to_type_id = 4
// 					AND d.cmps_id IN :campusIds
// 					AND d.acdc_year_id = :yearId
// 					AND d.amount = :amount
// 					AND d.is_active = 1
// 				GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
// 			) unique_ranges
// 			""", nativeQuery = true)
// 	Optional<Integer> sumZoneToCampusDistributionByCampusIdsYearAndAmount(
// 			@Param("campusIds") List<Integer> campusIds,
// 			@Param("yearId") Integer yearId,
// 			@Param("amount") Float amount);

// }

package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.Distribution;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, Integer> {

	@Query(value = "SELECT * FROM sce_application.sce_app_distrubution d WHERE d.created_by = :empId AND d.is_active = 1", nativeQuery = true)
	List<Distribution> findByCreatedBy(@Param("empId") int empId);

	@Query(value = "SELECT * FROM sce_application.sce_app_distrubution d WHERE d.created_by=:empId AND d.issued_to_type_id=:issuedToTypeId AND d.is_active=1 AND d.created_date <= CURRENT_TIMESTAMP ORDER BY d.created_date DESC", nativeQuery = true)
	List<Distribution> findByCreatedByAndIssuedToType(@Param("empId") int empId,
			@Param("issuedToTypeId") int issuedToTypeId);

	@Query("SELECT d FROM Distribution d WHERE d.academicYear.acdcYearId = :academicYearId AND d.appStartNo <= :endNo AND d.appEndNo >= :startNo AND d.isActive = 1")
	List<Distribution> findOverlappingDistributions(@Param("academicYearId") int academicYearId,
			@Param("startNo") int startNo, @Param("endNo") int endNo);

	@Query("SELECT d FROM Distribution d WHERE :admissionNo >= d.appStartNo AND :admissionNo <= d.appEndNo AND d.issuedToType.appIssuedId = 4 AND d.isActive = 1 ORDER BY d.appDistributionId DESC")
	List<Distribution> findProDistributionForAdmissionNumber(@Param("admissionNo") long admissionNo);

	@Query("SELECT SUM(d.totalAppCount) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1")
	Optional<Integer> sumTotalAppCountByCreatedByAndAmount(@Param("empId") int empId, @Param("yearId") int yearId,
			@Param("amount") Float amount);

	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1")
	Integer sumTotalAppCountByCreatedByAndYear(@Param("empId") int empId, @Param("yearId") int yearId);

	// Find minimum app start number distributed by Admin/CO
	@Query("SELECT MIN(d.appStartNo) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1")
	Optional<Integer> findMinAppStartNoByCreatedByAndAmount(@Param("empId") int empId, @Param("yearId") int yearId,
			@Param("amount") Float amount);

	// Find maximum app end number distributed by Admin/CO
	@Query("SELECT MAX(d.appEndNo) FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1")
	Optional<Integer> findMaxAppEndNoByCreatedByAndAmount(@Param("empId") int empId, @Param("yearId") int yearId,
			@Param("amount") Float amount);

	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1 ORDER BY d.appStartNo ASC")
	List<Distribution> findActiveByIssuedToEmpIdAndAmountOrderByStart(
			@Param("empId") int empId,
			@Param("yearId") int yearId,
			@Param("amount") Float amount);

	@Query("SELECT d.appDistributionId FROM Distribution d WHERE " +
			"d.issued_to_emp_id = :empId " +
			"AND d.appStartNo = :start " +
			"AND d.appEndNo = :end " +
			"AND d.amount = :amount " +
			"AND d.isActive = 1")
	Optional<Integer> findIdByEmpAndRange(
			@Param("empId") int empId,
			@Param("start") int start,
			@Param("end") int end,
			@Param("amount") Double amount);

	// 2. GET DISTRIBUTION ID FOR PRO
	// Finds the active transaction ID for a specific PRO and Range
	@Query("SELECT d.appDistributionId FROM Distribution d WHERE " +
			"d.issued_to_pro_id = :proId " +
			"AND d.appStartNo = :start " +
			"AND d.appEndNo = :end " +
			"AND d.amount = :amount " +
			"AND d.isActive = 1")
	Optional<Integer> findIdByProAndRange(
			@Param("proId") int proId,
			@Param("start") int start,
			@Param("end") int end,
			@Param("amount") Double amount);

	@Query("SELECT SUM(d.totalAppCount) FROM Distribution d WHERE " +
			"d.issued_to_pro_id = :proId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			// "AND d.amount = :amount " + <--- COMMENTED OUT
			"AND d.isActive = 1")
	Optional<Integer> sumTotalAppCountByIssuedToProIdAndAmount(
			@Param("proId") int proId,
			@Param("yearId") int yearId,
			@Param("amount") Float amount // Keep param to avoid changing Service code, but don't use it
	);

	@Query("SELECT MIN(d.appStartNo) FROM Distribution d WHERE d.issued_to_pro_id = :proId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1")
	Optional<Integer> findMinAppStartNoByIssuedToProId(@Param("proId") int proId, @Param("yearId") int yearId);

	// 3. Find Max End Number for PRO
	@Query("SELECT MAX(d.appEndNo) FROM Distribution d WHERE d.issued_to_pro_id = :proId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1")
	Optional<Integer> findMaxAppEndNoByIssuedToProId(@Param("proId") int proId, @Param("yearId") int yearId);

	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1 ORDER BY d.appStartNo ASC")
	List<Distribution> findActiveHoldingsForEmp(@Param("empId") Integer empId, @Param("yearId") Integer yearId);

	@Query("SELECT d FROM Distribution d WHERE d.created_by = :empId AND d.academicYear.acdcYearId = :yearId AND d.isActive = 1 ORDER BY d.appStartNo ASC")
	List<Distribution> findByCreatedByAndYear(@Param("empId") Integer empId, @Param("yearId") Integer yearId);

	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount ORDER BY d.appStartNo ASC")
	List<Distribution> findAllByIssuedToEmpIdAndAmountIncludingInactive(
			@Param("empId") int empId,
			@Param("yearId") int yearId,
			@Param("amount") Double amount);

	// Sum total_app_count for admin-to-campus distributions (issued_by_type_id = 1,
	// issued_to_type_id = 4) by zone and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 1 " +
			"AND d.issuedToType.appIssuedId = 4 " +
			"AND d.zone.zoneId = :zoneId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumAdminToCampusDistributionByZoneAndYear(
			@Param("zoneId") Integer zoneId,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for admin-to-campus distributions (issued_by_type_id = 1,
	// issued_to_type_id = 4) by zone, year, and amount
	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double
	// counting same application ranges
	@Query(value = """
			SELECT COALESCE(SUM(unique_ranges.total_app_count), 0)
			FROM (
			    SELECT DISTINCT
			        d.acdc_year_id,
			        d.app_start_no,
			        d.app_end_no,
			        MAX(d.total_app_count) as total_app_count
			    FROM sce_application.sce_app_distrubution d
			    WHERE d.issued_by_type_id = 1
			        AND d.issued_to_type_id = 4
			        AND d.zone_id = :zoneId
			        AND d.acdc_year_id = :yearId
			        AND d.amount = :amount
			        AND d.is_active = 1
			    GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
			) unique_ranges
			""", nativeQuery = true)
	Optional<Integer> sumAdminToCampusDistributionByZoneYearAndAmount(
			@Param("zoneId") Integer zoneId,
			@Param("yearId") Integer yearId,
			@Param("amount") Float amount);

	// Sum total_app_count for admin-to-zone distributions (issued_by_type_id = 1,
	// issued_to_type_id = 2) by zone and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 1 " +
			"AND d.issuedToType.appIssuedId = 2 " +
			"AND d.zone.zoneId = :zoneId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumAdminToZoneDistributionByZoneAndYear(
			@Param("zoneId") Integer zoneId,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for admin-to-DGM distributions (issued_by_type_id = 1,
	// issued_to_type_id = 3) by zone and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 1 " +
			"AND d.issuedToType.appIssuedId = 3 " +
			"AND d.zone.zoneId = :zoneId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumAdminToDgmDistributionByZoneAndYear(
			@Param("zoneId") Integer zoneId,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for admin-to-DGM distributions (issued_by_type_id = 1,
	// issued_to_type_id = 3) by zone, year, and amount
	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double
	// counting same application ranges
	@Query(value = """
			SELECT COALESCE(SUM(unique_ranges.total_app_count), 0)
			FROM (
			    SELECT DISTINCT
			        d.acdc_year_id,
			        d.app_start_no,
			        d.app_end_no,
			        MAX(d.total_app_count) as total_app_count
			    FROM sce_application.sce_app_distrubution d
			    WHERE d.issued_by_type_id = 1
			        AND d.issued_to_type_id = 3
			        AND d.zone_id = :zoneId
			        AND d.acdc_year_id = :yearId
			        AND d.amount = :amount
			        AND d.is_active = 1
			    GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
			) unique_ranges
			""", nativeQuery = true)
	Optional<Integer> sumAdminToDgmDistributionByZoneYearAndAmount(
			@Param("zoneId") Integer zoneId,
			@Param("yearId") Integer yearId,
			@Param("amount") Float amount);

	// Sum total_app_count for admin-to-campus distributions (issued_by_type_id = 1,
	// issued_to_type_id = 4) by campusIds and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 1 " +
			"AND d.issuedToType.appIssuedId = 4 " +
			"AND d.campus.campusId IN :campusIds " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumAdminToCampusDistributionByCampusIdsAndYear(
			@Param("campusIds") List<Integer> campusIds,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for zone-to-DGM distributions (issued_by_type_id = 2,
	// issued_to_type_id = 3) by DGM employee ID and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 2 " +
			"AND d.issuedToType.appIssuedId = 3 " +
			"AND d.issued_to_emp_id = :dgmEmpId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumZoneToDgmDistributionByEmpIdAndYear(
			@Param("dgmEmpId") Integer dgmEmpId,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for zone-to-campus distributions (issued_by_type_id = 2,
	// issued_to_type_id = 4) by campusIds and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 2 " +
			"AND d.issuedToType.appIssuedId = 4 " +
			"AND d.campus.campusId IN :campusIds " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumZoneToCampusDistributionByCampusIdsAndYear(
			@Param("campusIds") List<Integer> campusIds,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for admin-to-DGM distributions (issued_by_type_id = 1,
	// issued_to_type_id = 3) by DGM employee ID and year
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"WHERE d.issuedByType.appIssuedId = 1 " +
			"AND d.issuedToType.appIssuedId = 3 " +
			"AND d.issued_to_emp_id = :dgmEmpId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1")
	Optional<Integer> sumAdminToDgmDistributionByEmpIdAndYear(
			@Param("dgmEmpId") Integer dgmEmpId,
			@Param("yearId") Integer yearId);

	// Sum total_app_count for PRO distributions (issued_by_type_id = 4) by PRO
	// employee ID and year
	// This checks if PRO has distributed applications to others
	@Query("SELECT COALESCE(SUM(d.totalAppCount), 0) " +
			"FROM Distribution d " +
			"INNER JOIN d.issuedByType ibt " +
			"WHERE d.created_by = :proEmpId " +
			"AND d.academicYear.acdcYearId = :yearId " +
			"AND d.isActive = 1 " +
			"AND ibt.appIssuedId = 4")
	Optional<Integer> sumProDistributionByEmpIdAndYear(
			@Param("proEmpId") Integer proEmpId,
			@Param("yearId") Integer yearId);

	// Get year-wise total_app_count from Distribution table for ADMIN
	// Filtered by created_by (employeeId) and amount
	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double
	// counting same application ranges
	// For each unique range, uses the actual range calculation (app_end_no -
	// app_start_no + 1) to ensure accuracy
	// Returns [acdc_year_id, total_app_count] grouped by year
	@Query(value = """
			SELECT
			    unique_ranges.acdc_year_id,
			    COALESCE(SUM(unique_ranges.app_count), 0) as total_app_count
			FROM (
			    SELECT DISTINCT
			        d.acdc_year_id,
			        d.app_start_no,
			        d.app_end_no,
			        -- Use actual range calculation to ensure accuracy: (app_end_no - app_start_no + 1)
			        -- This ensures we count actual applications in the range, not stored total_app_count which might be duplicated
			        (d.app_end_no - d.app_start_no + 1) as app_count
			    FROM sce_application.sce_app_distrubution d
			    WHERE d.created_by = :employeeId
			        AND d.amount = :amount
			        AND d.is_active = 1
			    GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
			) unique_ranges
			GROUP BY unique_ranges.acdc_year_id
			ORDER BY unique_ranges.acdc_year_id
			""", nativeQuery = true)
	List<Object[]> getYearWiseTotalAppCountByCreatedByAndAmount(
			@Param("employeeId") Integer employeeId,
			@Param("amount") Float amount);

	// Sum Admin→Campus distributions (issued_by_type_id = 1, issued_to_type_id = 4)
	// by campusIds, year, and amount
	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double
	// counting same application ranges
	@Query(value = """
			SELECT COALESCE(SUM(unique_ranges.app_count), 0)
			FROM (
			    SELECT DISTINCT
			        d.acdc_year_id,
			        d.app_start_no,
			        d.app_end_no,
			        (d.app_end_no - d.app_start_no + 1) as app_count
			    FROM sce_application.sce_app_distrubution d
			    WHERE d.issued_by_type_id = 1
			        AND d.issued_to_type_id = 4
			        AND d.cmps_id IN :campusIds
			        AND d.acdc_year_id = :yearId
			        AND d.amount = :amount
			        AND d.is_active = 1
			    GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
			) unique_ranges
			""", nativeQuery = true)
	Optional<Integer> sumAdminToCampusDistributionByCampusIdsYearAndAmount(
			@Param("campusIds") List<Integer> campusIds,
			@Param("yearId") Integer yearId,
			@Param("amount") Float amount);

	// Sum Zone→Campus distributions (issued_by_type_id = 2, issued_to_type_id = 4)
	// by campusIds, year, and amount
	// Uses DISTINCT on (acdc_year_id, app_start_no, app_end_no) to avoid double
	// counting same application ranges
	@Query(value = """
			SELECT COALESCE(SUM(unique_ranges.app_count), 0)
			FROM (
			    SELECT DISTINCT
			        d.acdc_year_id,
			        d.app_start_no,
			        d.app_end_no,
			        (d.app_end_no - d.app_start_no + 1) as app_count
			    FROM sce_application.sce_app_distrubution d
			    WHERE d.issued_by_type_id = 2
			        AND d.issued_to_type_id = 4
			        AND d.cmps_id IN :campusIds
			        AND d.acdc_year_id = :yearId
			        AND d.amount = :amount
			        AND d.is_active = 1
			    GROUP BY d.acdc_year_id, d.app_start_no, d.app_end_no
			) unique_ranges
			""", nativeQuery = true)
	Optional<Integer> sumZoneToCampusDistributionByCampusIdsYearAndAmount(
			@Param("campusIds") List<Integer> campusIds,
			@Param("yearId") Integer yearId,
			@Param("amount") Float amount);

	// Find distinct academic year IDs for DGM campuses from Distribution table
	// Checks Admin→DGM (issued_by_type_id = 1, issued_to_type_id = 3),
	// Admin→Campus (issued_by_type_id = 1, issued_to_type_id = 4), and
	// Zone→Campus (issued_by_type_id = 2, issued_to_type_id = 4) distributions
	@Query("SELECT DISTINCT d.academicYear.acdcYearId " +
			"FROM Distribution d " +
			"WHERE d.isActive = 1 " +
			"AND ( " +
			"  (d.issuedByType.appIssuedId = 1 AND d.issuedToType.appIssuedId = 3 AND d.issued_to_emp_id = :dgmEmpId) OR "
			+
			"  (d.issuedByType.appIssuedId = 1 AND d.issuedToType.appIssuedId = 4 AND d.campus.campusId IN :campusIds) OR "
			+
			"  (d.issuedByType.appIssuedId = 2 AND d.issuedToType.appIssuedId = 4 AND d.campus.campusId IN :campusIds) "
			+
			")")
	List<Integer> findDistinctYearIdsForDgmCampuses(
			@Param("dgmEmpId") Integer dgmEmpId,
			@Param("campusIds") List<Integer> campusIds);

	@Query("SELECT d FROM Distribution d WHERE d.created_by = :issuerId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.appStartNo <= :endNo AND d.appEndNo >= :startNo AND d.isActive = 1")
	List<Distribution> findSubDistributionsInRange(
			@Param("issuerId") int issuerId,
			@Param("yearId") int yearId,
			@Param("amount") Float amount,
			@Param("startNo") int startNo,
			@Param("endNo") int endNo);

	// Find the parent distribution record that should be "consumed" when distributing applications
	// This finds the active distribution record received by the issuer that starts at the requested start number
	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1 AND d.appStartNo = :startNo ORDER BY d.appStartNo ASC")
	Optional<Distribution> findParentToConsume(
			@Param("empId") int empId,
			@Param("yearId") int yearId,
			@Param("amount") Float amount,
			@Param("startNo") int startNo);

	// Find the parent distribution that CONTAINS the given range (for remainder creation)
	// This finds active distributions received by the issuer that contain the entire range being distributed
	@Query("SELECT d FROM Distribution d WHERE d.issued_to_emp_id = :empId AND d.academicYear.acdcYearId = :yearId AND d.amount = :amount AND d.isActive = 1 AND d.appStartNo <= :startNo AND d.appEndNo >= :endNo ORDER BY d.appStartNo ASC")
	List<Distribution> findParentContainingRange(
			@Param("empId") int empId,
			@Param("yearId") int yearId,
			@Param("amount") Float amount,
			@Param("startNo") int startNo,
			@Param("endNo") int endNo);

}
