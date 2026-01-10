package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.AcademicYear;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Integer>{
	
	 List<AcademicYear> findByAcdcYearIdIn(List<Integer> acdcYearIds);
	 Optional<AcademicYear> findByYear(Integer year);
	 
	 // Find current academic year (latest year ID, typically the one with highest acdc_year_id)
	 @Query("SELECT a FROM AcademicYear a WHERE a.isActive = 1 ORDER BY a.acdcYearId DESC")
	 List<AcademicYear> findCurrentAcademicYear();
	 
	 // Find academic year by year ID
	 Optional<AcademicYear> findByAcdcYearId(Integer acdcYearId);
	 
	 // Find academic year by year value and active status
	 Optional<AcademicYear> findByYearAndIsActive(Integer year, Integer isActive);
	 
	 // Find all active academic years ordered by year field descending (filtered by is_active = 1)
	 @Query("SELECT a FROM AcademicYear a WHERE a.isActive = 1 ORDER BY a.year DESC")
	 List<AcademicYear> findAllActiveOrderedByYearDesc();
	 
	 // Find academic year by year ID with active status
	 @Query("SELECT a FROM AcademicYear a WHERE a.acdcYearId = :acdcYearId AND a.isActive = :isActive")
	 Optional<AcademicYear> findByAcdcYearIdAndIsActive(@Param("acdcYearId") Integer acdcYearId, @Param("isActive") Integer isActive);
	 
}