package com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpPreInputBO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskBatchRejectBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskExportBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpTaskRejectExportBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.transfer.EmpTaskTransferBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.transfer.EmpTransferTemplateImpXsl;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.transfer.ImportFeedbackDateBO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.*;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpTaskConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.EmpEmployee;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpPreInput;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpTask;
import com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.util.FeedbackDateVerifyHandler;
import com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.util.HfEmpPreInputVerifyHandler;
import com.ciicsh.gto.afsupportcenter.util.CalculateSocialUtils;
import com.ciicsh.gto.afsupportcenter.util.ExcelUtil;
import com.ciicsh.gto.afsupportcenter.util.constant.DictUtil;
import com.ciicsh.gto.afsupportcenter.util.exception.BusinessException;
import com.ciicsh.gto.afsupportcenter.util.interceptor.authenticate.UserContext;
import com.ciicsh.gto.afsupportcenter.util.logService.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogMessage;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.ExportResponseUtil;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/fundcommandservice/hfEmpTask")
public class HfEmpTaskController extends BasicController<HfEmpTaskService> {
    @Autowired
    HfEmpTaskHandleService hfEmpTaskHandleService;
    @Autowired
    EmpEmployeeService empEmployeeService;
    @Autowired
    HfEmpPreInputService hfEmpPreInputService;
    @Autowired
    HfFileImportService hfFileImportService;
    @Autowired
    HFImportEmpPreInputService hfImportEmpPreInputService;
    @Autowired
    LogApiUtil logApiUtil;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    /**
     * 雇员公积金任务查询
     * @param pageInfo
     * @return
     */
    @RequestMapping("/hfEmpTaskQuery")
    public JsonResult<PageRows> hfEmpTaskQuery(@RequestBody PageInfo pageInfo) {
        return JsonResultKit.of(business.queryHfEmpTaskInPage(pageInfo, UserContext.getUserId(), StringUtils.join(
            new Integer[] {
                HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK
//                HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK
            }, ',')));
    }

    /**
     * 雇员公积金任务导出
     * @param
     * @return
     */
    @RequestMapping("/hfEmpTaskExport")
    public void hfEmpTaskExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
        pageInfo.setPageSize(10000);
        pageInfo.setPageNum(1);
        PageRows<HfEmpTaskExportBo> result = business.queryHfEmpTaskInPage(pageInfo, UserContext.getUserId(), StringUtils.join(
            new Integer[] {
                HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK
//                HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK
            }, ','));
        long total = result.getTotal();
        ExportParams exportParams = new ExportParams();
        exportParams.setType(ExcelType.XSSF);
        exportParams.setSheetName("雇员公积金任务信息");
        Workbook workbook;

        if (total <= pageInfo.getPageSize()) {
            workbook = ExcelExportUtil.exportExcel(exportParams, HfEmpTaskExportBo.class, result.getRows());
        } else {
            workbook = ExcelExportUtil.exportBigExcel(exportParams, HfEmpTaskExportBo.class, result.getRows());
            int pageNum = (int) Math.ceil(total * 1.0 / pageInfo.getPageSize());
            for(int i = 2; i <= pageNum; i++) {
                pageInfo.setPageNum(i);
                result = business.queryHfEmpTaskInPage(pageInfo, StringUtils.join(
                    new Integer[] {
                        HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK
//                        HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK
                    }, ','));
                workbook = ExcelExportUtil.exportBigExcel(exportParams, HfEmpTaskExportBo.class, result.getRows());
            }
            ExcelExportUtil.closeExportBigExcel();
        }
        String fileName = "雇员公积金任务信息.xlsx";

        response.reset();
        response.setCharacterEncoding("UTF-8");
//            response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition",
//            "attachment;filename=" + fileName);
        ExportResponseUtil.encodeExportFileName(response, fileName);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 批退雇员公积金任务查询
     * @param pageInfo
     * @return
     */
    @RequestMapping("/hfEmpTaskRejectQuery")
    public JsonResult<PageRows> hfEmpTaskRejectQuery(@RequestBody PageInfo pageInfo) {
        return JsonResultKit.of(business.queryHfEmpTaskRejectInPage(pageInfo, UserContext.getUserId(), StringUtils.join(
            new Integer[] {
                HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK
//                HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK
            }, ',')));
    }

    /**
     * 批退雇员公积金任务导出
     * @param
     * @return
     */
    @RequestMapping("/hfEmpTaskRejectExport")
    public void hfEmpTaskRejectExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
        pageInfo.setPageSize(10000);
        pageInfo.setPageNum(1);
        PageRows<HfEmpTaskRejectExportBo> result = business.queryHfEmpTaskRejectInPage(pageInfo, UserContext.getUserId(), StringUtils.join(
            new Integer[] {
                HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK
//                HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK
            }, ','));
        long total = result.getTotal();
        ExportParams exportParams = new ExportParams();
        exportParams.setType(ExcelType.XSSF);
        exportParams.setSheetName("雇员公积金任务信息");
        Workbook workbook;

        if (total <= pageInfo.getPageSize()) {
            workbook = ExcelExportUtil.exportExcel(exportParams, HfEmpTaskRejectExportBo.class, result.getRows());
        } else {
            workbook = ExcelExportUtil.exportBigExcel(exportParams, HfEmpTaskRejectExportBo.class, result.getRows());
            int pageNum = (int) Math.ceil(total * 1.0 / pageInfo.getPageSize());
            for(int i = 2; i <= pageNum; i++) {
                pageInfo.setPageNum(i);
                result = business.queryHfEmpTaskRejectInPage(pageInfo, UserContext.getUserId(), StringUtils.join(
                    new Integer[] {
                        HfEmpTaskConstant.TASK_CATEGORY_TRANSFER_TASK
//                        HfEmpTaskConstant.TASK_CATEGORY_SPEC_TASK
                    }, ','));
                workbook = ExcelExportUtil.exportBigExcel(exportParams, HfEmpTaskRejectExportBo.class, result.getRows());
            }
            ExcelExportUtil.closeExportBigExcel();
        }
        String fileName = "批退雇员公积金任务信息.xlsx";

        response.reset();
        response.setCharacterEncoding("UTF-8");
//            response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition",
//            "attachment;filename=" + fileName);
        ExportResponseUtil.encodeExportFileName(response, fileName);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 雇员公积金任务批退
     * @param hfEmpTaskBatchRejectBo
     * @return
     */
    @RequestMapping("/hfEmpTaskBatchReject")
    public JsonResult hfEmpTaskBatchReject(@RequestBody HfEmpTaskBatchRejectBo hfEmpTaskBatchRejectBo) {
        Long[] selectedData = hfEmpTaskBatchRejectBo.getSelectedData();
        if (!ArrayUtils.isEmpty(selectedData)) {
            List<HfEmpTask> list = new ArrayList<>();
            for (Long empTaskId : selectedData) {
                HfEmpTask hfEmpTask = new HfEmpTask();
                hfEmpTask.setEmpTaskId(empTaskId);
                hfEmpTask.setTaskStatus(HfEmpTaskConstant.TASK_STATUS_REJECTED);
                hfEmpTask.setRejectionRemark(hfEmpTaskBatchRejectBo.getRejectionRemark());
                hfEmpTask.setModifiedTime(LocalDateTime.now());
                hfEmpTask.setModifiedBy(UserContext.getUserId());
                hfEmpTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
                list.add(hfEmpTask);
            }
            return hfEmpTaskHandleService.handleReject(hfEmpTaskBatchRejectBo, false);
//            if (!business.updateBatchById(list)) {
//                return JsonResultKit.ofError("数据库批量更新失败");
//            }
        }

        return JsonResultKit.of();
    }

    @RequestMapping("/newEmpTaskTxtExport")
    public void newEmpTaskTxtExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
//        try {
            Collection<Object> objects = pageInfo.getParams().values();

//            Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
            StringBuilder stringBuilder = new StringBuilder();
            String title = "序号|1|||姓名|单边比例|单边比例|||缴费金额|1010|身份证号码|出生日期|性别|单边金额|单边金额||缴费基数|||||||||-1|";
            String template = "%1$d|1|||{%2$s}.{employeeName}|%3$s|%4$s|||%5$s|1010|{%2$s}.{idNum}|{%2$s}.{birthday}|{%2$s}.{gender}|%6$s|%7$s||%8$s|||||||||-1|";


            if (objects != null) {
                List<String> outputList = new ArrayList<>(objects.size());
                Set<String> employeeIdSet = new HashSet<>();
                Wrapper<HfEmpTask> wrapper = new EntityWrapper<>();
                wrapper.in("emp_task_id", StringUtils.join(objects, ','));
                wrapper.in("task_category", "1,9");
                wrapper.eq("is_active", 1);
                wrapper.orderBy("created_time", false);

                List<HfEmpTask> list = business.selectList(wrapper);
                Map<Long, int[]> roundTypesMap = new HashMap<>();

                if (CollectionUtils.isNotEmpty(list)) {
                    for (int i = 0; i < list.size(); i++) {
                        HfEmpTask hfEmpTask = list.get(i);
                        employeeIdSet.add(hfEmpTask.getEmployeeId());
                        String policyId = hfEmpTask.getPolicyDetailId();
                        Integer welfareUnit = hfEmpTask.getWelfareUnit();
                        String effectiveMonth = hfEmpTask.getStartMonth();
                        if (StringUtils.isEmpty(effectiveMonth)) {
                            effectiveMonth = hfEmpTask.getEndMonth();
                        }
                        String hfTypeDicItemCode = DictUtil.DICT_ITEM_ID_FUND_BASIC;
                        if (hfEmpTask.getHfType() == HfEmpTaskConstant.HF_TYPE_ADDED) {
                            hfTypeDicItemCode = DictUtil.DICT_ITEM_ID_FUND_ADDED;
                        }
                        int[] roundTypes = null;
                        if (StringUtils.isNotEmpty(policyId)) {
                            roundTypes = hfEmpTaskHandleService.getRoundTypeProxy(policyId, welfareUnit, effectiveMonth, hfTypeDicItemCode);
                            if (roundTypes != null) {
                                if (roundTypes.length != 2) {
                                    throw new BusinessException("内控中心取得进位方式不正确");
                                } else {
                                    if (roundTypes[0] < 1 || roundTypes[0] > 10) {
                                        roundTypes[0] = 1;
                                    }
                                    if (roundTypes[1] < 1 || roundTypes[1] > 10) {
                                        roundTypes[1] = 1;
                                    }
                                }
                            }
                        }
                        if (roundTypes == null) {
                            roundTypes = new int[]{1, 1};
                        }
                        roundTypesMap.put(hfEmpTask.getEmpTaskId(), roundTypes);
                        BigDecimal comRatio = hfEmpTask.getRatioCom();
                        BigDecimal empRatio = hfEmpTask.getRatioEmp();
                        BigDecimal amount = CalculateSocialUtils.calculateByRoundType(hfEmpTask.getAmount(),
                            CalculateSocialUtils.getRoundTypeInWeight(roundTypesMap.get(hfEmpTask.getEmpTaskId())[0], roundTypesMap.get(hfEmpTask.getEmpTaskId())[1]));
                        BigDecimal comAmount = amount.multiply(comRatio).divide(comRatio.add(empRatio), 3, BigDecimal.ROUND_HALF_UP);
                        BigDecimal empAmount = amount.multiply(comRatio).divide(comRatio.add(empRatio), 3, BigDecimal.ROUND_HALF_UP);
                        comAmount = CalculateSocialUtils.calculateByRoundType(comAmount, roundTypesMap.get(hfEmpTask.getEmpTaskId())[0]);
                        empAmount = CalculateSocialUtils.calculateByRoundType(empAmount, roundTypesMap.get(hfEmpTask.getEmpTaskId())[1]);
                        String comRatioStr = CalculateSocialUtils.digitInSimpleFormat(comRatio.multiply(BigDecimal.valueOf(100)));
                        String empRatioStr = CalculateSocialUtils.digitInSimpleFormat(empRatio.multiply(BigDecimal.valueOf(100)));
                        String baseAmount = CalculateSocialUtils.digitInSimpleFormat(hfEmpTask.getEmpBase());
                        outputList.add(String.format(template, i + 1, hfEmpTask.getEmployeeId(), comRatioStr, empRatioStr, amount.toString(), comAmount.toString(), empAmount.toString(), baseAmount));
                    }
                }

                stringBuilder.append(title);
                if (CollectionUtils.isNotEmpty(outputList)) {
                    if (!employeeIdSet.isEmpty()) {
                        Wrapper<EmpEmployee> empWrapper = new EntityWrapper<>();
                        empWrapper.in("employee_id", employeeIdSet);
                        List<EmpEmployee> empList = empEmployeeService.selectList(empWrapper);

                        String employeeIdKey;
                        for (String output : outputList) {
                            for (EmpEmployee emp : empList) {
                                employeeIdKey = "{" + emp.getEmployeeId() + "}";
                                if (output.contains(employeeIdKey)) {
                                    output = output.replace(employeeIdKey + ".{employeeName}", emp.getEmployeeName())
                                        .replace(employeeIdKey + ".{idNum}", emp.getIdNum())
                                        .replace(employeeIdKey + ".{birthday}", (emp.getBirthday() != null)? emp.getBirthday().format(formatter) : "")
                                        .replace(employeeIdKey + ".{gender}", (emp.getGender() != null && emp.getGender()) ? "01" : "02");
                                    stringBuilder.append("\r\n");
                                    stringBuilder.append(output);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                stringBuilder.append(title);
            }

            byte[] dataByteArr = stringBuilder.toString().getBytes("UTF-8");
            String fileName = "开户文件.txt";

            OutputStream outputStream = response.getOutputStream();//获取OutputStream输出流
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "text/plain");
//        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ExportResponseUtil.encodeExportFileName(response, fileName);
            outputStream.write(dataByteArr);
            outputStream.flush();
            outputStream.close();
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @RequestMapping(value = "/empPreInputUpload", consumes = "multipart/form-data")
    public JsonResult empPreInputUpload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> files = multipartRequest.getMultiFileMap();//得到文件map对象
        ImportParams importParams = new ImportParams();
        List<HfEmpPreInputBO> successList = new ArrayList<>();
        List<HfEmpPreInputBO> failList = new ArrayList<>();

        try {
            importParams.setKeyIndex(null);
            importParams.setNeedVerfiy(true);
            importParams.setVerifyHanlder(new HfEmpPreInputVerifyHandler(hfEmpPreInputService, logApiUtil));
            hfFileImportService.executeExcelImport(hfFileImportService.IMPORT_TYPE_EMP_PRE_INPUT, hfFileImportService.DEFAULT_RELATED_UNIT_ID,
                null, hfImportEmpPreInputService, successList, failList,
                importParams, files, UserContext.getUserId());

            if (successList.size() == 0) {
                if (failList.size() == 0) {
                    return JsonResultKit.of(222, "上传文件的数据为空", failList);
                }
                return JsonResultKit.of(222, "上传文件的数据全部不正确", failList);
            } else if (failList.size() > 0) {
                return JsonResultKit.of(222, "上传文件存在不正确数据", failList);
            }
        } catch (Exception e) {
            LogMessage logMessage = LogMessage.create().setTitle("文件上传")
                .setContent("上传雇员公积金账号预录入信息：" + e.getMessage());
            logApiUtil.error(logMessage);
            return JsonResultKit.ofError("文件读取失败，请先检查文件格式是否正确");
        }

        return JsonResultKit.of(failList);
    }

    @RequestMapping("/downloadEmpPreInputTemplate")
    public void downloadEmpPreInputTemplate(HttpServletResponse response) {
        String fileNme = "雇员公积金账号预录入模板.xls";
        List<EmpTransferTemplateImpXsl> opts = new ArrayList();
        ExcelUtil.exportExcel(opts, HfEmpPreInputBO.class, fileNme, response);
    }
}
