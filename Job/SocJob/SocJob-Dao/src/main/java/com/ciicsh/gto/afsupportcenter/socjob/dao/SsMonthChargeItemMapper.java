package com.ciicsh.gto.afsupportcenter.socjob.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ciicsh.gto.afsupportcenter.socjob.entity.SsMonthChargeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;


/**
 * <p>
 * 雇员月度费用明细项目 Mapper 接口
 * </p>
 */

@Mapper
@Component
public interface SsMonthChargeItemMapper extends BaseMapper<SsMonthChargeItem> {

    /**
     * 根据monthChargeId 删除雇员月度费用明细项目信息
     * @param monthChargeId 雇员月度费用明细ID
     * @return 返回删除影响的行数
     */
    Integer delByMonthChargeId(@Param("monthChargeId")long monthChargeId);

}
