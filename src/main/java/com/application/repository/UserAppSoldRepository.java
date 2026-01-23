package com.application.repository;
 
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import com.application.dto.GraphSoldSummaryDTO;
import com.application.dto.MetricsAggregateDTO;
import com.application.entity.UserAppSold;
 
@Repository
public interface UserAppSoldRepository extends JpaRepository<UserAppSold, Long> {
 
    List<UserAppSold> findByEntityId(Integer entityId);
 
    @Query("SELECT u.campus.campusName, SUM(u.totalAppCount), SUM(u.sold) " + "FROM UserAppSold u "
            + "WHERE u.isActive = 1 AND u.entityId = 4 " + "GROUP BY u.campus.campusName")
    List<Object[]> getCampusWiseRates();
 
    @Query("""
                SELECT
                    a.acdcYearId,
                    COALESCE(SUM(CASE WHEN a.entityId = 4 THEN a.totalAppCount ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN a.entityId = 4 THEN a.sold ELSE 0 END), 0)
                FROM UserAppSold a
                WHERE a.isActive = 1
                  AND a.entityId IN (2, 3, 4)
                GROUP BY a.acdcYearId
                ORDER BY a.acdcYearId
            """)
    List<Object[]> getYearWiseIssuedAndSold();
 
    @Query("""
                SELECT NEW com.application.dto.GraphSoldSummaryDTO(
                    COALESCE(SUM(uas.totalAppCount), 0),
                    COALESCE(SUM(uas.sold), 0))
                FROM UserAppSold uas
                WHERE uas.empId = :dgmId
                  AND uas.acdcYearId = :acdcYearId
            """)
    Optional<GraphSoldSummaryDTO> getSalesSummaryByDgm(@Param("dgmId") Integer dgmId,
            @Param("acdcYearId") Integer acdcYearId);
 
    @Query("""
                SELECT NEW com.application.dto.GraphSoldSummaryDTO(
                    COALESCE(SUM(uas.totalAppCount), 0),
                    COALESCE(SUM(uas.sold), 0))
                FROM UserAppSold uas
                WHERE uas.entityId = 2
                  AND uas.zone.zoneId = :zoneId
                  AND uas.acdcYearId = :acdcYearId
            """)
    Optional<GraphSoldSummaryDTO> getSalesSummaryByZone(@Param("zoneId") Integer zoneId,
            @Param("acdcYearId") Integer acdcYearId);
 
    @Query("""
                SELECT NEW com.application.dto.GraphSoldSummaryDTO(
                    COALESCE(SUM(uas.totalAppCount), 0),
                    COALESCE(SUM(uas.sold), 0))
                FROM UserAppSold uas
                WHERE uas.campus.campusId = :campusId
                  AND uas.acdcYearId = :acdcYearId
            """)
    Optional<GraphSoldSummaryDTO> getSalesSummaryByCampus(@Param("campusId") Integer campusId,
            @Param("acdcYearId") Integer acdcYearId);

    // --- NEW: Distribution-based Metrics for Zone ---
    @Query("""
            SELECT NEW com.application.dto.MetricsAggregateDTO(
                COALESCE(SUM(u.totalAppCount), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0),
                0,
                COALESCE(SUM(u.appAvlbCount), 0),
                0,
                0,
                COALESCE(SUM(CASE WHEN d.appDistributionId IS NOT NULL THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0)
            )
            FROM UserAppSold u
            LEFT JOIN Distribution d ON (
                d.zone.zoneId = :zoneId
                AND d.issuedByType.appIssuedId = 1
                AND d.isActive = 1
                AND u.entityId = d.issuedToType.appIssuedId
                AND u.rangeStartNo >= d.appStartNo
                AND u.rangeEndNo <= d.appEndNo
                AND u.acdcYearId = d.academicYear.acdcYearId
            )
            WHERE u.zone.zoneId = :zoneId
              AND u.acdcYearId = :yearId
              AND u.isActive = 1
              AND u.entityId IN (2, 3, 4)
            """)
    Optional<MetricsAggregateDTO> getMetricsByZoneIdAndYearFromDistribution(
            @Param("zoneId") Integer zoneId,
            @Param("yearId") Integer yearId
    );
 
    @Query("""
                SELECT COALESCE(SUM(uas.totalAppCount), 0)
                FROM UserAppSold uas
                WHERE uas.entityId = 4
                  AND uas.zone.zoneId = :zoneId
                  AND uas.acdcYearId = :acdcYearId
            """)
    Optional<Long> getProMetricByZone(@Param("zoneId") Integer zoneId, @Param("acdcYearId") Integer acdcYearId);
 
    @Query("""
                SELECT COALESCE(SUM(uas.totalAppCount), 0)
                FROM UserAppSold uas
                WHERE uas.campus.campusId = :campusId
                  AND uas.acdcYearId = :acdcYearId
            """)
    Optional<Long> getProMetricByCampus(@Param("campusId") Integer campusId, @Param("acdcYearId") Integer acdcYearId);
 
    @Query("""
                SELECT COALESCE(SUM(uas.totalAppCount), 0)
                FROM UserAppSold uas
                WHERE uas.empId = :dgmId
                  AND uas.acdcYearId = :acdcYearId
            """)
    Optional<Long> getProMetricByDgm(@Param("dgmId") Integer dgmId, @Param("acdcYearId") Integer acdcYearId);
 
    // --- NEW: Method for a LIST of campuses (DGM-Rollup) ---
    @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(COALESCE(SUM(uas.totalAppCount), 0), COALESCE(SUM(uas.sold), 0)) FROM UserAppSold uas WHERE uas.entityId = 3 AND uas.campus.id IN :campusIds AND uas.acdcYearId = :yearId")
    Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusList(@Param("campusIds") List<Integer> campusIds,
            @Param("acdcYearId") Integer acdcYearId);
 
    // --- NEW: Method for a LIST of campuses (DGM-Rollup) ---
    @Query("SELECT COALESCE(SUM(uas.totalAppCount), 0) FROM UserAppSold uas WHERE uas.entityId = 3 AND uas.campus.id IN :campusIds AND uas.acdcYearId = :yearId")
    Optional<Long> getProMetricByCampusList(@Param("campusIds") List<Integer> campusIds,
            @Param("acdcYearId") Integer acdcYearId);
 
    // --- Methods to find distinct years for GRAPH ---
 
    @Query("SELECT DISTINCT uas.acdcYearId FROM UserAppSold uas WHERE uas.entityId IN (2, 4) AND uas.zone.id = :zoneId")
    List<Integer> findDistinctYearIdsByZone(@Param("zoneId") Integer zoneId);
 
    @Query("SELECT DISTINCT uas.acdcYearId FROM UserAppSold uas WHERE uas.entityId = 3 AND uas.empId = :dgmId")
    List<Integer> findDistinctYearIdsByDgm(@Param("dgmId") Integer dgmId);
 
    // This method is for a single campus (PRO role)
    @Query("SELECT DISTINCT uas.acdcYearId FROM UserAppSold uas WHERE uas.entityId = 4 AND uas.campus.id = :campusId")
    List<Integer> findDistinctYearIdsByCampus(@Param("campusId") Integer campusId);
 
    // --- NEW: Method for a LIST of campuses (DGM-Rollup) ---
    @Query("SELECT DISTINCT uas.acdcYearId FROM UserAppSold uas WHERE uas.entityId = 3 AND uas.campus.id IN :campusIds")
    List<Integer> findDistinctYearIdsByCampusList(@Param("campusIds") List<Integer> campusIds);
 
    // --- NEW: DGM List query for Zonal Rollup ('With PRO' card) ---
    @Query("SELECT COALESCE(SUM(uas.totalAppCount), 0) FROM UserAppSold uas WHERE uas.entityId = 4 AND uas.empId IN :dgmEmpIds AND uas.acdcYearId = :acdcYearId")
    Optional<Long> getProMetricByDgmList(@Param("dgmEmpIds") List<Integer> dgmEmpIds,
            @Param("acdcYearId") Integer acdcYearId);
 
    @Query("""
            SELECT new com.application.dto.GraphSoldSummaryDTO(
                COALESCE(SUM(CASE WHEN d.appDistributionId IS NOT NULL THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            )
            FROM UserAppSold u
            LEFT JOIN Distribution d ON (
                d.zone.zoneId = :zoneId
                AND d.issuedByType.appIssuedId = 1
                AND d.isActive = 1
                AND u.entityId = d.issuedToType.appIssuedId
                AND u.rangeStartNo >= d.appStartNo
                AND u.rangeEndNo <= d.appEndNo
                AND u.acdcYearId = d.academicYear.acdcYearId
            )
            WHERE u.zone.zoneId = :zoneId
              AND u.acdcYearId = :yearId
              AND u.amount = :amount
              AND u.isActive = 1
              AND u.entityId IN (2, 3, 4)
            """)
    Optional<GraphSoldSummaryDTO> getSalesSummaryByZoneAndAmount(@Param("zoneId") Integer zoneId,
            @Param("yearId") Integer yearId, @Param("amount") Float amount);
 
    @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.zone.zoneId = :zoneId AND u.amount = :amount")
    List<Integer> findDistinctYearIdsByZoneAndAmount(@Param("zoneId") Integer zoneId, @Param("amount") Float amount);
 
    @Query("SELECT new com.application.dto.GraphSoldSummaryDTO(COALESCE(SUM(CASE WHEN u.entityId = 3 THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0), SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END)) "
            + "FROM UserAppSold u " + "WHERE u.campus.id = :campusId AND u.acdcYearId = :yearId AND u.amount = :amount")
    Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusAndAmount(@Param("campusId") Integer campusId,
            @Param("yearId") Integer yearId, @Param("amount") Float amount);
 
    @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.campus.id = :campusId AND u.amount = :amount")
    List<Integer> findDistinctYearIdsByCampusAndAmount(@Param("campusId") Integer campusId,
            @Param("amount") Float amount);
 
    @Query("""
                SELECT
                    a.acdcYearId,
                    COALESCE(SUM(a.totalAppCount), 0),
                    COALESCE(SUM(a.sold), 0)
                FROM UserAppSold a
                WHERE a.isActive = 1
                  AND a.entityId = 3
                  AND a.empId = :empId
                  AND a.acdcYearId IN :yearIds
                GROUP BY a.acdcYearId
                ORDER BY a.acdcYearId
            """)
    List<Object[]> getYearWiseIssuedAndSoldByEmployee(@Param("empId") Integer empId,
            @Param("yearIds") List<Integer> yearIds);
 
    @Query(value = "SELECT z.zone_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s " + "JOIN sce_locations.sce_zone ON s.zone_id = sce_zone.zone_id "
            + "WHERE s.is_active = 1 " + "GROUP BY sce_zone.zone_name", nativeQuery = true)
    List<Object[]> findZonePerformanceNative();
 
// 2. DGMS (Native Query)
    @Query(value = """
          SELECT CONCAT(e.first_name, ' ', e.last_name) AS dgm_name,
                 (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance
          FROM sce_application.sce_user_app_sold s
          JOIN sce_employee.sce_emp e ON s.emp_id = e.emp_id
          WHERE s.is_active = 1
            AND s.entity_id = 3
          GROUP BY e.first_name, e.last_name
      """, nativeQuery = true)
      List<Object[]> findDgmPerformanceNative();
 
 
// 3. CAMPUSES (Native Query)
    @Query(value = "SELECT c.cmps_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s " + "JOIN sce_campus.sce_cmps c ON s.cmps_id = c.cmps_id "
            + "WHERE s.is_active = 1 " + "GROUP BY c.cmps_name", nativeQuery = true)
    List<Object[]> findCampusPerformanceNative();
   
    // --- NEW: Category Filtered Native Queries ---

    // 1. ZONES (Filtered by Category)
    @Query(value = "SELECT z.zone_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s "
            + "JOIN sce_locations.sce_zone z ON s.zone_id = z.zone_id "
            + "JOIN sce_admin.sce_emp_view e ON s.emp_id = e.emp_id "
            + "WHERE s.is_active = 1 "
            + "AND LOWER(e.cmps_category) = LOWER(:category) "
            + "GROUP BY z.zone_name", nativeQuery = true)
    List<Object[]> findZonePerformanceNativeByCategory(@Param("category") String category);

    // 2. DGMS (Filtered by Category)
    @Query(value = """
          SELECT CONCAT(e.first_name, ' ', e.last_name) AS dgm_name,
                 (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance
          FROM sce_application.sce_user_app_sold s
          JOIN sce_employee.sce_emp e ON s.emp_id = e.emp_id
          JOIN sce_admin.sce_emp_view ev ON s.emp_id = ev.emp_id
          WHERE s.is_active = 1
            AND s.entity_id = 3
            AND LOWER(ev.cmps_category) = LOWER(:category)
          GROUP BY e.first_name, e.last_name
      """, nativeQuery = true)
    List<Object[]> findDgmPerformanceNativeByCategory(@Param("category") String category);

    // 3. CAMPUSES (Filtered by Category)
    @Query(value = "SELECT c.cmps_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s "
            + "JOIN sce_campus.sce_cmps c ON s.cmps_id = c.cmps_id "
            + "JOIN sce_admin.sce_emp_view e ON s.emp_id = e.emp_id "
            + "WHERE s.is_active = 1 "
            + "AND LOWER(e.cmps_category) = LOWER(:category) "
            + "GROUP BY c.cmps_name", nativeQuery = true)
    List<Object[]> findCampusPerformanceNativeByCategory(@Param("category") String category);
   
    // --- NEW: Year-filtered Performance Queries ---
    
    // 1. ZONES (Filtered by Year)
    @Query(value = "SELECT z.zone_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s "
            + "JOIN sce_locations.sce_zone z ON s.zone_id = z.zone_id "
            + "WHERE s.is_active = 1 "
            + "AND s.acdc_year_id = :yearId "
            + "GROUP BY z.zone_name", nativeQuery = true)
    List<Object[]> findZonePerformanceNativeByYear(@Param("yearId") Integer yearId);
    
    // 2. ZONES (Filtered by Category and Year)
    @Query(value = "SELECT z.zone_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s "
            + "JOIN sce_locations.sce_zone z ON s.zone_id = z.zone_id "
            + "JOIN sce_admin.sce_emp_view e ON s.emp_id = e.emp_id "
            + "WHERE s.is_active = 1 "
            + "AND s.acdc_year_id = :yearId "
            + "AND LOWER(e.cmps_category) = LOWER(:category) "
            + "GROUP BY z.zone_name", nativeQuery = true)
    List<Object[]> findZonePerformanceNativeByCategoryAndYear(@Param("category") String category, @Param("yearId") Integer yearId);
    
    // 3. DGMS (Filtered by Year)
    @Query(value = """
          SELECT CONCAT(e.first_name, ' ', e.last_name) AS dgm_name,
                 (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance
          FROM sce_application.sce_user_app_sold s
          JOIN sce_employee.sce_emp e ON s.emp_id = e.emp_id
          WHERE s.is_active = 1
            AND s.entity_id = 3
            AND s.acdc_year_id = :yearId
          GROUP BY e.first_name, e.last_name
      """, nativeQuery = true)
    List<Object[]> findDgmPerformanceNativeByYear(@Param("yearId") Integer yearId);
    
    // 4. DGMS (Filtered by Category and Year)
    @Query(value = """
          SELECT CONCAT(e.first_name, ' ', e.last_name) AS dgm_name,
                 (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance
          FROM sce_application.sce_user_app_sold s
          JOIN sce_employee.sce_emp e ON s.emp_id = e.emp_id
          JOIN sce_admin.sce_emp_view ev ON s.emp_id = ev.emp_id
          WHERE s.is_active = 1
            AND s.entity_id = 3
            AND s.acdc_year_id = :yearId
            AND LOWER(ev.cmps_category) = LOWER(:category)
          GROUP BY e.first_name, e.last_name
      """, nativeQuery = true)
    List<Object[]> findDgmPerformanceNativeByCategoryAndYear(@Param("category") String category, @Param("yearId") Integer yearId);
    
    // 5. DGMS (Filtered by Zone and Year)
    @Query(value = """
            SELECT CONCAT(e.first_name, ' ', e.last_name) AS dgm_name,
                   (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance
            FROM sce_application.sce_user_app_sold s
            JOIN sce_employee.sce_emp e ON s.emp_id = e.emp_id
            WHERE s.is_active = 1
              AND s.entity_id = 3
              AND s.acdc_year_id = :yearId
              AND s.emp_id IN :dgmEmpIds
            GROUP BY e.first_name, e.last_name
        """, nativeQuery = true)
    List<Object[]> findDgmPerformanceForZoneAndYear(@Param("dgmEmpIds") List<Integer> dgmEmpIds, @Param("yearId") Integer yearId);
    
    // 6. CAMPUSES (Filtered by Year)
    @Query(value = "SELECT c.cmps_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s "
            + "JOIN sce_campus.sce_cmps c ON s.cmps_id = c.cmps_id "
            + "WHERE s.is_active = 1 "
            + "AND s.acdc_year_id = :yearId "
            + "GROUP BY c.cmps_name", nativeQuery = true)
    List<Object[]> findCampusPerformanceNativeByYear(@Param("yearId") Integer yearId);
    
    // 7. CAMPUSES (Filtered by Category and Year)
    @Query(value = "SELECT c.cmps_name, "
            + "(CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance "
            + "FROM sce_application.sce_user_app_sold s "
            + "JOIN sce_campus.sce_cmps c ON s.cmps_id = c.cmps_id "
            + "JOIN sce_admin.sce_emp_view e ON s.emp_id = e.emp_id "
            + "WHERE s.is_active = 1 "
            + "AND s.acdc_year_id = :yearId "
            + "AND LOWER(e.cmps_category) = LOWER(:category) "
            + "GROUP BY c.cmps_name", nativeQuery = true)
    List<Object[]> findCampusPerformanceNativeByCategoryAndYear(@Param("category") String category, @Param("yearId") Integer yearId);
    
    // 8. CAMPUSES (Filtered by DGM and Year)
    @Query(value = """
              SELECT c.cmps_name,
                  (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100 AS performance
              FROM sce_application.sce_user_app_sold s
              JOIN sce_campus.sce_cmps c ON s.cmps_id = c.cmps_id
              WHERE s.is_active = 1
                AND s.acdc_year_id = :yearId
                AND s.cmps_id IN :campusIds
              GROUP BY c.cmps_name
          """, nativeQuery = true)
    List<Object[]> findCampusPerformanceForDgmAndYear(@Param("campusIds") List<Integer> campusIds, @Param("yearId") Integer yearId);
   
    // --- Flexible Graph Data Methods (Year-wise with optional filters) ---
   
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId = 2 THEN u.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.zone.zoneId = :zoneId
              AND u.isActive = 1
              AND u.entityId IN (2, 4)
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZone(@Param("zoneId") Integer zoneId);
   
    // Single Campus (campusId): Get year-wise data with entity_id = 4 for total (issued)
    // Uses entity_id = 4 ONLY for both issued (totalAppCount) and sold
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(u.totalAppCount), 0),
                COALESCE(SUM(u.sold), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId = 4
              AND u.campus.campusId = :campusId
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampus(@Param("campusId") Integer campusId);
   
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(u.totalAppCount), 0),
                COALESCE(SUM(u.sold), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (2, 3, 4)
              AND u.amount = :amount
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByAmount(@Param("amount") Float amount);
   
    // NEW METHOD: Get year-wise data by matching series from Distribution to UserAppSold
    // For cards graph: Find distributions by created_by, is_active=1, amount
    // Match series (appStartNo-appEndNo) with UserAppSold (rangeStartNo-rangeEndNo)
    // Sum totalAppCount from UserAppSold for matching series
    // IMPORTANT: Ensure unique series - if same series appears in multiple entity_ids, count only once
    // Use native query to handle subquery with proper aliases
    @Query(value = """
            SELECT
                d.acdc_year_id,
                COALESCE(SUM(series_max.total_app_count), 0),
                COALESCE(SUM(series_max.sold_count), 0)
            FROM sce_application.sce_app_distrubution d
            INNER JOIN (
                SELECT 
                    u.acdc_year_id,
                    u.range_start_no,
                    u.range_end_no,
                    MAX(u.total_app_count) as total_app_count,
                    MAX(CASE WHEN u.entity_id = 4 THEN u.sold ELSE 0 END) as sold_count
                FROM sce_application.sce_user_app_sold u
                WHERE u.is_active = 1
                  AND u.entity_id IN (2, 3, 4)
                GROUP BY u.acdc_year_id, u.range_start_no, u.range_end_no
            ) series_max ON (
                series_max.range_start_no = d.app_start_no
                AND series_max.range_end_no = d.app_end_no
                AND series_max.acdc_year_id = d.acdc_year_id
            )
            WHERE d.created_by = :employeeId
              AND d.is_active = 1
              AND d.amount = :amount
            GROUP BY d.acdc_year_id
            ORDER BY d.acdc_year_id
        """, nativeQuery = true)
    List<Object[]> getYearWiseIssuedAndSoldByAmountAndEmployeeSeries(@Param("amount") Float amount, @Param("employeeId") Integer employeeId);
   
    // ZONAL ACCOUNTANT: Get year-wise data for distributions where Admin gave to DGM or Campus in that zone
    // Match series from Distribution to UserAppSold
    // IMPORTANT: Ensure unique series - if same series appears in multiple entity_ids, count only once
    @Query(value = """
            SELECT
                d.acdc_year_id,
                COALESCE(SUM(series_max.total_app_count), 0),
                COALESCE(SUM(series_max.sold_count), 0)
            FROM sce_application.sce_app_distrubution d
            INNER JOIN (
                SELECT 
                    u.acdc_year_id,
                    u.range_start_no,
                    u.range_end_no,
                    MAX(u.total_app_count) as total_app_count,
                    MAX(CASE WHEN u.entity_id = 4 THEN u.sold ELSE 0 END) as sold_count
                FROM sce_application.sce_user_app_sold u
                WHERE u.is_active = 1
                  AND u.entity_id IN (2, 3, 4)
                GROUP BY u.acdc_year_id, u.range_start_no, u.range_end_no
            ) series_max ON (
                series_max.range_start_no = d.app_start_no
                AND series_max.range_end_no = d.app_end_no
                AND series_max.acdc_year_id = d.acdc_year_id
            )
            WHERE d.issued_by_type_id = 1
              AND d.issued_to_type_id IN (3, 4)
              AND d.is_active = 1
              AND d.amount = :amount
              AND d.zone_id = :zoneId
            GROUP BY d.acdc_year_id
            ORDER BY d.acdc_year_id
        """, nativeQuery = true)
    List<Object[]> getYearWiseIssuedAndSoldByAmountAndZoneSeries(@Param("amount") Float amount, @Param("zoneId") Integer zoneId);
   
    // DGM: Get year-wise data for distributions where Admin gave to Campus under that DGM
    // Match series from Distribution to UserAppSold
    // IMPORTANT: Ensure unique series - if same series appears in multiple entity_ids, count only once
    @Query(value = """
            SELECT
                d.acdc_year_id,
                COALESCE(SUM(series_max.total_app_count), 0),
                COALESCE(SUM(series_max.sold_count), 0)
            FROM sce_application.sce_app_distrubution d
            INNER JOIN (
                SELECT 
                    u.acdc_year_id,
                    u.range_start_no,
                    u.range_end_no,
                    MAX(u.total_app_count) as total_app_count,
                    MAX(CASE WHEN u.entity_id = 4 THEN u.sold ELSE 0 END) as sold_count
                FROM sce_application.sce_user_app_sold u
                WHERE u.is_active = 1
                  AND u.entity_id IN (2, 3, 4)
                GROUP BY u.acdc_year_id, u.range_start_no, u.range_end_no
            ) series_max ON (
                series_max.range_start_no = d.app_start_no
                AND series_max.range_end_no = d.app_end_no
                AND series_max.acdc_year_id = d.acdc_year_id
            )
            WHERE d.issued_by_type_id = 1
              AND d.issued_to_type_id = 4
              AND d.is_active = 1
              AND d.amount = :amount
              AND d.cmps_id IN :campusIds
            GROUP BY d.acdc_year_id
            ORDER BY d.acdc_year_id
        """, nativeQuery = true)
    List<Object[]> getYearWiseIssuedAndSoldByAmountAndDgmCampusSeries(@Param("amount") Float amount, @Param("campusIds") List<Integer> campusIds);
   
    // PRO/PRINCIPAL/VICE PRINCIPAL: Get year-wise data for distributions to that campus
    // Match series from Distribution to UserAppSold
    // IMPORTANT: Ensure unique series - if same series appears in multiple entity_ids, count only once
    @Query(value = """
            SELECT
                d.acdc_year_id,
                COALESCE(SUM(series_max.total_app_count), 0),
                COALESCE(SUM(series_max.sold_count), 0)
            FROM sce_application.sce_app_distrubution d
            INNER JOIN (
                SELECT 
                    u.acdc_year_id,
                    u.range_start_no,
                    u.range_end_no,
                    MAX(u.total_app_count) as total_app_count,
                    MAX(CASE WHEN u.entity_id = 4 THEN u.sold ELSE 0 END) as sold_count
                FROM sce_application.sce_user_app_sold u
                WHERE u.is_active = 1
                  AND u.entity_id IN (2, 3, 4)
                GROUP BY u.acdc_year_id, u.range_start_no, u.range_end_no
            ) series_max ON (
                series_max.range_start_no = d.app_start_no
                AND series_max.range_end_no = d.app_end_no
                AND series_max.acdc_year_id = d.acdc_year_id
            )
            WHERE d.is_active = 1
              AND d.amount = :amount
              AND d.cmps_id = :campusId
            GROUP BY d.acdc_year_id
            ORDER BY d.acdc_year_id
        """, nativeQuery = true)
    List<Object[]> getYearWiseIssuedAndSoldByAmountAndCampusSeries(@Param("amount") Float amount, @Param("campusId") Integer campusId);
   
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId IN (2, 3) THEN u.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.zone.zoneId = :zoneId
              AND u.amount = :amount
              AND u.isActive = 1
              AND u.entityId IN (2, 3, 4)
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneAndAmount(@Param("zoneId") Integer zoneId, @Param("amount") Float amount);
    
    // ZONAL ACCOUNTANT: Get year-wise data directly from UserAppSold for zone with amount filter
    // Uses distinct series counting to avoid double counting when same series appears in multiple entity_ids
    // For zone: entity_id = 2 ONLY for issued (Zone level), entity_id = 4 for sold
    // Admin→DGM and Admin→Campus distributions are added separately in the service layer
    @Query(value = """
            SELECT
                series_data.acdc_year_id,
                COALESCE(SUM(series_data.total_app_count), 0),
                COALESCE(SUM(series_data.sold_count), 0)
            FROM (
                SELECT 
                    u.acdc_year_id,
                    u.range_start_no,
                    u.range_end_no,
                    MAX(CASE WHEN u.entity_id = 2 THEN u.total_app_count ELSE 0 END) as total_app_count,
                    MAX(CASE WHEN u.entity_id = 4 THEN u.sold ELSE 0 END) as sold_count
                FROM sce_application.sce_user_app_sold u
                WHERE u.is_active = 1
                  AND u.entity_id IN (2, 4)
                  AND u.zone_id = :zoneId
                  AND u.amount = :amount
                GROUP BY u.acdc_year_id, u.range_start_no, u.range_end_no
            ) series_data
            GROUP BY series_data.acdc_year_id
            ORDER BY series_data.acdc_year_id
            """, nativeQuery = true)
    List<Object[]> getYearWiseIssuedAndSoldByZoneAndAmountWithDistinct(@Param("zoneId") Integer zoneId, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(u.totalAppCount), 0),
                COALESCE(SUM(u.sold), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId = 4
              AND u.campus.campusId = :campusId
              AND u.amount = :amount
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusAndAmount(@Param("campusId") Integer campusId, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(u.totalAppCount), 0),
                COALESCE(SUM(u.sold), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId = 4
              AND u.zone.zoneId = :zoneId
              AND u.campus.campusId = :campusId
              AND u.amount = :amount
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusAndAmount(@Param("zoneId") Integer zoneId, @Param("campusId") Integer campusId, @Param("amount") Float amount);
    
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(u.totalAppCount), 0),
                COALESCE(SUM(u.sold), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId = 4
              AND u.zone.zoneId = :zoneId
              AND u.campus.campusId = :campusId
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampus(@Param("zoneId") Integer zoneId, @Param("campusId") Integer campusId);
   
    // --- NEW: Methods for multiple campus IDs ---
   
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId IN (1, 3) THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (1, 3, 4)
              AND u.campus.campusId IN :campusIds
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusList(@Param("campusIds") List<Integer> campusIds);

    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId IN (1, 3) THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (1, 3, 4)
              AND u.zone.zoneId = :zoneId
              AND u.campus.campusId IN :campusIds
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusList(@Param("zoneId") Integer zoneId, @Param("campusIds") List<Integer> campusIds);
    
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId IN (1, 3) THEN u.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (1, 3, 4)
              AND u.campus.campusId IN :campusIds
              AND u.amount = :amount
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusListAndAmount(@Param("campusIds") List<Integer> campusIds, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(CASE WHEN a.entityId IN (1, 3) THEN a.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN a.entityId = 4 THEN a.sold ELSE 0 END), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId IN (1, 3, 4)
              AND a.zone.zoneId = :zoneId
              AND a.campus.campusId IN :campusIds
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusListAndAmount(@Param("zoneId") Integer zoneId, @Param("campusIds") List<Integer> campusIds, @Param("amount") Float amount);
    
    // ========== NEW METHODS FOR CAMPUSIDS - SOLD WITH ENTITY_ID = 4 ==========
    // These methods keep issued calculation the same (entityId IN (1, 3))
    // But change sold calculation to use entity_id = 4 based on campusId
    // Existing methods (with entity_id = 3) remain unchanged
    
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId = 3 THEN u.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (3, 4)
              AND u.campus.campusId IN :campusIds
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusListWithEntity4(@Param("campusIds") List<Integer> campusIds);
    
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId IN (1, 3) THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (1, 3, 4)
              AND u.zone.zoneId = :zoneId
              AND u.campus.campusId IN :campusIds
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusListWithEntity4(@Param("zoneId") Integer zoneId, @Param("campusIds") List<Integer> campusIds);
    
    // DGM Rollup (campusIds): Get year-wise data with amount filter
    // Uses entity_id = 3 ONLY for issued (DGM level), entity_id = 4 for sold
    // Admin→Campus and Zone→Campus distributions are added separately in the service layer
    @Query("""
            SELECT
                u.acdcYearId,
                COALESCE(SUM(CASE WHEN u.entityId = 3 THEN u.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
            FROM UserAppSold u
            WHERE u.isActive = 1
              AND u.entityId IN (3, 4)
              AND u.campus.campusId IN :campusIds
              AND u.amount = :amount
            GROUP BY u.acdcYearId
            ORDER BY u.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusListAndAmountWithEntity4(@Param("campusIds") List<Integer> campusIds, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(CASE WHEN a.entityId IN (1, 3) THEN a.totalAppCount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN a.entityId = 4 THEN a.sold ELSE 0 END), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId IN (1, 3, 4)
              AND a.zone.zoneId = :zoneId
              AND a.campus.campusId IN :campusIds
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusListAndAmountWithEntity4(@Param("zoneId") Integer zoneId, @Param("campusIds") List<Integer> campusIds, @Param("amount") Float amount);
    
    // IMPORTANT: Ensure unique series - if same series appears in multiple entity_ids, count only once
    // For each unique series (rangeStartNo, rangeEndNo), take MAX totalAppCount to avoid double-counting
    // Returns: [totalAppCount, soldCount] as Object array
    @Query(value = """
            SELECT 
                COALESCE(SUM(series_max.total_app_count), 0),
                COALESCE(SUM(series_max.sold_count), 0)
            FROM (
                SELECT 
                    u.range_start_no,
                    u.range_end_no,
                    MAX(CASE WHEN u.entity_id = 3 THEN u.total_app_count ELSE 0 END) as total_app_count,
                    MAX(CASE WHEN u.entity_id = 4 THEN u.sold ELSE 0 END) as sold_count
                FROM sce_application.sce_user_app_sold u
                WHERE u.is_active = 1
                  AND u.entity_id IN (3, 4)
                  AND u.cmps_id IN :campusIds
                  AND u.acdc_year_id = :acdcYearId
                GROUP BY u.range_start_no, u.range_end_no
            ) series_max
        """, nativeQuery = true)
    List<Object[]> getSalesSummaryByCampusListWithEntity4Raw(
            @Param("campusIds") List<Integer> campusIds,
            @Param("acdcYearId") Integer acdcYearId
    );
   
    @Query("SELECT u.acdcYearId, SUM(u.sold), SUM(u.amount) " +
            "FROM UserAppSold u WHERE u.campus.id = :campusId GROUP BY u.acdcYearId")
     List<Object[]> getSalesSummaryByCampusId(@Param("campusId") Integer campusId);
 
     // --- FOR ZONAL ACCOUNTANT (Direct Zone ID) ---
     @Query("SELECT u.acdcYearId, SUM(u.sold), SUM(u.amount) " +
            "FROM UserAppSold u WHERE u.entityId = 2 AND u.zone.id = :zoneId GROUP BY u.acdcYearId")
     List<Object[]> getSalesSummaryByZoneId(@Param("zoneId") Integer zoneId);
   
     @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
              "COALESCE(SUM(u.totalAppCount), 0), " +
              "COALESCE(SUM(u.sold), 0)) " +
              "FROM UserAppSold u " +
              "WHERE u.campus.id = :campusId " +
              "AND u.acdcYearId = :yearId " +
              "AND u.isActive = 1 " +
              "AND u.entityId = 4")
      Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusIdAndYear(
              @Param("campusId") Integer campusId,
              @Param("yearId") Integer yearId
      );
 
       @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.campus.id = :campusId AND u.entityId = 4 AND u.isActive = 1")
      List<Integer> findDistinctYearIdsByCampusId(@Param("campusId") Integer campusId);
     
      // Fixed: Return Optional<Long> to match Service expectation
      @Query("SELECT COALESCE(SUM(u.sold), 0) FROM UserAppSold u WHERE u.campus.id = :campusId AND u.acdcYearId = :yearId")
      Optional<Long> getProMetricByCampusId(
              @Param("campusId") Integer campusId,
              @Param("yearId") Integer yearId
      );
 
      @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
               "COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.totalAppCount ELSE 0 END), 0), " +
               "COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)) " +
               "FROM UserAppSold u " +
               "WHERE u.zone.id = :zoneId AND u.acdcYearId = :yearId AND u.isActive = 1 AND u.entityId IN (2, 3, 4)")
      Optional<GraphSoldSummaryDTO> getSalesSummaryByZoneIdAndYear(
              @Param("zoneId") Integer zoneId,
              @Param("yearId") Integer yearId
      );
 
       @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.entityId IN (2, 4) AND u.zone.id = :zoneId AND u.isActive = 1")
      List<Integer> findDistinctYearIdsByZoneId(@Param("zoneId") Integer zoneId);
     
      // Fixed: Return Optional<Long>
       @Query("SELECT COALESCE(SUM(u.sold), 0) FROM UserAppSold u WHERE u.entityId = 4 AND u.zone.id = :zoneId AND u.acdcYearId = :yearId")
      Optional<Long> getProMetricByZoneId(
              @Param("zoneId") Integer zoneId,
              @Param("yearId") Integer yearId
      );
     
      @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
               "COALESCE(SUM(CASE WHEN u.entityId = 2 THEN u.totalAppCount ELSE 0 END), 0), " +
               "COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)) " +
              "FROM UserAppSold u " +
               "WHERE u.zone.id = :zoneId " +
               "AND u.acdcYearId = :yearId " +
               "AND u.isActive = 1 " +
               "AND u.entityId IN (2, 4)")
       Optional<GraphSoldSummaryDTO> getSalesSummaryByZoneId(
               @Param("zoneId") Integer zoneId,
               @Param("yearId") Integer yearId
       );
     
   // In UserAppSoldRepository.java
 
      @Query("""
          SELECT NEW com.application.dto.GraphSoldSummaryDTO(
              COALESCE(SUM(uas.totalAppCount), 0),
              COALESCE(SUM(uas.sold), 0))
          FROM UserAppSold uas
          WHERE uas.empId = :empId
            AND uas.acdcYearId = :acdcYearId
          """)
      Optional<GraphSoldSummaryDTO> getSalesSummaryByEmployee(@Param("empId") Integer empId,
                                                              @Param("acdcYearId") Integer acdcYearId);
     
   // In UserAppSoldRepository.java
 
      @Query("SELECT DISTINCT uas.acdcYearId FROM UserAppSold uas WHERE uas.empId = :empId")
      List<Integer> findDistinctYearIdsByEmployee(@Param("empId") Integer empId);
     
      @Query(value = """
            SELECT CONCAT(e.first_name, ' ', e.last_name) AS dgm_name,
                   (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100.0 AS performance
            FROM sce_application.sce_user_app_sold s
            JOIN sce_employee.sce_emp e ON s.emp_id = e.emp_id
            WHERE s.is_active = 1
              AND s.entity_id = 3
              AND s.emp_id IN :dgmEmpIds
            GROUP BY e.first_name, e.last_name
        """, nativeQuery = true)
        List<Object[]> findDgmPerformanceForZone(@Param("dgmEmpIds") List<Integer> dgmEmpIds);
 
        @Query(value = """
              SELECT c.cmps_name,
                  (CAST(SUM(s.sold) AS DECIMAL) / NULLIF(SUM(s.total_app_count), 0)) * 100 AS performance
              FROM sce_application.sce_user_app_sold s
              JOIN sce_campus.sce_cmps c ON s.cmps_id = c.cmps_id
              WHERE s.is_active = 1
                AND s.cmps_id IN :campusIds
              GROUP BY c.cmps_name
          """, nativeQuery = true)
          List<Object[]> findCampusPerformanceForDgm(@Param("campusIds") List<Integer> campusIds);
         
           @Query("""
           SELECT new com.application.dto.GraphSoldSummaryDTO(
               COALESCE(SUM(CASE WHEN d.appDistributionId IS NOT NULL THEN (u.totalAppCount - COALESCE(u.appAvlbCount, 0)) ELSE 0 END), 0),
               COALESCE(SUM(CASE WHEN u.entityId = 4 THEN u.sold ELSE 0 END), 0)
           )
           FROM UserAppSold u
           LEFT JOIN Distribution d ON (
               d.campus.campusId IN :campusIds
               AND d.issuedByType.appIssuedId IN (1, 2, 3)
               AND d.isActive = 1
               AND u.entityId = d.issuedToType.appIssuedId
               AND u.rangeStartNo >= d.appStartNo
               AND u.rangeEndNo <= d.appEndNo
               AND u.acdcYearId = d.academicYear.acdcYearId
           )
           WHERE u.isActive = 1
             AND u.entityId IN (2, 3, 4)
             AND u.campus.campusId IN :campusIds
             AND u.acdcYearId = :yearId
           """)
            Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusIdsAndYear(
                    @Param("campusIds") List<Integer> campusIds,
                    @Param("yearId") Integer yearId
            );
 
             @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.entityId IN (1, 3, 4) AND u.campus.id IN :campusIds AND u.isActive = 1")
            List<Integer> findDistinctYearIdsByCampusIds(@Param("campusIds") List<Integer> campusIds);
 
             @Query("SELECT DISTINCT u.empId FROM UserAppSold u WHERE u.zone.zoneId = :zoneId AND u.entityId = 3")
             List<Integer> findDgmIdsByZoneId(@Param("zoneId") Integer zoneId);
 
             // 2. Get all Campus IDs managed by a specific DGM
             // Assuming Entity 4 = PRO/Campus level sales tied to that DGM
             @Query("SELECT DISTINCT u.campus.campusId FROM UserAppSold u WHERE u.empId = :dgmId")
             List<Integer> findCampusIdsByDgmId(@Param("dgmId") Integer dgmId);
}
 