package com.application.entity;
 
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="sce_board_college_type",schema="sce_campus")
@Entity
public class CollegeType {
	
	@Id
	private int board_college_type_id;
	
	private String board_college_type;
	private int is_active;
 
}