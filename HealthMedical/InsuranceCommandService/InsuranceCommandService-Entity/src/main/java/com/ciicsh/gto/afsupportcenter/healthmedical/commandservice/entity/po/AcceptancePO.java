package com.ciicsh.gto.afsupportcenter.healthmedical.commandservice.entity.po;

import com.baomidou.mybatisplus.enums.IdType;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotations.TableId;
import java.time.LocalTime;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 补充医疗理赔表
 * </p>
 *
 * @author zhaogang
 * @since 2017-12-29
 */
@TableName("hm_acceptance")
public class AcceptancePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 受理单Id
     */
	@TableId(value="acceptance_id", type= IdType.AUTO)
	private Integer acceptanceId;
    /**
     * 案卷号（如：CIICYBQ20160126002）
     */
	@TableField("archieve_id")
	private String archieveId;
    /**
     * 导入日期
     */
	@TableField("input_date")
	private LocalTime inputDate;
    /**
     * 雇员编号
     */
	@TableField("employee_id")
	private String employeeId;
    /**
     * 状态：
1.未审核
2.已审核未付
3.已审核已付
4.已批退
     */
	private Integer status;
    /**
     * 处理时间
     */
	@TableField("handle_time")
	private LocalDate handleTime;
    /**
     * 发票数
     */
	@TableField("invoice_num")
	private Integer invoiceNum;
    /**
     * 公司理赔金额
     */
	@TableField("company_money")
	private BigDecimal companyMoney;
    /**
     * 保险公司理赔金额
     */
	@TableField("insurance_company_money")
	private BigDecimal insuranceCompanyMoney;
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


	public Integer getAcceptanceId() {
		return acceptanceId;
	}

	public void setAcceptanceId(Integer acceptanceId) {
		this.acceptanceId = acceptanceId;
	}

	public String getArchieveId() {
		return archieveId;
	}

	public void setArchieveId(String archieveId) {
		this.archieveId = archieveId;
	}

	public LocalTime getInputDate() {
		return inputDate;
	}

	public void setInputDate(LocalTime inputDate) {
		this.inputDate = inputDate;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public LocalDate getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(LocalDate handleTime) {
		this.handleTime = handleTime;
	}

	public Integer getInvoiceNum() {
		return invoiceNum;
	}

	public void setInvoiceNum(Integer invoiceNum) {
		this.invoiceNum = invoiceNum;
	}

	public BigDecimal getCompanyMoney() {
		return companyMoney;
	}

	public void setCompanyMoney(BigDecimal companyMoney) {
		this.companyMoney = companyMoney;
	}

	public BigDecimal getInsuranceCompanyMoney() {
		return insuranceCompanyMoney;
	}

	public void setInsuranceCompanyMoney(BigDecimal insuranceCompanyMoney) {
		this.insuranceCompanyMoney = insuranceCompanyMoney;
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
		return "Acceptance{" +
			", acceptanceId=" + acceptanceId +
			", archieveId=" + archieveId +
			", inputDate=" + inputDate +
			", employeeId=" + employeeId +
			", status=" + status +
			", handleTime=" + handleTime +
			", invoiceNum=" + invoiceNum +
			", companyMoney=" + companyMoney +
			", insuranceCompanyMoney=" + insuranceCompanyMoney +
			", isActive=" + isActive +
			", createdTime=" + createdTime +
			", modifiedTime=" + modifiedTime +
			", createdBy=" + createdBy +
			", modifiedBy=" + modifiedBy +
			"}";
	}
}
