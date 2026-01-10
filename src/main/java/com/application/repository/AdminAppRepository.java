package com.application.repository;
 
import java.util.List;
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.AdminApp;
 
@Repository
public interface AdminAppRepository extends JpaRepository<AdminApp, Integer> {
 
    // 1️⃣ Get amounts
//    @Query("""
//        SELECT DISTINCT a.app_amount
//        FROM AdminApp a
//        WHERE a.employee.id = :empId
//          AND a.academicYear.id = :academicYearId
//          AND a.is_active = 1
//    """)
    // Updated to return Double List (Amount list)
    @Query("SELECT a.app_amount FROM AdminApp a WHERE " +
           "a.employee.emp_id = :empId " +
           "AND a.academicYear.acdcYearId = :yearId " +
           "AND a.is_active = 1")
    List<Double> findAmountsByEmpIdAndAcademicYear(
            @Param("empId") int empId,
            @Param("yearId") int yearId
    );
 
    // 4️⃣ Validation for application number range
//    @Query("""
//        SELECT a FROM AdminApp a
//        WHERE :applicationNo BETWEEN a.appFromNo AND a.appToNo
//          AND a.academicYear.id = :academicYearId
//          AND a.is_active = 1
//    """)
//    Optional<AdminApp> findActiveAdminAppByAppNoAndAcademicYear(
//            @Param("applicationNo") long applicationNo,
//            @Param("academicYearId") int academicYearId
//    );
 
    // 6️⃣ Find by emp + year + amount
    @Query("""
        SELECT a FROM AdminApp a
        WHERE a.employee.emp_id = :empId
          AND a.academicYear.acdcYearId = :yearId
          AND (a.app_amount = :amount OR a.app_fee = :amount)
          AND a.is_active = 1
    """)
    Optional<AdminApp> findByEmpAndYearAndAmount(
            @Param("empId") int empId,
            @Param("yearId") int yearId,
            @Param("amount") Float amount
    );
   
    // Find all AdminApp records by emp + year + amount (returns List)
    @Query("""
        SELECT a FROM AdminApp a
        WHERE a.employee.emp_id = :empId
          AND a.academicYear.acdcYearId = :yearId
          AND (a.app_amount = :amount OR a.app_fee = :amount)
          AND a.is_active = 1
        ORDER BY a.admin_app_id ASC
    """)
    List<AdminApp> findAllByEmpAndYearAndAmount(
            @Param("empId") int empId,
            @Param("yearId") int yearId,
            @Param("amount") Double amount
    );
   
    @Query(value = "SELECT * FROM sce_application.sce_admin_app WHERE :appNo BETWEEN app_from_no AND app_to_no AND is_active = 1", nativeQuery = true)
    Optional<AdminApp> findByAppNoInRange(@Param("appNo") Long appNo);
   
    @Query("""
            SELECT a FROM AdminApp a
            WHERE :applicationNo BETWEEN a.appFromNo AND a.appToNo
              AND a.academicYear.acdcYearId = :academicYearId
              AND a.is_active = 1
        """)
        Optional<AdminApp> findActiveAdminAppByAppNoAndAcademicYear(
                @Param("applicationNo") long applicationNo,
                @Param("academicYearId") int academicYearId
        );
 
    @Query(value = """
            SELECT COALESCE(SUM(total_app), 0)
            FROM sce_application.sce_admin_app
            WHERE emp_id = :empId
              AND acdc_year_id = :academicYearId
              AND is_active = 1
        """, nativeQuery = true)
        Long sumTotalAppByEmployeeAndAcademicYear(
            @Param("empId") Integer empId,
            @Param("academicYearId") Integer academicYearId
        );
   
    @Query("""
            SELECT a FROM AdminApp a
            WHERE a.academicYear.acdcYearId = :yearId
              AND (a.app_amount = :amount OR a.app_fee = :amount)
              AND a.is_active = 1
            ORDER BY a.admin_app_id ASC
        """)
     List<AdminApp> findMasterRecordByYearAndAmount(
             @Param("yearId") int yearId,
             @Param("amount") Double amount
     );
    
    @Query("""
            SELECT COALESCE(SUM(a.totalApp), 0)
            FROM AdminApp a
            WHERE a.employee.emp_id = :empId
              AND a.academicYear.acdcYearId = :yearId
              AND (a.app_amount = :amount OR a.app_fee = :amount)
              AND a.is_active = 1
        """)
    Long sumTotalAppByEmployeeAndAcademicYearAndAmount(
        @Param("empId") Integer empId,
        @Param("yearId") Integer yearId,
        @Param("amount") Double amount
    );
    
    @Query("""
            SELECT a FROM AdminApp a
            WHERE a.employee.emp_id = :empId
              AND a.academicYear.acdcYearId = :yearId
              AND (a.app_amount = :amount OR a.app_fee = :amount)
              AND a.is_active = 1
            ORDER BY a.admin_app_id ASC
        """)
     List<AdminApp> findMasterRecordByYearAndAmountAndEmployee(
             @Param("empId") int empId,
             @Param("yearId") int yearId,
             @Param("amount") Double amount
     );
     
     // Get latest year from AdminApp for a specific employee
     @Query(value = """
             SELECT MAX(acdc_year_id)
             FROM sce_application.sce_admin_app
             WHERE emp_id = :empId
               AND is_active = 1
         """, nativeQuery = true)
     Integer findLatestYearIdByEmployee(@Param("empId") Integer empId);
     
     // Get latest year from AdminApp table (any employee)
     @Query(value = """
             SELECT MAX(acdc_year_id)
             FROM sce_application.sce_admin_app
             WHERE is_active = 1
         """, nativeQuery = true)
     Integer findLatestYearIdFromAdminApp();
     
     // Get distinct app_amount values from AdminApp table
     @Query(value = """
             SELECT DISTINCT app_amount
             FROM sce_application.sce_admin_app
             WHERE app_amount IS NOT NULL
             ORDER BY app_amount ASC
         """, nativeQuery = true)
     List<Double> findDistinctAppAmounts();
}
 
 