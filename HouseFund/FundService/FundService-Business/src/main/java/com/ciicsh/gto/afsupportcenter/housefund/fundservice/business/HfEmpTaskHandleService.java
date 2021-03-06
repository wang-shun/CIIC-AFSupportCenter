package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.service.IService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.*;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfArchiveBasePeriod;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpTask;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.sheetservice.api.dto.Result;

import java.util.List;

public interface HfEmpTaskHandleService extends IService<HfEmpTask> {
    /**
     * 查询雇员任务单办理信息
     * @param hfEmpTaskHandlePostBo
     * @return
     */
    List<HfEmpTaskHandleBo> getEmpTaskHandleData(HfEmpTaskHandlePostBo hfEmpTaskHandlePostBo);

    JsonResult inputDataSave(JSONObject params, boolean isHandle);

    JsonResult handleCancel(List<Long> empTaskIdList, String currentUser);

    JsonResult handleReject(HfEmpTaskBatchRejectBo hfEmpTaskBatchRejectBo, boolean isRetry);

    /**
     *  根据现有的任务单创建转移任务单
     *  @param hfEmpTaskCreateTransBo
     * @return
     */
    int createTransEmpTask(HfEmpTaskCreateTransBo hfEmpTaskCreateTransBo);

    int[] getRoundTypeProxy(String policyId, Integer payAccountType, String effectiveMonth, String hfTypeDicItemCode);

    List<ComposedEmpBasePeriodBO> composeEmpBasePeriod(List<HfArchiveBasePeriod> existsHfArchiveBasePeriodList);

    List<HfEmpTaskExportBo> queryHistoryEmpTask(Boolean isSelf, Long empTaskId);

    List<HfEmpTaskExportBo> getOriginEmpTask(String companyId, String employeeId, Integer hfType);

    HfEmpTaskExportBo getOriginEmpTaskById(Long empTaskId);

    Result apiCompleteTask(String taskId, String assignee) throws Exception;
}
