package com.ciicsh.gto.afsupportcenter.housefund.messageservice.host.message;

import com.alibaba.fastjson.JSON;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmpSocialDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeCompanyDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeInfoDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.proxy.AfEmployeeSocialProxy;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfComTaskService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfEmpTaskService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfPaymentAccountService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.utils.CommonApiUtils;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpTaskConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfComTask;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpTask;
import com.ciicsh.gto.afsupportcenter.housefund.messageservice.host.enumeration.FundCategory;
import com.ciicsh.gto.afsupportcenter.housefund.messageservice.host.enumeration.ProcessCategory;
import com.ciicsh.gto.afsupportcenter.housefund.messageservice.host.enumeration.TaskCategory;
import com.ciicsh.gto.afsupportcenter.util.constant.SocialSecurityConst;
import com.ciicsh.gto.afsupportcenter.util.enumeration.LogInfo;
import com.ciicsh.gto.afsupportcenter.util.logService.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogMessage;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.AfCompanyDetailResponseDTO;
import com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayApplyPayStatusDTO;
import com.ciicsh.gto.sheetservice.api.dto.TaskCreateMsgDTO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@EnableBinding(value = TaskSink.class)
@Component
public class KafkaReceiver {
    private final static Logger logger = LoggerFactory.getLogger(KafkaReceiver.class);

    @Autowired
    private HfEmpTaskService hfEmpTaskService;
    @Autowired
    private HfComTaskService hfComTaskService;
    @Autowired
    private HfPaymentAccountService hfPaymentAccountService;
    @Autowired
    private AfEmployeeSocialProxy employeeSocialProxy;
    @Autowired
    private LogApiUtil log;
    @Autowired
    private CommonApiUtils commonApiUtils;

    /**
     * 雇员公积金新进任务单
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_IN)
    public void fundEmpIn(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        //判断是否是公积金或者补充公积金
        if (TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) || TaskSink.ADD_FUND_NEW.equals(taskMsgDTO.getTaskType())) {
            logger.debug("start fundEmpIn: " + JSON.toJSONString(taskMsgDTO));
            log.info(LogMessage.create().setTitle("fundEmpIn").setContent("start fundEmpIn: " + JSON.toJSONString(taskMsgDTO)));
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            if (paramMap != null) {
                if (paramMap.get("fundType") == null
                    && paramMap.get("operation_type") != null) {
                    paramMap.put("fundType", "3");
                }

                if (paramMap.get("fundType") != null) {
                    String taskCategory = paramMap.get("fundType").toString();
                    String fundCategory = TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();
                    Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                    Integer taskCategoryInt = Integer.parseInt(taskCategory);
                    if (taskCategoryInt > TaskCategory.REOPEN.getCategory()) {
                        taskCategoryInt = TaskCategory.NOHANDLE.getCategory();
                    }
                    boolean result = saveEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEENEW.getCategory(), taskCategoryInt, null, cityCodeMap, 0);
                    String content = "end fundEmpIn: " + JSON.toJSONString(taskMsgDTO) + "，result：" + (result ? "Success!" : "Fail!");
                    logger.debug(content);
                    log.info(LogMessage.create().setTitle("fundEmpIn").setContent(content));
                }
            }
        }
    }

    /**
     * 雇员公积金终止任务单
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_OUT)
    public void fundEmpOut(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        if (TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType()) || TaskSink.ADD_FUND_STOP.equals(taskMsgDTO.getTaskType())) {
            logger.debug("start fundEmpOut: " + JSON.toJSONString(taskMsgDTO));
            log.info(LogMessage.create().setTitle("fundEmpOut").setContent("start fundEmpOut: " + JSON.toJSONString(taskMsgDTO)));
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            if(null != paramMap){
                if (paramMap.get("fundType") == null
                    && paramMap.get("operation_type") != null) {
                    paramMap.put("fundType", "2");
                }

                if (paramMap.get("fundType") != null) {
                    String fundType = paramMap.get("fundType").toString();
                    Integer taskCategory = fundType.equals("1") ? TaskCategory.TURNOUT.getCategory() : TaskCategory.SEALED.getCategory();
                    String fundCategory = TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType()) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();
                    Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                    boolean res = saveEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEESTOP.getCategory(), taskCategory, null, cityCodeMap, 0);
                    String content = "end fundEmpOut:" + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!");
                    logger.debug(content);
                    log.info(LogMessage.create().setTitle("fundEmpOut").setContent(content));
                }
            }

        }
    }

    /**
     * 雇员公积金补缴任务单
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_MAKE_UP)
    public void funEmpRepay(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        if (TaskSink.FUND_MAKE_UP.equals(taskMsgDTO.getTaskType()) || TaskSink.ADD_FUND_MAKE_UP.equals(taskMsgDTO.getTaskType())) {
            logger.debug("start funEmpRepay: " + JSON.toJSONString(taskMsgDTO));
            log.info(LogMessage.create().setTitle("funEmpRepay").setContent("start funEmpRepay: " + JSON.toJSONString(taskMsgDTO)));
            String fundCategory = TaskSink.FUND_MAKE_UP.equals(taskMsgDTO.getTaskType()) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            Map<String, Object> cityCodeMap = null;
            if (paramMap != null) {
                cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
            }
            boolean res = saveEmpTask(taskMsgDTO, fundCategory,ProcessCategory.EMPLOYEEREPAY.getCategory(),TaskCategory.REPAY.getCategory(), null, cityCodeMap, 0);
            String content = "end funEmpRepay: " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!");
            logger.debug(content);
            log.info(LogMessage.create().setTitle("funEmpRepay").setContent(content));
        }
    }

    private final static TaskCategory[] FLOP_IN_TASK_CATEGORIES = {
        TaskCategory.FLOPNEW,
        TaskCategory.FLOPINTO,
        TaskCategory.FLOPREOPEN
    };

    private final static TaskCategory[] FLOP_OUT_TASK_CATEGORIES = {
        TaskCategory.FLOPOUT,
        TaskCategory.FLOPSEALED
    };

    private final static TaskCategory[] OUT_TASK_CATEGORIES = {
        TaskCategory.TURNOUT,
        TaskCategory.SEALED
    };

    private final static Integer[] AUTO_OFFSET_TASK_CATEGORIES = {
        TaskCategory.TURNOUT.getCategory(),
        TaskCategory.SEALED.getCategory(),
        TaskCategory.FLOPOUT.getCategory(),
        TaskCategory.FLOPSEALED.getCategory(),
        TaskCategory.NOHANDLE.getCategory()
    };

    private final static Integer[] ALL_OUT_TASK_CATEGORIES = {
        TaskCategory.TURNOUT.getCategory(),
        TaskCategory.SEALED.getCategory(),
        TaskCategory.FLOPOUT.getCategory(),
        TaskCategory.FLOPSEALED.getCategory(),
    };

    private final static Integer[] ALL_IN_TASK_CATEGORIES = {
        TaskCategory.NEW.getCategory(),
        TaskCategory.CHANGEINTO.getCategory(),
        TaskCategory.REOPEN.getCategory(),
        TaskCategory.FLOPNEW.getCategory(),
        TaskCategory.FLOPINTO.getCategory(),
        TaskCategory.FLOPREOPEN.getCategory()
    };

    /**
     * 雇员公积金翻牌任务单
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_COMPANY_CHANGE)
    public void fundEmpFlop(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        if(TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType())
            || TaskSink.ADD_FUND_NEW.equals(taskMsgDTO.getTaskType())
            || TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType())
            || TaskSink.ADD_FUND_STOP.equals(taskMsgDTO.getTaskType())) {
            logger.debug("start fundEmpFlop: " + JSON.toJSONString(taskMsgDTO));
            log.info(LogMessage.create().setTitle("fundEmpFlop").setContent("start fundEmpFlop: " + JSON.toJSONString(taskMsgDTO)));
            String fundCategory = TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) || TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType()) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();

            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            Integer fundType;
            // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
            // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
            // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，oldAgreementId一概不记录，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
            // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（通常只有调整类别中的非0转0）；
            if (TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) || TaskSink.ADD_FUND_NEW.equals(taskMsgDTO.getTaskType())) {
                if(null != paramMap ) {
                    if (paramMap.get("fundType") == null
                        && paramMap.get("operation_type") != null) {
                        paramMap.put("fundType", "3");
                    }
                    if (paramMap.get("fundType") != null) {
                        fundType = Integer.parseInt(paramMap.get("fundType").toString());
                        logger.debug("start in fundEmpFlop: " + JSON.toJSONString(taskMsgDTO));
                        Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                        Integer taskCategory = TaskCategory.NOHANDLE.getCategory();
                        if (fundType <= FLOP_IN_TASK_CATEGORIES.length) {
                            taskCategory = FLOP_IN_TASK_CATEGORIES[fundType - 1].getCategory();
                        }

                        boolean res = saveEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEEFLOP.getCategory(), taskCategory, null, cityCodeMap, 0);
                        logger.debug("end in fundEmpFlop:  " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
                    }
                }
            }
            else{
                fundType = 2;

                String oldAgreementId = null;
                Map<String, Object> cityCodeMap = null;
                if (null != paramMap) {
                    if (paramMap.get("fundType") == null
                        && paramMap.get("operation_type") != null) {
                        paramMap.put("fundType", "2");
                    }
                    if (paramMap.get("oldEmpAgreementId") != null) {
                        oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
                    }
                    cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");

                    if (paramMap.get("fundType") != null) {
                        fundType = Integer.parseInt(paramMap.get("fundType").toString());
                        logger.debug("start out fundEmpFlop: " + JSON.toJSONString(taskMsgDTO));
                        boolean res = saveEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEEFLOP.getCategory(), FLOP_OUT_TASK_CATEGORIES[fundType - 1].getCategory(), oldAgreementId, cityCodeMap, 0);
                        logger.debug("end out fundEmpFlop:  " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
                    }
                }
            }
            logger.debug("end fundEmpFlop!");
            log.info(LogMessage.create().setTitle("fundEmpFlop").setContent("end fundEmpFlop!"));
        }
    }

    /**
     * 雇员公积金服务协议调整任务单
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_AGREEMENT_ADJUST)
    public void fundEmpAgreementAdjust(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        if(TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType())
            || TaskSink.ADD_FUND_NEW.equals(taskMsgDTO.getTaskType())
            || TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType())
            || TaskSink.ADD_FUND_STOP.equals(taskMsgDTO.getTaskType())) {
            log.info(LogMessage.create().setTitle("fundEmpAgreementAdjust").setContent("start fundEmpAgreementAdjust: " + JSON.toJSONString(taskMsgDTO)));
            //调整和新进（新开、转入和启封）
            if (TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) || TaskSink.ADD_FUND_NEW.equals(taskMsgDTO.getTaskType())) {
                logger.debug("start in fundEmpAgreementAdjust: " + JSON.toJSONString(taskMsgDTO));
                Map<String, Object> paramMap = taskMsgDTO.getVariables();
                String fundCategory = TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();

                if (null != paramMap && paramMap.get("fundType") != null) {
                    Integer taskCategory =  Integer.parseInt(paramMap.get("fundType").toString());
                    String oldAgreementId = null;

                    // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
                    // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
                    // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，oldAgreementId需记录(仅0转非0)，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
                    // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（仅非0转0）；
                    if (paramMap.get("fundType").equals("4")) {
                        taskCategory = TaskCategory.ADJUST.getCategory();
                    }
                    if (paramMap.get("oldEmpAgreementId") != null) {
                        oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
                    }
                    Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                    boolean res = saveEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEEAGREEMENTADJUST.getCategory(), taskCategory, oldAgreementId, cityCodeMap, 0);
                    logger.debug("end in fundEmpAgreementAdjust: " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
                }
            }//离职封存()
            else{
                String fundCategory = TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType()) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();
                agreementAdjustOrUpdateEmpStop(taskMsgDTO, fundCategory, 0);
            }
        }
    }

    /**
     * 雇员公积金服务协议更正任务单
     * @param message
     * @return
     */
    @StreamListener(TaskSink.AF_EMP_AGREEMENT_UPDATE)
    public void fundEmpAgreementCorrect(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        Integer processCategory = 0;
        Integer taskCategory = 0;
        Long empTaskId = null;
        String fundCategory = (TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType()) || TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType())) ? FundCategory.BASICFUND.getCategory() : FundCategory.ADDFUND.getCategory();
        log.info(LogMessage.create().setTitle("fundEmpAgreementCorrect").setContent("start fundEmpAgreementCorrect: " + JSON.toJSONString(taskMsgDTO)));

        //未办理任务单
        if (StringUtils.isBlank(taskMsgDTO.getTaskId())) {
            logger.debug("start fundEmpAgreementCorrect(not handled): " + JSON.toJSONString(taskMsgDTO));
            boolean res = updateEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEEAGREEMENTCORRECT.getCategory(), taskCategory, null,1);
            logger.debug("end fundEmpAgreementCorrect(not handled):" + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
        } //已办理任务单
        else{
            if (TaskSink.FUND_NEW.equals(taskMsgDTO.getTaskType())
                || TaskSink.ADD_FUND_NEW.equals(taskMsgDTO.getTaskType())
                || TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType())
                || TaskSink.ADD_FUND_STOP.equals(taskMsgDTO.getTaskType())) {
                logger.debug("start fundEmpAgreementCorrect: " + JSON.toJSONString(taskMsgDTO));
                try {
                    // 更正通道，收到停办消息，通常是两种情况：原消息为本地新开，更正为非0转0（调整）；或更正为翻牌封存（或翻牌转出），后者暂不考虑（前道已限制）；
                    if (TaskSink.FUND_STOP.equals(taskMsgDTO.getTaskType()) || TaskSink.ADD_FUND_STOP.equals(taskMsgDTO.getTaskType())) {
                        agreementAdjustOrUpdateEmpStop(taskMsgDTO, fundCategory, 1);
                    } else {
                        String oldAgreementId = null;
                        logger.debug("start fundEmpAgreementCorrect(already handled): " + JSON.toJSONString(taskMsgDTO));
                        HfEmpTask qd = new HfEmpTask();
                        //                    qd.setTaskId(paramMap.get("oldTaskId").toString());
                        qd.setBusinessInterfaceId(paramMap.get("oldEmpAgreementId").toString());
                        if (fundCategory.equals(FundCategory.BASICFUND.getCategory())) {
                            qd.setHfType(HfEmpTaskConstant.HF_TYPE_BASIC);
                        } else {
                            qd.setHfType(HfEmpTaskConstant.HF_TYPE_ADDED);
                        }
                        //查询旧的任务类型保存到新的任务单
                        List<HfEmpTask> resList = hfEmpTaskService.queryByTaskId(qd);
                        if (resList.size() > 0) {
    //                            HfEmpTask hfEmpTask = resList.get(0);
                            // 翻牌时，翻入翻出的empAgreementId相同，需排除翻出的
                            for (HfEmpTask hfEmpTask : resList) {
                                taskCategory = hfEmpTask.getTaskCategory();
                                processCategory = hfEmpTask.getProcessCategory();
                                empTaskId = hfEmpTask.getEmpTaskId();
                                    /* 更正业务流程，该业务场景已限制
                                    if (!TaskCategory.TURNOUT.getCategory().equals(taskCategory) &&
                                        !TaskCategory.SEALED.getCategory().equals(taskCategory) &&
                                        !TaskCategory.FLOPOUT.getCategory().equals(taskCategory) &&
                                        !TaskCategory.FLOPSEALED.getCategory().equals(taskCategory)
                                        ) {
                                        break;
                                    }*/
                            }
                        } else {
                            log.info(LogMessage.create().setTitle("fundEmpAgreementCorrect").setContent("根据oldEmpAgreementId未找到旧的任务单"));
                            // 如果没有查到旧的雇员协议：
                            processCategory = ProcessCategory.EMPLOYEENEW.getCategory();
                        }
                        // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
                        // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
                        // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，oldAgreementId需记录(仅0转非0)，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
                        // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（仅非0转0）；
                        if (paramMap.get("fundType").equals("4")) {
                            taskCategory = TaskCategory.ADJUST.getCategory();
                        }
                        if (ProcessCategory.EMPLOYEEAGREEMENTADJUST.getCategory().equals(processCategory) && paramMap.get("oldEmpAgreementId") != null) {
                            oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
                        }

                        Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                        if (cityCodeMap != null) {
                            cityCodeMap.put("preEmpTaskId", empTaskId);
                        }
                        // 调整状态更正时，oldEmpAgreementId是对应调整前协议，也同时对应更正前任务单的missionId
    //                        boolean res = saveEmpTask(taskMsgDTO, fundCategory, processCategory, taskCategory, paramMap.get("oldEmpAgreementId").toString(), 1);
                        boolean res = saveEmpTask(taskMsgDTO, fundCategory, processCategory, taskCategory, oldAgreementId, cityCodeMap,1);
                        logger.debug("end fundEmpAgreementCorrect(already handled): " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
                    }

                } catch (Exception e) {
                    logger.debug("fundEmpAgreementCorrect exception: " + e.getMessage(), e);
                    log.error(LogMessage.create().setTitle("fundEmpAgreementCorrect").setContent("fundEmpAgreementCorrect exception: " + e.getMessage()));
                }
            }
            logger.debug("end fundEmpAgreementCorrect!");
        }
    }

    /**
     * 调整或更正停办处理
     *
     * @param taskMsgDTO
     * @param fundCategory
     * @param isChange
     */
    private void agreementAdjustOrUpdateEmpStop(TaskCreateMsgDTO taskMsgDTO, String fundCategory, Integer isChange) {
        // 非0转0，ProcessCategory为调整，taskCategory为封存，新增一个封存任务单，但需要将oldAgreementId同时存入任务单记录；
        // 因为前道发出新开任务时，已经创建了雇员的费用段，oldAgreementId对应的是前一个费用段，任务单结束时需要依据oldAgreementId进行回调，以便前道对其进行处理
        log.info(LogMessage.create().setTitle("agreementAdjustOrUpdateEmpStop").setContent("start: " + JSON.toJSONString(taskMsgDTO)));
        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        int fundType = 2;
        String oldAgreementId = null;
        Long empTaskId = null;

        if (null != paramMap) {
            if (paramMap.get("fundType") != null) {
                fundType = Integer.parseInt(paramMap.get("fundType").toString());
            }
            Integer taskCategory = OUT_TASK_CATEGORIES[fundType - 1].getCategory();

            if (paramMap.get("oldEmpAgreementId") != null) {
                HfEmpTask qd = new HfEmpTask();
                qd.setBusinessInterfaceId(paramMap.get("oldEmpAgreementId").toString());
                if (fundCategory.equals(FundCategory.BASICFUND.getCategory())) {
                    qd.setHfType(HfEmpTaskConstant.HF_TYPE_BASIC);
                } else {
                    qd.setHfType(HfEmpTaskConstant.HF_TYPE_ADDED);
                }
                //查询旧的任务类型保存到新的任务单
                List<HfEmpTask> resList = hfEmpTaskService.queryByTaskId(qd);
                if (resList.size() > 0) {
                    empTaskId = resList.get(0).getEmpTaskId();
                    if (resList.get(0).getTaskCategory().equals(taskCategory)) {
                        log.warn(LogMessage.create().setTitle("agreementAdjustOrUpdateEmpStop").setContent("end: 更正前任务单已经是转出或封存状态，如果当前消息还是转出或封存状态，此时不生成任务单"));
                        logger.debug("agreementAdjustOrUpdateEmpStop(): 更正前任务单已经是转出或封存状态，如果当前消息还是转出或封存状态，此时不生成任务单");
                        return;
                    }
                }
            }

            Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
            // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
            // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
            // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，oldAgreementId需记录(仅0转非0)，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
            // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（仅非0转0）；
            if (paramMap.get("oldEmpAgreementId") != null) {
//                if (cityCodeMap == null || cityCodeMap.get("newFundCityCode") == null
//                    || cityCodeMap.get("newFundCityCode").equals(cityCodeMap.get("oldFundCityCode"))) {
                    oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
//                }
            }
            if (cityCodeMap != null) {
                cityCodeMap.put("preEmpTaskId", empTaskId);
            }
            boolean res = saveEmpTask(taskMsgDTO, fundCategory, ProcessCategory.EMPLOYEEAGREEMENTADJUST.getCategory(), taskCategory, oldAgreementId, cityCodeMap, isChange);
            log.info(LogMessage.create().setTitle("agreementAdjustOrUpdateEmpStop").setContent("end: " + JSON.toJSONString(taskMsgDTO) + ", result: " + (res ? "Success!" : "Fail!")));
            logger.debug("end agreementAdjustOrUpdateEmpStop:" + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
        } else {
            log.info(LogMessage.create().setTitle("agreementAdjustOrUpdateEmpStop").setContent("end: " + JSON.toJSONString(taskMsgDTO) + ", paramMap is null， result: Fail!"));
            logger.debug("end agreementAdjustOrUpdateEmpStop:" + JSON.toJSONString(taskMsgDTO) + "，paramMap is null， result：Fail!");
        }
    }

    /**
     * 公积金财务付款申请回调任务单
     * @param message
     * @return
     */
    @StreamListener(TaskSink.PAY_APPLY_PAY_STATUS_STREAM)
    public void applyFinancePayment(Message<PayApplyPayStatusDTO> message) {
        PayApplyPayStatusDTO taskMsgDTO = message.getPayload();
        logger.debug("start applyFinancePayment: " + JSON.toJSONString(taskMsgDTO));
        log.info(LogMessage.create().setTitle("applyFinancePayment").setContent("start applyFinancePayment: "+ JSON.toJSONString(taskMsgDTO)));
        try{
            if(taskMsgDTO.getBusinessType() == 2){
                if(taskMsgDTO.getPayStatus().equals(-1) || taskMsgDTO.getPayStatus().equals(8) || taskMsgDTO.getPayStatus().equals(9)){
                    boolean res = hfPaymentAccountService.updatePaymentInfo(taskMsgDTO);
                    logger.debug("applyFinancePayment result: " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
                }
            }
        }
        catch (Exception e){
            log.error(LogMessage.create().setTitle("applyFinancePayment").setContent("applyFinancePayment exception: " + e.getMessage()));
            logger.debug("applyFinancePayment exception: " + e.getMessage(),e);
        }
        logger.debug("end applyFinancePayment!");
    }

    /**
     * 客服中心调用更新企业任务单
     * @param message
     * @return
     */
    @StreamListener(TaskSink.AF_COMPANY_FUND_ACCOUNT_ONCE)
    public void updateComTask(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        logger.debug("start updateComTask: " + JSON.toJSONString(taskMsgDTO));
        log.info(LogMessage.create().setTitle("updateComTask").setContent("receiver: "+ JSON.toJSONString(taskMsgDTO)));
        //公积金
        try {
            HfComTask ele = hfComTaskService.selectById(taskMsgDTO.getMissionId());
            ele.setTaskId(taskMsgDTO.getTaskId());
            boolean res = hfComTaskService.updateById(ele);
            logger.debug("updateComTask result: " + JSON.toJSONString(taskMsgDTO) + "，result：" + (res ? "Success!" : "Fail!"));
        } catch (Exception e) {
            log.error(LogMessage.create().setTitle("updateComTask").setContent("updateComTask exception: " + e.getMessage()));
            logger.debug("updateComTask exception: "+e.getMessage(), e);
        }
        logger.debug("end updateComTask!");
    }

    /**
     * 获取当前雇员信息接口
     * @param taskMsgDTO
     * @return
     */
    private AfEmployeeInfoDTO getEmpInfo(TaskCreateMsgDTO taskMsgDTO,Integer processCategory,Integer taskCategory,  String oldAgreementId, Integer isChange) {
        AfEmployeeInfoDTO resDto = null;
        try {
            logger.debug("fund get employee info start, request:" + JSON.toJSONString(taskMsgDTO));
            Long empAgreementId = null;
            // 翻牌或调整通道时，如果是转出或封存的，根据oldAgreementId去获取转出或封存前的雇员信息
            if(
                (
                    processCategory.equals(ProcessCategory.EMPLOYEEFLOP.getCategory()) && (
                        taskCategory.equals(TaskCategory.FLOPOUT.getCategory())
                            || taskCategory.equals(TaskCategory.FLOPSEALED.getCategory())
                    )
                )
                || (
                    StringUtils.isEmpty(oldAgreementId) && (
                    processCategory.equals(ProcessCategory.EMPLOYEEAGREEMENTADJUST.getCategory()) && (
                        taskCategory.equals(TaskCategory.TURNOUT.getCategory())
                            || taskCategory.equals(TaskCategory.SEALED.getCategory()
                        )
                    )))
                ) {
                Map<String, Object> paramMap = taskMsgDTO.getVariables();
                if(null != paramMap){
                    empAgreementId = Long.parseLong(paramMap.get("oldEmpAgreementId").toString());
                }
            }
            else{
                logger.debug("fund get employee info taskMsgDTO.getMissionId():" + taskMsgDTO.getMissionId());
                Map<String, Object> paramMap = taskMsgDTO.getVariables();
                if(null != paramMap && paramMap.get("missionId") != null){
                    String varMissionId = paramMap.get("missionId").toString();
                    logger.debug("fund get employee info paramMap.get(missionId):" + varMissionId);

                    if (StringUtils.isNotEmpty(varMissionId)) {
                        // 雇员中心收到更正任务单时，原任务单还未发出时，agreementId有可能已更新，但是activiti产生的missionId不会更新，
                        // 此时从variables里面取得新的agreementId（key为：missionId）
                        empAgreementId = Long.parseLong(varMissionId);
                        Long missionId = Long.parseLong(taskMsgDTO.getMissionId());

                        if (empAgreementId.longValue() <= missionId.longValue()) {
                            empAgreementId = missionId;
                        } else {
                            taskMsgDTO.setMissionId(varMissionId);
                        }
                    } else {
                        empAgreementId = Long.parseLong(taskMsgDTO.getMissionId());
                    }
                } else {
                    empAgreementId = Long.parseLong(taskMsgDTO.getMissionId());
                }
            }
            resDto = employeeSocialProxy.getByEmpAgreement(empAgreementId);
            logger.debug("fund get employee info end, response:" + JSON.toJSONString(resDto));
        } catch (Exception e) {
            log.error(LogMessage.create().setTitle("getEmpInfo").setContent("fund get employee info exception: " + e.getMessage()));
            logger.debug("fund get employee info exception:" + e.getMessage(),e);
        }
        return resDto;
    }

    private AfEmployeeInfoDTO getEmpInfoByOldAgreementId(String oldAgreementId) {
        AfEmployeeInfoDTO resDto = null;
        if (StringUtils.isNotEmpty(oldAgreementId)) {
            resDto = employeeSocialProxy.getByEmpAgreement(Long.parseLong(oldAgreementId));
        }
        return resDto;
    }

    /**
     * 保存公积金雇员任务单
     * @param taskMsgDTO
     * @param taskCategory
     * @return
     */
    private boolean saveEmpTask(TaskCreateMsgDTO taskMsgDTO, String fundCategory, Integer processCategory,Integer taskCategory, String oldAgreementId, Map<String, Object> cityCodeMap, Integer isChange) {
        try {
            // 如果转外地，则取旧雇员协议
            if (oldAgreementId != null && cityCodeMap != null) {
                if (cityCodeMap.get("newFundCityCode") != null && !SocialSecurityConst.SHANGHAI_CITY_CODE.equals(cityCodeMap.get("newFundCityCode"))) {
                    cityCodeMap.put("oldAgreementId", oldAgreementId);
                    oldAgreementId = null;
                }
            }

            // 调用当前雇员信息获取接口
            AfEmployeeInfoDTO dto = getEmpInfo(taskMsgDTO,processCategory,taskCategory, oldAgreementId, isChange);

            if (dto != null) {
                AfEmployeeCompanyDTO companyDto = dto.getEmployeeCompany();
                AfCompanyDetailResponseDTO afCompanyDetailResponseDTO = null;

                if (companyDto != null) {
                    afCompanyDetailResponseDTO = commonApiUtils.getServiceCenterInfo(companyDto.getCompanyId());
                }

                AfEmpSocialDTO socialDTO = setEmpAccount(taskMsgDTO, dto, fundCategory);
                //插入数据到雇员任务单表
                boolean rtn = hfEmpTaskService.addEmpTask(taskMsgDTO, fundCategory, processCategory,taskCategory, oldAgreementId, isChange, cityCodeMap, dto, socialDTO, afCompanyDetailResponseDTO);

                // 判断是否自动抵消
                if (rtn && companyDto != null) {
                    log.info(LogMessage.create().setTitle("KafkaReceiver#saveSsEmpTask").setContent("判断是否自动抵消的类型"));
                    boolean isOK = false;
                    if (fundCategory.equals(FundCategory.BASICFUND.getCategory())) {
                        if (ArrayUtils.contains(AUTO_OFFSET_TASK_CATEGORIES, taskCategory)) {
                            isOK = hfEmpTaskService.autoOffset(companyDto.getCompanyId(), companyDto.getEmployeeId(), HfEmpTaskConstant.HF_TYPE_BASIC, 1);
                        }

                        if (!isOK) {
                            Map<String, Object> paramMap = taskMsgDTO.getVariables();
                            String operationType = (String)paramMap.get("operation_type");

                            if (ArrayUtils.contains(ALL_IN_TASK_CATEGORIES, taskCategory)) {
                                hfEmpTaskService.autoOffset(companyDto.getCompanyId(), companyDto.getEmployeeId(), HfEmpTaskConstant.HF_TYPE_BASIC, (operationType != null)? 3 : 2);
                            } else if (ArrayUtils.contains(ALL_OUT_TASK_CATEGORIES, taskCategory)) {
                                hfEmpTaskService.autoOffset(companyDto.getCompanyId(), companyDto.getEmployeeId(), HfEmpTaskConstant.HF_TYPE_BASIC, (operationType != null)? 2 : 3);
                            }
                        }
                    } else {
                        if (ArrayUtils.contains(AUTO_OFFSET_TASK_CATEGORIES, taskCategory)) {
                            isOK = hfEmpTaskService.autoOffset(companyDto.getCompanyId(), companyDto.getEmployeeId(), HfEmpTaskConstant.HF_TYPE_ADDED, 1);
                        }

                        if (!isOK) {
                            Map<String, Object> paramMap = taskMsgDTO.getVariables();
                            String operationType = (String)paramMap.get("operation_type");

                            if (ArrayUtils.contains(ALL_IN_TASK_CATEGORIES, taskCategory)) {
                                hfEmpTaskService.autoOffset(companyDto.getCompanyId(), companyDto.getEmployeeId(), HfEmpTaskConstant.HF_TYPE_ADDED, (operationType != null)? 3 : 2);
                            } else if (ArrayUtils.contains(ALL_OUT_TASK_CATEGORIES, taskCategory)) {
                                hfEmpTaskService.autoOffset(companyDto.getCompanyId(), companyDto.getEmployeeId(), HfEmpTaskConstant.HF_TYPE_ADDED, (operationType != null)? 2 : 3);
                            }
                        }
                    }
                }
                return rtn;
            }
            else {
                logger.debug("error:公积金雇员信息获取失败！");
                return false;
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.error(LogMessage.create().setTitle("KafkaReceiver#saveEmpTask").setContent("Fund:saveEmpTask exception: " + sw.toString()));
            pw.close();
//            logger.debug("exception:" + e.getMessage(), e);
            return false;
        }
    }

    private AfEmpSocialDTO setEmpAccount(TaskCreateMsgDTO taskMsgDTO, AfEmployeeInfoDTO dto, String fundCategory) {
        AfEmpSocialDTO socialDTO = null;
        List<AfEmpSocialDTO> socialList = dto.getEmpSocialList();
        if(null != socialList && socialList.size() > 0) {
            socialDTO = hfEmpTaskService.getAfEmpSocialByType(socialList, fundCategory);
            // 获取公积金账号（如果根据新雇员协议ID未取得，则根据老雇员协议ID去取）
            if (socialDTO != null && StringUtils.isEmpty(socialDTO.getAccount())) {
                Map<String, Object> paramMap = taskMsgDTO.getVariables();

                if (null != paramMap && paramMap.get("oldEmpAgreementId") != null) {
                    AfEmployeeInfoDTO oldDto = getEmpInfoByOldAgreementId(paramMap.get("oldEmpAgreementId").toString());

                    if (oldDto != null) {
                        socialList = oldDto.getEmpSocialList();
                        if(null != socialList && socialList.size() > 0) {
                            AfEmpSocialDTO oldSocialDTO = hfEmpTaskService.getAfEmpSocialByType(socialList, fundCategory);
                            if (oldSocialDTO != null) {
                                socialDTO.setAccount(oldSocialDTO.getAccount());
                            }
                        }
                    }
                }
            }
        }
        return socialDTO;
    }

    /**
     * 修改公积金雇员任务单
     * @param taskMsgDTO
     * @param fundCategory
     * @return
     */
    private boolean updateEmpTask(TaskCreateMsgDTO taskMsgDTO, String fundCategory, Integer processCategory,Integer taskCategory, String oldAgreementId, Integer isChange){
        try {
            //调用当前雇员信息获取接口
            AfEmployeeInfoDTO dto = getEmpInfo(taskMsgDTO,processCategory,taskCategory, oldAgreementId, isChange);

            if (dto != null) {
                AfEmpSocialDTO socialDTO = setEmpAccount(taskMsgDTO, dto, fundCategory);

                //更新任务单表信息
                return hfEmpTaskService.updateEmpTask(taskMsgDTO, fundCategory, dto, socialDTO);
            }
            else {
                logger.debug("error:公积金雇员信息获取失败！");
                return false;
            }
        } catch (Exception e) {
            log.error(LogMessage.create().setTitle("updateEmpTask").setContent("updateEmpTask exception: " + e.getMessage()));
            logger.debug("exception:" + e.getMessage(), e);
            return false;
        }
    }
}
