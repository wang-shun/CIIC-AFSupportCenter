package com.ciicsh.gto.afsupportcenter.housefund.siteservice.host.controller;

import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfPaymentBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfPaymentService;
import com.ciicsh.gto.afsupportcenter.util.aspect.log.Log;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 公积金汇缴支付控制器：公积金汇缴支付所有相关的操作都集中于此控制器
 * </p>
 *
 * @author 沈健
 * @since 2018-03-14
 */
@RestController
@RequestMapping("/api/fundcommandservice/hfFundPay")
public class HfFundPayController {

    @Autowired
    private HfPaymentService hfPaymentService;

    @Log("查询公积金汇缴支付列表")
    @PostMapping("/fundPays")
    public JsonResult<List<HfPaymentBo>> postFundPays(PageInfo pageInfo) {
        PageRows<HfPaymentBo> pageRows = hfPaymentService.getFundPays(pageInfo);
        return JsonResultKit.ofPage(pageRows);
    }

}
