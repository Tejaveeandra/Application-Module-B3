package com.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.application.entity.ProDetails;

@Repository
public interface ProDetailsRepository extends JpaRepository<ProDetails, Integer>{

	@Query("SELECT p.primary_contact_no FROM ProDetails p WHERE p.pro_detl_id = :proDetlId")
	Long findMobileNoByProDetlId(@Param("proDetlId") int proDetlId);
}
