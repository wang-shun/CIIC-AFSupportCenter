package com.ciicsh.gto.afsupportcenter.healthmedical.dao;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.ciicsh.gto.afsupportcenter.healthmedical.entity.bo.AcceptanceStatisticsBO;
import com.ciicsh.gto.afsupportcenter.healthmedical.entity.dto.SupplyMedicalAcceptanceDTO;
import com.ciicsh.gto.afsupportcenter.healthmedical.entity.po.SupplyMedicalAcceptance;

import java.util.List;

/**
 * <p>
 * 补充医疗受理单表 Mapper 接口
 * </p>
 *
 * @author xiweizhen
 */
public interface SupplyMedicalAcceptanceMapper extends BaseMapper<SupplyMedicalAcceptance> {

    /**
     * 补充医疗分页查询
     *
     * @param page
     * @param supplyMedicalAcceptanceDTO
     * @return
     */
    List<SupplyMedicalAcceptance> queryAcceptancePage(Page<SupplyMedicalAcceptance> page, SupplyMedicalAcceptanceDTO supplyMedicalAcceptanceDTO);

    /**
     * 查询汇总信息
     *
     * @param supplyMedicalAcceptanceDTO
     * @return
     */
    AcceptanceStatisticsBO queryAcceptanceTotal(SupplyMedicalAcceptanceDTO supplyMedicalAcceptanceDTO);
}