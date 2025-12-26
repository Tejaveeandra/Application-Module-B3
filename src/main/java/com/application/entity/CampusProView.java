package com.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sce_cmps_pro" , schema = "sce_application")
public class CampusProView {
	
	@Id
	private int cmps_id;
	private String cmps_name;
	private int cmps_emp_id;
	private String emp_name;
	private int is_active;
	
	 @Column(name = "zone_id")
	 private Integer zoneId;
	 
	 @Column(name = "zone_name")
	 private String zoneName;
	
	@Column(name = "is_our_emp")
	private Integer isOurEmp;
	
}
