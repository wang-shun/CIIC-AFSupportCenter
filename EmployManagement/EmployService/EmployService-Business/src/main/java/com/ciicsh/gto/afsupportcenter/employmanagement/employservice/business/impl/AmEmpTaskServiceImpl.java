package com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmpProductDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmpSocialDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeCompanyDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeInfoDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.proxy.AfEmployeeCompanyProxy;
import com.ciicsh.gto.afcompanycenter.queryservice.api.proxy.AfEmployeeProductProxy;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.api.dto.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.bo.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.utils.CommonApiUtils;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.utils.ReasonUtil;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.custom.employSearchExportOpt;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dao.AmEmpTaskMapper;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmEmpCustom;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmEmpEmployee;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmEmpMaterial;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmEmpTask;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsystemmanagecenter.apiservice.api.dto.auth.SMUserInfoDTO;
import com.ciicsh.gto.employeecenter.apiservice.api.dto.*;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.AfCompanyDetailResponseDTO;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.CompanyTypeDTO;
import com.ciicsh.gto.sheetservice.api.dto.TaskCreateMsgDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 用工退工任务单 服务实现类
 * </p>
 */
@Service
public class AmEmpTaskServiceImpl extends ServiceImpl<AmEmpTaskMapper, AmEmpTask> implements IAmEmpTaskService {

    private final static Logger logger = LoggerFactory.getLogger(AmEmpTaskServiceImpl.class);

    @Autowired
    private IAmEmpMaterialService amEmpMaterialService;

    @Autowired
    private CommonApiUtils employeeInfoProxy;

    @Autowired
    AfEmployeeCompanyProxy afEmployeeCompanyProxy;

    @Autowired
    private IAmCompanySetService iAmCompanySetService;

    @Autowired
    private  IAmCompanySetService amCompanySetService;

    @Autowired
    private IAmEmpCustomService amEmpCustomService;

    @Autowired
    private AmEmpEmployeeService amEmpEmployeeService;

    @Autowired
    private AfEmployeeProductProxy afEmployeeProductProxy;


    @Override
    public PageRows<AmEmpTaskBO> queryAmEmpTask(PageInfo pageInfo) {

        AmEmpTaskBO amEmpTaskBO = pageInfo.toJavaObject(AmEmpTaskBO.class);

        List<String> param = new ArrayList<String>();

        if (!StringUtil.isEmpty(amEmpTaskBO.getParams())) {
            String arr[] = amEmpTaskBO.getParams().split(",");
            for (int i = 0; i < arr.length; i++) {
                param.add(arr[i]);
            }
        }

        amEmpTaskBO.setParam(param);

        if (null != amEmpTaskBO.getTaskStatus() && amEmpTaskBO.getTaskStatus() == 0) {
            amEmpTaskBO.setTaskStatus(null);
        }

        if(amEmpTaskBO.getTaskStatus()!=null&&amEmpTaskBO.getTaskStatus()==6){
            return PageKit.doSelectPage(pageInfo, () -> baseMapper.queryAmEmpTaskOther(amEmpTaskBO));
        }else{
            return PageKit.doSelectPage(pageInfo, () -> baseMapper.queryAmEmpTask(amEmpTaskBO));
        }


    }

    @Override
    public List<AmEmpTaskBO> taskCount(PageInfo pageInfo) {
        AmEmpTaskBO amEmpTaskBO = pageInfo.toJavaObject(AmEmpTaskBO.class);
        List<String> param = new ArrayList<String>();

        if (!StringUtil.isEmpty(amEmpTaskBO.getParams())) {
            String arr[] = amEmpTaskBO.getParams().split(",");
            for (int i = 0; i < arr.length; i++) {
                param.add(arr[i]);
            }
        }
        amEmpTaskBO.setParam(param);

        return baseMapper.taskCount(amEmpTaskBO);
    }

    @Override
    public List<AmEmpTaskBO> queryAmEmpTaskById(Map<String, Object> param) {
        return baseMapper.queryAmEmploymentById(param);
    }
    /**
     * 保存数据到雇员任务单表
     *
     * @param taskMsgDTO
     * @param taskCategory
     * @return
     * @throws Exception
     */
    @Transactional(
        rollbackFor = {Exception.class}
    )
    @Override
    public boolean  insertTaskTb(TaskCreateMsgDTO taskMsgDTO, Integer taskCategory) throws Exception {

        AmEmpTask amEmpTask = new AmEmpTask();
        amEmpTask.setTaskId(taskMsgDTO.getTaskId());
        amEmpTask.setBusinessInterfaceId(taskMsgDTO.getMissionId());
        amEmpTask.setTaskCategory(taskCategory);
        amEmpTask.setTaskStatus(1);

        if(StringUtil.isEmpty(taskMsgDTO.getVariables().get("empCompanyId")))
        {
            logger.info("empCompanyId is null ...");
            return false;
        }
        AmEmpTaskBO bo = this.queryAmEmpTaskBO(taskMsgDTO.getVariables().get("empCompanyId"));
        amEmpTask.setEmpCompanyId(taskMsgDTO.getVariables().get("empCompanyId").toString());

        if(null!=bo){
            amEmpTask.setCompanyId(bo.getCompanyId());
            amEmpTask.setEmployeeId(bo.getEmployeeId());
            amEmpTask.setTaskFormContent(JSON.toJSONString(taskMsgDTO.getVariables()));
        }

        String archiveDirection = null;
        String employeeNature = null;
        String submitterId = null;//材料提交人
        Map<String, Object> map = null;
        try {
            map = taskMsgDTO.getVariables();

            archiveDirection = (String)map.get("archiveDirection");
            employeeNature = (String)map.get("employeeNature");
            EmployeeBO personNature = baseMapper.queryNature(employeeNature);
            EmployeeBO employeeBO  = baseMapper.queryArchiveDriection(archiveDirection);
            if(null!=personNature){
                amEmpTask.setEmployeeNature(personNature.getPersonNature());
            }
            if(null!=employeeBO){
                amEmpTask.setArchiveDirection(employeeBO.getArchiveDirection());
            }
            submitterId = taskMsgDTO.getVariables().get("submitterId")==null?"":map.get("submitterId").toString();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        //TODO 调用吴敬磊接口传入taskMsgDTO.getMissionId()返回数据
        AfEmployeeInfoDTO dto = null;
        AfEmployeeCompanyDTO employeeCompany = null;
        try {
            dto = employeeInfoProxy.callInf(taskMsgDTO);
            employeeCompany = dto.getEmployeeCompany();
            if(null!=employeeCompany)
            {
                amEmpTask.setCreatedBy(employeeCompany.getCreatedBy());
                amEmpTask.setModifiedBy(employeeCompany.getModifiedBy());
                amEmpTask.setSubmitterId(submitterId);
                if(employeeCompany.getHireUnit()!=null)
                {
                    amEmpTask.setEmployCode(employeeCompany.getHireUnit());
                    amEmpTask.setEmployProperty(ReasonUtil.getYgsx(employeeCompany.getHireUnit().toString()));
                }
            }

            //如果是翻盘
            if("emp_company_change".equals(taskMsgDTO.getProcessDefinitionKey()))
            {
                amEmpTask.setChange("是");
            }else{
                amEmpTask.setChange("否");
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        amEmpTask.setActive(true);
        amEmpTask.setModifiedTime(LocalDateTime.now());
        amEmpTask.setCreatedTime(LocalDateTime.now());

        baseMapper.insert(amEmpTask);

        try {
            this.saveEmpCustom(employeeCompany,amEmpTask.getEmpTaskId(),bo.getCompanyId());

            this.saveEmpEmployee(taskMsgDTO,bo,amEmpTask.getEmpTaskId());

        } catch (Exception e) {

        }

        try {
            List<String> list = (List<String>) map.get("materialList");
            SMUserInfoDTO smUserInfoDTO = null;
            if(!StringUtil.isEmpty(submitterId))
            {
                smUserInfoDTO = employeeInfoProxy.getUserInfo(submitterId);
            }
            List<AmEmpMaterial> amEmpMaterialsList = new ArrayList<>();
            if(null!=list)
            {
                for(String str:list)
                {
                    AmEmpMaterial amEmpMaterial = new AmEmpMaterial();
                    amEmpMaterial.setMaterialName(str);
                    amEmpMaterial.setEmployeeId(bo.getEmployeeId());
                    amEmpMaterial.setOperateType(1);
                    amEmpMaterial.setActive(true);
                    String createdBy = "System";
                    try {
                        createdBy = smUserInfoDTO.getUserId();
                    } catch (Exception e) {

                    }
                    amEmpMaterial.setCreatedBy(createdBy);
                    amEmpMaterial.setCreatedTime(LocalDateTime.now());
                    amEmpMaterial.setModifiedTime(LocalDateTime.now());
                    amEmpMaterial.setModifiedBy(createdBy);
                    amEmpMaterial.setSubmitterDate(LocalDate.now());
                    amEmpMaterial.setSubmitterId(submitterId);
                    amEmpMaterial.setSubmitterName(smUserInfoDTO==null?"":smUserInfoDTO.getDisplayName());
                    amEmpMaterial.setExtension(smUserInfoDTO==null?"":smUserInfoDTO.getExtension());
                    amEmpMaterial.setEmpTaskId(amEmpTask.getEmpTaskId());
                    amEmpMaterialsList.add(amEmpMaterial);
                }
                amEmpMaterialService.insertBatch(amEmpMaterialsList);
            }else{
                logger.info("materialList",map.get("materialList"));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public boolean insertTaskFire(TaskCreateMsgDTO taskMsgDTO, Integer taskCategory) throws Exception {

        AmEmpTask amEmpTask = new AmEmpTask();
        amEmpTask.setTaskId(taskMsgDTO.getTaskId());
        amEmpTask.setBusinessInterfaceId(taskMsgDTO.getMissionId());
        amEmpTask.setTaskCategory(taskCategory);
        amEmpTask.setTaskStatus(99);

        //TODO 调用吴敬磊接口传入taskMsgDTO.getMissionId()返回数据
        AfEmployeeInfoDTO dto = null;
        try {
            dto = employeeInfoProxy.callInf(taskMsgDTO);
            AfEmployeeCompanyDTO employeeCompany = dto.getEmployeeCompany();
            amEmpTask.setEmployeeId(employeeCompany.getEmployeeId());
            amEmpTask.setCompanyId(employeeCompany.getCompanyId());
            amEmpTask.setTaskFormContent(JSON.toJSONString(taskMsgDTO.getVariables()));

            amEmpTask.setOutDate(employeeCompany==null?null:employeeCompany.getOutDate());

            if(null!=employeeCompany&&null!=employeeCompany.getOutReason()){
                amEmpTask.setOutReasonCode(employeeCompany.getOutReason().toString());
                amEmpTask.setOutReason(ReasonUtil.getReasonOut(employeeCompany.getOutReason().toString()));
            }else{
                if(employeeCompany!=null){
                    logger.info(JSON.toJSONString(employeeCompany));
                }
                logger.info("outReason is null "+"  MissionId is "+taskMsgDTO.getMissionId());
            }
            //如果是调整或者更正
            if("emp_agreement_adjust".equals(taskMsgDTO.getProcessDefinitionKey())||"emp_agreement_update".equals(taskMsgDTO.getProcessDefinitionKey()))
            {
                amEmpTask.setOutReason("转其他城市缴纳");
                amEmpTask.setOutReasonCode("changeOther");
                List<AfEmpSocialDTO> empSocialList = dto.getEmpSocialList();
                if(null!=empSocialList)
                {
                    boolean isContinue = false;
                    for(AfEmpSocialDTO afEmpSocialDTO:empSocialList)
                    {
                        if("DIT00042".equals(afEmpSocialDTO.getItemCode())&&null!=afEmpSocialDTO.getEndDate())
                        {
                            amEmpTask.setOutDate(afEmpSocialDTO.getEndDate());
                            isContinue = true;
                            break;
                        }
                    }
                    if(!isContinue)
                    {
                        for(AfEmpSocialDTO afEmpSocialDTO:empSocialList)
                        {
                            if(null!=afEmpSocialDTO.getEndDate())
                            {
                                amEmpTask.setOutDate(afEmpSocialDTO.getEndDate());
                                break;
                            }

                        }
                    }
                }
            }
            //如果是翻盘
            if("emp_company_change".equals(taskMsgDTO.getProcessDefinitionKey()))
            {
                amEmpTask.setChange("是");
            }else{
                amEmpTask.setChange("否");
            }
            if(employeeCompany!=null&&employeeCompany.getHireUnit()!=null)
            {
                amEmpTask.setEmployCode(employeeCompany.getHireUnit());
                amEmpTask.setEmployProperty(ReasonUtil.getYgsx(employeeCompany.getHireUnit().toString()));
            }
            if(employeeCompany!=null)
            {
                amEmpTask.setCreatedBy(employeeCompany.getCreatedBy());
                amEmpTask.setModifiedBy(employeeCompany.getModifiedBy());
                amEmpTask.setSubmitterId(employeeCompany.getCreatedBy());
            }

            try {
                //更新离职原因
                AmEmpTaskBO amEmpTaskBO = new AmEmpTaskBO();
                amEmpTaskBO.setEmployeeId(amEmpTask.getEmployeeId());
                amEmpTaskBO.setCompanyId(amEmpTask.getCompanyId());
                List<AmEmpTaskBO> amEmpTaskBOList = baseMapper.queryEmpTask(amEmpTaskBO);
                if(null!=amEmpTaskBOList&&amEmpTaskBOList.size()>0){
                    AmEmpTaskBO  amEmpTaskBO1 =  amEmpTaskBOList.get(0);
                    AmEmpTask amEmpTask1 = this.selectById(amEmpTaskBO1.getEmpTaskId());
                    amEmpTask1.setOutDate(amEmpTask.getOutDate());
                    amEmpTask1.setOutReason(amEmpTask.getOutReason());
                    amEmpTask1.setOutReasonCode(amEmpTask.getOutReasonCode());

                    this.insertOrUpdate(amEmpTask1);
                }
            } catch (Exception e) {

            }

        } catch (Exception e) {
            logger.error("callOut interface error ......");
            logger.error(e.getMessage(), e);
        }

        amEmpTask.setActive(true);
        amEmpTask.setModifiedTime(LocalDateTime.now());
        amEmpTask.setCreatedTime(LocalDateTime.now());

        baseMapper.insert(amEmpTask);

        return true;
    }

    @Override
    public AmEmpTaskBO queryAccout(String companyId) {

        AmEmpTaskBO amEmpTaskBO = null;

        try {
            amEmpTaskBO = baseMapper.queryAccout(companyId);
        } catch (Exception e) {

        }

        return amEmpTaskBO;
    }

    @Override
    public AmEmpTaskBO queryEmpTask(AmEmpTaskBO amEmpTaskBO) {
        AmEmpTaskBO empTaskBO = null;
        try {
            List<AmEmpTaskBO>  list = baseMapper.queryEmpTask(amEmpTaskBO);
            if(null!=list&&list.size()>0){
                return  list.get(0);
            }
        } catch (Exception e) {

        }
        return empTaskBO;
    }

    @Override
    public boolean updateTaskStatus(Map<String,Object> param) {
        Integer i =0;
        i = baseMapper.updateTaskStatus(param);
        if(i>0){
            return  true;
        }
        return false;
    }

    @Override
    public AmEmpTask getAmEmpTaskById(Long amEmpTaskId) {
        return super.selectById(amEmpTaskId);
    }

    @Override
    public Map<String, Object> getInformation(AmTaskParamBO param) {

        Map<String,Object> map = new HashMap<>();

        AmCustomBO customBO = new AmCustomBO();//客户信息
        AmEmpTaskBO employeeBO = new AmEmpTaskBO();//雇佣信息
        AmEmpTask amEmpTask = null;
        String  missId = "";
        customBO.setCompanyId(param.getCompanyId());
        try {
            if(null!=param.getEmpTaskId()&&param.isResign()==false)
            {
                amEmpTask = super.selectById(param.getEmpTaskId());
                missId = amEmpTask.getBusinessInterfaceId();
                employeeBO.setArchiveDirection(amEmpTask==null?"":amEmpTask.getArchiveDirection());
                employeeBO.setEmployeeNature(amEmpTask==null?"":amEmpTask.getEmployeeNature());
                employeeBO.setEmployProperty(amEmpTask==null?"":amEmpTask.getEmployProperty());
            }else{
                AmEmpTaskBO amEmpTaskBO = new AmEmpTaskBO();
                amEmpTaskBO.setCompanyId(param.getCompanyId());
                amEmpTaskBO.setEmployeeId(param.getEmployeeId());

                amEmpTask = this.queryEmpTask(amEmpTaskBO);
                missId = amEmpTask.getBusinessInterfaceId();
                employeeBO.setArchiveDirection(amEmpTask==null?"":amEmpTask.getArchiveDirection());
                employeeBO.setEmployeeNature(amEmpTask==null?"":amEmpTask.getEmployeeNature());
                employeeBO.setEmployProperty(amEmpTask==null?"":amEmpTask.getEmployProperty());

            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        //调用前道接口
        AfEmployeeCompanyDTO afEmployeeCompanyDTO = null;
        try {
            TaskCreateMsgDTO taskMsgDTO = new TaskCreateMsgDTO();
            taskMsgDTO.setMissionId(amEmpTask.getBusinessInterfaceId());
            AfEmployeeInfoDTO afEmployeeInfoDTO = employeeInfoProxy.callInfByMissId(taskMsgDTO);
            afEmployeeCompanyDTO = afEmployeeInfoDTO.getEmployeeCompany();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        if(afEmployeeCompanyDTO!=null)
        {
            employeeBO.setTemplateType(ReasonUtil.getYgsx(afEmployeeCompanyDTO.getHireUnit()==null?"":afEmployeeCompanyDTO.getHireUnit().toString()));
            employeeBO.setPosition(afEmployeeCompanyDTO.getPosition());

            Map<String,Object> param0 = new HashMap<>();
            List<AmEmpTaskBO> list = null;
            if(afEmployeeCompanyDTO.getHireUnit()==1){ //独立户
                param0.put("companyId",param.getCompanyId());
                list = baseMapper.querySocial(param0);
            }else {//大库
                list = baseMapper.querySocialCi();
            }

            if(list!=null&&list.size()>0)
            {
                employeeBO.setSsAccount(list.get(0).getSsAccount());
                employeeBO.setSettlementArea(list.get(0).getSettlementArea());
                employeeBO.setAccountRepairDate(list.get(0).getAccountRepairDate());
                employeeBO.setSsPwd(list.get(0).getSsPwd());
            }
        }

        EmployeeQueryDTO var1 = new EmployeeQueryDTO();
        var1.setBusinessType(1);
        var1.setIdCardType(param.getIdCardType());
        var1.setIdNum(param.getIdNum());
        JsonResult<EmployeeInfoDTO> jsonResult = null;//雇佣信息接口

        try {
            jsonResult = employeeInfoProxy.getEmployeeInfo(var1);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        if(null!=jsonResult&&null!=jsonResult.getData()){
            EmployeeInfoDTO employeeInfoDTO = jsonResult.getData();
            employeeBO.setEmployeeId(employeeInfoDTO.getEmployeeId());
            employeeBO.setIdNum(employeeInfoDTO.getIdNum());
            employeeBO.setEmployeeName(employeeInfoDTO.getEmployeeName());
            employeeBO.setSex(employeeInfoDTO.getGender()==0?"女":"男");
            employeeBO.setMobile(employeeInfoDTO.getMobile());
            employeeBO.setResidenceAddress(employeeInfoDTO.getResidenceAddress());
        }

        EmployeeHireInfoQueryDTO employeeHireInfoQueryDTO = new EmployeeHireInfoQueryDTO();
        employeeHireInfoQueryDTO.setCompanyId(param.getCompanyId());
        employeeHireInfoQueryDTO.setEmployeeId(param.getEmployeeId());
        JsonResult<EmployeeHireInfoDTO> employeeHireInfo = null;//雇佣雇佣信息接口

        try {
            employeeHireInfo = employeeInfoProxy.getEmployeeHireInfo(employeeHireInfoQueryDTO);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(employeeHireInfo!=null&&null!=employeeHireInfo.getData()){
            EmployeeHireInfoDTO employeeHireInfoDTO = employeeHireInfo.getData();
            employeeBO.setFirstInDate(employeeHireInfoDTO.getFirstInDate()==null?"":sdf.format(employeeHireInfoDTO.getFirstInDate()));
            employeeBO.setFirstInCompanyDate(employeeHireInfoDTO.getFirstInCompanyDate()==null?"":sdf.format(employeeHireInfoDTO.getFirstInCompanyDate()));
            employeeBO.setOrganizationCode(employeeHireInfoDTO.getOrganizationCode());
            employeeBO.setPosition(employeeHireInfoDTO.getPosition());
            employeeBO.setLaborStartDate(employeeHireInfoDTO.getLaborStartDate()==null?"":sdf.format(employeeHireInfoDTO.getLaborStartDate()));
            employeeBO.setLaborEndDate(employeeHireInfoDTO.getLaborEndDate()==null?"":sdf.format(employeeHireInfoDTO.getLaborEndDate()));
            if(employeeHireInfoDTO.getLaborEndDate()==null)
            {
                employeeBO.setIsUnlimitedContract("是");
            }else{
                employeeBO.setIsUnlimitedContract("否");

                if(employeeHireInfoDTO.getLaborStartDate()!=null){
                    String d = ReasonUtil.getCondemnationYears(employeeHireInfoDTO.getLaborStartDate(),employeeHireInfoDTO.getLaborEndDate());
                    employeeBO.setSendCondemnationYears(d);
                }
            }

            SMUserInfoDTO smUserInfoDTO = null;
            if(!StringUtil.isEmpty(employeeHireInfoDTO.getEmployeeCenterOperator()))
            {
                smUserInfoDTO = employeeInfoProxy.getUserInfo(employeeHireInfoDTO.getEmployeeCenterOperator());
            }

            customBO.setEmployeeCenterOperator(smUserInfoDTO==null?"":smUserInfoDTO.getDisplayName());

        }

        AmCompanySetBO amCompanySetBO = new AmCompanySetBO();
        amCompanySetBO.setCompanyId(param.getCompanyId());
        AmCompanySetBO amCompanySetBO1 = amCompanySetService.queryAmCompanySet(amCompanySetBO);
        if(amCompanySetBO1!=null)
        {
            employeeBO.setEmploySpecial(ReasonUtil.removeMark(amCompanySetBO1.getEmploySpecial()));
            employeeBO.setKeyType(amCompanySetBO1.getKeyType());
            employeeBO.setKeyCode(amCompanySetBO1.getKeyCode());
            employeeBO.setKeyPwd(amCompanySetBO1.getKeyPwd());
            employeeBO.setKeyStatus(amCompanySetBO1.getKeyStatus());
        }

        //档案费
        try {
            Long ll = Long.parseLong(missId);
            List<AfEmpProductDTO> afEmpProductDTOList = afEmployeeProductProxy.getByEmpAgreement(ll,1);
            for(AfEmpProductDTO afEmpProductDTO:afEmpProductDTOList)
            {
                if("CPJSW1800005".equals(afEmpProductDTO.getBasicProductId())){
                    employeeBO.setFileFee("有");
                }
            }
        } catch (NumberFormatException e) {

        }


        //获取服务中心
        AfCompanyDetailResponseDTO afCompanyDetailResponseDTO = employeeInfoProxy.getCompanyDetail(param.getCompanyId());
        customBO.setServiceCenter(afCompanyDetailResponseDTO==null?"":afCompanyDetailResponseDTO.getServiceCenter());//服务中心
        String companyName = afCompanyDetailResponseDTO==null?"":afCompanyDetailResponseDTO.getCompanyName();
        this.setCustomBO(amEmpTask,customBO,companyName);

        try {
            AmCustomBO amCustomBO  = amEmpCustomService.getCustom(param.getEmpTaskId());
            customBO.setLeaderShipName(amCustomBO==null?"":amCustomBO.getLeaderShipName());//客服经理
            customBO.setCreatedDisplayName(amCustomBO==null?"":amCustomBO.getCreatedDisplayName());//客服专员
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        map.put("customBO",customBO);
        map.put("employeeBO",employeeBO);

        return map;
    }



    @Override
    public List<employSearchExportOpt> queryAmEmpTaskList(AmEmpTaskBO amEmpTaskBO) {
        return  baseMapper.queryAmEmpTaskList(amEmpTaskBO);
    }

    @Override
    public AmEmpTaskBO queryAmEmpTaskBO(Object empCompanyId) {
        Map<String,Object> param = new HashMap<>();
        param.put("empCompanyId",empCompanyId);
        AmEmpTaskBO bo = baseMapper.selectEmployId(param);
        return  bo;
    }

    @Override
    public AmEmpTaskBO getDefualtEmployBO(AmEmpTaskBO amEmpTaskBO) {
        AmTaskParamBO amTaskParamBO = new AmTaskParamBO();
        amTaskParamBO.setEmployeeId(amEmpTaskBO.getEmployeeId());
        amTaskParamBO.setCompanyId(amEmpTaskBO.getCompanyId());

        AmEmpEmployeeBO amEmpEmployeeBO = amEmpEmployeeService.queryDefaultAmEmployee(amTaskParamBO);

        AmEmpTask amEmpTask = super.selectById(amEmpTaskBO.getEmpTaskId());
        AmEmpTaskBO amEmpTaskBO1 = new AmEmpTaskBO();
        BeanUtils.copyProperties(amEmpTask,amEmpTaskBO1);

        amEmpTaskBO1 = defaultRule(amEmpTaskBO1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if(amEmpEmployeeBO!=null)
        {
            /**
             * 用工属性根据雇佣类型  派遣默认中智
              */
          if(null!=amEmpEmployeeBO.getHireUnit()&&amEmpEmployeeBO.getHireUnit()==1)
          {
              amEmpTaskBO1.setEmployProperty("独立");
          }

          if(null!=amEmpEmployeeBO.getLaborStartDate()){
              amEmpTaskBO1.setFirstInDate(sdf.format(amEmpEmployeeBO.getLaborStartDate()));//实际录用日期,合同开始日期
          }
        }

        amEmpTaskBO1.setOpenAfDate(sdf.format(new Date()));
        amEmpTaskBO1.setEmployStyle("1");//默认全日制
        return amEmpTaskBO1;
    }

    @Override
    public boolean insertTaskFireChange(TaskCreateMsgDTO taskMsgDTO, Integer taskCategory) throws Exception {
        return false;
    }

    @Override
    public EmploymentDTO getEmploymentByTaskId(TaskParamDTO taskParamDTO) {
        List<EmploymentDTO> list = baseMapper.getEmploymentByTaskId(taskParamDTO);
        if(list!=null&&list.size()>0){
            return  list.get(0);
        }
        return null;
    }

    @Override
    public ArchiveDTO getArchiveByEmployeeId(TaskParamDTO taskParamDTO) {
        List<ArchiveDTO> list = baseMapper.getArchiveByEmployeeId(taskParamDTO);
        if(null!=list&&list.size()>0)
        {
            return  list.get(0);
        }
        return null;
    }

    @Override
    public ResignDTO getResignByTaskId(TaskParamDTO taskParamDTO) {
        List<ResignFeedbackDTO> resignFeedbackDTOList = baseMapper.queryResignLinkByTaskId(taskParamDTO);
        List<ResignDTO> resignDTOList = baseMapper.queryResignByTaskId(taskParamDTO);
        if(null!=resignDTOList&&resignDTOList.size()>0){
            ResignDTO resignDTO = resignDTOList.get(0);
            resignDTO.setFeedbackDTOList(resignFeedbackDTOList);
            return  resignDTO;
        }
        return null;
    }


    AmEmpTaskBO  defaultRule(AmEmpTaskBO amEmpTaskBO){
        if("外来三险".equals(amEmpTaskBO.getEmployeeNature())||"外地人员".equals(amEmpTaskBO.getEmployeeNature()))
        {
            amEmpTaskBO.setHandleType("外来从业人员");
            amEmpTaskBO.setArchivePlace("外来从业人员");
        }

        if("居住证".equals(amEmpTaskBO.getEmployeeNature()))
        {
            amEmpTaskBO.setHandleType(amEmpTaskBO.getEmployeeNature());
            amEmpTaskBO.setArchivePlace(amEmpTaskBO.getEmployeeNature());
        }
        if("上海失业人员".equals(amEmpTaskBO.getEmployeeNature())&&"户口所在地".equals(amEmpTaskBO.getArchiveDirection()))
        {
            AmCompanySetBO amCompanySetBO = new AmCompanySetBO();
            amCompanySetBO.setCompanyId(amEmpTaskBO.getCompanyId());
            AmCompanySetBO amCompanySetBO1 = iAmCompanySetService.queryAmCompanySet(amCompanySetBO);

            if(amCompanySetBO1.getCompanySpecial8()!=null&&amCompanySetBO1.getCompanySpecial8()==1){
                amEmpTaskBO.setHandleType("调档");
            }else {
                amEmpTaskBO.setHandleType("属地管理");
                amEmpTaskBO.setArchivePlace("属地管理");
            }

        }
        if("人才引进".equals(amEmpTaskBO.getEmployeeNature())&&"中智".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType(amEmpTaskBO.getArchiveDirection());
            amEmpTaskBO.setArchivePlace(amEmpTaskBO.getArchiveDirection());
        }
        if("上海失业人员".equals(amEmpTaskBO.getEmployeeNature())&&"区人才".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType(amEmpTaskBO.getArchiveDirection());
            amEmpTaskBO.setArchivePlace(amEmpTaskBO.getArchiveDirection());
        }
        if("农村富裕劳动力".equals(amEmpTaskBO.getEmployeeNature())&&"农村富裕劳动力".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType("农民工");
            amEmpTaskBO.setArchivePlace("农村富裕劳动力");
        }
        if("上海失业人员".equals(amEmpTaskBO.getEmployeeNature())&&"市人才".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType("市人才");
            amEmpTaskBO.setArchivePlace("市人才");
        }
        if("人才引进".equals(amEmpTaskBO.getEmployeeNature())&&"高教中心".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType("高校");
            amEmpTaskBO.setArchivePlace("就业指导中心");
        }
        if("退休人员".equals(amEmpTaskBO.getEmployeeNature())&&"退休".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType("退休");
            amEmpTaskBO.setArchivePlace("退休");
        }
        if("协保人员".equals(amEmpTaskBO.getEmployeeNature())&&"协保".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType("协保");
            amEmpTaskBO.setArchivePlace("协保");
        }
        if("非全日制用工".equals(amEmpTaskBO.getEmployeeNature())&&"非全日制".equals(amEmpTaskBO.getArchiveDirection()))
        {
            amEmpTaskBO.setHandleType("非全日制");
            amEmpTaskBO.setArchivePlace("非全日制");
        }

        return  amEmpTaskBO;
    }

    /**
     * 获取公司信息通过销售中心接口
     * 保存客户信息对象
     */
    void saveEmpCustom(AfEmployeeCompanyDTO employeeCompany,Long empTaskId,String companyId){

        try {
            AfCompanyDetailResponseDTO afCompanyDetailResponseDTO = employeeInfoProxy.getCompanyDetail(companyId);
            AmEmpCustom amEmpCustom = new AmEmpCustom();
            amEmpCustom.setEmpTaskId(empTaskId);
            amEmpCustom.setCreatedDisplayName(employeeCompany==null?"":employeeCompany.getCreatedDisplayName());//客服专员
            amEmpCustom.setModifiedDisplayName(employeeCompany==null?"":employeeCompany.getCreatedDisplayName());
            amEmpCustom.setLeaderShipId(employeeCompany==null?"":employeeCompany.getLeadershipId());
            amEmpCustom.setLeaderShipName(employeeCompany==null?"":employeeCompany.getLeadershipName());//客服经理
            amEmpCustom.setCreatedBy(employeeCompany==null?"":employeeCompany.getCreatedBy());
            amEmpCustom.setServiceCenter(afCompanyDetailResponseDTO.getServiceCenter());
            amEmpCustom.setTitle(employeeCompany==null?"":employeeCompany.getTitle());
            amEmpCustom.setActive(true);
            amEmpCustom.setCreatedTime(LocalDateTime.now());
            amEmpCustom.setModifiedTime(LocalDateTime.now());

            amEmpCustomService.insert(amEmpCustom);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }

    void saveEmpEmployee(TaskCreateMsgDTO taskMsgDTO,AmEmpTaskBO bo,Long empTaskId){

        EmployeeHireInfoQueryDTO employeeHireInfoQueryDTO = new EmployeeHireInfoQueryDTO();
        employeeHireInfoQueryDTO.setCompanyId(bo.getCompanyId());
        employeeHireInfoQueryDTO.setEmployeeId(bo.getEmployeeId());
        JsonResult<EmployeeHireInfoDTO> employeeHireInfo = null;//雇佣雇佣信息接口

        try {
            employeeHireInfo = employeeInfoProxy.getEmployeeHireInfo(employeeHireInfoQueryDTO);

            EmployeeHireInfoDTO employeeHireInfoDTO = employeeHireInfo.getData();

            AmEmpEmployee  amEmpEmployee = new AmEmpEmployee();
            amEmpEmployee.setEmployeeId(bo.getEmployeeId());
            amEmpEmployee.setCompanyId(bo.getCompanyId());
            amEmpEmployee.setEmpTaskId(empTaskId);
            amEmpEmployee.setLaborStartDate(employeeHireInfoDTO.getLaborStartDate());
            amEmpEmployee.setLaborEndDate(employeeHireInfoDTO.getLaborEndDate());
            amEmpEmployee.setGender(employeeHireInfoDTO.getGender());
            amEmpEmployee.setIdNum(employeeHireInfoDTO.getIdNum());
            amEmpEmployee.setEmployeeName(employeeHireInfoDTO.getEmployeeName());
            amEmpEmployee.setPosition(employeeHireInfoDTO.getPosition());
            amEmpEmployee.setOrganizationCode(employeeHireInfoDTO.getOrganizationCode());
            amEmpEmployee.setFirstInDate(employeeHireInfoDTO.getFirstInDate());
            amEmpEmployee.setFirstInCompanyDate(employeeHireInfoDTO.getFirstInCompanyDate());
            amEmpEmployee.setResidenceAddress(employeeHireInfoDTO.getResidenceAddress());
            amEmpEmployee.setMobile(employeeHireInfoDTO.getMobile());

            try {
                SMUserInfoDTO smUserInfoDTO = null;
                if(!StringUtil.isEmpty(employeeHireInfoDTO.getEmployeeCenterOperator()))
                {
                    smUserInfoDTO = employeeInfoProxy.getUserInfo(employeeHireInfoDTO.getEmployeeCenterOperator());
                    amEmpEmployee.setEmployeeCenterOperator(smUserInfoDTO==null?"":smUserInfoDTO.getDisplayName());
                }
            } catch (Exception e) {

            }

            //调用客服中心接口
            AfEmployeeCompanyDTO afEmployeeCompanyDTO = null;
            try {
                AfEmployeeInfoDTO afEmployeeInfoDTO = employeeInfoProxy.callInfByMissId(taskMsgDTO);
                afEmployeeCompanyDTO = afEmployeeInfoDTO.getEmployeeCompany();

                if(afEmployeeCompanyDTO!=null)
                {
                    amEmpEmployee.setHireUnit(afEmployeeCompanyDTO.getHireUnit());
                    amEmpEmployee.setPosition(afEmployeeCompanyDTO.getPosition());

                    Map<String,Object> param0 = new HashMap<>();
                    List<AmEmpTaskBO> list = null;
                    if(afEmployeeCompanyDTO.getHireUnit()==1){ //独立户
                        param0.put("companyId",afEmployeeCompanyDTO.getCompanyId());
                        list = baseMapper.querySocial(param0);
                    }else {//大库
                        list = baseMapper.querySocialCi();
                    }

                    if(list!=null&&list.size()>0)
                    {
                        amEmpEmployee.setSsAccount(list.get(0).getSsAccount());
                        amEmpEmployee.setSettlementArea(list.get(0).getSettlementArea());
                        amEmpEmployee.setAccountRepairDate(list.get(0).getAccountRepairDate());
                        amEmpEmployee.setSsPwd(list.get(0).getSsPwd());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }

            //单位性质
            CompanyTypeDTO companyTypeDTO = employeeInfoProxy.getCompanyType(bo.getCompanyId());
            amEmpEmployee.setCompanyType(companyTypeDTO==null?"":companyTypeDTO.getTypeName());

            //档案费
            try {
                Long ll = Long.parseLong(taskMsgDTO.getMissionId());
                List<AfEmpProductDTO> afEmpProductDTOList = afEmployeeProductProxy.getByEmpAgreement(ll,1);
                for(AfEmpProductDTO afEmpProductDTO:afEmpProductDTOList)
                {
                    if("CPJSW1800005".equals(afEmpProductDTO.getBasicProductId())){
                        amEmpEmployee.setFileFee("有");
                    }
                }
            } catch (NumberFormatException e) {

            }

            amEmpEmployeeService.insert(amEmpEmployee);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }



    }

    AmEmpTask setEmployeeId(AmEmpTask amEmpTask,TaskCreateMsgDTO taskMsgDTO){
        Object empCompanyId = taskMsgDTO.getVariables().get("empCompanyId");
        amEmpTask.setEmpCompanyId(empCompanyId.toString());
        AmEmpTaskBO bo = this.queryAmEmpTaskBO(empCompanyId);
        if(null!=bo){
            amEmpTask.setCompanyId(bo.getCompanyId());
            amEmpTask.setEmployeeId(bo.getEmployeeId());
            amEmpTask.setTaskFormContent(JSON.toJSONString(taskMsgDTO.getVariables()));
        }
        return  amEmpTask;
    }

    AmCustomBO  setCustomBO( AmEmpTask amEmpTask,AmCustomBO customBO,String companyName){
        if(amEmpTask!=null&&amEmpTask.getEmployCode()!=null)
        {
            if(amEmpTask.getEmployCode()==1){//是独立
                customBO.setCompanyName(companyName);
            }else if(amEmpTask.getEmployCode()==2){
                customBO.setCompanyName("中智上海经济技术合作公司");
            }else if(amEmpTask.getEmployCode()==3){
                customBO.setCompanyName(companyName);
                customBO.setCici("上海中智项目外包咨询服务有限公司");
            }
//            customBO.setTaskId(amEmpTask.getTaskId());
        }else{
            customBO.setCompanyName(companyName);
        }

        return customBO;
    }


}


