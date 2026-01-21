package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.dto.AppSeriesDTO;
import com.application.entity.BalanceTrack;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface BalanceTrackRepository extends JpaRepository<BalanceTrack, Integer> {

        @Query("SELECT b FROM BalanceTrack b WHERE b.academicYear.acdcYearId = :academicYearId AND b.employee.id = :employeeId AND b.isActive = 1")
        Optional<BalanceTrack> findBalanceTrack(@Param("academicYearId") int academicYearId,
                        @Param("employeeId") int employeeId);

        @Query("SELECT b FROM BalanceTrack b WHERE b.academicYear.acdcYearId = :academicYearId AND b.employee.id = :employeeId AND b.isActive = 1")
        List<BalanceTrack> findAppNumberRanges(@Param("academicYearId") int academicYearId,
                        @Param("employeeId") int employeeId);

        @Query("SELECT bt FROM BalanceTrack bt WHERE :appNo BETWEEN bt.appFrom AND bt.appTo AND bt.isActive = 1 AND bt.issuedByType.appIssuedId = 4")
        Optional<BalanceTrack> findActiveBalanceTrackByAppNoRange(@Param("appNo") Long appNo);

        @Query("SELECT DISTINCT b.amount FROM BalanceTrack b WHERE b.employee.id = :empId AND b.academicYear.id = :academicYearId AND b.isActive = 1")
        List<Float> findAmountsByEmpIdAndAcademicYear(
                        @Param("empId") int empId,
                        @Param("academicYearId") int academicYearId // NEW PARAMETER
        );

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT b FROM BalanceTrack b WHERE " +
                        "b.academicYear.acdcYearId = :yearId " +
                        "AND b.employee.emp_id = :empId " +
                        "AND b.isActive = 1 " +
                        "AND b.amount = :amount " +
                        "ORDER BY b.appFrom ASC")
        List<BalanceTrack> findActiveBalancesByEmpAndAmount(
                        @Param("yearId") int yearId,
                        @Param("empId") int empId,
                        @Param("amount") Float amount);

        // Read-only version without lock for GET endpoints
        @Query("SELECT b FROM BalanceTrack b WHERE " +
                        "b.academicYear.acdcYearId = :yearId " +
                        "AND b.employee.emp_id = :empId " +
                        "AND b.isActive = 1 " +
                        "AND b.amount = :amount " +
                        "AND b.appAvblCnt > 0 " +
                        "ORDER BY b.appFrom ASC")
        List<BalanceTrack> findActiveBalancesByEmpAndAmountReadOnly(
                        @Param("yearId") int yearId,
                        @Param("empId") int empId,
                        @Param("amount") Float amount);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT b FROM BalanceTrack b WHERE " + "b.academicYear.acdcYearId = :yearId "
                        + "AND b.employee.emp_id = :empId " + "AND b.amount = :amount " + "AND b.isActive = 1 "
                        + "AND b.appTo = :targetEnd")
        Optional<BalanceTrack> findMergeableRowForEmployee(@Param("yearId") int yearId, @Param("empId") int empId,
                        @Param("amount") Float amount, @Param("targetEnd") int targetEnd);

        @Query("SELECT new com.application.dto.AppSeriesDTO(concat(b.appFrom, ' - ', b.appTo), b.appFrom, b.appTo, b.appAvblCnt) "
                        +
                        "FROM BalanceTrack b WHERE " +
                        "b.employee.emp_id = :empId " +
                        "AND b.amount = :amount " +
                        "AND b.isActive = 1 " +
                        "ORDER BY b.appFrom ASC")
        List<AppSeriesDTO> findSeriesByEmpIdAndAmount(
                        @Param("empId") int empId,
                        @Param("amount") Double amount);

        @Query("SELECT new com.application.dto.AppSeriesDTO(concat(b.appFrom, ' - ', b.appTo), b.appFrom, b.appTo, b.appAvblCnt) "
                        +
                        "FROM BalanceTrack b WHERE " +
                        "b.issuedToProId = :proId " +
                        "AND b.amount = :amount " +
                        "AND b.isActive = 1 " +
                        "ORDER BY b.appFrom ASC")
        List<AppSeriesDTO> findSeriesByProIdAndAmount(
                        @Param("proId") int proId,
                        @Param("amount") Double amount);

        // New methods with academicYearId filter
        @Query("SELECT new com.application.dto.AppSeriesDTO(concat(b.appFrom, ' - ', b.appTo), b.appFrom, b.appTo, b.appAvblCnt) "
                        +
                        "FROM BalanceTrack b WHERE " +
                        "b.employee.emp_id = :empId " +
                        "AND b.academicYear.acdcYearId = :academicYearId " +
                        "AND b.amount = :amount " +
                        "AND b.isActive = 1 " +
                        "ORDER BY b.appFrom ASC")
        List<AppSeriesDTO> findSeriesByEmpIdYearAndAmount(
                        @Param("empId") int empId,
                        @Param("academicYearId") int academicYearId,
                        @Param("amount") Double amount);

        @Query("SELECT new com.application.dto.AppSeriesDTO(concat(b.appFrom, ' - ', b.appTo), b.appFrom, b.appTo, b.appAvblCnt) "
                        +
                        "FROM BalanceTrack b WHERE " +
                        "b.issuedToProId = :proId " +
                        "AND b.academicYear.acdcYearId = :academicYearId " +
                        "AND b.amount = :amount " +
                        "AND b.isActive = 1 " +
                        "ORDER BY b.appFrom ASC")
        List<AppSeriesDTO> findSeriesByProIdYearAndAmount(
                        @Param("proId") int proId,
                        @Param("academicYearId") int academicYearId,
                        @Param("amount") Double amount);

        @Query("SELECT b FROM BalanceTrack b WHERE " + "b.academicYear.acdcYearId = :yearId "
                        + "AND b.issuedToProId = :proId " + "AND b.isActive = 1 " + "AND b.amount = :amount")
        Optional<BalanceTrack> findActiveBalanceByProAndAmount(@Param("yearId") int yearId, @Param("proId") int proId,
                        @Param("amount") Float amount);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT b FROM BalanceTrack b WHERE b.academicYear.acdcYearId = :yearId AND b.issuedToProId = :proId AND b.isActive = 1 AND b.amount = :amount ORDER BY b.appFrom ASC")
        List<BalanceTrack> findActiveBalancesByProAndAmount(@Param("yearId") int yearId, @Param("proId") int proId,
                        @Param("amount") Float amount);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT b FROM BalanceTrack b WHERE " + "b.academicYear.acdcYearId = :yearId "
                        + "AND b.issuedToProId = :proId " + "AND b.amount = :amount " + "AND b.isActive = 1 "
                        + "AND b.appTo = :targetEnd")
        Optional<BalanceTrack> findMergeableRowForPro(@Param("yearId") int yearId, @Param("proId") int proId,
                        @Param("amount") Float amount, @Param("targetEnd") int targetEnd);

        @Query("""
                            SELECT COALESCE(SUM(a.totalApp), 0)
                            FROM AdminApp a
                            WHERE a.employee.id = :empId
                              AND a.academicYear.id = :academicYearId
                              AND a.is_active = 1
                        """)
        Long sumTotalAppByEmployeeAndAcademicYear(
                        @Param("empId") Integer empId,
                        @Param("academicYearId") Integer academicYearId);

        @Query("""
                            SELECT COALESCE(SUM(b.appAvblCnt), 0)
                            FROM BalanceTrack b
                            WHERE b.employee.id = :empId
                              AND b.academicYear.acdcYearId = :academicYearId
                              AND b.isActive = 1
                        """)
        Long sumAppAvblCntByEmployeeAndAcademicYear(
                        @Param("empId") Integer empId,
                        @Param("academicYearId") Integer academicYearId);

        @Query("""
                            SELECT COALESCE(SUM(b.appAvblCnt), 0)
                            FROM BalanceTrack b
                            WHERE b.employee.id = :empId
                              AND b.academicYear.acdcYearId = :academicYearId
                              AND b.amount = :amount
                              AND b.isActive = 1
                        """)
        Long sumAppAvblCntByEmployeeAndAcademicYearAndAmount(
                        @Param("empId") Integer empId,
                        @Param("academicYearId") Integer academicYearId,
                        @Param("amount") Double amount);

        // Find the next available start number (appFrom) for an employee
        // Returns the minimum appFrom from active balance tracks matching the criteria
        @Query("SELECT MIN(b.appFrom) FROM BalanceTrack b WHERE b.employee.emp_id = :employeeId AND b.academicYear.acdcYearId = :academicYearId AND b.amount = :amount AND b.isActive = 1 AND b.appAvblCnt > 0")
        Optional<Integer> findNextAvailableStart(
                        @Param("employeeId") int employeeId,
                        @Param("academicYearId") int academicYearId,
                        @Param("amount") Float amount);
}
