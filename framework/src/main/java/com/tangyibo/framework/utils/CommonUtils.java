package com.tangyibo.framework.utils;

import java.util.List;

public class CommonUtils {
    /**
     * 检查List是否可用
     * @param list
     * @return
     */
    public static boolean isEmpty(List list){
        return list != null && list.size() > 0;
    }
}
