package com.ciicsh.gto.afsupportcenter.housefund.siteservice.dao;

import com.ciicsh.gto.afsupportcenter.housefund.siteservice.entity.HfPayment;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 公积金汇缴支付批次表 Mapper 接口
 * </p>
 */
@Mapper
@Component
public interface HfPaymentMapper extends BaseMapper<HfPayment> {

}
