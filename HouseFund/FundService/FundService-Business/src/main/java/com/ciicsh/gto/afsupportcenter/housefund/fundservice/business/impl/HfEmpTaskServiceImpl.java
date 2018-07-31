package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmpAgreementDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmpSocialDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeCompanyDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeInfoDTO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskCreateTransBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskExportBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskRejectExportBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.ComAccountTransBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfComAccountService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfEmpTaskService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpTaskConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dao.HfEmpTaskMapper;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpTask;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.constant.DictUtil;
import com.ciicsh.gto.afsupportcenter.util.constant.SocialSecurityConst;
import com.ciicsh.gto.afsupportcenter.util.enumeration.ProcessCategory;
import com.ciicsh.gto.afsupportcenter.util.exception.BusinessException;
import com.ciicsh.gto.afsupportcenter.util.logService.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogMessage;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.AfCompanyDetailResponseDTO;
import com.ciicsh.gto.sheetservice.api.dto.TaskCreateMsgDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 雇员任务单总表 服务实现类
 * </p>
 */
@Service
public class HfEmpTaskServiceImpl extends ServiceImpl<HfEmpTaskMapper, HfEmpTask> implements HfEmpTaskService {
    @Autowired
    private LogApiUtil logApiUtil;
    @Autowired
    private HfComAccountService hfComAccountService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMM");

    @Override
    public PageRows<HfEmpTaskExportBo> queryHfEmpTaskInPage(PageInfo pageInfo, String userId) {
        return queryHfEmpTaskInPage(pageInfo, userId, null);
    }

    @Override
    public PageRows<HfEmpTaskExportBo> queryHfEmpTaskInPage(PageInfo pageInfo, String userId, String exceptTaskCategories) {
        HfEmpTaskBo hfEmpTaskBo = pageInfo.toJavaObject(HfEmpTaskBo.class);
        hfEmpTaskBo.setUserId(userId);
        if (StringUtils.isNotBlank(exceptTaskCategories)) {
            hfEmpTaskBo.setExceptTaskCategories(exceptTaskCategories);
        }

        List<String> param = new ArrayList<String>();
        List<String> orderParam = new ArrayList<String>();

        if (!StringUtil.isEmpty(hfEmpTaskBo.getParams())) {
            String arr[] = hfEmpTaskBo.getParams().split(",");
            for (int i = 0; i < arr.length; i++) {
                if(arr[i].indexOf("processStatus")!=-1){
                    String str[] = arr[i].split(" ");
                    String regexp = "\'";
                    String status = str[2].replaceAll(regexp, "");
                    hfEmpTaskBo.setProcessStatus(Integer.parseInt(status));
                }else if(arr[i].indexOf("preInput")!=-1){
                        String str[] = arr[i].split(" ");
                        String regexp = "\'";
                        String preInput = str[2].replaceAll(regexp, "");
                        hfEmpTaskBo.setPreInput(Integer.parseInt(preInput));
                }else{

                    if(arr[i].indexOf("desc")>0||arr[i].indexOf("asc")>0){
                        orderParam.add(arr[i]);
                    }else {
                        if(arr[i].indexOf("hf_account_type")!=-1)
                        {
                            String str[] = arr[i].split(" ");
                            String regexp = "\'";
                            String status = str[2].replaceAll(regexp, "");
                            if ("null".equals(status)) {
                                status = "0";
                            }
                            hfEmpTaskBo.setHfAccountType(Integer.parseInt(status));
                        }
                        if(arr[i].indexOf("payment_bank")!=-1)
                        {
                            String str[] = arr[i].split(" ");
                            String regexp = "\'";
                            String status = str[2].replaceAll(regexp, "");
                            if ("null".equals(status)) {
                                status = "0";
                            }
                            hfEmpTaskBo.setPaymentBank(Integer.parseInt(status));
                        }
                        if(arr[i].indexOf("hf_com_account")!=-1)
                        {
                            String str[] = arr[i].split(" ");
                            String regexp = "\'";
                            String status = str[2].replaceAll(regexp, "");
                            if ("null".equals(status)) {
                                status = "0";
                            }
                            hfEmpTaskBo.setHfComAccount(status);
                        }

                        param.add(arr[i]);
                    }

                }

            }
        }

        hfEmpTaskBo.setParam(param);
        hfEmpTaskBo.setOrderParam(orderParam);
        return PageKit.doSelectPage(pageInfo, () -> baseMapper.queryHfEmpTask(hfEmpTaskBo));
    }

    @Override
    public PageRows<HfEmpTaskRejectExportBo> queryHfEmpTaskRejectInPage(PageInfo pageInfo, String userId, String exceptTaskCategories) {
        HfEmpTaskBo hfEmpTaskBo = pageInfo.toJavaObject(HfEmpTaskBo.class);
        hfEmpTaskBo.setUserId(userId);
        if (StringUtils.isNotBlank(exceptTaskCategories)) {
            hfEmpTaskBo.setExceptTaskCategories(exceptTaskCategories);
        }

        List<String> param = new ArrayList<String>();
        List<String> orderParam = new ArrayList<String>();

        if (!StringUtil.isEmpty(hfEmpTaskBo.getParams())) {
            String arr[] = hfEmpTaskBo.getParams().split(",");
            for (int i = 0; i < arr.length; i++) {
                if(arr[i].indexOf("processStatus")!=-1){
                    String str[] = arr[i].split(" ");
                    String regexp = "\'";
                    String status = str[2].replaceAll(regexp, "");
                    hfEmpTaskBo.setProcessStatus(Integer.parseInt(status));
                }else if(arr[i].indexOf("preInput")!=-1){
                    String str[] = arr[i].split(" ");
                    String regexp = "\'";
                    String preInput = str[2].replaceAll(regexp, "");
                    hfEmpTaskBo.setPreInput(Integer.parseInt(preInput));
                }else{

                    if(arr[i].indexOf("desc")>0||arr[i].indexOf("asc")>0){
                        orderParam.add(arr[i]);
                    }else {
                        if (arr[i].indexOf("desc") > 0 || arr[i].indexOf("asc") > 0) {
                            orderParam.add(arr[i]);
                        } else {
                            if (arr[i].indexOf("hf_account_type") != -1) {
                                String str[] = arr[i].split(" ");
                                String regexp = "\'";
                                String status = str[2].replaceAll(regexp, "");
                                hfEmpTaskBo.setHfAccountType(Integer.parseInt(status));
                            }
                            if (arr[i].indexOf("payment_bank") != -1) {
                                String str[] = arr[i].split(" ");
                                String regexp = "\'";
                                String status = str[2].replaceAll(regexp, "");
                                hfEmpTaskBo.setPaymentBank(Integer.parseInt(status));
                            }
                            if (arr[i].indexOf("hf_com_account") != -1) {
                                String str[] = arr[i].split(" ");
                                String regexp = "\'";
                                String status = str[2].replaceAll(regexp, "");
                                hfEmpTaskBo.setHfComAccount(status);
                            }

                            param.add(arr[i]);
                        }
                    }
                }
            }
        }

        hfEmpTaskBo.setParam(param);
        hfEmpTaskBo.setOrderParam(orderParam);
        return PageKit.doSelectPage(pageInfo, () -> baseMapper.queryHfEmpTaskReject(hfEmpTaskBo));
    }

    /**
     * 查询任务单信息
     * @param hfEmpTask
     */
    @Override
    public List<HfEmpTask> queryByTaskId(HfEmpTask hfEmpTask) {
        return baseMapper.queryByTaskId(hfEmpTask);
    }

    /**
     * 添加数据到雇员任务单表
     * @param taskMsgDTO
     * @param fundCategory
     * @param taskCategory
     * @param isChange
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public boolean addEmpTask(TaskCreateMsgDTO taskMsgDTO, String fundCategory, Integer processCategory,Integer taskCategory, String oldAgreementId, Integer isChange,
                              Map<String, Object> cityCodeMap, AfEmployeeInfoDTO dto, AfEmpSocialDTO socialDTO, AfCompanyDetailResponseDTO afCompanyDetailResponseDTO) throws Exception {
        AfEmployeeCompanyDTO companyDto = dto.getEmployeeCompany();
        AfEmpAgreementDTO afEmpAgreementDTO = dto.getNowAgreement();
        HfEmpTask hfEmpTask = new HfEmpTask();
        hfEmpTask.setTaskId(taskMsgDTO.getTaskId());
        hfEmpTask.setBusinessInterfaceId(taskMsgDTO.getMissionId());

        // 调整通道或更正通道过来的任务单，都需要加上oldAgreementId，回调前道接口时需使用
        if (oldAgreementId != null) {
            hfEmpTask.setOldAgreementId(oldAgreementId);
        }

        if (cityCodeMap != null) {
            if (cityCodeMap.get("oldFundCityCode") != null) {
                hfEmpTask.setOldCityCode(cityCodeMap.get("oldFundCityCode").toString());
            }

            if (cityCodeMap.get("newFundCityCode") != null) {
                hfEmpTask.setNewCityCode(cityCodeMap.get("newFundCityCode").toString());
            }
        }

        if(null != companyDto){
            hfEmpTask.setCompanyId(companyDto.getCompanyId());
            hfEmpTask.setEmployeeId(companyDto.getEmployeeId());
            hfEmpTask.setSubmitterId(afEmpAgreementDTO.getCreatedBy());
            hfEmpTask.setSubmitterRemark(companyDto.getRemark());
            //福利办理方
            hfEmpTask.setWelfareUnit(companyDto.getFundUnit());
            if (companyDto.getInDate() != null) {
                hfEmpTask.setInDate(LocalDateTime.ofInstant(companyDto.getInDate().toInstant(), ZoneId.systemDefault()));
            }
            if(null != companyDto.getOutDate()){
                hfEmpTask.setOutDate(LocalDateTime.ofInstant(companyDto.getOutDate().toInstant(), ZoneId.systemDefault()));
            }
            hfEmpTask.setCreatedBy(afEmpAgreementDTO.getCreatedBy() != null ? afEmpAgreementDTO.getCreatedBy() : "system");
            hfEmpTask.setCreatedDisplayName(afEmpAgreementDTO.getCreatedDisplayName());
            hfEmpTask.setModifiedBy(afEmpAgreementDTO.getCreatedBy() != null ? afEmpAgreementDTO.getCreatedBy() : "system");
            hfEmpTask.setModifiedDisplayName(afEmpAgreementDTO.getCreatedDisplayName());
            hfEmpTask.setLeaderShipId(companyDto.getLeadershipId() != null ? companyDto.getLeadershipId() : "system");
            hfEmpTask.setLeaderShipName(companyDto.getLeadershipName() != null ? companyDto.getLeadershipName() : "system");

            if (afCompanyDetailResponseDTO != null) {
                hfEmpTask.setServiceCenterId(afCompanyDetailResponseDTO.getServiceCenterId());
                hfEmpTask.setServiceCenter(afCompanyDetailResponseDTO.getServiceCenter());
            }
        }
        hfEmpTask.setSubmitTime(LocalDate.now());
        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        //转出单位(来源地)
        hfEmpTask.setTransferOutUnit(this.getTransUnit(paramMap));
        hfEmpTask.setProcessCategory(processCategory);
        //任务类型
        hfEmpTask.setTaskCategory(taskCategory);

        //是否更正 1 是 0 否
        hfEmpTask.setIsChange(isChange);
        hfEmpTask.setTaskFormContent(JSON.toJSONString(dto));

        //前道传递的政策明细ID,用它调用系统中心获取进位方式
        if (dto.getNowAgreement() != null && dto.getNowAgreement().getFundPolicyId() != null) {
            hfEmpTask.setPolicyDetailId(dto.getNowAgreement().getFundPolicyId());
        }
        //TODO 表中加字段
        //办理状态：1、未处理 2 、处理中(已办)  3 已完成(已做) 4、批退 5、不需处理
        hfEmpTask.setTaskStatus(1);
        //入职日期

        hfEmpTask.setActive(true);


        hfEmpTask.setModifiedTime(LocalDateTime.now());
        hfEmpTask.setCreatedTime(LocalDateTime.now());
        hfEmpTask.setAmount(new BigDecimal(0));
        List<AfEmpSocialDTO> socialList = dto.getEmpSocialList();
        if(null != socialList && socialList.size() > 0){
            this.setEmpTask(hfEmpTask,socialDTO,fundCategory);

            // 调整或更正的转出或封存时
            if (oldAgreementId != null && (
                    ProcessCategory.AF_EMP_AGREEMENT_ADJUST.getCategory().equals(processCategory)
                        || ProcessCategory.AF_EMP_AGREEMENT_UPDATE.getCategory().equals(processCategory)
                ) && (
                    HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT == taskCategory
                        || HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE == taskCategory
            )) {
                // 非0转0不是常规意义的停办，是一种调整任务，所以没有截止年月，此处需特别处理
                if (StringUtils.isEmpty(hfEmpTask.getEndMonth()) && StringUtils.isNotEmpty(hfEmpTask.getStartMonth())) {
                    YearMonth startMonthDate = YearMonth.parse(hfEmpTask.getStartMonth(), formatter);
                    hfEmpTask.setEndMonth(startMonthDate.minusMonths(1).format(formatter));
                    hfEmpTask.setStartMonth(null);
                }
            }
        }

        //公积金类型:1 基本 2 补充
        Integer hfType = fundCategory.equals(DictUtil.DICT_ITEM_ID_FUND_BASIC) ? 1 : 2;
        hfEmpTask.setHfType(hfType);

        // 重复任务单校验
        Map<String, Object> condition = new HashMap<>();
        condition.put("business_interface_id", hfEmpTask.getBusinessInterfaceId());
        condition.put("task_id", hfEmpTask.getTaskId());
        condition.put("is_change", hfEmpTask.getIsChange());
        condition.put("is_active", 1);
        List<HfEmpTask> hfEmpTaskList = baseMapper.selectByMap(condition);

        if (CollectionUtils.isNotEmpty(hfEmpTaskList)) {
            logApiUtil.warn(LogMessage.create().setTitle("HfEmpTaskServiceImpl#addEmpTask")
                .setContent("任务单幂等校验未通过。business_interface_id=" + String.valueOf(hfEmpTask.getBusinessInterfaceId())
                    + ", task_id=" + String.valueOf(hfEmpTask.getTaskId())
                    + ", is_change=" + String.valueOf(hfEmpTask.getIsChange())));
            hfEmpTask.setActive(false);
        }

        baseMapper.insert(hfEmpTask);

        if (hfEmpTask.getActive()) {
            // 转出或封存任务单，同时生成转移任务单
            createTransferTask(hfEmpTask, null);
        }
        return true;
    }

    /**
     * 修改数据到雇员任务单表
     * @param taskMsgDTO 消息队列接受的对象
     * @param fundCategory 公积金类别（基本或者补充公积金）
     * @param dto        取得的雇员信息
     * @return
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public boolean updateEmpTask(TaskCreateMsgDTO taskMsgDTO, String fundCategory, AfEmployeeInfoDTO dto, AfEmpSocialDTO socialDTO) {

        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        AfEmployeeCompanyDTO companyDto = dto.getEmployeeCompany();
        AfEmpAgreementDTO afEmpAgreementDTO = dto.getNowAgreement();
        HfEmpTask hfEmpTask = new HfEmpTask();
        hfEmpTask.setTaskId(paramMap.get("oldTaskId").toString());

        // 调整通道或更正通道过来的任务单（目前只有更正通道调用该方法），都需要加上oldAgreementId，回调前道接口时需使用
        if (paramMap.get("oldAgreementId") != null) {
            hfEmpTask.setOldAgreementId(paramMap.get("oldAgreementId").toString());
        }

        //查询旧的任务类型保存到新的任务单
        hfEmpTask = baseMapper.selectOne(hfEmpTask);

        if(null != companyDto){
            hfEmpTask.setCompanyId(companyDto.getCompanyId());
            hfEmpTask.setEmployeeId(companyDto.getEmployeeId());
            hfEmpTask.setSubmitterId(afEmpAgreementDTO.getCreatedBy());
            hfEmpTask.setSubmitterRemark(companyDto.getRemark());
            //福利办理方
            hfEmpTask.setWelfareUnit(companyDto.getFundUnit());
            if (companyDto.getInDate() != null) {
                hfEmpTask.setInDate(LocalDateTime.ofInstant(companyDto.getInDate().toInstant(), ZoneId.systemDefault()));
            }
            if(null != companyDto.getOutDate()){
                hfEmpTask.setOutDate(LocalDateTime.ofInstant(companyDto.getOutDate().toInstant(), ZoneId.systemDefault()));
            }
            hfEmpTask.setModifiedBy(afEmpAgreementDTO.getModifiedBy() != null ? afEmpAgreementDTO.getModifiedBy() : "system");
            hfEmpTask.setModifiedDisplayName(afEmpAgreementDTO.getModifiedDisplayName());
            hfEmpTask.setLeaderShipId(companyDto.getLeadershipId() != null ? companyDto.getLeadershipId() : "system");
            hfEmpTask.setLeaderShipName(companyDto.getLeadershipName() != null ? companyDto.getLeadershipName() : "system");
        }

        hfEmpTask.setBusinessInterfaceId(taskMsgDTO.getMissionId());
        hfEmpTask.setTaskFormContent(JSON.toJSONString(dto));
        //转出单位(来源地)
        hfEmpTask.setTransferOutUnit(this.getTransUnit(paramMap));
        //前道传递的政策明细ID,用它调用系统中心获取进位方式
        if (dto.getNowAgreement() != null && dto.getNowAgreement().getFundPolicyId() != null) {
            hfEmpTask.setPolicyDetailId(dto.getNowAgreement().getFundPolicyId());
        }
        //TODO 表中加字段
        hfEmpTask.setModifiedTime(LocalDateTime.now());
        List<AfEmpSocialDTO> socialList = dto.getEmpSocialList();
        if(null != socialList && socialList.size() > 0){
            this.setEmpTask(hfEmpTask,socialDTO,fundCategory);

            // 调整或更正的转出或封存时
            if (paramMap.get("oldAgreementId") != null && (
                ProcessCategory.AF_EMP_AGREEMENT_ADJUST.getCategory().equals(hfEmpTask.getProcessCategory())
                    || ProcessCategory.AF_EMP_AGREEMENT_UPDATE.getCategory().equals(hfEmpTask.getProcessCategory())
            ) && (
                HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT == hfEmpTask.getTaskCategory()
                    || HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE == hfEmpTask.getTaskCategory()
            )) {
                // 非0转0不是常规意义的停办，是一种调整任务，所以没有截止年月，此处需特别处理
                if (StringUtils.isEmpty(hfEmpTask.getEndMonth()) && StringUtils.isNotEmpty(hfEmpTask.getStartMonth())) {
                    YearMonth startMonthDate = YearMonth.parse(hfEmpTask.getStartMonth(), formatter);
                    hfEmpTask.setEndMonth(startMonthDate.minusMonths(1).format(formatter));
                    hfEmpTask.setStartMonth(null);
                }
            }
        }
        baseMapper.updateById(hfEmpTask);

        return true;
    }


    /**
     * 设置EmpTask的值
     * @param empTask
     * @param socialDTO
     * @param fundCategory
     */
    private void setEmpTask(HfEmpTask empTask,AfEmpSocialDTO socialDTO,String fundCategory){
//        AfEmpSocialDTO socialDTO = this.getAfEmpSocialByType(socialDTOS,fundCategory);
        if(null != socialDTO){
            empTask.setEmpBase(socialDTO.getPersonalBase());
            if(null != socialDTO.getStartDate()){
                empTask.setStartMonth(StringUtil.dateToString(socialDTO.getStartDate(),"yyyyMM"));
            }
            if(null != socialDTO.getEndDate()){
                empTask.setEndMonth(StringUtil.dateToString(socialDTO.getEndDate(),"yyyyMM"));
            }
            if(null != socialDTO.getTotal()){
                empTask.setAmount(socialDTO.getTotal());
            }
            if(null != socialDTO.getPersonalRatio()){
                empTask.setRatioEmp(socialDTO.getPersonalRatio());
            }
            if(null != socialDTO.getPersonalRatio()){
                empTask.setRatioCom(socialDTO.getCompanyRatio());
            }
            if(null != socialDTO.getAccount()){
                empTask.setHfEmpAccount(socialDTO.getAccount());
            }
        }
    }

    /**
     * 根据公积金类型获取AfEmpSocialDTO
     * @param socialDTOS
     * @param fundCategory
     * @return
     */
    public AfEmpSocialDTO getAfEmpSocialByType(List<AfEmpSocialDTO> socialDTOS,String fundCategory){
        AfEmpSocialDTO socialDTO = null;
        List<AfEmpSocialDTO> fundInfos = socialDTOS
            .stream()
            .filter(x->null != x.getPolicyType() && x.getPolicyType().equals(2) && x.getItemCode().equals(fundCategory))
            .collect(Collectors.toList());
        if(null != fundInfos && fundInfos.size() > 0){
            socialDTO = fundInfos.get(0);
        }
        return socialDTO;
    }

    @Override
    public Integer getExistHandleRemarkCount(HfEmpTaskBo hfEmpTaskBo) {
        return baseMapper.getExistHandleRemarkCount(hfEmpTaskBo);
    }

    /**
     * 转出或封存（翻牌转出或翻牌封存）类型的任务单办理完成自动生成转移任务单
     *
     * @param inputHfEmpTask 任务单表数据
     * @param comAccountId   企业账户ID
     */
    @Override
    public void createTransferTask(HfEmpTask inputHfEmpTask, Long comAccountId) {
        HfEmpTaskCreateTransBo hfEmpTaskCreateTransBo;
        ComAccountTransBo comAccountTransBo;

        switch (inputHfEmpTask.getTaskCategory()) {
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
            case HfEmpTaskConstant.TASK_CATEGORY_FLOP_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_FLOP_TRANS_OUT:
                hfEmpTaskCreateTransBo = new HfEmpTaskCreateTransBo();
                hfEmpTaskCreateTransBo.setEmpTaskId(inputHfEmpTask.getEmpTaskId());
                comAccountTransBo = new ComAccountTransBo();
                List<ComAccountTransBo> comAccountTransBoList;

                // 如果转出或封存时，转入单位统一为市公积金中心
//                if (inputHfEmpTask.getTaskCategory() == HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE
//                    || inputHfEmpTask.getTaskCategory() == HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT) {
                    String transferInUnit = SocialSecurityConst.FUND_OUT_UNIT_LIST.get(0); //市公积金中心单位名称
                    hfEmpTaskCreateTransBo.setTransferInUnit(transferInUnit);
                    String transferInUnitAccount =
                        (inputHfEmpTask.getHfType() == HfEmpTaskConstant.HF_TYPE_BASIC) ? SocialSecurityConst.CENTER_BASIC_COM_ACCOUNT
                            : SocialSecurityConst.CENTER_ADDED_COM_ACCOUNT;
                    hfEmpTaskCreateTransBo.setTransferInUnitAccount(transferInUnitAccount);
//                }
                comAccountTransBo.setHfType(inputHfEmpTask.getHfType());
                if (comAccountId != null) {
                    comAccountTransBo.setComAccountId(comAccountId);
                } else {
                    comAccountTransBo.setWelfareUnit(inputHfEmpTask.getWelfareUnit());
                    comAccountTransBo.setCompanyId(inputHfEmpTask.getCompanyId());
                }
                comAccountTransBoList = hfComAccountService.queryComAccountTransBoList(comAccountTransBo);
                if (CollectionUtils.isNotEmpty(comAccountTransBoList)) {
                    if (comAccountTransBoList.size() > 1) {
                        String hfTypeName = (comAccountTransBo.getHfType() == HfEmpTaskConstant.HF_TYPE_BASIC) ? "基本公积金" : "补充公积金";
                        throw new BusinessException("转出单位的" + hfTypeName + "账户存在重复数据");
                    }
                    hfEmpTaskCreateTransBo.setTransferOutUnit(comAccountTransBoList.get(0).getComAccountName());
                    hfEmpTaskCreateTransBo.setTransferOutUnitAccount(comAccountTransBoList.get(0).getHfComAccount());
                }
                hfEmpTaskCreateTransBo.setTaskStatus(HfEmpTaskConstant.TASK_STATUS_UNHANDLED);
                hfEmpTaskCreateTransBo.setModifiedBy(inputHfEmpTask.getModifiedBy());
                hfEmpTaskCreateTransBo.setModifiedDisplayName(inputHfEmpTask.getModifiedDisplayName());
                baseMapper.createTransEmpTask(hfEmpTaskCreateTransBo);
                break;
//            case HfEmpTaskConstant.TASK_CATEGORY_FLOP_OPEN:
//            case HfEmpTaskConstant.TASK_CATEGORY_FLOP_TRANS_IN:
//                // 翻牌转入或翻牌启封时
//                Map<String, Object> condition = new HashMap<>();
//                hfEmpTaskCreateTransBo = new HfEmpTaskCreateTransBo();
//                condition.put("employee_id", hfEmpTaskCreateTransBo.getEmployeeId());
//                condition.put("task_category", HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK);
//                condition.put("hf_type", hfEmpTaskCreateTransBo.getHfType());
//                condition.put("task_status", HfEmpTaskConstant.TASK_STATUS_UNHANDLED);
//                condition.put("business_interface_id", inputHfEmpTask.getBusinessInterfaceId());
//                condition.put("is_active", 1);
//                List<HfEmpTask> hfEmpTaskList = selectByMap(condition);
//
//                // 判断相应的翻牌转出或翻牌封存办理时，生成的转移任务单是否存在
//                if (CollectionUtils.isNotEmpty(hfEmpTaskList)) {
//                    int size = hfEmpTaskList.size();
//                    if (size > 0) {
//                        HfEmpTask transferTask = hfEmpTaskList.get(size - 1);
//                        HfEmpTask updateTransferTask = new HfEmpTask();
//                        comAccountTransBo = new ComAccountTransBo();
//                        comAccountTransBo.setHfType(inputHfEmpTask.getHfType());
//                        comAccountTransBo.setComAccountId(comAccountId);
//                        comAccountTransBoList = hfComAccountService.queryComAccountTransBoList(comAccountTransBo);
//                        if (CollectionUtils.isNotEmpty(comAccountTransBoList)) {
//                            if (comAccountTransBoList.size() > 1) {
//                                String hfTypeName = (comAccountTransBo.getHfType() == HfEmpTaskConstant.HF_TYPE_BASIC) ? "基本公积金" : "补充公积金";
//                                throw new BusinessException("转入单位的" + hfTypeName + "账户存在重复数据");
//                            }
//                            updateTransferTask.setTransferInUnit(comAccountTransBoList.get(0).getComAccountName());
//                            updateTransferTask.setTransferInUnitAccount(comAccountTransBoList.get(0).getHfComAccount());
//                        }
//                        updateTransferTask.setEmpTaskId(transferTask.getEmpTaskId());
//                        updateTransferTask.setModifiedBy(UserContext.getUserId());
//                        updateTransferTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
//                        updateById(updateTransferTask);
//                    }
//                }
//                break;
            default:
                break;
        }
    }


    /**
     * 转出单位(来源地)
     * @param paramMap
     * @return
     */
    private String getTransUnit(Map<String, Object> paramMap){
        String transUnit = "";
        if (null != paramMap.get("source")) {
            String source = paramMap.get("source").toString();
            if ("1".equals(source)) {
                transUnit = "中心";
            }
            else if ("2".equals(source)) {
                transUnit ="原单位";
            }
        }
        return transUnit;
    }
}
