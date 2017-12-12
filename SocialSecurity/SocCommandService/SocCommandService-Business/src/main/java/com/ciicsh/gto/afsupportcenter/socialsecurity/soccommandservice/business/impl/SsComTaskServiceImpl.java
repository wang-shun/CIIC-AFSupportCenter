package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.ISsComTaskService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dao.SsAccountRatioMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dao.SsComAccountMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dao.SsComTaskMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dto.SsComTaskDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsAccountRatio;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsComAccount;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsComTask;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 独立库客户任务单 服务实现类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@Service
public class SsComTaskServiceImpl extends ServiceImpl<SsComTaskMapper, SsComTask> implements ISsComTaskService {

    @Autowired
    public SsComTaskMapper ssComTaskMapper;
    @Autowired
    public SsComAccountMapper sComAccountMapper;
    @Autowired
    public SsAccountRatioMapper ssAccountRatioMapper;
    /**
     * 获得企业任务单 未处理
     * xsj
     * @return
     */
    @Override
    public PageRows<SsComTaskDTO> queryNoProgressCompanyTask(PageInfo pageInfo){
        //将json对象转 DTO对象
        SsComTaskDTO  ssComTaskDTO= pageInfo.toJavaObject(SsComTaskDTO.class);
        return PageKit.doSelectPage(pageInfo,()-> baseMapper.queryNoProgressCompanyTask(ssComTaskDTO));
    }
    /**
     * 获得企业任务单 处理中
     * xsj
     * @return
     */
    @Override
    public PageRows<SsComTaskDTO> queryProgressingCompanyTask(PageInfo pageInfo){
        //将json对象转 DTO对象
        SsComTaskDTO  ssComTaskDTO= pageInfo.toJavaObject(SsComTaskDTO.class);
        return PageKit.doSelectPage(pageInfo,()-> baseMapper.queryProgressingCompanyTask(ssComTaskDTO));
    }

    /**
     * 获得企业任务单 处理中
     * xsj
     * @return
     */
    @Override
    public PageRows<SsComTaskDTO> queryFinshedCompanyTask(PageInfo pageInfo){
        //将json对象转 DTO对象
        SsComTaskDTO  ssComTaskDTO= pageInfo.toJavaObject(SsComTaskDTO.class);
        return PageKit.doSelectPage(pageInfo,()-> baseMapper.queryFinshedCompanyTask(ssComTaskDTO));
    }

    /**
     * 获得企业任务单 批退
     * xsj
     * @return
     */
    @Override
    public PageRows<SsComTaskDTO> queryRefusedCompanyTask(PageInfo pageInfo){
        //将json对象转 DTO对象
        SsComTaskDTO  ssComTaskDTO= pageInfo.toJavaObject(SsComTaskDTO.class);
        return PageKit.doSelectPage(pageInfo,()-> baseMapper.queryRefusedCompanyTask(ssComTaskDTO));
    }
    /**
     * 批量修改批退任务
     * xsj
     * @return
     */
    @Override
    public boolean updatePatchRefuseTask(List<SsComTask> ssComTaskList){
        //
        return  baseMapper.updatePatchRefuseTask(ssComTaskList);
    }

    //查询企业信息和材料
    public SsComTaskDTO queryComInfoAndMaterial(SsComTaskDTO ssComTaskDTO){
        //
        return  baseMapper.queryComInfoAndMaterial(ssComTaskDTO);
    }

    /**
     *  查询企业信息和 前道传过来的JSON（包含社保截止和付款方式）
     * @param ssComTask
     * @return
     */
    public SsComTaskDTO queryComInfoAndPayWay(SsComTask ssComTask){

        return baseMapper.queryComInfoAndPayWay(ssComTask);
    }
    /**
     * 企业任务开户办理 在内做事物
     */
    public boolean addOrUpdateCompanyTask(SsComTask ssComTask, SsComAccount ssComAccount, SsAccountRatio ssAccountRatio){
            //如果 账户ID为空 则添加  否则修改
            if (null == ssComAccount.getComAccountId()) {
                ssComAccount.setActive(true);
                ssComAccount.setCreatedTime(LocalDateTime.now());
                ssComAccount.setCreatedBy("xsj");
                sComAccountMapper.insert(ssComAccount);
                //将账户ID 赋给 任务单
                ssComTask.setComAccountId(ssComAccount.getComAccountId());
            }
            else{
                sComAccountMapper.updateById(ssComAccount);
            }
            //修改任务详情
            baseMapper.updateById(ssComTask);

            if(null==ssAccountRatio.getSsAccountRatioId()){
                //工伤比例变更历史表 如果没有则添加
                ssAccountRatio.setActive(true);
                ssAccountRatio.setCreatedTime(LocalDateTime.now());
                ssAccountRatio.setComAccountId(ssComAccount.getComAccountId());
                ssAccountRatio.setCreatedBy("xsj");
                ssAccountRatioMapper.insert(ssAccountRatio);
            }
            else {
                ssAccountRatioMapper.updateById(ssAccountRatio);
            }
        return true;
    }

    /**
     * 查询账户信息和材料信息
     * @param ssComTaskDTO
     * @return
     */
    public SsComTaskDTO queryAccountInfoAndMaterial(SsComTaskDTO ssComTaskDTO){

        return baseMapper.queryAccountInfoAndMaterial(ssComTaskDTO);
    }
}
