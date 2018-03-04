package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business;

import com.baomidou.mybatisplus.service.IService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfPayment;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;

public interface HfPaymentService extends IService<HfPayment> {

    JsonResult<String> doReviewdePass(HfPayment hfPayment);

}