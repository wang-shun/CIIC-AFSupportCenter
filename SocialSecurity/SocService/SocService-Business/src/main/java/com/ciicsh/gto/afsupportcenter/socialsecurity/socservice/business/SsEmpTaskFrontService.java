package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.business;

import com.baomidou.mybatisplus.service.IService;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.employee.AfEmployeeInfoDTO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.bo.SsEmpTaskFrontBO;
import com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.SsEmpTaskFront;
import com.ciicsh.gto.salecenter.apiservice.api.dto.company.AfCompanyDetailResponseDTO;
import com.ciicsh.gto.sheetservice.api.dto.TaskCreateMsgDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 雇员任务单前道传递信息,创建任务单的同时，就要把前道的传递信息复制到这表，当前表复制前道cmy_af_emp_socia 服务类
 * </p>
 */
public interface SsEmpTaskFrontService extends IService<SsEmpTaskFront> {

    /**
     * 保存数据到雇员任务单表
     *
     * @param taskMsgDTO
     * @param taskCategory
     * @param isChange
     * @param dto
     * @return
     */
    boolean saveEmpTaskTc(TaskCreateMsgDTO taskMsgDTO, Integer taskCategory, Integer processCategory, Integer isChange, String oldAgreementId,  AfEmployeeInfoDTO dto,
                          AfCompanyDetailResponseDTO afCompanyDetailResponseDTO, Map<String, Object> cityCodeMap);

    /**
     * 更新旧的雇员任务单
     *
     * @param taskMsgDTO 消息队列接受的对象
     * @param dto        取得的雇员信息
     * @return
     * @author zhangxj
     * @date 2017-12-28
     */
    boolean updateEmpTaskTc(TaskCreateMsgDTO taskMsgDTO,
                            AfEmployeeInfoDTO dto);

    /**
     * 保存数据到雇员任务单表
     *
     * @param taskMsgDTO
     * @param socialType
     * @param isChange
     * @param oldAgreementId
     * @param dto
     * @return
     * @throws Exception
     */
    boolean saveSsEmpTask(TaskCreateMsgDTO taskMsgDTO, Integer socialType, Integer processCategory, Integer isChange,
                          String oldAgreementId, AfEmployeeInfoDTO dto,
                          AfCompanyDetailResponseDTO afCompanyDetailResponseDTO, Map<String, Object> cityCodeMap) throws Exception;

    Integer getEmpTaskDetailCount(String businessInterfaceId);

    List<SsEmpTaskFrontBO> getOriginEmpTaskList(Long empArchiveId);
}
