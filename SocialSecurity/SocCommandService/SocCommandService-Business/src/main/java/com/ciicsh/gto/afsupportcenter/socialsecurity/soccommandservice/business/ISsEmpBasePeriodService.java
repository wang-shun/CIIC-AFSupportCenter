package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.bo.SsEmpTaskBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsEmpBasePeriod;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 * 雇员正常汇缴社保的基数分段表(每段一个基数)， 每次社保基数变更（比如年调）或补缴都会更新这张表 服务类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
public interface ISsEmpBasePeriodService extends IService<SsEmpBasePeriod> {

    /**
     * 根据雇员任务 保存数据
     *
     * @param periods
     * @param empTask
     */
    void saveForEmpTask(List<SsEmpBasePeriod> periods, SsEmpTaskBO empTask);
}
