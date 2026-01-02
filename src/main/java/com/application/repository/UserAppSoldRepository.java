package com.application.repository;
 
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import com.application.dto.GraphSoldSummaryDTO;
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
                    COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                    SUM(a.sold)
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
    @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(COALESCE(SUM(uas.totalAppCount), 0), COALESCE(SUM(uas.sold), 0)) FROM UserAppSold uas WHERE uas.entityId = 3 AND uas.campus.id IN :campusIds AND uas.acdcYearId = :acdcYearId")
    Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusList(@Param("campusIds") List<Integer> campusIds,
            @Param("acdcYearId") Integer acdcYearId);
 
    // --- NEW: Method for a LIST of campuses (DGM-Rollup) ---
    @Query("SELECT COALESCE(SUM(uas.totalAppCount), 0) FROM UserAppSold uas WHERE uas.entityId = 3 AND uas.campus.id IN :campusIds AND uas.acdcYearId = :acdcYearId")
    Optional<Long> getProMetricByCampusList(@Param("campusIds") List<Integer> campusIds,
            @Param("acdcYearId") Integer acdcYearId);
 
    // --- Methods to find distinct years for GRAPH ---
 
    @Query("SELECT DISTINCT uas.acdcYearId FROM UserAppSold uas WHERE uas.entityId = 2 AND uas.zone.id = :zoneId")
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
 
    @Query("SELECT new com.application.dto.GraphSoldSummaryDTO(SUM(u.totalAppCount), SUM(u.sold)) "
            + "FROM UserAppSold u " + "WHERE u.zone.zoneId = :zoneId AND u.acdcYearId = :yearId AND u.amount = :amount")
    Optional<GraphSoldSummaryDTO> getSalesSummaryByZoneAndAmount(@Param("zoneId") Integer zoneId,
            @Param("yearId") Integer yearId, @Param("amount") Float amount);
 
    @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.zone.zoneId = :zoneId AND u.amount = :amount")
    List<Integer> findDistinctYearIdsByZoneAndAmount(@Param("zoneId") Integer zoneId, @Param("amount") Float amount);
 
    @Query("SELECT new com.application.dto.GraphSoldSummaryDTO(SUM(u.totalAppCount), SUM(u.sold)) "
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
            + "FROM sce_application.sce_user_app_sold s " + "JOIN sce_locations.sce_zone z ON s.zone_id = z.zone_id "
            + "WHERE s.is_active = 1 " + "GROUP BY z.zone_name", nativeQuery = true)
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
   
    // --- Flexible Graph Data Methods (Year-wise with optional filters) ---
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 2
              AND a.zone.zoneId = :zoneId
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZone(@Param("zoneId") Integer zoneId);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 4
              AND a.campus.campusId = :campusId
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampus(@Param("campusId") Integer campusId);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId IN (2, 3, 4)
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByAmount(@Param("amount") Float amount);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 2
              AND a.zone.zoneId = :zoneId
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneAndAmount(@Param("zoneId") Integer zoneId, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 4
              AND a.campus.campusId = :campusId
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusAndAmount(@Param("campusId") Integer campusId, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 4
              AND a.zone.zoneId = :zoneId
              AND a.campus.campusId = :campusId
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusAndAmount(@Param("zoneId") Integer zoneId, @Param("campusId") Integer campusId, @Param("amount") Float amount);
   
    // --- NEW: Methods for multiple campus IDs ---
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 3
              AND a.campus.campusId IN :campusIds
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusList(@Param("campusIds") List<Integer> campusIds);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 3
              AND a.campus.campusId IN :campusIds
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByCampusListAndAmount(@Param("campusIds") List<Integer> campusIds, @Param("amount") Float amount);
   
    @Query("""
            SELECT
                a.acdcYearId,
                COALESCE(SUM(a.totalAppCount - a.appAvlbCount), 0),
                COALESCE(SUM(a.sold), 0)
            FROM UserAppSold a
            WHERE a.isActive = 1
              AND a.entityId = 3
              AND a.zone.zoneId = :zoneId
              AND a.campus.campusId IN :campusIds
              AND a.amount = :amount
            GROUP BY a.acdcYearId
            ORDER BY a.acdcYearId
        """)
    List<Object[]> getYearWiseIssuedAndSoldByZoneCampusListAndAmount(@Param("zoneId") Integer zoneId, @Param("campusIds") List<Integer> campusIds, @Param("amount") Float amount);
   
    @Query("SELECT u.acdcYearId, SUM(u.sold), SUM(u.amount) " +
            "FROM UserAppSold u WHERE u.campus.id = :campusId GROUP BY u.acdcYearId")
     List<Object[]> getSalesSummaryByCampusId(@Param("campusId") Integer campusId);
 
     // --- FOR ZONAL ACCOUNTANT (Direct Zone ID) ---
     @Query("SELECT u.acdcYearId, SUM(u.sold), SUM(u.amount) " +
            "FROM UserAppSold u WHERE u.entityId = 2 AND u.zone.id = :zoneId GROUP BY u.acdcYearId")
     List<Object[]> getSalesSummaryByZoneId(@Param("zoneId") Integer zoneId);
   
     @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
             "COALESCE(SUM(u.totalAppCount), 0), COALESCE(SUM(u.sold), 0)) " +
             "FROM UserAppSold u WHERE u.campus.id = :campusId AND u.acdcYearId = :yearId")
      Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusIdAndYear(
              @Param("campusId") Integer campusId,
              @Param("yearId") Integer yearId
      );
 
      @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.campus.id = :campusId")
      List<Integer> findDistinctYearIdsByCampusId(@Param("campusId") Integer campusId);
     
      // Fixed: Return Optional<Long> to match Service expectation
      @Query("SELECT COALESCE(SUM(u.sold), 0) FROM UserAppSold u WHERE u.campus.id = :campusId AND u.acdcYearId = :yearId")
      Optional<Long> getProMetricByCampusId(
              @Param("campusId") Integer campusId,
              @Param("yearId") Integer yearId
      );
 
      @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
             "COALESCE(SUM(u.totalAppCount), 0), COALESCE(SUM(u.sold), 0)) " +
             "FROM UserAppSold u WHERE u.entityId = 2 AND u.zone.id = :zoneId AND u.acdcYearId = :yearId AND u.isActive = 1")
      Optional<GraphSoldSummaryDTO> getSalesSummaryByZoneIdAndYear(
              @Param("zoneId") Integer zoneId,
              @Param("yearId") Integer yearId
      );
 
      @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.entityId = 2 AND u.zone.id = :zoneId")
      List<Integer> findDistinctYearIdsByZoneId(@Param("zoneId") Integer zoneId);
     
      // Fixed: Return Optional<Long>
      @Query("SELECT COALESCE(SUM(u.sold), 0) FROM UserAppSold u WHERE u.entityId = 2 AND u.zone.id = :zoneId AND u.acdcYearId = :yearId")
      Optional<Long> getProMetricByZoneId(
              @Param("zoneId") Integer zoneId,
              @Param("yearId") Integer yearId
      );
     
      @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
              "COALESCE(SUM(u.totalAppCount), 0), " +
              "COALESCE(SUM(u.sold), 0)) " +
              "FROM UserAppSold u " +
              "WHERE u.entityId = 2 AND u.zone.id = :zoneId AND u.acdcYearId = :yearId")
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
         
          @Query("SELECT NEW com.application.dto.GraphSoldSummaryDTO(" +
                   "COALESCE(SUM(u.totalAppCount), 0), COALESCE(SUM(u.sold), 0)) " +
                   "FROM UserAppSold u WHERE u.entityId = 3 AND u.campus.id IN :campusIds AND u.acdcYearId = :yearId AND u.isActive = 1")
            Optional<GraphSoldSummaryDTO> getSalesSummaryByCampusIdsAndYear(
                    @Param("campusIds") List<Integer> campusIds,
                    @Param("yearId") Integer yearId
            );
 
            @Query("SELECT DISTINCT u.acdcYearId FROM UserAppSold u WHERE u.entityId = 3 AND u.campus.id IN :campusIds")
            List<Integer> findDistinctYearIdsByCampusIds(@Param("campusIds") List<Integer> campusIds);
 
 
}
 