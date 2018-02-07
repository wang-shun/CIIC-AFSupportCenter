package com.ciicsh.gto.afsupportcenter.healthmedical.entity.po;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 智灵通理赔汇总表
 * </p>
 *
 * @author xiweizhen
 */
@TableName("hm_acceptance_summary")
public class AcceptanceSummary extends Model<AcceptanceSummary> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "summary_id", type = IdType.AUTO)
    private Integer summaryId;
    /**
     * 序号
     */
    @TableField("zlt_sn")
    private Integer zltSn;
    /**
     * 案卷号
     */
    @TableField("dossier_number")
    private String dossierNumber;
    /**
     * 雇员编号
     */
    @TableField("employee_id")
    private String employeeId;
    /**
     * 雇员姓名
     */
    @TableField("employee_name")
    private String employeeName;
    /**
     * 发票张数
     */
    @TableField("invoice_number")
    private Integer invoiceNumber;
    /**
     * 索赔金额
     */
    @TableField("total_compensation_amount")
    private BigDecimal totalCompensationAmount;
    /**
     * 核赔金额
     */
    @TableField("total_hepei_amount")
    private BigDecimal totalHepeiAmount;
    /**
     * 申请金额
     */
    @TableField("total_application_amount")
    private BigDecimal totalApplicationAmount;
    /**
     * 核准金额
     */
    @TableField("total_approved_amount")
    private BigDecimal totalApprovedAmount;
    /**
     * 财务年度累计已赔金额
     */
    @TableField("total_finanical_amount")
    private BigDecimal totalFinanicalAmount;
    /**
     * 保单年度累计已赔金额
     */
    @TableField("total_insurance_policy_amount")
    private BigDecimal totalInsurancePolicyAmount;
    /**
     * 国寿理赔金额
     */
    @TableField("total_guoshou_amount")
    private BigDecimal totalGuoshouAmount;
    /**
     * 公司理赔金额
     */
    @TableField("total_company_amount")
    private BigDecimal totalCompanyAmount;
    /**
     * 导入日期
     */
    @TableField("input_date")
    private Date inputDate;
    /**
     * 公司编号
     */
    @TableField("company_id")
    private Integer companyId;
    /**
     * 报销编号
     */
    @TableField("reimbursement_id")
    private String reimbursementId;
    /**
     * 审核
     */
    private Boolean audit;
    /**
     * 审核日期
     */
    @TableField("audit_date")
    private Date auditDate;
    /**
     * 成功
     */
    private Boolean success;
    /**
     * 付款方式
     */
    @TableField("pay_way")
    private String payWay;
    /**
     * 打卡号
     */
    @TableField("bank_card")
    private String bankCard;
    /**
     * 理赔日期
     */
    @TableField("claim_date")
    private Date claimDate;
    /**
     * 理赔金额
     */
    @TableField("total_claim_amount")
    private BigDecimal totalClaimAmount;
    /**
     * 连带被保险人姓名
     */
    @TableField("insured_name")
    private String insuredName;
    /**
     * 审核人
     */
    private String auditor;
    /**
     * 雇员付款记录
     */
    @TableField("pay_record")
    private Integer payRecord;
    /**
     * 理赔备注
     */
    @TableField("claim_remark")
    private String claimRemark;
    /**
     * 分类自负金额
     */
    @TableField("total_cs_payment_amount")
    private BigDecimal totalCsPaymentAmount;
    /**
     * 报销类别
     */
    private String type;
    /**
     * 投保公司
     */
    @TableField("insurance_company")
    private String insuranceCompany;
    /**
     * 受理批号
     */
    @TableField("acceptance_id")
    private String acceptanceId;
    /**
     * 保险公司到款
     */
    @TableField("insurance_company_money")
    private Boolean insuranceCompanyMoney;
    /**
     * 保险公司到款日期
     */
    @TableField("insurance_company_money_date")
    private Date insuranceCompanyMoneyDate;
    /**
     * 保险公司到款序号
     */
    @TableField("insurance_company_money_sn")
    private Integer insuranceCompanyMoneySn;
    /**
     * 赔付金额百分比
     */
    @TableField("pay_percentage")
    private String payPercentage;
    /**
     * 分类自负金额百分比
     */
    @TableField("cs_percentage")
    private String csPercentage;
    /**
     * 累计免赔额
     */
    @TableField("mianpei_amount")
    private BigDecimal mianpeiAmount;
    /**
     * 保险理赔金额
     */
    @TableField("total_insurance_company_money")
    private BigDecimal totalInsuranceCompanyMoney;
    /**
     * 处理中
     */
    private Boolean processing;
    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;


    public Integer getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Integer summaryId) {
        this.summaryId = summaryId;
    }

    public Integer getZltSn() {
        return zltSn;
    }

    public void setZltSn(Integer zltSn) {
        this.zltSn = zltSn;
    }

    public String getDossierNumber() {
        return dossierNumber;
    }

    public void setDossierNumber(String dossierNumber) {
        this.dossierNumber = dossierNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalCompensationAmount() {
        return totalCompensationAmount;
    }

    public void setTotalCompensationAmount(BigDecimal totalCompensationAmount) {
        this.totalCompensationAmount = totalCompensationAmount;
    }

    public BigDecimal getTotalHepeiAmount() {
        return totalHepeiAmount;
    }

    public void setTotalHepeiAmount(BigDecimal totalHepeiAmount) {
        this.totalHepeiAmount = totalHepeiAmount;
    }

    public BigDecimal getTotalApplicationAmount() {
        return totalApplicationAmount;
    }

    public void setTotalApplicationAmount(BigDecimal totalApplicationAmount) {
        this.totalApplicationAmount = totalApplicationAmount;
    }

    public BigDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    public void setTotalApprovedAmount(BigDecimal totalApprovedAmount) {
        this.totalApprovedAmount = totalApprovedAmount;
    }

    public BigDecimal getTotalFinanicalAmount() {
        return totalFinanicalAmount;
    }

    public void setTotalFinanicalAmount(BigDecimal totalFinanicalAmount) {
        this.totalFinanicalAmount = totalFinanicalAmount;
    }

    public BigDecimal getTotalInsurancePolicyAmount() {
        return totalInsurancePolicyAmount;
    }

    public void setTotalInsurancePolicyAmount(BigDecimal totalInsurancePolicyAmount) {
        this.totalInsurancePolicyAmount = totalInsurancePolicyAmount;
    }

    public BigDecimal getTotalGuoshouAmount() {
        return totalGuoshouAmount;
    }

    public void setTotalGuoshouAmount(BigDecimal totalGuoshouAmount) {
        this.totalGuoshouAmount = totalGuoshouAmount;
    }

    public BigDecimal getTotalCompanyAmount() {
        return totalCompanyAmount;
    }

    public void setTotalCompanyAmount(BigDecimal totalCompanyAmount) {
        this.totalCompanyAmount = totalCompanyAmount;
    }

    public Date getInputDate() {
        return inputDate;
    }

    public void setInputDate(Date inputDate) {
        this.inputDate = inputDate;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getReimbursementId() {
        return reimbursementId;
    }

    public void setReimbursementId(String reimbursementId) {
        this.reimbursementId = reimbursementId;
    }

    public Boolean getAudit() {
        return audit;
    }

    public void setAudit(Boolean audit) {
        this.audit = audit;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public Date getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(Date claimDate) {
        this.claimDate = claimDate;
    }

    public BigDecimal getTotalClaimAmount() {
        return totalClaimAmount;
    }

    public void setTotalClaimAmount(BigDecimal totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
    }

    public String getInsuredName() {
        return insuredName;
    }

    public void setInsuredName(String insuredName) {
        this.insuredName = insuredName;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public Integer getPayRecord() {
        return payRecord;
    }

    public void setPayRecord(Integer payRecord) {
        this.payRecord = payRecord;
    }

    public String getClaimRemark() {
        return claimRemark;
    }

    public void setClaimRemark(String claimRemark) {
        this.claimRemark = claimRemark;
    }

    public BigDecimal getTotalCsPaymentAmount() {
        return totalCsPaymentAmount;
    }

    public void setTotalCsPaymentAmount(BigDecimal totalCsPaymentAmount) {
        this.totalCsPaymentAmount = totalCsPaymentAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    public String getAcceptanceId() {
        return acceptanceId;
    }

    public void setAcceptanceId(String acceptanceId) {
        this.acceptanceId = acceptanceId;
    }

    public Boolean getInsuranceCompanyMoney() {
        return insuranceCompanyMoney;
    }

    public void setInsuranceCompanyMoney(Boolean insuranceCompanyMoney) {
        this.insuranceCompanyMoney = insuranceCompanyMoney;
    }

    public Date getInsuranceCompanyMoneyDate() {
        return insuranceCompanyMoneyDate;
    }

    public void setInsuranceCompanyMoneyDate(Date insuranceCompanyMoneyDate) {
        this.insuranceCompanyMoneyDate = insuranceCompanyMoneyDate;
    }

    public Integer getInsuranceCompanyMoneySn() {
        return insuranceCompanyMoneySn;
    }

    public void setInsuranceCompanyMoneySn(Integer insuranceCompanyMoneySn) {
        this.insuranceCompanyMoneySn = insuranceCompanyMoneySn;
    }

    public String getPayPercentage() {
        return payPercentage;
    }

    public void setPayPercentage(String payPercentage) {
        this.payPercentage = payPercentage;
    }

    public String getCsPercentage() {
        return csPercentage;
    }

    public void setCsPercentage(String csPercentage) {
        this.csPercentage = csPercentage;
    }

    public BigDecimal getMianpeiAmount() {
        return mianpeiAmount;
    }

    public void setMianpeiAmount(BigDecimal mianpeiAmount) {
        this.mianpeiAmount = mianpeiAmount;
    }

    public BigDecimal getTotalInsuranceCompanyMoney() {
        return totalInsuranceCompanyMoney;
    }

    public void setTotalInsuranceCompanyMoney(BigDecimal totalInsuranceCompanyMoney) {
        this.totalInsuranceCompanyMoney = totalInsuranceCompanyMoney;
    }

    public Boolean getProcessing() {
        return processing;
    }

    public void setProcessing(Boolean processing) {
        this.processing = processing;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    protected Serializable pkVal() {
        return this.summaryId;
    }

    @Override
    public String toString() {
        return "AcceptanceSummary{" +
            ", summaryId=" + summaryId +
            ", zltSn=" + zltSn +
            ", dossierNumber=" + dossierNumber +
            ", employeeId=" + employeeId +
            ", employeeName=" + employeeName +
            ", invoiceNumber=" + invoiceNumber +
            ", totalCompensationAmount=" + totalCompensationAmount +
            ", totalHepeiAmount=" + totalHepeiAmount +
            ", totalApplicationAmount=" + totalApplicationAmount +
            ", totalApprovedAmount=" + totalApprovedAmount +
            ", totalFinanicalAmount=" + totalFinanicalAmount +
            ", totalInsurancePolicyAmount=" + totalInsurancePolicyAmount +
            ", totalGuoshouAmount=" + totalGuoshouAmount +
            ", totalCompanyAmount=" + totalCompanyAmount +
            ", inputDate=" + inputDate +
            ", companyId=" + companyId +
            ", reimbursementId=" + reimbursementId +
            ", audit=" + audit +
            ", auditDate=" + auditDate +
            ", success=" + success +
            ", payWay=" + payWay +
            ", bankCard=" + bankCard +
            ", claimDate=" + claimDate +
            ", totalClaimAmount=" + totalClaimAmount +
            ", insuredName=" + insuredName +
            ", auditor=" + auditor +
            ", payRecord=" + payRecord +
            ", claimRemark=" + claimRemark +
            ", totalCsPaymentAmount=" + totalCsPaymentAmount +
            ", type=" + type +
            ", insuranceCompany=" + insuranceCompany +
            ", acceptanceId=" + acceptanceId +
            ", insuranceCompanyMoney=" + insuranceCompanyMoney +
            ", insuranceCompanyMoneyDate=" + insuranceCompanyMoneyDate +
            ", insuranceCompanyMoneySn=" + insuranceCompanyMoneySn +
            ", payPercentage=" + payPercentage +
            ", csPercentage=" + csPercentage +
            ", mianpeiAmount=" + mianpeiAmount +
            ", totalInsuranceCompanyMoney=" + totalInsuranceCompanyMoney +
            ", processing=" + processing +
            ", createdTime=" + createdTime +
            "}";
    }
}
