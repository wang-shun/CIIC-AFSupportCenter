package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dto;

import lombok.Data;

@Data
public class SsEmpTaskArchiveDTO {
    private String empBase;
    private String startMonth;
    private String handleMonth;
    private String remark;
    private Integer taskCategory;
    private String taskCategoryName;
    private Long empArchiveId;
    private String companyId;
    private String employeeId;
    private String employeeName;
    private String idNum;
    private String ssSerial;
    private Integer empClassify;
    private String empClassifyName;
    private String archiveStatus;
    private String ssMonth;
    private String endMonth;
    private String settlementArea;

}
