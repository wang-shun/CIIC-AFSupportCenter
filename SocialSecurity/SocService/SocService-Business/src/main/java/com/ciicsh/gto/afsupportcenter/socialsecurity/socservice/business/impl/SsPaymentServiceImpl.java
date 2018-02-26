package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsPaymentBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsPaymentSrarchBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsPaymentService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dao.SsPaymentComMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dao.SsPaymentMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dto.PayapplyCompanyProxyDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dto.PayapplyEmployeeProxyDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsPayment;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsPaymentCom;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.settlementcenter.payment.cmdapi.PayapplyServiceProxy;
import com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayApplyProxyDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 社保支付批次 服务实现类
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@Service
public class SsPaymentServiceImpl extends ServiceImpl<SsPaymentMapper, SsPayment> implements SsPaymentService {
    @Autowired
    private SsPaymentComMapper ssPaymentComMapper;

    @Autowired
    private PayapplyServiceProxy payapplyServiceProxy;

    @Override
    public PageRows<SsPaymentBO> paymentQuery(PageInfo pageInfo) {
        return PageKit.doSelectPage(pageInfo, () -> baseMapper.paymentQuery(pageInfo.toJavaObject(SsPaymentBO.class)));
    }

    @Override
    public List<SsPaymentBO> showAddBatch(SsPaymentSrarchBO paymentSrarchDTO) {
        return baseMapper.searchPaymentByAccountTypeAndState(paymentSrarchDTO);
    }

    @Override
    public JsonResult<String> doApplyPay(SsPayment ssPayment) {
        JsonResult<String> json = new JsonResult<>();
        json.setCode(0);

        //暂时移出,为了查询
        String applyRemark = ssPayment.getApplyRemark();
        ssPayment.setApplyRemark(null);

        //验证
        //根据ID获取到记录
        ssPayment = baseMapper.selectOne(ssPayment);

        //验证客户费用ID是否能取到数据
        if (!Optional.ofNullable(ssPayment).isPresent()) {
            json.setCode(2);
            json.setMessage("没有与选中列匹配的支付批次");
            return json;
        }

        //验证状态,只有3 ,可付 5,内部审批批退 状态的数据可申请支付
        if (3 != ssPayment.getPaymentState() && 5 != ssPayment.getPaymentState() && 7 != ssPayment.getPaymentState()) {
            json.setCode(3);
            json.setMessage("只有可付和内部审批批退状态的记录可申请支付");
            return json;
        }

        //验证该批次中的社保账户中所有的客户费用明细是否都在该批次下
        //取出批次中所有的社保账户
        List<Long> accountList = ssPaymentComMapper.getAccountIdByPaymentId(ssPayment.getPaymentId());
        //依次检验改社保账户中是否有不在改批次下的社保明细
        if (Optional.ofNullable(accountList).isPresent()) {
            //放入参数
            SsPaymentCom ssPaymentCom = new SsPaymentCom();
            ssPaymentCom.setPaymentId(ssPayment.getPaymentId());
            ssPaymentCom.setPaymentMonth(ssPayment.getPaymentMonth());
            for (int i = 0; i < accountList.size(); i++) {
                //不在该批次的明细条数
                ssPaymentCom.setComAccountId(accountList.get(i));
                int notInPaymentCount = ssPaymentComMapper.getPaymentComCountNotInPayment(ssPaymentCom);
                if (notInPaymentCount > 0) {
                    json.setCode(3);
                    json.setMessage("企业社保账户:" + accountList.get(i) + ";该账户下有客服费用未在本批次中,不能申请支付");
                    return json;
                }
            }
        }

        //验证结束

        //执行业务
        //将批次状态改为申请中
        ssPayment.setPaymentState(4);
        ssPayment.setApplyRemark(applyRemark);
        ssPayment.setRequestUser("张三");
        ssPayment.setRequestDate(LocalDate.now());
        baseMapper.updateById(ssPayment);

        //将批次下的客户费用明细的状态也改为申请中
        //取出批次下所有的客户费用
        List<SsPaymentCom> ssPaymentComList = ssPaymentComMapper.getPaymentComByPaymentId(ssPayment.getPaymentId());
        if (Optional.ofNullable(ssPaymentComList).isPresent()) {
            for (int i = 0; i < ssPaymentComList.size(); i++) {
                //修改状态为申请中
                SsPaymentCom ssPaymentCom = ssPaymentComList.get(i);
                ssPaymentCom.setPaymentState(4);
                ssPaymentCom.setModifiedBy("张三");
                ssPaymentCom.setModifiedTime(LocalDateTime.now());
                ssPaymentComMapper.updateById(ssPaymentCom);
            }
        }
        return json;
    }

    @Override
    public JsonResult<String> doDelPayment(SsPayment ssPayment) {
        JsonResult<String> json = new JsonResult<>();
        json.setCode(0);

        //验证
        //根据ID获取到记录
        ssPayment = baseMapper.selectOne(ssPayment);

        //验证客户费用ID是否能取到数据
        if (!Optional.ofNullable(ssPayment).isPresent()) {
            json.setCode(2);
            json.setMessage("没有与选中列匹配的支付批次");
            return json;
        }

        //验证状态,只有3 ,可付 5,内部审批批退 状态的数据可申请支付
        if (3 != ssPayment.getPaymentState() && 5 != ssPayment.getPaymentState()) {
            json.setCode(3);
            json.setMessage("只有可付和内部审批批退状态的记录可删除");
            return json;
        }
        //验证结束
        //将批次逻辑删除
        ssPayment.setActive(false);
        ssPayment.setModifiedBy("张三");
        ssPayment.setModifiedTime(LocalDateTime.now());
        baseMapper.updateById(ssPayment);

        //将批次下的客户费用明细移出批次
        //取出批次下所有的客户费用
        List<SsPaymentCom> ssPaymentComList = ssPaymentComMapper.getPaymentComByPaymentId(ssPayment.getPaymentId());
        if (Optional.ofNullable(ssPaymentComList).isPresent()) {
            for (int i = 0; i < ssPaymentComList.size(); i++) {
                //修改状态为申请中
                SsPaymentCom ssPaymentCom = ssPaymentComList.get(i);
                ssPaymentCom.setPaymentId(null);
                ssPaymentCom.setModifiedBy("张三");
                ssPaymentCom.setModifiedTime(LocalDateTime.now());
                ssPaymentComMapper.updateAllColumnById(ssPaymentCom);
            }
        }
        return json;

    }

    @Override
    public JsonResult<String> addPayment(SsPayment ssPayment) {
        JsonResult<String> json = new JsonResult<>();
        json.setCode(0);

        SsPayment newSsPayment = new SsPayment();
        //传入值
        newSsPayment.setPaymentMonth(ssPayment.getPaymentMonth());
        newSsPayment.setPaymentBatchNum(ssPayment.getPaymentBatchNum());
        newSsPayment.setAccountType(ssPayment.getAccountType());
        //默认值
        newSsPayment.setPaymentState(3);
        newSsPayment.setTotalEmpCount(0);
        newSsPayment.setTotalCom(0);
        newSsPayment.setTotalAccount(0);
        newSsPayment.setTotalApplicationAmount(new BigDecimal(0));
        newSsPayment.setActive(true);
        newSsPayment.setCreatedBy("张三");
        newSsPayment.setModifiedBy("张三");
        newSsPayment.setCreatedTime(LocalDateTime.now());

        baseMapper.insert(newSsPayment);

        return json;
    }

    @Override
    public JsonResult<String> doReviewdePass(SsPayment ssPayment) {
        JsonResult<String> json = new JsonResult<>();
        json.setCode(0);

        //验证
        //根据ID获取到记录
        ssPayment = baseMapper.selectOne(ssPayment);

        //验证客户费用ID是否能取到数据
        if (ssPayment == null) {
            json.setCode(2);
            json.setMessage("没有与选中数据匹配的支付批次");
            return json;
        }

        //验证状态,只有3 ,可付 5,内部审批批退 状态的数据可申请支付
        if (4 != ssPayment.getPaymentState()) {
            json.setCode(3);
            json.setMessage("只有申请中状态的批次能审核通过");
            return json;
        }
        //验证结束,调用外部审批接口
        PayApplyProxyDTO resDto = financePayApi(ssPayment);
        com.ciicsh.gto.settlementcenter.payment.cmdapi.common.JsonResult<PayApplyProxyDTO> jsRes =
            payapplyServiceProxy.addShSocialInsurancePayApply(resDto);

        json.setCode(Integer.parseInt(jsRes.getCode()));
        json.setMessage(jsRes.getMsg());
        return json;
    }

    /**
     * 执行调用财务API
     *
     * @param ssPayment
     */
    private PayApplyProxyDTO financePayApi(SsPayment ssPayment) {
        PayApplyProxyDTO dto = new PayApplyProxyDTO();
        List<PayapplyCompanyProxyDTO> paymentComList = baseMapper.getPaymentComList(ssPayment.getPaymentId(),
            ssPayment.getPaymentMonth());
        List<PayapplyEmployeeProxyDTO> paymentEmpList = baseMapper.getPaymentEmpList(ssPayment.getPaymentId(),
            ssPayment.getPaymentMonth());

        List<com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyCompanyProxyDTO> companyDtos = paymentComList.stream().map(x->{
            com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyCompanyProxyDTO pdto = new com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyCompanyProxyDTO();
            BeanUtils.copyProperties(x,pdto);
            return pdto;
        }).collect(Collectors.toList());

        List<com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyEmployeeProxyDTO> employeeDtos = paymentEmpList.stream().map(x->{
            com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyEmployeeProxyDTO pdto = new com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayapplyEmployeeProxyDTO();
            BeanUtils.copyProperties(x,pdto);
            return pdto;
        }).collect(Collectors.toList());

        dto.setCompanyList(companyDtos);
        dto.setEmployeeList(employeeDtos);

        dto.setDepartmentName("福利保障部社保");
        dto.setIsFinancedept(0);
        dto.setBusinessType(1);//业务类型
        dto.setPayWay(3);//转账
        dto.setPayAmount(ssPayment.getTotalApplicationAmount());//申请支付金额
        dto.setReceiver("社保中心");//收款方名称
        dto.setApplyer(ssPayment.getRequestUser());  //申请人
        dto.setApplyDate(StringUtil.getNow());//申请日期
        dto.setBusinessPkId(ssPayment.getPaymentId());//业务方主键ID(整型)

        //支付独立社保费用+支付月份  1 大库、2 外包、3独立户
        if (ssPayment.getAccountType() == 1) {
            dto.setPayReason("支付大库社保费用" + ssPayment.getPaymentMonth());
        } else if (ssPayment.getAccountType() == 2) {
            dto.setPayReason("支付外包社保费用" + ssPayment.getPaymentMonth());
        } else if (ssPayment.getAccountType() == 3) {
            dto.setPayReason("支付独立户社保费用" + ssPayment.getPaymentMonth());
        }
        dto.setPayPurpose(dto.getPayReason());

        return dto;
    }

    @Override
    public JsonResult<String> doRejection(SsPayment ssPayment) {
        JsonResult<String> json = new JsonResult<>();
        json.setCode(0);
        String rejectionRemark = ssPayment.getRejectionRemark();
        //暂时移出,为了查询
        ssPayment.setRejectionRemark(null);

        //验证
        //根据ID获取到记录
        ssPayment = baseMapper.selectOne(ssPayment);

        //验证客户费用ID是否能取到数据
        if (!Optional.ofNullable(ssPayment).isPresent()) {
            json.setCode(2);
            json.setMessage("没有与选中列匹配的支付批次");
            return json;
        }

        //验证状态,只有3 ,可付 5,内部审批批退 状态的数据可申请支付
        if (4 != ssPayment.getPaymentState()) {
            json.setCode(3);
            json.setMessage("只有申请中状态的批次能批退");
            return json;
        }


        //验证结束

        //执行业务
        //将批次状态改为内部审批批退
        ssPayment.setRejectionRemark(rejectionRemark);
        ssPayment.setPaymentState(5);
        ssPayment.setRequestUser("张三");
        ssPayment.setRequestDate(LocalDate.now());
        ssPayment.setModifiedBy("张三");
        ssPayment.setModifiedTime(LocalDateTime.now());
        //组装批退历史
        String rejectionHis = ssPayment.getRejectionHis();
        if (!Optional.ofNullable(rejectionHis).isPresent()) {
            rejectionHis = "";
        }
        /**
         * 批退历史备份格式
         [
         {
         总雇员数：
         账户总数：
         客户总数：
         申请总金额：
         批退备注：
         批退人：
         批退时间：
         },
         ]
         */
        String newRejectionHis = "{" + "总雇员数：" + ssPayment.getTotalEmpCount() + " ;"
            + "账户总数：" + ssPayment.getTotalAccount() + " ;"
            + "客户总数：" + ssPayment.getTotalCom() + " ;"
            + "申请总金额：" + ssPayment.getTotalApplicationAmount() + " ;"
            + "批退备注：" + ssPayment.getRejectionRemark() + " ;"
            + "批退人：" + ssPayment.getRequestUser() + " ;"
            + "批退时间：" + ssPayment.getRequestDate() + " ;"
            + "}";
        rejectionHis = rejectionHis + newRejectionHis;
        ssPayment.setRejectionHis(rejectionHis);
        baseMapper.updateById(ssPayment);

        //将批次下的客户费用明细的状态也改为内部审批批退
        //取出批次下所有的客户费用
        List<SsPaymentCom> ssPaymentComList = ssPaymentComMapper.getPaymentComByPaymentId(ssPayment.getPaymentId());
        if (Optional.ofNullable(ssPaymentComList).isPresent()) {
            for (int i = 0; i < ssPaymentComList.size(); i++) {
                //修改状态为内部审批批退
                SsPaymentCom ssPaymentCom = ssPaymentComList.get(i);
                ssPaymentCom.setPaymentState(5);
                ssPaymentCom.setModifiedBy("张三");
                ssPaymentCom.setModifiedTime(LocalDateTime.now());
                ssPaymentComMapper.updateById(ssPaymentCom);
            }
        }
        return json;
    }
}