package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.host.controller;


import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.bo.SsMonthEmpChangeBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.ISsMonthEmpChangeService;
import com.ciicsh.gto.afsupportcenter.util.aspect.log.Log;
import com.ciicsh.gto.afsupportcenter.util.web.controller.BasicController;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 雇员月度变更主表 前端控制器
 * </p>
 *
 * @author HuangXing
 * @since 2017-12-01
 */
@RestController
@RequestMapping("/api/soccommandservice/ssMonthEmpChange")
public class SsMonthEmpChangeController  extends BasicController<ISsMonthEmpChangeService> {
    /**
     * <p>Description: 社保汇总基本数据查询</p>
     *
     * @author wengxk
     * @date 2017-12-13
     * @param ssMonthEmpChangeBO 社保总汇检索条件
     * @return  JsonResult<SsMonthEmpChangeBO>
     */
    @Log("社保汇总基本数据查询")
    @PostMapping("/serachMonthEmpChange")
    public JsonResult<SsMonthEmpChangeBO> serachMonthEmpChange(SsMonthEmpChangeBO ssMonthEmpChangeBO) {
        SsMonthEmpChangeBO resultDto = business.serachMonthEmpChangeByStatementId(ssMonthEmpChangeBO);
        JsonResult<SsMonthEmpChangeBO> result = new JsonResult<>();
        result.setData(resultDto);

        return result;
    }
}
