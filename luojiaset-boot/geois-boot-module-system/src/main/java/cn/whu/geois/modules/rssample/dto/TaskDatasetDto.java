package cn.whu.geois.modules.rssample.dto;

import lombok.Data;

/**
 * @Author WuSiKun
 * @Date 2021-12-29 09:29
 * @Description
 */
@Data
public class TaskDatasetDto {
    /**
     * 任务类型名称
     */
    private String name;
    /**
     * 数量
     */
    private Integer dataNum;
}
