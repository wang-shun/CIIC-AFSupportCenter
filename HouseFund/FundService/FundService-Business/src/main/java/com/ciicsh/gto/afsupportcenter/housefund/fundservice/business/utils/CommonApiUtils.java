package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.utils;

import com.ciicsh.gto.afcompanycenter.commandservice.api.dto.employee.AfEmpSocialUpdateDateDTO;
import com.ciicsh.gto.afcompanycenter.commandservice.api.proxy.AfEmployeeSocialProxy;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto.TaskSheetRequestDTO;
import com.ciicsh.gto.afsystemmanagecenter.apiservice.api.SSPolicyProxy;
import com.ciicsh.gto.afsystemmanagecenter.apiservice.api.dto.item.GetSSPItemsRequestDTO;
import com.ciicsh.gto.afsystemmanagecenter.apiservice.api.dto.item.GetSSPItemsResposeDTO;
import com.ciicsh.gto.basicdataservice.api.CityServiceProxy;
import com.ciicsh.gto.basicdataservice.api.DicItemServiceProxy;
import com.ciicsh.gto.basicdataservice.api.dto.CityDTO;
import com.ciicsh.gto.basicdataservice.api.dto.DicItemDTO;
import com.ciicsh.gto.basicdataservice.api.dto.EmptyDicItemDTO;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.AfCompanyDetailResponseDTO;
import com.ciicsh.gto.salecenter.apiservice.api.proxy.CompanyProxy;
import com.ciicsh.gto.sheetservice.api.SheetServiceProxy;
import com.ciicsh.gto.sheetservice.api.dto.Result;
import com.ciicsh.gto.sheetservice.api.dto.request.TaskRequestDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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
    SSPolicyProxy ssPolicyProxy;

    @Autowired
    CompanyProxy companyProxy;

    @Autowired
    CityServiceProxy cityServiceProxy;

    /**
     * 调用客服中心的完成任务接口
     *
     * @param taskSheetRequestDTO
     * @return
     */
    public Result completeTask(@RequestBody TaskSheetRequestDTO taskSheetRequestDTO) throws Exception {
        logger.info("customer系统调用完成任务接口：" + taskSheetRequestDTO.toString());
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTaskId(taskSheetRequestDTO.getTaskId());
        taskRequestDTO.setAssignee(taskSheetRequestDTO.getAssignee());
        taskRequestDTO.setVariables(taskSheetRequestDTO.getVariable());
        Result restResult = sheetServiceProxy.completeTask(taskRequestDTO);
        logger.info("customer系统收到完成任务接口返回：" + String.valueOf("code:" + restResult.getCode() + "message:") +
            restResult.getMessage());
        return restResult;
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
     * 获取进位方式
     * @param var1
     * @return
     */
    public com.ciicsh.gto.afsystemmanagecenter.apiservice.api.dto.JsonResult<GetSSPItemsResposeDTO> getRoundingType(GetSSPItemsRequestDTO var1){
        return ssPolicyProxy.getSSPItems(var1);
    }

    /**
     * 根据客户编号获取服务中心
     *
     * @param companyId
     * @return
     */
    public AfCompanyDetailResponseDTO getServiceCenterInfo(String companyId) {
        com.ciicsh.gto.salecenter.apiservice.api.dto.core.JsonResult<AfCompanyDetailResponseDTO> jsonResult = companyProxy.afDetail(companyId);

        if (jsonResult != null && jsonResult.getCode() == 0) {
            return jsonResult.getObject();
        }
        return null;
    }

    /**
     * 根据城市编号获取城市名称
     * @param cityCode
     * @return
     */
    public String getCityName(String cityCode) {
        if (StringUtils.isNotEmpty(cityCode)) {
            CityDTO cityDTO = cityServiceProxy.selectByCityCode(cityCode);
            if (cityDTO != null) {
                return cityDTO.getCityName();
            }
        }
        return null;
    }
}
