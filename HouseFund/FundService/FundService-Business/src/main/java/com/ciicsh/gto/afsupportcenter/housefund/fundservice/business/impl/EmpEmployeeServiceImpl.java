package com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.EmpEmployeeService;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.dao.EmpEmployeeMapper;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.entity.EmpEmployee;
import org.springframework.stereotype.Service;

@Service
public class EmpEmployeeServiceImpl extends ServiceImpl<EmpEmployeeMapper, EmpEmployee> implements EmpEmployeeService{
}
