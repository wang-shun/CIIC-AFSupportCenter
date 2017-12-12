package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity;

import com.baomidou.mybatisplus.enums.IdType;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotations.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 对账差异结果表
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-07
 */
@TableName("ss_statement_result")
public class SsStatementResultPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录Id
     */
	@TableId(value="statement_detail_id", type= IdType.AUTO)
    private Long statementDetailId;

    /**
     * 外键，对账单Id
     */
    @TableField("statement_id")
    private Long statementId;

    /**
     * 外键:雇员Id
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 变更类型
     */
    @TableField("change_type")
    private Integer changeType;

    /**
     * 变更类型名称
     */
    @TableField("change_type_name")
    private String changeTypeName;

    /**
     * 项目类型
     */
    @TableField("project_type")
    private Integer projectType;

    /**
     * 项目名称
     */
    @TableField("project_type_name")
    private String projectTypeName;

    /**
     * 外键：险种
     */
    @TableField("ss_type")
    private Byte ssType;

    /**
     * 社保险种名称
     */
    @TableField("ss_type_name")
    private String ssTypeName;

    /**
     * 从社保局导入金额
     */
    @TableField("imp_amount")
    private BigDecimal impAmount;

    /**
     * 社保系统计算出的金额
     */
    @TableField("ss_amount")
    private BigDecimal ssAmount;

    /**
     * 差值
     */
    @TableField("diff_amount")
    private BigDecimal diffAmount;

    /**
     * 是否有效
     */
    @TableField("is_active")
    private Boolean isActive;
    /**
     * 创建时间
     */
	@TableField("created_time")
	private LocalDateTime createdTime;
    /**
     * 更新时间
     */
	@TableField("modified_time")
	private LocalDateTime modifiedTime;
    /**
     * 创建者用户Id
     */
	@TableField("created_by")
	private String createdBy;
    /**
     * 更新者用户Id
     */
	@TableField("modified_by")
	private String modifiedBy;


    /**
     * 获取记录Id
     *
     * @return statement_detail_id - 记录Id
     */
    public Long getStatementDetailId() {
        return statementDetailId;
    }

    /**
     * 设置记录Id
     *
     * @param statementDetailId 记录Id
     */
    public void setStatementDetailId(Long statementDetailId) {
        this.statementDetailId = statementDetailId;
    }

    /**
     * 获取外键，对账单Id
     *
     * @return statement_id - 外键，对账单Id
     */
    public Long getStatementId() {
        return statementId;
    }

    /**
     * 设置外键，对账单Id
     *
     * @param statementId 外键，对账单Id
     */
    public void setStatementId(Long statementId) {
        this.statementId = statementId;
    }

    /**
     * 获取外键:雇员Id
     *
     * @return employee_id - 外键:雇员Id
     */
    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * 设置外键:雇员Id
     *
     * @param employeeId 外键:雇员Id
     */
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId == null ? null : employeeId.trim();
    }

    /**
     * 获取变更类型
     *
     * @return change_type - 变更类型
     */
    public Integer getChangeType() {
        return changeType;
    }

    /**
     * 设置变更类型
     *
     * @param changeType 变更类型
     */
    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    /**
     * 获取变更类型名称
     *
     * @return change_type_name - 变更类型名称
     */
    public String getChangeTypeName() {
        return changeTypeName;
    }

    /**
     * 设置变更类型名称
     *
     * @param changeTypeName 变更类型名称
     */
    public void setChangeTypeName(String changeTypeName) {
        this.changeTypeName = changeTypeName == null ? null : changeTypeName.trim();
    }

    /**
     * 获取外键：险种
     *
     * @return ss_type - 外键：险种
     */
    public Byte getSsType() {
        return ssType;
    }

    /**
     * 设置外键：险种
     *
     * @param ssType 外键：险种
     */
    public void setSsType(Byte ssType) {
        this.ssType = ssType;
    }

    /**
     * 获取社保险种名称
     *
     * @return ss_type_name - 社保险种名称
     */
    public String getSsTypeName() {
        return ssTypeName;
    }

    /**
     * 设置社保险种名称
     *
     * @param ssTypeName 社保险种名称
     */
    public void setSsTypeName(String ssTypeName) {
        this.ssTypeName = ssTypeName == null ? null : ssTypeName.trim();
    }

    /**
     * 获取从社保局导入金额
     *
     * @return imp_amount - 从社保局导入金额
     */
    public BigDecimal getImpAmount() {
        return impAmount;
    }

    /**
     * 设置从社保局导入金额
     *
     * @param impAmount 从社保局导入金额
     */
    public void setImpAmount(BigDecimal impAmount) {
        this.impAmount = impAmount;
    }

    /**
     * 获取社保系统计算出的金额
     *
     * @return ss_amount - 社保系统计算出的金额
     */
    public BigDecimal getSsAmount() {
        return ssAmount;
    }

    /**
     * 设置社保系统计算出的金额
     *
     * @param ssAmount 社保系统计算出的金额
     */
    public void setSsAmount(BigDecimal ssAmount) {
        this.ssAmount = ssAmount;
    }

    /**
     * 获取差值
     *
     * @return diff_amount - 差值
     */
    public BigDecimal getDiffAmount() {
        return diffAmount;
    }

    /**
     * 设置差值
     *
     * @param diffAmount 差值
     */
    public void setDiffAmount(BigDecimal diffAmount) {
        this.diffAmount = diffAmount;
    }

    /**
     * 获取是否有效
     *
     * @return is_active - 是否有效
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * 设置是否有效
     *
     * @param isActive 是否有效
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public Integer getProjectType() {
        return projectType;
    }

    public void setProjectType(Integer projectType) {
        this.projectType = projectType;
    }

    public String getProjectTypeName() {
        return projectTypeName;
    }

    public void setProjectTypeName(String projectTypeName) {
        this.projectTypeName = projectTypeName;
    }
}
