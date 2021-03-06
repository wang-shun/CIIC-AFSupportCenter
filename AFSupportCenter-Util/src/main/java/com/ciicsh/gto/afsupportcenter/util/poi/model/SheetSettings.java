package com.ciicsh.gto.afsupportcenter.util.poi.model;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.*;

/**
 * The type Sheet settings.
 *
 * @author wujinglei
 * @ClassName: SheetSettings
 * @Description: 表配置
 * @date 2014年6月11日 上午10:36:28
 */
public final class SheetSettings {

    /**
     * 跳过条数
     */
    private Integer skipRows;

    /**
     * 表名
     */
    private String sheetName;

    /**
     * 表序号
     */
    private Integer sheetSeq;

    /**
     * 标题
     */
    private String title;

    /**
     * 标题样式
     */
    private CellStyleSettings titleStyle;

    /**
     * 列设置
     */
    private List<CellSettings> cellSettingsList;

    /**
     * 导出的数据
     */
    private List exportData;

    /**
     * 数据class类型
     */
    private Class dataClazzType;

    private Integer cellCount;

    private Map<String,String> cellAddressMap = new HashMap();

    /**
     * The Select target set.
     */
    public Set<String> selectTargetSet = new HashSet<String>();

    /**
     * @author: wujinglei
     * @date: 2014年6月11日 上午10:40:27
     * @Description:
     */
    @SuppressWarnings("unused")
    private SheetSettings() {

    }

    /**
     * Instantiates a new Sheet settings.
     *
     * @param sheetSeq the sheet seq
     * @param skipRows the skip rows
     * @author: wujinglei
     * @date: 2014年6月11日 上午10:40:41
     * @Description:强制序号及忽略行数
     */
    public SheetSettings(Integer sheetSeq, Integer skipRows) {
        this.sheetSeq = sheetSeq;
        this.skipRows = skipRows;
    }

    /**
     * Instantiates a new Sheet settings.
     *
     * @param sheetName the sheet name
     */
    public SheetSettings(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Instantiates a new Sheet settings.
     *
     * @param sheetName     the sheet name
     * @param dataClazzType the dataClazzType
     */
    public SheetSettings(String sheetName, Class dataClazzType) {
        this.sheetName = sheetName;
        this.dataClazzType = dataClazzType;
    }

    /**
     * Instantiates a new Sheet settings.
     *
     * @param sheetName the sheet name
     * @param sheetSeq  the sheet seq
     * @param skipRows  the skip rows 强制序号及忽略行数
     */
    public SheetSettings(String sheetName, Integer sheetSeq, Integer skipRows) {
        this.sheetName = sheetName;
        this.sheetSeq = sheetSeq;
        this.skipRows = skipRows;
    }

    /**
     * Instantiates a new Sheet settings.
     *
     * @param sheetName     the sheet name
     * @param sheetSeq      the sheet seq
     * @param skipRows      the skip rows
     * @param dataClazzType the dataClazzType
     * @author: wujinglei
     * @date: 2014 -6-21 下午4:53:51
     * @Description:按表名来构造(导入时用)
     */
    public SheetSettings(String sheetName, Integer sheetSeq, Integer skipRows, Class dataClazzType) {
        this.sheetName = sheetName;
        this.sheetSeq = sheetSeq;
        this.skipRows = skipRows;
        this.dataClazzType = dataClazzType;
    }

    /**
     * Instantiates a new Sheet settings.
     *
     * @param sheetName     the sheet name
     * @param exportData    the export data
     * @param dataClazzType the data clazz type
     * @author: wujinglei
     * @date: 2014年6月11日 下午4:02:50
     * @Description:(导出时用)
     */
    public SheetSettings(String sheetName, List exportData, Class dataClazzType) {
        this.sheetName = sheetName;
        this.exportData = exportData;
        this.dataClazzType = dataClazzType;
    }

    /**
     * Add title sheet settings.
     *
     * @param title      the title
     * @param titleStyle the title style
     * @return the sheet settings
     */
    public SheetSettings addTitle(String title, CellStyleSettings titleStyle) {
        this.title = title;
        this.titleStyle = titleStyle;
        return this;
    }

    /**
     * Add title sheet settings.
     *
     * @param title the title
     * @return the sheet settings
     */
    public SheetSettings addTitle(String title) {
        this.title = title;
        CellStyleSettings cellStyleSettings = new CellStyleSettings();
        cellStyleSettings.setTitleFont("宋体");
        cellStyleSettings.setTitleSize((short) 20);
        cellStyleSettings.setTitleFontColor(IndexedColors.BLUE.getIndex());
        cellStyleSettings.setAlignment(HorizontalAlignment.CENTER);
        cellStyleSettings.setVerticalAlignment(VerticalAlignment.CENTER);
        this.titleStyle = cellStyleSettings;
        return this;
    }

    /**
     * Gets sheet name.
     *
     * @return the sheetName
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * Sets sheet name.
     *
     * @param sheetName the sheetName to set
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Gets sheet seq.
     *
     * @return the sheetSeq
     */
    public Integer getSheetSeq() {
        return sheetSeq;
    }

    /**
     * Sets sheet seq.
     *
     * @param sheetSeq the sheetSeq to set
     */
    public void setSheetSeq(Integer sheetSeq) {
        this.sheetSeq = sheetSeq;
    }

    /**
     * Set cell settings.
     *
     * @param list the list
     */
    public void setCellSettings(List<CellSettings> list) {
        this.cellSettingsList = list;
    }

    public void setCellSettings(CellSettings[] arrays) {
        this.cellSettingsList = new ArrayList<CellSettings>(Arrays.asList(arrays));
    }

    /**
     * Gets skip rows.
     *
     * @return the skipRows
     */
    public Integer getSkipRows() {
        return skipRows;
    }

    /**
     * Sets skip rows.
     *
     * @param skipRows the skipRows to set
     */
    public void setSkipRows(Integer skipRows) {
        this.skipRows = skipRows;
    }

    /**
     * Sets export data.
     *
     * @param exportData the exportData to set
     */
    public void setExportData(List exportData) {
        this.exportData = exportData;
    }

    @Override
    public String toString() {
        return "SheetSettings{" +
                "skipRows=" + skipRows +
                ", sheetName='" + sheetName + '\'' +
                ", sheetSeq=" + sheetSeq +
                ", title='" + title + '\'' +
                ", titleStyle=" + titleStyle +
                ", cellSettingsList=" + cellSettingsList +
                ", exportData=" + exportData +
                ", dataClazzType=" + dataClazzType +
                ", cellCount=" + cellCount +
                ", cellAddressMap=" + cellAddressMap +
                ", selectTargetSet=" + selectTargetSet +
                '}';
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets title style.
     *
     * @return the title style
     */
    public CellStyleSettings getTitleStyle() {
        return titleStyle;
    }

    /**
     * Gets data clazz type.
     *
     * @return the dataClazzType
     */
    public Class getDataClazzType() {
        return dataClazzType;
    }

    /**
     * Gets cell address map.
     *
     * @return the cell address map
     */
    public Map<String, String> getCellAddressMap() {
        return cellAddressMap;
    }

    /**
     * Sets cell address map.
     *
     * @param cellAddressMap the cell address map
     */
    public void setCellAddressMap(Map<String, String> cellAddressMap) {
        this.cellAddressMap = cellAddressMap;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets title style.
     *
     * @param titleStyle the title style
     */
    public void setTitleStyle(CellStyleSettings titleStyle) {
        this.titleStyle = titleStyle;
    }

    /**
     * Gets cell settings list.
     *
     * @return the cell settings list
     */
    public List<CellSettings> getCellSettingsList() {
        return cellSettingsList;
    }

    /**
     * Sets cell settings list.
     *
     * @param cellSettingsList the cell settings list
     */
    public void setCellSettingsList(List<CellSettings> cellSettingsList) {
        this.cellSettingsList = cellSettingsList;
    }

    /**
     * Gets export data.
     *
     * @return the export data
     */
    public List getExportData() {
        return exportData;
    }

    /**
     * Sets data clazz type.
     *
     * @param dataClazzType the data clazz type
     */
    public void setDataClazzType(Class dataClazzType) {
        this.dataClazzType = dataClazzType;
    }

    /**
     * Gets cell count.
     *
     * @return the cell count
     */
    public Integer getCellCount() {
        return cellCount;
    }

    /**
     * Sets cell count.
     *
     * @param cellCount the cell count
     */
    public void setCellCount(Integer cellCount) {
        this.cellCount = cellCount;
    }

    /**
     * Gets select target set.
     *
     * @return the select target set
     */
    public Set<String> getSelectTargetSet() {
        return selectTargetSet;
    }

    /**
     * Sets select target set.
     *
     * @param selectTargetSet the select target set
     */
    public void setSelectTargetSet(Set<String> selectTargetSet) {
        this.selectTargetSet = selectTargetSet;
    }
}
