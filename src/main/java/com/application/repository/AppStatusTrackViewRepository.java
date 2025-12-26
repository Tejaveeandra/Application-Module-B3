package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.dto.AppStatusDTO;
import com.application.entity.AppStatusTrackView;

@Repository
public interface AppStatusTrackViewRepository extends JpaRepository<AppStatusTrackView, Integer>{
	
	Optional<AppStatusTrackView> findByNum(Long num);
	@Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = :cmpsId")
    List<AppStatusTrackView> findByCmps_id(@Param("cmpsId") int cmpsId);
	
	@Query("SELECT a FROM AppStatusTrackView a WHERE a.cmps_id = " +
	           "(SELECT e.campus.campusId FROM Employee e WHERE e.emp_id = :empId)")
	    List<AppStatusTrackView> findByEmployeeCampus(@Param("empId") int empId);
	
	@Query("SELECT a FROM AppStatusTrackView a WHERE a.num = :num AND a.cmps_name = :cmpsName")
	Optional<AppStatusTrackView> findByNumAndCmps_name(@Param("num") int num, @Param("cmpsName") String cmpsName);
	
	 @Query("SELECT new com.application.dto.AppStatusDTO(a.num, a.status, a.cmps_name, a.zone_name) " +
	           "FROM AppStatusTrackView a")
	    List<AppStatusDTO> getAllStatusData();
	 
	 @Query("SELECT new com.application.dto.AppStatusDTO(s.num, s.status, s.cmps_name, s.zone_name) " +
		       "FROM AppStatusTrackView s JOIN SCEmployeeEntity e ON s.pro_emp_id = e.empId " +
		       "WHERE LOWER(e.category) = LOWER(:category)") // <--- Case Insensitive Check
		List<AppStatusDTO> getStatusDataByCategory(@Param("category") String category);
	 
	 @Query("SELECT new com.application.dto.AppStatusDTO( " +
		       "a.num, " +                 // applicationNo  
		       "a.status, " +              // displayStatus  
		       "a.cmps_name, " +           // campus  
		       "a.zone_name ) " +          // zone
		       "FROM AppStatusTrackView a " +
		       "WHERE a.cmps_id IN :campusIds " +
		       "ORDER BY a.date DESC")
		List<AppStatusDTO> findDTOByCampusIds(@Param("campusIds") List<Integer> campusIds);
	 
	 @Query("SELECT a FROM AppStatusTrackView a ORDER BY a.date DESC")
	 List<AppStatusTrackView> findAllLatest();

	 
	 @Query("SELECT a FROM AppStatusTrackView a WHERE a.num >= :startNo AND a.num <= :endNo")
     List<AppStatusTrackView> findByApplicationNumberRange(@Param("startNo") Integer startNo, @Param("endNo") Integer endNo);
    
     // Count applications by status in a range
     @Query("SELECT COUNT(a) FROM AppStatusTrackView a WHERE a.num >= :startNo AND a.num <= :endNo AND a.status = :status")
     Long countByApplicationNumberRangeAndStatus(@Param("startNo") Integer startNo, @Param("endNo") Integer endNo, @Param("status") String status);
     
     @Query("SELECT a FROM AppStatusTrackView a JOIN SCEmployeeEntity e ON a.pro_emp_id = e.empId " +
             "WHERE LOWER(e.category) = LOWER(:category)")
      List<AppStatusTrackView> findAllByCategory(@Param("category") String category);

      // 2. For ZONAL ACCOUNTANT
      @Query("SELECT a FROM AppStatusTrackView a WHERE a.zone_id = :zoneId")
      List<AppStatusTrackView> findByZone_id(@Param("zoneId") int zoneId);

      // 3. For DGM (Using dgm_emp_id column in the View)
      @Query("SELECT a FROM AppStatusTrackView a WHERE a.dgm_emp_id = :dgmEmpId")
      List<AppStatusTrackView> findByDgm_emp_id(@Param("dgmEmpId") int dgmEmpId);

      @Query("SELECT a FROM AppStatusTrackView a WHERE a.pro_emp_id = :proEmpId")
      List<AppStatusTrackView> findByPro_emp_id(@Param("proEmpId") int proEmpId);
}
