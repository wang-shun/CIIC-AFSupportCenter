package com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice.api;


import com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice.api.core.Result;
import com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice.api.dto.GiftDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xiweizhen
 */
@FeignClient("afsupportcenter-center-query-service")
@RequestMapping("/api/gift")
public interface GiftCommandProxy {
    /**
     * 根据主键查询礼品信息
     *
     * @param entity
     * @return
     */
    @PostMapping("/findGiftList")
    Result findGiftList(GiftDTO entity);

}
