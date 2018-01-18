package com.ciicsh.gto.afsupportcenter.credentialscommandservice.business;


import com.baomidou.mybatisplus.service.IService;
import com.ciicsh.gto.afsupportcenter.credentialscommandservice.entity.po.OrgPolicy;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 办理机构政策 服务类
 * </p>
 *
 * @author guwei
 * @since 2018-01-15
 */
public interface OrgPolicyService extends IService<OrgPolicy> {

    /**
     * 条件查询办理机构政策列表
     * @param orgPolicy
     * @return
     */
    List<OrgPolicy> select(OrgPolicy orgPolicy);

    /**
     * 插入或更新办理机构政策
     * @param entity
     * @return
     */
    @Override
    boolean insertOrUpdate(OrgPolicy entity);

    /**
     * 删除办理机构政策
     * @param id
     * @return
     */
    boolean deleteById(Integer id);
}
