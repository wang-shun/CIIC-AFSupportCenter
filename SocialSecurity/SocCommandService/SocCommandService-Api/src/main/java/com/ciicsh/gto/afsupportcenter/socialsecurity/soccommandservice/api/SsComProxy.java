package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.JsonResult;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.SsComAccountParamDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 企业社保账户信息查询接口
 */
@FeignClient("support-center-soc-command-service")
@RequestMapping("/api/soccommandservice/sscom")
public interface SsComProxy {

    /**
     * 获取企业社保账户信息表
     *
         * @param paramDto
     * @return
         */
    @RequestMapping("/getSsComAccountList")
    JsonResult<List<com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.SsComAccountDTO>>
    getSsComAccountList(@RequestBody SsComAccountParamDto paramDto);

}
