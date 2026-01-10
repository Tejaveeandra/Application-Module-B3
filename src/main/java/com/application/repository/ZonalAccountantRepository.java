package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.ZonalAccountant;

@Repository
public interface ZonalAccountantRepository extends JpaRepository<ZonalAccountant, Integer> {

	List<ZonalAccountant> findByZoneZoneIdAndIsActive(int zoneId, int isActive);

	@Query("SELECT za.zone.zoneId FROM ZonalAccountant za WHERE za.employee.emp_id = :empId AND za.isActive = 1")
	List<Integer> findZoneIdByEmployeeId(@Param("empId") int empId);

	@Query("SELECT za FROM ZonalAccountant za " +
			"LEFT JOIN FETCH za.zone " +
			"LEFT JOIN FETCH za.employee " +
			"LEFT JOIN FETCH za.campus " +
			"WHERE za.employee.emp_id = :empId AND za.isActive = 1 " +
			"ORDER BY za.zone_acct_id DESC")
	List<ZonalAccountant> findByEmployeeEmpId(@Param("empId") int empId);

	@Query("SELECT COUNT(za) FROM ZonalAccountant za WHERE za.zone.zoneId = :zoneId AND za.isActive = 1")
	Integer validateZone(@Param("zoneId") Integer zoneId);

	@Query("SELECT z FROM ZonalAccountant z WHERE z.employee.emp_id = :empId AND z.isActive = 1")
	Optional<ZonalAccountant> findByEmployeeEmpId(@Param("empId") Integer empId);

	@Query("SELECT z FROM ZonalAccountant z WHERE z.employee.emp_id = :empId AND z.isActive = 1")
	Optional<ZonalAccountant> lookupByEmpId(@Param("empId") Integer empId);

	@Query("SELECT z FROM ZonalAccountant z WHERE z.employee.emp_id = :empId AND z.isActive = 1")
	List<ZonalAccountant> findActiveByEmployee(@Param("empId") Integer empId);

}