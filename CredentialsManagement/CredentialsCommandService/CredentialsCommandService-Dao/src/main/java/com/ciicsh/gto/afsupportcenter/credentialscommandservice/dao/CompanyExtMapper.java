package com.ciicsh.gto.afsupportcenter.credentialscommandservice.dao;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ciicsh.gto.afsupportcenter.credentialscommandservice.entity.po.CompanyExt;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
  * 客户数据维护（客户扩展表） Mapper 接口
 * </p>
 *
 * @author guwei
 * @since 2018-01-15
 */
@Repository
public interface CompanyExtMapper extends BaseMapper<CompanyExt> {

    /**
     * 根据客户code查询办证信息
     * @param companyId
     * @return
     */
    List<CompanyExt> selectBYCompanyId(String companyId);

    /**
     * 根据客户code和办证类型更新办证信息
     * @param companyExt
     * @return
     */
    int updateBycompanyIdAndType(CompanyExt companyExt);
}