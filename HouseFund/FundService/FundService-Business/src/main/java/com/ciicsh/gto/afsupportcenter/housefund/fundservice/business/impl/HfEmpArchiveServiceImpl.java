package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfArchiveBasePeriodBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfComAccountBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpArchiveBo;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.HfEmpComBO;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.HfEmpArchiveService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dao.HfEmpArchiveMapper;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto.EmpAccountImpXsl;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.HfEmpArchive;
import com.ciicsh.gto.afsupportcenter.util.page.PageInfo;
import com.ciicsh.gto.afsupportcenter.util.page.PageKit;
import com.ciicsh.gto.afsupportcenter.util.page.PageRows;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResult;
import com.ciicsh.gto.afsupportcenter.util.web.response.JsonResultKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 雇员本地公积金档案主表,
 * 由中智代缴过社保的雇员在此表必有一条记录，如果雇员跳槽到另外一家客户，就会在此表产 服务实现类
 * </p>
 */
@Service
public class HfEmpArchiveServiceImpl extends ServiceImpl<HfEmpArchiveMapper, HfEmpArchive> implements HfEmpArchiveService {

    public PageRows<HfEmpArchiveBo> queryEmpArchive(PageInfo pageInfo) {
        HfEmpArchiveBo dto = pageInfo.toJavaObject(HfEmpArchiveBo.class);
        return PageKit.doSelectPage(pageInfo, () -> baseMapper.queryEmpArchive(dto));
    }

    public Map<String, Object> viewEmpArchiveInfo(String empArchiveId, String companyId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        HfEmpArchiveBo viewEmpArchiveBo = baseMapper.viewEmpArchive(empArchiveId);
        HfArchiveBasePeriodBo viewEmpPeriodBo = baseMapper.viewEmpPeriod(empArchiveId, "1");//基本
        HfArchiveBasePeriodBo viewEmpPeriodAddBo = baseMapper.viewEmpPeriod(empArchiveId, "2");//补充
        HfComAccountBo viewComAccountBo = baseMapper.viewComAccount(companyId);
        List listEmpTaskPeriodBo = baseMapper.listEmpTaskPeriod(empArchiveId, "1");//基本
        List listEmpTaskPeriodAddBo = baseMapper.listEmpTaskPeriod(empArchiveId, "2");//补充
        List listEmpTransferBo = baseMapper.listEmpTransfer(empArchiveId);

        HfEmpComBO hfEmpComBO = baseMapper.fetchManager(companyId, viewEmpArchiveBo.getEmployeeId());
        org.springframework.beans.BeanUtils.copyProperties(hfEmpComBO, viewComAccountBo);
        resultMap.put("viewEmpArchive", viewEmpArchiveBo);
        resultMap.put("viewEmpPeriod", viewEmpPeriodBo);
        resultMap.put("viewEmpPeriodAdd", viewEmpPeriodAddBo);
        resultMap.put("viewComAccount", viewComAccountBo);
        resultMap.put("listEmpTaskPeriod", listEmpTaskPeriodBo);
        resultMap.put("listEmpTaskPeriodAdd", listEmpTaskPeriodAddBo);
        resultMap.put("listEmpTransfer", listEmpTransferBo);
        return resultMap;
    }

    public boolean saveComAccount(Map<String, String> updateDto) {
        try {
            baseMapper.updateArchiveEmpAccount(updateDto.get("hfEmpAccount"), Long.valueOf(updateDto.get("empArchiveId")));
            if (Optional.ofNullable(updateDto.get("empArchiveIdBc")).isPresent()) {
                baseMapper.updateArchiveEmpAccount(updateDto.get("hfEmpAccountBc"), Long.valueOf(updateDto.get("empArchiveIdBc")));
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public JsonResult xlsImportEmpAccount(List<EmpAccountImpXsl> xls, String fileName) {
        StringBuffer retStr = new StringBuffer();
        int type = 0;
        try {
            for (EmpAccountImpXsl xlsRecord : xls) {
                if (StringUtils.isBlank(xlsRecord.getEmpAccount()) || xlsRecord.getEmpAccount().length() > 20) {
                    type = 1;
                    retStr.append(xlsRecord.getEmpName());
                    break;
                }
                Map map = baseMapper.selectEmpByCardIdAndName(xlsRecord.getEmpName(), xlsRecord.getIdNum());
                if (map == null) {
                    type = 2;
                    retStr.append(xlsRecord.getEmpName());
                    break;
                }
                HfEmpArchive hfEmpArchive = new HfEmpArchive();
                hfEmpArchive.setHfEmpAccount(xlsRecord.getEmpAccount());
                hfEmpArchive.setEmpArchiveId((Long) map.get("emp_archive_id"));
                baseMapper.updateById(hfEmpArchive);
            }
        } catch (Exception e) {
            type = 3;
        }
        String ret = "";
        switch (type) {
            case 1:
                ret = retStr.toString() + "，导入的公积金账号为空或者数字超过长度。";
                break;
            case 2:
                ret = retStr.toString() + "，根据身份证号和姓名无法从系统中找到对应的雇员。";
                break;
            case 3:
                ret = "保存导出数据是发生异常！";
                break;
        }
        if (type == 0) {
            return JsonResultKit.of(0, "导入成功！");
        } else {
            return JsonResultKit.of(1, "导入失败!原因： \n" + ret);
        }
    }

    @Override
    public int deleteHfEmpArchiveByEmpTaskIds(List<Long> empTaskIdList) {
        return baseMapper.deleteHfEmpArchiveByEmpTaskIds(empTaskIdList);
    }

    @Override
    public Map queryHfEmpArchiveByEmpTaskIds(List<Long> empTaskIdList) {
        return baseMapper.queryHfEmpArchiveByEmpTaskIds(empTaskIdList);
    }

    @Override
    public String getEmpAccountByEmployeeId(String employeeId, Integer hfType) {
        return baseMapper.getEmpAccountByEmployeeId(employeeId, hfType);
    }
}
