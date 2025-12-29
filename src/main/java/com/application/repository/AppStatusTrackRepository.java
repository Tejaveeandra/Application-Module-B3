package com.application.repository;
 
import java.util.List;
 
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import org.springframework.data.jpa.repository.Query;
 
import org.springframework.data.repository.query.Param;
 
import org.springframework.stereotype.Repository;
 
import com.application.dto.AppStatusTrackDTO;
 
import com.application.dto.MetricsAggregateDTO;
 
import com.application.entity.AppStatusTrack;
 
@Repository
 
public interface AppStatusTrackRepository extends JpaRepository<AppStatusTrack, Integer> {
 
    @Query("SELECT new com.application.dto.AppStatusTrackDTO(" +
 
            "SUM(t.totalApp), SUM(t.appSold), SUM(t.appConfirmed), " +
 
            "SUM(t.appAvailable), SUM(t.appIssued), SUM(t.appDamaged), " +
 
            "SUM(t.appUnavailable)) " +
 
            "FROM AppStatusTrack t WHERE t.isActive = 1")
 
    Optional<AppStatusTrackDTO> findLatestAggregatedStats();
 
    @Query("SELECT MAX(a.academicYear.acdcYearId) FROM AppStatusTrack a")
 
    Integer findLatestYearId();
 
    @Query("""
 
                SELECT
 
                    SUM(a.totalApp),
 
                    SUM(a.appSold),
 
                    SUM(a.appConfirmed),
 
                    SUM(a.appAvailable),
 
                    SUM(a.appIssued),
 
                    SUM(a.appDamaged),
 
                    SUM(a.appUnavailable)
 
                FROM AppStatusTrack a
 
                WHERE a.isActive = 1
 
            """)
 
    List<Object[]> getOverallTotals();
 
    @Query("""
 
                SELECT SUM(a.totalApp)
 
                FROM AppStatusTrack a
 
                WHERE a.isActive = 1
 
                  AND a.issuedByType.appIssuedId = 4
 
            """)
 
    Long getOverallWithPro();
 
    @Query("""
 
                SELECT
 
                    SUM(a.totalApp),
 
                    SUM(a.appSold),
 
                    SUM(a.appConfirmed),
 
                    SUM(a.appAvailable),
 
                    SUM(a.appIssued),
 
                    SUM(a.appDamaged),
 
                    SUM(a.appUnavailable)
 
                FROM AppStatusTrack a
 
                WHERE a.isActive = 1
 
                  AND a.academicYear.acdcYearId = :yearId
 
            """)
 
    List<Object[]> getTotalsByYear(Integer yearId);
 
    @Query("""
 
                SELECT SUM(a.totalApp)
 
                FROM AppStatusTrack a
 
                WHERE a.isActive = 1
 
                  AND a.issuedByType.appIssuedId = 4
 
                  AND a.academicYear.acdcYearId = :yearId
 
            """)
 
    Long getWithProByYear(Integer yearId);
 
    public record MetricsAggregate(long totalApp, long appSold, long appConfirmed, long appAvailable,
 
            long appUnavailable, long appDamaged, long appIssued) {
 
    }
 
    /**
 
     * Aggregates all metric counts for a specific Zone and Academic Year.
 
     */
 
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" +
            "COALESCE(SUM(ast.totalApp), 0), " +
            "COALESCE(SUM(ast.appSold), 0), " +
            "COALESCE(SUM(ast.appConfirmed), 0), " +
            "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), " +
            "COALESCE(SUM(ast.appUnavailable), 0), " +
            "COALESCE(SUM(ast.appDamaged), 0), " +
            "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) " +
            "FROM AppStatusTrack ast " +
            "LEFT JOIN ast.issuedByType ibt " +
            "WHERE ast.zone.id = :zoneId AND ast.academicYear.acdcYearId = :acdcYearId AND ast.isActive = 1")
 
    Optional<MetricsAggregateDTO> getMetricsByZoneAndYear( // <-- Use new DTO
 
            @Param("zoneId") Long zoneId, @Param("acdcYearId") Integer acdcYearId);
   
    @Query("""
            SELECT COALESCE(SUM(ast.appIssued), 0)
            FROM AppStatusTrack ast
            WHERE ast.zone.id = :zoneId
            AND ast.academicYear.acdcYearId = :acdcYearId
            AND ast.issuedByType.appIssuedId = 4
            """)
            Long getWithProIssuedByZone(
                    @Param("zoneId") Long zoneId,
                    @Param("acdcYearId") Integer acdcYearId
            );
 
 
    /**
 
     * Aggregates all metric counts for a specific DGM (Employee) and Academic Year.
 
     */
 
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO("
            + "COALESCE(SUM(ast.totalApp), 0), "
            + "COALESCE(SUM(ast.appSold), 0), "
            + "COALESCE(SUM(ast.appConfirmed), 0), "
            + "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), "
            + "COALESCE(SUM(ast.appUnavailable), 0), "
            + "COALESCE(SUM(ast.appDamaged), 0), "
            + "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) FROM AppStatusTrack ast "
            + "LEFT JOIN ast.issuedByType ibt "
            + "WHERE ast.employee.id = :empId AND ast.academicYear.acdcYearId = :acdcYearId")
 
    Optional<MetricsAggregateDTO> getMetricsByEmployeeAndYear(@Param("empId") Integer empId,
 
            @Param("acdcYearId") Integer acdcYearId);
   
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO("
 
            + "COALESCE(SUM(ast.totalApp), 0), COALESCE(SUM(ast.appSold), 0), COALESCE(SUM(ast.appConfirmed), 0), "
 
            + "COALESCE(SUM(ast.appAvailable), 0), COALESCE(SUM(ast.appUnavailable), 0), "
 
            + "COALESCE(SUM(ast.appDamaged), 0), COALESCE(SUM(ast.appIssued), 0)) " + "FROM AppStatusTrack ast "
 
            + "WHERE ast.academicYear.acdcYearId = :acdcYearId")
 
    Optional<MetricsAggregateDTO> getMetricsByYear(
 
            @Param("acdcYearId") Integer acdcYearId);
 
    /**
 
     * Aggregates all metric counts for a specific Campus and Academic Year.
 
     */
 
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" + // <-- Use new DTO
            "COALESCE(SUM(ast.totalApp), 0), " +
            "COALESCE(SUM(ast.appSold), 0), " +
            "COALESCE(SUM(ast.appConfirmed), 0), " +
            "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), " +
            "COALESCE(SUM(ast.appUnavailable), 0), " +
            "COALESCE(SUM(ast.appDamaged), 0), " +
            "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) FROM AppStatusTrack ast "
            + "LEFT JOIN ast.issuedByType ibt "
            + "WHERE ast.campus.id = :campusId AND ast.academicYear.acdcYearId = :acdcYearId")
 
    Optional<MetricsAggregateDTO> getMetricsByCampusAndYear( // <-- Use new DTO
 
            @Param("campusId") Long campusId, @Param("acdcYearId") Integer acdcYearId);
 
    @Query("SELECT DISTINCT ast.academicYear.acdcYearId FROM AppStatusTrack ast WHERE ast.zone.id = :zoneId")
 
    List<Integer> findDistinctYearIdsByZone(@Param("zoneId") Long zoneId);
 
    @Query("SELECT DISTINCT ast.academicYear.acdcYearId FROM AppStatusTrack ast WHERE ast.employee.id = :empId")
 
    List<Integer> findDistinctYearIdsByEmployee(@Param("empId") Integer empId);
 
    @Query("SELECT DISTINCT ast.academicYear.acdcYearId FROM AppStatusTrack ast WHERE ast.campus.id = :campusId")
 
    List<Integer> findDistinctYearIdsByCampus(@Param("campusId") Long campusId);
 
    // --- NEW: Method for a LIST of campuses (DGM-Rollup) ---
 
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" +
            "COALESCE(SUM(ast.totalApp), 0), " +
            "COALESCE(SUM(ast.appSold), 0), " +
            "COALESCE(SUM(ast.appConfirmed), 0), " +
            "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), " +
            "COALESCE(SUM(ast.appUnavailable), 0), " +
            "COALESCE(SUM(ast.appDamaged), 0), " +
            "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) FROM AppStatusTrack ast " +
            "LEFT JOIN ast.issuedByType ibt " +
            "WHERE ast.campus.id IN :campusIds AND ast.academicYear.acdcYearId = :acdcYearId")
 
    Optional<MetricsAggregateDTO> getMetricsByCampusListAndYear(@Param("campusIds") List<Integer> campusIds,
 
            @Param("acdcYearId") Integer acdcYearId);
 
    // --- NEW: Method for a LIST of campuses (DGM-Rollup) ---
 
    @Query("SELECT DISTINCT ast.academicYear.acdcYearId FROM AppStatusTrack ast WHERE ast.campus.id IN :campusIds")
 
    List<Integer> findDistinctYearIdsByCampusList(@Param("campusIds") List<Integer> campusIds);
 
    // --- NEW: Employee List query for Zonal Rollup (Metrics) ---
 
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" +
            "COALESCE(SUM(ast.totalApp), 0), " +
            "COALESCE(SUM(ast.appSold), 0), " +
            "COALESCE(SUM(ast.appConfirmed), 0), " +
            "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), " +
            "COALESCE(SUM(ast.appUnavailable), 0), " +
            "COALESCE(SUM(ast.appDamaged), 0), " +
            "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) FROM AppStatusTrack ast " +
            "LEFT JOIN ast.issuedByType ibt " +
            "WHERE ast.employee.id IN :empIds AND ast.academicYear.acdcYearId = :acdcYearId")
 
    Optional<MetricsAggregateDTO> getMetricsByEmployeeListAndYear(@Param("empIds") List<Integer> empIds,
 
            @Param("acdcYearId") Integer acdcYearId);
 
    // --- NEW: Employee List query for Zonal Rollup (Years) ---
 
    @Query("SELECT DISTINCT ast.academicYear.acdcYearId FROM AppStatusTrack ast WHERE ast.employee.id IN :empIds")
 
    List<Integer> findDistinctYearIdsByEmployeeList(@Param("empIds") List<Integer> empIds);
 
    @Query("""
 
            SELECT COALESCE(SUM(a.appAvailable), 0)
 
            FROM AppStatusTrack a
 
            WHERE a.isActive = 1
 
              AND a.issuedByType.appIssuedId = 4
 
              AND a.employee.id = :empId
 
              AND a.academicYear.acdcYearId = :academicYearId
 
        """)
 
        Long getWithProAvailableByEmployeeAndYear(
 
            @Param("empId") Integer empId,
 
            @Param("academicYearId") Integer academicYearId
 
        );
   
    @Query("""
 
            SELECT COALESCE(SUM(a.appAvailable), 0)
 
            FROM AppStatusTrack a
 
            WHERE a.isActive = 1
 
              AND a.issuedByType.appIssuedId = 4
 
              AND a.academicYear.acdcYearId = :academicYearId
 
        """)
 
        Long getWithProAvailableByYear(
 
            @Param("academicYearId") Integer academicYearId
 
        );
   
    // New Method in AppStatusTrackRepository
    @Query("SELECT NEW com.application.dto.MetricsAggregateDTO("
        + "COALESCE(SUM(ast.totalApp), 0), "
        + "COALESCE(SUM(ast.appSold), 0), "
        + "COALESCE(SUM(ast.appConfirmed), 0), "
        + "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), "
        + "COALESCE(SUM(ast.appUnavailable), 0), "
        + "COALESCE(SUM(ast.appDamaged), 0), "
        + "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) "
        + "FROM AppStatusTrack ast "
        + "LEFT JOIN ast.issuedByType ibt") // <--- No WHERE clause
    Optional<MetricsAggregateDTO> getAdminMetricsAllTime();
   
    // New Method in AppStatusTrackRepository
    @Query("SELECT COALESCE(SUM(ast.appAvailable), 0) "
        + "FROM AppStatusTrack ast "
        + "WHERE ast.issuedByType.appIssuedId = 4") // <--- Only filtering by Issued Type
    Long getWithProAvailableAllTime();      
       
        @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" +
                 "COALESCE(SUM(a.totalApp), 0), " +
                 "COALESCE(SUM(a.appSold), 0), " +
                 "COALESCE(SUM(a.appConfirmed), 0), " +
                 "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN a.appAvailable ELSE 0 END), 0), " +
                 "COALESCE(SUM(a.appUnavailable), 0), " +
                 "COALESCE(SUM(a.appDamaged), 0), " +
                 "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN a.totalApp ELSE 0 END), 0)) " +
                 "FROM AppStatusTrack a " +
                 "LEFT JOIN a.issuedByType ibt " +
                 "WHERE a.campus.id = :campusId AND a.academicYear.id = :yearId")
         Optional<MetricsAggregateDTO> getMetricsByCampusIdAndYear(
                 @Param("campusId") Integer campusId,
                 @Param("yearId") Integer yearId
         );
 
         @Query("SELECT DISTINCT a.academicYear.id FROM AppStatusTrack a WHERE a.campus.id = :campusId")
         List<Integer> findDistinctYearIdsByCampusId(@Param("campusId") Integer campusId);
 
 
         // -------------------------------------------------------------------------
         //  FIXED: ZONE DIRECT (Returns MetricsAggregateDTO)
         // -------------------------------------------------------------------------
         @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" +
                 "COALESCE(SUM(a.totalApp), 0), " +
                 "COALESCE(SUM(a.appSold), 0), " +
                 "COALESCE(SUM(a.appConfirmed), 0), " +
                 "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN a.appAvailable ELSE 0 END), 0), " +
                 "COALESCE(SUM(a.appUnavailable), 0), " +
                 "COALESCE(SUM(a.appDamaged), 0), " +
                 "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN a.totalApp ELSE 0 END), 0)) " +
                 "FROM AppStatusTrack a " +
                 "LEFT JOIN a.issuedByType ibt " +
                 "WHERE a.zone.id = :zoneId AND a.academicYear.id = :yearId AND a.isActive = 1")
         Optional<MetricsAggregateDTO> getMetricsByZoneIdAndYear(
                 @Param("zoneId") Integer zoneId,
                 @Param("yearId") Integer yearId
         );
 
         @Query(value = "SELECT DISTINCT a.acdc_year_id FROM sce_application.sce_app_stats_trk a WHERE a.zone_id = :zoneId AND a.is_active = 1", nativeQuery = true)
         List<Integer> findDistinctYearIdsByZoneId(@Param("zoneId") Integer zoneId);
         
         @Query(value = "SELECT COALESCE(SUM(a.app_available), 0) " +
                "FROM sce_application.sce_app_stats_trk a " +
                "WHERE a.zone_id = :zoneId " +
                "AND a.acdc_year_id = :yearId " +
                "AND a.is_active = 1 " +
                "AND a.app_issued_type_id = 4", nativeQuery = true)
         Optional<Long> getProMetricByZoneId_FromStatus(
                 @Param("zoneId") Integer zoneId,
                 @Param("yearId") Integer yearId
         );
 
         // For CAMPUS
         @Query("SELECT COALESCE(SUM(a.appAvailable), 0) " +
                "FROM AppStatusTrack a " +
                "WHERE a.campus.id = :campusId " +
                "AND a.academicYear.id = :yearId " +
                "AND a.issuedByType.appIssuedId = 4")
         Optional<Long> getProMetricByCampusId_FromStatus(
                 @Param("campusId") Integer campusId,
                 @Param("yearId") Integer yearId
         );
         
      // In AppStatusTrackRepository.java
 
         @Query("SELECT NEW com.application.dto.MetricsAggregateDTO("
             + "COALESCE(SUM(ast.totalApp), 0), "
             + "COALESCE(SUM(ast.appSold), 0), "
             + "COALESCE(SUM(ast.appConfirmed), 0), "
             + "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN ast.appAvailable ELSE 0 END), 0), "
             + "COALESCE(SUM(ast.appUnavailable), 0), "
             + "COALESCE(SUM(ast.appDamaged), 0), "
             + "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN ast.totalApp ELSE 0 END), 0)) "
             + "FROM AppStatusTrack ast "
             + "LEFT JOIN ast.issuedByType ibt "
             + "WHERE ast.employee.id = :empId "
             + "AND ast.academicYear.acdcYearId = :acdcYearId") // Check: This was the fix from the last error
         Optional<MetricsAggregateDTO> getMetricsByEmployeeAndYear(
             @Param("empId") Long empId,
             @Param("acdcYearId") Integer acdcYearId);
         
      @Query("SELECT COALESCE(SUM(a.appAvailable), 0) " +
             "FROM AppStatusTrack a " +
             "WHERE a.employee.id = :empId " + // <--- CHANGED FROM .empId to .id
             "AND a.academicYear.acdcYearId = :yearId " +
             "AND a.issuedByType.appIssuedId = 4")
      Optional<Long> getProMetricByEmployeeId_FromStatus(
          @Param("empId") Integer empId,
          @Param("yearId") Integer yearId
      );
         
      // In AppStatusTrackRepository.java
 
         @Query("SELECT DISTINCT ast.academicYear.acdcYearId FROM AppStatusTrack ast WHERE ast.employee.id = :empId")
         List<Integer> findDistinctYearIdsByEmployee(@Param("empId") Long empId); // Note: Use Long if employee.empId is Long
 
         @Query("SELECT NEW com.application.dto.MetricsAggregateDTO(" +
                    "COALESCE(SUM(a.totalApp), 0), " +
                    "COALESCE(SUM(a.appSold), 0), " +
                    "COALESCE(SUM(a.appConfirmed), 0), " +
                    "COALESCE(SUM(CASE WHEN (ibt.appIssuedId IS NULL OR ibt.appIssuedId != 4) THEN a.appAvailable ELSE 0 END), 0), " +
                    "COALESCE(SUM(a.appUnavailable), 0), " +
                    "COALESCE(SUM(a.appDamaged), 0), " +
                    "COALESCE(SUM(CASE WHEN ibt.appIssuedId = 4 THEN a.totalApp ELSE 0 END), 0)) " +
                    "FROM AppStatusTrack a " +
                    "LEFT JOIN a.issuedByType ibt " +
                    "WHERE a.campus.id IN :campusIds AND a.academicYear.id = :yearId AND a.isActive = 1")
            Optional<MetricsAggregateDTO> getMetricsByCampusIdsAndYear(
                    @Param("campusIds") List<Integer> campusIds,
                    @Param("yearId") Integer yearId
            );
 
            @Query("SELECT COALESCE(SUM(a.appAvailable), 0) " +
                   "FROM AppStatusTrack a " +
                   "WHERE a.campus.id IN :campusIds " +
                   "AND a.academicYear.id = :yearId " +
                   "AND a.issuedByType.appIssuedId = 4")
            Optional<Long> getProMetricByCampusIds_FromStatus(
                    @Param("campusIds") List<Integer> campusIds,
                    @Param("yearId") Integer yearId
            );
 
            @Query("SELECT DISTINCT a.academicYear.id FROM AppStatusTrack a WHERE a.campus.id IN :campusIds")
            List<Integer> findDistinctYearIdsByCampusIds(@Param("campusIds") List<Integer> campusIds);
}