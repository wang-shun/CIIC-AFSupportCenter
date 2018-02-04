package com.ciicsh.gto.afsupportcenter.healthmedical.business;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.ciicsh.gto.afsupportcenter.healthmedical.entity.bo.AcceptanceStatisticsBO;
import com.ciicsh.gto.afsupportcenter.healthmedical.entity.dto.SupplyMedicalAcceptanceDTO;
import com.ciicsh.gto.afsupportcenter.healthmedical.entity.po.SupplyMedicalAcceptance;

/**
 * <p>
 * 补充医疗受理单表 服务类
 * </p>
 *
 * @author xiweizhen
 */
public interface SupplyMedicalAcceptanceService extends IService<SupplyMedicalAcceptance> {
    /**
     * 补充医疗分页查询
     *
     * @param page
     * @param supplyMedicalAcceptanceDTO
     * @return
     */
    Page<SupplyMedicalAcceptance> queryAcceptancePage(Page<SupplyMedicalAcceptance> page, SupplyMedicalAcceptanceDTO supplyMedicalAcceptanceDTO);

    /**
     * 同步备份表数据到业务表
     */
    void acceptance();

    /**
     * 定时同步智灵通受理单数据
     *
     * @return
     */
    boolean syncAcceptanceSummaryDetail();

    /**
     * 查询统计信息
     *
     * @param supplyMedicalAcceptanceDTO
     */
    AcceptanceStatisticsBO queryAcceptanceTotal(SupplyMedicalAcceptanceDTO supplyMedicalAcceptanceDTO);
}