package com.ciicsh.gto.afsupportcenter.healthmedical.commandservice.entity.po;

import com.baomidou.mybatisplus.annotations.TableId;
import java.time.LocalTime;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 保单号批次明细
 * </p>
 *
 * @author zhaogang
 * @since 2017-12-20
 */
@TableName("hm_insurance_policy_batch_detail")
public class InsurancePolicyBatchDetailPO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("insurance_policy_num_batch_detail_id")
	private Integer insurancePolicyNumBatchDetailId;
	@TableField("insurance_policy_num_batch_id")
	private String insurancePolicyNumBatchId;
    /**
     * 雇员编号
     */
	@TableField("employee_id")
	private Integer employeeId;
    /**
     * 公司编号
     */
	@TableField("company_id")
	private Integer companyId;
    /**
     * 投保日期
     */
	@TableField("insure_date")
	private LocalTime insureDate;
    /**
     * 保费
     */
	private String premium;
    /**
     * 代理费
     */
	@TableField("agency_fee")
	private String agencyFee;
    /**
     * 是否可用
     */
	@TableField("is_active")
	private Boolean isActive;
    /**
     * 创建时间
     */
	@TableField("created_time")
	private LocalTime createdTime;
    /**
     * 最后更新时间
     */
	@TableField("modified_time")
	private LocalTime modifiedTime;
    /**
     * 创建者登录名
     */
	@TableField("created_by")
	private String createdBy;
    /**
     * 修改者登录名
     */
	@TableField("modified_by")
	private String modifiedBy;


	public Integer getInsurancePolicyNumBatchDetailId() {
		return insurancePolicyNumBatchDetailId;
	}

	public void setInsurancePolicyNumBatchDetailId(Integer insurancePolicyNumBatchDetailId) {
		this.insurancePolicyNumBatchDetailId = insurancePolicyNumBatchDetailId;
	}

	public String getInsurancePolicyNumBatchId() {
		return insurancePolicyNumBatchId;
	}

	public void setInsurancePolicyNumBatchId(String insurancePolicyNumBatchId) {
		this.insurancePolicyNumBatchId = insurancePolicyNumBatchId;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public LocalTime getInsureDate() {
		return insureDate;
	}

	public void setInsureDate(LocalTime insureDate) {
		this.insureDate = insureDate;
	}

	public String getPremium() {
		return premium;
	}

	public void setPremium(String premium) {
		this.premium = premium;
	}

	public String getAgencyFee() {
		return agencyFee;
	}

	public void setAgencyFee(String agencyFee) {
		this.agencyFee = agencyFee;
	}

	public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalTime createdTime) {
		this.createdTime = createdTime;
	}

	public LocalTime getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(LocalTime modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Override
	public String toString() {
		return "InsurancePolicyBatchDetail{" +
			", insurancePolicyNumBatchDetailId=" + insurancePolicyNumBatchDetailId +
			", insurancePolicyNumBatchId=" + insurancePolicyNumBatchId +
			", employeeId=" + employeeId +
			", companyId=" + companyId +
			", insureDate=" + insureDate +
			", premium=" + premium +
			", agencyFee=" + agencyFee +
			", isActive=" + isActive +
			", createdTime=" + createdTime +
			", modifiedTime=" + modifiedTime +
			", createdBy=" + createdBy +
			", modifiedBy=" + modifiedBy +
			"}";
	}
}