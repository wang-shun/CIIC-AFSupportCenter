package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.JsonResult;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.SsComAccountParamDto;
import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.api.dto.SsComTaskDTO;
import com.ciicsh.gto.commonservice.util.dto.Result;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 企业社保账户信息查询接口
 */
@FeignClient("service-soccommandservice")
@RequestMapping("/api/soccommandservice/sscomtask")
public interface SsComTaskProxy {
    /**
     * 保存企业任务单
     *
     * @param ssComTaskDTO
     * @return
     */
    @PostMapping("/saveSsComTask")
    public Result saveSsComTask(@RequestBody SsComTaskDTO ssComTaskDTO);

}