package com.application.entity;

import java.util.Date;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@DynamicInsert
@DynamicUpdate
@Table(name="sce_stud_personal_detls" , schema = "sce_student")
public class StudentPersonalDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer stud_personal_id;
	private Long stud_aadhaar_no;
	private Integer is_active;
	private Integer created_by;

	@Column(name = "created_date", insertable = false, updatable = false)
    private Date created_date;

    private Integer updated_by;
    private Date updated_date;
	private Date dob;
	
	@ManyToOne
	@JoinColumn(name = "food_type_id")
	private FoodType foodType;
	
	@ManyToOne
	@JoinColumn(name = "caste_id")
	private Caste caste;
	
	@ManyToOne
	@JoinColumn(name = "religion_id")
	private Religion religion;
	
	@ManyToOne
	@JoinColumn(name = "stud_adms_id")
	private StudentAcademicDetails studentAcademicDetails;
	
	@ManyToOne
	@JoinColumn(name = "blood_group_id")
	private BloodGroup bloodGroup;
	

}