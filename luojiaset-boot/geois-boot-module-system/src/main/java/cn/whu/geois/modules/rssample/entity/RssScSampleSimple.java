package cn.whu.geois.modules.rssample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/5/17
 */
@Data
@TableName("rss_sc_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain=true)
public class RssScSampleSimple {
    private Integer id;
    private String classNames;
    private String classCodes;
    private String datasetName;
    private Integer sampleHeight;
    private Integer sampleWidth;
    private String instrument;
    private Object thumb;

    private Object imagePath;
    private Integer imageChannels;
    private Integer trnValueTest;

    private String imageResolution;
}

