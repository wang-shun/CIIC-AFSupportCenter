package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.ciicsh.gto.afcompanycenter.commandservice.api.dto.employee.AfEmpSocialUpdateDateDTO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.*;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.ComAccountExtBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.customer.ComAccountParamExtBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.*;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.utils.CommonApiUtils;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpArchiveConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpTaskConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpTaskPeriodConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfMonthChargeConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dao.HfEmpTaskMapper;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto.TaskSheetRequestDTO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.*;
import com.ciicsh.gto.afsupportcenter.util.constant.DictUtil;
import com.ciicsh.gto.afsupportcenter.util.exception.BusinessException;
import com.ciicsh.gto.afsupportcenter.util.kit.DateKit;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import com.ciicsh.gto.afsystemmanagecenter.apiservice.api.dto.item.GetSSPItemsRequestDTO;
import com.ciicsh.gto.afsystemmanagecenter.apiservice.api.dto.item.SSPItemDTO;
import com.ciicsh.gto.commonservice.util.dto.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@Service
public class HfEmpTaskHandleServiceImpl extends ServiceImpl<HfEmpTaskMapper, HfEmpTask> implements HfEmpTaskHandleService {
    @Autowired
    private HfEmpArchiveService hfEmpArchiveService;
    @Autowired
    private HfMonthChargeService hfMonthChargeService;
    @Autowired
    private HfArchiveBasePeriodService hfArchiveBasePeriodService;
    @Autowired
    private HfEmpTaskPeriodService hfEmpTaskPeriodService;
    @Autowired
    private HfComAccountService hfComAccountService;
    @Autowired
    private HfArchiveBaseAdjustService hfArchiveBaseAdjustService;
    @Autowired
    private CommonApiUtils commonApiUtils;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMM");

    private BlockingQueue<GetSSPItemsRequestDTO> getRoundingTypeParamsQueue = new LinkedBlockingDeque<>();
    private BlockingQueue<SSPItemDTO> getRoundingTypeResultQueue = new LinkedBlockingDeque<>();
    private volatile boolean isExit = false;

    @Override
    public List<HfEmpTaskHandleBo> getEmpTaskHandleData(HfEmpTaskHandlePostBo hfEmpTaskHandleDTO) {
        return baseMapper.getEmpTaskHandleData(hfEmpTaskHandleDTO);
    }

    /**
     * 雇员任务单相关数据保存
     *
     * @param params JSON格式对象参数
     * @param isHandle 是否办理
     * @return JSON格式处理结果
     */
    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public JsonResult inputDataSave(JSONObject params, boolean isHandle) {
        Long empTaskId = params.getLong("empTaskId");
        Integer taskStatus = params.getInteger("taskStatus");
        HfEmpTask hfEmpTask = this.selectById(empTaskId);
        if (hfEmpTask == null || !hfEmpTask.getActive()) {
            return JsonResultKit.ofError("当前任务单已不存在");
        }
        if (StringUtils.isEmpty(hfEmpTask.getTaskId())) {
            return JsonResultKit.ofError("当前任务单TaskId为空");
        }
        if (StringUtils.isEmpty(hfEmpTask.getBusinessInterfaceId())) {
            return JsonResultKit.ofError("当前任务单BusinessInterfaceId为空");
        }
        if ((hfEmpTask.getTaskStatus() != null && !hfEmpTask.getTaskStatus().equals(taskStatus))
            || (hfEmpTask.getTaskStatus() == null && taskStatus != HfEmpTaskConstant.TASK_STATUS_UNHANDLED)) {
            return JsonResultKit.ofError("当前雇员任务单状态已变更，处理失败");
        }

        HfEmpTask inputHfEmpTask = new HfEmpTask();
        inputHfEmpTask.setEmpTaskId(empTaskId);
        inputHfEmpTask.setTaskCategory(params.getInteger("taskCategory"));

        // 任务单费用段是否存在判断
        JSONArray operatorListData = params.getJSONArray("operatorListData");
        switch (inputHfEmpTask.getTaskCategory()) {
            case HfEmpTaskConstant.TASK_CATEGORY_IN_ADD:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_TRANS_IN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_OPEN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_MULTI_TRANS_IN:
            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST:
            case HfEmpTaskConstant.TASK_CATEGORY_REPAIR:
                if (CollectionUtils.isEmpty(operatorListData)) {
                    return JsonResultKit.ofError("当前任务单费用段信息为空");
                } else {
                    String startMonth = "999912";
                    String endMonth = "190001";
                    String dataStartMonth;
                    String dataEndMonth;

                    for (int i = 0; i < operatorListData.size(); i++) {
                        JSONObject data = operatorListData.getJSONObject(i);
                        dataStartMonth = data.getString("startMonth");
                        dataEndMonth = data.getString("endMonth");
                        if (StringUtils.isNotEmpty(dataStartMonth)) {
                            if (Integer.valueOf(startMonth) > Integer.valueOf(dataStartMonth)) {
                                startMonth = dataStartMonth;
                            }
                        } else {
                            startMonth = "190001";
                        }

                        if (StringUtils.isNotEmpty(dataEndMonth)) {
                            if (Integer.valueOf(endMonth) < Integer.valueOf(dataEndMonth)) {
                                endMonth = dataEndMonth;
                            }
                        } else {
                            endMonth = "999912";
                        }
                    }

                    if (!"190001".equals(startMonth)) {
                        inputHfEmpTask.setStartMonth(startMonth);
                    }
                    if (!"999912".equals(endMonth)) {
                        inputHfEmpTask.setEndMonth(endMonth);
                    }
                }
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_MULTI_TRANS_OUT:
//            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_CLOSE:
                if (isHandle) {
                    YearMonth hfMonthDate = YearMonth.parse(params.getString("hfMonth"), formatter);
                    YearMonth endMonthDate = YearMonth.parse(params.getString("endMonth"), formatter);

                    if (hfMonthDate.isBefore(endMonthDate) || hfMonthDate.equals(endMonthDate)) {
                        return JsonResultKit.ofError("请于客户汇缴月到达截止缴费月的次月来办理当前任务单");
                    } else if (hfMonthDate.isAfter(endMonthDate.plusMonths(1))) {
                        return JsonResultKit.ofError("当前任务单已逾期，不能办理");
                    }
                }
                break;
            default:
                break;
        }

        inputHfEmpTask.setHfEmpAccount(params.getString("hfEmpAccount"));
//        String startMonth = params.getString("startMonth");
//        if (StringUtils.isNotEmpty(startMonth)) {
//            inputHfEmpTask.setStartMonth(startMonth);
//        }
        inputHfEmpTask.setOperationRemind(params.getInteger("operationRemind"));
        String operationRemindDateStr = params.getString("operationRemindDate");
        if (StringUtils.isNotBlank(operationRemindDateStr)) {
            inputHfEmpTask.setOperationRemindDate(LocalDate.parse(operationRemindDateStr));
        }
        inputHfEmpTask.setHandleRemark(params.getString("handleRemark"));
        inputHfEmpTask.setRejectionRemark(params.getString("rejectionRemark"));
        inputHfEmpTask.setModifiedBy("test"); //TODO
        inputHfEmpTask.setModifiedTime(LocalDateTime.now());

        if (isHandle) {
            ComAccountParamExtBo comAccountParamExtBo = new ComAccountParamExtBo();
            comAccountParamExtBo.setCompanyId(hfEmpTask.getCompanyId());
            List<ComAccountExtBo> hfComAccountList = hfComAccountService.queryHfComAccountList(comAccountParamExtBo);
            if (CollectionUtils.isNotEmpty(hfComAccountList)) {
                if (hfComAccountList.size() > 1) {
                    return JsonResultKit.ofError("当前雇员任务单所属的企业账户数据有误");
                }
            } else {
                return JsonResultKit.ofError("当前雇员任务单所属的企业账户不存在");
            }

            JsonResult rlt = setEmpTask(params, inputHfEmpTask);
            if (rlt.getCode() != 200) {
                return rlt;
            }

            // 雇员档案处理
            inputHfEmpTask.setHfType(hfEmpTask.getHfType());
            Long existEmpArchive = (Long) rlt.getData();
            Long newEmpArchive = handleEmpArchive(params, existEmpArchive, inputHfEmpTask);
            this.updateById(inputHfEmpTask);

            if (newEmpArchive != null) {
                inputHfEmpTask.setEmpArchiveId(newEmpArchive);
            } else {
                inputHfEmpTask.setEmpArchiveId(existEmpArchive);
            }

            inputHfEmpTask.setCompanyId(hfEmpTask.getCompanyId());
            inputHfEmpTask.setEmployeeId(hfEmpTask.getEmployeeId());
            inputHfEmpTask.setTaskId(hfEmpTask.getTaskId());
            inputHfEmpTask.setBusinessInterfaceId(hfEmpTask.getBusinessInterfaceId());
        } else {
            this.updateById(inputHfEmpTask);
        }

        List<HfArchiveBasePeriod> hfArchiveBasePeriodList = null;
        List<HfEmpTaskPeriod> hfEmpTaskPeriodList;
        // 任务单费用段操作处理
        if (operatorListData != null) {
            hfEmpTaskPeriodList = operatorListData.toJavaList(HfEmpTaskPeriod.class);
            List<Long> empTaskPeriodIdList = new ArrayList<>();
            hfEmpTaskPeriodList.stream().forEach(e -> {
                if (e.getEmpTaskPeriodId() != null) {
                    e.setModifiedBy(inputHfEmpTask.getModifiedBy());
                    e.setModifiedTime(LocalDateTime.now());
                    empTaskPeriodIdList.add(e.getEmpTaskPeriodId());
                } else {
                    e.setEmpTaskId(empTaskId);
                    e.setCreatedBy(inputHfEmpTask.getModifiedBy());
                    e.setModifiedBy(inputHfEmpTask.getModifiedBy());
                }
            });
            Wrapper<HfEmpTaskPeriod> wrapper = new EntityWrapper<>();
            wrapper.eq("emp_task_id", empTaskId);
            wrapper.eq("is_active", 1);
            wrapper.notIn("emp_task_period_id", empTaskPeriodIdList);
            hfEmpTaskPeriodService.delete(wrapper);
            hfEmpTaskPeriodService.insertOrUpdateBatch(hfEmpTaskPeriodList);

            if (isHandle) {
                switch (inputHfEmpTask.getTaskCategory()) {
                    case HfEmpTaskConstant.TASK_CATEGORY_IN_ADD:
                    case HfEmpTaskConstant.TASK_CATEGORY_IN_TRANS_IN:
                    case HfEmpTaskConstant.TASK_CATEGORY_IN_OPEN:
                    case HfEmpTaskConstant.TASK_CATEGORY_IN_MULTI_TRANS_IN:
                    case HfEmpTaskConstant.TASK_CATEGORY_ADJUST:
                        if (inputHfEmpTask.getTaskCategory() != HfEmpTaskConstant.TASK_CATEGORY_ADJUST) {
                            List<HfEmpTaskPeriod> createEmpBasePeriodList = hfEmpTaskPeriodList.stream().filter(e
                                    -> e.getRemitWay() == HfEmpTaskPeriodConstant.REMIT_WAY_NORMAL
                            ).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(createEmpBasePeriodList)) {
                                hfArchiveBasePeriodList = createEmpBasePeriod(inputHfEmpTask, createEmpBasePeriodList);
                            } else {
                                throw new BusinessException("正常汇缴的任务单费用段数据创建失败");
                            }

                            List<HfEmpTaskPeriod> repairEmpBasePeriodList =  hfEmpTaskPeriodList.stream().filter(e
                                -> e.getRemitWay() == HfEmpTaskPeriodConstant.REMIT_WAY_REPAIR).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(repairEmpBasePeriodList)) {
                                List<HfArchiveBasePeriod> repairArchiveBasePeriodList = repairEmpBasePeriod(inputHfEmpTask, repairEmpBasePeriodList);
                                if (CollectionUtils.isNotEmpty(repairArchiveBasePeriodList)) {
                                    hfArchiveBasePeriodList.addAll(repairArchiveBasePeriodList);
                                } else {
                                    // TODO log.error uncovered logic
                                    System.out.println("Creation type emp task, repairArchiveBasePeriodList is empty, it caused by uncovered logic");
                                    throw new BusinessException("补缴公积金费用段数据取得失败");
                                }
                            }
                            createHfMonthCharge(inputHfEmpTask, hfArchiveBasePeriodList);
                        } else {
//                            List<HfEmpTaskPeriod> adjustEmpBasePeriodList = hfEmpTaskPeriodList.stream().filter(e
//                                    -> e.getRemitWay() == HfEmpTaskPeriodConstant.REMIT_WAY_ADJUST
//                            ).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(hfEmpTaskPeriodList)) {
                                hfArchiveBasePeriodList = adjustEmpBasePeriod(inputHfEmpTask, hfEmpTaskPeriodList);
                            } else {
                                throw new BusinessException("调整的任务单费用段数据创建失败");
                            }
                            createHfMonthCharge(inputHfEmpTask, hfArchiveBasePeriodList);
                        }
                        break;
                    case HfEmpTaskConstant.TASK_CATEGORY_REPAIR:
                        hfArchiveBasePeriodList = repairEmpBasePeriod(inputHfEmpTask, hfEmpTaskPeriodList);
                        createHfMonthCharge(inputHfEmpTask, hfArchiveBasePeriodList);
                        break;

                    default:
                        break;
                }
            }
        } else {
            String endMonth = params.getString("endMonth");
            inputHfEmpTask.setEndMonth(endMonth);
            Map<String, Object> condition = new HashMap<>();
            condition.put("emp_task_id", empTaskId);
            condition.put("is_active", 1);

            HfEmpTaskPeriod hfEmpTaskPeriod = new HfEmpTaskPeriod();
            hfEmpTaskPeriod.setCreatedBy(inputHfEmpTask.getModifiedBy());
            hfEmpTaskPeriod.setEndMonth(endMonth);
            hfEmpTaskPeriod.setHfMonth(params.getString("hfMonth"));

            hfEmpTaskPeriodList = hfEmpTaskPeriodService.selectByMap(condition);
            if (CollectionUtils.isNotEmpty(hfEmpTaskPeriodList)) {
                if (hfEmpTaskPeriodList.size() > 1) {
                    return JsonResultKit.ofError("当前雇员任务单费用段数据不正确");
                }

                hfEmpTaskPeriod.setEmpTaskPeriodId(hfEmpTaskPeriodList.get(0).getEmpTaskPeriodId());
            } else {
                hfEmpTaskPeriod.setEmpTaskId(empTaskId);
                hfEmpTaskPeriod.setRemitWay(HfEmpTaskPeriodConstant.REMIT_WAY_NONE);
                hfEmpTaskPeriod.setModifiedTime(LocalDateTime.now());
                hfEmpTaskPeriod.setModifiedBy(inputHfEmpTask.getModifiedBy());
            }
            hfEmpTaskPeriodService.insertOrUpdate(hfEmpTaskPeriod);

            if (isHandle) {
                // 转出或封存任务单时，没有任务单费用段信息
                switch (inputHfEmpTask.getTaskCategory()) {
//                    case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_CLOSE:
                    case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
                    case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
                    case HfEmpTaskConstant.TASK_CATEGORY_OUT_MULTI_TRANS_OUT:
                        HfArchiveBasePeriod hfArchiveBasePeriod = stopEmpBasePeriod(inputHfEmpTask, hfEmpTaskPeriod);
                        hfArchiveBasePeriodList = new ArrayList<>();
                        hfArchiveBasePeriodList.add(hfArchiveBasePeriod);
                        createHfMonthCharge(inputHfEmpTask, hfArchiveBasePeriodList);
                        break;
                    default:
                        break;
                }
            }
        }
        if (isHandle) {
            try {
                int rtnCode = apiUpdateConfirmDate(inputHfEmpTask.getCompanyId(),
                    Long.valueOf(inputHfEmpTask.getBusinessInterfaceId()),
                    inputHfEmpTask,
                    hfArchiveBasePeriodList,
                    false);
            } catch (Exception e) {
                e.printStackTrace(); // TODO log
                throw new BusinessException("访问客服中心的雇员任务单实缴金额回调接口失败");
            }
            try {
                Result result = apiCompleteTask(inputHfEmpTask.getTaskId(),
                    inputHfEmpTask.getModifiedBy());
            } catch (Exception e) {
                e.printStackTrace(); // TODO log
                throw new BusinessException("访问客服中心的完成任务接口失败");
            }
        }
        return JsonResultKit.of();
    }

    /**
     * 雇员任务单批退处理
     *
     * @param hfEmpTaskBatchRejectBo 页面提交参数对象
     * @return 处理结果
     */
    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public JsonResult handleReject(HfEmpTaskBatchRejectBo hfEmpTaskBatchRejectBo) {
        Long empTaskId = hfEmpTaskBatchRejectBo.getSelectedData()[0];
        HfEmpTask hfEmpTask = this.selectById(empTaskId);
        if (hfEmpTask == null || !hfEmpTask.getActive()) {
            return JsonResultKit.ofError("当前任务单已不存在");
        }
        if (StringUtils.isEmpty(hfEmpTask.getTaskId())) {
            return JsonResultKit.ofError("当前任务单TaskId为空");
        }
        if (StringUtils.isEmpty(hfEmpTask.getBusinessInterfaceId())) {
            return JsonResultKit.ofError("当前任务单BusinessInterfaceId为空");
        }

        HfEmpTask inputHfEmpTask = new HfEmpTask();
        inputHfEmpTask.setEmpTaskId(empTaskId);
        inputHfEmpTask.setStartMonth(hfEmpTask.getStartMonth());
        inputHfEmpTask.setEndMonth(hfEmpTask.getEndMonth());
        inputHfEmpTask.setTaskStatus(HfEmpTaskConstant.TASK_STATUS_REJECTED);
        inputHfEmpTask.setRejectionRemark(hfEmpTaskBatchRejectBo.getRejectionRemark());
        inputHfEmpTask.setModifiedTime(LocalDateTime.now());
        inputHfEmpTask.setModifiedBy("test"); // TODO currentUser

        this.updateById(inputHfEmpTask);

        try {
            int rtnCode = apiUpdateConfirmDate(hfEmpTask.getCompanyId(),
                Long.valueOf(hfEmpTask.getBusinessInterfaceId()),
                inputHfEmpTask,
                new ArrayList<HfArchiveBasePeriod>(1) { { add(new HfArchiveBasePeriod()); } },
                true);
        } catch (Exception e) {
            e.printStackTrace(); // TODO log
            throw new BusinessException("访问客服中心的雇员任务单实缴金额回调接口失败");
        }
        try {
            Result result = apiCompleteTask(hfEmpTask.getTaskId(),
                "test"); // TODO currentUser
        } catch (Exception e) {
            e.printStackTrace(); // TODO log
            throw new BusinessException("访问客服中心的完成任务接口失败");
        }

        return JsonResultKit.of();
    }

    /**
     * 雇员任务单撤销处理
     * @param empTaskIdList
     * @return
     */
    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public JsonResult handleCancel(List<Long> empTaskIdList, String currentUser) {
        List<HfEmpTask> hfEmpTaskList = this.selectBatchIds(empTaskIdList);
        if (CollectionUtils.isNotEmpty(hfEmpTaskList)) {
            List<Long> outEmpTaskIdList = new ArrayList<>();
            List<HfEmpArchive> adjustEmpArchiveIdList = new ArrayList<>();
            List<Long> repairEmpTaskIdList = new ArrayList<>();
            List<Long> inEmpTaskIdList = new ArrayList<>();
            List<HfEmpTask> updateHfEmpTaskList = new ArrayList<>(hfEmpTaskList.size());

            Map<Long, Long> empTaskIdEmpArchiveIdMap = hfEmpArchiveService.queryHfEmpArchiveByEmpTaskIds(empTaskIdList);

            for (HfEmpTask hfEmpTask : hfEmpTaskList) {
                if (!hfEmpTask.getActive()) {
                    String taskCategoryName = DictUtil.getInstance().getTextByItemValueAndTypeValue(
                        String.valueOf(hfEmpTask.getTaskCategory()),
                        DictUtil.TYPE_VALUE_HF_LOCAL_TASK_CATEGORY,
                        false);
                    return JsonResultKit.ofError(
                         String.format("客户编号：%1$s雇员编号：%2$s的雇员%3$s任务单（ID：%4$d）已经被删除",
                             hfEmpTask.getCompanyId(),
                             hfEmpTask.getEmployeeId(),
                             taskCategoryName,
                             hfEmpTask.getEmpTaskId()
                         )
                    );
                } else if (hfEmpTask.getTaskStatus() != HfEmpTaskConstant.TASK_STATUS_HANDLED) {
                    String taskCategoryName = DictUtil.getInstance().getTextByItemValueAndTypeValue(
                        String.valueOf(hfEmpTask.getTaskCategory()),
                        DictUtil.TYPE_VALUE_HF_LOCAL_TASK_CATEGORY,
                        false);
                    return JsonResultKit.ofError(
                        String.format("客户编号：%1$s雇员编号：%2$s的雇员%3$s任务单（ID：%4$d）不是已办状态",
                            hfEmpTask.getCompanyId(),
                            hfEmpTask.getEmployeeId(),
                            taskCategoryName,
                            hfEmpTask.getEmpTaskId()
                        )
                    );
                }
                HfEmpArchive hfEmpArchive;
                switch (hfEmpTask.getTaskCategory()) {
//                    case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_CLOSE:
                    case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
                    case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
                    case HfEmpTaskConstant.TASK_CATEGORY_OUT_MULTI_TRANS_OUT:
                        hfEmpArchive = new HfEmpArchive();
                        if (empTaskIdEmpArchiveIdMap != null && hfEmpTask.getEmpArchiveId() == null) {
                            hfEmpArchive.setEmpArchiveId(empTaskIdEmpArchiveIdMap.get(hfEmpTask.getEmpArchiveId()));
                        } else {
                            hfEmpArchive.setEmpArchiveId(hfEmpTask.getEmpArchiveId());
                        }

                        hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_COMPLETED);
                        adjustEmpArchiveIdList.add(hfEmpArchive);
                        outEmpTaskIdList.add(hfEmpTask.getEmpTaskId());
                        break;
                    case HfEmpTaskConstant.TASK_CATEGORY_REPAIR:
                        repairEmpTaskIdList.add(hfEmpTask.getEmpTaskId());
                        break;
                    case HfEmpTaskConstant.TASK_CATEGORY_ADJUST:
//                        hfEmpArchive = new HfEmpArchive();
//                        if (empTaskIdEmpArchiveIdMap != null && hfEmpTask.getEmpArchiveId() == null) {
//                            hfEmpArchive.setEmpArchiveId(empTaskIdEmpArchiveIdMap.get(hfEmpTask.getEmpArchiveId()));
//                        } else {
//                            hfEmpArchive.setEmpArchiveId(hfEmpTask.getEmpArchiveId());
//                        }
//                        hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_CLOSED);
//                        adjustEmpArchiveIdList.add(hfEmpArchive);
//                        inEmpTaskIdList.add(hfEmpTask.getEmpTaskId());
                        break;
                    default:
                        inEmpTaskIdList.add(hfEmpTask.getEmpTaskId());
                        break;
                }

                HfEmpTask updateHfEmpTask = new HfEmpTask();
                updateHfEmpTask.setEmpTaskId(hfEmpTask.getEmpTaskId());
                updateHfEmpTask.setTaskStatus(HfEmpTaskConstant.TASK_STATUS_UNHANDLED);
                updateHfEmpTask.setModifiedBy("test"); // TODO
                updateHfEmpTask.setModifiedTime(LocalDateTime.now());
                updateHfEmpTaskList.add(updateHfEmpTask);
            }

            if (CollectionUtils.isNotEmpty(outEmpTaskIdList)) {
                HfMonthChargeBo hfMonthChargeBo = new HfMonthChargeBo();
                hfMonthChargeBo.setEmpTaskIdList(outEmpTaskIdList);
                hfMonthChargeBo.setChgPaymentType(HfMonthChargeConstant.PAYMENT_TYPE_NORMAL);
                hfMonthChargeBo.setModifiedBy("test"); // TODO
                hfMonthChargeService.updateHfMonthCharge(hfMonthChargeBo);
                HfArchiveBasePeriodUpdateBo hfArchiveBasePeriodUpdateBo = new HfArchiveBasePeriodUpdateBo();
                hfArchiveBasePeriodUpdateBo.setEmpTaskIdList(outEmpTaskIdList);
                hfArchiveBasePeriodUpdateBo.setModifiedBy("test"); // TODO
                hfArchiveBasePeriodService.updateHfArchiveBasePeriods(hfArchiveBasePeriodUpdateBo);

                if (CollectionUtils.isNotEmpty(adjustEmpArchiveIdList)) {
                    hfEmpArchiveService.updateBatchById(adjustEmpArchiveIdList);
                }
            }
            if (CollectionUtils.isNotEmpty(inEmpTaskIdList)) {
                hfMonthChargeService.deleteHfMonthCharges(inEmpTaskIdList);
                hfArchiveBasePeriodService.deleteHfArchiveBasePeriods(inEmpTaskIdList);

                if (CollectionUtils.isNotEmpty(adjustEmpArchiveIdList)) {
                    hfEmpArchiveService.updateBatchById(adjustEmpArchiveIdList);
                } else {
                    hfEmpArchiveService.deleteHfEmpArchiveByEmpTaskIds(inEmpTaskIdList);
                }
            }
            if (CollectionUtils.isNotEmpty(repairEmpTaskIdList)) {
                hfMonthChargeService.deleteHfMonthCharges(repairEmpTaskIdList);
                hfArchiveBaseAdjustService.deleteHfArchiveBaseAdjusts(repairEmpTaskIdList);
                hfArchiveBasePeriodService.deleteHfArchiveBasePeriods(repairEmpTaskIdList);
            }

            HfEmpTaskPeriodInactiveBo hfEmpTaskPeriodInactiveBo = new HfEmpTaskPeriodInactiveBo();
            hfEmpTaskPeriodInactiveBo.setEmpTaskIdList(empTaskIdList);
            hfEmpTaskPeriodInactiveBo.setModifiedBy("test"); //TODO
            hfEmpTaskPeriodService.inactiveHfEmpTaskPeriods(hfEmpTaskPeriodInactiveBo);
            this.updateBatchById(updateHfEmpTaskList);
        } else {
            return JsonResultKit.ofError("雇员任务单数据不存在");
        }
        return JsonResultKit.of();
    }

    /**
     * 根据任务单费用分段表数据实装雇员档案费用分段表数据
     *
     * @param hfArchiveBasePeriodList 雇员档案费用分段表数据列表
     * @param hfEmpTask 任务单表数据
     * @param hfEmpTaskPeriod 任务费用分段表数据
     * @param repairHfArchiveBasePeriod 差额补缴的雇员档案费用分段表数据
     */
    private void setHfArchiveBasePeriodList(List<HfArchiveBasePeriod> hfArchiveBasePeriodList, HfEmpTask hfEmpTask, HfEmpTaskPeriod hfEmpTaskPeriod, HfArchiveBasePeriod repairHfArchiveBasePeriod) {
        HfArchiveBasePeriod hfArchiveBasePeriod = new HfArchiveBasePeriod();
        System.out.println("e.getArchiveBasePeriodId() = " + hfEmpTaskPeriod.getArchiveBasePeriodId()); // TODO log
        hfArchiveBasePeriod.setArchiveBasePeriodId(hfEmpTaskPeriod.getArchiveBasePeriodId());
        hfArchiveBasePeriod.setEmpTaskId(hfEmpTaskPeriod.getEmpTaskId());
        hfArchiveBasePeriod.setEmployeeId(hfEmpTask.getEmployeeId());
        hfArchiveBasePeriod.setCompanyId(hfEmpTask.getCompanyId());
        hfArchiveBasePeriod.setBaseAmount(hfEmpTaskPeriod.getBaseAmount());
        hfArchiveBasePeriod.setRatioCom(hfEmpTaskPeriod.getRatioCom());
        hfArchiveBasePeriod.setRatioEmp(hfEmpTaskPeriod.getRatioEmp());
        BigDecimal ratioCom = hfEmpTaskPeriod.getRatioCom();
        BigDecimal ratioEmp = hfEmpTaskPeriod.getRatioEmp();
        BigDecimal amount = hfEmpTaskPeriod.getAmount();
//        if (hfEmpTaskPeriod.getRatioCom() != null) {
//            ratioCom = hfEmpTaskPeriod.getRatioCom();
//        }
//        if (hfEmpTaskPeriod.getRatioEmp() != null) {
//            ratioEmp = hfEmpTaskPeriod.getRatioEmp();
//        }
//        if (hfEmpTaskPeriod.getAmount() != null) {
//            amount = hfEmpTaskPeriod.getAmount();
//        }
        hfArchiveBasePeriod.setRatio(ratioCom.add(ratioEmp).setScale(2, BigDecimal.ROUND_HALF_UP));
//        if (hfArchiveBasePeriod.getRatio().compareTo(BigDecimal.ZERO) > 0) {
        if (repairHfArchiveBasePeriod == null) {
            hfArchiveBasePeriod.setAmount(hfEmpTaskPeriod.getAmount());
            hfArchiveBasePeriod.setAmountEmp(amount.multiply(ratioEmp.divide(hfArchiveBasePeriod.getRatio())).setScale(2, BigDecimal.ROUND_HALF_UP));
            hfArchiveBasePeriod.setComAmount(amount.multiply(ratioCom.divide(hfArchiveBasePeriod.getRatio())).setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            // 代码特殊处理，仅为了差额补缴时，不重新计算单月差额
            hfArchiveBasePeriod.setAmountEmp(repairHfArchiveBasePeriod.getAmountEmp());
            hfArchiveBasePeriod.setComAmount(repairHfArchiveBasePeriod.getComAmount());
            hfArchiveBasePeriod.setAmount(repairHfArchiveBasePeriod.getAmountEmp()
                .add(repairHfArchiveBasePeriod.getComAmount()).setScale(2, BigDecimal.ROUND_HALF_UP));
            hfArchiveBasePeriod.setDiffRepair(true);
        }

        hfArchiveBasePeriod.setStartMonth(hfEmpTaskPeriod.getStartMonth());
        hfArchiveBasePeriod.setEndMonth(hfEmpTaskPeriod.getEndMonth());
        hfArchiveBasePeriod.setHfMonth(hfEmpTaskPeriod.getHfMonth());
        hfArchiveBasePeriod.setRepairReason(hfEmpTaskPeriod.getRepairReason());
        hfArchiveBasePeriod.setModifiedBy(hfEmpTask.getModifiedBy());

        if (hfArchiveBasePeriod.getArchiveBasePeriodId() == null) {
            hfArchiveBasePeriod.setCreatedBy(hfEmpTask.getModifiedBy());
            hfArchiveBasePeriod.setEmpArchiveId(hfEmpTask.getEmpArchiveId());
            hfArchiveBasePeriod.setRemitWay(hfEmpTaskPeriod.getRemitWay());
            hfArchiveBasePeriod.setHfType(hfEmpTask.getHfType());
        } else {
            hfArchiveBasePeriod.setModifiedTime(LocalDateTime.now());
        }
        hfArchiveBasePeriodList.add(hfArchiveBasePeriod);
    }

    /**
     * 新进或转入任务时雇员档案费用分段表数据处理
     *
     * @param hfEmpTask 任务单表数据
     * @param hfEmpTaskPeriodList 任务费用分段表数据列表
     * @return 雇员档案费用分段表数据
     */
    private List<HfArchiveBasePeriod> createEmpBasePeriod(HfEmpTask hfEmpTask, List<HfEmpTaskPeriod> hfEmpTaskPeriodList) {
        List<HfArchiveBasePeriod> hfArchiveBasePeriodList = new ArrayList<>(hfEmpTaskPeriodList.size());
        hfEmpTaskPeriodList.stream().forEach(e -> setHfArchiveBasePeriodList(hfArchiveBasePeriodList, hfEmpTask, e, null));
        hfArchiveBasePeriodService.insertOrUpdateBatch(hfArchiveBasePeriodList);
        return hfArchiveBasePeriodList;
    }

    /**
     * 补缴任务时雇员档案费用分段表数据处理
     * 分全额补缴及差额补缴
     *
     * @param hfEmpTask 任务单表数据
     * @param hfEmpTaskPeriodList 任务费用分段表数据列表
     * @return 雇员档案费用分段表数据
     */
    private List<HfArchiveBasePeriod> repairEmpBasePeriod(HfEmpTask hfEmpTask, List<HfEmpTaskPeriod> hfEmpTaskPeriodList) {
        List<HfArchiveBasePeriod> hfArchiveBasePeriodList;
        List<HfArchiveBasePeriod> diffHfArchiveBasePeriodList;
        EntityWrapper<HfArchiveBasePeriod> wrapper = new EntityWrapper<>();
        wrapper.where("employee_id={0} AND is_active = 1", hfEmpTask.getEmployeeId());
        wrapper.orderBy("start_month", true);
        List<HfArchiveBasePeriod> existHfArchiveBasePeriodList = hfArchiveBasePeriodService.selectList(wrapper);

        if (CollectionUtils.isNotEmpty(existHfArchiveBasePeriodList)) {
            hfArchiveBasePeriodList = new ArrayList<>();
            diffHfArchiveBasePeriodList = new ArrayList<>();
            List<HfArchiveBaseAdjust> hfArchiveBaseAdjustList = new ArrayList<>();

            hfEmpTaskPeriodList.stream().forEach(e -> {
                if (StringUtils.isEmpty(e.getStartMonth()) || StringUtils.isEmpty(e.getEndMonth()) || StringUtils.isEmpty(e.getHfMonth())) {
                    throw new BusinessException("补缴任务费用分段中缴费起始年月或缴费截止年月或汇缴年月为空");
                }
                final YearMonth hfMonth = YearMonth.parse(e.getHfMonth(), formatter);
                YearMonth repairStartMonth = YearMonth.parse(e.getStartMonth(), formatter);
                YearMonth repairEndMonth = YearMonth.parse(e.getEndMonth(), formatter);
                if (repairEndMonth.equals(hfMonth) || repairEndMonth.isAfter(hfMonth)) {
                    throw new BusinessException("补缴任务费用分段中缴费截止年月不能在汇缴年月以后");
                }

                YearMonth[] permitYearMonth = new YearMonth[1];
                boolean isMatch = false;

                for (HfArchiveBasePeriod hfArchiveBasePeriod : existHfArchiveBasePeriodList) {
                    YearMonth startMonth = YearMonth.parse(hfArchiveBasePeriod.getStartMonth(), formatter);
                    YearMonth endMonth = hfMonth;
                    if (StringUtils.isNotEmpty(hfArchiveBasePeriod.getEndMonth())) {
                        endMonth = YearMonth.parse(hfArchiveBasePeriod.getEndMonth(), formatter);
                    }
                    // 匹配费用段，如果任务单费用段包含在雇员档案费用段中，那么支持差额补缴；
                    // 差额补缴时，雇员档案费用段不变，增加雇员调整差异数据
                    if ((repairStartMonth.isAfter(startMonth) || repairStartMonth.equals(startMonth))
                        && (repairEndMonth.isBefore(endMonth) || repairEndMonth.equals(endMonth))) {
                        if (!hfEmpTask.getCompanyId().equals(hfArchiveBasePeriod.getCompanyId())) {
                            throw new BusinessException("补缴任务费用分段中缴费期间与所匹配的费用段不属于同一个客户");
                        }
                        setHfArchiveBaseAdjust(hfArchiveBaseAdjustList, hfEmpTask, e, hfArchiveBasePeriod);
                        setHfArchiveBasePeriodList(diffHfArchiveBasePeriodList, hfEmpTask, e, hfArchiveBasePeriod);
                        isMatch = true;
                        break;
                    } else {
                        // 匹配费用段空档区间，如果任务单费用段包含在雇员档案费用段空档区间中，那么支持全额补缴；
                        // 全额补缴时，更新雇员档案费用段，不需要增加雇员调整差异数据
                        if (permitYearMonth[0] == null) {
                            // 匹配最早的费用段，判断补缴段的截止年月是否早于雇员档案费用段的起始年月
                            if (repairEndMonth.isBefore(startMonth)) {
                                if (!hfEmpTask.getCompanyId().equals(hfArchiveBasePeriod.getCompanyId())) {
                                    throw new BusinessException("补缴任务费用分段中缴费期间与所匹配的费用段不属于同一个客户");
                                }
                                permitYearMonth[0] = endMonth;
                                setHfArchiveBasePeriodList(hfArchiveBasePeriodList, hfEmpTask, e, null);
                                isMatch = true;
                                break;
                            }
                        } else if (endMonth.isBefore(hfMonth)) {
                            // 如果雇员档案费用段的截止年月早于当前年月
                            // 那么判断补缴段是否属于两个雇员档案费用段之间的空档期间
                            if (repairStartMonth.isAfter(permitYearMonth[0]) && repairEndMonth.isBefore(startMonth)) {
                                if (!hfEmpTask.getCompanyId().equals(hfArchiveBasePeriod.getCompanyId())) {
                                    throw new BusinessException("补缴任务费用分段中缴费期间与所匹配的费用段不属于同一个客户");
                                }
                                permitYearMonth[0] = endMonth;
                                setHfArchiveBasePeriodList(hfArchiveBasePeriodList, hfEmpTask, e, null);
                                isMatch = true;
                                break;
                            }
//                        } else {
//                            //
//                            if (repairStartMonth.isAfter(startMonth) && repairEndMonth.isBefore(endMonth)) {
//                                if (!hfEmpTask.getCompanyId().equals(hfArchiveBasePeriod.getCompanyId())) {
//                                    throw new BusinessException("补缴任务费用分段中缴费期间与所匹配的费用段不属于同一个客户");
//                                }
//                                setHfArchiveBasePeriodList(hfArchiveBasePeriodList, hfEmpTask, e, null);
//                                isMatch = true;
//                                break;
//                            }
                        }
                    }
                }

                if (!isMatch) {
                    throw new BusinessException("补缴任务费用分段期间不正确");
                }
            });

            if (CollectionUtils.isNotEmpty(hfArchiveBaseAdjustList)) {
                hfArchiveBaseAdjustService.insertBatch(hfArchiveBaseAdjustList);
            }
        } else {
            // 雇员档案费用段不存在时，数据不正常，需先做新增再做补缴
            throw new BusinessException("当前雇员的雇员汇缴月份段数据不存在，不能补缴");
        }

        if (CollectionUtils.isNotEmpty(hfArchiveBasePeriodList)) {
            hfArchiveBasePeriodService.insertOrUpdateBatch(hfArchiveBasePeriodList);
        }

        if (CollectionUtils.isNotEmpty(diffHfArchiveBasePeriodList)) {
            hfArchiveBasePeriodList.addAll(diffHfArchiveBasePeriodList);
        }
        return hfArchiveBasePeriodList;
    }

    /**
     * 调整任务时雇员档案费用分段表数据处理（类似于差额补缴）
     *
     * @param hfEmpTask
     * @param hfEmpTaskPeriodList
     * @return
     */
    private List<HfArchiveBasePeriod> adjustEmpBasePeriod(HfEmpTask hfEmpTask, List<HfEmpTaskPeriod> hfEmpTaskPeriodList) {
        List<HfArchiveBasePeriod> hfArchiveBasePeriodList;
        List<HfArchiveBasePeriod> diffHfArchiveBasePeriodList;
        EntityWrapper<HfArchiveBasePeriod> wrapper = new EntityWrapper<>();
        wrapper.where("employee_id={0} AND company_id={1} AND is_active = 1", hfEmpTask.getEmployeeId(), hfEmpTask.getCompanyId());
        wrapper.orderBy("start_month", true);
        List<HfArchiveBasePeriod> existHfArchiveBasePeriodList = hfArchiveBasePeriodService.selectList(wrapper);

        if (CollectionUtils.isNotEmpty(existHfArchiveBasePeriodList)) {
            hfArchiveBasePeriodList = new ArrayList<>();
            diffHfArchiveBasePeriodList = new ArrayList<>();

            hfEmpTaskPeriodList.stream().forEach(e -> {
                if (StringUtils.isEmpty(e.getStartMonth()) || StringUtils.isEmpty(e.getHfMonth())) {
                    throw new BusinessException("调整任务费用分段中缴费起始年月或汇缴年月为空");
                }
                YearMonth hfMonth = YearMonth.parse(e.getHfMonth(), formatter);
                YearMonth adjustStartMonth = YearMonth.parse(e.getStartMonth(), formatter);

                // 调整任务费用分段中调整段处理
                if (e.getRemitWay() == HfEmpTaskPeriodConstant.REMIT_WAY_ADJUST) {
                    if (!adjustStartMonth.equals(hfMonth)) {
                        throw new BusinessException("调整任务费用分段中调整段的缴费起始年月必须等于汇缴年月");
                    }

                    for (HfArchiveBasePeriod existHfArchiveBasePeriod : existHfArchiveBasePeriodList) {
                        if (StringUtils.isEmpty(existHfArchiveBasePeriod.getEndMonth())) {
                            YearMonth startMonth = YearMonth.parse(existHfArchiveBasePeriod.getStartMonth(), formatter);
                            if (startMonth.isAfter(hfMonth) || startMonth.equals(hfMonth)) {
                                throw new BusinessException("雇员档案费用段中缴费起始年月必须小于汇缴年月");
                            }

                            HfArchiveBasePeriod hfArchiveBasePeriod = new HfArchiveBasePeriod();
                            hfArchiveBasePeriod.setEmpTaskId(hfEmpTask.getEmpTaskId());
                            hfArchiveBasePeriod.setRemitWay(e.getRemitWay());
                            hfArchiveBasePeriod.setArchiveBasePeriodId(existHfArchiveBasePeriod.getArchiveBasePeriodId());
                            hfArchiveBasePeriod.setHfMonth(e.getHfMonth());
                            hfArchiveBasePeriod.setEndMonth(hfMonth.minusMonths(1).format(formatter));
                            hfArchiveBasePeriod.setModifiedBy(hfEmpTask.getModifiedBy());
                            hfArchiveBasePeriod.setModifiedTime(LocalDateTime.now());
                            hfArchiveBasePeriod.setBaseAmount(existHfArchiveBasePeriod.getBaseAmount());
                            hfArchiveBasePeriod.setRatio(existHfArchiveBasePeriod.getRatio());
                            hfArchiveBasePeriod.setRatioCom(existHfArchiveBasePeriod.getRatioCom());
                            hfArchiveBasePeriod.setRatioEmp(existHfArchiveBasePeriod.getRatioEmp());
                            hfArchiveBasePeriod.setAmount(existHfArchiveBasePeriod.getAmount());
                            hfArchiveBasePeriod.setAmountEmp(existHfArchiveBasePeriod.getAmountEmp());
                            hfArchiveBasePeriod.setComAmount(existHfArchiveBasePeriod.getComAmount());
                            hfArchiveBasePeriod.setCompanyId(existHfArchiveBasePeriod.getCompanyId());
                            hfArchiveBasePeriod.setEmployeeId(existHfArchiveBasePeriod.getEmployeeId());
                            hfArchiveBasePeriod.setEmpArchiveId(existHfArchiveBasePeriod.getEmpArchiveId());
                            hfArchiveBasePeriod.setHfType(existHfArchiveBasePeriod.getHfType());
                            hfArchiveBasePeriodList.add(hfArchiveBasePeriod);

                            setHfArchiveBasePeriodList(hfArchiveBasePeriodList, hfEmpTask, e, null);
                            break;
                        }
                    }

                    if (CollectionUtils.isEmpty(hfArchiveBasePeriodList)) {
                        throw new BusinessException("当前雇员的雇员汇缴月份段数据不正确，已封存或转出的雇员不能进行调整");
                    }
                } else { // 调整任务费用分段中补缴段处理（差额补缴）

                    if (StringUtils.isEmpty(e.getEndMonth())) {
                        throw new BusinessException("调整任务费用分段中补缴段的缴费截止年月为空");
                    }
                    YearMonth adjustEndMonth = YearMonth.parse(e.getEndMonth(), formatter);

                    if (!adjustEndMonth.equals(hfMonth.minusMonths(1))) {
                        throw new BusinessException("调整任务费用分段中补缴段的缴费截止年月必须等于汇缴年月的前月");
                    }

                    YearMonth startMonth;
                    YearMonth endMonth = adjustEndMonth;
                    List<HfArchiveBaseAdjust> hfArchiveBaseAdjustList = new ArrayList<>();
                    boolean isEnd = false;

                    for (HfArchiveBasePeriod existHfArchiveBasePeriod : existHfArchiveBasePeriodList) {
                        startMonth = YearMonth.parse(existHfArchiveBasePeriod.getStartMonth(), formatter);

                        if (StringUtils.isNotEmpty(existHfArchiveBasePeriod.getEndMonth())) {
                            endMonth = YearMonth.parse(existHfArchiveBasePeriod.getEndMonth(), formatter);
                        }
                        HfEmpTaskPeriod hfEmpTaskPeriod = new HfEmpTaskPeriod();
                        hfEmpTaskPeriod.setHfMonth(e.getHfMonth());
                        hfEmpTaskPeriod.setBaseAmount(e.getBaseAmount());
                        hfEmpTaskPeriod.setRatio(e.getRatio());
                        hfEmpTaskPeriod.setRatioCom(e.getRatioCom());
                        hfEmpTaskPeriod.setRatioEmp(e.getRatioEmp());
                        hfEmpTaskPeriod.setAmount(e.getAmount());
                        hfEmpTaskPeriod.setRemitWay(e.getRemitWay());
                        hfEmpTaskPeriod.setRepairReason(e.getRepairReason());

                        if (adjustStartMonth.isBefore(startMonth)) {
                            hfEmpTaskPeriod.setStartMonth(existHfArchiveBasePeriod.getStartMonth());
                        } else {
                            hfEmpTaskPeriod.setStartMonth(e.getStartMonth());
                        }

                        if (adjustEndMonth.isAfter(endMonth)) {
                            hfEmpTaskPeriod.setEndMonth(existHfArchiveBasePeriod.getEndMonth());
                            adjustStartMonth = endMonth;
                        } else {
                            hfEmpTaskPeriod.setEndMonth(e.getEndMonth());
                            isEnd = true;
                        }
                        setHfArchiveBaseAdjust(hfArchiveBaseAdjustList, hfEmpTask, hfEmpTaskPeriod, existHfArchiveBasePeriod);
                        setHfArchiveBasePeriodList(diffHfArchiveBasePeriodList, hfEmpTask, hfEmpTaskPeriod, existHfArchiveBasePeriod);

                        if (isEnd) {
                            break;
                        }
                    }

                    if (CollectionUtils.isNotEmpty(hfArchiveBaseAdjustList)) {
                        hfArchiveBaseAdjustService.insertBatch(hfArchiveBaseAdjustList);
                    }
                }
            });

            if (CollectionUtils.isNotEmpty(hfArchiveBasePeriodList)) {
                hfArchiveBasePeriodService.insertOrUpdateBatch(hfArchiveBasePeriodList);
                hfArchiveBasePeriodList.addAll(diffHfArchiveBasePeriodList);
            }
        } else {
            throw new BusinessException("当前雇员的雇员汇缴月份段数据不存在，不能调整");
        }
        return hfArchiveBasePeriodList;
    }

    /**
     * 转出或封存任务时雇员档案费用分段表数据处理
     *
     * @param hfEmpTask
     * @param hfEmpTaskPeriod
     * @return
     */
    private HfArchiveBasePeriod stopEmpBasePeriod(HfEmpTask hfEmpTask, HfEmpTaskPeriod hfEmpTaskPeriod) {
        EntityWrapper<HfArchiveBasePeriod> wrapper = new EntityWrapper<>();
        wrapper.where("employee_id={0} AND company_id={1} AND is_active = 1", hfEmpTask.getEmployeeId(), hfEmpTask.getCompanyId());
        wrapper.orderBy("start_month", false);
        List<HfArchiveBasePeriod> existHfArchiveBasePeriodList = hfArchiveBasePeriodService.selectList(wrapper);

        if (CollectionUtils.isNotEmpty(existHfArchiveBasePeriodList)) {
            HfArchiveBasePeriod lastHfArchiveBasePeriod = existHfArchiveBasePeriodList.get(0);
            if (StringUtils.isNotEmpty(lastHfArchiveBasePeriod.getEndMonth())) {
                throw new BusinessException("当前雇员最后汇缴月份段的缴费截止月已经存在");
            }
            HfArchiveBasePeriod hfArchiveBasePeriod = new HfArchiveBasePeriod();
            hfArchiveBasePeriod.setEmpTaskId(hfEmpTask.getEmpTaskId());
            hfArchiveBasePeriod.setRemitWay(lastHfArchiveBasePeriod.getRemitWay());
            hfArchiveBasePeriod.setArchiveBasePeriodId(lastHfArchiveBasePeriod.getArchiveBasePeriodId());
            hfArchiveBasePeriod.setHfMonth(hfEmpTaskPeriod.getHfMonth());
            hfArchiveBasePeriod.setEndMonth(hfEmpTaskPeriod.getEndMonth());
            hfArchiveBasePeriod.setModifiedBy(hfEmpTask.getModifiedBy());
            hfArchiveBasePeriod.setModifiedTime(LocalDateTime.now());
            hfArchiveBasePeriod.setBaseAmount(lastHfArchiveBasePeriod.getBaseAmount());
            hfArchiveBasePeriod.setRatio(lastHfArchiveBasePeriod.getRatio());
            hfArchiveBasePeriod.setRatioCom(lastHfArchiveBasePeriod.getRatioCom());
            hfArchiveBasePeriod.setRatioEmp(lastHfArchiveBasePeriod.getRatioEmp());
            hfArchiveBasePeriod.setAmount(lastHfArchiveBasePeriod.getAmount());
            hfArchiveBasePeriod.setAmountEmp(lastHfArchiveBasePeriod.getAmountEmp());
            hfArchiveBasePeriod.setComAmount(lastHfArchiveBasePeriod.getComAmount());
            hfArchiveBasePeriod.setCompanyId(lastHfArchiveBasePeriod.getCompanyId());
            hfArchiveBasePeriod.setEmployeeId(lastHfArchiveBasePeriod.getEmployeeId());
            hfArchiveBasePeriod.setEmpArchiveId(lastHfArchiveBasePeriod.getEmpArchiveId());
            hfArchiveBasePeriod.setHfType(lastHfArchiveBasePeriod.getHfType());
            hfArchiveBasePeriodService.updateById(hfArchiveBasePeriod);

            return hfArchiveBasePeriod;
        } else {
            throw new BusinessException("当前雇员的汇缴月份段数据不存在");
        }
    }

    /**
     * 创建雇员月度汇缴明细库数据
     *
     * @param hfEmpTask 任务单表数据
     * @param hfArchiveBasePeriodList 雇员档案费用分段表数据列表
     */
    private void createHfMonthCharge(HfEmpTask hfEmpTask, List<HfArchiveBasePeriod> hfArchiveBasePeriodList) {
        int paymentType = 0;
        switch (hfEmpTask.getTaskCategory()) {
            case HfEmpTaskConstant.TASK_CATEGORY_IN_ADD:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_TRANS_IN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_MULTI_TRANS_IN:
                paymentType = HfMonthChargeConstant.PAYMENT_TYPE_NEW;
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_IN_OPEN:
                paymentType = HfMonthChargeConstant.PAYMENT_TYPE_OPEN;
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_REPAIR:
                paymentType = HfMonthChargeConstant.PAYMENT_TYPE_REPAIR;
                break;
//            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_OPEN:
//                paymentType = HfMonthChargeConstant.PAYMENT_TYPE_ADJUST_OPEN;
//                break;
            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST:
                paymentType = HfMonthChargeConstant.PAYMENT_TYPE_ADJUST_OPEN;
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_MULTI_TRANS_OUT:
                paymentType = HfMonthChargeConstant.PAYMENT_TYPE_CLOSE;
                break;
            default:
                break;
        }

            // 根据缴费开始年月至缴费截止年月的区间生成相应的雇员月度汇缴明细记录；
            // 而新进、转入、调整启封、启封、集体转入时，任务单没有缴费截止年月，那么仅根据缴费开始年月生成一条相应的雇员月度汇缴明细记录；
            for (HfArchiveBasePeriod e : hfArchiveBasePeriodList) {
                String startMonth = e.getStartMonth();
                String endMonth = e.getEndMonth();
                if (e.getRemitWay() == HfEmpTaskPeriodConstant.REMIT_WAY_ADJUST) {
                    if (StringUtils.isNotEmpty(endMonth)) {
                        paymentType = HfMonthChargeConstant.PAYMENT_TYPE_ADJUST_CLOSE;
//                        addCloseMonthRecharge(hfEmpTask, e.getHfMonth(), HfMonthChargeConstant.PAYMENT_TYPE_ADJUST_CLOSE);
//                        continue;
                    } else {
                        paymentType = HfMonthChargeConstant.PAYMENT_TYPE_ADJUST_OPEN;
                    }
                }
                YearMonth startMonthDate;
                YearMonth endMonthDate;

                if (paymentType != HfMonthChargeConstant.PAYMENT_TYPE_ADJUST_CLOSE
                    && paymentType != HfMonthChargeConstant.PAYMENT_TYPE_CLOSE) {
                    if (StringUtils.isEmpty(startMonth)) {
                        throw new BusinessException("雇员档案费用分段表中缴费起始月为空");
                    }
                    startMonthDate = YearMonth.parse(startMonth, formatter);
                    endMonthDate = startMonthDate;

                    if (StringUtils.isNotEmpty(endMonth)) {
                        endMonthDate = YearMonth.parse(endMonth, formatter);
                    }
                } else {
                    if (StringUtils.isEmpty(endMonth)) {
                        throw new BusinessException("雇员档案费用分段表中缴费截止月为空");
                    }
                    endMonthDate = YearMonth.parse(endMonth, formatter);
                    startMonthDate = endMonthDate;
                }

                long months = startMonthDate.until(endMonthDate, ChronoUnit.MONTHS);
                List<HfMonthCharge> hfMonthChargeList = new ArrayList<>();
                BigDecimal comAmount = e.getComAmount();
                BigDecimal empAmount = e.getAmountEmp();
                BigDecimal amount = e.getAmount();

                if (e.getRemitWay() == HfEmpTaskPeriodConstant.REMIT_WAY_REPAIR) {
                    paymentType = HfMonthChargeConstant.PAYMENT_TYPE_REPAIR;
                }

                if (e.getDiffRepair() != null && e.getDiffRepair()) {
                    paymentType = HfMonthChargeConstant.PAYMENT_TYPE_DIFF_REPAIR;

                    // 如果是补缴任务单，雇员月度汇缴明细库差额数据可能会被覆盖（某些年月被多次补缴）
                    HfMonthChargeBo hfMonthChargeBo = new HfMonthChargeBo();
                    hfMonthChargeBo.setInactive(true);
                    hfMonthChargeBo.setEmpArchiveId(e.getEmpArchiveId());
                    hfMonthChargeBo.setHfType(e.getHfType());
                    hfMonthChargeBo.setHfMonth(e.getHfMonth()); // 按汇缴月份来覆盖
                    hfMonthChargeBo.setSsMonthBelongStart(e.getStartMonth());
                    hfMonthChargeBo.setSsMonthBelongEnd(e.getEndMonth());
//                    hfMonthChargeBo.setPaymentTypes(StringUtils.join(new Integer[] {
//                        HfMonthChargeConstant.PAYMENT_TYPE_REPAIR,
//                        HfMonthChargeConstant.PAYMENT_TYPE_DIFF_REPAIR
//                    }, ','));
                    hfMonthChargeBo.setPaymentTypes(String.valueOf(HfMonthChargeConstant.PAYMENT_TYPE_DIFF_REPAIR));
                    hfMonthChargeBo.setModifiedBy(hfEmpTask.getModifiedBy());
                    hfMonthChargeService.updateHfMonthCharge(hfMonthChargeBo);

                    HfMonthChargeDiffBo hfMonthChargeDiffBo = hfMonthChargeService.getHfMonthChargeDiffSum(hfMonthChargeBo);
                    if (hfMonthChargeDiffBo != null) {
                        // 往期汇缴月份保留，新增数据需计算与原有雇员所属公积金月份相同数据的差额
                        amount = amount.subtract(hfMonthChargeDiffBo.getAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        comAmount = comAmount.subtract(hfMonthChargeDiffBo.getComAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        empAmount = empAmount.subtract(hfMonthChargeDiffBo.getEmpAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                }

                // 根据雇员费用段期间，每月生成一条雇员月度汇缴明细记录
                for (long i = 0; i <= months; i++) {
                    HfMonthCharge hfMonthCharge = new HfMonthCharge();
                    hfMonthCharge.setEmpArchiveId(e.getEmpArchiveId());
                    hfMonthCharge.setEmpTaskId(hfEmpTask.getEmpTaskId());
                    hfMonthCharge.setHfMonth(e.getHfMonth());
                    hfMonthCharge.setSsMonthBelong(startMonthDate.plusMonths(i).format(formatter));
                    hfMonthCharge.setCompanyId(hfEmpTask.getCompanyId());
                    hfMonthCharge.setEmployeeId(hfEmpTask.getEmployeeId());
                    hfMonthCharge.setHfType(e.getHfType());
                    hfMonthCharge.setAmount(amount);
                    hfMonthCharge.setComAmount(comAmount);
                    hfMonthCharge.setEmpAmount(empAmount);
                    hfMonthCharge.setBase(e.getBaseAmount());
                    hfMonthCharge.setRatio(e.getRatio());
                    hfMonthCharge.setRatioCom(e.getRatioCom());
                    hfMonthCharge.setRatioEmp(e.getRatioEmp());
                    hfMonthCharge.setPaymentType(paymentType);
                    hfMonthCharge.setRepairReason(e.getRepairReason());
                    hfMonthCharge.setCreatedBy(hfEmpTask.getModifiedBy());
                    hfMonthCharge.setModifiedBy(hfEmpTask.getModifiedBy());
                    hfMonthChargeList.add(hfMonthCharge);
                }
                hfMonthChargeService.insertBatch(hfMonthChargeList);
            }
//        } else {
//            // 正常开票时，生成汇缴月为下月的标准雇员月度汇缴明细记录；
//            // 那么转出、封存、调整封存、集体转出时，将已生成的下月标准雇员月度汇缴明细记录，费用种类用“标准”更新为相应种类；
//            HfArchiveBasePeriod hfArchiveBasePeriod = hfArchiveBasePeriodList.get(0);
//            String hfMonth = hfArchiveBasePeriod.getHfMonth();
//            addCloseMonthRecharge(hfEmpTask, hfMonth, paymentType);
//            Map<String, Object> condition = new HashMap<>();
//            condition.put("is_active", 1);
//            condition.put("hf_month", hfMonth);
//            condition.put("emp_archive_id", hfEmpTask.getEmpArchiveId());
//            condition.put("hf_type", hfEmpTask.getHfType());
//            condition.put("payment_type", HfMonthChargeConstant.PAYMENT_TYPE_NORMAL);
//            List<HfMonthCharge> hfMonthChargeList = hfMonthChargeService.selectByMap(condition);
//            if (CollectionUtils.isNotEmpty(hfMonthChargeList)) {
//                if (hfMonthChargeList.size() > 1) {
//                    throw new BusinessException("开票时自动生成的标准雇员月度汇缴明细记录重复");
//                }
//

//                HfMonthCharge hfMonthCharge = new HfMonthCharge();
//                hfMonthCharge.setMonthChargeId(hfMonthChargeList.get(0).getMonthChargeId());
//                hfMonthCharge.setEmpTaskId(hfEmpTask.getEmpTaskId());
//                hfMonthCharge.setPaymentType(paymentType[0]);
//                hfMonthCharge.setModifiedBy(hfEmpTask.getModifiedBy());
//                hfMonthChargeService.updateById(hfMonthCharge);
//        }
    }

    private void addCloseMonthRecharge(HfEmpTask hfEmpTask, String hfMonth, int paymentType) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("is_active", 1);
        condition.put("hf_month", hfMonth);
        condition.put("emp_archive_id", hfEmpTask.getEmpArchiveId());
        condition.put("hf_type", hfEmpTask.getHfType());
        condition.put("payment_type", HfMonthChargeConstant.PAYMENT_TYPE_NORMAL);
        List<HfMonthCharge> hfMonthChargeList = hfMonthChargeService.selectByMap(condition);
        if (CollectionUtils.isNotEmpty(hfMonthChargeList)) {
            if (hfMonthChargeList.size() > 1) {
                throw new BusinessException("开票时自动生成的标准雇员月度汇缴明细记录重复");
            }
        } else {
            // 新进或转入当月就转出或封存时？NG
            throw new BusinessException("开票时自动生成的标准雇员月度汇缴明细记录不存在");
        }
        HfMonthCharge hfMonthCharge = hfMonthChargeList.get(0);
        hfMonthCharge.setMonthChargeId(null);
        hfMonthCharge.setPaymentType(paymentType);
        hfMonthCharge.setCreatedBy(hfEmpTask.getModifiedBy());
        hfMonthCharge.setModifiedBy(hfEmpTask.getModifiedBy());
        hfMonthChargeService.insert(hfMonthCharge);
    }

    /**
     * 雇员任务表数据处理
     *
     * @param params 画面输入的参数
     * @param inputHfEmpTask 画面输入的雇员任务数据
     */
    private JsonResult setEmpTask(JSONObject params, HfEmpTask inputHfEmpTask) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("company_id", params.getString("companyId"));
        condition.put("employee_id", params.getString("employeeId"));
        condition.put("is_active", 1);
        boolean isNothing = false;
        Long empArchiveId = null;
        List<HfEmpArchive> hfEmpArchiveList = hfEmpArchiveService.selectByMap(condition);
        if (CollectionUtils.isNotEmpty(hfEmpArchiveList)) {
            if (hfEmpArchiveList.size() > 1) {
                return JsonResultKit.ofError("该雇员的雇员档案数据不正确");
            }
            empArchiveId = hfEmpArchiveList.get(0).getEmpArchiveId();

            hfEmpArchiveList = hfEmpArchiveList.stream()
                .filter(e -> e.getArchiveStatus() == null || e.getArchiveStatus() != HfEmpArchiveConstant.ARCHIVE_STATUS_CLOSED)
                .collect(Collectors.toList());
        } else {
            isNothing = true;
        }

        switch (params.getInteger("taskCategory")) {
            case HfEmpTaskConstant.TASK_CATEGORY_IN_ADD:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_TRANS_IN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_OPEN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_MULTI_TRANS_IN:
                if (CollectionUtils.isNotEmpty(hfEmpArchiveList)) {
                    return JsonResultKit.ofError("雇员档案已存在，且非封存状态");
                }
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_REPAIR:
            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST:
                if (isNothing) {
                    return JsonResultKit.ofError("雇员档案不存在");
                }
                break;
//            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_MULTI_TRANS_OUT:
                if (CollectionUtils.isEmpty(hfEmpArchiveList)) {
                    return JsonResultKit.ofError("非封存状态雇员档案不存在");
                }
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_TRANS_TASK:
            case HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK:
            case HfEmpTaskConstant.TASK_CATEGORY_TURN_OVER:
                // TODO 这3种？这里不处理？
                break;
            default:
                break;
        }

        inputHfEmpTask.setTaskStatus(HfEmpTaskConstant.TASK_STATUS_HANDLED);
//        inputHfEmpTask.setHandleStatus(2); //
        inputHfEmpTask.setHandleDate(YearMonth.now().format(formatter));
        inputHfEmpTask.setHandleUserId("test"); // TODO
        inputHfEmpTask.setHandleUserName("test"); // TODO

        return JsonResultKit.of(empArchiveId);
    }

    /**
     * 雇员档案表数据处理
     *
     * @param params 画面传入参数
     * @param inputHfEmpTask 任务单表数据
     */
    private Long handleEmpArchive(JSONObject params, Long empArchiveId, HfEmpTask inputHfEmpTask) {
        HfEmpArchive hfEmpArchive = new HfEmpArchive();
        System.out.println("inputHfEmpTask.getEmpArchiveId() = " + inputHfEmpTask.getEmpArchiveId()); // TODO Log
        hfEmpArchive.setStartMonth(inputHfEmpTask.getStartMonth());
//        hfEmpArchive.setEndMonth(inputHfEmpTask.getEndMonth());
        hfEmpArchive.setOperationRemind(inputHfEmpTask.getOperationRemind());
        hfEmpArchive.setOperationRemindDate(inputHfEmpTask.getOperationRemindDate());

        // 办理状态变更
        setEmpArchiveStatus(hfEmpArchive, inputHfEmpTask.getTaskCategory());

        hfEmpArchive.setModifiedBy("test"); // TODO
        boolean isNew = false;

        if (empArchiveId == null) {
            hfEmpArchive.setCompanyId(params.getString("companyId"));
            hfEmpArchive.setEmployeeId(params.getString("employeeId"));
            hfEmpArchive.setComAccountId(params.getInteger("comAccountId"));
            hfEmpArchive.setHfType(inputHfEmpTask.getHfType());
            if (hfEmpArchive.getHfType() == 1) {
                hfEmpArchive.setComAccountClassId(params.getLong("basicComAccountClassId"));
            } else {
                hfEmpArchive.setComAccountClassId(params.getLong("addedComAccountClassId"));
                hfEmpArchive.setBelongEmpArchiveId(params.getLong("belongEmpArchiveId"));
            }

            hfEmpArchive.setHfEmpAccount(inputHfEmpTask.getHfEmpAccount());
            hfEmpArchive.setCreatedBy("test"); // TODO
            isNew = true;
        } else {
            hfEmpArchive.setEmpArchiveId(empArchiveId);
            hfEmpArchive.setModifiedTime(LocalDateTime.now());
        }
        hfEmpArchiveService.insertOrUpdate(hfEmpArchive);

        if (isNew) {
            return hfEmpArchive.getEmpArchiveId();
        }
        return null;
    }

    /**
     * 雇员公积金历史月份调整差异表数据处理
     *
     * @param hfArchiveBaseAdjustList 雇员公积金历史月份调整差异数据列表
     * @param hfEmpTask 任务单表数据
     * @param hfEmpTaskPeriod 雇员档案费用分段表数据
     */
    private void setHfArchiveBaseAdjust(List<HfArchiveBaseAdjust> hfArchiveBaseAdjustList,
                                        HfEmpTask hfEmpTask,
                                        HfEmpTaskPeriod hfEmpTaskPeriod,
                                        HfArchiveBasePeriod hfArchiveBasePeriod) {
        String startMonth = hfEmpTaskPeriod.getStartMonth();
        String endMonth = hfEmpTaskPeriod.getEndMonth();
        HfArchiveBaseAdjust hfArchiveBaseAdjust = new HfArchiveBaseAdjust();
        hfArchiveBaseAdjust.setEmpTaskId(hfEmpTask.getEmpTaskId());
        hfArchiveBaseAdjust.setHfMonth(hfEmpTaskPeriod.getHfMonth());
        hfArchiveBaseAdjust.setCompanyId(hfEmpTask.getCompanyId());
        hfArchiveBaseAdjust.setEmployeeId(hfEmpTask.getEmployeeId());
        hfArchiveBaseAdjust.setEmpArchiveId(hfEmpTask.getEmpArchiveId());
        hfArchiveBaseAdjust.setStartMonth(startMonth);
        hfArchiveBaseAdjust.setEndMonth(endMonth);
        hfArchiveBaseAdjust.setNewBaseAmount(hfEmpTaskPeriod.getBaseAmount());
        BigDecimal ratio = hfEmpTaskPeriod.getRatioCom().add(hfEmpTaskPeriod.getRatioEmp()).setScale(2, BigDecimal.ROUND_HALF_UP);
        hfArchiveBaseAdjust.setRatio(ratio);
        hfArchiveBaseAdjust.setRatioCom(hfEmpTaskPeriod.getRatioCom());
        hfArchiveBaseAdjust.setRatioEmp(hfEmpTaskPeriod.getRatioEmp());

        BigDecimal existRatio = hfArchiveBasePeriod.getRatioCom().add(hfArchiveBasePeriod.getRatioEmp()).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal months = BigDecimal.valueOf(YearMonth.parse(startMonth, formatter).until(YearMonth.parse(endMonth, formatter), ChronoUnit.MONTHS) + 1);
        BigDecimal singleComDiffAmount = hfEmpTaskPeriod.getAmount().multiply(hfEmpTaskPeriod.getRatioCom()).divide(ratio, 2, BigDecimal.ROUND_HALF_UP)
            .subtract(hfArchiveBasePeriod.getAmount().multiply(hfArchiveBasePeriod.getRatioCom()).divide(existRatio, 2, BigDecimal.ROUND_HALF_UP));
        BigDecimal singleEmpDiffAmount = hfEmpTaskPeriod.getAmount().multiply(hfEmpTaskPeriod.getRatioEmp()).divide(ratio, 2, BigDecimal.ROUND_HALF_UP)
            .subtract(hfArchiveBasePeriod.getAmount().multiply(hfArchiveBasePeriod.getRatioEmp()).divide(existRatio, 2, BigDecimal.ROUND_HALF_UP));

        BigDecimal comDiffAmount = singleComDiffAmount.multiply(months).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal empDiffAmount = singleEmpDiffAmount.multiply(months).setScale(2, BigDecimal.ROUND_HALF_UP);
        hfArchiveBaseAdjust.setComDiffSumAmount(comDiffAmount);
        hfArchiveBaseAdjust.setEmpDiffSumAmount(empDiffAmount);
        hfArchiveBaseAdjust.setComempSumDiffAmount(comDiffAmount.add(empDiffAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
        hfArchiveBaseAdjust.setCreatedBy(hfEmpTask.getModifiedBy());
        hfArchiveBaseAdjust.setModifiedBy(hfEmpTask.getModifiedBy());
        hfArchiveBaseAdjustList.add(hfArchiveBaseAdjust);

        // 仅是为了提高执行效率，在后面生成雇员月度汇缴明细库时无需重新计算，借用该两个字段（差额补缴时不更新档案费用分段表）
        hfArchiveBasePeriod.setComAmount(singleComDiffAmount);
        hfArchiveBasePeriod.setAmountEmp(singleEmpDiffAmount);
    }

    /**
     * 访问客服中心的完成任务接口
     *
     * @param taskId 任务单ID
     * @param assignee 办理人
     * @return 接口返回结果
     * @throws Exception 接口throws出的Exception
     */
    public Result apiCompleteTask(String taskId, String assignee) throws Exception {
        TaskSheetRequestDTO taskSheetRequestDTO = new TaskSheetRequestDTO();
        taskSheetRequestDTO.setTaskId(taskId);
        taskSheetRequestDTO.setAssignee(assignee);
        return commonApiUtils.completeTask(taskSheetRequestDTO); // TODO return code?
    }

    /**
     * 访问雇员任务单实缴金额回调接口（支持中心调用客服中心）
     *
     * @param companyId 客户编号
     * @param empAgreementId 业务接口ID
     * @param hfEmpTask 任务单
     * @param hfArchiveBasePeriodList 雇员费用段列表
     * @param isReject 是否批退
     * @return 接口返回结果
     * @throws Exception 接口throws出的Exception
     */
    public int apiUpdateConfirmDate(String companyId,
                                       Long empAgreementId,
                                       HfEmpTask hfEmpTask,
                                       List<HfArchiveBasePeriod> hfArchiveBasePeriodList,
                                       boolean isReject) throws Exception {
        if (CollectionUtils.isNotEmpty(hfArchiveBasePeriodList)) {
            List<AfEmpSocialUpdateDateDTO> afEmpSocialUpdateDateDTOList = new ArrayList<>(hfArchiveBasePeriodList.size());
            DateKit.setDatePattern("yyyyMMdd");
            BigDecimal companyConfirmAmount = BigDecimal.ZERO;
            BigDecimal personalConfirmAmount = BigDecimal.ZERO;
            String startMonth = hfEmpTask.getStartMonth();
            String endMonth = hfEmpTask.getEndMonth();

            for (HfArchiveBasePeriod hfArchiveBasePeriod : hfArchiveBasePeriodList) {
                if (!isReject && companyConfirmAmount == BigDecimal.ZERO) {
                    companyConfirmAmount = hfArchiveBasePeriod.getComAmount();
                    personalConfirmAmount = hfArchiveBasePeriod.getAmountEmp();
                }
            }

            AfEmpSocialUpdateDateDTO afEmpSocialUpdateDateDTO = new AfEmpSocialUpdateDateDTO();
            if (StringUtils.isNotEmpty(startMonth)) {
                afEmpSocialUpdateDateDTO.setStartConfirmDate(DateKit.toDate(startMonth + "01"));
            }
            if (StringUtils.isNotEmpty(endMonth)) {
                afEmpSocialUpdateDateDTO.setEndConfirmDate(DateKit.toDate(endMonth + "01"));
            }
            afEmpSocialUpdateDateDTO.setCompanyId(companyId);
            afEmpSocialUpdateDateDTO.setEmpAgreementId(empAgreementId);
            afEmpSocialUpdateDateDTO.setItemCode(DictUtil.DICT_ID_FUND);
            afEmpSocialUpdateDateDTO.setCompanyConfirmAmount(companyConfirmAmount);
            afEmpSocialUpdateDateDTO.setPersonalConfirmAmount(personalConfirmAmount);
            afEmpSocialUpdateDateDTOList.add(afEmpSocialUpdateDateDTO);
            return commonApiUtils.updateConfirmDate(afEmpSocialUpdateDateDTOList); // TODO return code?
        }
        return 0;
    }

    /**
     * 根据任务单类型及雇员档案当前原始状态来设置雇员档案中的任务单状态及原始状态
     *
     * @param hfEmpArchive 雇员档案当前原始状态
     * @param taskCategory 任务单类型
     */
    private void setEmpArchiveStatus(HfEmpArchive hfEmpArchive, Integer taskCategory) {
        Integer origStatus = hfEmpArchive.getArchiveStatus();

        switch (taskCategory) {
            case HfEmpTaskConstant.TASK_CATEGORY_IN_ADD:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_TRANS_IN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_OPEN:
            case HfEmpTaskConstant.TASK_CATEGORY_IN_MULTI_TRANS_IN:
                hfEmpArchive.setArchiveTaskStatus(HfEmpArchiveConstant.ARCHIVE_TASK_STATUS_HANDLED);
                hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_HANDLED);
                break;
//            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_CLOSE:
//                hfEmpArchive.setArchiveTaskStatus(HfEmpArchiveConstant.ARCHIVE_TASK_STATUS_CLOSED);
//                hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_CLOSED);
//                break;
//            case HfEmpTaskConstant.TASK_CATEGORY_ADJUST_OPEN:
//                hfEmpArchive.setArchiveTaskStatus(HfEmpArchiveConstant.ARCHIVE_TASK_STATUS_HANDLED);
//                hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_HANDLED);
//                break;
            case HfEmpTaskConstant.TASK_CATEGORY_REPAIR:
                if (origStatus == null || origStatus == HfEmpArchiveConstant.ARCHIVE_STATUS_CLOSED) {
                    hfEmpArchive.setArchiveTaskStatus(HfEmpArchiveConstant.ARCHIVE_TASK_STATUS_CLOSED);
                    hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_CLOSED);
                } else if (origStatus == HfEmpArchiveConstant.ARCHIVE_STATUS_HANDLED || origStatus == HfEmpArchiveConstant.ARCHIVE_STATUS_COMPLETED) {
                    hfEmpArchive.setArchiveTaskStatus(HfEmpArchiveConstant.ARCHIVE_TASK_STATUS_HANDLED);
                    hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_HANDLED);
                }
                break;
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_TRANS_OUT:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_CLOSE:
            case HfEmpTaskConstant.TASK_CATEGORY_OUT_MULTI_TRANS_OUT:
                hfEmpArchive.setArchiveTaskStatus(HfEmpArchiveConstant.ARCHIVE_TASK_STATUS_CLOSED);
                hfEmpArchive.setArchiveStatus(HfEmpArchiveConstant.ARCHIVE_STATUS_CLOSED);
                break;
            default:
                break;
        }
    }

    @Override
    public int createTransEmpTask(@RequestBody  HfEmpTaskCreateTransBo hfEmpTaskCreateTransBo) {
        return baseMapper.createTransEmpTask(hfEmpTaskCreateTransBo);
    }
}
