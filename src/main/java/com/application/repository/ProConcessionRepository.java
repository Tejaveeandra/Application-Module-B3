package com.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.ProConcession;

@Repository
public interface ProConcessionRepository extends JpaRepository<ProConcession, Integer>{
	
	
	 @Query("SELECT p FROM ProConcession p WHERE p.adm_no = :admNo AND p.is_active = 1")
	    List<ProConcession> findByAdmNo(@Param("admNo") String admNo);

	 @Query("SELECT p FROM ProConcession p WHERE p.adm_no = :admNo AND p.is_active = 1")
	 ProConcession findActiveByAdmNo(@Param("admNo") String admNo);


}
