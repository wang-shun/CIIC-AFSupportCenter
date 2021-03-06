package com.ciicsh.gto.afsupportcenter.socjob.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.ciicsh.gto.RedisManager;
import com.ciicsh.gto.afsupportcenter.socjob.dao.*;
import com.ciicsh.gto.afsupportcenter.socjob.entity.*;
import com.ciicsh.gto.afsupportcenter.socjob.entity.bo.PaymentDetailBO;
import com.ciicsh.gto.afsupportcenter.socjob.entity.custom.SsAccountComExt;
import com.ciicsh.gto.afsupportcenter.socjob.entity.custom.SsEmpBaseArchiveExt;
import com.ciicsh.gto.afsupportcenter.socjob.entity.custom.SsEmpBasePeriodExt;
import com.ciicsh.gto.afsupportcenter.socjob.entity.custom.SsMonthChargeExt;
import com.ciicsh.gto.afsupportcenter.socjob.service.SsPaymentComService;
import com.ciicsh.gto.afsupportcenter.socjob.service.enums.ComputeType;
import com.ciicsh.gto.afsupportcenter.socjob.service.enums.CostCategory;
import com.ciicsh.gto.afsupportcenter.socjob.util.CommonUtils;
import com.ciicsh.gto.afsupportcenter.util.CalculateSocialUtils;
import com.ciicsh.gto.afsupportcenter.util.DateUtil;
import com.ciicsh.gto.afsupportcenter.util.kafkaMessage.SocReportMessage;
import com.ciicsh.gto.util.ExpireTime;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by houwanhua on 2018/1/11.
 */
@Service
public class SsPaymentComServiceImpl implements SsPaymentComService {

    @Autowired
    private SsComAccountMapper accountMapper;

    @Autowired
    private SsAccountComRelationMapper comRelationMapper;

    @Autowired
    private SsPaymentComMapper paymentComMapper;

    @Autowired
    private SsMonthChargeMapper monthChargeMapper;

    @Autowired
    private SsMonthChargeItemMapper monthChargeItemMapper;

    @Autowired
    private SsEmpBasePeriodMapper empBasePeriodMapper;

    @Autowired
    private SsEmpBaseDetailMapper empBaseDetailMapper;

    @Autowired
    private SsEmpBaseAdjustDetailMapper empBaseAdjustDetailMapper;

    @Autowired
    private SsMonthEmpChangeMapper monthEmpChangeMapper;

    @Autowired
    private SsMonthEmpChangeDetailMapper monthEmpChangeDetailMapper;

    @Autowired
    private SsPaymentDetailMapper paymentDetailMapper;

    @Autowired
    private SsCalcSettingMapper ssCalcSettingMapper;

    @Autowired
    private SsPaymentDetailComMapper paymentDetailComMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void generateSocPaymentInfo(String paymentMonth) throws Exception {
        //根据社保参保户登记码获取待生成的列表数据
        List<SsAccountComExt> accountComExts = accountMapper.getSsComAccounts();
        if (null != accountComExts && accountComExts.size() > 0) {
            accountComExts.forEach(accountComExt -> {
                this.generateInfo(accountComExt, paymentMonth);
            });
        }
    }
    //数据导入，客户要求先生成4个月的支付数据
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void generateSocPaymentInfoForImpData(String paymentMonth) throws Exception {
        //根据社保参保户登记码获取待生成的列表数据
        List<SsAccountComExt> accountComExts = accountMapper.getSsComAccounts();
        if (null != accountComExts && accountComExts.size() > 0) {
            accountComExts.forEach(accountComExt -> {
                this.generateInfoForImpData(accountComExt, paymentMonth);
            });
        }
    }
    private void generateInfoForImpData(SsAccountComExt accountComExt, String paymentMonth) {
        //是否存在支付信息
        Integer val = paymentComMapper.ifExistPayment(accountComExt.getComAccountId(), paymentMonth);
        if (val <= 0) {
            //新增支付信息
            addPaymentCom(accountComExt, paymentMonth);
        }
        /*****生成雇员社保明细****/
        //如果数据已经存在，先删除已经存在的数据(标准数据)
        String ssMonth = DateUtil.plusMonth(paymentMonth, 1);

        /******生成变更汇总表*****/
        generateMonthEmpChange(accountComExt, paymentMonth,"system");

        /*****生成社保通知书*****/
        generatePaymentDetail(accountComExt, paymentMonth);

        /*****更新paymentCom表中的合计金额*****/
        paymentComMapper.updateSsMonthChargeTotalAmount(accountComExt.getComAccountId(), paymentMonth);

    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void generateSocPaymentInfo(Long comAccountId, String paymentMonth) throws Exception {
        List<SsAccountComExt> accountComExts = accountMapper.getSsComAccount(comAccountId);
        if (null != accountComExts && accountComExts.size() > 0) {
            accountComExts.forEach(accountComExt -> {
                this.generateInfo(accountComExt, paymentMonth);
            });
        }
    }


    private void generateInfo(SsAccountComExt accountComExt, String paymentMonth) {
        //是否存在支付信息
        Integer val = paymentComMapper.ifExistPayment(accountComExt.getComAccountId(), paymentMonth);
        if (val <= 0) {
            //新增支付信息
            addPaymentCom(accountComExt, paymentMonth);
        }
        /*****生成雇员社保明细****/
        //如果数据已经存在，先删除已经存在的数据(标准数据)
        String ssMonth = DateUtil.plusMonth(paymentMonth, 1);
        this.delMonthChargeInfos(accountComExt.getComAccountId(), ssMonth, 1);
        //生成标准明细
//        List<SsEmpBaseArchiveExt> empBaseArchiveExts = empBasePeriodMapper.getEmpBaseArchiveExts(accountComExt.getComAccountId(), paymentMonth);
//        if (null != empBaseArchiveExts && empBaseArchiveExts.size() > 0) {
//            empBaseArchiveExts.forEach(ext -> this.createStandardMonthChange(ext, paymentMonth));
//        }


        List<SsMonthCharge> ssMonthChargesList = monthChargeMapper.getSsMonthChargeList(accountComExt.getComAccountId(), paymentMonth);
        if (null != ssMonthChargesList && ssMonthChargesList.size() > 0) {

            ssMonthChargesList.forEach(ext -> {
                ext.setSsMonthBelong(ssMonth);
                ext.setSsMonth(ssMonth);
                ext.setCreatedBy("system");
                ext.setModifiedBy("system");
                ext.setCreatedTime(LocalDateTime.now());
                ext.setModifiedTime(LocalDateTime.now());
                ext.setEmpPaymentStatus(0);
                ext.setModifiedBy("system");
                this.createStandardMonthCharge(ext);
            });
        }


        /******生成变更汇总表*****/
       // generateMonthEmpChange(accountComExt, paymentMonth,"system");

        /*****生成社保通知书*****/
        generatePaymentDetail(accountComExt, paymentMonth);

//        /*****更新paymentCom表中的合计金额*****/
//        paymentComMapper.updateSsMonthChargeTotalAmount(accountComExt.getComAccountId(), paymentMonth);
        //  }
    }

    /**
     * 生成变更汇总表
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void generateMonthEmpChangeReport(SocReportMessage message) throws Exception {
        List<SsAccountComExt> accountComExts = accountMapper.getSsComAccount(message.getComAccountId());
        if (null != accountComExts && accountComExts.size() > 0) {
            accountComExts.forEach(accountComExt -> {
                this.generateMonthEmpChange(accountComExt, message.getSsMonth(),message.getLastComputeUser());
            });
        }
    }

    /**
     * 生成社保通知书
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void generatePaymentDetailReport(Long comAccountId, String paymentMonth) throws Exception {
        List<SsAccountComExt> accountComExts = accountMapper.getSsComAccount(comAccountId);
        if (null != accountComExts && accountComExts.size() > 0) {
            accountComExts.forEach(accountComExt -> {
                this.generatePaymentDetail(accountComExt, paymentMonth);
            });
        }
    }

    private void generateMonthEmpChange(SsAccountComExt accountComExt, String paymentMonth,String userName) {
        // 获取除退账之外的雇员社保明细扩展信息
        List<SsMonthChargeExt> allMonthChargeExts = monthChargeMapper.getSsMonthChargeExts(accountComExt.getComAccountId(), paymentMonth);
        //如果变更汇总表已经存在，先删除存在的数据
        this.delMonthEmpChangeInfos(accountComExt.getComAccountId(), paymentMonth);
        //生成变更汇总表
        this.createMonthEmpChange(allMonthChargeExts, accountComExt.getComAccountId(), paymentMonth,userName);
        /*****更新paymentCom表中的合计金额*****/
        paymentComMapper.updateSsMonthChargeTotalAmount(accountComExt.getComAccountId(), paymentMonth);
    }

    //生成付款通知书
    private void generatePaymentDetail(SsAccountComExt accountComExt, String paymentMonth) {
        // 获取除退账之外的雇员社保明细扩展信息
        List<SsMonthChargeExt> allMonthChargeExts = monthChargeMapper.getSsMonthChargeExts(accountComExt.getComAccountId(), paymentMonth);
        //第一步：如果已经存在数据，先删除
        paymentDetailMapper.delPaymentDetail(accountComExt.getComAccountId(), paymentMonth);
        paymentDetailComMapper.delPaymentDetailCom(accountComExt.getComAccountId(), paymentMonth);
        //第二步：生成数据
        this.createPaymentDetail(allMonthChargeExts, accountComExt.getComAccountId(), paymentMonth);
        /*****更新paymentCom表中的合计金额*****/
        paymentComMapper.updateSsMonthChargeTotalAmount(accountComExt.getComAccountId(), paymentMonth);
    }

    /**
     * 新增支付信息
     *
     * @param ext          企业社保账户扩展信息
     * @param paymentMonth 支付年月
     * @return
     */
    private boolean addPaymentCom(SsAccountComExt ext, String paymentMonth) {
        try {
            List<SsAccountComRelation> comRelations = this.getAccountComRelation(ext.getComAccountId());
            if (null != comRelations && comRelations.size() > 0) {
                comRelations.forEach(rel -> {
                    SsPaymentCom paymentCom = new SsPaymentCom();
                    paymentCom.setComAccountId(ext.getComAccountId());
                    paymentCom.setCompanyId(rel.getCompanyId());
                    paymentCom.setPaymentMonth(paymentMonth);
                    // paymentWay与paymentState状态联动
                    // paymentWay==2表示客户自付，对应paymentState状态为2（无需支付）
                    // paymentWay!=2表示我司付款（我司代付款、我司垫付），对应paymentState状态为1（未到账）
                    paymentCom.setPaymentState(ext.getPaymentWay() == 2 ? 2 : 1);
                    paymentCom.setActive(true);
                    paymentCom.setCreatedTime(LocalDateTime.now());
                    paymentCom.setCreatedBy("system");
                    paymentCom.setModifiedTime(LocalDateTime.now());
                    paymentCom.setModifiedBy("system");
                    paymentComMapper.insert(paymentCom);
                });
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 根据社保账户查询关联的客户list
     *
     * @param comAccountId
     * @return
     */
    private List<SsAccountComRelation> getAccountComRelation(Long comAccountId) {
        SsAccountComRelation comRelation = new SsAccountComRelation();
        comRelation.setComAccountId(comAccountId);
        comRelation.setActive(true);
        EntityWrapper<SsAccountComRelation> entityWrapper = new EntityWrapper<>(comRelation);
        return comRelationMapper.selectList(entityWrapper);
    }

    /**
     * 根据条件删除月度缴费明细信息以及详细信息
     *
     * @param comAccountId 企业社保账户
     * @param paymentMonth 支付年月
     */

    /**
     * 根据条件删除月度缴费明细信息以及详细信息
     *
     * @param comAccountId 企业社保账户
     * @param paymentMonth 支付年月
     * @param costCategory 1标准 2 新进 3 转入  4 补缴 5 调整 （顺调)）6 转出 7封存 8 退账 9 调整（倒调）
     */
    private void delMonthChargeInfos(long comAccountId, String paymentMonth, Integer costCategory) {
        List<SsMonthCharge> monthCharges = monthChargeMapper.getMonthChangesByCondition(comAccountId, paymentMonth, costCategory);
        if (null != monthCharges && monthCharges.size() > 0) {
            monthChargeMapper.delByCondition(comAccountId, paymentMonth, costCategory);
            monthCharges.forEach(x -> monthChargeItemMapper.delByMonthChargeId(x.getMonthChargeId()));
        }
    }

    /**
     * 根据条件删除雇员月度变更主表信息以及详细信息
     *
     * @param comAccountId 企业社保账户
     * @param paymentMonth 支付年月
     */
    private void delMonthEmpChangeInfos(long comAccountId, String paymentMonth) {
        List<SsMonthEmpChange> monthEmpChanges = monthEmpChangeMapper.getMonthEmpChangesByCondition(comAccountId, paymentMonth);
        if (null != monthEmpChanges && monthEmpChanges.size() > 0) {
            monthEmpChangeMapper.delMonthEmpChangeByfCondition(comAccountId, paymentMonth);
            monthEmpChanges.forEach(x -> monthEmpChangeDetailMapper.delByMonthEmpChangeId(x.getMonthEmpChangeId()));
        }
    }

    /**
     * 根据费用类别转换新数据
     *
     * @param comAccountId 企业社保账户
     * @param paymentMonth 支付年月
     * @return 返回新的EmpBasePeriodExt类别
     */
    private List<SsEmpBasePeriodExt> convertNewEmpBasePeriodExt(Long comAccountId, String paymentMonth) {
        List<SsEmpBasePeriodExt> newEmpBasePeriodExts = new ArrayList<>();
        List<SsEmpBasePeriodExt> empBasePeriodExts = empBasePeriodMapper.getEmpBasePeriodExts(comAccountId, paymentMonth);
        if (null != empBasePeriodExts && empBasePeriodExts.size() > 0) {
            empBasePeriodExts.forEach(ext -> {
                if (ext.getCategory() == 4) {
                    this.addEmpBasePeriodExtToList(newEmpBasePeriodExts, ext);
                } else if (ext.getCategory() == 9) {
                    this.addEmpBasePeriodExtToList(newEmpBasePeriodExts, ext);
                } else {
                    newEmpBasePeriodExts.add(ext);
                }
            });
        }
        return newEmpBasePeriodExts;
    }

    /**
     * 把新的EmpBasePeriodExt加入列表中
     *
     * @param empBasePeriodExts
     * @param ext
     */
    private void addEmpBasePeriodExtToList(List<SsEmpBasePeriodExt> empBasePeriodExts, SsEmpBasePeriodExt ext) {
        try {
            List<String> months = CommonUtils.getMonths(ext.getStartMonth(), ext.getEndMonth());
            if (null != months && months.size() > 0) {
                months.forEach(x -> empBasePeriodExts.add(cloneSsEmpBasePeriodExt(ext, x)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * 克隆SsEmpBasePeriodExt
     *
     * @param basePeriodExt 旧的SsEmpBasePeriodExt
     * @param ssMonthBelong 所属月份
     * @return 返回新的SsEmpBasePeriodExt
     */
    private SsEmpBasePeriodExt cloneSsEmpBasePeriodExt(SsEmpBasePeriodExt basePeriodExt, String ssMonthBelong) {
        SsEmpBasePeriodExt ext = new SsEmpBasePeriodExt();
        BeanUtils.copyProperties(basePeriodExt, ext);
        ext.setSsMonthBelong(ssMonthBelong);
        return ext;
    }


    /**
     * 生成标准月度缴费明细信息以及详细信息
     *
     * @param ext          雇员本地社保档案扩展信息
     * @param paymentMonth 支付年月
     */
    private void createStandardMonthChange(SsEmpBaseArchiveExt ext, String paymentMonth) {
        List<SsEmpBaseDetail> baseDetails = empBaseDetailMapper.getEmpBaseDetailsByPeriodId(ext.getEmpBasePeriodId());
        if (null != baseDetails && baseDetails.size() > 0) {
            BigDecimal totalAmount = baseDetails.stream()
                .map(i -> i.getComAmount().add(i.getComAdditionAmount()).add(i.getEmpAmount()).add(i.getEmpAdditionAmount()))
                .reduce(new BigDecimal(0), (x, y) -> x.add(y));
            SsMonthCharge monthCharge = addStandardMonthCharge(ext, paymentMonth, totalAmount);
            if (null != monthCharge) {
                baseDetails.forEach(x -> addStandardMonthChargeItem(x, monthCharge.getMonthChargeId()));
            }
        }
    }

    private void createStandardMonthCharge(SsMonthCharge ssMonthCharge) {
        Long monthChargeId = ssMonthCharge.getMonthChargeId();
        ssMonthCharge.setMonthChargeId(null);
        Long empTaskId = ssMonthCharge.getEmpTaskId();
        ssMonthCharge.setEmpTaskId(null);
        Integer costCategory = ssMonthCharge.getCostCategory();
        ssMonthCharge.setCostCategory(1);
        monthChargeMapper.insert(ssMonthCharge);

        if (costCategory != 5) {
            List<SsMonthChargeItem> ssMonthChargeItemsList = monthChargeItemMapper.getMonthChargeItemByMonthChargeId(monthChargeId);
            if (null != ssMonthChargeItemsList && ssMonthChargeItemsList.size() > 0) {
                ssMonthChargeItemsList.forEach(ssMonthChargeItem -> {
                    ssMonthChargeItem.setMonthChargeItemId(null);
                    ssMonthChargeItem.setMonthChargeId(ssMonthCharge.getMonthChargeId());
                    ssMonthChargeItem.setCreatedBy("system");
                    ssMonthChargeItem.setModifiedBy("system");
                    ssMonthChargeItem.setCreatedTime(LocalDateTime.now());
                    ssMonthChargeItem.setModifiedTime(LocalDateTime.now());
                    monthChargeItemMapper.insert(ssMonthChargeItem);
                });
            }
        } else {
            Map<String, Object> condition = new HashMap<>();
            condition.put("is_active", 1);
            condition.put("emp_task_id", empTaskId);
            condition.put("emp_archive_id", ssMonthCharge.getEmpArchiveId());
            List<SsEmpBasePeriod> list = empBasePeriodMapper.selectByMap(condition);

            if (CollectionUtils.isNotEmpty(list)) {
                for (SsEmpBasePeriod ssEmpBasePeriod : list) {
                    if (StringUtils.isEmpty(ssEmpBasePeriod.getEndMonth())) {
                        List<SsEmpBaseDetail> details = empBaseDetailMapper.getEmpBaseDetailsByPeriodId(ssEmpBasePeriod.getEmpBasePeriodId());
                        if (CollectionUtils.isNotEmpty(details)) {
                            details.forEach(detail -> {
                                SsMonthChargeItem ssMonthChargeItem = new SsMonthChargeItem();
                                ssMonthChargeItem.setMonthChargeItemId(null);
                                ssMonthChargeItem.setMonthChargeId(ssMonthCharge.getMonthChargeId());
                                ssMonthChargeItem.setSsType(detail.getSsType());
                                ssMonthChargeItem.setSsTypeName(detail.getSsTypeName());
                                ssMonthChargeItem.setComAmount(detail.getComAmount());
                                ssMonthChargeItem.setComAmountOrig(detail.getComAmountOrig());
                                ssMonthChargeItem.setEmpAmount(detail.getEmpAmount());
                                ssMonthChargeItem.setSubTotalAmount(detail.getComempAmount());
                                ssMonthChargeItem.setComRatio(detail.getComRatio());
                                ssMonthChargeItem.setEmpRatio(detail.getEmpRatio());
                                ssMonthChargeItem.setCreatedBy("system");
                                ssMonthChargeItem.setModifiedBy("system");
                                ssMonthChargeItem.setCreatedTime(LocalDateTime.now());
                                ssMonthChargeItem.setModifiedTime(LocalDateTime.now());
                                monthChargeItemMapper.insert(ssMonthChargeItem);
                            });
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 添加标准月度缴费明细
     *
     * @param ext          雇员本地社保档案扩展信息
     * @param paymentMonth 支付年月
     * @param totalAmount  社保总金额
     * @return 返回月度缴费明细对象
     */
    private SsMonthCharge addStandardMonthCharge(SsEmpBaseArchiveExt ext, String paymentMonth, BigDecimal totalAmount) {
        SsMonthCharge monthCharge = new SsMonthCharge();
        monthCharge.setComAccountId(ext.getComAccountId());
        monthCharge.setSsMonthBelong(paymentMonth);
        monthCharge.setSsMonth(paymentMonth);
        monthCharge.setEmployeeId(ext.getEmployeeId());
        monthCharge.setEmpArchiveId(ext.getEmpArchiveId().toString());
        monthCharge.setBaseAmount(ext.getBaseAmount()); //社保基数
        monthCharge.setTotalAmount(totalAmount); //总金额
        monthCharge.setCostCategory(1);
        monthCharge.setActive(true);
        monthCharge.setCreatedTime(LocalDateTime.now());
        monthCharge.setCreatedBy("system");
        monthCharge.setModifiedTime(LocalDateTime.now());
        monthCharge.setModifiedBy("system");
        monthChargeMapper.insert(monthCharge);
        return monthCharge;
    }


    /**
     * 添加标准月度缴费明细详细
     *
     * @param empBaseDetail
     * @param monthChargeId
     */
    private void addStandardMonthChargeItem(SsEmpBaseDetail empBaseDetail, long monthChargeId) {
        SsMonthChargeItem monthChargeItem = new SsMonthChargeItem();
        monthChargeItem.setMonthChargeId(monthChargeId);
        monthChargeItem.setSsType(empBaseDetail.getSsType());
        monthChargeItem.setSsTypeName(empBaseDetail.getSsTypeName());
        monthChargeItem.setComAmount(empBaseDetail.getComAmount().add(empBaseDetail.getComAdditionAmount()));
        monthChargeItem.setComAmountOrig(empBaseDetail.getComAmountOrig().add(empBaseDetail.getComAdditionAmount()));
        monthChargeItem.setEmpAmount(empBaseDetail.getEmpAmount().add(empBaseDetail.getEmpAdditionAmount()));
        monthChargeItem.setSubTotalAmount(empBaseDetail.getComAmount().add(empBaseDetail.getComAdditionAmount()).add(empBaseDetail.getEmpAmount()).add(empBaseDetail.getEmpAdditionAmount()));
        monthChargeItem.setComRatio(empBaseDetail.getComRatio());
        monthChargeItem.setEmpRatio(empBaseDetail.getEmpRatio());
        monthChargeItem.setActive(true);
        monthChargeItem.setCreatedTime(LocalDateTime.now());
        monthChargeItem.setCreatedBy("system");
        monthChargeItem.setModifiedTime(LocalDateTime.now());
        monthChargeItem.setModifiedBy("system");
        monthChargeItemMapper.insert(monthChargeItem);
    }


    /**
     * 生成非标准月度缴费明细信息以及详细信息
     *
     * @param ext 雇员本地社保档案扩展信息
     */
    private void createNoStandardMonthChange(SsEmpBasePeriodExt ext) {
        switch (ext.getCategory()) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                this.createNoMonthChangeInfoByBasePeriod(ext);
                break;
            case 8:
                this.createNoStandardMonthChargeByRefund(ext);
                break;
            case 9:
                this.createNoMonthChangeInfoByBaseAdjust(ext);
                break;
            default:
                break;
        }
    }

    /**
     * 根据雇员社保汇缴基数明细信息生成月度缴费明细
     */
    private void createNoMonthChangeInfoByBasePeriod(SsEmpBasePeriodExt ext) {
        List<SsEmpBaseDetail> baseDetails = empBaseDetailMapper.getEmpBaseDetailsByPeriodId(ext.getEmpBasePeriodId());
        if (null != baseDetails && baseDetails.size() > 0) {
            BigDecimal totalAmount = baseDetails.stream()
                .map(i -> i.getComAmount().add(i.getComAdditionAmount()).add(i.getEmpAmount()).add(i.getEmpAdditionAmount()))
                .reduce(new BigDecimal(0), (x, y) -> x.add(y));

            SsMonthCharge monthCharge = addNoStandardMonthChargeByBasePeriod(ext, totalAmount);
            if (null != monthCharge) {
                baseDetails.forEach(x -> addNoStandardMonthChargeItemByBasePeriod(x, monthCharge.getMonthChargeId(), ext.getCategory()));
            }
        }
    }

    /**
     * 根据雇员社保基数调整历史月差异信息生成月度缴费明细
     */
    private void createNoMonthChangeInfoByBaseAdjust(SsEmpBasePeriodExt ext) {
        List<SsEmpBaseAdjustDetail> adjustDetails = empBaseAdjustDetailMapper.getEmpBaseAdjustDetailByBaseAdjustId(ext.getEmpBasePeriodId());
        if (null != adjustDetails && adjustDetails.size() > 0) {
            BigDecimal totalAmount = adjustDetails.stream()
                .map(i -> i.getComAmount().add(i.getComAdditionAmount()).add(i.getEmpAmount()).add(i.getEmpAdditionAmount()))
                .reduce(new BigDecimal(0), (x, y) -> x.add(y));

            SsMonthCharge monthCharge = addNoStandardMonthChargeByBasePeriod(ext, totalAmount);
            if (null != monthCharge) {
                adjustDetails.forEach(x -> addNoStandardMonthChargeItemByBaseAdjust(x, monthCharge.getMonthChargeId()));
            }
        }
    }


    /**
     * 添加非标准月度缴费明细（By BasePeriod）
     *
     * @param ext         雇员正常汇缴社保的基数分段扩展信息
     * @param totalAmount 社保总金额
     * @return 返回月度缴费明细对象
     */
    private SsMonthCharge addNoStandardMonthChargeByBasePeriod(SsEmpBasePeriodExt ext, BigDecimal totalAmount) {
        SsMonthCharge monthCharge = new SsMonthCharge();
        monthCharge.setComAccountId(ext.getComAccountId());
        monthCharge.setSsMonthBelong(ext.getSsMonthBelong());
        monthCharge.setSsMonth(ext.getSsMonth());
        monthCharge.setEmployeeId(ext.getEmployeeId());
        monthCharge.setEmpArchiveId(ext.getEmpArchiveId().toString());
        monthCharge.setBaseAmount(ext.getBaseAmount()); //社保基数

        if (ext.getCategory().equals(CostCategory.OUT.getCategory()) || ext.getCategory().equals(CostCategory.SEALED.getCategory())) {
            totalAmount = totalAmount.negate();
        }
        monthCharge.setTotalAmount(totalAmount); //总金额
        monthCharge.setCostCategory(ext.getCategory());
        monthCharge.setActive(true);
        monthCharge.setCreatedTime(LocalDateTime.now());
        monthCharge.setCreatedBy("system");
        monthCharge.setModifiedTime(LocalDateTime.now());
        monthCharge.setModifiedBy("system");
        monthChargeMapper.insert(monthCharge);
        return monthCharge;
    }


    /**
     * 添加非标准月度缴费明细（By Refund）
     *
     * @param ext 雇员正常汇缴社保的基数分段扩展信息
     * @return 返回月度缴费明细对象
     */
    private void createNoStandardMonthChargeByRefund(SsEmpBasePeriodExt ext) {
        SsMonthCharge monthCharge = new SsMonthCharge();
        monthCharge.setComAccountId(ext.getComAccountId());
        monthCharge.setSsMonthBelong(ext.getSsMonthBelong());
        monthCharge.setSsMonth(ext.getSsMonth());
        monthCharge.setEmployeeId(ext.getEmployeeId());
        monthCharge.setEmpArchiveId(ext.getEmpArchiveId().toString());
        monthCharge.setBaseAmount(BigDecimal.valueOf(0)); //社保基数
        monthCharge.setTotalAmount(ext.getTotalAmount()); //总金额
        monthCharge.setCostCategory(ext.getCategory());
        monthCharge.setActive(true);
        monthCharge.setCreatedTime(LocalDateTime.now());
        monthCharge.setCreatedBy("system");
        monthCharge.setModifiedTime(LocalDateTime.now());
        monthCharge.setModifiedBy("system");
        monthChargeMapper.insert(monthCharge);
    }


    /**
     * 添加非标准月度缴费明细详细（By BasePeriod）
     *
     * @param empBaseDetail
     * @param monthChargeId
     */
    private void addNoStandardMonthChargeItemByBasePeriod(SsEmpBaseDetail empBaseDetail, long monthChargeId, Integer category) {
        SsMonthChargeItem monthChargeItem = new SsMonthChargeItem();
        monthChargeItem.setMonthChargeId(monthChargeId);
        monthChargeItem.setSsType(empBaseDetail.getSsType());
        monthChargeItem.setSsTypeName(empBaseDetail.getSsTypeName());
        BigDecimal comAmount = empBaseDetail.getComAmount().add(empBaseDetail.getComAdditionAmount());
        BigDecimal empAmount = empBaseDetail.getEmpAmount().add(empBaseDetail.getEmpAdditionAmount());
        BigDecimal totalAmount = empBaseDetail.getComAmount().add(empBaseDetail.getComAdditionAmount()).add(empBaseDetail.getEmpAmount()).add(empBaseDetail.getEmpAdditionAmount());
        //转出或者封存
        if (category.equals(CostCategory.OUT.getCategory()) || category.equals(CostCategory.SEALED.getCategory())) {
            comAmount = comAmount.negate();
            empAmount = empAmount.negate();
            totalAmount = totalAmount.negate();
        }
        monthChargeItem.setComAmount(comAmount);
        monthChargeItem.setEmpAmount(empAmount);
        monthChargeItem.setSubTotalAmount(totalAmount);
        monthChargeItem.setActive(true);
        monthChargeItem.setCreatedTime(LocalDateTime.now());
        monthChargeItem.setCreatedBy("system");
        monthChargeItem.setModifiedTime(LocalDateTime.now());
        monthChargeItem.setModifiedBy("system");
        monthChargeItemMapper.insert(monthChargeItem);
    }


    /**
     * 添加非标准月度缴费明细详细（By BaseAdjust）
     *
     * @param baseAdjustDetail
     * @param monthChargeId
     */
    private void addNoStandardMonthChargeItemByBaseAdjust(SsEmpBaseAdjustDetail baseAdjustDetail, long monthChargeId) {
        SsMonthChargeItem monthChargeItem = new SsMonthChargeItem();
        monthChargeItem.setMonthChargeId(monthChargeId);
        monthChargeItem.setSsType(baseAdjustDetail.getSsType());
        monthChargeItem.setSsTypeName(baseAdjustDetail.getSsTypeName());
        BigDecimal comAmount = baseAdjustDetail.getComAmount().add(baseAdjustDetail.getComAdditionAmount());
        BigDecimal comAmountOrig = baseAdjustDetail.getComAmountOrig().add(baseAdjustDetail.getComAdditionAmount());
        BigDecimal empAmount = baseAdjustDetail.getEmpAmount().add(baseAdjustDetail.getEmpAdditionAmount());
        BigDecimal totalAmount = baseAdjustDetail.getComAmount().add(baseAdjustDetail.getComAdditionAmount()).add(baseAdjustDetail.getEmpAmount()).add(baseAdjustDetail.getEmpAdditionAmount());
        monthChargeItem.setComAmount(comAmount);
        monthChargeItem.setComAmountOrig(comAmountOrig);
        monthChargeItem.setEmpAmount(empAmount);
        monthChargeItem.setSubTotalAmount(totalAmount);
        monthChargeItem.setComRatio(baseAdjustDetail.getComRatio());
        monthChargeItem.setEmpRatio(baseAdjustDetail.getEmpRatio());
        monthChargeItem.setActive(true);
        monthChargeItem.setCreatedTime(LocalDateTime.now());
        monthChargeItem.setCreatedBy("system");
        monthChargeItem.setModifiedTime(LocalDateTime.now());
        monthChargeItem.setModifiedBy("system");
        monthChargeItemMapper.insert(monthChargeItem);
    }


    /**
     * 生成雇员月度变更信息
     *
     * @param comAccountId 企业社保账户
     * @param paymentMonth
     */
    private void createMonthEmpChange(List<SsMonthChargeExt> allMonthChargeExts, long comAccountId, String paymentMonth,String userName) {
        List<SsMonthChargeExt> monthChargeExts = allMonthChargeExts.stream().filter(x -> x.getCategory() != 1).collect(Collectors.toList());
        List<SsMonthChargeExt> yysMonthChargeExts = new ArrayList<>();
        List<SsMonthChargeExt> gsyMonthChargeExts = new ArrayList<>();
        if (null != monthChargeExts && monthChargeExts.size() > 0) {
            yysMonthChargeExts = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00042") || x.getSsType().equals("DIT00043") || x.getSsType().equals("DIT00046")).collect(Collectors.toList());
            gsyMonthChargeExts = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00044") || x.getSsType().equals("DIT00045")).collect(Collectors.toList());
        }

        if (null != yysMonthChargeExts && yysMonthChargeExts.size() > 0) {
            SsMonthEmpChange monthEmpChange = addMonthEmpChange(comAccountId, paymentMonth, ComputeType.YYS.toString(),userName);
            if (monthEmpChange != null) {
                this.createSsMonthEmpChangeDetial(monthEmpChange, yysMonthChargeExts);
            }
        }

        if (null != gsyMonthChargeExts && gsyMonthChargeExts.size() > 0) {
            SsMonthEmpChange monthEmpChange = addMonthEmpChange(comAccountId, paymentMonth, ComputeType.GSY.toString(),userName);
            if (monthEmpChange != null) {
                this.createSsMonthEmpChangeDetial(monthEmpChange, gsyMonthChargeExts);
            }

        }
    }


    /**
     * 增加雇员月度变更主表信息
     *
     * @param comAccountId 企业社保账户
     * @param paymentMonth 支付年月
     * @param computeType  变更汇总表类型
     * @return
     */
    private SsMonthEmpChange addMonthEmpChange(long comAccountId, String paymentMonth, String computeType ,String userName) {
        SsMonthEmpChange ssMonthEmpChange = new SsMonthEmpChange();
        ssMonthEmpChange.setSsMonth(paymentMonth);
        ssMonthEmpChange.setLastComputeTime(LocalDateTime.now());
        ssMonthEmpChange.setComputeUserId(userName);
        ssMonthEmpChange.setComputeType(computeType);
        ssMonthEmpChange.setComAccountId(comAccountId);
        ssMonthEmpChange.setActive(true);
        ssMonthEmpChange.setCreatedTime(LocalDateTime.now());
        ssMonthEmpChange.setCreatedBy(userName);
        ssMonthEmpChange.setModifiedTime(LocalDateTime.now());
        ssMonthEmpChange.setModifiedBy(userName);
        monthEmpChangeMapper.insert(ssMonthEmpChange);
        return ssMonthEmpChange;
    }


    /**
     * 创建雇员月度变更表明细信息
     *
     * @param monthEmpChange 雇员月度变更主表信息
     * @param chargeExts
     */
    private void createSsMonthEmpChangeDetial(SsMonthEmpChange monthEmpChange, List<SsMonthChargeExt> chargeExts) {
        //按雇员分组
        Map<String, List<SsMonthChargeExt>> empMap = chargeExts.stream().collect(Collectors.groupingBy(SsMonthChargeExt::getEmployeeId));
        if (null != empMap && empMap.size() > 0) {
            empMap.forEach((employeeId, groupByEmpValue) -> {
                //按费用类别分组
                Map<Integer, List<SsMonthChargeExt>> categoryMap = groupByEmpValue.stream().collect(Collectors.groupingBy(SsMonthChargeExt::getCategory));
                if (null != categoryMap && categoryMap.size() > 0) {
                    categoryMap.forEach((category, groupByCategoryValue) -> {
                        //按社保类型分组
                        Map<String, List<SsMonthChargeExt>> ssTypeMap = groupByCategoryValue.stream().collect(Collectors.groupingBy(SsMonthChargeExt::getSsType));
                        if (null != ssTypeMap && ssTypeMap.size() > 0) {
                            ssTypeMap.forEach((ssType, groupBySsTypeValue) -> {
                                if (groupBySsTypeValue.size() > 0) {
                                    this.addSsMonthEmpChangeDetial(monthEmpChange.getMonthEmpChangeId(), employeeId, category, groupBySsTypeValue);
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    /**
     * 增加雇员月度变更表明细
     *
     * @param monthEmpChangeId 雇员月度变更主表ID
     * @param employeeId       雇员ID
     * @param chargeExts
     */
    private void addSsMonthEmpChangeDetial(long monthEmpChangeId, String employeeId, Integer category, List<SsMonthChargeExt> chargeExts) {
        SsMonthChargeExt chargeExt = chargeExts.get(0);
        SsMonthEmpChangeDetail changeDetail = new SsMonthEmpChangeDetail();
        changeDetail.setMonthEmpChangeId(monthEmpChangeId);
        changeDetail.setEmployeeId(employeeId);
        changeDetail.setChangeType(category);
        changeDetail.setChangeTypeName(chargeExt.getCategoryName());
        changeDetail.setBaseAmount(chargeExt.getBaseAmount());
        changeDetail.setSsType(chargeExt.getSsType());
        changeDetail.setSsTypeName(chargeExt.getSsTypeName());

        if (!category.equals(CostCategory.REPPAYMENT.getCategory()) && !category.equals(CostCategory.INVERSEADJUST.getCategory())) {
            changeDetail.setComAmount(chargeExts.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y)));
            changeDetail.setEmpAmount(chargeExts.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y)));
        } else {
            changeDetail.setComCompensateAmount(chargeExts.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y)));
            changeDetail.setEmpCompensateAmount(chargeExts.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y)));
        }

        changeDetail.setActive(true);
        changeDetail.setCreatedTime(LocalDateTime.now());
        changeDetail.setCreatedBy("system");
        changeDetail.setModifiedTime(LocalDateTime.now());
        changeDetail.setModifiedBy("system");
        monthEmpChangeDetailMapper.insert(changeDetail);
    }


    /**
     * 生成月度本地社保应付金额记录明细表
     *
     * @param allMonthChargeExts
     * @param comAccountId
     * @param paymentMonth
     */
    private void createPaymentDetail(List<SsMonthChargeExt> allMonthChargeExts, long comAccountId, String paymentMonth) {
        List<SsMonthChargeExt> monthChargeExts = allMonthChargeExts.stream().filter(x -> !(x.getCategory() == 9 && (x.getEmpAmount().compareTo(BigDecimal.ZERO) == -1 || x.getComAmount().compareTo(BigDecimal.ZERO) == -1))).collect(Collectors.toList());

        List<SsPaymentDetail> paymentDetails = new ArrayList<>();

        List<PaymentDetailBO> paymentDetailBOList = monthChargeItemMapper.sumComAmountOrigInSsType(comAccountId, null, paymentMonth, 0);

        //单位应缴纳社会保险费
//        List<SsMonthChargeExt> currentYearCom = monthChargeExts.stream().filter(x -> x.getSsMonthBelongYy().equals(x.getSsMonthYy())).collect(Collectors.toList());
//        if (null != currentYearCom && currentYearCom.size() > 0) {
//            paymentDetails.add(this.getComPaymentDetail(comAccountId, paymentMonth, 1, currentYearCom));
//        }
        if (CollectionUtils.isNotEmpty(paymentDetailBOList)) {
            paymentDetails.add(this.getComPaymentDetail(comAccountId, paymentMonth, 1, paymentDetailBOList));
        }

        paymentDetailBOList = monthChargeItemMapper.sumComAmountOrigInSsType(comAccountId, null, paymentMonth, 1);

        //单位应补缴历年社会保险费
//        List<SsMonthChargeExt> noCurrentYearCom = monthChargeExts.stream().filter(x -> !x.getSsMonthBelongYy().equals(x.getSsMonthYy())).collect(Collectors.toList());
//        if (null != noCurrentYearCom && noCurrentYearCom.size() > 0) {
//            paymentDetails.add(this.getComPaymentDetail(comAccountId, paymentMonth, 2, noCurrentYearCom));
//        }
        if (CollectionUtils.isNotEmpty(paymentDetailBOList)) {
            paymentDetails.add(this.getComPaymentDetail(comAccountId, paymentMonth, 2, paymentDetailBOList));
        }


        //个人应缴纳社会保险费
        List<SsMonthChargeExt> currentMonthEmp = monthChargeExts.stream().filter(x -> x.getSsMonthBelong().equals(x.getSsMonth())).collect(Collectors.toList());
        if (null != currentMonthEmp && currentMonthEmp.size() > 0) {
            paymentDetails.add(this.getEmpPaymentDetail(comAccountId, paymentMonth, 3, currentMonthEmp));
        }
        //个人应补缴历月社会保险费
        List<SsMonthChargeExt> noCurrentMonthEmp = monthChargeExts.stream().filter(x -> !x.getSsMonthBelong().equals(x.getSsMonth())).collect(Collectors.toList());
        if (null != noCurrentMonthEmp && noCurrentMonthEmp.size() > 0) {
            paymentDetails.add(this.getEmpPaymentDetail(comAccountId, paymentMonth, 4, noCurrentMonthEmp));
        }

        if (null != paymentDetails && paymentDetails.size() > 0) {
            SsPaymentDetail paymentDetail = new SsPaymentDetail();
            paymentDetail.setComAccountId(comAccountId);
            paymentDetail.setPaymentMonth(paymentMonth);
            paymentDetail.setSeq("9");
            paymentDetail.setPaymentItem(9);
            paymentDetail.setPaymentItemName(this.paySeqTable().get(9));

            //养老
            BigDecimal pensionAmount = paymentDetails.stream().map(p -> p.getBasePensionAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setBasePensionAmount(pensionAmount);

            //医疗
            BigDecimal medicalAmount = paymentDetails.stream().map(p -> p.getBaseMedicalAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setBaseMedicalAmount(medicalAmount);

            //失业
            BigDecimal unemploymentAmount = paymentDetails.stream().map(p -> p.getUnemploymentAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setUnemploymentAmount(unemploymentAmount);

            //工伤
            BigDecimal accidentAmount = paymentDetails.stream().map(p -> p.getAccidentAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setAccidentAmount(accidentAmount);

            //生育
            BigDecimal maternityAmount = paymentDetails.stream().map(p -> p.getMaternityAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setMaternityAmount(maternityAmount);

            paymentDetail.setAddMedicalAmount(BigDecimal.valueOf(0));
            paymentDetail.setActive(true);
            paymentDetail.setCreatedTime(LocalDateTime.now());
            paymentDetail.setCreatedBy("system");
            paymentDetail.setModifiedTime(LocalDateTime.now());
            paymentDetail.setModifiedBy("system");

            paymentDetails.add(paymentDetail);

            paymentDetails.forEach(x -> paymentDetailMapper.insert(x));

            List<String> companyIdList = monthChargeMapper.getSsMonthChargeCompanyIds(comAccountId, paymentMonth);

            if (CollectionUtils.isNotEmpty(companyIdList)) {
                List<SsPaymentDetailCom> ssPaymentDetailComList = new ArrayList<>();

                for (String companyId : companyIdList) {
                    paymentDetailBOList = monthChargeItemMapper.sumComAmountOrigInSsType(comAccountId, companyId, paymentMonth, 0);

                    if (CollectionUtils.isNotEmpty(paymentDetailBOList)) {
                        Optional<SsPaymentDetail> optional = paymentDetails.stream().filter(x -> "1".equals(x.getSeq())).findFirst();

                        if (optional.isPresent()) {
                            SsPaymentDetail ssPaymentDetail = optional.get();
                            ssPaymentDetailComList.add(this.getComPaymentDetailCom(ssPaymentDetail.getPaymentDetailId(), comAccountId, companyId, paymentMonth, paymentDetailBOList));
                        }
                    }

                    paymentDetailBOList = monthChargeItemMapper.sumComAmountOrigInSsType(comAccountId, companyId, paymentMonth, 1);

                    if (CollectionUtils.isNotEmpty(paymentDetailBOList)) {
                        Optional<SsPaymentDetail> optional = paymentDetails.stream().filter(x -> "2".equals(x.getSeq())).findFirst();

                        if (optional.isPresent()) {
                            SsPaymentDetail ssPaymentDetail = optional.get();
                            ssPaymentDetailComList.add(this.getComPaymentDetailCom(ssPaymentDetail.getPaymentDetailId(), comAccountId, companyId, paymentMonth, paymentDetailBOList));
                        }
                    }
                }

                ssPaymentDetailComList.forEach(x -> paymentDetailComMapper.insert(x));
            }
        }
    }


    /**
     * 获取企业本地社保应付金额交易记录明细
     *
     * @return
     */
//    private SsPaymentDetail getComPaymentDetail(long comAccountId, String paymentMonth, Integer seq, List<SsMonthChargeExt> monthChargeExts) {
    private SsPaymentDetail getComPaymentDetail(long comAccountId, String paymentMonth, Integer seq, List<PaymentDetailBO> paymentDetailBOList) {
        SsPaymentDetail paymentDetail = new SsPaymentDetail();
        paymentDetail.setComAccountId(comAccountId);
        paymentDetail.setPaymentMonth(paymentMonth);
        paymentDetail.setSeq(seq.toString());
        paymentDetail.setPaymentItem(seq);
        paymentDetail.setPaymentItemName(this.paySeqTable().get(seq));

//        //养老
//        List<SsMonthChargeExt> pensions = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00042")).collect(Collectors.toList());
//        if (null != pensions && pensions.size() > 0) {
////            BigDecimal pensionAmount = pensions.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
//            BigDecimal pensionAmount = pensions.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
//            paymentDetail.setBasePensionAmount(pensionAmount);
//        } else {
//            paymentDetail.setBasePensionAmount(BigDecimal.valueOf(0));
//        }
//        //医疗
//        List<SsMonthChargeExt> medicals = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00043")).collect(Collectors.toList());
//        if (null != medicals && medicals.size() > 0) {
//            BigDecimal medicalAmount = medicals.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
//            paymentDetail.setBaseMedicalAmount(medicalAmount);
//        } else {
//            paymentDetail.setBaseMedicalAmount(BigDecimal.valueOf(0));
//        }
//        //失业
//        List<SsMonthChargeExt> unemployments = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00046")).collect(Collectors.toList());
//        if (null != unemployments && unemployments.size() > 0) {
//            BigDecimal unemploymentAmount = unemployments.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
//            paymentDetail.setUnemploymentAmount(unemploymentAmount);
//        } else {
//            paymentDetail.setUnemploymentAmount(BigDecimal.valueOf(0));
//        }
//        //工伤
//        List<SsMonthChargeExt> accidents = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00044")).collect(Collectors.toList());
//        if (null != accidents && accidents.size() > 0) {
//            BigDecimal accidentAmount = accidents.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
//            paymentDetail.setAccidentAmount(accidentAmount);
//        } else {
//            paymentDetail.setAccidentAmount(BigDecimal.valueOf(0));
//        }
//        //生育
//        List<SsMonthChargeExt> maternitys = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00045")).collect(Collectors.toList());
//        if (null != maternitys && maternitys.size() > 0) {
//            BigDecimal maternityAmount = maternitys.stream().map(p -> p.getComAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
//            paymentDetail.setMaternityAmount(maternityAmount);
//        } else {
//            paymentDetail.setMaternityAmount(BigDecimal.valueOf(0));
//        }
        Map<String, Integer> calcSettingMap = getCalcSettingMap(1, paymentMonth);

//        if (CollectionUtils.isNotEmpty(paymentDetailBOList)) {
        for (PaymentDetailBO paymentDetailBO : paymentDetailBOList) {
            if ("DIT00042".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                paymentDetail.setBasePensionAmount(CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType));
            } else if ("DIT00043".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                paymentDetail.setBaseMedicalAmount(CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType));
            } else if ("DIT00046".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                paymentDetail.setUnemploymentAmount(CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType));
            } else if ("DIT00044".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                paymentDetail.setAccidentAmount(CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType));
            } else if ("DIT00045".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                paymentDetail.setMaternityAmount(CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType));
            }
        }
//        }
        paymentDetail.setAddMedicalAmount(BigDecimal.valueOf(0));
        paymentDetail.setActive(true);
        paymentDetail.setCreatedTime(LocalDateTime.now());
        paymentDetail.setCreatedBy("system");
        paymentDetail.setModifiedTime(LocalDateTime.now());
        paymentDetail.setModifiedBy("system");
        return paymentDetail;
    }

    private SsPaymentDetailCom getComPaymentDetailCom(long paymentDetailId, long comAccountId, String companyId, String paymentMonth, List<PaymentDetailBO> paymentDetailBOList) {
        SsPaymentDetailCom paymentDetailCom = new SsPaymentDetailCom();
        paymentDetailCom.setPaymentDetailId(paymentDetailId);
        paymentDetailCom.setComAccountId(comAccountId);
        paymentDetailCom.setPaymentMonth(paymentMonth);
        paymentDetailCom.setCompanyId(companyId);

        Map<String, Integer> calcSettingMap = getCalcSettingMap(1, paymentMonth);
        BigDecimal sumComAmount = BigDecimal.ZERO;

        for (PaymentDetailBO paymentDetailBO : paymentDetailBOList) {

            if ("DIT00042".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                BigDecimal amount = CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType);
                paymentDetailCom.setBasePensionAmount(amount);
                sumComAmount = sumComAmount.add(amount);
            } else if ("DIT00043".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                BigDecimal amount = CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType);
                paymentDetailCom.setBaseMedicalAmount(amount);
                sumComAmount = sumComAmount.add(amount);
            } else if ("DIT00046".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                BigDecimal amount = CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType);
                paymentDetailCom.setUnemploymentAmount(amount);
                sumComAmount = sumComAmount.add(amount);
            } else if ("DIT00044".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                BigDecimal amount = CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType);
                paymentDetailCom.setAccidentAmount(amount);
                sumComAmount = sumComAmount.add(amount);
            } else if ("DIT00045".equals(paymentDetailBO.getSsType())) {
                int roundType = calcSettingMap.get(paymentDetailBO.getSsType());
                BigDecimal amount = CalculateSocialUtils.calculateByRoundType(paymentDetailBO.getComAmountOrigSum(), roundType);
                paymentDetailCom.setMaternityAmount(amount);
                sumComAmount = sumComAmount.add(amount);
            }
        }

        paymentDetailCom.setSumComAmount(sumComAmount);
        paymentDetailCom.setActive(true);
        paymentDetailCom.setCreatedTime(LocalDateTime.now());
        paymentDetailCom.setCreatedBy("system");
        paymentDetailCom.setModifiedTime(LocalDateTime.now());
        paymentDetailCom.setModifiedBy("system");
        return paymentDetailCom;
    }


    /**
     * 获取个人本地社保应付金额交易记录明细
     *
     * @return
     */
    private SsPaymentDetail getEmpPaymentDetail(long comAccountId, String paymentMonth, Integer seq, List<SsMonthChargeExt> monthChargeExts) {
        SsPaymentDetail paymentDetail = new SsPaymentDetail();
        paymentDetail.setComAccountId(comAccountId);
        paymentDetail.setPaymentMonth(paymentMonth);
        paymentDetail.setSeq(seq.toString());
        paymentDetail.setPaymentItem(seq);
        paymentDetail.setPaymentItemName(this.paySeqTable().get(seq));

        //养老
        List<SsMonthChargeExt> pensions = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00042")).collect(Collectors.toList());
        if (null != pensions && pensions.size() > 0) {
            BigDecimal pensionAmount = pensions.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setBasePensionAmount(pensionAmount);
        } else {
            paymentDetail.setBasePensionAmount(BigDecimal.valueOf(0));
        }
        //医疗
        List<SsMonthChargeExt> medicals = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00043")).collect(Collectors.toList());
        if (null != medicals && medicals.size() > 0) {
            BigDecimal medicalAmount = medicals.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setBaseMedicalAmount(medicalAmount);
        } else {
            paymentDetail.setBaseMedicalAmount(BigDecimal.valueOf(0));
        }
        //失业
        List<SsMonthChargeExt> unemployments = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00046")).collect(Collectors.toList());
        if (null != unemployments && unemployments.size() > 0) {
            BigDecimal unemploymentAmount = unemployments.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setUnemploymentAmount(unemploymentAmount);
        } else {
            paymentDetail.setUnemploymentAmount(BigDecimal.valueOf(0));
        }
        //工伤
        List<SsMonthChargeExt> accidents = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00044")).collect(Collectors.toList());
        if (null != accidents && accidents.size() > 0) {
            BigDecimal accidentAmount = accidents.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setAccidentAmount(accidentAmount);
        } else {
            paymentDetail.setAccidentAmount(BigDecimal.valueOf(0));
        }
        //生育
        List<SsMonthChargeExt> maternitys = monthChargeExts.stream().filter(x -> x.getSsType().equals("DIT00045")).collect(Collectors.toList());
        if (null != maternitys && maternitys.size() > 0) {
            BigDecimal maternityAmount = maternitys.stream().map(p -> p.getEmpAmount()).reduce(new BigDecimal(0), (x, y) -> x.add(y));
            paymentDetail.setMaternityAmount(maternityAmount);
        } else {
            paymentDetail.setMaternityAmount(BigDecimal.valueOf(0));
        }

        paymentDetail.setAddMedicalAmount(BigDecimal.valueOf(0));
        paymentDetail.setActive(true);
        paymentDetail.setCreatedTime(LocalDateTime.now());
        paymentDetail.setCreatedBy("system");
        paymentDetail.setModifiedTime(LocalDateTime.now());
        paymentDetail.setModifiedBy("system");
        return paymentDetail;
    }

    /**
     * 社保缴纳通知单序号
     */
    private Hashtable<Integer, String> paySeqTable() {
        Hashtable<Integer, String> table = new Hashtable<>();
        table.put(1, "单位应缴纳社会保险费");
        table.put(2, "单位应补缴历年社会保险费");
        table.put(3, "个人应缴纳社会保险费");
        table.put(4, "个人应补缴历月社会保险费");
        table.put(5, "其他应缴纳社会保险费");
        table.put(6, "预缴社会保险费");
        table.put(7, "单位缓缴社会保险费");
        table.put(9, "缴纳合计");
        return table;
    }

    private Map<String, Integer> getCalcSettingMap(Integer paymentPart, String startMonth) {
        String key = "-CalcSettingMap-310000-" + paymentPart + "-" + startMonth + "-";
        Map<String, Integer> calcSettingMap = RedisManager.get(key, Map.class);
        if (MapUtils.isEmpty(calcSettingMap)) {
            List<SsCalcSetting> ssCalcSettingList = ssCalcSettingMapper.getShComSettingByMonth(1, startMonth);
            if (CollectionUtils.isNotEmpty(ssCalcSettingList)) {
                if(calcSettingMap==null){
                    calcSettingMap =new HashMap<>();
                }
                for (SsCalcSetting ssCalcSetting : ssCalcSettingList) {
                    calcSettingMap.put(ssCalcSetting.getSsType(), ssCalcSetting.getRoundType());
                }
                RedisManager.set(key, calcSettingMap, ExpireTime.ONE_DAY);
            }
        }

        return calcSettingMap;
    }
}