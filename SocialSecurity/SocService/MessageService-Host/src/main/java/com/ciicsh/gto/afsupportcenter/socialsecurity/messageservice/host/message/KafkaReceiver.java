package com.ciicsh.gto.afsupportcenter.socialsecurity.messageservice.host.message;

import com.alibaba.fastjson.JSON;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeCompanyDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeInfoDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.proxy.AfEmployeeSocialProxy;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsEmpTaskBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsComTaskService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsEmpTaskFrontService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsEmpTaskService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.SsPaymentComService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business.utils.CommonApiUtils;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsComTask;
import com.ciicsh.gto.afsupportcenter.util.constant.SocialSecurityConst;
import com.ciicsh.gto.afsupportcenter.util.enumeration.LogInfo;
import com.ciicsh.gto.afsupportcenter.util.enumeration.ProcessCategory;
import com.ciicsh.gto.afsupportcenter.util.logService.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogMessage;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.AfCompanyDetailResponseDTO;
import com.ciicsh.gto.settlementcenter.payment.cmdapi.dto.PayApplyPayStatusDTO;
import com.ciicsh.gto.sheetservice.api.dto.TaskCreateMsgDTO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by houwanhua on 2018/2/24
 */
@EnableBinding(value = TaskSink.class)
@Component
public class KafkaReceiver {

    @Autowired
    private SsEmpTaskService ssEmpTaskService;
    @Autowired
    private SsEmpTaskFrontService ssEmpTaskFrontService;
    @Autowired
    private SsComTaskService ssComTaskService;
    @Autowired
    private SsPaymentComService ssPaymentComService;
    @Autowired
    private AfEmployeeSocialProxy afEmployeeSocialProxy;

    @Autowired
    LogApiUtil logApiUtil;
    @Autowired
    CommonApiUtils commonApiUtils;

    private final static Integer[] AUTO_OFFSET_TASK_CATEGORIES = {
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_5),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_6),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_14),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_15),
        SocialSecurityConst.TASK_CATEGORY_NO_HANDLE
    };

    private final static Integer[] ALL_IN_TASK_CATEGORIES = {
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_1),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_2),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_12),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_13)
    };

    private final static Integer[] ALL_OUT_TASK_CATEGORIES = {
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_5),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_6),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_14),
        Integer.parseInt(SocialSecurityConst.TASK_TYPE_15)
    };

    /**
     * 订阅社保新进任务单
     *
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_IN)
    public void receiveSocialNew(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        //判断taskType是否是社保新进(social_new)，如果不是则无需处理
        if (TaskSink.SOCIAL_NEW.equals(taskMsgDTO.getTaskType())) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_IN).setContent(TaskSink.SOCIAL_NEW + " JSON: " + JSON.toJSONString(taskMsgDTO)));
            //获取任务单参数
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            if (paramMap != null) {
                if (paramMap.get("socialType") == null
                    && paramMap.get("operation_type") != null) {
                    paramMap.put("socialType", "2");
                }
//            if (Optional.ofNullable(paramMap).isPresent()) {
//                if (!Optional.ofNullable(paramMap.get("socialType")).isPresent()
//                    && Optional.ofNullable(paramMap.get("operation_type")).isPresent()) {
//                    paramMap.put("socialType", SocialSecurityConst.TASK_TYPE_2);
//                }

                if (paramMap.get("socialType") != null) {
                    String socialType = paramMap.get("socialType").toString();
                    Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                    if (cityCodeMap != null) {
                        cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
                    }
                    Integer taskCategoryInt = Integer.parseInt(socialType);
                    if (taskCategoryInt > SocialSecurityConst.SOCIAL_TYPE_2) {
                        taskCategoryInt = SocialSecurityConst.TASK_CATEGORY_NO_HANDLE;
                    }
                    //雇员服务协议ID
//                String empAgreementId = taskMsgDTO.getMissionId();
                    saveSsEmpTask(taskMsgDTO, taskCategoryInt, ProcessCategory.AF_EMP_IN.getCategory(), null, cityCodeMap, 0);

                }
            }
        }
    }

    /**
     * 订阅雇员终止任务单
     *
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_OUT)
    public void receiveEmpOut(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        //判断taskType是否是社保新进(social_stop)，如果不是则无需处理
        if (TaskSink.SOCIAL_STOP.equals(taskMsgDTO.getTaskType())) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_OUT).setContent(TaskSink.SOCIAL_STOP + " JSON: " + JSON.toJSONString(taskMsgDTO)));
//            String empAgreementId = taskMsgDTO.getMissionId();
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            Map<String, Object> cityCodeMap = null;
            if (paramMap != null) {
                cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                if (cityCodeMap != null) {
                    cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
                }
            }
            saveSsEmpTask(taskMsgDTO, Integer.parseInt(SocialSecurityConst.TASK_TYPE_5), ProcessCategory.AF_EMP_OUT.getCategory(), null, cityCodeMap,0);
        }
    }

    /**
     * 订阅雇员补缴任务单
     *
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_MAKE_UP)
    public void receiveChargeResume(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        //判断taskType是否是社保新进(social_make_up)，如果不是则无需处理
        if (TaskSink.SOCIAL_MAKE_UP.equals(taskMsgDTO.getTaskType())) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_MAKE_UP).setContent(TaskSink.SOCIAL_MAKE_UP + " JSON: " + JSON.toJSONString(taskMsgDTO)));
//            String empAgreementId = taskMsgDTO.getMissionId();
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            Map<String, Object> cityCodeMap = null;
            if (paramMap != null) {
                cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                if (cityCodeMap != null) {
                    cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
                }
            }
            saveSsEmpTask(taskMsgDTO, Integer.parseInt(SocialSecurityConst.TASK_TYPE_4), ProcessCategory.AF_EMP_MAKE_UP.getCategory(), null, cityCodeMap, 0);
        }
    }

    /**
     * 订阅雇员翻牌任务单
     *
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_COMPANY_CHANGE)
    public void receiveEmpCompanyChange(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        //获取任务单参数
        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        //社保翻牌新进或转入
        // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
        // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
        // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，oldAgreementId一概不记录，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
        // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（通常只有调整类别中的非0转0）；
        if (TaskSink.SOCIAL_NEW.equals(taskMsgDTO.getTaskType())) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_COMPANY_CHANGE).setContent(TaskSink.SOCIAL_NEW + " JSON: " + JSON.toJSONString(taskMsgDTO)));
            if (paramMap != null) {
                if (paramMap.get("socialType") == null
                    && paramMap.get("operation_type") != null) {
                    paramMap.put("socialType", "2");
                }

                if (paramMap.get("socialType") != null) {
                    int socialType = Integer.parseInt(paramMap.get("socialType").toString());
                    Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                    if (cityCodeMap != null) {
                        cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
                    }

//                empAgreementId = taskMsgDTO.getMissionId();
                    //新进转入判断，任务单保存时转换成支持中心的翻牌新进&翻牌转入类型
                    if (SocialSecurityConst.SOCIAL_TYPE_1 == socialType) {
                        saveSsEmpTask(taskMsgDTO, Integer.parseInt(SocialSecurityConst.TASK_TYPE_12), ProcessCategory.AF_EMP_COMPANY_CHANGE.getCategory(), null, cityCodeMap, 0);
                    } else if (SocialSecurityConst.SOCIAL_TYPE_2 == socialType) {
                        saveSsEmpTask(taskMsgDTO, Integer.parseInt(SocialSecurityConst.TASK_TYPE_13), ProcessCategory.AF_EMP_COMPANY_CHANGE.getCategory(), null, cityCodeMap, 0);
                    } else {
                        saveSsEmpTask(taskMsgDTO, SocialSecurityConst.TASK_CATEGORY_NO_HANDLE, ProcessCategory.AF_EMP_COMPANY_CHANGE.getCategory(), null, cityCodeMap, 0);
                    }
                }
            }
            //翻牌转出
        } else if (TaskSink.SOCIAL_STOP.equals(taskMsgDTO.getTaskType())) {
            String oldAgreementId = null;
            Map<String, Object> cityCodeMap = null;
            if (paramMap != null) {
                if (paramMap.get("oldEmpAgreementId") != null) {
                    oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
                }

                cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                if (cityCodeMap != null) {
                    cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
                }
            }

            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_COMPANY_CHANGE).setContent(TaskSink.SOCIAL_STOP + " JSON: " + JSON.toJSONString(taskMsgDTO)));
            saveSsEmpTask(taskMsgDTO, Integer.parseInt(SocialSecurityConst.TASK_TYPE_14), ProcessCategory.AF_EMP_COMPANY_CHANGE.getCategory(), oldAgreementId, cityCodeMap, 0);
        }
    }


    /**
     * 订阅雇员服务协议调整任务单
     *
     * @param message
     */
    @StreamListener(TaskSink.AF_EMP_AGREEMENT_ADJUST)
    public void receiveAdjust(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        //判断taskType是否是社保新进或停办(social_new或social_stop)，如果不是则无需处理
        //注：客服中心调整基数从0变非0时，收到的任务单是调整还是新开或转入，根据雇员中心传入的参数确定
        // 客服中心调整基数从非0变0时，收到的任务单就是社保停办任务单
        String socialType;
        String oldAgreementId = null;

        if (TaskSink.SOCIAL_NEW.equals(taskMsgDTO.getTaskType())) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_AGREEMENT_ADJUST).setContent(TaskSink.SOCIAL_NEW + " JSON: " + JSON.toJSONString(taskMsgDTO)));

            //获取社保办理类型
            // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
            // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
            // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，social_startAndStop为true，oldAgreementId一概不记录，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
            // 不为true，则oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
            // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（仅非0转0）；
            socialType = paramMap.get("socialType").toString();
//            if ("3".equals(socialType) && paramMap.get("oldEmpAgreementId") != null
            if ((paramMap.get("social_startAndStop") == null
                || !Boolean.valueOf(paramMap.get("social_startAndStop").toString())) && paramMap.get("oldEmpAgreementId") != null) {
                oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
            }
            Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
            if (cityCodeMap != null) {
                cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
            }
            saveSsEmpTask(taskMsgDTO, Integer.parseInt(socialType), ProcessCategory.AF_EMP_AGREEMENT_ADJUST.getCategory(), oldAgreementId, cityCodeMap, 0);
        } else if (TaskSink.SOCIAL_STOP.equals(taskMsgDTO.getTaskType())) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_AGREEMENT_ADJUST).setContent(TaskSink.SOCIAL_STOP + " JSON: " + JSON.toJSONString(taskMsgDTO)));
            agreementAdjustOrUpdateEmpStop(taskMsgDTO, 0, TaskSink.AF_EMP_AGREEMENT_ADJUST);
        }
    }

    /**
     * 订阅雇员服务协议更正任务单
     *
     * @param message
     * @return
     */
    @StreamListener(TaskSink.AF_EMP_AGREEMENT_UPDATE)
    public void receiveUpdate(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_AGREEMENT_UPDATE).setContent(" JSON: " + JSON.toJSONString(taskMsgDTO)));
        Map<String, Object> paramMap = taskMsgDTO.getVariables();

        //调用接口-调用客服中心接口，获取任务单表单信息
        AfEmployeeInfoDTO dto = callEmpAgreement(taskMsgDTO, ProcessCategory.AF_EMP_AGREEMENT_UPDATE.getCategory(), paramMap.get("oldEmpAgreementId").toString());
        //taskId为空，该消息则是由af客服中心发出，表示支持中心历史任务单未完成
        if (StringUtils.isBlank(taskMsgDTO.getTaskId())) {
            SsEmpTaskBO ssEmpTaskBO = new SsEmpTaskBO();
            ssEmpTaskBO.setTaskId(paramMap.get("oldTaskId").toString());
            ssEmpTaskBO.setEmployeeId(dto.getEmployeeCompany().getEmployeeId());
            ssEmpTaskBO.setCompanyId(dto.getEmployeeCompany().getCompanyId());
            List<SsEmpTaskBO> resList = ssEmpTaskService.queryByTaskId(ssEmpTaskBO);
            //如果查询到历史任务单，则直接进行更新操作
            if (resList != null && resList.size() > 0) {
                ssEmpTaskFrontService.updateEmpTaskTc(taskMsgDTO, dto);
            }
            //已办理任务单
        } else {   //taskId不为空，该kafka则是由任务单中心发出，表示支持中心历史任务单已完成

            //判断taskType是否是社保新进或停办(social_new或social_stop)，如果不是则无需处理
            if (TaskSink.SOCIAL_STOP.equals(taskMsgDTO.getTaskType())) {
                agreementAdjustOrUpdateEmpStop(taskMsgDTO, 1, TaskSink.AF_EMP_AGREEMENT_UPDATE);
            } else if (TaskSink.SOCIAL_NEW.equals(taskMsgDTO.getTaskType())) {
                logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_AGREEMENT_UPDATE).setContent(" JSON: " + JSON.toJSONString(taskMsgDTO)));
                try {

                    Integer taskCategory = 0;
                    Long empTaskId = null;
                    String oldAgreementId = null;
                    SsEmpTaskBO ssEmpTaskBO = new SsEmpTaskBO();
                    ssEmpTaskBO.setBusinessInterfaceId(paramMap.get("oldEmpAgreementId").toString());
                    //查询旧的任务类型保存到新的任务单
                    List<SsEmpTaskBO> resList = ssEmpTaskService.queryByBusinessInterfaceId(ssEmpTaskBO);
                    if (resList.size() > 0) {
                        for (SsEmpTaskBO bo : resList) {
                            //                            ssEmpTaskBO = resList.get(0);
                            taskCategory = bo.getTaskCategory();
                                /* 更正业务流程，该业务场景已限制
                                // 翻牌时，翻入翻出的empAgreementId相同，需排除翻出的
                                if (!SocialSecurityConst.TASK_TYPE_5.equals(String.valueOf(taskCategory)) &&
                                    !SocialSecurityConst.TASK_TYPE_6.equals(String.valueOf(taskCategory)) &&
                                    !SocialSecurityConst.TASK_TYPE_7.equals(String.valueOf(taskCategory)) &&
                                    !SocialSecurityConst.TASK_TYPE_14.equals(String.valueOf(taskCategory)) &&
                                    !SocialSecurityConst.TASK_TYPE_15.equals(String.valueOf(taskCategory))
                                    ) {
                                    break;
                                }*/
                            empTaskId = bo.getEmpTaskId();
                        }
                    } else {
                        logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_AGREEMENT_UPDATE).setContent("根据oldEmpAgreementId未找到旧的任务单"));
                        // 如果没有查到旧的雇员协议：
                        if (paramMap.get("socialType") != null) {
                            String socialType = paramMap.get("socialType").toString();
                            taskCategory = Integer.parseInt(socialType);
                        }
                    }

                    // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
                    // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
                    // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，social_startAndStop为true，oldAgreementId一概不记录，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
                    // 不为true，则oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
                    // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（仅非0转0）；
//                    if (Integer.parseInt(SocialSecurityConst.TASK_TYPE_3) == taskCategory && paramMap.get("oldEmpAgreementId") != null) {
                    if ((paramMap.get("social_startAndStop") == null
                        || !Boolean.valueOf(paramMap.get("social_startAndStop").toString())) && paramMap.get("oldEmpAgreementId") != null) {
                        oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
                    }

                    AfCompanyDetailResponseDTO afCompanyDetailResponseDTO = null;

                    if (dto != null) {
                        AfEmployeeCompanyDTO afEmployeeCompanyDTO = dto.getEmployeeCompany();

                        if (afEmployeeCompanyDTO != null) {
                            afCompanyDetailResponseDTO = commonApiUtils.getServiceCenterInfo(afEmployeeCompanyDTO.getCompanyId());
                        }
                    }

                    Map<String, Object> cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");
                    if (cityCodeMap != null) {
                        cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));

                        if (empTaskId != null) {
                            cityCodeMap.put("preEmpTaskId", empTaskId);
                        }
                    }
                    ssEmpTaskFrontService.saveEmpTaskTc(taskMsgDTO, taskCategory, ProcessCategory.AF_EMP_AGREEMENT_UPDATE.getCategory(), 1, oldAgreementId, dto, afCompanyDetailResponseDTO, cityCodeMap);

                } catch (Exception e) {
                    logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey() + "#" + TaskSink.AF_EMP_AGREEMENT_UPDATE).setContent(e.getMessage()));
                }
            }
        }
    }

    /**
     * 调整或更正停办处理
     *
     * @param taskMsgDTO
     * @param isChange
     */
    private void agreementAdjustOrUpdateEmpStop(TaskCreateMsgDTO taskMsgDTO, Integer isChange, String taskSink) {
        // 非0转0，ProcessCategory为调整，taskCategory为封存，新增一个封存任务单，但需要将oldAgreementId同时存入任务单记录；
        // 因为前道发出新开任务时，已经创建了雇员的费用段，oldAgreementId对应的是前一个费用段，任务单结束时需要依据oldAgreementId进行回调，以便前道对其进行处理
        logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()).setContent(" JSON: " + JSON.toJSONString(taskMsgDTO)));
        Map<String, Object> paramMap = taskMsgDTO.getVariables();
        String oldAgreementId = null;
        Map<String, Object> cityCodeMap = null;
        Long empTaskId = null;

        // 如果oldAgreementId存在时，则要回调接口，通知前道关闭费用段
        // 调整类别任务单，只发一个消息（新旧雇员协议在同一任务单中记录），oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
        // 非调整类别的SOCIAL_NEW,FUND_NEW,ADDED_FUND_NEW类型的任务单，social_startAndStop为true，oldAgreementId一概不记录，任务单回调时，不回调旧雇员协议，仅回调新雇员协议；
        // 不为true，则oldAgreementId需记录，任务单回调时，同时需回调新旧雇员协议；
        // 当SOCIAL_STOP,FUND_STOP,ADDED_FUND_STOP类型的任务单，oldAgreementId需记录，任务单回调时，根据情况回调旧雇员协议（仅非0转0）；
        if (null != paramMap) {
            if (paramMap.get("oldEmpAgreementId") != null) {
                SsEmpTaskBO ssEmpTaskBO = new SsEmpTaskBO();
                ssEmpTaskBO.setBusinessInterfaceId(paramMap.get("oldEmpAgreementId").toString());
                //查询旧的任务类型保存到新的任务单
                List<SsEmpTaskBO> resList = ssEmpTaskService.queryByBusinessInterfaceId(ssEmpTaskBO);
                if (resList.size() > 0) {
                    ssEmpTaskBO = resList.get(0);
                    empTaskId = ssEmpTaskBO.getEmpTaskId();

                    if (ssEmpTaskBO.getTaskCategory().equals(Integer.parseInt(SocialSecurityConst.TASK_TYPE_5))
                        || ssEmpTaskBO.getTaskCategory().equals(Integer.parseInt(SocialSecurityConst.TASK_TYPE_6))) {
                        // 更正前任务单已经是转出或封存状态，如果当前消息还是转出或封存状态，此时不生成任务单
                        logApiUtil.warn(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()).setContent(taskSink+"#"+"更正前任务单已经是转出或封存状态，如果当前消息还是转出或封存状态，此时不生成任务单"));
                        return;
                    }
                }

                cityCodeMap = (Map<String, Object>) paramMap.get("cityCode");

//                if (cityCodeMap == null || cityCodeMap.get("newSocialCityCode") == null
//                    || cityCodeMap.get("newSocialCityCode").equals(cityCodeMap.get("oldSocialCityCode"))) {
                    oldAgreementId = paramMap.get("oldEmpAgreementId").toString();
//                }
                if (cityCodeMap != null) {
                    cityCodeMap.put("socialStartAndStop", paramMap.get("social_startAndStop"));
                    cityCodeMap.put("preEmpTaskId", empTaskId);
                }
            }

            saveSsEmpTask(taskMsgDTO, Integer.parseInt(SocialSecurityConst.TASK_TYPE_5), ProcessCategory.AF_EMP_AGREEMENT_ADJUST.getCategory(), oldAgreementId, cityCodeMap, isChange);
        }
    }

    /**
     * 订阅财务付款申请回调任务单
     *
     * @param message
     * @return
     */
    @StreamListener(TaskSink.PAY_APPLY_PAY_STATUS_STREAM)
    public void applyFinancePayment(Message<PayApplyPayStatusDTO> message) {
        PayApplyPayStatusDTO taskMsgDTO = message.getPayload();
        logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+TaskSink.PAY_APPLY_PAY_STATUS_STREAM).setContent(" JSON: " + JSON.toJSONString(taskMsgDTO)));
        if (taskMsgDTO.getBusinessType() == 1) {
            try {
                ssPaymentComService.savePaymentInfo(taskMsgDTO);
            } catch (Exception e) {
                logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+TaskSink.PAY_APPLY_PAY_STATUS_STREAM).setContent(e.getMessage()));
            }
        }
    }

    /**
     * 订阅客服中心调用更新企业任务单
     *
     * @param message
     * @return
     */
    @StreamListener(TaskSink.AF_COMPANY_SOCIAL_ACCOUNT_ONCE)
    public void updateComTask(Message<TaskCreateMsgDTO> message) {
        TaskCreateMsgDTO taskMsgDTO = message.getPayload();
        logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+TaskSink.AF_COMPANY_SOCIAL_ACCOUNT_ONCE).setContent(" JSON: " + JSON.toJSONString(taskMsgDTO)));
        try {
            SsComTask comTask = ssComTaskService.selectById(taskMsgDTO.getMissionId());
            comTask.setTaskId(taskMsgDTO.getTaskId());
            ssComTaskService.updateById(comTask);
        } catch (Exception e) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+TaskSink.AF_COMPANY_SOCIAL_ACCOUNT_ONCE).setContent(e.getMessage()));
        }
    }

    /**
     * 从接口获取数据并保存到社保雇员任务单表
     *
     * @param taskMsgDTO
     * @param oldAgreementId
     * @return
     */
    private AfEmployeeInfoDTO callEmpAgreement(TaskCreateMsgDTO taskMsgDTO, Integer processCategory, String oldAgreementId) {
        Long empAgreementId = null;
        // 翻牌或调整通道时，如果是转出或封存的，根据oldAgreementId去获取转出或封存前的雇员信息
        if (TaskSink.SOCIAL_STOP.equals(taskMsgDTO.getTaskType()) && (
            processCategory.equals(ProcessCategory.AF_EMP_COMPANY_CHANGE.getCategory())
                || (StringUtils.isEmpty(oldAgreementId)
                && processCategory.equals(ProcessCategory.AF_EMP_AGREEMENT_ADJUST.getCategory())
            )
        )) {
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            if (null != paramMap) {
                empAgreementId = Long.parseLong(paramMap.get("oldEmpAgreementId").toString());
            }
        } else {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"callEmpAgreement").setContent("fund get employee info taskMsgDTO.getMissionId():" + taskMsgDTO.getMissionId()));
            Map<String, Object> paramMap = taskMsgDTO.getVariables();
            if (null != paramMap && paramMap.get("missionId") != null) {
                String varMissionId = paramMap.get("missionId").toString();
                logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"callEmpAgreement").setContent("fund get employee info paramMap.get(missionId):" + varMissionId));

                if (org.apache.commons.lang.StringUtils.isNotEmpty(varMissionId)) {
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
        logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"callEmpAgreement").setContent("empAgreementId：" + empAgreementId));
        AfEmployeeInfoDTO afEmployeeInfoDTO = null;
        try {
            afEmployeeInfoDTO = afEmployeeSocialProxy.getByEmpAgreement(empAgreementId);
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"callEmpAgreement").setContent("afEmployeeInfoDTO：" + JSON.toJSONString(afEmployeeInfoDTO)));
        } catch (Exception e) {
            logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"callEmpAgreement").setContent(e.getMessage()));
        }
        return afEmployeeInfoDTO;
    }

    /**
     * 从接口获取数据并保存到社保雇员任务单表
     *
     * @param taskMsgDTO
     * @param socialType
     * @return
     */
    private void saveSsEmpTask(TaskCreateMsgDTO taskMsgDTO, Integer socialType, Integer processCategory, String oldAgreementId, Map<String, Object> cityCodeMap, Integer isChange) {
        try {
            // 如果转外地，则取旧雇员协议
            if (oldAgreementId != null && cityCodeMap != null) {
                if (cityCodeMap.get("newSocialCityCode") != null && !SocialSecurityConst.SHANGHAI_CITY_CODE.equals(cityCodeMap.get("newSocialCityCode"))) {
                    cityCodeMap.put("oldAgreementId", oldAgreementId);
                    oldAgreementId = null;
                }
            }

            // 调用当前雇员信息获取接口
            AfEmployeeInfoDTO dto = callEmpAgreement(taskMsgDTO, processCategory, oldAgreementId);
            AfCompanyDetailResponseDTO afCompanyDetailResponseDTO = null;

            AfEmployeeCompanyDTO afEmployeeCompanyDTO = null;
            if (dto != null) {
                afEmployeeCompanyDTO = dto.getEmployeeCompany();

                if (afEmployeeCompanyDTO != null) {
                    afCompanyDetailResponseDTO = commonApiUtils.getServiceCenterInfo(afEmployeeCompanyDTO.getCompanyId());
                }
            }

            //保存雇员任务单表数据
            ssEmpTaskFrontService.saveSsEmpTask(taskMsgDTO, socialType, processCategory, isChange, oldAgreementId, dto, afCompanyDetailResponseDTO, cityCodeMap);

            // 判断是否自动抵消
            if (afEmployeeCompanyDTO != null) {
                logApiUtil.info(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"saveSsEmpTask").setContent("判断是否自动抵消的类型"));
                boolean isOK = false;
                if (ArrayUtils.contains(AUTO_OFFSET_TASK_CATEGORIES, socialType)) {
                    isOK = ssEmpTaskService.autoOffset(afEmployeeCompanyDTO.getCompanyId(), afEmployeeCompanyDTO.getEmployeeId(), 1);
                }
                if (!isOK) {
                    Map<String, Object> paramMap = taskMsgDTO.getVariables();
                    String operationType = (String)paramMap.get("operation_type");

                    if (ArrayUtils.contains(ALL_IN_TASK_CATEGORIES, socialType)) {
                        ssEmpTaskService.autoOffset(afEmployeeCompanyDTO.getCompanyId(), afEmployeeCompanyDTO.getEmployeeId(), (operationType != null)? 3 : 2);
                    } else if (ArrayUtils.contains(ALL_OUT_TASK_CATEGORIES, socialType)) {
                        ssEmpTaskService.autoOffset(afEmployeeCompanyDTO.getCompanyId(), afEmployeeCompanyDTO.getEmployeeId(), (operationType != null)? 2 : 3);
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logApiUtil.error(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"saveSsEmpTask").setContent(sw.toString()));
            pw.close();
//            logApiUtil.error(LogMessage.create().setTitle(LogInfo.SOURCE_MESSAGE.getKey()+"#"+"saveSsEmpTask").setContent(e.getMessage()));
        }
    }

}
