// package com.application.repository;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import com.application.dto.AppStatusDTO;
// import com.application.entity.AppStatusTrackView;

// @Repository
// public interface AppStatusTrackViewRepository extends JpaRepository<AppStatusTrackView, Integer> {

//         Optional<AppStatusTrackView> findByNum(Long num);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId ORDER BY a.date DESC")
//         List<AppStatusTrackView> findByCmps_id(@Param("cmpsId") int cmpsId);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = " +
//                         "(SELECT e.campus.campusId FROM Employee e WHERE e.emp_id = :empId)")
//         List<AppStatusTrackView> findByEmployeeCampus(@Param("empId") int empId);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.num = :num AND a.cmps_name = :cmpsName")
//         Optional<AppStatusTrackView> findByNumAndCmps_name(@Param("num") int num, @Param("cmpsName") String cmpsName);

//         @Query("SELECT new com.application.dto.AppStatusDTO(a.num, a.status, a.cmps_name, a.zone_name) " +
//                         "FROM AppStatusTrackView a")
//         List<AppStatusDTO> getAllStatusData();

//         @Query("SELECT new com.application.dto.AppStatusDTO(s.num, s.status, s.cmps_name, s.zone_name) " +
//                         "FROM AppStatusTrackView s JOIN SCEmployeeEntity e ON s.pro_emp_id = e.empId " +
//                         "WHERE LOWER(e.category) = LOWER(:category)") // <--- Case Insensitive Check
//         List<AppStatusDTO> getStatusDataByCategory(@Param("category") String category);

//         @Query("SELECT new com.application.dto.AppStatusDTO( " +
//                         "a.num, " + // applicationNo
//                         "a.status, " + // displayStatus
//                         "a.cmps_name, " + // campus
//                         "a.zone_name ) " + // zone
//                         "FROM AppStatusTrackView a " +
//                         "WHERE a.cmps_id IN :campusIds " +
//                         "ORDER BY a.date DESC")
//         List<AppStatusDTO> findDTOByCampusIds(@Param("campusIds") List<Integer> campusIds);

//         @Query("SELECT a FROM AppStatusTrackView a ORDER BY a.date DESC")
//         List<AppStatusTrackView> findAllLatest();

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.num >= :startNo AND a.num <= :endNo")
//         List<AppStatusTrackView> findByApplicationNumberRange(@Param("startNo") Integer startNo,
//                         @Param("endNo") Integer endNo);

//         // Count applications by status in a range
//         @Query("SELECT COUNT(a) FROM AppStatusTrackView a WHERE a.num >= :startNo AND a.num <= :endNo AND a.status = :status")
//         Long countByApplicationNumberRangeAndStatus(@Param("startNo") Integer startNo, @Param("endNo") Integer endNo,
//                         @Param("status") String status);

//         @Query("SELECT a FROM AppStatusTrackView a JOIN SCEmployeeEntity e ON a.pro_emp_id = e.empId " +
//                         "WHERE LOWER(e.category) = LOWER(:category) ORDER BY a.date DESC")
//         List<AppStatusTrackView> findAllByCategory(@Param("category") String category);

//         // 2. For ZONAL ACCOUNTANT
//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId ORDER BY a.date DESC")
//         List<AppStatusTrackView> findByZone_id(@Param("zoneId") int zoneId);

//         // 3. For DGM (Using dgm_emp_id column in the View)
//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId ORDER BY a.date DESC")
//         List<AppStatusTrackView> findByDgm_emp_id(@Param("dgmEmpId") int dgmEmpId);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.pro_emp_id = :proEmpId")
//         List<AppStatusTrackView> findByPro_emp_id(@Param("proEmpId") int proEmpId);

//         // --- Filtered Queries by Business ID (Category) ---

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId " +
//                         "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) "
//                         +
//                         "ORDER BY a.date DESC")
//         List<AppStatusTrackView> findByZone_idAndBusinessId(@Param("zoneId") int zoneId,
//                         @Param("businessId") int businessId);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId " +
//                         "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) "
//                         +
//                         "ORDER BY a.date DESC")
//         List<AppStatusTrackView> findByDgm_emp_idAndBusinessId(@Param("dgmEmpId") int dgmEmpId,
//                         @Param("businessId") int businessId);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId " +
//                         "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) "
//                         +
//                         "ORDER BY a.date DESC")
//         List<AppStatusTrackView> findByCmps_idAndBusinessId(@Param("cmpsId") int cmpsId,
//                         @Param("businessId") int businessId);

//         @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) ORDER BY a.date DESC")
//         List<AppStatusTrackView> findAllByBusinessId(@Param("businessId") int businessId);

//         // Get year-wise sold, fast sale, and confirmed for admin distributions with
//         // amount filter
//         // Sold = "not confirmed" or "fast sale" (with variations)
//         // Fast Sale = "fast sale" (with variations)
//         // Confirmed = "confirmed"
//         // Uses DISTINCT on (year_id, app_no) to avoid double counting when same
//         // application appears in multiple distributions
//         @Query(value = """
//                         SELECT
//                             distinct_apps.acdc_year_id,
//                             COALESCE(SUM(CASE
//                                 WHEN LOWER(TRIM(distinct_apps.status)) = 'not confirmed'
//                                  OR LOWER(REPLACE(REPLACE(REPLACE(TRIM(distinct_apps.status), ' ', ''), '_', ''), '-', '')) = 'fastsale'
//                                  OR LOWER(TRIM(distinct_apps.status)) = 'fast sale'
//                                 THEN 1 ELSE 0 END), 0) as sold_count,
//                             COALESCE(SUM(CASE
//                                 WHEN LOWER(REPLACE(REPLACE(REPLACE(TRIM(distinct_apps.status), ' ', ''), '_', ''), '-', '')) = 'fastsale'
//                                  OR LOWER(TRIM(distinct_apps.status)) = 'fast sale'
//                                 THEN 1 ELSE 0 END), 0) as fast_sale_count,
//                             COALESCE(SUM(CASE
//                                 WHEN LOWER(TRIM(distinct_apps.status)) = 'confirmed'
//                                 THEN 1 ELSE 0 END), 0) as confirmed_count
//                         FROM (
//                             SELECT DISTINCT
//                                 d.acdc_year_id,
//                                 a.app_no,
//                                 a.status
//                             FROM sce_application.sce_app_distrubution d
//                             INNER JOIN sce_application.sce_app_status_track a ON (
//                                 a.app_no >= d.app_start_no
//                                 AND a.app_no <= d.app_end_no
//                             )
//                             WHERE d.created_by = :employeeId
//                               AND d.is_active = 1
//                               AND d.amount = :amount
//                         ) distinct_apps
//                         GROUP BY distinct_apps.acdc_year_id
//                         ORDER BY distinct_apps.acdc_year_id
//                         """, nativeQuery = true)
//         List<Object[]> getYearWiseSoldFastSaleConfirmedByAdminAndAmount(
//                         @Param("employeeId") Integer employeeId,
//                         @Param("amount") Float amount);
// }
package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.dto.AppStatusDTO;
import com.application.entity.AppStatusTrackView;

@Repository
public interface AppStatusTrackViewRepository extends JpaRepository<AppStatusTrackView, Integer> {

    Optional<AppStatusTrackView> findByNum(Long num);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId ORDER BY a.date DESC")
    List<AppStatusTrackView> findByCmps_id(@Param("cmpsId") int cmpsId);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = " +
            "(SELECT e.campus.campusId FROM Employee e WHERE e.emp_id = :empId)")
    List<AppStatusTrackView> findByEmployeeCampus(@Param("empId") int empId);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.num = :num AND a.cmps_name = :cmpsName")
    Optional<AppStatusTrackView> findByNumAndCmps_name(@Param("num") int num, @Param("cmpsName") String cmpsName);

    @Query("SELECT new com.application.dto.AppStatusDTO(a.num, a.status, a.cmps_name, a.zone_name) " +
            "FROM AppStatusTrackView a")
    List<AppStatusDTO> getAllStatusData();

    @Query("SELECT new com.application.dto.AppStatusDTO(s.num, s.status, s.cmps_name, s.zone_name) " +
            "FROM AppStatusTrackView s JOIN SCEmployeeEntity e ON s.pro_emp_id = e.empId " +
            "WHERE LOWER(e.category) = LOWER(:category)") // <--- Case Insensitive Check
    List<AppStatusDTO> getStatusDataByCategory(@Param("category") String category);

    @Query("SELECT new com.application.dto.AppStatusDTO( " +
            "a.num, " + // applicationNo
            "a.status, " + // displayStatus
            "a.cmps_name, " + // campus
            "a.zone_name ) " + // zone
            "FROM AppStatusTrackView a " +
            "WHERE a.cmps_id IN :campusIds " +
            "ORDER BY a.date DESC")
    List<AppStatusDTO> findDTOByCampusIds(@Param("campusIds") List<Integer> campusIds);

    @Query("SELECT a FROM AppStatusTrackView a ORDER BY a.date DESC")
    List<AppStatusTrackView> findAllLatest();

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.num >= :startNo AND a.num <= :endNo")
    List<AppStatusTrackView> findByApplicationNumberRange(@Param("startNo") Integer startNo,
            @Param("endNo") Integer endNo);

    // Count applications by status in a range
    @Query("SELECT COUNT(a) FROM AppStatusTrackView a WHERE a.num >= :startNo AND a.num <= :endNo AND a.status = :status")
    Long countByApplicationNumberRangeAndStatus(@Param("startNo") Integer startNo, @Param("endNo") Integer endNo,
            @Param("status") String status);

    @Query("SELECT a FROM AppStatusTrackView a JOIN SCEmployeeEntity e ON a.pro_emp_id = e.empId " +
            "WHERE LOWER(e.category) = LOWER(:category) ORDER BY a.date DESC")
    List<AppStatusTrackView> findAllByCategory(@Param("category") String category);

    // 2. For ZONAL ACCOUNTANT
    @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId ORDER BY a.date DESC")
    List<AppStatusTrackView> findByZone_id(@Param("zoneId") int zoneId);

    // 3. For DGM (Using dgm_emp_id column in the View)
    @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId ORDER BY a.date DESC")
    List<AppStatusTrackView> findByDgm_emp_id(@Param("dgmEmpId") int dgmEmpId);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.pro_emp_id = :proEmpId")
    List<AppStatusTrackView> findByPro_emp_id(@Param("proEmpId") int proEmpId);

    // --- Filtered Queries by Business ID (Category) ---

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId " +
            "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "ORDER BY a.date DESC")
    List<AppStatusTrackView> findByZone_idAndBusinessId(@Param("zoneId") int zoneId,
            @Param("businessId") int businessId);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId " +
            "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "ORDER BY a.date DESC")
    List<AppStatusTrackView> findByDgm_emp_idAndBusinessId(@Param("dgmEmpId") int dgmEmpId,
            @Param("businessId") int businessId);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId " +
            "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "ORDER BY a.date DESC")
    List<AppStatusTrackView> findByCmps_idAndBusinessId(@Param("cmpsId") int cmpsId,
            @Param("businessId") int businessId);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) ORDER BY a.date DESC")
    List<AppStatusTrackView> findAllByBusinessId(@Param("businessId") int businessId);

    // --- Filtered Queries by Academic Year IDs ---

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findAllByBusinessIdAndYearIds(@Param("businessId") int businessId, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a JOIN SCEmployeeEntity e ON a.pro_emp_id = e.empId " +
            "WHERE LOWER(e.category) = LOWER(:category) AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findAllByCategoryAndYearIds(@Param("category") String category, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findByZone_idAndYearIds(@Param("zoneId") int zoneId, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId " +
            "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findByZone_idAndBusinessIdAndYearIds(@Param("zoneId") int zoneId,
            @Param("businessId") int businessId, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findByDgm_emp_idAndYearIds(@Param("dgmEmpId") int dgmEmpId, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId " +
            "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findByDgm_emp_idAndBusinessIdAndYearIds(@Param("dgmEmpId") int dgmEmpId,
            @Param("businessId") int businessId, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findByCmps_idAndYearIds(@Param("cmpsId") int cmpsId, @Param("yearIds") List<Integer> yearIds);

    @Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId " +
            "AND a.cmps_id IN (SELECT c.campusId FROM Campus c WHERE c.businessType.businessTypeId = :businessId) " +
            "AND a.acdc_year_id IN :yearIds ORDER BY a.date DESC")
    List<AppStatusTrackView> findByCmps_idAndBusinessIdAndYearIds(@Param("cmpsId") int cmpsId,
            @Param("businessId") int businessId, @Param("yearIds") List<Integer> yearIds);

    // Get year-wise sold, fast sale, and confirmed for admin distributions with
    // amount filter
    // Sold = "not confirmed" or "fast sale" (with variations)
    // Fast Sale = "fast sale" (with variations)
    // Confirmed = "confirmed"
    // Uses DISTINCT on (year_id, app_no) to avoid double counting when same
    // application appears in multiple distributions
    @Query(value = """
                SELECT
                    distinct_apps.acdc_year_id,
                    COALESCE(SUM(CASE
                        WHEN LOWER(TRIM(distinct_apps.status)) = 'not confirmed'
                         OR LOWER(REPLACE(REPLACE(REPLACE(TRIM(distinct_apps.status), ' ', ''), '_', ''), '-', '')) = 'fastsale'
                         OR LOWER(TRIM(distinct_apps.status)) = 'fast sale'
                        THEN 1 ELSE 0 END), 0) as sold_count,
                    COALESCE(SUM(CASE
                        WHEN LOWER(REPLACE(REPLACE(REPLACE(TRIM(distinct_apps.status), ' ', ''), '_', ''), '-', '')) = 'fastsale'
                         OR LOWER(TRIM(distinct_apps.status)) = 'fast sale'
                        THEN 1 ELSE 0 END), 0) as fast_sale_count,
                    COALESCE(SUM(CASE
                        WHEN LOWER(TRIM(distinct_apps.status)) = 'confirmed'
                        THEN 1 ELSE 0 END), 0) as confirmed_count
                FROM (
                    SELECT DISTINCT
                        d.acdc_year_id,
                        a.app_no,
                        a.status
                FROM sce_application.sce_app_distrubution d
                    INNER JOIN sce_application.sce_app_status_track a ON (
                        a.app_no >= d.app_start_no
                        AND a.app_no <= d.app_end_no
                )
                WHERE d.created_by = :employeeId
                  AND d.is_active = 1
                  AND d.amount = :amount
                ) distinct_apps
                GROUP BY distinct_apps.acdc_year_id
                ORDER BY distinct_apps.acdc_year_id
            """, nativeQuery = true)
    List<Object[]> getYearWiseSoldFastSaleConfirmedByAdminAndAmount(
            @Param("employeeId") Integer employeeId,
            @Param("amount") Float amount);
}
