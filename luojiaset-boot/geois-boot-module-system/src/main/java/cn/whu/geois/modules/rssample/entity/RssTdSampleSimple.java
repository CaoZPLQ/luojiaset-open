package cn.whu.geois.modules.rssample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author czp
 * @version 1.0
 */
@Data
@TableName("rss_td_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "rss_td_sample对象", description = "3D样本元数据表")
public class RssTdSampleSimple {
    private Integer id;
    private String datasetName;
    private Integer sampleHeight;
    private Integer sampleWidth;
    private String bbox;
    private String instrument;

    private String imageResolution;
}
