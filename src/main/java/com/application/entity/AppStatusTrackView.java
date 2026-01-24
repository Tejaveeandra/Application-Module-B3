package com.application.entity;

import java.time.LocalDateTime;

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
@Table(name = "sce_app_status_track", schema = "sce_application")
public class AppStatusTrackView {

	@Id
	@Column(name = "app_no")
	private Integer num;
	private Integer pro_emp_id;
	private String pro_name;
	private Integer dgm_emp_id;
	private String dgm_name;
	private Integer zone_id;
	private String zone_name;
	private Integer cmps_id;
	private String cmps_name;
	private String status;
	private LocalDateTime date;
	@Column(name = "acdc_year_id")
	private Integer acdc_year_id;
	@Column(name = "academic_year")
	private String academic_year;
}
