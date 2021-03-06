package com.ciicsh.gto.afsupportcenter.employmanagement.sitservice.host.controller;


import com.alibaba.fastjson.JSONObject;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.bo.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.utils.CommonApiUtils;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.utils.ReasonUtil;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.utils.TaskCommonUtils;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.custom.employSearchExportOpt;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dto.AmEmpCollectExportPageDTO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dto.AmEmpDispatchExportPageDTO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dto.AmEmpExplainExportDTO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dto.AmEmpExplainExportPageDTO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.sitservice.host.util.WordUtils;
import com.ciicsh.gto.afsupportcenter.util.ExcelUtil;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.aspect.log.Log;
import com.ciicsh.gto.afsupportcenter.util.interceptor.authenticate.UserContext;
import com.ciicsh.gto.afsupportcenter.util.logService.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogMessage;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 用工退工任务单 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/api/employservice/amEmpTask")
public class AmEmpTaskController extends BasicController<IAmEmpTaskService> {

    private final static Logger logger = LoggerFactory.getLogger(AmEmpTaskController.class);

    @Autowired
    private IAmEmpMaterialService iAmEmpMaterialService;

    @Autowired
    private IAmEmploymentService amEmploymentService;

    @Autowired
    private IAmArchiveService amArchiveService;

    @Autowired
    private IAmRemarkService amRemarkService;

    @Autowired
    private CommonApiUtils employeeInfoProxy;

    @Autowired
    private  AmEmpEmployeeService amEmpEmployeeService;

    @Autowired
    private  IAmEmpCustomService amEmpCustomService;

    @Autowired
    private IAmArchiveAdvanceService amArchiveAdvanceService;

    @Autowired
    private  IAmArchiveLinkService amArchiveLinkService;

    @Autowired
    private LogApiUtil logApiUtil;

    @Autowired
    private IAmEmployeChangeService  amEmployeChangeService;


    /**
     *用工资料任务单查询
     * @param pageInfo
     * @return
     */
    @Log("用工资料任务单查询")
    @RequestMapping("/queryAmEmpTask")
    public JsonResult<PageRows> queryAmEmpTask(PageInfo pageInfo) {

        PageRows<AmEmpTaskBO> result = business.queryAmEmpTask(pageInfo);

        List<AmEmpTaskBO> list = result.getRows();
        if(list!=null&&list.size()>0)
        {
            for(AmEmpTaskBO amEmpTaskBO:list)
            {
                if(amEmpTaskBO!=null&&amEmpTaskBO.getEmployCode()!=null)
                {
                    if(amEmpTaskBO.getEmployCode()==1){//是独立

                    }else if(amEmpTaskBO.getEmployCode()==2){
                        amEmpTaskBO.setTitle("中智上海经济技术合作公司");
                    }else if(amEmpTaskBO.getEmployCode()==3){
                        amEmpTaskBO.setCiCi("上海中智项目外包咨询服务有限公司");
                    }
                }
                if(!StringUtil.isEmpty(amEmpTaskBO.getEmploySpecial()))
                {
                    amEmpTaskBO.setEmploySpecial("有");
                }
            }
        }

        return JsonResultKit.of(result);

    }

    /**
     * 用工任务汇总统计
     * @param pageInfo
     * @return
     */
    @Log("用工任务汇总统计")
    @RequestMapping("/queryAmEmpTaskCount")
    public  JsonResult<AmEmpTaskCollection>  taskCount(PageInfo pageInfo){

        List<AmEmpTaskBO> list = business.taskCount(pageInfo);

        AmEmpTaskCountBO amEmpTaskCountBO = new AmEmpTaskCountBO();
        List<AmEmpTaskCountBO>  temp = new ArrayList<>();
        amEmpTaskCountBO.setAmount(list.size());
        int num =0;
        int otherNum=0;
        for(int i=0;i<list.size();i++)
        {
            AmEmpTaskBO amEmpTaskBO = list.get(i);
             int status = amEmpTaskBO.getTaskStatus();
            if(1==status){
                amEmpTaskCountBO.setNoSign(amEmpTaskBO.getCount());
                num = num + amEmpTaskBO.getCount();
            }else if(11==status){
                amEmpTaskCountBO.setBorrowKey(amEmpTaskBO.getCount());
                num = num + amEmpTaskBO.getCount();
            }else if(3==status){
                amEmpTaskCountBO.setEmploySuccess(amEmpTaskBO.getCount());
                num = num + amEmpTaskBO.getCount();
            }else if(4==status){
                amEmpTaskCountBO.setEmployFailed(amEmpTaskBO.getCount());
                num = num + amEmpTaskBO.getCount();
            }else if(5==status){
                amEmpTaskCountBO.setEmployCancel(amEmpTaskBO.getCount());
                num = num + amEmpTaskBO.getCount();
            }else if(66==status){
                amEmpTaskCountBO.setSystemCancel(amEmpTaskBO.getCount());
                num = num + amEmpTaskBO.getCount();
            }else{
                otherNum = otherNum+amEmpTaskBO.getCount();
                amEmpTaskCountBO.setOther(otherNum);
                num = num + amEmpTaskBO.getCount();
            }
            amEmpTaskCountBO.setAmount(num);
        }
        temp.add(amEmpTaskCountBO);
        AmEmpTaskCollection amEmpTaskCollection = new AmEmpTaskCollection();
        amEmpTaskCollection.setRow(temp);

        AmEmpTaskBO amEmpTaskBOCount = pageInfo.toJavaObject(AmEmpTaskBO.class);
        AmTaskStatusBO amTaskStatusBO = new AmTaskStatusBO();
        List<String> param = new ArrayList<String>();

        if (!StringUtil.isEmpty(amEmpTaskBOCount.getParams())) {
            String arr[] = amEmpTaskBOCount.getParams().split(",");
            for (int i = 0; i < arr.length; i++) {
                param.add(arr[i]);
            }
        }
        amEmpTaskBOCount.setParam(param);
        if(StringUtil.isEmpty(amEmpTaskBOCount.getJob()))
        {
            amEmpTaskBOCount.setJob("Y");
            List<AmEmpTaskBO> jobList = business.jobCount(amEmpTaskBOCount);
            amTaskStatusBO.setJob(jobList.get(0).getCount());
            amEmpTaskBOCount.setJob("N");
            List<AmEmpTaskBO> jobListOther = business.jobCount(amEmpTaskBOCount);
            amTaskStatusBO.setNoJob(jobListOther.get(0).getCount());
        }else{
            List<AmEmpTaskBO> jobList = business.jobCount(amEmpTaskBOCount);
            if("Y".equals(amEmpTaskBOCount.getJob()))
            {
                amTaskStatusBO.setJob(jobList.get(0).getCount());
                amEmpTaskBOCount.setJob("N");
                List<AmEmpTaskBO> jobListOther = business.jobCount(amEmpTaskBOCount);
                amTaskStatusBO.setNoJob(jobListOther.get(0).getCount());
            }else{
                amEmpTaskBOCount.setJob("Y");
                List<AmEmpTaskBO> jobListOther = business.jobCount(amEmpTaskBOCount);
                amTaskStatusBO.setJob(jobListOther.get(0).getCount());
                amTaskStatusBO.setNoJob(jobList.get(0).getCount());
            }
        }

        amEmpTaskCollection.setAmTaskStatusBO(amTaskStatusBO);
        return  JsonResultKit.of(amEmpTaskCollection);
    }

    /**
     * 用工办理查询
     * @param
     * @return
     */
    @Log("用工办理查询")
    @RequestMapping("/employeeDetailInfoQuery")
    public JsonResult employeeDetailInfoQuery(AmTaskParamBO amTaskParamBO) {

        /**
         * 获取雇员信息
         */
        AmEmpEmployeeBO amEmpEmployeeBO = amEmpEmployeeService.queryAmEmployeeByTaskId(amTaskParamBO.getEmpTaskId(),0);

        AmCustomBO amCustomBO1 = amEmpCustomService.getCustom(amTaskParamBO.getEmpTaskId());

        AmEmpTaskBO bo = new AmEmpTaskBO();
        bo.setEmployeeId(amTaskParamBO.getEmployeeId());
        bo.setCompanyId(amTaskParamBO.getCompanyId());

        //用工材料
        PageInfo pageInfo = new PageInfo();
        JSONObject params = new JSONObject();
        params.put("employeeId",amTaskParamBO.getEmployeeId());
        params.put("remarkType",amTaskParamBO.getRemarkType());
        params.put("companyId",amTaskParamBO.getCompanyId());
        params.put("operateType",new Integer(1));
        params.put("empTaskId",amTaskParamBO.getEmpTaskId());
        pageInfo.setParams(params);

        //用工材料
        AmMaterialBO amMaterialBO = new AmMaterialBO();
        List<AmEmpMaterialBO> empMaterialList = new ArrayList<>();
        PageRows<AmEmpMaterialBO> result = iAmEmpMaterialService.queryAmEmpMaterial(pageInfo);
        // 加了事务 回查
        result = iAmEmpMaterialService.queryAmEmpMaterial(pageInfo);
        //用工材料流转记录
        List<AmEmpMaterialOperationLogBO> logList = iAmEmpMaterialService.queryAmEmpMaterialOperationLogList(amTaskParamBO.getEmpTaskId());
        amMaterialBO.setLogBOList(logList);
        empMaterialList.addAll(result.getRows());
        amMaterialBO.setMaterialsData(empMaterialList);
        if(result.getRows().size()>0){
            String submitterId = result.getRows().get(0).getSubmitterId();
            String submitterName = result.getRows().get(0).getSubmitterName();
            String extension = result.getRows().get(0).getExtension();
            if(null!=result.getRows().get(0)){
                amMaterialBO.setReasonValue(result.getRows().get(0).getRejectReason());
            }

            if("system".equals(submitterId)){
                amMaterialBO.setSubmitName("自动提交");
            }else {
                amMaterialBO.setSubmitName(submitterName);
                amMaterialBO.setExtension(extension);
            }
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //用工信息
        List<AmEmploymentBO> resultEmployList = amEmploymentService.queryAmEmployment(params);
        //用工档案
        AmArchiveBO amArchiveBO = null;
        if(null!=resultEmployList&&resultEmployList.size()>0)
        {
            params.put("employmentId",resultEmployList.get(0).getEmploymentId());
            List<AmArchiveBO> amArchiveBOList = amArchiveService.queryAmArchiveList(params);
            if(null!=amArchiveBOList&&amArchiveBOList.size()>0)
            {
                amArchiveBO = amArchiveBOList.get(0);
                if(!StringUtil.isEmpty(amArchiveBO.getEmployFeedback()))
                {
                    if(null!=amArchiveBO.getEmployFeedback()&&!"11".equals(amArchiveBO.getEmployFeedback())){
                        amArchiveBO.setEnd(true);
                    }
                }

                List<AmArchiveLink> amArchiveLinks = amArchiveLinkService.queryByArchiveId(amArchiveBO.getArchiveId());
                if(null!=amArchiveLinks&&amArchiveLinks.size()>0)
                {
                    resultMap.put("archiveNote",amArchiveLinks);
                }

            }
        }

        //用工备注
        PageRows<AmRemarkBO> amRemarkBOPageRows = amRemarkService.queryAmRemark(pageInfo);


        AmRemarkBO queryBo = new AmRemarkBO();
        queryBo.setEmpTaskId(amTaskParamBO.getEmpTaskId());
        //档案备注
        queryBo.setRemarkType(2);
        List<AmRemarkBO> archiveAmRemarkBOList = amRemarkService.getAmRemakList(queryBo);

        //档案备注
        if(null!=archiveAmRemarkBOList&&archiveAmRemarkBOList.size()>0)
        {
            resultMap.put("archiveRemarkBo",archiveAmRemarkBOList);
        }

        //客户信息
        if(null!=amCustomBO1){
            resultMap.put("customerInfo",amCustomBO1);
        }
        //雇员信息
        if(null!=amEmpEmployeeBO){
            resultMap.put("amEmpTaskBO",amEmpEmployeeBO);
        }

        resultMap.put("amMaterialBO",amMaterialBO);

        if(null!=amArchiveBO){
            resultMap.put("amArchaiveBo",amArchiveBO);
        }

        // 预留档案类别
        List<AmArchiveDocSeqBO> boList = amArchiveService.queryAmArchiveDocTypeByType(1);
        List<AmArchiveDocSeqBO> boList2 = amArchiveService.queryAmArchiveDocTypeByType(2);
        resultMap.put("docSeqList",boList);
        resultMap.put("docSeqList2",boList2);

        if(null!= resultEmployList&&resultEmployList.size()>0)
        {
            resultMap.put("amEmploymentBO",resultEmployList.get(0));
        }

        if(null!=amRemarkBOPageRows)
        {
            resultMap.put("amRemarkBo",amRemarkBOPageRows);
        }

        UserInfoBO userInfoBO = new UserInfoBO();
        userInfoBO.setUserName(ReasonUtil.getUserName());

        resultMap.put("userInfo",userInfoBO);

        return JsonResultKit.of(resultMap);

    }

    /**
     * 保存用工信息
     */
    @Log("保存用工信息")
    @RequestMapping("/saveEmployee")
    public JsonResult<AmEmployment> saveEmployee(AmEmployment entity) {

        String userId = ReasonUtil.getUserId();
        String userName = ReasonUtil.getUserName();
        LocalDateTime now = LocalDateTime.now();
        if(entity.getEmploymentId()==null){
            entity.setCreatedTime(now);
            entity.setModifiedTime(now);
            entity.setCreatedBy(userId);
            entity.setModifiedBy(userId);
            entity.setIsActive(1);

        }else{
            AmEmployment entity1 = amEmploymentService.selectById(entity.getEmploymentId());
            entity.setCreatedBy(entity1.getCreatedBy());
            entity.setCreatedTime(entity1.getCreatedTime());
            entity.setIsActive(1);
            entity.setModifiedTime(now);
            entity.setModifiedBy(userId);
        }
        entity.setEmployOperateMan(userName);
        amEmploymentService.insertOrUpdateAllColumn(entity);
        return JsonResultKit.of(entity);
    }

    @RequestMapping("/saveEmployeeCheck")
    public JsonResult<Boolean> saveEmployeeCheck(AmEmployment entity) {
        /**
         * 如果存在更新合同字段，更新的合同开始时间和实际用工时间比较
         */
        AmEmployeChange amEmployeChange = amEmployeChangeService.getEmployeeChange(entity.getEmpTaskId());
        if(null!=amEmployeChange)
        {
            if(null!=amEmployeChange.getLaborStartDate()&&null!=entity.getEmployDate())
            {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String ldate = sdf.format(amEmployeChange.getLaborStartDate());

                if(!ldate.toString().equals(entity.getEmployDate().toString()))
                {
                    return  JsonResultKit.of(true);
                }
                return  JsonResultKit.of(false);
            }
        }
        return  JsonResultKit.of(false);
    }

    /**
     * 保存用工档案
     * @param bo
     * @return
     */
    @Log("保存用工档案")
    @RequestMapping("/saveAmArchive")
    public  JsonResult<AmArchiveBO>  saveAmArchive(AmArchiveBO amArchiveBO){

        Map<String,Object> map = null;

        try {
            map = amArchiveService.saveArchive(amArchiveBO);
        } catch (Exception e) {
            return JsonResultKit.of(null);
        }

        Boolean result = (Boolean)map.get("result");
        AmArchive entity = (AmArchive)map.get("entity");
        amArchiveBO.setArchiveId(entity.getArchiveId());
        if(result){
            if(null!=amArchiveBO.getEmployFeedback()&&!"11".equals(amArchiveBO.getEmployFeedback())){
                amArchiveBO.setEnd(true);
            }
        }

        String taskId = null;
        if(map.get("taskId")!=null)
        {
            taskId = map.get("taskId").toString();
        }

        //如果满足在用工办理页面提交
        if("0".equals(amArchiveBO.getIsFrist()))
        {
            if(result&&!StringUtil.isEmpty(entity.getEmployFeedback()))
            {
                /**
                 * u盘外借 不会调用complateTask,只发kafaka消息
                 */
                if("11".equals(entity.getEmployFeedback()))
                {

                }else{
                    Map<String,Object> variables = new HashMap<>();
                    variables.put("status", ReasonUtil.getYgResult(entity.getEmployFeedback()));
                    variables.put("remark",ReasonUtil.getYgfk(entity.getEmployFeedback()));
                    String userName = "system";
                    try {
                        userName = UserContext.getUser().getDisplayName();
                    } catch (Exception e) {

                    }
                    variables.put("assignee",userName);
                    try {
                        TaskCommonUtils.completeTask(taskId,employeeInfoProxy,variables);
                    } catch (Exception e) {
                        logApiUtil.error(LogMessage.create().setTitle("EmployMaterial").setContent(e.getMessage()));
                    }
                }
            }
        }

        return JsonResultKit.of(amArchiveBO);
    }

    /**
     * 保存用工档案寄信
     * @param bo
     * @return
     */
    @Log("保存用工档案")
    @RequestMapping("/saveAmArchiveSend")
    public  JsonResult<Boolean>  saveAmArchiveSend(AmPostBO amPostBO){

        Boolean result = amArchiveService.saveArchiveSend(amPostBO);

        return JsonResultKit.of(result);
    }

    @PostMapping("/saveAmRemark")
    @Log("保存用工备注信息")
    public JsonResult  saveAmRemark(AmRemark bo) {

        if(bo.getRemarkId()==null)
        {
            LocalDateTime now = LocalDateTime.now();
            bo.setCreatedTime(now);
            bo.setModifiedTime(now);
            bo.setCreatedBy(ReasonUtil.getUserId());
            bo.setModifiedBy(ReasonUtil.getUserId());
        }
        bo.setRemarkDate(LocalDate.now());

        boolean result = false;
        try {
            result = amRemarkService.insert(bo);
        } catch (Exception e) {

        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("result",result);
        resultMap.put("data",bo);

        return JsonResultKit.of(resultMap);

    }

    @Log("用工备注查询")
    @RequestMapping("/queryAmRemark")
    public JsonResult queryAmRemark(PageInfo pageInfo) {
        PageRows<AmRemarkBO> result = amRemarkService.queryAmRemark(pageInfo);
        String userName = "System";
        try {
            userName = UserContext.getUser().getDisplayName();
        } catch (Exception e) {

        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userName",userName);
        resultMap.put("result",result);
        return JsonResultKit.of(resultMap);
    }

    @RequestMapping("/deleteAmRemark")
    public JsonResult<Boolean>  deleteAmRemark(Long amRemarkId){
        boolean  result = amRemarkService.deleteAmRemark(amRemarkId);

        return JsonResultKit.of(result);
    }

    @PostMapping("/receiveMaterial")
    @Transactional(
        rollbackFor = {Exception.class}
    )
    public JsonResult receiveMaterial(@RequestBody List<AmEmpMaterial> list){
        String userName = "system";
        String userId = "system";
        try {
            userName = UserContext.getUser().getDisplayName();
            userId = UserContext.getUser().getUserId();
        } catch (Exception e) {

        }
        for(AmEmpMaterial material:list)
        {
            // 1签收 2批退
            if(1 == material.getOperateType()){
                material.setReceiveId(userId);
                material.setReceiveName(userName);
                material.setReceiveDate(LocalDate.now());
            }else if(2 == material.getOperateType()){
                material.setRejectId(userId);
                material.setRejectName(userName);
                material.setRejectDate(LocalDate.now());
            }
            material.setModifiedTime(LocalDateTime.now());
            material.setModifiedBy(userId);
        }

        AmEmpTask amEmpTask = business.selectById(list.get(0).getEmpTaskId());
        // 调用雇员中心 签收
        String message = iAmEmpMaterialService.receiveMaterial(amEmpTask.getHireTaskId(),1,null);
        Map<String,Object> map = new HashMap<>();
        if("签收成功".equals(message)){
            //材料签收成功状态不变
//            amEmpTask.setTaskStatus(2);
//            business.insertOrUpdate(amEmpTask);
            boolean result =  iAmEmpMaterialService.updateBatchById(list);
            List<AmEmpMaterialOperationLogBO> logList = iAmEmpMaterialService.queryAmEmpMaterialOperationLogList(list.get(0).getEmpTaskId());
            map.put("logList",logList);
        }
        map.put("result",message);
        map.put("data",list);
        return JsonResultKit.of(map);
    }

    @PostMapping("/rejectMaterial")
    public JsonResult rejectMaterial(@RequestBody List<AmEmpMaterial> list){

        String userName = "system";
        String userId = "system";
        try {
            userName = UserContext.getUser().getDisplayName();
            userId = UserContext.getUser().getUserId();
        } catch (Exception e) {

        }
        for(AmEmpMaterial material:list)
        {
            material.setRejectDate(LocalDate.now());
            material.setRejectName(userName);
            material.setRejectId(userId);
            material.setModifiedTime(LocalDateTime.now());
//            material.setActive(false);
        }
        AmEmpTask amEmpTask = business.selectById(list.get(0).getEmpTaskId());
        // 调用雇员中心 批退
        String message = iAmEmpMaterialService.receiveMaterial(amEmpTask.getHireTaskId(),2,list.get(0).getRejectReason());
        Map<String,Object> map = new HashMap<>();
        if("批退成功".equals(message)){
            boolean result =  iAmEmpMaterialService.updateBatchById(list);
            List<AmEmpMaterialOperationLogBO> logList = iAmEmpMaterialService.queryAmEmpMaterialOperationLogList(list.get(0).getEmpTaskId());
            map.put("logList",logList);
        }
        map.put("result",message);
        map.put("data",list);
        return JsonResultKit.of(map);
    }

    @RequestMapping("/updateTaskStatus")
    public  JsonResult<Boolean>  updateTaskStatus(String employmentId){
        Map<String,Object>  param = new HashMap<>();
        param.put("employmentId",employmentId);
       boolean result = business.updateTaskStatus(param);
        return JsonResultKit.of(result);
    }

    /**
     * 雇员社保查询查询导出
     */
    @RequestMapping("/employSearchExportOpt")
    public void employSearchExportOpt(HttpServletResponse response, AmEmpTaskBO amEmpTaskBO) {

        List<String> param = new ArrayList<String>();
        List<String> orderParam = new ArrayList<String>();
        if (!StringUtil.isEmpty(amEmpTaskBO.getParams())) {
            String arr[] = amEmpTaskBO.getParams().split(",");
            for (int i = 0; i < arr.length; i++) {
                if(!StringUtil.isEmpty(arr[i]))
                {
                    if(arr[i].indexOf("desc")>0||arr[i].indexOf("asc")>0){
                        orderParam.add(arr[i]);
                    }else {
                        param.add(arr[i]);
                    }
                }

            }
            if(amEmpTaskBO.getParams().indexOf("material_name")!=-1){
                amEmpTaskBO.setMaterial("1");
            }
        }

        amEmpTaskBO.setParam(param);
        amEmpTaskBO.setOrderParam(orderParam);

        if (null != amEmpTaskBO.getTaskStatus() && amEmpTaskBO.getTaskStatus() == 0) {
            amEmpTaskBO.setTaskStatus(null);
        }

        if(amEmpTaskBO.getTaskStatus()!=null&&amEmpTaskBO.getTaskStatus()==6){
            amEmpTaskBO.setTaskStatusOther(0);
        }

        Date date = new Date();
        String fileNme = "用工任务单_"+ StringUtil.getDateString(date)+".xls";

        List<employSearchExportOpt> opts = business.queryAmEmpTaskList(amEmpTaskBO);

        for(employSearchExportOpt employSearchExportOpt:opts)
        {
            if(employSearchExportOpt.getEmployCode()!=null)
            {
                if(employSearchExportOpt.getEmployCode()==2){//代理也就是独立

                }else if(employSearchExportOpt.getEmployCode()==1){
                    employSearchExportOpt.setTitle("中智上海经济技术合作公司");
                }else if(employSearchExportOpt.getEmployCode()==3){
                    String str = "上海中智项目外包咨询服务有限公司";
                    str = employSearchExportOpt.getTitle()+" "+str;
                    employSearchExportOpt.setTitle(str);
                }
            }
            if(!StringUtil.isEmpty(employSearchExportOpt.getEmploySpecial()))
            {
                employSearchExportOpt.setEmploySpecial("有");
            }
        }

        ExcelUtil.exportExcel(opts,employSearchExportOpt.class,fileNme,response);
    }

    /**
     * 用工录用名册打印导出Word
     */
    @RequestMapping("/employSearchExportOptUseWord")
    public @ResponseBody
    void employSearchExportOptUseWord(AmEmpTaskBO amEmpTaskBO,HttpServletResponse response){

        try {

            logApiUtil.info(LogMessage.create().setTitle("employSearchExportOptUseWord").setContent("用工录用名册打印 start"));

            // 中智大库
            List<AmEmpDispatchExportPageDTO> dtoList = business.queryExportOptDispatch(amEmpTaskBO,2,12);

            // 外包
            List<AmEmpDispatchExportPageDTO> dtoList2 = business.queryExportOptDispatch(amEmpTaskBO,3,12);

            //独立户
            List<AmEmpDispatchExportPageDTO> dtoList3 = business.queryExportOptDispatch(amEmpTaskBO,12);


            Map<String, Object> map = new HashMap<>();

            map.put("list",dtoList);
            map.put("list2",dtoList2);
            map.put("list3",dtoList3);


            WordUtils.exportMillCertificateWord(response,map,"用工录用名册","AM_USE_TEMP.ftl");

        } catch (Exception e) {
            logApiUtil.error(LogMessage.create().setTitle("employSearchExportOptUseWord").setContent(e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 派遣录用名册打印导出Word
     */
    @RequestMapping("/employSearchExportOptDispatchWord")
    public @ResponseBody
    void employSearchExportOptDispatchWord(AmEmpTaskBO amEmpTaskBO,HttpServletResponse response){



        try {
            // 中智大库
            List<AmEmpDispatchExportPageDTO> dtoList = business.queryExportOptDispatch(amEmpTaskBO,2,9);

            // 外包
            List<AmEmpDispatchExportPageDTO> dtoList2 = business.queryExportOptDispatch(amEmpTaskBO,3,9);

            //独立户
            List<AmEmpDispatchExportPageDTO> dtoList3 = business.queryExportOptDispatch(amEmpTaskBO,9);


            Map<String, Object> map = new HashMap<>();

            map.put("list",dtoList);
            map.put("list2",dtoList2);
            map.put("list3",dtoList3);
            WordUtils.exportMillCertificateWord(response,map,"派遣录用名册","AM_DISPATCH_TEMP.ftl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 外来独立打印导出Word
     */
    @RequestMapping("/employSearchExportOptAlonehWord")
    public @ResponseBody
    void employSearchExportOptAlonehWord(AmEmpTaskBO amEmpTaskBO,HttpServletResponse response){

        // 中智大库
        List<AmEmpDispatchExportPageDTO> dtoList = business.queryExportOptDispatch(amEmpTaskBO,2,10);

        // 外包
        List<AmEmpDispatchExportPageDTO> dtoList2 = business.queryExportOptDispatch(amEmpTaskBO,3,10);

        //独立户
        List<AmEmpDispatchExportPageDTO> dtoList3 = business.queryExportOptDispatch(amEmpTaskBO,10);

        Integer count = 0;
        for (AmEmpDispatchExportPageDTO dto:dtoList) {
            count += dto.getList().size();
        }
        for (AmEmpDispatchExportPageDTO dto:dtoList2) {
            count += dto.getList().size();
        }
        for (AmEmpDispatchExportPageDTO dto:dtoList3) {
            count += dto.getList().size();
        }

        Map<String, Object> map = new HashMap<>();

        map.put("list",dtoList);
        map.put("list2",dtoList2);
        map.put("list3",dtoList3);
        map.put("count",count);

        try {
            WordUtils.exportMillCertificateWord(response,map,"外来独立","AM_ALONE_TEMP.ftl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 外来派遣导出Word
     */
    @RequestMapping("/employSearchExportOptExtDispatchWord")
    public @ResponseBody
    void employSearchExportOptExtDispatchWord(AmEmpTaskBO amEmpTaskBO,HttpServletResponse response){

        // 中智大库
        List<AmEmpDispatchExportPageDTO> dtoList = business.queryExportOptDispatch(amEmpTaskBO,2,9);

        // 外包
        List<AmEmpDispatchExportPageDTO> dtoList2 = business.queryExportOptDispatch(amEmpTaskBO,3,9);

        //独立户
        List<AmEmpDispatchExportPageDTO> dtoList3 = business.queryExportOptDispatch(amEmpTaskBO,9);

        Integer count = 0;
        for (AmEmpDispatchExportPageDTO dto:dtoList) {
            count += dto.getList().size();
        }
        for (AmEmpDispatchExportPageDTO dto:dtoList2) {
            count += dto.getList().size();
        }
        for (AmEmpDispatchExportPageDTO dto:dtoList3) {
            count += dto.getList().size();
        }

        Map<String, Object> map = new HashMap<>();

        map.put("list",dtoList);
        map.put("list2",dtoList2);
        map.put("list3",dtoList3);
        map.put("count",count);

        try {
            WordUtils.exportMillCertificateWord(response,map,"外来派遣","AM_EXT_DISPATCH_TEMP.ftl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 采集表汇总表导出Word
     */
    @RequestMapping("/employSearchExportOptExtCollectWord")
    public @ResponseBody
    void employSearchExportOptExtCollectWord(HttpServletResponse response, AmEmpTaskBO amEmpTaskBO){
        // 中智大库
        List<AmEmpCollectExportPageDTO> dtoList = business.queryExportOptCollect(amEmpTaskBO,2);

        // 外包
        List<AmEmpCollectExportPageDTO> dtoList2 = business.queryExportOptCollect(amEmpTaskBO,3);

        //独立户
        List<AmEmpCollectExportPageDTO> dtoList3 = business.queryExportOptCollect(amEmpTaskBO);

        Integer sum = 0;

        for (AmEmpCollectExportPageDTO dto:dtoList) {
            sum += dto.getList1().size();
            sum += dto.getList2().size();
            sum += dto.getList3().size();
        }
        for (AmEmpCollectExportPageDTO dto:dtoList2) {
            sum += dto.getList1().size();
            sum += dto.getList2().size();
            sum += dto.getList3().size();
        }
        for (AmEmpCollectExportPageDTO dto:dtoList3) {
            sum += dto.getList1().size();
            sum += dto.getList2().size();
            sum += dto.getList3().size();
        }

        Map<String, Object> map = new HashMap<>();

        map.put("list",dtoList);
        map.put("list2",dtoList2);
        map.put("list3",dtoList3);
        map.put("sum",sum);

        try {
            WordUtils.exportMillCertificateWord(response,map,"采集表汇总表","AM_COLLECT_TEMP.ftl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用工外来情况说明导出Word
     */
    @RequestMapping("/employSearchExportOptExtExplainWord")
    public @ResponseBody
    void employSearchExportOptExtExplainWord(HttpServletResponse response, AmEmpTaskBO amEmpTaskBO){
        // 中智大库
        List<AmEmpExplainExportPageDTO> dtoList = business.queryExportOptExplain(amEmpTaskBO,2);

        // 外包
        List<AmEmpExplainExportPageDTO> dtoList2 = business.queryExportOptExplain(amEmpTaskBO,3);

        //独立户
        List<AmEmpExplainExportPageDTO> dtoList3 = business.queryExportOptExplain(amEmpTaskBO);

        Integer count = 0;
        for (AmEmpExplainExportPageDTO dto:dtoList) {
            for (AmEmpExplainExportDTO d:dto.getList()) {
                if(d.getEmployeeName()!=null){
                    count++;
                }
            }
        }
        for (AmEmpExplainExportPageDTO dto:dtoList2) {
            for (AmEmpExplainExportDTO d:dto.getList()) {
                if(d.getEmployeeName()!=null){
                    count++;
                }
            }
        }
        for (AmEmpExplainExportPageDTO dto:dtoList3) {
            for (AmEmpExplainExportDTO d:dto.getList()) {
                if(d.getEmployeeName()!=null){
                    count++;
                }
            }
        }

        Map<String, Object> map = new HashMap<>();

        map.put("list",dtoList);
        map.put("list2",dtoList2);
        map.put("list3",dtoList3);
        map.put("count",count);

        try {
            WordUtils.exportMillCertificateWord(response,map,"外来情况说明","AM_EXPLAIN_TEMP.ftl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/getDefualtEmployBO")
    public  JsonResult<AmEmpTaskBO>  getDefualtEmployBO(AmEmpTaskBO amEmpTaskBO){
        AmEmpTaskBO amEmpTaskBO1 = business.getDefualtEmployBO(amEmpTaskBO);
        return JsonResultKit.of(amEmpTaskBO1);
    }

    /**
     * 档案类别 预留档案类别查询
     * @param
     * @return
     */
    @Log("档案类别查询")
    @RequestMapping("/queryAmArchiveDocType")
    public JsonResult queryAmArchiveDocType() {
        // 预留档案类别
        List<AmArchiveDocSeqBO> boList = amArchiveService.queryAmArchiveDocTypeByType(1);
        List<AmArchiveDocSeqBO> boList2 = amArchiveService.queryAmArchiveDocTypeByType(2);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("docSeqList",boList);
        resultMap.put("docSeqList2",boList2);
        return JsonResultKit.of(resultMap);
    }

    @PostMapping("/saveBatchEmploy")
    public JsonResult<AmArchiveBO>  saveBatchEmploy(@RequestBody AmArchiveBO amArchiveBO) {
        Map<String,Object>  map  = business.batchSaveEmployee(amArchiveBO);
        Boolean result = (Boolean)map.get("result");

        if(null!=map.get("size"))
        {
            amArchiveBO.setRemark("请先保存用工");
            return JsonResultKit.of(amArchiveBO);
        }

        if(result)
        {
            /**
             * 如果数据保存成功并且用工完成，调用工作流
             */
            if(null!=amArchiveBO.getEmployFeedback()&&!"11".equals(amArchiveBO.getEmployFeedback()))
            {
                Map<String,Object> variables = new HashMap<>();
                variables.put("status", ReasonUtil.getYgResult(amArchiveBO.getEmployFeedback()));
                variables.put("remark",ReasonUtil.getYgfk(amArchiveBO.getEmployFeedback()));
                String userName = "system";
                try {
                    userName = UserContext.getUser().getDisplayName();
                } catch (Exception e) {

                }
                variables.put("assignee",userName);
                List<String> taskIdList = (List<String>)map.get("taskIdList");
                for(String taskId:taskIdList)
                {
                    try {
                        TaskCommonUtils.completeTask(taskId,employeeInfoProxy,variables);
                    } catch (Exception e) {
                        logApiUtil.error(LogMessage.create().setTitle("EmployMaterial").setContent(e.getMessage()));
                    }
                }
                amArchiveBO.setEnd(true);
            }
        }

        return JsonResultKit.of(amArchiveBO);
    }

    @RequestMapping("/batchSaveEmployment")
    public JsonResult batchSaveEmployment(EmployeeBatchBO employeeBatchBO){
        Map<String,Object> map = business.batchSaveEmployment(employeeBatchBO);
        return  JsonResultKit.of(map);
    }

    @RequestMapping("/batchCheck")
    public JsonResult  batchCheck(EmployeeBatchBO employeeBatchBO){
        Map<String,Object>  map = business.batchCheck(employeeBatchBO);
        return  JsonResultKit.of(map);
    }

    @RequestMapping("/batchCheckArchive")
    public JsonResult  batchCheckArchive(EmployeeBatchBO employeeBatchBO){
        Map<String,Object>  map = business.batchCheckArchive(employeeBatchBO);
        return  JsonResultKit.of(map);
    }

    @PostMapping("/saveBatchArchive")
    public JsonResult  saveBatchArchive(AmArchiveBO amArchiveBO) {
        Map<String,Object>  map  = business.batchSaveArchive(amArchiveBO);
        return  JsonResultKit.of(map);
    }

}

