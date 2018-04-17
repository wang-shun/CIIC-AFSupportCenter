package com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.controller;

import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HFStatementCompareBO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HFStatementCompareService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto.FundStatementDetailDTO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto.HFStatementCompareDTO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto.NewStatementDTO;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.ciicsh.gto.afsupportcenter.util.kit.JsonKit;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/fundcommandservice/statement")
public class StatementCompareController extends BasicController<HFStatementCompareService> {

    /**
     * 根据筛选条件获取公积金对账单
     *
     * @param pageInfo
     * @return
     */
    @PostMapping("/getStatements")
    public JsonResult<List<HFStatementCompareDTO>> getStatements(@RequestBody PageInfo pageInfo) {

        String hfMonth = pageInfo.getParams().getString("hfMonth");
        String hfComAccount = pageInfo.getParams().getString("hfComAccount");
        PageRows<HFStatementCompareBO> users = PageKit.doSelectPage(pageInfo, () -> business.getHFStatementCompareRecord(hfMonth, hfComAccount));

        List<HFStatementCompareDTO> dtos = JsonKit.castToList(users.getRows(), HFStatementCompareDTO.class);
        PageRows<HFStatementCompareDTO> result = new PageRows<>();
        result.setRows(dtos);
        result.setTotal(users.getTotal());
        return JsonResultKit.ofPage(result);
    }

    /**
     * 新建对账
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/addStatement")
    public JsonResult<Long> addStatement(NewStatementDTO newStatement, MultipartFile file) {
        long statementId = 0;
        try {
            statementId = business.addStatement(newStatement, file);
        }
        catch (Exception e) {
            return JsonResultKit.ofError(e.getMessage());
        }

        return JsonResultKit.of(0, "添加对账单成功", statementId);
    }

    /**
     * 删除对账单记录
     *
     * @param statementId
     * @return
     */
    @GetMapping("/delStatement/{statementId}")
    public JsonResult<Boolean> delStatement(@PathVariable("statementId") long statementId) {
        Boolean result = business.delStatement(statementId) > 0;
        return JsonResultKit.of(0, result ? "删除成功" : "删除失败", result);
    }

    /**
     * 获取对账单记录详情
     *
     * @param statementId
     * @return
     */
    @GetMapping("/getStatementDetail/{statementId}")
    public JsonResult<FundStatementDetailDTO> getStatementDetail(@PathVariable("statementId") long statementId) {
        FundStatementDetailDTO result = business.getStatementDetail(statementId);

        return JsonResultKit.of(0, "获取对账单详情成功", result);

    }

    /**
     * 执行对账
     *
     * @param statementId
     * @return
     */
    @GetMapping("/execStatement/{statementId}")
    public JsonResult<Boolean> execStatement(@PathVariable("statementId") long statementId) {
        try{
            business.execStatement(statementId);
        }
        catch (Exception ex){
            return JsonResultKit.of(0, ex.getMessage(), false);

        }

        return JsonResultKit.of(0, "对账成功", true);

    }


}
