package com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.bo;

import com.ciicsh.gto.afsupportcenter.socialsecurity.soccommandservice.entity.SsMonthEmpChangeDetail;

import java.math.BigDecimal;

/**
 * <p>
 * 雇员月度变更表明细
该表结果有可能需要调整
 * </p>
 *
 * @author wengxk
 * @since 2017-12-13
 */
public class SsMonthEmpChangeDetailBO extends SsMonthEmpChangeDetail {

    private static final long serialVersionUID = 1L;


    /**
     * 员工名
     */
    private String employeeName;

	/*--------页面展示字段----------------------------------------------------------*/

	/*-------养老保险-Pension-------*/
    /**
     * 企业金额-养老保险
     */
    private BigDecimal comAmountPension;
    /**
     * 雇员金额-养老保险
     */
    private BigDecimal empAmountPension;
    /**
     * 企业补缴金额-养老保险
     */
    private BigDecimal comCompensatedAmountPension;
    /**
     * 雇员补缴金额-养老保险
     */
    private BigDecimal empCompensatedAmountPension;
    /**
     * 一次性支付-养老保险
     */
    private BigDecimal onePaymentPension;

	/*-------医疗保险-Medical Insurance-------*/
    /**
     * 企业金额-医疗保险
     */
    private BigDecimal comAmountMedical;
    /**
     * 雇员金额-医疗保险
     */
    private BigDecimal empAmountMedical;
    /**
     * 企业补缴金额-医疗保险
     */
    private BigDecimal comCompensatedAmountMedical;
    /**
     * 雇员补缴金额-医疗保险
     */
    private BigDecimal empCompensatedAmountMedical;

	/*-------失业保险-Unemployment Insurance-------*/
    /**
     * 企业金额-失业保险
     */
    private BigDecimal comAmountUnemployment;
    /**
     * 雇员金额-失业保险
     */
    private BigDecimal empAmountUnemployment;
    /**
     * 企业补缴金额-失业保险
     */
    private BigDecimal comCompensatedAmountUnemployment;
    /**
     * 雇员补缴金额-失业保险
     */
    private BigDecimal empCompensatedAmountUnemployment;

	/*-------工伤保险-Work-related Accident Insurance-------*/
    /**
     * 企业金额-工伤保险
     */
    private BigDecimal comAmountAccident;
    /**
     * 雇员金额-工伤保险
     */
    private BigDecimal empAmountAccident;
    /**
     * 企业补缴金额-工伤保险
     */
    private BigDecimal comCompensatedAmountAccident;
    /**
     * 雇员补缴金额-工伤保险
     */
    private BigDecimal empCompensatedAmountAccident;

	/*-------生育保险-Maternity-------*/
    /**
     * 企业金额-生育保险
     */
    private BigDecimal comAmountMaternity;
    /**
     * 雇员金额-生育保险
     */
    private BigDecimal empAmountMaternity;
    /**
     * 企业补缴金额-生育保险
     */
    private BigDecimal comCompensatedAmountMaternity;
    /**
     * 雇员补缴金额-生育保险
     */
    private BigDecimal empCompensatedAmountMaternity;







    public BigDecimal getComAmountPension() {
        return comAmountPension;
    }

    public void setComAmountPension(BigDecimal comAmountPension) {
        this.comAmountPension = comAmountPension;
    }

    public BigDecimal getEmpAmountPension() {
        return empAmountPension;
    }

    public void setEmpAmountPension(BigDecimal empAmountPension) {
        this.empAmountPension = empAmountPension;
    }

    public BigDecimal getComCompensatedAmountPension() {
        return comCompensatedAmountPension;
    }

    public void setComCompensatedAmountPension(BigDecimal comCompensatedAmountPension) {
        this.comCompensatedAmountPension = comCompensatedAmountPension;
    }

    public BigDecimal getEmpCompensatedAmountPension() {
        return empCompensatedAmountPension;
    }

    public void setEmpCompensatedAmountPension(BigDecimal empCompensatedAmountPension) {
        this.empCompensatedAmountPension = empCompensatedAmountPension;
    }

    public BigDecimal getOnePaymentPension() {
        return onePaymentPension;
    }

    public void setOnePaymentPension(BigDecimal onePaymentPension) {
        this.onePaymentPension = onePaymentPension;
    }

    public BigDecimal getComAmountMedical() {
        return comAmountMedical;
    }

    public void setComAmountMedical(BigDecimal comAmountMedical) {
        this.comAmountMedical = comAmountMedical;
    }

    public BigDecimal getEmpAmountMedical() {
        return empAmountMedical;
    }

    public void setEmpAmountMedical(BigDecimal empAmountMedical) {
        this.empAmountMedical = empAmountMedical;
    }

    public BigDecimal getComCompensatedAmountMedical() {
        return comCompensatedAmountMedical;
    }

    public void setComCompensatedAmountMedical(BigDecimal comCompensatedAmountMedical) {
        this.comCompensatedAmountMedical = comCompensatedAmountMedical;
    }

    public BigDecimal getEmpCompensatedAmountMedical() {
        return empCompensatedAmountMedical;
    }

    public void setEmpCompensatedAmountMedical(BigDecimal empCompensatedAmountMedical) {
        this.empCompensatedAmountMedical = empCompensatedAmountMedical;
    }

    public BigDecimal getComAmountUnemployment() {
        return comAmountUnemployment;
    }

    public void setComAmountUnemployment(BigDecimal comAmountUnemployment) {
        this.comAmountUnemployment = comAmountUnemployment;
    }

    public BigDecimal getEmpAmountUnemployment() {
        return empAmountUnemployment;
    }

    public void setEmpAmountUnemployment(BigDecimal empAmountUnemployment) {
        this.empAmountUnemployment = empAmountUnemployment;
    }

    public BigDecimal getComCompensatedAmountUnemployment() {
        return comCompensatedAmountUnemployment;
    }

    public void setComCompensatedAmountUnemployment(BigDecimal comCompensatedAmountUnemployment) {
        this.comCompensatedAmountUnemployment = comCompensatedAmountUnemployment;
    }

    public BigDecimal getEmpCompensatedAmountUnemployment() {
        return empCompensatedAmountUnemployment;
    }

    public void setEmpCompensatedAmountUnemployment(BigDecimal empCompensatedAmountUnemployment) {
        this.empCompensatedAmountUnemployment = empCompensatedAmountUnemployment;
    }

    public BigDecimal getComAmountAccident() {
        return comAmountAccident;
    }

    public void setComAmountAccident(BigDecimal comAmountAccident) {
        this.comAmountAccident = comAmountAccident;
    }

    public BigDecimal getEmpAmountAccident() {
        return empAmountAccident;
    }

    public void setEmpAmountAccident(BigDecimal empAmountAccident) {
        this.empAmountAccident = empAmountAccident;
    }

    public BigDecimal getComCompensatedAmountAccident() {
        return comCompensatedAmountAccident;
    }

    public void setComCompensatedAmountAccident(BigDecimal comCompensatedAmountAccident) {
        this.comCompensatedAmountAccident = comCompensatedAmountAccident;
    }

    public BigDecimal getEmpCompensatedAmountAccident() {
        return empCompensatedAmountAccident;
    }

    public void setEmpCompensatedAmountAccident(BigDecimal empCompensatedAmountAccident) {
        this.empCompensatedAmountAccident = empCompensatedAmountAccident;
    }

    public BigDecimal getComAmountMaternity() {
        return comAmountMaternity;
    }

    public void setComAmountMaternity(BigDecimal comAmountMaternity) {
        this.comAmountMaternity = comAmountMaternity;
    }

    public BigDecimal getEmpAmountMaternity() {
        return empAmountMaternity;
    }

    public void setEmpAmountMaternity(BigDecimal empAmountMaternity) {
        this.empAmountMaternity = empAmountMaternity;
    }

    public BigDecimal getComCompensatedAmountMaternity() {
        return comCompensatedAmountMaternity;
    }

    public void setComCompensatedAmountMaternity(BigDecimal comCompensatedAmountMaternity) {
        this.comCompensatedAmountMaternity = comCompensatedAmountMaternity;
    }

    public BigDecimal getEmpCompensatedAmountMaternity() {
        return empCompensatedAmountMaternity;
    }

    public void setEmpCompensatedAmountMaternity(BigDecimal empCompensatedAmountMaternity) {
        this.empCompensatedAmountMaternity = empCompensatedAmountMaternity;
    }


    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}