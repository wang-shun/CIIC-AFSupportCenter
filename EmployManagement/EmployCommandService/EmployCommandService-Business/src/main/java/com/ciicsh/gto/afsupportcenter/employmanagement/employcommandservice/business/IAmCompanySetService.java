package com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.business;

import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.bo.AmCompanySetBO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.entity.AmCompanySet;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 公司特殊情况设置表 服务类
 * </p>
 *
 * @author xsj
 * @since 2018-03-21
 */
public interface IAmCompanySetService extends IService<AmCompanySet> {

    AmCompanySetBO queryAmCompanySet(AmCompanySetBO amCompanySetBO);
}