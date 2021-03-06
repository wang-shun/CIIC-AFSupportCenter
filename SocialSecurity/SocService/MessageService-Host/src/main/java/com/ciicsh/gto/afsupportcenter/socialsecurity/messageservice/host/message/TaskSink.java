package com.ciicsh.gto.afsupportcenter.socialsecurity.messageservice.host.message;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * Created by houwanhua on 2018/2/24
 */
@Service
public interface TaskSink {

    /**
     * 雇员新增
     */
    String AF_EMP_IN = "common_taskservice_af_empin_channel";

    /**
     * 雇员终止
     */
    String AF_EMP_OUT = "common_taskservice_af_empout_channel";

    /**
     * 雇员补缴
     */
    String AF_EMP_MAKE_UP = "common_taskservice_af_emp_make_up_channel";

    /**
     * 雇员翻牌
     */
    String AF_EMP_COMPANY_CHANGE = "common_taskservice_af_emp_company_change_channel";

    /**
     * 雇员服务协议调整
     */
    String AF_EMP_AGREEMENT_ADJUST = "common_taskservice_af_emp_agreement_adjust_channel";

    /**
     * 雇员服务协议更正
     */
    String AF_EMP_AGREEMENT_UPDATE = "common_taskservice_af_emp_agreement_update_channel";

    /**
     * 财务中心付款申请返回
     */
    String PAY_APPLY_PAY_STATUS_STREAM = "pay_apply_pay_status_stream_channel";

    /**
     * 接受客服中心调用更新企业任务单
     */
    String AF_COMPANY_SOCIAL_ACCOUNT_ONCE = "common_taskservice_af_company_social_account_once_channel";

    /**
     * 社保办理
     */
    String SOCIAL_NEW = "social_new";

    /**
     * 社保停办
     */
    String SOCIAL_STOP = "social_stop";

    /**
     * 社保补缴
     */
    String SOCIAL_MAKE_UP = "social_make_up";

    @Input(AF_EMP_IN)
    MessageChannel afEmpIn();

    @Input(AF_EMP_OUT)
    MessageChannel afEmpOut();

    @Input(AF_EMP_MAKE_UP)
    MessageChannel afEmpMakeUp();

    @Input(AF_EMP_COMPANY_CHANGE)
    MessageChannel afEmpCompanyChange();

    @Input(AF_EMP_AGREEMENT_ADJUST)
    MessageChannel afEmpAgreementAdjust();

    @Input(AF_EMP_AGREEMENT_UPDATE)
    MessageChannel afEmpAgreementUpdate();

    @Input(PAY_APPLY_PAY_STATUS_STREAM)
    MessageChannel rejectPayApplyIdStream();

    @Input(AF_COMPANY_SOCIAL_ACCOUNT_ONCE)
    MessageChannel afCompanySocialAccountOnce();

}
