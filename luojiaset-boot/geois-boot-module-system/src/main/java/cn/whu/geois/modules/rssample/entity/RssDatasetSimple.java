package cn.whu.geois.modules.rssample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czp
 * @version 1.0
 * @date 2021/4/28 21:14
 */
@Data
@TableName("rss_dataset")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_dataset对象", description="数据集元数据表")
public class RssDatasetSimple {
    private Integer id;
    private String name;
    private String datasetVersion;
    private String datasetCopy;
    private String thumb;
    private String taskType;
    private String visit;
}
