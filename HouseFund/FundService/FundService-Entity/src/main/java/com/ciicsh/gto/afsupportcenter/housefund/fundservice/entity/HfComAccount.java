package com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity;

import com.baomidou.mybatisplus.enums.IdType;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 企业公积金账户：存储中智大库、中智外包、独立户企业的账号，含基本公积金和补充公积金
HF：House Fun
 * </p>
 */
@TableName("hf_com_account")
public class HfComAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
	@TableId(value="com_account_id", type= IdType.AUTO)
	private Long comAccountId;
    /**
     * 企业账户名称
     */
	@TableField("com_account_name")
	private String comAccountName;
    /**
     * 付款方式:
            1 自付（客户自己汇缴给银行，雇员由中智办理）
            2 我司付款（客户预付）
            3 垫付
     */
	@TableField("payment_way")
	private Integer paymentWay;
    /**
     * 1 独立户 2 大库、3 外包
     */
	@TableField("hf_account_type")
	private Integer hfAccountType;
    /**
     * 客户公积金账户 每月的关账到哪一天1-31
     */
	@TableField("close_day")
	private Integer closeDay;
    /**
     * 公积金企业U盾代管
     */
	@TableField("ukey_store")
	private Integer ukeyStore;
    /**
     * 缴费区县：15 徐汇—X、16 西郊—C、17东方路—P、18 卢湾—L、0 黄浦—H
     */
	@TableField("payment_bank")
	private Integer paymentBank;
    /**
     * 备注
     */
    @TableField("remark")
	private String remark;
    /**
     * 账户状态:0初始 1有效 2 终止
     */
    @TableField("state")
	private Integer state;
    /**
     * 是否可用
     */
	@TableField("is_active")
	private Boolean isActive;
    /**
     * 创建时间
     */
	@TableField("created_time")
	private Date createdTime;
    /**
     * 最后更新时间
     */
	@TableField("modified_time")
	private Date modifiedTime;
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
    @TableField("modified_display_name")
    private String modifiedDisplayName;

    @TableField("created_display_name")
    private String createdDisplayName;

    public String getModifiedDisplayName() {
        return modifiedDisplayName;
    }

    public void setModifiedDisplayName(String modifiedDisplayName) {
        this.modifiedDisplayName = modifiedDisplayName;
    }

    public String getCreatedDisplayName() {
        return createdDisplayName;
    }

    public void setCreatedDisplayName(String createdDisplayName) {
        this.createdDisplayName = createdDisplayName;
    }

    public Long getComAccountId() {
		return comAccountId;
	}

	public void setComAccountId(Long comAccountId) {
		this.comAccountId = comAccountId;
	}

	public String getComAccountName() {
		return comAccountName;
	}

	public void setComAccountName(String comAccountName) {
		this.comAccountName = comAccountName;
	}

	public Integer getPaymentWay() {
		return paymentWay;
	}

	public void setPaymentWay(Integer paymentWay) {
		this.paymentWay = paymentWay;
	}

	public Integer getHfAccountType() {
		return hfAccountType;
	}

	public void setHfAccountType(Integer hfAccountType) {
		this.hfAccountType = hfAccountType;
	}

	public Integer getCloseDay() {
		return closeDay;
	}

	public void setCloseDay(Integer closeDay) {
		this.closeDay = closeDay;
	}

	public Integer getUkeyStore() {
		return ukeyStore;
	}

	public void setUkeyStore(Integer ukeyStore) {
		this.ukeyStore = ukeyStore;
	}

	public Integer getPaymentBank() {
		return paymentBank;
	}

	public void setPaymentBank(Integer paymentBank) {
		this.paymentBank = paymentBank;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Date modifiedTime) {
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
		return "HfComAccount{" +
			", comAccountId=" + comAccountId +
			", comAccountName=" + comAccountName +
			", paymentWay=" + paymentWay +
			", hfAccountType=" + hfAccountType +
			", closeDay=" + closeDay +
			", ukeyStore=" + ukeyStore +
			", paymentBank=" + paymentBank +
			", remark=" + remark +
			", state=" + state +
			", isActive=" + isActive +
			", createdTime=" + createdTime +
			", modifiedTime=" + modifiedTime +
			", createdBy=" + createdBy +
			", modifiedBy=" + modifiedBy +
			"}";
	}
}
