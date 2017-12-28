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
 * 本地社保应付金额交易记录明细表
 * </p>
 */
@TableName("ss_payment_detail")
public class SsPaymentDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录Id
     */
	@TableId(value="payment_detail_id", type= IdType.AUTO)
	private Long paymentDetailId;
    /**
     * 外键，支付记录Id
     */
	@TableField("payment_com_id")
	private Long paymentComId;
    /**
     * 序号
     */
	private String seq;
    /**
     * 支付项目代码
     */
	@TableField("payment_item")
	private Integer paymentItem;
    /**
     * 支付项目名称
     */
	@TableField("payment_item_name")
	private String paymentItemName;
    /**
     * 社保险种类型，取自全局数据字典表gtobasicdb.DicItem
     */
	@TableField("ss_type")
	private Integer ssType;
    /**
     * 社保险种名称
     */
	@TableField("ss_type_name")
	private String ssTypeName;
    /**
     * 金额值
     */
	private BigDecimal amount;
    /**
     * 是否有效,0-无效 1-有效
     */
	@TableField("is_active")
	private Boolean isActive;
	@TableField("created_time")
	private LocalDateTime createdTime;
	@TableField("modified_time")
	private LocalDateTime modifiedTime;
	@TableField("created_by")
	private String createdBy;
	@TableField("modified_by")
	private String modifiedBy;


	public Long getPaymentDetailId() {
		return paymentDetailId;
	}

	public void setPaymentDetailId(Long paymentDetailId) {
		this.paymentDetailId = paymentDetailId;
	}

	public Long getPaymentComId() {
		return paymentComId;
	}

	public void setPaymentComId(Long paymentComId) {
		this.paymentComId = paymentComId;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public Integer getPaymentItem() {
		return paymentItem;
	}

	public void setPaymentItem(Integer paymentItem) {
		this.paymentItem = paymentItem;
	}

	public String getPaymentItemName() {
		return paymentItemName;
	}

	public void setPaymentItemName(String paymentItemName) {
		this.paymentItemName = paymentItemName;
	}

	public Integer getSsType() {
		return ssType;
	}

	public void setSsType(Integer ssType) {
		this.ssType = ssType;
	}

	public String getSsTypeName() {
		return ssTypeName;
	}

	public void setSsTypeName(String ssTypeName) {
		this.ssTypeName = ssTypeName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
		return "SsPaymentDetail{" +
			", paymentDetailId=" + paymentDetailId +
			", paymentComId=" + paymentComId +
			", seq=" + seq +
			", paymentItem=" + paymentItem +
			", paymentItemName=" + paymentItemName +
			", ssType=" + ssType +
			", ssTypeName=" + ssTypeName +
			", amount=" + amount +
			", isActive=" + isActive +
			", createdTime=" + createdTime +
			", modifiedTime=" + modifiedTime +
			", createdBy=" + createdBy +
			", modifiedBy=" + modifiedBy +
			"}";
	}
}
