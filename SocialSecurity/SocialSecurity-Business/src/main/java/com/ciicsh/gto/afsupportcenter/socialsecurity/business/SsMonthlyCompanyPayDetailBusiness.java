package com.ciicsh.gto.afsupportcenter.socialsecurity.business;

import com.ciicsh.gto.afsupportcenter.socialsecurity.dao.SsMonthlyCompanyPayDetailMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.entity.SsMonthlyCompanyPayDetail;
import com.ciicsh.gto.afsupportcenter.util.business.Business;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SsMonthlyCompanyPayDetailBusiness extends Business<SsMonthlyCompanyPayDetail, SsMonthlyCompanyPayDetailMapper> {


}
