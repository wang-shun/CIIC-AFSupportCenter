package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dao;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsEmpTaskFront;
import com.baomidou.mybatisplus.mapper.BaseMapper;

/**
 * <p>
 * 雇员任务单前道传递信息,创建任务单的同时，就要把前道的传递信息复制到这表，当前表复制前道cmy_af_emp_socia Mapper 接口
 * </p>
 */
public interface SsEmpTaskFrontMapper extends BaseMapper<SsEmpTaskFront> {

}
