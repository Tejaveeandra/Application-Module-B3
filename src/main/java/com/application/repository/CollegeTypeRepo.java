package com.application.repository;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
 
import com.application.dto.GenericDropdownDTO;
import com.application.entity.CollegeType;
@Repository
public interface CollegeTypeRepo extends  JpaRepository<CollegeType, Integer> {
	
	@Query("SELECT new com.application.dto.GenericDropdownDTO(c.board_college_type_id, c.board_college_type) " +
		       "FROM CollegeType c WHERE c.is_active = 1")
		List<GenericDropdownDTO> getActiveCollegeTypes();
 
 
}