package cn.whu.geois.modules.rssample.util;

import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/3/3
 */
public class ListUtil {

    public static <T> T getLastElement(List<T> list){
        return list.get(list.size()-1);
    }

    public static List<CellRangeAddress> sortEmergedRegions (List<CellRangeAddress> list){
        Collections.sort(list, new Comparator<CellRangeAddress>() {
            @Override
            public int compare(CellRangeAddress o1, CellRangeAddress o2) {
                int diff = o1.getFirstRow()-o2.getFirstRow();
                if (diff > 0) {
                    return 1;
                }else if(diff<0){
                    return -1;
                }
                return 0;
            }
        });
        return list;
    }
}
