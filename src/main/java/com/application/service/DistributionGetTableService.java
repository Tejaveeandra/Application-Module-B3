package com.application.service;
 
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.application.dto.DistributionGetTableDTO;
import com.application.entity.AdminApp;
import com.application.entity.Distribution;
import com.application.repository.AdminAppRepository;
import com.application.repository.CampaignRepository;
import com.application.repository.DgmRepository;
import com.application.repository.DistributionRepository;
import com.application.repository.StateRepository;
import com.application.repository.DistrictRepository;
import com.application.repository.CityRepository;
import com.application.repository.EmployeeRepository; // 🎯 New Import
import com.application.repository.CampusProViewRepository;
import com.application.repository.ProDetailsRepository;
import com.application.entity.CampusProView;
 
@Service
public class DistributionGetTableService {
 
    @Autowired
    private DistributionRepository distributionRepository;
 
    @Autowired
    private DgmRepository dgmRepository;
 
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private AdminAppRepository adminAppRepository;
    
    @Autowired
    StateRepository state;
    
    @Autowired
    DistrictRepository district;
    
    @Autowired
    CityRepository city;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CampusProViewRepository campusProViewRepository;
    
    @Autowired
    private ProDetailsRepository proDetailsRepository;
    
//    @Cacheable(value = "distributionsByEmployee", key = "#empId")
    public List<DistributionGetTableDTO> getDistributionsByEmployeeAndIssuedToType(int empId, int issuedToTypeId) {
        
        final int PRO_ISSUED_TYPE_ID = 4;
        
        List<Distribution> distributions =
                distributionRepository.findByCreatedByAndIssuedToType(empId, issuedToTypeId);
       
        return distributions.stream().map(d -> {
            try {
            DistributionGetTableDTO dto = new DistributionGetTableDTO();
 
            // 1. Set displaySeries
            dto.setDisplaySeries(d.getAppStartNo() + " - " + d.getAppEndNo());
 
            // 2. Populate all base fields and fetch mobile number
            dto.setAppDistributionId(d.getAppDistributionId());
            dto.setAppStartNo(d.getAppStartNo());
            dto.setAppEndNo(d.getAppEndNo());
            dto.setTotalAppCount(d.getTotalAppCount());
            dto.setAmount(d.getAmount());
            dto.setIsActive(d.getIsActive());
            dto.setCreated_by(d.getCreated_by());
            // 🎯 FIXED: Handle null for issued_to_emp_id by falling back to issued_to_pro_id (assuming both are Integer; adjust if types differ)
            Integer issuedToEmpId = d.getIssued_to_emp_id();
            Integer issuedToProId = d.getIssued_to_pro_id();
            int issuedToId = (issuedToEmpId != null) ? issuedToEmpId.intValue() : (issuedToProId != null ? issuedToProId.intValue() : 0);
            dto.setIssued_to_emp_id(issuedToId);
            dto.setIssueDate(d.getIssueDate());
            
            // 🎯 NEW LOGIC: Fetch Mobile Number
            try {
                // The repository returns a String, but the DTO field is Long (mobileNmuber)
                String mobileString = employeeRepository.findMobileNoByEmpId(d.getCreated_by());
                if (mobileString != null) {
                     // Parse the String to Long for the DTO (handle typo: mobileNmuber)
                    dto.setMobileNmuber(Long.parseLong(mobileString));
                }
            } catch (Exception e) {
                // Handle case where mobile number might be invalid or not found
                System.err.println("Error fetching or parsing mobile number for employee " + d.getCreated_by() + ": " + e.getMessage());
            }
 
            // 3. Handle related IDs and NAMES from JPA relationships
            int acdcYearId = 0;
            
            if (d.getIssuedByType() != null) dto.setIssued_by_type_id(d.getIssuedByType().getAppIssuedId());
            if (d.getIssuedToType() != null) dto.setIssued_to_type_id(d.getIssuedToType().getAppIssuedId());
            
            // --- LOCATION IDs and NAMES (FIXED) ---
            if (d.getState() != null) {
                dto.setState_id(d.getState().getStateId());
                dto.setStatename(d.getState().getStateName());
            }
            if (d.getDistrict() != null) {
                dto.setDistrict_id(d.getDistrict().getDistrictId());
                dto.setDistrictname(d.getDistrict().getDistrictName());
            }
            if (d.getCity() != null) {
                dto.setCity_id(d.getCity().getCityId());
                dto.setCityname(d.getCity().getCityName());
            }
            
            if (d.getZone() != null) dto.setZone_id(d.getZone().getZoneId());
            if (d.getCampus() != null) dto.setCmps_id(d.getCampus().getCampusId());
            if (d.getAcademicYear() != null) {
                acdcYearId = d.getAcademicYear().getAcdcYearId();
                dto.setAcdc_year_id(acdcYearId);
                dto.setAcademicYear(d.getAcademicYear().getAcademicYear());
            }
            
            // 4. Master Range Enrichment
            int masterStart = 0;
            int masterEnd = 0;
            Double amount = d.getAmount() != null ? d.getAmount().doubleValue() : null;
 
            if (acdcYearId > 0 && amount != null) {
                List<AdminApp> masterRecords = adminAppRepository.findMasterRecordByYearAndAmount(
                        acdcYearId, amount);
 
                if (!masterRecords.isEmpty()) {
                    AdminApp master = masterRecords.get(0);
                    masterStart = master.getAppFromNo();
                    masterEnd = master.getAppToNo();
                }
            }
            
            // 5. Set Master Range (Uncomment if fields exist in DTO)
            // dto.setMasterStartNo(masterStart);
            // dto.setMasterEndNo(masterEnd);
            
            
            // 6. Derive the additional fields (Names, Zone, DGM, Campaign)
            String issuedToName = null;
            if (d.getIssuedToEmployee() != null) {
                // Internal employee (is_our_emp = 1)
                issuedToName = d.getIssuedToEmployee().getFirst_name() + " " + d.getIssuedToEmployee().getLast_name();
            } else if (d.getIssued_to_pro_id() != null && issuedToTypeId == PRO_ISSUED_TYPE_ID) {
                // External PRO (is_our_emp = 0) - issued_to_pro_id contains campus_id
                try {
                    List<com.application.dto.GenericDropdownDTO> proList = 
                        campusProViewRepository.findDropdownByCampusId(d.getIssued_to_pro_id());
                    if (!proList.isEmpty()) {
                        // Get the first PRO name from the campus
                        issuedToName = proList.get(0).getName();
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching PRO name from CampusProView for campus_id " + 
                        d.getIssued_to_pro_id() + ": " + e.getMessage());
                }
            } else if (d.getCampus() != null) {
                issuedToName = d.getCampus().getCampusName();
            } else if (d.getZone() != null) {
                issuedToName = d.getZone().getZoneName();
            } else if (d.getDistrict() != null) {
                issuedToName = d.getDistrict().getDistrictName();
            } else if (d.getCity() != null) {
                issuedToName = d.getCity().getCityName();
            } else if (d.getState() != null) {
                issuedToName = d.getState().getStateName();
            }
            dto.setIssuedToName(issuedToName);
 
            String zoneName = null;
            if (d.getZone() != null) {
                zoneName = d.getZone().getZoneName();
            }
            dto.setZoneName(zoneName);
 
            String campusName = null;
            if (d.getCampus() != null) {
                campusName = d.getCampus().getCampusName();
            }
            dto.setCampusName(campusName);
 
            String dgmName = null;
            // ... (DGM repository logic here) ...
            dto.setDgmName(dgmName);
 
            String campaignAreaName = null;
            int campaignAreaId = 0;
            // ... (Campaign repository logic here) ...
            
            dto.setCampaignAreaName(campaignAreaName);
            dto.setCampaignAreaId(campaignAreaId);
            
            return dto;
            } catch (Exception e) {
                System.err.println("Error processing distribution ID " + d.getAppDistributionId() + ": " + e.getMessage());
                e.printStackTrace();
                // Return a minimal DTO to prevent breaking the entire response
                DistributionGetTableDTO errorDto = new DistributionGetTableDTO();
                errorDto.setAppDistributionId(d.getAppDistributionId());
                errorDto.setDisplaySeries("Error loading data");
                return errorDto;
            }
        }).toList();
    }

    public String getMobileByCmpsEmpId(int cmpsEmpId) {
        // Check the sce_cmps_pro view to get is_our_emp
        CampusProView viewRecord = campusProViewRepository.findByEmp_id(cmpsEmpId)
            .orElse(null);
        
        if (viewRecord == null) {
            return null;
        }
        
        // Check is_our_emp value
        Integer isOurEmp = viewRecord.getIsOurEmp();
        
        if (isOurEmp != null && isOurEmp == 1) {
            // is_our_emp = 1, get from Employee table
            return employeeRepository.findMobileNoByEmpId(cmpsEmpId);
        } else if (isOurEmp != null && isOurEmp == 0) {
            // is_our_emp = 0, get from ProDetails table
            Long mobileLong = proDetailsRepository.findMobileNoByProDetlId(cmpsEmpId);
            if (mobileLong != null) {
                return String.valueOf(mobileLong);
            }
            return null;
        }
        
        return null;
    }

}