package com.application.repository;
 
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import com.application.entity.StudentAcademicDetails;
 
@Repository
public interface StudentAcademicDetailsRepository extends JpaRepository<StudentAcademicDetails, Integer> {
 
    Optional<StudentAcademicDetails> findByStudAdmsNo(Long admissionNo);
    
    @Query("SELECT s FROM StudentAcademicDetails s WHERE s.studAdmsNo = :admissionNo AND s.is_active = :isActive")
    Optional<StudentAcademicDetails> findByStudAdmsNoAndIs_active(@Param("admissionNo") Long admissionNo, @Param("isActive") int isActive);
}