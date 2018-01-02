package com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbcommandservice.dao;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ciicsh.gto.afsupportcenter.flexiblebenefit.entity.po.ApplyGiftRecordPO;

/**
 * <p>
  * 礼品申请记录表 Mapper 接口
 * </p>
 *
 * @author xiweizhen
 * @since 2017-12-18
 */
public interface ApplyGiftRecordCommandMapper extends BaseMapper<ApplyGiftRecordPO> {

    Integer insertSelective(ApplyGiftRecordPO applyGiftRecordPO);
}