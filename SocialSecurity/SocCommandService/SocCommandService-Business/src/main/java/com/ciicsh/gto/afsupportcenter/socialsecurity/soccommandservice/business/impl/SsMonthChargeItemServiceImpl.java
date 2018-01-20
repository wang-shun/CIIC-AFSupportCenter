package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.impl;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.bo.SsMonthChargeItemBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.bo.SsMonthEmpChangeDetailBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsMonthChargeItem;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dao.SsMonthChargeItemMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.ISsMonthChargeItemService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 雇员月度费用明细项目 服务实现类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-23
 */
@Service
public class SsMonthChargeItemServiceImpl extends ServiceImpl<SsMonthChargeItemMapper, SsMonthChargeItem> implements ISsMonthChargeItemService {

    @Override
    public List<SsMonthChargeItemBO> queryEmlpyeeMonthFeeDetail(SsMonthChargeItemBO ssMonthChargeItemBO) {
        List<SsMonthChargeItemBO> ssMonthChargeItemBOList =baseMapper.queryEmlpyeeMonthFeeDetail(ssMonthChargeItemBO);
         return ssMonthChargeItemBOList;
    }


}
