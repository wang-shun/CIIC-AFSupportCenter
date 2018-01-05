package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 本地社保中，中智公司与社保局的对账单（各一条记录）
 * </p>
 *
 * @author wengxk
 * @since 2017-12-08
 */
public class SsOperatePaymentDTO {

    private static final long serialVersionUID = 1L;



    /**
     * 批次ID
     */
    private Long paymentId;

    /**
     * 申请备注
     */
    private String applyRemark;
    /**
     * 批退备注
     */
    private String rejectionRemark;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getApplyRemark() {
        return applyRemark;
    }

    public void setApplyRemark(String applyRemark) {
        this.applyRemark = applyRemark;
    }

    public String getRejectionRemark() {
        return rejectionRemark;
    }

    public void setRejectionRemark(String rejectionRemark) {
        this.rejectionRemark = rejectionRemark;
    }
}
