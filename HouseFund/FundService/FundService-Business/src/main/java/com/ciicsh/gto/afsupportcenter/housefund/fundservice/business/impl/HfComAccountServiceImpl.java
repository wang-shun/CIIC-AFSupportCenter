package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.ComAccountExtBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.ComAccountParamExtBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.ComAccountTransBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfComAccountService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dao.HfComAccountMapper;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfComAccount;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 企业公积金账户：存储中智大库、中智外包、独立户企业的账号，含基本公积金和补充公积金
HF：House Fun 服务实现类
 * </p>
 */
@Service
public class HfComAccountServiceImpl extends ServiceImpl<HfComAccountMapper, HfComAccount> implements HfComAccountService {

    /**
     * 查询企业社保账户信息表
     *
     * @param extBo
     * @return
     */
    @Override
    public List<ComAccountExtBo> getHfComAccountList(ComAccountParamExtBo extBo) {
        return baseMapper.getHfComAccountList(extBo);
    }

    @Override
    public List<ComAccountTransBo> queryComAccountTransBoList(ComAccountTransBo comAccountTransBo) {
        return baseMapper.queryComAccountTransBoList(comAccountTransBo);
    }
}