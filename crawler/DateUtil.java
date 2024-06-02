package com.et.crawler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  17:57
 * @Description: TODO 日期相关工具类
 * @Version: 1.0
 */
public class DateUtil {

    /**
     * 日期格式路径
     * 例如：2024/06/01
     * @return
     */
    public static String datePath(){
        SimpleDateFormat fmt = new SimpleDateFormat("YYYY/MM/DD");
        return fmt.format(new Date());
    }
}
