package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssScSample;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 场景分类样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-15
 * @Version: V1.0
 */
public interface IRssScSampleService extends IService<RssScSample> {
    boolean insertBatch(List<RssScSample> entityList);
    boolean insertBatch(List<RssScSample> entityList, int batchSize);
}
