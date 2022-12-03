package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssScClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 场景分类体系表
 * @Author: jeecg-boot
 * @Date:   2021-03-30
 * @Version: V1.0
 */
public interface IRssScClassService extends IService<RssScClass> {
    boolean insertBatch(List<RssScClass> entityList);
    boolean insertBatch(List<RssScClass> entityList, int batchSize);
}
