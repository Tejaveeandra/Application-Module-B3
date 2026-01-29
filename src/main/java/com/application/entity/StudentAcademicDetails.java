package com.application.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "sce_stud_acdc_detl", schema = "sce_student")
public class StudentAcademicDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int stud_adms_id;
	@Column(name = "stud_adms_no")
	private Long studAdmsNo;
	
	@Column(name = "ht_no")
	private String ht_no;
	
	@Column(name = "first_name")
	private String first_name;
	
	@Column(name = "last_name")
	private String last_name;
	
	@Column(name = "adms_date")
	private LocalDate adms_date;
	
	@Column(name = "created_by")
	private int created_by;
	
	@Column(name = "created_date")
	private LocalDateTime created_date;
	
	@Column(name = "updated_by")
	private Integer updated_by;
	
	@Column(name = "updated_date")
	private LocalDateTime updated_date;
	
	@Column(name = "doj")
	private LocalDate doj;
	// @Column(name = "orientation_batch_id")
	// private int orientation_batch_id;
	@Column(name = "pre_school_name")
	private String pre_school_name;
	
	@Column(name = "admission_referred_by")
	private String admission_referred_by;
	
	@Column(name = "score_app_no")
	private String score_app_no;
	
	@Column(name = "score_marks")
	private Integer score_marks;
	
	@Column(name = "additional_orientation_fee")
	private Long additional_orientation_fee;
	
	@Column(name = "app_sale_date")
	private LocalDateTime app_sale_date;
	
	@Column(name = "app_conf_date")
	private LocalDateTime app_conf_date;
	
	@Column(name = "is_active")
	private int is_active;
	
	@Column(name = "apaar_no")
	private String apaar_no;
	
	@Column(name = "annexure_path")
	private String annexure_path;
	
	@Column(name = "lang_id")
	private int[] lang_id;
	
	@Column(name = "photo_path")
	private String photo_path;
	
	@Column(name = "pro_receipt_no")
	private Long pro_receipt_no;

	@ManyToOne
	@JoinColumn(name = "acdc_year_id")
	private AcademicYear academicYear;

	@ManyToOne
	@JoinColumn(name = "gender_id")
	private Gender gender;

	@ManyToOne
	@JoinColumn(name = "adms_type_id")
	private AdmissionType admissionType;

	@ManyToOne
	@JoinColumn(name = "cmps_id")
	private Campus campus;

	@ManyToOne
	@JoinColumn(name = "stud_type_id")
	private StudentType studentType;

	@ManyToOne
	@JoinColumn(name = "study_type_id")
	private StudyType studyType;

	@ManyToOne
	@JoinColumn(name = "section_id")
	private Section section;

	@ManyToOne
	@JoinColumn(name = "quota_id")
	private Quota quota;

	@ManyToOne
	@JoinColumn(name = "status_id")
	private Status status;

	@ManyToOne
	@JoinColumn(name = "class_id")
	private StudentClass studentClass;

	@ManyToOne
	@JoinColumn(name = "pro_id", referencedColumnName = "emp_id")
	private Employee employee;

	@ManyToOne
	@JoinColumn(name = "pre_school_state_id", referencedColumnName = "state_id")
	private State state;

	@ManyToOne
	@JoinColumn(name = "pre_school_district_id", referencedColumnName = "district_id")
	private District district;

	@ManyToOne
	@JoinColumn(name = "pre_school_type_id", referencedColumnName = "school_type_id")
	private CampusSchoolType preCampusSchoolType;

	@ManyToOne
	@JoinColumn(name = "orientation_id")
	private Orientation orientation;

	@Column(name = "pre_hallticket_no")
	private String pre_hallticket_no;

	@ManyToOne
	@JoinColumn(name = "pre_college_type_id", referencedColumnName = "board_college_type_id")
	private CollegeType collegeType;

	@ManyToOne
	@JoinColumn(name = "pre_college_state_id", referencedColumnName = "state_id")
	private State state2;

	@ManyToOne
	@JoinColumn(name = "pre_college_district_id", referencedColumnName = "district_id")
	private District district2;

	@Column(name = "pre_college_name")
	private String pre_college_name;

	@ManyToOne
	@JoinColumn(name = "stud_status_id")
	private Status studStatus;

}