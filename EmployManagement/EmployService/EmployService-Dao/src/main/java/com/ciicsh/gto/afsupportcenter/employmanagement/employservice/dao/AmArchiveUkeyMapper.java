package com.ciicsh.gto.afsupportcenter.employmanagement.employservice.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.bo.AmArchiveUkeyBO;
import com.ciicsh.gto.afsupportcenter.employmanagement.employservice.entity.AmArchiveUkey;

import java.util.List;

/**
 * <p>
 * 档案Ukey表
 * </p>
 */
public interface AmArchiveUkeyMapper extends BaseMapper<AmArchiveUkey> {

     List<AmArchiveUkey> queryUkeyList(AmArchiveUkeyBO bo);
}
