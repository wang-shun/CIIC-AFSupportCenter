package com.ciicsh.gto.afsupportcenter.healthmedical.entity.bo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 雇员付款退回
 * </p>
 *
 * @author chenpb
 * @since 2018-02-01
 */
public class EmpBankRefundBO {

    /** 付款申请记录编号 */
    private String applyId;
    /** 业务ID */
    private Integer businessId;
    /** 客户业务类型 */
    private Integer businessType;
    /** 公司编号 */
    private String companyId;
    /** 雇员编号 */
    private String employeeId;
    /** 客户经理 */
    private String customerManager;
    /** 服务中心 */
    private String serviceCenter;
    /** 银行卡ID */
    private String bankcardId;
    /** 支付金额 */
    private BigDecimal payAmount;
    /** 退票日期 */
    private Date refundDate;
    /** 退票原因 */
    private String reason;

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getCustomerManager() {
        return customerManager;
    }

    public void setCustomerManager(String customerManager) {
        this.customerManager = customerManager;
    }

    public String getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public String getBankcardId() {
        return bankcardId;
    }

    public void setBankcardId(String bankcardId) {
        this.bankcardId = bankcardId;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public Date getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(Date refundDate) {
        this.refundDate = refundDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "EmpBankRefundBO{" +
            "applyId=" + applyId +
            ", businessId=" + businessId +
            ", companyId='" + companyId + '\'' +
            ", employeeId='" + employeeId + '\'' +
            ", customerManager='" + customerManager + '\'' +
            ", serviceSenter='" + serviceCenter + '\'' +
            ", bankcardId='" + bankcardId + '\'' +
            ", payAmount=" + payAmount +
            ", remark='" + reason + '\'' +
            '}';
    }
}
