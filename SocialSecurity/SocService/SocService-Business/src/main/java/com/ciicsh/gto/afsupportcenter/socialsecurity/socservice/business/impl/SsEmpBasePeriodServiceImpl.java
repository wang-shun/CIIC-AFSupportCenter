package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsEmpBasePeriodBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsEmpBasePeriodService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dao.SsEmpBaseDetailMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dao.SsEmpBasePeriodMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsEmpBaseDetail;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsEmpBasePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 雇员正常汇缴社保的基数分段表(每段一个基数)， 每次社保基数变更（比如年调）或补缴都会更新这张表 服务实现类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@Service
public class SsEmpBasePeriodServiceImpl extends ServiceImpl<SsEmpBasePeriodMapper, SsEmpBasePeriod> implements SsEmpBasePeriodService {

    @Autowired
    private SsEmpBaseDetailMapper ssEmpBaseDetailMapper;

    @Transactional
    @Override
    public void saveForEmpTaskId(List<SsEmpBasePeriod> periods, Long empTaskId) {
        SsEmpBasePeriod period = new SsEmpBasePeriod();
        period.setEmpTaskId(empTaskId);
        // 删除 old
        baseMapper.delete(new EntityWrapper(period));
        // 保存 new
        if (CollectionUtils.isNotEmpty(periods)) {
            this.insertBatch(periods);
        }
    }

    @Transactional
    @Override
    public void deleteByEmpTaskId(Long empTaskId) {
        SsEmpBasePeriod period = new SsEmpBasePeriod();
        period.setEmpTaskId(empTaskId);
        EntityWrapper wrapper = new EntityWrapper(period);
        List<SsEmpBasePeriod> periods = baseMapper.selectList(wrapper);

        periods.forEach(p -> {
            SsEmpBaseDetail detail = new SsEmpBaseDetail();
            detail.setEmpArchiveId(p.getEmpArchiveId());
            detail.setEmpBasePeriodId(p.getEmpBasePeriodId());
            ssEmpBaseDetailMapper.delete(new EntityWrapper(detail));
            baseMapper.delete(wrapper);
        });
    }

    @Transactional
    @Override
    public void saveAdjustmentPeriod(SsEmpBasePeriod ssEmpBasePeriod, List<SsEmpBasePeriod> newEmpBasePeriodList) {

        SsEmpBasePeriod period = new SsEmpBasePeriod();
        //截止endMonth
        baseMapper.updateById(ssEmpBasePeriod);
        //追加调整的月份
        if (CollectionUtils.isNotEmpty(newEmpBasePeriodList)) {
            this.insertBatch(newEmpBasePeriodList);
        }
    }

    @Override
    public List<SsEmpBasePeriod> queryPeriodByEmpArchiveId(String empArchiveId) {
        return baseMapper.queryPeriodByEmpArchiveId(empArchiveId);
    }

    @Override
    public void updateEndMonthById(SsEmpBasePeriod ssEmpBasePeriod) {
        baseMapper.updateEndMonthById(ssEmpBasePeriod);
    }

    @Override
    public void saveBackPeriod(List<SsEmpBasePeriod> newEmpBasePeriodList) {
        //添加补缴 福利段
        if (CollectionUtils.isNotEmpty(newEmpBasePeriodList)) {
            this.insertBatch(newEmpBasePeriodList);
        }
    }

    @Override
    public void updateEndMonAndHandleMon(SsEmpBasePeriod ssEmpBasePeriod) {
        baseMapper.updateEndMonAndHandleMon(ssEmpBasePeriod);
    }

    @Override
    public Integer updateReductionById(SsEmpBasePeriod ssEmpBasePeriod) {
        return baseMapper.updateReductionById(ssEmpBasePeriod);
    }

    @Override
    public List<SsEmpBasePeriod> queryPeriodByEmployeeIdAndCompanyId(String companyId, String employeeId) {
        return baseMapper.queryPeriodByEmployeeIdAndCompanyId(companyId, employeeId);
    }

    @Override
    public List<SsEmpBasePeriodBO> getEmpBasePeriodByIntervalYear(String companyId, String employeeId, Integer intervalYear) {
        return baseMapper.getEmpBasePeriodByIntervalYear(companyId, employeeId, intervalYear);
    }
}
