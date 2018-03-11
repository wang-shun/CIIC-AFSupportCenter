package com.ciicsh.gto.afsupportcenter.housefund.fundservice.dto;

public class ComFundAccountNameDTO
{
    /**
     * 企业账户Id
     */
    private int comAccountId;

    /**
     * 企业账户名称
     */
    private String comAccountName;

    /**
     * 企业账户类型，1 大库 2 外包 3 独立户
     */
    private Byte hfAccountType;


    public int getComAccountId() {
        return comAccountId;
    }

    public void setComAccountId(int comAccountId) {
        this.comAccountId = comAccountId;
    }

    public String getComAccountName() {
        return comAccountName;
    }

    public void setComAccountName(String comAccountName) {
        this.comAccountName = comAccountName;
    }

    public Byte getHfAccountType() {
        return hfAccountType;
    }

    public void setHfAccountType(Byte hfAccountType) {
        this.hfAccountType = hfAccountType;
    }
}
