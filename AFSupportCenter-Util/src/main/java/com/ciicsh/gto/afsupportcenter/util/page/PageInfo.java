package com.ciicsh.gto.afsupportcenter.util.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.ciicsh.gto.afsupportcenter.util.kit.JsonKit;

import java.io.Serializable;
import java.util.Map;

/**
 * 分页参数类
 */
public class PageInfo implements Serializable {
    // 第几页
    private Integer pageNum;
    // 查询多少条
    private Integer pageSize;
    // 排序方式
    private String orderBy;
    // 排序
    private boolean isAsc;

    // filterEmpty true 过滤空字符串
    private boolean filterEmpty = true;

    // 参数对象，方便转成其他对象
    private JSONObject params = new JSONObject();

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public void setAsc(boolean asc) {
        isAsc = asc;
    }

    public boolean isFilterEmpty() {
        return filterEmpty;
    }

    public void setFilterEmpty(boolean filterEmpty) {
        this.filterEmpty = filterEmpty;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
            "pageNum=" + pageNum +
            ", pageSize=" + pageSize +
            ", orderBy='" + orderBy + '\'' +
            ", isAsc=" + isAsc +
            ", filterEmpty=" + filterEmpty +
            ", params=" + params +
            '}';
    }

    public <V> V put(String key, V value) {
        return (V) params.put(key, value);
    }

    public <V> void putAll(Map<String, ? extends V> m) {
        params.putAll(m);
    }

    public <V> V put(V bean) {
        JSONObject jsonObject = JsonKit.castToObject(bean, JSONObject.class);
        this.putAll(jsonObject);
        return bean;
    }

    /**
     * 自定函数，方便替换算法
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T toJavaObject(Class<T> clazz) {
        String text = JSON.toJSONString(params, new EmptyValueFilter(filterEmpty));
        return JSON.parseObject(text, clazz);
    }

    private static class EmptyValueFilter implements ValueFilter {

        // filterEmpty true 过滤空字符串
        private boolean filterEmpty = true;

        public EmptyValueFilter(boolean filterEmpty) {
            this.filterEmpty = filterEmpty;
        }

        @Override
        public Object process(Object object, String propertyName, Object propertyValue) {
            if (filterEmpty && "".equals(propertyValue)) {
                return null;
            }
            return propertyValue;
        }
    }
}
