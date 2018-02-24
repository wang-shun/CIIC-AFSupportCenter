package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.business.utils;

import com.ciicsh.gto.afcompanycenter.commandservice.api.dto.employee.AfEmpSocialUpdateDateDTO;
import com.ciicsh.gto.afcompanycenter.commandservice.api.proxy.AfEmployeeSocialProxy;
import com.ciicsh.gto.basicdataservice.api.DicItemServiceProxy;
import com.ciicsh.gto.basicdataservice.api.dto.DicItemDTO;
import com.ciicsh.gto.basicdataservice.api.dto.EmptyDicItemDTO;
import com.ciicsh.gto.commonservice.util.dto.Result;
import com.ciicsh.gto.employeecenter.apiservice.api.dto.EmployeeInfoDTO;
import com.ciicsh.gto.employeecenter.apiservice.api.dto.EmployeeSearchDTO;
import com.ciicsh.gto.employeecenter.apiservice.api.dto.Page;
import com.ciicsh.gto.employeecenter.apiservice.api.proxy.EmployeeInfoProxy;
import com.ciicsh.gto.employeecenter.util.JsonResult;
import com.ciicsh.gto.sheetservice.api.SheetServiceProxy;
import com.ciicsh.gto.sheetservice.api.dto.request.TaskRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Created by houwanhua on 2018/2/22.
 */
@Component
public class CommonApiUtils {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    SheetServiceProxy sheetServiceProxy;

    @Autowired
    DicItemServiceProxy dicItemServiceProxy;

    @Autowired
    AfEmployeeSocialProxy afEmployeeSocialProxy;

    @Autowired
    EmployeeInfoProxy employeeInfoProxy;

    /**
     * 调用客服中心的完成任务接口
     * @param requestDTO
     * @return
     */
    public Result completeTask(@RequestBody TaskRequestDTO requestDTO) throws Exception {
        logger.info("customer系统调用完成任务接口：" + requestDTO.toString());
        com.ciicsh.gto.commonservice.util.dto.Result restResult = sheetServiceProxy.completeTask(requestDTO);
        logger.info("customer系统收到完成任务接口返回：" + String.valueOf("code:" + restResult.getCode() + "message:") + restResult.getMessage());
        return restResult;
    }

    /**
     * 根据ID取得名称
     *
     * @param dicItemId
     * @return
     */
    public DicItemDTO selectByDicItemId(String dicItemId) throws Exception {
        return dicItemServiceProxy.selectByDicItemId(dicItemId);
    }

    /**
     * 根据ID取得名称
     *
     * @param listByDicId
     * @return
     */
    public List<DicItemDTO> listByDicId(String listByDicId) throws Exception {
        return dicItemServiceProxy.listByDicId(listByDicId);
    }

    /**
     * 刷新REDIS中的ID数据
     *
     * @param dicItemDto
     * @return
     */
    public void fresh2Redis(EmptyDicItemDTO dicItemDto) throws Exception {
        dicItemServiceProxy.fresh2Redis(dicItemDto);
    }

    /**
     * 雇员任务单实缴金额回调接口（支持中心调用客服中心）
     *
     * @param var1
     * @return int
     */
    public int updateConfirmDate(@RequestBody List<AfEmpSocialUpdateDateDTO> var1) throws Exception {
        return afEmployeeSocialProxy.updateConfirmDate(var1);
    }

    /**
     * 获取雇员信息（支持中心调用雇员中心）
     *
     * @param var1 传入employeeId和业务类型
     * @return
     */
    public JsonResult<Page<EmployeeInfoDTO>> searchEmployeeInfo(
        @RequestBody EmployeeSearchDTO var1) throws Exception {
        return employeeInfoProxy.searchEmployeeInfo(var1);
    }
}