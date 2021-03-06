package com.ciicsh.gto.afsupportcenter.housefund.fundservice.dao;

import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfComTaskBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.AccountInfoBO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfComTask;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 企业任务单总表：有关于企业所有任务单的表单字段都集中记录当前表
Com：公司简写 Mapper 接口
 * </p>
 */
@Repository
public interface HfComTaskMapper extends BaseMapper<HfComTask> {

    /**
     * 保存企业任务单
     *
     * @param hfComTask
     * @return
     */
    boolean insertComTask(HfComTask hfComTask);

    /**
     * 判断企业任务单是否存在
     * @return
     */
    Integer isExistComTask(@Param("companyId") String companyId, @Param("hfType") Integer hfType, @Param("taskCategory") Integer taskCategory);

    /**
     * 获取企业任务单列表
     *
     * @param hfComTaskBo
     * @return
     */
    List<HfComTaskBo> queryCompanyTask(HfComTaskBo hfComTaskBo);
    List<HfComTaskBo> queryCompanyTaskProcessing(HfComTaskBo hfComTaskBo);

    AccountInfoBO getAccountsByCompany(@Param("companyId") String companyId,@Param("hfType") Integer hfType);
}
