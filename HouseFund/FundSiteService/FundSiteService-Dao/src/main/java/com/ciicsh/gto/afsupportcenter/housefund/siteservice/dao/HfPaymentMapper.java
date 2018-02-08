package com.ciicsh.gto.afsupportcenter.housefund.siteservice.dao;

import com.ciicsh.gto.afsupportcenter.housefund.siteservice.entity.HfPayment;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyCompanyProxyDTO;
import com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyEmployeeProxyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 公积金汇缴支付批次表 Mapper 接口
 * </p>
 */
@Mapper
@Component
public interface HfPaymentMapper extends BaseMapper<HfPayment> {

    /**
     * <p></p>
     *
     * @author linhui
     * @date 2018-01-11
     */
    List<PayapplyCompanyProxyDTO> getPaymentComList(@Param("paymentId") Long paymentId, @Param("paymentMonth") String
        paymentMonth);

    List<PayapplyEmployeeProxyDTO> getPaymentEmpList(@Param("paymentId") Long paymentId, @Param("paymentMonth")
        String paymentMonth);

}
