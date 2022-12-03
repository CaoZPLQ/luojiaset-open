package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssOrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 订单信息表
 * @Author: jeecg-boot
 * @Date:   2021-07-05
 * @Version: V1.0
 */
public interface IRssOrderInfoService extends IService<RssOrderInfo> {
    boolean insertBatch(List<RssOrderInfo> entityList);
    boolean insertBatch(List<RssOrderInfo> entityList, int batchSize);
}
