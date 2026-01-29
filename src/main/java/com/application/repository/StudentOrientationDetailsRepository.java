package com.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.StudentAcademicDetails;
import com.application.entity.StudentOrientationDetails;

@Repository
public interface StudentOrientationDetailsRepository extends JpaRepository<StudentOrientationDetails, Integer>{
	
	Optional<StudentOrientationDetails> findByStudentAcademicDetails(StudentAcademicDetails studentAcademicDetails);
	
	@Query("SELECT s FROM StudentOrientationDetails s WHERE s.studentAcademicDetails = :studentAcademicDetails AND s.is_active = 1")
	Optional<StudentOrientationDetails> findByStudentAcademicDetailsAndIsActive(@Param("studentAcademicDetails") StudentAcademicDetails studentAcademicDetails);
    
}
