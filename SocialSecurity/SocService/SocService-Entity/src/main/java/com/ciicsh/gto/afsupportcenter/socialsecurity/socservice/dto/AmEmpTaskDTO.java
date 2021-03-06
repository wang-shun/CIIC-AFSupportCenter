package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用工退工任务单
 * </p>
 *
 * @author xsj
 * @since 2018-02-25
 */
public class AmEmpTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键,可作为任务单序号
     */
	private Long empTaskId;
    /**
     * 雇员id
     */
	private String employeeId;
    /**
     * 客户Id
     */
	private String companyId;
    /**
     * 任务类型 1用工  2 退工 3变更 4 特殊
            
     */
	private Integer taskCategory;
    /**
     * 任务单提交人SysUserId
     */
	private String submitterId;
    /**
     * 任务单提交人所属部门Id
     */
	private String submitterDeptId;
    /**
     * 发起时间
     */
	private LocalDateTime submitTime;
    /**
     * 任务发起人备注
     */
	private String submitterRemark;
    /**
     * 任务处理状态：
            用工状态：1未反馈  2退工成功  3档未到先退工  退工单盖章未返回  退工失败  前道要求批退  其它（重打退工单 | 撤销退工  | 自开退工单  | 未交  |用工已办,未反馈|等翻牌联系单（暂留）|UKey外借|单项服务,原退工成功|转外地社保,原退工成功|转人员性质无需退工|退工成功,改社保|重复任务单|等修改备案表)
            退工状态：用工材料未签收  已开F单未完成  用工成功  用工失败  前道要求撤消用工 其它（用工材料批退  | 用工成功查无档  |  用工成功，前道已中止  |  重复任务单 |  UKey外借）
     */
	private Integer taskStatus;
    /**
     * 经办人用户ID
     */
	private String handleUserId;
    /**
     * 办理时间
     */
	private LocalDateTime handleTime;
    /**
     * 办理备注
     */
	private String handleRemark;
    /**
     * 批退备注
     */
	private String rejectionRemark;
    /**
     * TaskService 反馈的 task_id  流程下的任务ID
     */
	private String taskId;
    /**
     * 是否可用
     */
	private Boolean isActive;
    /**
     * 创建时间
     */
	private LocalDateTime createdTime;
    /**
     * 最后更新时间
     */
	private LocalDateTime modifiedTime;
    /**
     * 创建者登录名
     */
	private String createdBy;
    /**
     * 修改者登录名
     */
	private String modifiedBy;
    /**
     * 开AF单日期
     */
    private LocalDate openAfDate;
    /**
     * 存档地
     */
    private String archivePlace;
    /**
     *用工日期
     */
    private LocalDate employFeedbackOptDate;

    public Long getEmpTaskId() {
		return empTaskId;
	}

	public void setEmpTaskId(Long empTaskId) {
		this.empTaskId = empTaskId;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Integer getTaskCategory() {
		return taskCategory;
	}

	public void setTaskCategory(Integer taskCategory) {
		this.taskCategory = taskCategory;
	}

	public String getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}

	public String getSubmitterDeptId() {
		return submitterDeptId;
	}

	public void setSubmitterDeptId(String submitterDeptId) {
		this.submitterDeptId = submitterDeptId;
	}

	public LocalDateTime getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(LocalDateTime submitTime) {
		this.submitTime = submitTime;
	}

	public String getSubmitterRemark() {
		return submitterRemark;
	}

	public void setSubmitterRemark(String submitterRemark) {
		this.submitterRemark = submitterRemark;
	}

	public Integer getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(Integer taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getHandleUserId() {
		return handleUserId;
	}

	public void setHandleUserId(String handleUserId) {
		this.handleUserId = handleUserId;
	}

	public LocalDateTime getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(LocalDateTime handleTime) {
		this.handleTime = handleTime;
	}

	public String getHandleRemark() {
		return handleRemark;
	}

	public void setHandleRemark(String handleRemark) {
		this.handleRemark = handleRemark;
	}

	public String getRejectionRemark() {
		return rejectionRemark;
	}

	public void setRejectionRemark(String rejectionRemark) {
		this.rejectionRemark = rejectionRemark;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
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

    public LocalDate getOpenAfDate() {
        return openAfDate;
    }

    public void setOpenAfDate(LocalDate openAfDate) {
        this.openAfDate = openAfDate;
    }

    public String getArchivePlace() {
        return archivePlace;
    }

    public void setArchivePlace(String archivePlace) {
        this.archivePlace = archivePlace;
    }

    public LocalDate getEmployFeedbackOptDate() {
        return employFeedbackOptDate;
    }

    public void setEmployFeedbackOptDate(LocalDate employFeedbackOptDate) {
        this.employFeedbackOptDate = employFeedbackOptDate;
    }

    @Override
	public String toString() {
		return "AmEmpTask{" +
			", empTaskId=" + empTaskId +
			", employeeId=" + employeeId +
			", companyId=" + companyId +
			", taskCategory=" + taskCategory +
			", submitterId=" + submitterId +
			", submitterDeptId=" + submitterDeptId +
			", submitTime=" + submitTime +
			", submitterRemark=" + submitterRemark +
			", taskStatus=" + taskStatus +
			", handleUserId=" + handleUserId +
			", handleTime=" + handleTime +
			", handleRemark=" + handleRemark +
			", rejectionRemark=" + rejectionRemark +
			", taskId=" + taskId +
			", isActive=" + isActive +
			", createdTime=" + createdTime +
			", modifiedTime=" + modifiedTime +
			", createdBy=" + createdBy +
			", modifiedBy=" + modifiedBy +
			"}";
	}
}
