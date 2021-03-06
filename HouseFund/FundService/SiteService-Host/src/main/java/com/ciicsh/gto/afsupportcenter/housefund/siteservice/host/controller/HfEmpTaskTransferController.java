package com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.controller;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.transfer.*;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.*;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.constant.HfEmpTaskConstant;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpTask;
import com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.util.FeedbackDateVerifyHandler;
import com.ciicsh.gto.afsupportcenter.util.ExcelUtil;
import com.ciicsh.gto.afsupportcenter.util.PdfUtil;
import com.ciicsh.gto.afsupportcenter.util.WordUtil;
import com.ciicsh.gto.afsupportcenter.util.constant.HouseFundConst;
import com.ciicsh.gto.afsupportcenter.util.constant.SocialSecurityConst;
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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/fundcommandservice/hfEmpTask")
public class HfEmpTaskTransferController extends BasicController<HfEmpTaskTransferService> {
    @Autowired
    HfEmpTaskHandleService hfEmpTaskHandleService;
    @Autowired
    EmpEmployeeService empEmployeeService;
    @Autowired
    HfFileImportService hfFileImportService;
    @Autowired
    HFImportFeedbackDateService hfImportFeedbackDateService;
    @Autowired
    LogApiUtil logApiUtil;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    private static final int MULTI_EXPORT_CREATED_BY_MAX_LENTH = 22;
    /**
     * 雇员公积金转移任务查询
     *
     * @param pageInfo
     * @return
     */
    @RequestMapping("/queryEmpTaskTransfer")
    public JsonResult<PageRows> queryEmpTaskTransfer(PageInfo pageInfo) {
        return JsonResultKit.of(business.queryEmpTaskTransferPage(pageInfo));
    }

    /**
     * 雇员公积金转移任务新建雇员查询
     *
     * @param pageInfo
     * @return
     */
    @RequestMapping("/queryEmpTaskTransferNewTask")
    public JsonResult<PageRows> queryEmpTaskTransferNewTask(PageInfo pageInfo) {
        return JsonResultKit.of(business.queryEmpTaskTransferNewTaskPage(pageInfo));
    }

    /**
     * 雇员公积金转移任务单表单查询
     *
     * @param employeeId
     * @param companyId
     * @param hfType
     * @param empTaskId
     * @return
     */
    @RequestMapping("/queryComEmpTransferForm")
    public JsonResult<HfEmpTaskHandleVo> queryComEmpTransferForm(@RequestParam("employeeId") String employeeId,
                                                                 @RequestParam("companyId") String companyId,
                                                                 @RequestParam(value = "hfType", required = false) String hfType,
                                                                 @RequestParam(value = "empTaskId", required = false) String empTaskId) {
        HfEmpTaskHandleVo hfEmpTaskHandleBo = new HfEmpTaskHandleVo();
        if(hfType == null){
            hfType = HouseFundConst.HF_TYPE_BASE;
        }

        long employeeTaskId = 0;
        if (Optional.ofNullable(empTaskId).isPresent()) {
            employeeTaskId = Long.valueOf(empTaskId);
        }
        //获取企业账户和雇员信息
        hfEmpTaskHandleBo = business.queryComEmpTransferForm(employeeId, companyId, employeeTaskId);
        //获取转移任务单信息
        hfEmpTaskHandleBo.setHfType(Integer.parseInt(hfType));
        hfEmpTaskHandleBo.setProcessCategory(9);
        hfEmpTaskHandleBo.setTaskCategory(8);//转移任务单
        return JsonResultKit.of(hfEmpTaskHandleBo);
    }

    /**
     * 提交转移任务单
     */
    @RequestMapping("/submitTransferTask")
    public JsonResult submitTransferTask(@RequestBody EmpTaskTransferBo empTaskTransferBo) {
        try {
            return business.submitTransferTask(empTaskTransferBo);
        } catch (BusinessException e) {
            return JsonResultKit.ofError(e.getMessage());
        }
    }
    /**
     * 获取转移任务单打印数据
     */
    @RequestMapping("/getPrintTransfer")
    public JsonResult getPrintTransfer(EmpTaskTransferBo empTaskTransferBo) throws Exception {
        List<Map<String, Object>> printList = business.printTransferTask(empTaskTransferBo);
        return JsonResultKit.of(printList);
    }

    /**
     * 打印转移任务单PDF
     */
    @RequestMapping("/printTransferTask")
    public void printTransferTask(HttpServletResponse response, EmpTaskTransferBo empTaskTransferBo) throws Exception {
        try {
            String templateFilePath;
            List<Map<String, Object>> printList = business.printTransferTask(empTaskTransferBo);
            templateFilePath = "/template/SH_HF_TRANSFER_TMP.pdf";
            String fileName = "上海市公积金转移通知书.pdf";
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/pdf");
//            response.setHeader("Content-Disposition",
//                "attachment;filename=" + fileName);
            ExportResponseUtil.encodeExportFileName(response, fileName);
            PdfUtil.createPdfByTemplate(templateFilePath,
                PdfUtil.DEFAULT_FONT_NAME,
                PdfUtil.DEFAULT_FONT_ENCODING,
                false,
                true,
                printList,
                response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "text/html");
            response.getWriter().write(e.getMessage() + "<a href=\"javascript:history.go(-1)\">返回</a>");
        }

    }

    /**
     * 不需处理转移任务单
     */
    @RequestMapping("/notHandleTransfer")
    public JsonResult notHandleTransfer(@RequestBody EmpTaskTransferBo empTaskTransferBo) {
        return business.notHandleTransfer(empTaskTransferBo);
    }

    /**
     * 雇员公积金转移清册导出
     *
     * @param pageInfo
     * @return
     */
    @RequestMapping("/multiEmpTaskTransferExport")
    public void multiEmpTaskTransferExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
        EmpTaskTransferBo empTaskTransferBo = pageInfo.toJavaObject(EmpTaskTransferBo.class);
        if (StringUtils.isEmpty(empTaskTransferBo.getTransferInUnit())) {
            empTaskTransferBo.setTransferInUnit(SocialSecurityConst.FUND_OUT_UNIT_LIST.get(1));
        }
        List<EmpTaskTransferBo> empTaskTransferBoList = business.queryEmpTaskTransfer(empTaskTransferBo);

        Map<Integer, Map<String, Object>> alMap = new HashMap<>();
        String[] transferOutUnitAccounts = {"", ""};
        String[] transferInUnitAccounts = {"", ""};
        String[] companyIdsArr = {"", ""};
        String[] paymentBanks = {"", ""};

        for (int i = 0; i < 2; i++) {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, Object>> mapList = new ArrayList<>();
            final int m = i;
            List<EmpTaskTransferBo> list = empTaskTransferBoList.stream().filter(e -> e.getHfType() == m + 1).collect(Collectors.toList());
            HashSet<String> companyIdSet = new HashSet<>();

            if (CollectionUtils.isNotEmpty(list)) {
                String account = list.get(0).getTransferOutUnitAccount();
                if (account != null) {
                    transferOutUnitAccounts[i] = account;
                }
                account = list.get(0).getTransferInUnitAccount();
                if (account != null) {
                    transferInUnitAccounts[i] = account;
                }

                for (int j = 0; j < list.size(); j++) {
                    if (!transferOutUnitAccounts[i].equals(list.get(j).getTransferOutUnitAccount())) {
                        response.setCharacterEncoding("UTF-8");
                        response.setHeader("content-Type", "text/html");
                        response.getWriter().write("仅支持一次导出相同转出单位公积金账号的信息<a href=\"javascript:history.go(-1)\">返回</a>");
                        return;
                    }
                    if (!transferInUnitAccounts[i].equals(list.get(j).getTransferInUnitAccount())) {
                        response.setCharacterEncoding("UTF-8");
                        response.setHeader("content-Type", "text/html");
                        response.getWriter().write("仅支持一次导出相同转入单位公积金账号的信息<a href=\"javascript:history.go(-1)\">返回</a>");
                        return;
                    }

                    Map<String, Object> lm = new HashMap<>();
                    lm.put("hfEmpAccount", list.get(j).getHfEmpAccount());
                    lm.put("employeeName", list.get(j).getEmployeeName());
                    lm.put("rowNo", j + 1);
                    mapList.add(lm);

                    companyIdSet.add(list.get(j).getCompanyId());
                }
                companyIdsArr[i] = StringUtils.join(companyIdSet.stream().sorted().toArray(), ' ');

                String paymentBankName = list.get(0).getPaymentBankName();
                if (StringUtils.isNotEmpty(paymentBankName)) {
                    paymentBanks[i] = paymentBankName;
                }
            }
            map.put("maplist", mapList);
            alMap.put(i, map);
        }

        TemplateExportParams params = new TemplateExportParams("/template/SH_HF_MULTI_TRANSFER_TMP.xlsx", 0, 1);
        Workbook workbook = ExcelExportUtil.exportExcel(alMap, params);
        String currentUser = UserContext.getUser().getDisplayName();

        if (currentUser == null) {
            currentUser = "";
        }
        int currentUserLength = currentUser.length();

        if (currentUserLength > MULTI_EXPORT_CREATED_BY_MAX_LENTH) {
            currentUser = currentUser.substring(0, MULTI_EXPORT_CREATED_BY_MAX_LENTH);
        } else if (currentUserLength < MULTI_EXPORT_CREATED_BY_MAX_LENTH) {
            char[] spaces = new char[MULTI_EXPORT_CREATED_BY_MAX_LENTH - currentUserLength];
            Arrays.fill(spaces, ' ');
            currentUser = currentUser + String.valueOf(spaces);
        }

        for (int i = 0; i < 2; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String left = sheet.getHeader().getLeft();
            left = left.replace("{{transferOutUnitAccount}}", transferOutUnitAccounts[i]);
            left = left.replace("{{transferInUnitAccount}}", transferInUnitAccounts[i]);
            sheet.getHeader().setLeft(left);

            String right = sheet.getHeader().getRight();
            right = right.replace("{{companyId}}", companyIdsArr[i]);
            right = right.replace("{{paymentBankName}}", paymentBanks[i]);
            sheet.getHeader().setRight(right);

            String center = sheet.getFooter().getCenter();
            center = center.replace("{{createdBy}}", currentUser);
            sheet.getFooter().setCenter(center);
        }
        try {
            String fileName = "上海市公积金雇员转移清册.xlsx";

            response.reset();
            response.setCharacterEncoding("UTF-8");
//            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ExportResponseUtil.encodeExportFileName(response, fileName);
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            logApiUtil.error(LogMessage.create().setTitle("HfEmpTaskTransferController#multiEmpTaskTransferExport").setContent(e.getMessage()));
        }
    }

    /**
     * 雇员公积金转移TXT导出
     *
     * @param response
     * @param pageInfo
     * @throws Exception
     */
    /*@RequestMapping("/empTaskTransferTxtExport")
    public void empTaskTransferTxtExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
        EmpTaskTransferBo empTaskTransferBo = pageInfo.toJavaObject(EmpTaskTransferBo.class);
        List<EmpTaskTransferBo> empTaskTransferBoList = business.queryEmpTaskTransfer(empTaskTransferBo);
        Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        String title = "序号|公积金账号||||";
        String template = "%1$d|%2$s||||";
        List<String> outputList = new ArrayList<>();
       // writer.append(title);
        if (CollectionUtils.isNotEmpty(empTaskTransferBoList)) {
            for (int i = 0; i < empTaskTransferBoList.size(); i++) {
                empTaskTransferBo = empTaskTransferBoList.get(i);
                outputList.add(String.format(template, i + 1, empTaskTransferBo.getHfEmpAccount()));
            }
            for (String output : outputList) {
                writer.append("\r\n");
                writer.append(output);
            }
        }
        String fileName = "上海市公积金雇员转移TXT.txt";
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "text/plain");
        // response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ExportResponseUtil.encodeExportFileName(response, fileName);
        writer.close();
    }*/

    @RequestMapping("/empTaskTransferTxtExport")
    public void empTaskTransferTxtExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
        EmpTaskTransferBo empTaskTransferBo = pageInfo.toJavaObject(EmpTaskTransferBo.class);
        List<EmpTaskTransferBo> empTaskTransferBoList = business.queryEmpTaskTransfer(empTaskTransferBo);
        //Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        OutputStream outputStream = response.getOutputStream();//获取OutputStream输出流
        String title = "序号|公积金账号||||";
        String template = "%1$d|%2$s||||";
        //List<String> outputList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        if (CollectionUtils.isNotEmpty(empTaskTransferBoList)) {
            for (int i = 0; i < empTaskTransferBoList.size(); i++) {
                empTaskTransferBo = empTaskTransferBoList.get(i);
                sb.append("\r\n");
                sb.append(String.format(template, i + 1, empTaskTransferBo.getHfEmpAccount()));
            }
        }

        byte[] dataByteArr = sb.toString().getBytes("UTF-8");
        String fileName = "上海市公积金雇员转移TXT.txt";
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "text/plain");
        // response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ExportResponseUtil.encodeExportFileName(response, fileName);
        outputStream.write(dataByteArr);
        outputStream.flush();
        outputStream.close();
    }

    @RequestMapping(value = "/feedbackDateUpload", consumes = "multipart/form-data")
    public JsonResult feedbackDateUpload(HttpServletRequest request) {
        EmpTaskTransferBo empTaskTransferBo = new EmpTaskTransferBo();
        empTaskTransferBo.setTaskStatus(HfEmpTaskConstant.TASK_STATUS_COMPLETED); // TODO 2 or 3?
        List<EmpTaskTransferBo> empTaskTransferBoList = business.queryEmpTaskTransfer(empTaskTransferBo);

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> files = multipartRequest.getMultiFileMap();//得到文件map对象
        ImportParams importParams = new ImportParams();
        List<ImportFeedbackDateBO> successList = new ArrayList<>();
        List<ImportFeedbackDateBO> failList = new ArrayList<>();

        try {
            importParams.setKeyIndex(null);
            importParams.setNeedVerfiy(true);
            importParams.setVerifyHanlder(new FeedbackDateVerifyHandler(logApiUtil, empTaskTransferBoList));
            hfFileImportService.executeExcelImport(hfFileImportService.IMPORT_TYPE_EMP_TASK_TRANS_FEEDBACK_DATE, hfFileImportService.DEFAULT_RELATED_UNIT_ID,
                null, hfImportFeedbackDateService, successList, failList,
                importParams, files, UserContext.getUserId());
        } catch (Exception e) {
            LogMessage logMessage = LogMessage.create().setTitle("文件上传")
                .setContent("上传回单日期：" + e.getMessage());
            logApiUtil.error(logMessage);
            return JsonResultKit.ofError("文件读取失败，请先检查文件格式是否正确");
        }

        return JsonResultKit.of(failList);
    }

    /**
     * 雇员公积金转移任务批量更新回单日期
     *
     * @param feedbackDateBatchUpdateBO
     * @return
     */
    @RequestMapping("/batchUpdateFeedbackDate")
    public JsonResult batchUpdateFeedbackDate(@RequestBody FeedbackDateBatchUpdateBO feedbackDateBatchUpdateBO) {
        Long[] selectedData = feedbackDateBatchUpdateBO.getSelectedData();
        if (!ArrayUtils.isEmpty(selectedData)) {
            List<HfEmpTask> list = new ArrayList<>();
            for (Long empTaskId : selectedData) {
                HfEmpTask hfEmpTask = new HfEmpTask();
                hfEmpTask.setEmpTaskId(empTaskId);
                hfEmpTask.setFeedbackDate(feedbackDateBatchUpdateBO.getFeedbackDate());
                hfEmpTask.setModifiedTime(LocalDateTime.now());
                hfEmpTask.setModifiedBy(UserContext.getUserId());
                hfEmpTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
                list.add(hfEmpTask);
            }
            if (!business.updateBatchById(list)) {
                return JsonResultKit.ofError("数据库批量更新失败");
            }
        }else {
            return business.batchUpdateFeedbackDate(feedbackDateBatchUpdateBO);
        }
        return JsonResultKit.of();
    }

    @RequestMapping("/batchPrintNote")
    public JsonResult batchPrintNote(FeedbackDateBatchUpdateBO feedbackDateBatchUpdateBO) {
        Long[] selectedData = feedbackDateBatchUpdateBO.getSelectedData();
        if (!ArrayUtils.isEmpty(selectedData)) {
            List<HfEmpTask> list = new ArrayList<>();
            for (Long empTaskId : selectedData) {
                HfEmpTask hfEmpTask = new HfEmpTask();
                hfEmpTask.setEmpTaskId(empTaskId);
                hfEmpTask.setFeedbackDate(feedbackDateBatchUpdateBO.getFeedbackDate());
                hfEmpTask.setModifiedTime(LocalDateTime.now());
                hfEmpTask.setModifiedBy(UserContext.getUserId());
                hfEmpTask.setModifiedDisplayName(UserContext.getUser().getDisplayName());
                list.add(hfEmpTask);
            }
            if (!business.updateBatchById(list)) {
                return JsonResultKit.ofError("数据库批量更新失败");
            }
        }else {
            return business.batchUpdateFeedbackDate(feedbackDateBatchUpdateBO);
        }

//        List<Map<String, Object>> printList = business.printTransferTask(empTaskTransferBo);
//        return JsonResultKit.of(printList);
        return JsonResultKit.of();
    }

    /**
     * 雇员公积金转移导出
     */
    @RequestMapping("/queryEmpTaskTransferExp")
    public JsonResult<PageRows> queryEmpTaskTransferExp(PageInfo pageInfo) {
        return null;
        //return JsonResultKit.of(business.queryEmpTaskTransferPage(pageInfo));
    }

    /**
     * 雇员公积金转移导出
     */
    @RequestMapping("/downloadTransferTemplateFile")
    public void downloadTransferTemplateFile(HttpServletResponse response) {
        String fileNme = "雇员公积金转移导入模板.xls";
        List<EmpTransferTemplateImpXsl> opts = new ArrayList();
        ExcelUtil.exportExcel(opts, EmpTransferTemplateImpXsl.class, fileNme, response);
    }

    /**
     * 雇员公积金封存清册导出校验
     * @param pageInfo
     * @return
     */
    @RequestMapping("/checkEmpTransferEndMonthSame")
    public JsonResult<Integer> checkEmpTransferEndMonthSame(PageInfo pageInfo) {
        EmpTaskTransferBo empTaskTransferBo = pageInfo.toJavaObject(EmpTaskTransferBo.class);
        if (StringUtils.isEmpty(empTaskTransferBo.getTransferInUnit())) {
            empTaskTransferBo.setTransferInUnit(SocialSecurityConst.FUND_OUT_UNIT_LIST.get(1));
        }
        int rtn = 0;
        List<String> endMonthList = business.getEmpTransferEndMonth(empTaskTransferBo);
        if (CollectionUtils.isEmpty(endMonthList)) {
            rtn = 1;
//        } else {
//            if (endMonthList.size() > 2) {
//                rtn = 2;
//            } else if (endMonthList.size() == 2) {
//                if (StringUtils.isNotEmpty(endMonthList.get(0)) && StringUtils.isNotEmpty(endMonthList.get(1))) {
//                    rtn = 2;
//                }
//            }
        }


        return JsonResultKit.of(rtn);
    }

    /**
     * 雇员公积金封存清册导出
     * @param response
     * @param pageInfo
     * @throws Exception
     */
    @RequestMapping("/empToCenterTransferExport")
    public void empToCenterTransferExport(HttpServletResponse response, PageInfo pageInfo) throws Exception {
        try {
            EmpTaskTransferBo empTaskTransferBo = pageInfo.toJavaObject(EmpTaskTransferBo.class);
            if (StringUtils.isEmpty(empTaskTransferBo.getTransferInUnit())) {
                empTaskTransferBo.setTransferInUnit(SocialSecurityConst.FUND_OUT_UNIT_LIST.get(1));
            }
            List<EmpTaskTransferBo> empTaskTransferBoList = business.queryEmpTaskTransfer(empTaskTransferBo);

            if (CollectionUtils.isNotEmpty(empTaskTransferBoList)) {
                List<EmpTransferToCenterBO> empTransferToCenterBOList = business.addEmpTransferToCenterBOList(empTaskTransferBoList);
                Map map = new HashMap();
                map.put("pageList", empTransferToCenterBOList);

                WordUtil.getInstance().exportWord(response, map, "雇员公积金封存清册", "FUND_CLOSING_LIST_TEMP.ftl");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
