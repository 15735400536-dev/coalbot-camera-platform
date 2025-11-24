package com.coalbot.module.camera.utils;

import com.coalbot.module.core.response.PageList;
import com.github.pagehelper.PageInfo;

public class TypeUtils {

    /**
     * long转int
     * @param longValue
     * @return
     */
    public static int longToInt(long longValue) {
        int intValue;
        if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
            intValue = (int) longValue;
        } else {
            // 处理溢出逻辑（如抛异常、取边界值）
            throw new IllegalArgumentException("long值超出int范围");
        }
        return intValue;
    }

    /**
     * PageInfo转PageList
     * @param pageInfo
     * @return
     */
    public static <T> PageList<T> pageInfoToPageList(PageInfo<T> pageInfo) {
        PageList<T> pageList = new PageList<>();
        pageList.setPageSize(pageInfo.getPageSize());
        pageList.setCurPage(pageInfo.getPages());
        pageList.setTotalCount(pageInfo.getTotal());
        pageList.setTotalPage(pageInfo.getPageNum());
        pageList.setList(pageInfo.getList());
        return pageList;
    }

}
