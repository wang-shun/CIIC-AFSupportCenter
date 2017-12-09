package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.host.controller;


import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.ISsStatementService;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dto.SsEmpTaskDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.dto.SsStatementDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsStatementPO;
import com.ciicsh.gto.afsupportcenter.util.aspect.log.Log;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 本地社保中，中智公司与社保局的对账单（各一条记录） 前端控制器
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@RestController
@RequestMapping("/api/soccommandservice/ssStatement")
public class SsStatementController  extends BasicController<ISsStatementService> {

    /**
     * <p>Description: 对账单查询</p>
     *
     * @author wengxk
     * @date 2017-12-08
     * @param pageInfo 翻页检索条件
     * @return  JsonResult<List<SsStatementDTO>>
     */
    @Log("对账单查询")
    @PostMapping("/statementQuery")
    public JsonResult<List<SsStatementDTO>> statementQuery(PageInfo pageInfo) {
        //
       /* PageRows<SsStatementPO> pagePORows = business.statementQuery(pageInfo);
        PageRows<SsStatementDTO> pageRows = new PageRows<SsStatementDTO>();
        BeanUtils.copyProperties(pagePORows,pageRows);*/
        PageRows<SsStatementDTO> pageRows =business.statementQuery(pageInfo);


        return JsonResultKit.ofPage(pageRows);
    }







}

