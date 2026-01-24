package com.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.application.dto.AppStatusDTO;
import com.application.entity.AppStatusTrackView;
import com.application.entity.Campus;
import com.application.entity.SCEmployeeEntity;
import com.application.exception.ZoneIdRequiredException;
import com.application.repository.AppStatusTrackViewRepository;
import com.application.repository.CampusRepository;
import com.application.repository.SCEmployeeRepository;

@Service
public class ApplicationStatusViewService {

    @Autowired
    private AppStatusTrackViewRepository appStatusTrackViewRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private SCEmployeeRepository scEmployeeRepository;

    // @Cacheable(value = "appStatusByCampus", key = "#cmpsId")
    public List<AppStatusTrackView> getApplicationStatusByCampus(int cmpsId) {
        return appStatusTrackViewRepository.findByCmps_id(cmpsId);
    }

    public List<AppStatusTrackView> getApplicationStatusByEmployeeCampus(int empId) {
        try {
            List<AppStatusTrackView> result = appStatusTrackViewRepository.findByEmployeeCampus(empId);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<AppStatusTrackView> getApplicationStatusByRole(int empId, String category, List<Integer> yearIds) {
        // 1. Fetch Employee
        SCEmployeeEntity employee = scEmployeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));

        // DEBUG: Print what we found
        System.out.println("--- DEBUG LOG ---");
        System.out.println("Logged in User: " + employee.getFirstName());
        System.out.println("Raw Role from DB: '" + employee.getEmpStudApplicationRole() + "'");
        System.out.println("Raw Category from DB: '" + employee.getCategory() + "'");
        System.out.println("Input Category Param: '" + category + "'");
        System.out.println("Input Year IDs: " + yearIds);

        // 2. Normalize Role (Handle nulls and Case)
        String role = (employee.getEmpStudApplicationRole() != null)
                ? employee.getEmpStudApplicationRole().trim().toUpperCase()
                : "";

        System.out.println("Normalized Role for Switch: '" + role + "'");

        // 3. Switch Logic
        // Determine Business ID if category is provided
        Integer businessId = null;
        if (category != null) {
            if (category.equalsIgnoreCase("school")) {
                businessId = 2;
            } else if (category.equalsIgnoreCase("college")) {
                businessId = 1;
            }
        }

        switch (role) {
            case "ADMIN":
                // Returns all (filtered by category if provided)
                if (businessId != null) {
                    return (yearIds != null && !yearIds.isEmpty())
                            ? appStatusTrackViewRepository.findAllByBusinessIdAndYearIds(businessId, yearIds)
                            : appStatusTrackViewRepository.findAllByBusinessId(businessId);
                }
                return (yearIds != null && !yearIds.isEmpty())
                        ? appStatusTrackViewRepository.findAllByCategoryAndYearIds(category, yearIds)
                        : appStatusTrackViewRepository.findAllByCategory(category);

            case "ZONAL ACCOUNTANT":
                if (businessId != null) {
                    return (yearIds != null && !yearIds.isEmpty())
                            ? appStatusTrackViewRepository.findByZone_idAndBusinessIdAndYearIds(employee.getZoneId(), businessId, yearIds)
                            : appStatusTrackViewRepository.findByZone_idAndBusinessId(employee.getZoneId(), businessId);
                }
                return (yearIds != null && !yearIds.isEmpty())
                        ? appStatusTrackViewRepository.findByZone_idAndYearIds(employee.getZoneId(), yearIds)
                        : appStatusTrackViewRepository.findByZone_id(employee.getZoneId());

            case "DGM":
                if (businessId != null) {
                    return (yearIds != null && !yearIds.isEmpty())
                            ? appStatusTrackViewRepository.findByDgm_emp_idAndBusinessIdAndYearIds(empId, businessId, yearIds)
                            : appStatusTrackViewRepository.findByDgm_emp_idAndBusinessId(empId, businessId);
                }
                return (yearIds != null && !yearIds.isEmpty())
                        ? appStatusTrackViewRepository.findByDgm_emp_idAndYearIds(empId, yearIds)
                        : appStatusTrackViewRepository.findByDgm_emp_id(empId);

            case "CAMPUS":
                if (businessId != null) {
                    return (yearIds != null && !yearIds.isEmpty())
                            ? appStatusTrackViewRepository.findByCmps_idAndBusinessIdAndYearIds(employee.getEmpCampusId(), businessId, yearIds)
                            : appStatusTrackViewRepository.findByCmps_idAndBusinessId(employee.getEmpCampusId(), businessId);
                }
                return (yearIds != null && !yearIds.isEmpty())
                        ? appStatusTrackViewRepository.findByCmps_idAndYearIds(employee.getEmpCampusId(), yearIds)
                        : appStatusTrackViewRepository.findByCmps_id(employee.getEmpCampusId());

            case "PRO":
                System.out.println("Matched Case: PRO - Expanding visibility to campus-wide data");
                // Expand visibility: Show all applications for the PRO's assigned campus
                if (businessId != null) {
                    return (yearIds != null && !yearIds.isEmpty())
                            ? appStatusTrackViewRepository.findByCmps_idAndBusinessIdAndYearIds(employee.getEmpCampusId(), businessId, yearIds)
                            : appStatusTrackViewRepository.findByCmps_idAndBusinessId(employee.getEmpCampusId(), businessId);
                }
                return (yearIds != null && !yearIds.isEmpty())
                        ? appStatusTrackViewRepository.findByCmps_idAndYearIds(employee.getEmpCampusId(), yearIds)
                        : appStatusTrackViewRepository.findByCmps_id(employee.getEmpCampusId());

            default:
                System.out.println("!! NO MATCH FOUND !! Role: " + role);
                return new ArrayList<>();
        }
    }

    @Cacheable(value = "allstatustable")
    public List<AppStatusDTO> getStatusByCategory(String category) {
        // category will be "school" or "college"
        return appStatusTrackViewRepository.getStatusDataByCategory(category);
    }

    public List<AppStatusDTO> fetchApplicationStatus(String category, Integer zoneId) {

        List<Campus> campuses;

        // --------------------------------------------
        // CATEGORY = SCHOOL → businessId = 2
        // zoneId is mandatory for SCHOOL
        // --------------------------------------------
        if (category.equalsIgnoreCase("school")) {
            int businessId = 2;

            if (zoneId == null) {
                throw new ZoneIdRequiredException("Zone ID must be provided for SCHOOL category");
            }

            campuses = campusRepository.findSchoolCampusesByZone(businessId, zoneId);
        }

        // --------------------------------------------
        // CATEGORY = COLLEGE → businessId = 1
        // zoneId is optional
        // --------------------------------------------
        else if (category.equalsIgnoreCase("college")) {
            int businessId = 1;

            if (zoneId != null) {
                campuses = campusRepository.findCollegeCampusesByZone(businessId, zoneId);
            } else {
                campuses = campusRepository.findCollegeCampuses(businessId);
            }
        }

        // --------------------------------------------
        // INVALID CATEGORY
        // --------------------------------------------
        else {
            campuses = campusRepository.findAll();
        }

        // Extract campus IDs
        List<Integer> campusIds = campuses.stream()
                .map(Campus::getCampusId)
                .toList();

        if (campusIds.isEmpty()) {
            return List.of();
        }

        return appStatusTrackViewRepository.findDTOByCampusIds(campusIds);
    }

    public List<AppStatusTrackView> getAllLatestStatus() {
        return appStatusTrackViewRepository.findAllLatest();
    }

}