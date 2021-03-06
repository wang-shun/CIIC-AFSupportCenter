package com.ciicsh.gto.afsupportcenter.socialsecurity.socservice.entity.custom;



import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by houwanhua on 2018/1/22.
 */
@ExcelTarget("TestPerson")
public class TestPerson implements Serializable {

    private static final long serialVersionUID = 2347992043559803236L;

    @Excel(name = "姓名", orderNum = "0")
    private String name;

    @Excel(name = "性别", replace = {"男_1", "女_2"}, orderNum = "1")
    private String sex;

    @Excel(name = "生日", format = "yyyy-MM-dd", orderNum = "2")
    private Date birthday;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
