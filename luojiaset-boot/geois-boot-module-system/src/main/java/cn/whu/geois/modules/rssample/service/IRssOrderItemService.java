package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 订单明细表
 * @Author: jeecg-boot
 * @Date:   2021-06-06
 * @Version: V1.0
 */
public interface IRssOrderItemService extends IService<RssOrderItem> {
    boolean insertBatch(List<RssOrderItem> entityList);
    boolean insertBatch(List<RssOrderItem> entityList, int batchSize);
}
