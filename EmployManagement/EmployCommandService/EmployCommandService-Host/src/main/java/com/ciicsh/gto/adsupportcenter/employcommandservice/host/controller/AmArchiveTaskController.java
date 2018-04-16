package com.ciicsh.gto.adsupportcenter.employcommandservice.host.controller;

import com.alibaba.fastjson.JSONObject;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.bo.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.business.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.business.utils.ReasonUtil;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.dto.AmArchiveDTO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.entity.AmArchiveUse;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.entity.AmEmpMaterial;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.entity.AmEmpTask;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.entity.AmInjury;
import com.ciicsh.gto.afsupportcenter.employmanagement.employcommandservice.entity.custom.archiveSearchExportOpt;
import com.ciicsh.gto.afsupportcenter.util.ExcelUtil;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.interceptor.authenticate.UserContext;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by zhangzhiwen on 2018/2/6.
 */

@RestController
@RequestMapping("/api/employcommandservice/amArchiveTask")
public class AmArchiveTaskController extends BasicController<IAmEmploymentService> {

    @Autowired
    private IAmArchiveService amArchiveService;

   @Autowired
   private IAmInjuryService amInjuryService;

   @Autowired
   private IAmEmpMaterialService amEmpMaterialService;

    @Autowired
    private IAmRemarkService amRemarkService;

    @Autowired
    private IAmEmpMaterialService iAmEmpMaterialService;

    @Autowired
    private  IAmArchiveUseService iAmArchiveUseService;

    @Autowired
    private IAmEmploymentService amEmploymentService;

    @Autowired
    private  IAmResignService amResignService;

    @Autowired
    private IAmEmpTaskService taskService;

    @Autowired
    private  AmEmpEmployeeService amEmpEmployeeService;

    @Autowired
    private  IAmEmpCustomService amEmpCustomService;

    @RequestMapping("/queryAmArchive")
    public JsonResult<PageRows> queryAmArchive(PageInfo pageInfo){
        PageRows<AmEmploymentBO> result = business.queryAmArchive(pageInfo);
        List<AmEmploymentBO> data = result.getRows();
        for(AmEmploymentBO amEmploymentBO:data)
        {
            if(!StringUtil.isEmpty(amEmploymentBO.getResignFeedback1())){
                amEmploymentBO.setResignFeedback1(ReasonUtil.getTgfk(amEmploymentBO.getResignFeedback1()));
            }

            if(!StringUtil.isEmpty(amEmploymentBO.getEmployFeedback())){
                amEmploymentBO.setEmployFeedback(ReasonUtil.getYgfk(amEmploymentBO.getEmployFeedback()));
            }

            if(amEmploymentBO!=null&&amEmploymentBO.getEmployCode()!=null)
            {
                if(amEmploymentBO.getEmployCode()==1){//是独立

                }else if(amEmploymentBO.getEmployCode()==2){
                    amEmploymentBO.setTitle("中智上海经济技术合作公司");
                }else if(amEmploymentBO.getEmployCode()==3){
                    amEmploymentBO.setCici("上海中智项目外包咨询服务有限公司");
                }
            }

            if(!StringUtil.isEmpty(amEmploymentBO.getArchiveSpecial()))
            {
                int last = amEmploymentBO.getArchiveSpecial().lastIndexOf(",");
                amEmploymentBO.setArchiveSpecial(amEmploymentBO.getArchiveSpecial().substring(0,last));
            }
        }

        return JsonResultKit.of(result);
    }

    @RequestMapping("/queryAmEmpTaskCount")
    public  JsonResult<AmEmpTaskCollection>  taskCountEmployee(PageInfo pageInfo){

        List<AmEmploymentBO> list = business.taskCountEmployee(pageInfo);

        AmEmpTaskCountBO amEmpTaskCountBO = new AmEmpTaskCountBO();
        List<AmEmpTaskCountBO>  temp = new ArrayList<>();
        amEmpTaskCountBO.setAmount(list.size());
        int num =0;
        int otherNum =0;
        for(int i=0;i<list.size();i++)
        {
            AmEmploymentBO amEmploymentBO = list.get(i);
            int status = amEmploymentBO.getTaskStatus();
            if(1==status){
                amEmpTaskCountBO.setNoSign(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(2==status){
                amEmpTaskCountBO.setFinished(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(3==status){
                amEmpTaskCountBO.setEmploySuccess(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(4==status){
                amEmpTaskCountBO.setEmployFailed(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(5==status){
                amEmpTaskCountBO.setEmployCancel(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else{
                otherNum = otherNum + amEmploymentBO.getCount();
                amEmpTaskCountBO.setOther(otherNum);
                num = num + amEmploymentBO.getCount();
            }
            amEmpTaskCountBO.setAmount(num);

        }
        temp.add(amEmpTaskCountBO);
        AmEmpTaskCollection amEmpTaskCollection = new AmEmpTaskCollection();
        amEmpTaskCollection.setRow(temp);
        return  JsonResultKit.of(amEmpTaskCollection);

    }

    @RequestMapping("/queryResignTaskCount")
    public  JsonResult<AmResignCollection>  taskCountResign(PageInfo pageInfo){
        
        List<AmEmploymentBO> list = business.taskCountResign(pageInfo);

        AmResTaskCountBO amEmpTaskCountBO = new AmResTaskCountBO();
        List<AmResTaskCountBO>  temp = new ArrayList<>();
        amEmpTaskCountBO.setAmount(list.size());
        int num =0;
        int otherNum =0;
        for(int i=0;i<list.size();i++)
        {
            AmEmploymentBO amEmploymentBO = list.get(i);
            int status = amEmploymentBO.getTaskStatus();
            if(1==status){
                amEmpTaskCountBO.setNoFeedback(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(2==status){
                amEmpTaskCountBO.setRefuseFailed(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(3==status){
                amEmpTaskCountBO.setRefuseBeforeWithFile(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(4==status){
                amEmpTaskCountBO.setRefuseTicketStampNoReturn(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(5==status){
                amEmpTaskCountBO.setRefuseFailed(amEmploymentBO.getCount());
                num = num + amEmploymentBO.getCount();
            }else if(6==status){
                amEmpTaskCountBO.setBeforeBatchNeedRefuse(amEmploymentBO.getCount());
            }else{
                otherNum = otherNum + amEmploymentBO.getCount();
                amEmpTaskCountBO.setOther(otherNum);
                num = num + amEmploymentBO.getCount();
            }
            amEmpTaskCountBO.setAmount(num);

        }
        temp.add(amEmpTaskCountBO);
        AmResignCollection  amResignCollection = new AmResignCollection ();
        amResignCollection.setRow(temp);
        return  JsonResultKit.of(amResignCollection);
    }

    @RequestMapping("/archiveDetailInfoQuery")
    public JsonResult archiveDetailInfoQuery(AmTaskParamBO amTaskParamBO){

        /**
         * 获取雇员信息
         */
        AmEmpEmployeeBO amEmpEmployeeBO = amEmpEmployeeService.queryAmEmployeeByTaskId(amTaskParamBO.getEmpTaskId());

        AmCustomBO amCustomBO = amEmpCustomService.getCustom(amTaskParamBO.getEmpTaskId());

        AmResignBO amResignBO = new AmResignBO();

        PageInfo pageInfo = new PageInfo();
        JSONObject params = new JSONObject();
        params.put("employeeId",amTaskParamBO.getEmployeeId());
        params.put("remarkType",amTaskParamBO.getRemarkType());
        params.put("empTaskId",amTaskParamBO.getEmpTaskId());
        params.put("employmentId",amTaskParamBO.getEmploymentId());
        params.put("companyId",amTaskParamBO.getCompanyId());
        params.put("operateType",new Integer(2));
        params.put("empTaskResignId",amTaskParamBO.getEmpTaskResignId());
        pageInfo.setParams(params);


        //用工档案
        List<AmArchiveBO> amArchiveBOList = amArchiveService.queryAmArchiveList(params);
        //档案备注
        PageRows<AmRemarkBO> amRemarkBOPageRows = amRemarkService.queryAmRemark(pageInfo);

        //用工备注
        AmRemarkBO queryBo = new AmRemarkBO();
        queryBo.setRemarkType(1);
        queryBo.setEmpTaskId(amTaskParamBO.getEmpTaskId());

        List<AmRemarkBO> amRemarkBOList = amRemarkService.getAmRemakList(queryBo);

        //退工归还材料签收
        PageRows<AmEmpMaterialBO> result = null;

        PageRows<AmEmpMaterialBO> resultMaterial = iAmEmpMaterialService.queryMaterialDic(pageInfo);

        //用工信息
        List<AmEmploymentBO> resultEmployList = amEmploymentService.queryAmEmployment(params);

        List<AmResignBO> listResignBO = amResignService.queryAmResignDetail(params);

        //退工信息
        if(null!=listResignBO&&listResignBO.size()>0){
            AmResignBO resignBO = listResignBO.get(0);
            if(resignBO.getIsFinish()!=null&&1==resignBO.getIsFinish())
            {
                amResignBO = listResignBO.get(0);
                if(!StringUtil.isEmpty(amResignBO.getResignFeedback())){
                    amResignBO.setResignFeedback(ReasonUtil.getTgfk(amResignBO.getResignFeedback()));
                }
                if(!StringUtil.isEmpty(amResignBO.getIfLaborManualReturn())){
                    amResignBO.setIfLaborManualReturnStr(ReasonUtil.getIsTj(amResignBO.getIfLaborManualReturn().toString()));
                }

                AmEmpTask amEmpTask = taskService.selectById(amTaskParamBO.getEmpTaskResignId());

                if(null!=amEmpTask){
                    java.text.DateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    if(null!=amEmpTask.getOutDate()){
                        amResignBO.setOutDate(sdf.format(amEmpTask.getOutDate()));
                    }
                    amResignBO.setOutReason(amEmpTask.getOutReason());
                }
            }
            params.put("empTaskId",resignBO.getEmpTaskId());
            pageInfo.setParams(params);
            result = iAmEmpMaterialService.queryAmEmpMaterial(pageInfo);

        }


        AmEmploymentBO amEmploymentBO = new AmEmploymentBO();
        if(null!=resultEmployList&&resultEmployList.size()>0){
            amEmploymentBO = resultEmployList.get(0);
        }

        if(!StringUtil.isEmpty(amEmploymentBO.getEmployStyle()))
        {
            amEmploymentBO.setEmployStyle(ReasonUtil.getYgfs(amEmploymentBO.getEmployStyle()));
        }

        //用工信息里边的用工备注  取最新一条
        if(null!= amRemarkBOList&& amRemarkBOList.size()>0)
        {
            amEmploymentBO.setEmployNotes(amRemarkBOList.get(0).getRemarkContent());
        }

        Map<String, Object> resultMap = new HashMap<>();
        //客户信息
        resultMap.put("customerInfo",amCustomBO);
        //雇员信息
        resultMap.put("amEmpTaskBO",amEmpEmployeeBO);

        resultMap.put("resignBO",amResignBO);

        if(null!=amArchiveBOList&&amArchiveBOList.size()>0)
        {
            AmArchiveBO  amArchiveBO = amArchiveBOList.get(0);
            AmArchiveDTO amArchiveDTO = new AmArchiveDTO();
            BeanUtils.copyProperties(amArchiveBO,amArchiveDTO);
            resultMap.put("amArchaiveBo",amArchiveDTO);

            params.put("archiveId",amArchiveBO.getArchiveId());
            pageInfo.setParams(params);

            AmInjuryBO amInjuryBO = new AmInjuryBO();

            amInjuryBO.setArchiveId(amArchiveBO.getArchiveId().toString());

            List<AmInjuryBO>  amInjuryBOList = amInjuryService.queryAmInjury(amInjuryBO);

            if(null!=amInjuryBOList&&amInjuryBOList.size()>0)
            {
                resultMap.put("amInjuryBOList",amInjuryBOList);
            }

        }

        resultMap.put("amEmploymentBO",amEmploymentBO);

        if(null!=amRemarkBOPageRows)
        {
            resultMap.put("amRemarkBo",amRemarkBOPageRows);
        }

        if(null!=result&&result.getRows().size()>0){
            resultMap.put("materialList",result.getRows());
        }

        resultMap.put("resultMaterial",resultMaterial.getRows());



        return  JsonResultKit.of(resultMap);
    }

    @PostMapping("/saveAmInjury")
    public JsonResult<Boolean>  saveAmInjury(@RequestBody List<AmInjury> list) {
        List<AmInjury>  data = new ArrayList<AmInjury>();
        for(AmInjury bo:list)
        {
            LocalDateTime now = LocalDateTime.now();
            bo.setCreatedTime(now);
            bo.setModifiedTime(now);
            bo.setCreatedBy(ReasonUtil.getUserId());
            bo.setModifiedBy(ReasonUtil.getUserId());
            if(bo.getInjuryId()==null){
                data.add(bo);
            }
        }

        boolean result = false;
        try {
            result = amInjuryService.insertBatch(data);
        } catch (Exception e) {

        }

        return JsonResultKit.of(result);
    }

    @PostMapping("/saveAmEmpMaterial")
    public JsonResult<Integer>  saveAmEmpMaterial(@RequestBody List<AmEmpMaterial> list) {
        String userId = "System";
        String userName = "System";
        try {
            userId = UserContext.getUserId();
            userName = UserContext.getUser().getDisplayName();
        } catch (Exception e) {

        }
        List<AmEmpMaterial>  data = new ArrayList<AmEmpMaterial>();
        for(AmEmpMaterial bo:list)
        {
            if(bo.getReceiveName() != null){
                return JsonResultKit.of(2);
            }
            bo.setOperateType(2);
            LocalDateTime now = LocalDateTime.now();
            bo.setCreatedTime(now);
            bo.setModifiedTime(now);
            bo.setCreatedBy(userId);
            bo.setModifiedBy(userId);
            bo.setRejectDate(LocalDate.now());
            bo.setRejectId(userId);
            bo.setRejectName(userName);
            bo.setSubmitterDate(LocalDate.now());
            if(bo.getEmpMaterialId()==null){
                data.add(bo);
            }

        }
        boolean result = false;
        try {
            result = amEmpMaterialService.insertOrUpdateBatch(data);
        } catch (Exception e) {

        }

        return JsonResultKit.of(result?1:0);
    }

    @RequestMapping("/deleteAmInjury")
    public JsonResult<Boolean>  deleteAmInjury(Long injuryId){
        boolean  result = amInjuryService.deleteAmInjury(injuryId);
        return JsonResultKit.of(result);
    }

    @RequestMapping("/deleteAmEmpMaterial")
    public JsonResult<Boolean>  deleteAmEmpMaterial(AmEmpMaterial amEmpMaterial){
        boolean  result = amEmpMaterialService.deleteById(amEmpMaterial);
        return JsonResultKit.of(result);
    }


    @PostMapping("/saveAmArchiveUse")
    public JsonResult<Boolean>  saveAmArchiveUse(@RequestBody List<AmArchiveUse> list) {

        for(AmArchiveUse bo:list)
        {
            LocalDateTime now = LocalDateTime.now();

            if(bo.getArchiveUseId()==null){
                bo.setCreatedTime(now);
                bo.setModifiedTime(now);
                bo.setCreatedBy(ReasonUtil.getUserId());
                bo.setModifiedBy(ReasonUtil.getUserId());
            }else {
                bo.setModifiedTime(now);
                bo.setModifiedBy(ReasonUtil.getUserId());
            }
        }

        boolean result = false;
        try {
            result = iAmArchiveUseService.insertOrUpdateBatch(list);
        } catch (Exception e) {

        }

        return JsonResultKit.of(result);
    }

    @RequestMapping("/queryArchiveUse")
    public JsonResult queryArchiveUse(AmArchiveUse archiveUse){

        PageInfo pageInfo = new PageInfo();
        JSONObject params = new JSONObject();
        params.put("employeeId",archiveUse.getEmployeeId());
        params.put("archiveId",archiveUse.getArchiveId());
        params.put("useBorrow",0);
        pageInfo.setParams(params);


        Map<String, Object> resultMap = new HashMap<>();

        PageRows<AmArchiveUse>  amArchiveUsePageRows  = iAmArchiveUseService.queryAmArchiveUse(pageInfo);

        params.put("useBorrow",1);
        pageInfo.setParams(params);

        PageRows<AmArchiveUse>  amArchiveUsePageRows1  = iAmArchiveUseService.queryAmArchiveUse(pageInfo);

        if(null!=amArchiveUsePageRows&&amArchiveUsePageRows.getRows().size()>0){
            resultMap.put("amArchiveUsePageRows",amArchiveUsePageRows);
        }

        if(null!=amArchiveUsePageRows1&&amArchiveUsePageRows1.getRows().size()>0){
            resultMap.put("amArchiveUsePageRows1",amArchiveUsePageRows1);
        }

        String userName = "System";
        try {
            userName = UserContext.getUser().getDisplayName();
        } catch (Exception e) {

        }
        resultMap.put("userName",userName);
        return  JsonResultKit.of(resultMap);
    }

    @RequestMapping("/deleteAmArchiveUse")
    public JsonResult<Boolean>  deleteAmArchiveUse(AmArchiveUse amArchiveUse){

        boolean result = iAmArchiveUseService.deleteAmArchiveUse(amArchiveUse);

        return JsonResultKit.of(result);

    }

    @RequestMapping("/archiveSearchExportOpt")
    public void archiveSearchExportOpt(HttpServletResponse response, AmEmploymentBO amEmploymentBO) {
        List<String> param = new ArrayList<String>();

        if(!StringUtil.isEmpty(amEmploymentBO.getParams()))
        {
            String arr[] = amEmploymentBO.getParams().split(",");
            for(int i=0;i<arr.length;i++) {
                param.add(arr[i]);
            }
        }

        amEmploymentBO.setParam(param);

        if(null!=amEmploymentBO.getTaskStatus()&&amEmploymentBO.getTaskStatus()==0){
            amEmploymentBO.setTaskStatus(null);
        }

        Date date = new Date();
        String fileNme = "用工档案任务单_"+ StringUtil.getDateString(date)+".xls";

        List<archiveSearchExportOpt> opts = business.queryAmArchiveList(amEmploymentBO);

        for(archiveSearchExportOpt temp:opts)
        {
            temp.setEmployFeedback(ReasonUtil.getYgfk(temp.getEmployFeedback()));
            temp.setResignFeedback1(ReasonUtil.getTgfk(temp.getResignFeedback1()));

            if(!StringUtil.isEmpty(temp.getArchiveSpecial()))
            {
                int last = temp.getArchiveSpecial().lastIndexOf(",");
                temp.setArchiveSpecial(temp.getArchiveSpecial().substring(0,last));
            }
        }

        ExcelUtil.exportExcel(opts,archiveSearchExportOpt.class,fileNme,response);
    }

}
