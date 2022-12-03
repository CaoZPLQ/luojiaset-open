package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssClassMap;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 数据集类别关系映射表
 * @Author: jeecg-boot
 * @Date:   2021-05-10
 * @Version: V1.0
 */
public interface IRssClassMapService extends IService<RssClassMap> {
    boolean insertBatch(List<RssClassMap> entityList);
    boolean insertBatch(List<RssClassMap> entityList, int batchSize);

}
