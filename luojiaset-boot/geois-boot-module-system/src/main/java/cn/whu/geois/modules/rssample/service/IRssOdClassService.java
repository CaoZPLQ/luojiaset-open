package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssOdClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 目标检测分类体系表
 * @Author: jeecg-boot
 * @Date:   2021-03-30
 * @Version: V1.0
 */
public interface IRssOdClassService extends IService<RssOdClass> {
    boolean insertBatch(List<RssOdClass> entityList);
    boolean insertBatch(List<RssOdClass> entityList, int batchSize);
}
