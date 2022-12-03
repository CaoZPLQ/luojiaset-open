package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssGqjcClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 地理国情监测分类体系表
 * @Author: jeecg-boot
 * @Date:   2021-01-29
 * @Version: V1.0
 */
public interface IRssGqjcClassService extends IService<RssGqjcClass> {
    boolean insertBatch(List<RssGqjcClass> entityList);
    boolean insertBatch(List<RssGqjcClass> entityList, int batchSize);
}
