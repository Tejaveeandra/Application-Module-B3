package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.CampusProView;

@Repository
public interface CampusProViewRepository extends JpaRepository<CampusProView, Integer>{
	
	@Query("SELECT v FROM CampusProView v WHERE v.cmps_id = :campusId")
    List<CampusProView> findByCampusId(@Param("campusId") int campusId);

	    @Query("""
	           SELECT new com.application.dto.GenericDropdownDTO(cpv.cmps_emp_id, cpv.emp_name)
	           FROM CampusProView cpv
	           WHERE cpv.cmps_id = :campusId
	             AND cpv.is_active = 1
	           """)
	    List<com.application.dto.GenericDropdownDTO> findDropdownByCampusId(@Param("campusId") int campusId);
	    
	    @Query("SELECT c FROM CampusProView c WHERE c.cmps_emp_id = :empId")
	    Optional<CampusProView> findByEmp_id(@Param("empId") int empId);

}
