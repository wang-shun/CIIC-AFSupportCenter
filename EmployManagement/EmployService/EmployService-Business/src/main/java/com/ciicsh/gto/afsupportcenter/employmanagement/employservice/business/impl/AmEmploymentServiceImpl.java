package com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.common.entity.JsonResult;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.bo.*;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.AmEmpEmployeeService;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.IAmEmploymentService;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.business.IAmRemarkService;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.custom.archiveSearchExportOpt;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dao.AmArchiveMapper;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dao.AmEmploymentMapper;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dto.AmArchiveReturnPrintDTO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dto.AmEmpArchiveAdvanceXsl;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmArchive;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmEmpEmployee;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmEmployment;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmRemark;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.api.FundApiProxy;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.api.dto.HfEmpInfoDTO;
import com.ciicsh.gto.afsupportcenter.util.DateUtil;
import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.interceptor.authenticate.UserContext;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import com.ciicsh.gto.employeecenter.apiservice.api.dto.ResidentInfoDTO;
import com.ciicsh.gto.employeecenter.apiservice.api.proxy.EmployeeInfoProxy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用工主表 服务实现类
 * </p>
 */
@Service
public class AmEmploymentServiceImpl extends ServiceImpl<AmEmploymentMapper, AmEmployment> implements IAmEmploymentService {

    @Autowired
    private EmployeeInfoProxy employeeInfoProxy;

    @Autowired
    private FundApiProxy fundApiProxy;

    @Autowired
    private AmEmpEmployeeService amEmpEmployeeService;

    @Autowired
    private AmEmploymentMapper amEmploymentMapper;

    @Autowired
    private AmArchiveMapper amArchiveMapper;

    @Autowired
    private IAmRemarkService amRemarkService;


    @Override
    public List<AmEmploymentBO> queryAmEmployment(Map<String, Object> param) {
        return baseMapper.queryAmEmployment(param);
    }

    @Override
    public PageRows<AmEmploymentBO> queryAmArchive(PageInfo pageInfo) {
        AmEmploymentBO amEmploymentBO = pageInfo.toJavaObject(AmEmploymentBO.class);

        if(null!=amEmploymentBO.getTaskStatus()&&amEmploymentBO.getTaskStatus()==6)
        {
            amEmploymentBO.setTaskStatus(null);
            amEmploymentBO.setTaskStatusOther(0);
        }

        if(null!=amEmploymentBO.getTaskResignStatus()&&amEmploymentBO.getTaskResignStatus()==6){
            amEmploymentBO.setTaskResignStatus(null);
            amEmploymentBO.setTaskResignStatusOther(0);
        }

        List<String> param = new ArrayList<String>();
        List<String> orderParam = new ArrayList<String>();
        if(!StringUtil.isEmpty(amEmploymentBO.getParams()))
        {
            String arr[] = amEmploymentBO.getParams().split(",");
            for(int i=0;i<arr.length;i++) {
                if(!StringUtil.isEmpty(arr[i]))
                {
                    if(arr[i].indexOf("desc")>0||arr[i].indexOf("asc")>0){
                        orderParam.add(arr[i]);
                    }else {
                        param.add(arr[i]);
                    }
                }

            }
        }

        amEmploymentBO.setParam(param);
        amEmploymentBO.setOrderParam(orderParam);
        //如果是查询用工总的数量就不需要状态了
        if(null!=amEmploymentBO.getTaskStatus()&&amEmploymentBO.getTaskStatus()==0){
            amEmploymentBO.setTaskStatus(null);
        }
        //如果是查询退工总的数量就不需要状态了
        if(null!=amEmploymentBO.getTaskResignStatus()&&amEmploymentBO.getTaskResignStatus()==0){
            amEmploymentBO.setTaskResignStatus(null);
        }

        if(amEmploymentBO.getTaskCategory()!=null&&amEmploymentBO.getTaskCategory()==2)
        {
            return PageKit.doSelectPage(pageInfo,() -> baseMapper.queryAmArchiveResign(amEmploymentBO));
        }else {
            return PageKit.doSelectPage(pageInfo,() -> baseMapper.queryAmArchive(amEmploymentBO));
        }


    }

    @Override
    public List<AmEmploymentBO> taskCountEmployee(PageInfo pageInfo) {
        AmEmploymentBO amEmploymentBO = pageInfo.toJavaObject(AmEmploymentBO.class);

        List<String> param = new ArrayList<String>();

        if(!StringUtil.isEmpty(amEmploymentBO.getParams()))
        {
            String arr[] = amEmploymentBO.getParams().split(",");
            for(int i=0;i<arr.length;i++) {
                param.add(arr[i]);
            }
        }

        amEmploymentBO.setParam(param);
        return baseMapper.taskCountEmployee(amEmploymentBO);
    }

    @Override
    public List<AmEmploymentBO> taskCountResign(PageInfo pageInfo) {
        AmEmploymentBO amEmploymentBO = pageInfo.toJavaObject(AmEmploymentBO.class);

        List<String> param = new ArrayList<String>();

        if(!StringUtil.isEmpty(amEmploymentBO.getParams()))
        {
            String arr[] = amEmploymentBO.getParams().split(",");
            for(int i=0;i<arr.length;i++) {
                param.add(arr[i]);
            }
        }

        amEmploymentBO.setParam(param);
        return baseMapper.taskCountResign(amEmploymentBO);
    }

    @Override
    public List<archiveSearchExportOpt> queryAmArchiveList(AmEmploymentBO amEmploymentBO) {
        return baseMapper.queryAmArchiveList(amEmploymentBO);
    }

    @Override
    public List<AmEmploymentBO> queryAmEmploymentResign(Map<String, Object> param) {
        return baseMapper.queryAmEmploymentResign(param);
    }

    @Override
    @Transactional(
        rollbackFor = {Exception.class}
    )
    public com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult xlsImportAmEmpAdvance(List<AmEmpArchiveAdvanceXsl> opts, String fileName) {

        StringBuffer retStr = new StringBuffer();

        for (AmEmpArchiveAdvanceXsl xsl:opts) {
            System.out.println(xsl);
            Wrapper<AmEmpEmployee> wrapper = new EntityWrapper<>();
            wrapper.eq("employee_name",xsl.getEmploymentName());
            wrapper.eq("id_num",xsl.getIdNum());
            // 是否有雇员
            List<AmEmpEmployee> empEmployeeList = amEmpEmployeeService.selectList(wrapper);
            if(empEmployeeList == null || empEmployeeList.size() == 0) {
                retStr.append(xsl.getEmploymentName() + xsl.getIdNum() + "这个雇员在系统中不存在！ ");
                continue;
            }
            AmEmpEmployee employee = empEmployeeList.get(0);
            Wrapper<AmEmployment> wrapper2 = new EntityWrapper<>();
            wrapper2.eq("employment_id",xsl.getMatchEmployIndex());
            wrapper2.eq("employee_id",employee.getEmployeeId());
            List<AmEmployment> empList = amEmploymentMapper.selectList(wrapper2);
            if(empList == null || empList.size() == 0) {
                retStr.append(xsl.getEmploymentName() +" "+ xsl.getIdNum() + "这个雇员对应的用工序号 " + xsl.getMatchEmployIndex() + " 不匹配系统的用工序号！ ");
                continue;
            }
            AmEmployment amEmployment = empList.get(0);
            Wrapper<AmArchive> wrapper3 = new EntityWrapper<>();
            wrapper3.eq("employee_id",employee.getEmployeeId());
            wrapper3.eq("company_id",employee.getCompanyId());
            wrapper3.eq("employment_id",amEmployment.getEmploymentId());
            List<AmArchive> archives = amArchiveMapper.selectList(wrapper3);
            if(archives!=null && archives.size()>0){
                AmArchive amArchive = archives.get(0);
                amArchive.setDocType(xsl.getDocType());
                amArchive.setDocNum(xsl.getDocNum());
                amArchive.setDocFrom(xsl.getDocFrom());
                amArchive.setArchivePlace(xsl.getArchivePlace());
                amArchive.setStorageDate(DateUtil.dateToLocaleDate(xsl.getCreatedDate()));
                amArchive.setLuyongHandleEnd(xsl.getEmployHandleEnd());
                amArchive.setModifiedBy(UserContext.getUser().getDisplayName());
                amArchive.setModifiedTime(LocalDateTime.now());
                amArchiveMapper.updateById(amArchive);
            }else{
                AmArchive amArchive = new AmArchive();
                amArchive.setCompanyId(employee.getCompanyId());
                amArchive.setEmployeeId(xsl.getEmployeeId());
                amArchive.setEmploymentId(xsl.getMatchEmployIndex());
                amArchive.setDocType(xsl.getDocType());
                amArchive.setDocNum(xsl.getDocNum());
                amArchive.setDocFrom(xsl.getDocFrom());
                amArchive.setArchivePlace(xsl.getArchivePlace());
                amArchive.setStorageDate(DateUtil.dateToLocaleDate(xsl.getCreatedDate()));
                amArchive.setLuyongHandleEnd(xsl.getEmployHandleEnd());
                amArchive.setCreatedBy(UserContext.getUser().getDisplayName());
                amArchive.setModifiedBy(UserContext.getUser().getDisplayName());
                amArchive.setCreatedTime(LocalDateTime.now());
                amArchive.setModifiedTime(LocalDateTime.now());
                amArchiveMapper.insert(amArchive);
            }
            AmRemark amRemark = new AmRemark();
            amRemark.setCreatedBy(UserContext.getUserId());
            amRemark.setModifiedBy(UserContext.getUserId());
            amRemark.setCreatedTime(LocalDateTime.now());
            amRemark.setModifiedTime(LocalDateTime.now());
            amRemark.setRemarkContent(xsl.getDocRemark());
            amRemark.setRemarkMan(UserContext.getUser().getDisplayName());
            amRemark.setRemarkDate(LocalDate.now());
            amRemark.setEmpTaskId(employee.getEmpTaskId());
            amRemark.setRemarkType(2);
            amRemark.setActive(true);
            amRemarkService.insert(amRemark);
        }
        if (retStr.toString()=="") {
            return JsonResultKit.of(0, "导入成功！");
        } else {
            return JsonResultKit.of(1, "部分导入失败!原因： \n" + retStr.toString());
        }
    }

    @Override
    public List<AmEmploymentBO> queryAmEmploymentBatch(List<Long> empTaskIds) {
        EmployeeBatchBO employeeBatchBO = new  EmployeeBatchBO();
        employeeBatchBO.setEmpTaskIds(empTaskIds);
        return baseMapper.queryAmEmploymentBatch(employeeBatchBO);
    }

    @Override
    public AmEmpTaskCollection queryArchiveTaskCount(AmEmploymentBO amEmploymentBO) {
        List<String> param = new ArrayList<String>();

        if(!StringUtil.isEmpty(amEmploymentBO.getParams()))
        {
            String arr[] = amEmploymentBO.getParams().split(",");
            for(int i=0;i<arr.length;i++) {
                param.add(arr[i]);
            }
        }

        amEmploymentBO.setParam(param);
        AmEmpTaskCollection amEmpTaskCollection = new AmEmpTaskCollection();
        List<AmEmploymentBO> list = baseMapper.queryArchiveTaskCount(amEmploymentBO);
        AmArchiveStatusBO bo = new AmArchiveStatusBO();
        Integer handleEnd = 0;
        Integer noHandleEnd = 0;
        if(null!=list&&list.size()>0)
        {
            for(AmEmploymentBO amEmploymentBO1:list){
                if(null!=amEmploymentBO1.getLuyongHandleEnd()&&amEmploymentBO1.getLuyongHandleEnd()){
                    handleEnd = amEmploymentBO1.getCount();
                }else{
                    noHandleEnd = noHandleEnd + amEmploymentBO1.getCount();
                }
            }
        }
        bo.setHandleEnd(handleEnd);
        bo.setNoHandleEnd(noHandleEnd);
        amEmpTaskCollection.setAmArchiveStatusBO(bo);
        AmTaskStatusBO amTaskStatusBO = new AmTaskStatusBO();
        List<AmEmploymentBO> secondList = baseMapper.queryTaskCount(amEmploymentBO);
        Integer job = 0;
        Integer noJob = 0;
        if(null!=secondList&&secondList.size()>0){
            for(AmEmploymentBO temp:secondList){
                if("Y".equals(temp.getJob())){
                    job = job + temp.getCount();
                }else{
                    noJob = noJob + temp.getCount();
                }
            }
        }
        amTaskStatusBO.setNoJob(noJob);
        amTaskStatusBO.setJob(job);
        amEmpTaskCollection.setAmTaskStatusBO(amTaskStatusBO);
        return amEmpTaskCollection;
    }

    @Override
    public List<AmArchiveReturnPrintDTO> queryAmArchiveForeignerPritDate(PageInfo pageInfo) {
        PageRows<AmEmploymentBO> boResult = queryAmArchive(pageInfo);
        List<AmArchiveReturnPrintDTO> list = new ArrayList<>();
        List<AmEmploymentBO> data = boResult.getRows();
        for (AmEmploymentBO bo:data) {
            AmArchiveReturnPrintDTO dto = new AmArchiveReturnPrintDTO();
            dto.setDocType(bo.getDocType());
            dto.setDocNum(bo.getDocNum());
            dto.setEmployeeId(bo.getEmployeeId());// 雇员ID
            dto.setEmployeeName(bo.getEmployeeName());// 雇员姓名
            dto.setGender(bo.getGender());// 姓别
            dto.setStartDate(new Date());// 合同开始时间
            dto.setEmployStyle(bo.getEmployStyle());
            dto.setEndDate(new Date());// 合同终止时间
            dto.setEndType(bo.getEndType());// 合同终止类型
            dto.setIdNum(bo.getIdNum());// 身份证号
            com.ciicsh.gto.employeecenter.apiservice.api.dto.JsonResult<ResidentInfoDTO> residen
                = employeeInfoProxy.getEmpResidentDetailInfo(bo.getEmployeeId());
            if(residen.getData()!=null){
                BeanUtils.copyProperties(residen.getData(),dto);
            }
            JsonResult<HfEmpInfoDTO> hfResult = fundApiProxy.getHfEmpInfoById(bo.getCompanyId(),bo.getEmployeeId());
            if(hfResult.getData()!=null){
               dto.setHfAccount(hfResult.getData().getHfEmpAccount());// 公积金
               dto.setHfAccountBC(hfResult.getData().getHfEmpAccountBC());// 补充公积金
            }
            dto.setOperationName(UserContext.getUser().getDisplayName());
            dto.setOperationDate(new Date());// 退工日期
            dto.setMobile("54594545");
            dto.setIfLaborManualReturnStr(1);// 是否被交退人员
            list.add(dto);
        }

        return list;
    }
}
