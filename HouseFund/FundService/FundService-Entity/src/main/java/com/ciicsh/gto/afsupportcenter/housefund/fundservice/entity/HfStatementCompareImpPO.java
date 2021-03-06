package com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity;

import com.baomidou.mybatisplus.enums.IdType;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotations.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 对账导入文件
 * </p>
 */
@TableName("hf_statement_compare_imp")
public class HfStatementCompareImpPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
	@TableId(value="statement_compare_imp_id", type= IdType.AUTO)
	private Long statementCompareImpId;


    @TableId(value="statement_compare_id")
    private Long statementCompareId;

	/**
     * 客户账号
     */
	@TableField("com_account")
	private String comAccount;

    /**
     * 雇员个人账号
     */
    @TableField("emp_account")
    private String empAccount;

    /**
     * 外键:雇员Id
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 雇员姓名
     */
	@TableField("emp_name")
	private String empName;
    /**
     * 身份证号码
     */
	@TableField("emp_card_num")
	private String empCardNum;
    /**
     * 个人账户状态:0010009  010001 0010002  需要对账的状态0010001,其他账号状态忽略
     */
	@TableField("emp_account_state")
	private String empAccountState;
    /**
     * 月汇缴额
     */
	@TableField("monthly_amount")
	private BigDecimal monthlyAmount;
    /**
     * 是否可用
     */
	@TableField("is_active")
	private Boolean isActive;
    /**
     * 创建时间
     */
	@TableField("created_time")
	private LocalDateTime createdTime;
    /**
     * 最后更新时间
     */
	@TableField("modified_time")
	private LocalDateTime modifiedTime;
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


	public Long getStatementCompareImpId() {
		return statementCompareImpId;
	}

	public void setStatementCompareImpId(Long statementCompareImpId) {
		this.statementCompareImpId = statementCompareImpId;
	}

    public Long getStatementCompareId() {
        return statementCompareId;
    }

    public void setStatementCompareId(Long statementCompareId) {
        this.statementCompareId = statementCompareId;
    }

    public String getComAccount() {
		return comAccount;
	}

	public void setComAccount(String comAccount) {
		this.comAccount = comAccount;
	}

    public String getEmpAccount() {
        return empAccount;
    }

    public void setEmpAccount(String empAccount) {
        this.empAccount = empAccount;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getEmpCardNum() {
		return empCardNum;
	}

	public void setEmpCardNum(String empCardNum) {
		this.empCardNum = empCardNum;
	}

	public String getEmpAccountState() {
		return empAccountState;
	}

	public void setEmpAccountState(String empAccountState) {
		this.empAccountState = empAccountState;
	}

	public BigDecimal getMonthlyAmount() {
		return monthlyAmount;
	}

	public void setMonthlyAmount(BigDecimal monthlyAmount) {
		this.monthlyAmount = monthlyAmount;
	}

	public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public LocalDateTime getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(LocalDateTime modifiedTime) {
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
		return "HfStatementCompareImpPO{" +
			", statementCompareImpId=" + statementCompareImpId +
			", comAccount=" + comAccount +
            ", employeeId=" + employeeId +
			", empName=" + empName +
			", empCardNum=" + empCardNum +
			", empAccountState=" + empAccountState +
			", monthlyAmount=" + monthlyAmount +
			", isActive=" + isActive +
			", createdTime=" + createdTime +
			", modifiedTime=" + modifiedTime +
			", createdBy=" + createdBy +
			", modifiedBy=" + modifiedBy +
			"}";
	}
}
