package com.ciicsh.gto.afsupportcenter.socialsecurity.siteservice.host.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsComAccountBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsComTaskBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsAccountComRelationService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsComTaskService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.utils.CommonApiUtils;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.utils.TaskCommonUtils;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.dto.SsComTaskDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsAccountComRelation;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsAccountRatio;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsComAccount;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsComTask;
import com.ciicsh.gto.afsupportcenter.util.CommonTransform;
import com.ciicsh.gto.afsupportcenter.util.ExcelUtil;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.interceptor.authenticate.UserContext;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import com.ciicsh.gto.logservice.api.LogServiceProxy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 独立库客户任务单 前端控制器
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@RestController
@RequestMapping("/api/soccommandservice/ssComTask")
public class SsComTaskController extends BasicController<SsComTaskService>{
    @Autowired
    private LogServiceProxy logServiceProxy;
    @Autowired
    private CommonApiUtils commonApiUtils;
    @Autowired
    private SsAccountComRelationService ssAccountComRelationService;
    @Autowired
    private SsComTaskService ssComTaskService;

    /**
     * 查询未处理企业任务单
     */
    @RequestMapping(value = "getNoProgressTask")
    public JsonResult<List<SsComTaskBO>> getNoProgressCompanyTask(PageInfo pageInfo) {
        PageRows<SsComTaskBO> pageRows = business.queryNoProgressCompanyTask(pageInfo);
        return JsonResultKit.ofPage(pageRows);
    }


    /**
     * 未处理企业任务单导出
     */
    @RequestMapping("/noProgressTaskExport")
    public void noProgressTaskExport(HttpServletResponse response, PageInfo pageInfo) {
        Date date = new Date();
        String fileNme = "企业任务单未处理_" + StringUtil.getDateString(date) + ".xls";
        SsComTaskBO taskBO = pageInfo.toJavaObject(SsComTaskBO.class);
        List<SsComTaskBO> taskBos = business.getNoProgressCompanyTasks(taskBO);
        ExcelUtil.exportExcel(taskBos, SsComTaskBO.class, fileNme, response);
    }


    /**
     * 查询处理中企业任务单
     * @param pageInfo
     * @return
     */
    @RequestMapping(value = "getProgressingTask")
    public JsonResult<List<SsComTaskBO>> getNoProgressingCompanyTask(PageInfo pageInfo) {
        //mybatis 分页插件
        PageRows<SsComTaskBO> pageRows = business.queryProgressingCompanyTask(pageInfo);
        return JsonResultKit.ofPage(pageRows);
    }


    /**
     * 处理中企业任务单导出
     */
    @RequestMapping("/progressingTaskExport")
    public void progressingTaskExport(HttpServletResponse response, PageInfo pageInfo) {
        Date date = new Date();
        String fileNme = "企业任务单处理中_" + StringUtil.getDateString(date) + ".xls";
        SsComTaskBO taskBO = pageInfo.toJavaObject(SsComTaskBO.class);
        List<SsComTaskBO> taskBos = business.getProgressingCompanyTasks(taskBO);
        ExcelUtil.exportExcel(taskBos, SsComTaskBO.class, fileNme, response);
    }


    /**
     * 查询已完成企业任务单
     * @param pageInfo
     * @return
     */
    @RequestMapping(value = "getFinshedTask")
    public JsonResult<List<SsComTaskBO>> getFinshedCompanyTask(PageInfo pageInfo) {
        //mybatis 分页插件
        PageRows<SsComTaskBO> pageRows = business.queryFinshedCompanyTask(pageInfo);
        return JsonResultKit.ofPage(pageRows);
    }

    /**
     * 查询批退企业任务单
     * @param pageInfo
     * @return
     */
    @RequestMapping(value = "getRefusedTask")
    public JsonResult<List<SsComTaskBO>> getRefusedCompanyTask(PageInfo pageInfo) {
        //mybatis 分页插件
        PageRows<SsComTaskBO> pageRows = business.queryRefusedCompanyTask(pageInfo);
        return JsonResultKit.ofPage(pageRows);
    }

    /**
     * 批退任务单
     * @param taskIdStr
     * @param refuseReason
     * @return
     */
    @RequestMapping(value = "refusingTask")
    public JsonResult<Boolean> refusingTask(@RequestParam(value = "taskIdStr", required = true) String taskIdStr,
                                            @RequestParam(value = "refuseReason", required = true) String
                                                refuseReason) {
        //mybatis 使用mybatis 的自带的批量修改
        List<SsComTask> dataList = new ArrayList<SsComTask>();
        //将前台传过来的参数解析成 任务单对象的数组
        if (isNotNull(taskIdStr)) {
            String[] taskIdArr = taskIdStr.split(",");
            for (int i = 0; i < taskIdArr.length; i++) {
                SsComTask ssComTask = new SsComTask();
                //批退的任务单ID
                ssComTask.setComTaskId(Long.parseLong(taskIdArr[i]));
                //批退状态 4
                ssComTask.setTaskStatus(4);
                //批退原因
                if (isNotNull(refuseReason))
                    ssComTask.setRejectionRemark(refuseReason);
                //批退工作流
                SsComTask task = ssComTaskService.selectById(ssComTask.getComTaskId());
                ssComTask.setModifiedTime(LocalDateTime.now());
                ssComTask.setModifiedBy(UserContext.getUserId());
                ssComTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
                TaskCommonUtils.completeTask(task.getTaskId(),commonApiUtils,UserContext.getUser().getDisplayName());
                dataList.add(ssComTask);
            }
        }
        boolean result = business.updateBatchById(dataList);
        JsonResult<Boolean> returnResult = JsonResultKit.of(result);
        return returnResult;
    }

    /**
     * 获得企业信息和材料收缴信息
     * @param ssComTaskDTO
     * @return
     */
    @RequestMapping(value = "getCompanyInfoAndMaterial")
    public JsonResult<SsComTaskBO> getCompanyInfoAndMaterial(SsComTaskDTO ssComTaskDTO) {
        SsComTaskBO ssComTaskBO = CommonTransform.convertToDTO(ssComTaskDTO, SsComTaskBO.class);
        if (null != ssComTaskBO.getComTaskId()) {
            if (isNotNull(ssComTaskBO.getOperatorType())) {
                //1 开户 2 转移 3 变更 4 终止
                if ("1".equals(ssComTaskBO.getOperatorType())) {
                    //开户时 需要查询企业信息(企业信息和材料)
                    SsComTaskBO ssComTaskBORes = business.queryComInfoAndMaterial(ssComTaskBO);
                    return JsonResultKit.of(ssComTaskBORes);
                } else {
                    //变更 转移 终止时 需要查询账户信息(账户信息和材料)
                    SsComTaskBO ssComTaskBORes = business.queryAccountInfoAndMaterial(ssComTaskBO);
                    return JsonResultKit.of(ssComTaskBORes);
                }
            } else return JsonResultKit.ofError("操作类型为空");
            //前台传ID 为空 返回空
        } else return JsonResultKit.ofError("任务ID为空");
    }

    /**
     * 查询企业信息和前道传过来的JSON（包含社保截止和付款方式）
     * @param ssComTaskDTO
     * @return
     */
    @RequestMapping(value = "getComInfoAndPayWay")
    public JsonResult<SsComTaskBO> queryComInfoAndPayWay(SsComTaskDTO ssComTaskDTO) {
        //DTO转BO
        SsComTaskBO ssComTask = CommonTransform.convertToDTO(ssComTaskDTO, SsComTaskBO.class);
        SsComTaskBO ssComTaskBO = business.queryComInfoAndPayWay(ssComTask);

        if(null==ssComTaskBO.getComAccountId()){
            SsComTask ssComTask1 =business.selectById(ssComTask.getComTaskId());
            SsComTaskBO ssComTaskBO1 =  new SsComTaskBO();
            BeanUtils.copyProperties(ssComTaskBO,ssComTaskBO1);
            BeanUtils.copyProperties(ssComTask1,ssComTaskBO1);
            //暂时这么改，有时间的话，企业公积金任务单的前端要重新整理代码。
            SsComAccountBO ssComAccountBO=new SsComAccountBO();
            BeanUtils.copyProperties(ssComTask1,ssComAccountBO);
            ssComAccountBO.setExpireDate(ssComTask1.getExpireDateFront());
            ssComTaskBO1.setSsComAccountBO(ssComAccountBO);
            return JsonResultKit.of(ssComTaskBO1);
        }
        return JsonResultKit.of(ssComTaskBO);
    }

    /**
     * 添加或者修改企业任务单（开户）
     * @param map
     * @return
     */
    @RequestMapping(value = "addOrUpdateCompanyTask")
    public JsonResult<Boolean> addOrUpdateCompanyTask(@RequestParam Map<String, String> map) {
        //获得社保账户信息
        SsComAccount ssComAccount = getSsComAccount(map);
        //获得企业任务单信息
        SsComTask ssComTask = getSsComTask(map);
        //获得工伤变更信息
        SsAccountRatio ssAccountRatio = getSsAccountRatio(map);
        //查询是否有
       EntityWrapper<SsAccountComRelation> ew = new EntityWrapper<SsAccountComRelation>();
        ew.where("company_id={0}",map.get("companyId")).and("is_active=1");
        List<SsAccountComRelation> resList = ssAccountComRelationService.selectList(ew);
        SsAccountComRelation ssAccountComRelation = null;
        //关系表中有则 不添加
        if (resList.size()==0) {
            ssAccountComRelation = new SsAccountComRelation();
            ssAccountComRelation.setCompanyId(map.get("companyId"));
            ssAccountComRelation.setMajorCom(new Integer(1));
            ssAccountComRelation.setCreatedTime(LocalDateTime.now());
            ssAccountComRelation.setCreatedBy(UserContext.getUserId());
            ssAccountComRelation.setModifiedBy(UserContext.getUserId());
            ssAccountComRelation.setModifiedTime(LocalDateTime.now());
        }else{
            ssAccountComRelation = resList.get(0);
        }
        String retCheckResult=business.checkComAccountDuplicate(ssComAccount);
        if ( !retCheckResult.equals("")){//检查输入的内容是否重复
            return JsonResultKit.ofError(retCheckResult.substring(0,retCheckResult.length()-1));
        }
        String result = business.addOrUpdateCompanyTask(ssComTask, ssComAccount, ssAccountRatio, ssAccountComRelation);

        if (result.equals("SUCC")){

            return JsonResultKit.of(true);
        }else{
            return JsonResultKit.ofError(result);
        }


    }

    /**
     * 终止任务单的操作
     * @param ssComTaskDTO
     * @return
     */
    @RequestMapping("updateOrEndingTask")
    public JsonResult<Boolean> updateOrEndingTask(SsComTaskDTO ssComTaskDTO) {
        boolean result = false;
        //0、初始（材料收缴） 1、受理中  2、送审中  3 、已完成  4、批退
        SsComTaskBO ssComTaskBO = CommonTransform.convertToDTO(ssComTaskDTO, SsComTaskBO.class);
        if (ssComTaskBO.getTaskStatus() == 3) {
            //调用工作流
            TaskCommonUtils.completeTask(ssComTaskBO.getTaskId(),commonApiUtils,UserContext.getUserName());
            if (null != ssComTaskBO.getEndDate() && null != ssComTaskBO.getComAccountId()) {
                SsComAccount ssComAccount = new SsComAccount();
                //2 表示终止
                ssComAccount.setState(new Integer(2));
                ssComAccount.setComAccountId(ssComTaskBO.getComAccountId());
                ssComAccount.setEndDate(ssComTaskBO.getEndDate());
                ssComAccount.setModifiedTime(LocalDateTime.now());
                ssComAccount.setModifiedBy(UserContext.getUserId());
                result = business.updateOrHandlerTask(ssComTaskBO, ssComAccount);
            }
        } else {
            SsComTask comTask = new SsComTask();
            BeanUtils.copyProperties(ssComTaskBO, comTask);
            comTask.setModifiedBy(UserContext.getUserId());
            comTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
            comTask.setModifiedTime(LocalDateTime.now());

            //只是做任务单的状态切换，任务单不完成
            result = business.updateById(comTask);
        }
        return JsonResultKit.of(result);
    }

    /**
     * 转移任务单的操作
     * @param SsComTaskDTO
     * @return
     */
    @RequestMapping("updateOrTransferTask")
    public JsonResult<Boolean> updateOrTransferTask(SsComTaskDTO SsComTaskDTO) {
        boolean result = false;
        //DTO 转BO
        SsComTaskBO ssComTaskBO = CommonTransform.convertToDTO(SsComTaskDTO, SsComTaskBO.class);
        //动态扩展数据 预存转移办理时 不完成的状态的数据
        String transferDate = ssComTaskBO.getTransferDate();
        String settlementArea = ssComTaskBO.getSettlementArea();
        Map<String, String> dynamicExtendMap = new HashMap<String, String>();
        dynamicExtendMap.put("settlementArea", null == settlementArea ? "" : settlementArea);
        dynamicExtendMap.put("transferDate", null == transferDate ? "" : transferDate);
        String dynamicExtendStr = JSONObject.toJSONString(dynamicExtendMap);
        ssComTaskBO.setDynamicExtend(dynamicExtendStr);
        //0、初始（材料收缴） 1、受理中  2、送审中  3 、已完成  4、批退
        if (ssComTaskBO.getTaskStatus() == 3) {
            //调用工作流
            TaskCommonUtils.completeTask(ssComTaskBO.getTaskId(),commonApiUtils,UserContext.getUserName());
            if (isNotNull(ssComTaskBO.getSettlementArea()) && null != ssComTaskBO.getComAccountId()) {
                SsComAccount ssComAccount = new SsComAccount();
                ssComAccount.setComAccountId(ssComTaskBO.getComAccountId());
                ssComAccount.setSettlementArea(ssComTaskBO.getSettlementArea());
                ssComAccount.setModifiedBy(UserContext.getUserId());
                ssComAccount.setModifiedTime(LocalDateTime.now());
                result = business.updateOrHandlerTask(ssComTaskBO, ssComAccount);
            }
        } else {
            SsComTask comTask = new SsComTask();
            BeanUtils.copyProperties(ssComTaskBO, comTask);
            comTask.setModifiedBy(UserContext.getUserId());
            comTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
            comTask.setModifiedTime(LocalDateTime.now());

            //只是做任务单的状态切换，任务单不完成
            result = business.updateById(comTask);
        }
        return JsonResultKit.of(result);
    }

    /**
     * 变更任务单的操作
     * @param ssComTaskDTO
     * @return
     */
    @RequestMapping("updateOrChangeTask")
    public JsonResult<Boolean> updateOrChangeTask(SsComTaskDTO ssComTaskDTO) {
        SsComTaskBO ssComTaskBO = CommonTransform.convertToDTO(ssComTaskDTO, SsComTaskBO.class);
        boolean result = false;
        Map<String, String> dynamicExtendMap = new HashMap<String, String>();
        // 1行业比例变更  2 付款方式变更 3 公司名称变更
        dynamicExtendMap.put("changeContentValue", ssComTaskBO.getChangeContentValue());
        if ("1".equals(ssComTaskBO.getChangeContentValue())) {
            //动态扩展数据 预存转移办理时数据
            dynamicExtendMap.put("belongsIndustry", null == ssComTaskBO.getBelongsIndustry() ? "" : ssComTaskBO
                .getBelongsIndustry());
            dynamicExtendMap.put("companyWorkInjuryPercentage", null == ssComTaskBO.getCompanyWorkInjuryPercentage()
                ? "" : ssComTaskBO.getCompanyWorkInjuryPercentage());
            dynamicExtendMap.put("startMonth", null == ssComTaskBO.getStartMonth() ? "" : ssComTaskBO.getStartMonth()
                .toString());
        } else if ("2".equals(ssComTaskBO.getChangeContentValue())) {
            dynamicExtendMap.put("paymentWay", null == ssComTaskBO.getPaymentWay() ? "" : String.valueOf(ssComTaskBO
                .getPaymentWay()));
            dynamicExtendMap.put("billReceiver", null == ssComTaskBO.getBillReceiver() ? "" : String.valueOf
                (ssComTaskBO.getBillReceiver()));
        } else if ("3".equals(ssComTaskBO.getChangeContentValue())) {
            dynamicExtendMap.put("comAccountName", null == ssComTaskBO.getComAccountName() ? "" : ssComTaskBO
                .getComAccountName());
        }
        String dynamicExtendStr = JSONObject.toJSONString(dynamicExtendMap);
        ssComTaskBO.setDynamicExtend(dynamicExtendStr);
        //0、初始（材料收缴） 1、受理中  2、送审中  3 、已完成  4、批退
        if (ssComTaskBO.getTaskStatus() == 3) {
            //调用工作流
            TaskCommonUtils.completeTask(ssComTaskBO.getTaskId(),commonApiUtils,UserContext.getUserName());
            Object object = getObject(ssComTaskBO);
            //变更类型操作
            result = business.updateOrHandlerTask(ssComTaskBO, object);
        } else {
            ssComTaskBO.setComAccountId(null);
            //只做任务单的状态切换，任务单尚不完成

            SsComTask comTask = new SsComTask();
            BeanUtils.copyProperties(ssComTaskBO, comTask);
            comTask.setModifiedBy(UserContext.getUserId());
            comTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
            comTask.setModifiedTime(LocalDateTime.now());
            result = business.updateById(comTask);
        }
        return JsonResultKit.of(result);
    }

    /**
     * 任务单撤销
     * @param ssComTask
     * @return
     */
    @RequestMapping("/taskRevocation")
    public JsonResult<Boolean> taskRevocation(SsComTask ssComTask) {
        //修改任单状态和时间
        ssComTask.setModifiedBy(UserContext.getUserId());
        ssComTask.setModifiedTime(LocalDateTime.now());
        ssComTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
        return JsonResultKit.of(business.updateTaskStatusForRevoke(ssComTask) > 0 ? true : false);
    }


    public SsComAccount getSsComAccount(Map<String, String> map) {
        SsComAccount ssComAccount = new SsComAccount();
        //设置账户ID
        ssComAccount.setComAccountId(isNotNull(map.get("comAccountId")) ? Long.parseLong(map.get("comAccountId")) :
            null);
        //法人
        ssComAccount.setLegalPerson(isNotNull(map.get("legalPerson")) ? map.get("legalPerson") : null);
        //联系地址
        ssComAccount.setContactAddress(isNotNull(map.get("contactAddress")) ? map.get("contactAddress") : null);
        //参保户登记码
        ssComAccount.setSsAccount(isNotNull(map.get("ssAccount")) ? map.get("ssAccount") : null);
        //卡号
        ssComAccount.setBankAccount(isNotNull(map.get("bankAccount")) ? map.get("bankAccount") : null);
        //养老金账户公司名称
        ssComAccount.setComAccountName(isNotNull(map.get("comAccountName")) ? map.get("comAccountName") : null);
        //结算区县
        ssComAccount.setSettlementArea(isNotNull(map.get("settlementArea")) ? map.get("settlementArea") : null);
        //付款银行
        ssComAccount.setPaymentBank(isNotNull(map.get("paymentBank")) ? map.get("paymentBank") : null);
        //付款方式
        ssComAccount.setPaymentWay(isNotNull(map.get("paymentWay")) ? Integer.valueOf(map.get("paymentWay")) : null);
        //付款类型
        ssComAccount.setPaymentType(isNotNull(map.get("paymentType")) ? Integer.valueOf(map.get("paymentType")) : null);
        //账单接收方
        ssComAccount.setBillReceiver(isNotNull(map.get("billReceiver")) ? Integer.valueOf(map.get("billReceiver")) :
            null);
        //用户名
        ssComAccount.setSsUsername(isNotNull(map.get("ssUsername")) ? map.get("ssUsername") : null);
        //密码
        ssComAccount.setSsPwd(isNotNull(map.get("ssPwd")) ? map.get("ssPwd") : null);
        //初始余额
        //ssComAccount.setInitialBalance(isNotNull(map.get("initialBalance")) ? new BigDecimal(map.get
        //   ("initialBalance")) : null);
        //初期欠费
        //ssComAccount.setInitialDebt(isNotNull(map.get("initialDebt")) ? new BigDecimal(map.get("initialDebt")) : null);
        //来源地
        ssComAccount.setOriginPlace(isNotNull(map.get("originPlace")) ? Integer.valueOf(map.get("originPlace")) : null);
        //来源地备注
        ssComAccount.setOriginPlaceRemark(isNotNull(map.get("originPlaceRemark")) ? map.get("originPlaceRemark") :
            null);
        //办理备注
        ssComAccount.setRemark(isNotNull(map.get("handleRemark")) ? map.get("handleRemark") : null);
        //查询账户
        ssComAccount.setQueryAccount(isNotNull(map.get("queryAccount")) ? map.get("queryAccount") : null);
        //交予方式
        ssComAccount.setDeliverWay(isNotNull(map.get("deliverWay")) ? map.get("deliverWay") : null);
        //交予方式备注
        ssComAccount.setDeliverWayRemark(isNotNull(map.get("deliverWayRemark")) ? map.get("deliverWayRemark") : null);
        //字符串转LocalDate 格式
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        //给凭证时间
        //ssComAccount.setProvideCertificateTime(isNotNull(map.get("provideCertificateTime")) ? LocalDate.parse(map.get
        //    ("provideCertificateTime"), dateFormatter) : null);
        //变更时间
//        ssComAccount.setChangeTime(isNotNull(map.get("changeTime")) ? LocalDateTime.parse(map.get("changeTime"),
//            timeFormatter) : null);
//        //收到日期
//        ssComAccount.setReceiveDate(isNotNull(map.get("receiveDate")) ? LocalDate.parse(map.get("receiveDate"),
//            dateFormatter) : null);
//        //转入日期
//        ssComAccount.setIntoDate(isNotNull(map.get("intoDate")) ? LocalDate.parse(map.get("intoDate"), dateFormatter)
//            : null);
        //发出材料
        ssComAccount.setDispatchMaterial(isNotNull(map.get("dispatchMaterial")) ? map.get("dispatchMaterial") : null);
        //设置每个月截止时间
        ssComAccount.setExpireDate(isNotNull(map.get("expireDate")) ? Integer.valueOf(map.get("expireDate")) : null);
        //默认独立户
        ssComAccount.setSsAccountType(new Integer(3));
        ssComAccount.setModifiedBy(UserContext.getUserId());
        ssComAccount.setModifiedTime(LocalDateTime.now());
        return ssComAccount;
    }

    //获得企业任务单对象
    public SsComTask getSsComTask(Map<String, String> map) {
        SsComTask ssComTask = new SsComTask();
        //企业任务单id
        ssComTask.setComTaskId(isNotNull(map.get("comTaskId")) ? Long.parseLong(map.get("comTaskId")) : null);

        ssComTask.setCompanyId(isNotNull(map.get("companyId")) ? map.get("companyId") : null);
        //任务单类型
        ssComTask.setTaskCategory(isNotNull(map.get("taskCategory")) ? map.get("taskCategory") : null);
        //法人
        ssComTask.setLegalPerson(isNotNull(map.get("legalPerson")) ? map.get("legalPerson") : null);
        //联系地址
        ssComTask.setContactAddress(isNotNull(map.get("contactAddress")) ? map.get("contactAddress") : null);
        //前道传过来的截止日期和支付方式
        ssComTask.setTaskFormContent(isNotNull(map.get("taskFormContent")) ? map.get("taskFormContent") : null);
        //任务单状态
        ssComTask.setTaskStatus(isNotNull(map.get("taskStatus")) ? Integer.valueOf(map.get("taskStatus")) : null);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //任务受理时间
        ssComTask.setStartHandleDate(isNotNull(map.get("startHandleDate")) ? LocalDate.parse(map.get
            ("startHandleDate"), dateFormatter) : null);
        //任务送审时间
        ssComTask.setSendCheckDate(isNotNull(map.get("sendCheckDate")) ? LocalDate.parse(map.get("sendCheckDate"),
            dateFormatter) : null);
        //任务完成时间
        ssComTask.setFinishDate(isNotNull(map.get("finishDate")) ? LocalDate.parse(map.get("finishDate"),
            dateFormatter) : null);
        //办理备注
        ssComTask.setHandleRemark(isNotNull(map.get("handleRemark")) ? map.get("handleRemark") : null);

        ssComTask.setSsAccount(isNotNull(map.get("ssAccount")) ? map.get("ssAccount") : null);

        ssComTask.setBankAccount(isNotNull(map.get("bankAccount")) ? map.get("bankAccount") : null);

        ssComTask.setComAccountName(isNotNull(map.get("comAccountName")) ? map.get("comAccountName") : null);

        ssComTask.setPaymentBank(isNotNull(map.get("paymentBank")) ? map.get("paymentBank") : null);

        ssComTask.setPaymentWay(isNotNull(map.get("paymentWay")) ? Integer.valueOf(map.get("paymentWay")) : null);

        ssComTask.setPaymentType(isNotNull(map.get("paymentType")) ? Integer.valueOf(map.get("paymentType")) : null);

        ssComTask.setBillReceiver(isNotNull(map.get("billReceiver")) ? Integer.valueOf(map.get("billReceiver")) : null);

        ssComTask.setIndustryCategory(isNotNull(map.get("industryCategory")) ? map.get("industryCategory") : null);

        ssComTask.setExpireDateFront(isNotNull(map.get("expireDate")) ? Integer.valueOf(map.get("expireDate")):null);

        ssComTask.setSettlementArea(isNotNull(map.get("settlementArea"))?map.get("settlementArea"):null);

        ssComTask.setDispatchMaterial(isNotNull(map.get("dispatchMaterial"))?map.get("dispatchMaterial"):null);
        ssComTask.setModifiedBy(UserContext.getUserId());
        ssComTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
        ssComTask.setModifiedTime(LocalDateTime.now());
        return ssComTask;
    }

    //获得工伤比例信息
    private SsAccountRatio getSsAccountRatio(Map<String, String> map) {

        SsAccountRatio ssAccountRatio = new SsAccountRatio();
        ssAccountRatio.setSsAccountRatioId(isNotNull(map.get("ssAccountRatioId")) ? Long.valueOf(map.get
            ("ssAccountRatioId")) : null);
        //行业类别
        ssAccountRatio.setIndustryCategory(isNotNull(map.get("industryCategory")) ? map.get("industryCategory") : null);
        //企业工伤比例
        ssAccountRatio.setComRatio(isNotNull(map.get("comRatio")) ? new BigDecimal(map.get("comRatio")).setScale(4,
            BigDecimal.ROUND_HALF_UP) : null);
        //开始月份
        ssAccountRatio.setStartMonth(isNotNull(map.get("startMonth")) ? map.get("startMonth").replaceAll("-", "") :
            null);

        ssAccountRatio.setModifiedBy(UserContext.getUserId());
        ssAccountRatio.setModifiedTime(LocalDateTime.now());
        return ssAccountRatio;
    }

    //是否不为空
    public boolean isNotNull(String charecter) {
        return StringUtils.isNotBlank(charecter);
    }

    //变更的对象返回
    public Object getObject(SsComTaskBO ssComTaskBO) {
        //用于返回包装后的账户信息
        SsComAccount ssComAccount = new SsComAccount();
        ssComAccount.setModifiedBy(UserContext.getUserId());
        ssComAccount.setModifiedTime(LocalDateTime.now());
        if ("1".equals(ssComTaskBO.getChangeContentValue())) {//行业比例变更
            List<SsAccountRatio> SsAccountRatioList = new ArrayList<SsAccountRatio>();
            //用于作修改的对象
            SsAccountRatio ssAccountRatioUpdate = new SsAccountRatio();
            //用于作添加的新信息
            SsAccountRatio ssAccountRatioInsert = new SsAccountRatio();
            ssAccountRatioInsert.setComAccountId(ssComTaskBO.getComAccountId());
            ssAccountRatioInsert.setIndustryCategory(ssComTaskBO.getBelongsIndustry());
            ssAccountRatioInsert.setComRatio(new BigDecimal(ssComTaskBO.getCompanyWorkInjuryPercentage()));
            String monthInsert = ssComTaskBO.getStartMonth();
            ssAccountRatioInsert.setStartMonth(monthInsert);
            ssAccountRatioInsert.setActive(true);
            ssAccountRatioInsert.setCreatedBy(UserContext.getUserId());
            ssAccountRatioInsert.setCreatedTime(LocalDateTime.now());
            ssAccountRatioInsert.setModifiedBy(UserContext.getUserId());
            ssAccountRatioInsert.setModifiedTime(LocalDateTime.now());
            SsAccountRatioList.add(0, ssAccountRatioInsert);
            //原来数据只需要修改endMonth 在原来的月份上减一月
            String monthUpdate = TaskCommonUtils.getLastMonth(Integer.valueOf(ssComTaskBO.getStartMonth()));
            ssAccountRatioUpdate.setComAccountId(ssComTaskBO.getComAccountId());
            ssAccountRatioUpdate.setEndMonth(monthUpdate);
            ssAccountRatioUpdate.setModifiedBy(UserContext.getUserId());
            ssAccountRatioUpdate.setModifiedTime(LocalDateTime.now());
            SsAccountRatioList.add(1, ssAccountRatioUpdate);
            //返回两个工伤变更的对象List
            return SsAccountRatioList;
        } else if ("2".equals(ssComTaskBO.getChangeContentValue())) {//支付方式变更
            ssComAccount.setComAccountId(ssComTaskBO.getComAccountId());
            ssComAccount.setPaymentWay(ssComTaskBO.getPaymentWay());
            ssComAccount.setBillReceiver(ssComTaskBO.getBillReceiver());
            return ssComAccount;
        } else if ("3".equals(ssComTaskBO.getChangeContentValue())) {//公司名称变更
            ssComAccount.setComAccountId(ssComTaskBO.getComAccountId());
            ssComAccount.setComAccountName(ssComTaskBO.getComAccountName());
            return ssComAccount;
        } else {
            return null;
        }
    }

//    /**
//     * 企业社保账户开户、变更、转移、转出的 创建任务单接口
//     *
//     * @param ssComTaskDTO
//     * @return
//     */
//    @Log("企业社保账户开户、变更、转移、转出的 创建任务单接口")
//    @PostMapping("/saveSsComTask")
//    public com.ciicsh.common.customer.JsonResult saveSsComTask(@RequestBody SsComTaskDTO ssComTaskDTO) {
//        try {
//            if (StringUtils.isBlank(ssComTaskDTO.getCompanyId())) {
//                return com.ciicsh.common.customer.JsonResult.faultMessage("客户Id不能为空！");
//            }
//            if (StringUtils.isBlank(ssComTaskDTO.getTaskCategory())) {
//                return com.ciicsh.common.customer.JsonResult.faultMessage("任务类型不能为空！");
//            }
//            SsComTaskBO ssComTask = new SsComTaskBO();
//            BeanUtils.copyProperties(ssComTaskDTO, ssComTask);
//            int cnt = business.countComTaskByCond(ssComTask);
//            if (cnt > 0) {
//                return com.ciicsh.common.customer.JsonResult.faultMessage("该企业已存在相同类型的处理中任务单，不能重复添加！");
//            }
//            Long newComTaskId = insertSsComTask(ssComTaskDTO);
//            return com.ciicsh.common.customer.JsonResult.success(newComTaskId);
//        } catch (Exception e) {
//            return com.ciicsh.common.customer.JsonResult.faultMessage(e.getMessage());
//        }
//    }
//
//    //保存企业任务单
//    private Long insertSsComTask(@RequestBody SsComTaskDTO ssComTaskDTO) {
//        boolean result = false;
//        //数据转换
//        SsComTask ssComTask = CommonTransform.convertToEntity(ssComTaskDTO, SsComTask.class);
//        ssComTask.setTaskStatus(0);
//        ssComTask.setActive(true);
//        ssComTask.setCreatedTime(LocalDateTime.now());
//        ssComTask.setModifiedTime(LocalDateTime.now());
//        ssComTask.setCreatedBy("system");
//        ssComTask.setModifiedBy("system");
//
//        //任务单上前道系统传递过来的内容，Json格式
//        ssComTask.setTaskFormContent(JSONObject.toJSONString(ssComTaskDTO));
//
//        business.insertComTask(ssComTask);
//        return ssComTask.getComTaskId();
//    }
}