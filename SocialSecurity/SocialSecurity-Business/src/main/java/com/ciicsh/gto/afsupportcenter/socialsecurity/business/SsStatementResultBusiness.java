package com.ciicsh.gto.afsupportcenter.socialsecurity.business;

import com.ciicsh.gto.afsupportcenter.socialsecurity.dao.SsStatementResultMapper;
import com.ciicsh.gto.afsupportcenter.socialsecurity.entity.SsStatementResult;
import com.ciicsh.gto.afsupportcenter.util.business.Business;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SsStatementResultBusiness extends Business<SsStatementResult, SsStatementResultMapper> {

}
