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
 * @date 2021/5/3 17:08
 */
@Data
@TableName("rss_od_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_od_sample对象", description="目标检测样本元数据表")
public class RssOdSampleSimple {
    private Integer id;
    private String classNames;
    private String classCodes;
    private String datasetName;
    private String objectNums;
    private Integer sampleHeight;
    private Integer sampleWidth;
    private String bbox;
    private String instrument;
    private Object thumb;
    private Integer trnValueTest;
    private Integer imageChannels;
    private String imagePath;
    private String labelPath;

    private String imageResolution;
}
