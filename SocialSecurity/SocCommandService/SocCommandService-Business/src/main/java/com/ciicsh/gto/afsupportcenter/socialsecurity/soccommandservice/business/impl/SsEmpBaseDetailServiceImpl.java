package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsEmpBaseDetail;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dao.SsEmpBaseDetailMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.SsEmpBaseDetailService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 雇员社保汇缴基数明细表，
 * 该表细化到每一个社保险种的月度段的基数、比例、公司缴纳金额、个人缴纳金额 服务实现类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@Service
public class SsEmpBaseDetailServiceImpl extends ServiceImpl<SsEmpBaseDetailMapper, SsEmpBaseDetail> implements SsEmpBaseDetailService {

    @Override
    public void saveForSsEmpBaseDetail(List<SsEmpBaseDetail> details, SsEmpBaseDetail detail) {
        // 删除 old
        baseMapper.delete(new EntityWrapper(detail));
        // 保存 new
        if (CollectionUtils.isNotEmpty(details)) {
            this.insertBatch(details);
        }
    }
}
