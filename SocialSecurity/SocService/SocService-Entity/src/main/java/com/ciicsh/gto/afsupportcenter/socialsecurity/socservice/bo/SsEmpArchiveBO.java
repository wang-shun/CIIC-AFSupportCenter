package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo;

import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsEmpArchive;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsEmpTask;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public class SsEmpArchiveBO extends SsEmpArchive {

    // 来源表 emp_employee
    // 雇员姓名
    private String employeeName;
    // 雇员证件号
    private String idNum;
    //企业社保账号
    private String ssAccount;
    //养老金独立开户密码
    private String ssPwd;
    //社保类型
    private Integer ssAccountType;
    //结算区县
    private String settlementArea;
    //客户名称
    private String title;
    //社保账户名称
    private String comAccountName;
    //学历
    private String education;
    //户口地址
    private String residenceAddress;
    //户口属性
    private String residenceAttribute;
    //联系人地址
    private String contactAddress;
    //雇员属性
    private String employeeAttribute;
    //新增和转入 对应一些雇员信息，比如人员属性和工资等
    private SsEmpTask ssEmpTask;
    //旧基数
    private BigDecimal oldEmpBase;
    //客服经理
    private String leaderShipName;
    //客服中心
    private Integer serviceCenterValue;
    //客服中心
    private String serviceCenter;

    private Integer empClassify;

    private String zipCode;

    private Integer idCardType;

    private LocalDate laborStartDate;

    private LocalDate laborEndDate;

    private Integer afempStatus;

    private LocalDate outOperateDate;

    private String endMonth;

    private Integer archiveTaskStatus;

    private String ssSerial;

    private String ssMonth;

    private Integer hasOut;

    private String orderParam;

    public Integer getHasOut() {
        return hasOut;
    }

    public void setHasOut(Integer hasOut) {
        this.hasOut = hasOut;
    }

    @Override
    public String getSsMonth() {
        return ssMonth;
    }

    @Override
    public void setSsMonth(String ssMonth) {
        this.ssMonth = ssMonth;
    }

    @Override
    public String getSsSerial() {
        return ssSerial;
    }

    @Override
    public void setSsSerial(String ssSerial) {
        this.ssSerial = ssSerial;
    }

    @Override
    public Integer getArchiveTaskStatus() {
        return archiveTaskStatus;
    }

    @Override
    public void setArchiveTaskStatus(Integer archiveTaskStatus) {
        this.archiveTaskStatus = archiveTaskStatus;
    }

    @Override
    public String getEndMonth() {
        return endMonth;
    }

    @Override
    public void setEndMonth(String endMonth) {
        this.endMonth = endMonth;
    }

    public LocalDate getLaborStartDate() {
        return laborStartDate;
    }

    public void setLaborStartDate(LocalDate laborStartDate) {
        this.laborStartDate = laborStartDate;
    }

    public LocalDate getLaborEndDate() {
        return laborEndDate;
    }

    public void setLaborEndDate(LocalDate laborEndDate) {
        this.laborEndDate = laborEndDate;
    }

    public Integer getAfempStatus() {
        return afempStatus;
    }

    public void setAfempStatus(Integer afempStatus) {
        this.afempStatus = afempStatus;
    }

    public LocalDate getOutOperateDate() {
        return outOperateDate;
    }

    public void setOutOperateDate(LocalDate outOperateDate) {
        this.outOperateDate = outOperateDate;
    }

    @Override
    public Integer getEmpClassify() {
        return empClassify;
    }

    @Override
    public void setEmpClassify(Integer empClassify) {
        this.empClassify = empClassify;
    }

    @Override
    public String getServiceCenter() {
        return serviceCenter;
    }

    @Override
    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public Integer getServiceCenterValue() {
        return serviceCenterValue;
    }

    public String getSsPwd() {
        return ssPwd;
    }

    public void setSsPwd(String ssPwd) {
        this.ssPwd = ssPwd;
    }

    public void setServiceCenterValue(Integer serviceCenterValue) {
        this.serviceCenterValue = serviceCenterValue;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getSsAccount() {
        return ssAccount;
    }

    public void setSsAccount(String ssAccount) {
        this.ssAccount = ssAccount;
    }

    public Integer getSsAccountType() {
        return ssAccountType;
    }

    public void setSsAccountType(Integer ssAccountType) {
        this.ssAccountType = ssAccountType;
    }

    public String getSettlementArea() {
        return settlementArea;
    }

    public void setSettlementArea(String settlementArea) {
        this.settlementArea = settlementArea;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComAccountName() {
        return comAccountName;
    }

    public void setComAccountName(String comAccountName) {
        this.comAccountName = comAccountName;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getResidenceAddress() {
        return residenceAddress;
    }

    public void setResidenceAddress(String residenceAddress) {
        this.residenceAddress = residenceAddress;
    }

    public String getResidenceAttribute() {
        return residenceAttribute;
    }

    public void setResidenceAttribute(String residenceAttribute) {
        this.residenceAttribute = residenceAttribute;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getEmployeeAttribute() {
        return employeeAttribute;
    }

    public void setEmployeeAttribute(String employeeAttribute) {
        this.employeeAttribute = employeeAttribute;
    }

    public SsEmpTask getSsEmpTask() {
        return ssEmpTask;
    }

    public void setSsEmpTask(SsEmpTask ssEmpTask) {
        this.ssEmpTask = ssEmpTask;
    }

    public BigDecimal getOldEmpBase() {
        return oldEmpBase;
    }

    public void setOldEmpBase(BigDecimal oldEmpBase) {
        this.oldEmpBase = oldEmpBase;
    }

    public String getLeaderShipName() {
        return leaderShipName;
    }

    public void setLeaderShipName(String leaderShipName) {
        this.leaderShipName = leaderShipName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Integer getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(Integer idCardType) {
        this.idCardType = idCardType;
    }

    public String getOrderParam() {
        return orderParam;
    }

    public void setOrderParam(String orderParam) {
        this.orderParam = orderParam;
    }
}
