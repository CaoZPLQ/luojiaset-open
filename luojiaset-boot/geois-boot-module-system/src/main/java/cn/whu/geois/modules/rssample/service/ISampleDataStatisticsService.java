package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.dto.ClassNumDto;
import cn.whu.geois.modules.rssample.dto.TaskDatasetDto;

import java.util.List;
import java.util.Map;

/**
 * @Author WuSiKun
 * @Date 2021-12-28 14:57
 * @Description
 */
public interface ISampleDataStatisticsService {
    /**
     * 获取总的数据集和总的样本数
     */
    Map<String,Object> getTotalDataStatistics();

    /**
     * 统计各种任务类型的数据集数量
     */
    List<TaskDatasetDto> getDatasetNumOfTaskType();

    /**
     * 按任务类型统计样本数量
     */
    List<Map<String, Object>> getSampleNumOfTaskType();

    /**
     * 按分类体系统计各种任务类型
     */
    List<Map<String, Object>> getClassNumOfTaskType();

    /**
     * 目标识别中排行前5的分类名称
     */
    List<ClassNumDto> getTop5ClassNumOfOdSample();

    /**
     * 地物分类中排行前5的分类名称
     */
    List<ClassNumDto> getTop5ClassNumOfLcSample();
}
