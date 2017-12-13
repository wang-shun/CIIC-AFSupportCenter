package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.EmpEmployee;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 雇员基本信息表
雇员的公共信息存放在此表，此表的雇员信息为唯一数据，AF、BPO、FC雇员信息分别在各自的扩展信息表中 服务类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-11
 */
public interface IEmpEmployeeService extends IService<EmpEmployee> {

}
