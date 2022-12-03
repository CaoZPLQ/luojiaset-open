package cn.whu.geois.modules.rssample.entity;

import lombok.Data;

/**
 * @author czp
 * @version 1.0
 * @date 2021/6/11 10:08
 */
@Data
public class RssOrderRequest {
    private String userId;
    private String taskType;
    private String[] sampleId;
}
