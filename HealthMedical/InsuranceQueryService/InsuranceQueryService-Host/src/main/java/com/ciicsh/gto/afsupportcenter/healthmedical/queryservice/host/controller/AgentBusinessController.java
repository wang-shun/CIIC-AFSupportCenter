package com.ciicsh.gto.afsupportcenter.healthmedical.queryservice.host.controller;


import com.ciicsh.gto.afsupportcenter.healthmedical.queryservice.api.AgentBusinessQueryProxy;
import com.ciicsh.gto.afsupportcenter.healthmedical.queryservice.api.dto.AgentBusinessDTO;
import com.ciicsh.gto.afsupportcenter.healthmedical.queryservice.business.AgentBusinessQueryService;
import com.ciicsh.gto.afsupportcenter.healthmedical.queryservice.entity.bo.AgentBusinessBO;
import com.ciicsh.gto.afsupportcenter.util.ConvertUtil;
import com.ciicsh.gto.afsupportcenter.util.core.Result;
import com.ciicsh.gto.afsupportcenter.util.core.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 代收代付发放表 前端控制器
 * </p>
 *
 * @author zhaogang
 * @since 2017-12-13
 */
@RestController
@RequestMapping("/queryservice/AgentBusiness")
public class AgentBusinessController implements AgentBusinessQueryProxy {

    @Autowired
    private AgentBusinessQueryService agentBusinessQueryService;


    @RequestMapping(value = "/getAgentBusinessIPListById", method = {RequestMethod.GET, RequestMethod.POST})
    public Result getAgentBusinessIPListById(String agentbusinessipid) {
        try {
            List<AgentBusinessBO> bolist = agentBusinessQueryService.getAgentBusinessIPListById(agentbusinessipid);
            List<AgentBusinessDTO> agentBusinessIpDTOList = ConvertUtil.listConvert(bolist, AgentBusinessDTO.class);
            return ResultGenerator.genSuccessResult(agentBusinessIpDTOList);
        } catch (Exception e) {
            return ResultGenerator.genServerFailResult();
        }
    }
}
