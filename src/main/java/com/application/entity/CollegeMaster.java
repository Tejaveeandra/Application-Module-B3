package com.application.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sce_board_college" , schema = "sce_campus")
public class CollegeMaster {
	
	@Id
	private int college_master_id;
	private String district_name;
	private String college_code;
	private String college_name;
//	@Column(name="clg_grp")
//	private String clgGrp;
	@ManyToOne
	@JoinColumn(name = "district_id")
	private District district;
	
	@ManyToOne
	@JoinColumn(name = "new_district_id")
	private District district2;
	
	@ManyToOne
	@JoinColumn(name = "college_type_id")
	private CollegeType  collegeType;;
}