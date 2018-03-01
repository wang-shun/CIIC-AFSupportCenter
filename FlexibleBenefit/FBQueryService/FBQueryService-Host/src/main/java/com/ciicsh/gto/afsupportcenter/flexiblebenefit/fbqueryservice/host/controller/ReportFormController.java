package com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice.host.controller;

import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.company.AfProductWithCompanyDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.dto.request.AfProductParamsDTO;
import com.ciicsh.gto.afcompanycenter.queryservice.api.proxy.AfProductPublicProxy;
import com.ciicsh.gto.afsupportcenter.flexiblebenefit.entity.dto.ExpCompanyDTO;
import com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice.host.utils.ExportUtils;
import com.ciicsh.gto.afsupportcenter.util.result.JsonResult;
import com.ciicsh.gto.salecenter.apiservice.api.dto.LinkmanDTO;
import com.ciicsh.gto.salecenter.apiservice.api.proxy.SalLinkmanProxy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: guwei
 * @Description:
 * @Date: Created in 15:41 2018/2/24
 */
@RestController
@RequestMapping("/api/reportform")
public class ReportFormController {

    @Autowired
    private AfProductPublicProxy afProductPublicProxy;

    @Autowired
    private SalLinkmanProxy salLinkmanProxy;

    /**
     * 获取参加活动公司清单
     * @return
     */
    @GetMapping("/get")
    public JsonResult getCompanyPage(String companyId, String companyName,
                                     String majordomo, String manager,
                                     String productId, HttpServletResponse response){
//        AfProductParamsDTO afProductParamsDTO = new AfProductParamsDTO();
//        afProductParamsDTO.setCompanyId(companyId);
//        afProductParamsDTO.setProductId(productId);
//        List<AfProductWithCompanyDTO> productWithCompany = afProductPublicProxy.getProductWithCompany(afProductParamsDTO);
//        List<ExpCompanyDTO> expCompanyDTOS = new ArrayList<>();
//        productWithCompany.stream().forEach( i -> {
//            ExpCompanyDTO expCompanyDTO = new ExpCompanyDTO();
//            BeanUtils.copyProperties(i,expCompanyDTO);
//            LinkmanDTO linkmanDTO = new LinkmanDTO();
//            linkmanDTO.setCompanyName(i.getTitle());
//            linkmanDTO = salLinkmanProxy.getContacts(linkmanDTO).getObject().get(0);
//            BeanUtils.copyProperties(linkmanDTO,expCompanyDTO);
//            expCompanyDTOS.add(expCompanyDTO);
//        });
        List<String> names = new ArrayList<>();
        names.add("客户经理");
        names.add("客户总监");
        names.add("公司编号");
        names.add("公司名称");
        names.add("公司联系人");
        names.add("电话");
        names.add("地址");
        names.add("邮编");
        names.add("服务产品");
        names.add("人数");
        names.add("电子邮箱");
        names.add("手机");
        names.add("传真号码");
        List<String> fields = new ArrayList<>();
        fields.add("manager");
        fields.add("majordomo");
        fields.add("companyId");
        fields.add("title");
        fields.add("linkmanName");
        fields.add("telNum");
        fields.add("address");
        fields.add("postCode");
        fields.add("productName");
        fields.add("count");
        fields.add("email");
        fields.add("mobile");
        fields.add("telautogramNum");

        List<ExpCompanyDTO> expCompanyDTOS = new ArrayList<>();
        ExpCompanyDTO e = new ExpCompanyDTO();
        e.setAddress("地址");
        e.setTitle("公司");
        expCompanyDTOS.add(e);
        ExportUtils.exportExcel("Company表单.xlsx",names,fields,expCompanyDTOS, response);
        return JsonResult.success(null);
    }

}
