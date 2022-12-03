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
@TableName("rss_lc_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "rss_lc_sample对象", description = "地物分类样本元数据表")
public class RssCdSampleSimple {
    private Integer id;
    private String classNames;
    private String classCodes;
    private String datasetName;
    private Integer sampleHeight;
    private Integer sampleWidth;
    private String preInstrument;
    private String postInstrument;

    private Integer preImageChannels;
    private Integer postImageChannels;
    private Integer trnValueTest;
    private Object preImagePath;
    private Object postImagePath;

    private String preImageResolution;
    private String postImageResolution;
}
