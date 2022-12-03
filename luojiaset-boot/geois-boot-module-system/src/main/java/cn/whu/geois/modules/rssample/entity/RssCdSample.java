package cn.whu.geois.modules.rssample.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 变化检测样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-12
 * @Version: V1.0
 */
@Data
@TableName("rss_cd_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_cd_sample对象", description="变化检测样本元数据表")
public class RssCdSample {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**datasetId*/
	@Excel(name = "datasetId", width = 15)
    @ApiModelProperty(value = "datasetId")
	private java.lang.Integer datasetId;
	/**sampleArea*/
	@Excel(name = "sampleArea", width = 15)
    @ApiModelProperty(value = "sampleArea")
	private java.lang.Object sampleArea;
	/**sampleQuality*/
	@Excel(name = "sampleQuality", width = 15)
    @ApiModelProperty(value = "sampleQuality")
	private java.lang.String sampleQuality;
	/**sampleLabeler*/
	@Excel(name = "sampleLabeler", width = 15)
    @ApiModelProperty(value = "sampleLabeler")
	private java.lang.String sampleLabeler;
	/**annotationDate*/
    @ApiModelProperty(value = "annotationDate")
	private java.util.Date annotationDate;
	/**preSampleDate*/
    @ApiModelProperty(value = "preSampleDate")
	private java.util.Date preSampleDate;
	/**postSampleDate*/
    @ApiModelProperty(value = "postSampleDate")
	private java.util.Date postSampleDate;
	/**preImagePath*/
	@Excel(name = "preImagePath", width = 15)
    @ApiModelProperty(value = "preImagePath")
	private java.lang.Object preImagePath;
	/**postImagePath*/
	@Excel(name = "postImagePath", width = 15)
    @ApiModelProperty(value = "postImagePath")
	private java.lang.Object postImagePath;
	/**preImageType*/
	@Excel(name = "preImageType", width = 15)
    @ApiModelProperty(value = "preImageType")
	private java.lang.String preImageType;
	/**preImageChannels*/
	@Excel(name = "preImageChannels", width = 15)
    @ApiModelProperty(value = "preImageChannels")
	private java.lang.Integer preImageChannels;
	/**preImageResolution*/
	@Excel(name = "preImageResolution", width = 15)
    @ApiModelProperty(value = "preImageResolution")
	private java.lang.String preImageResolution;
	/**labelPath*/
	@Excel(name = "labelPath", width = 15)
    @ApiModelProperty(value = "labelPath")
	private java.lang.Object labelPath;
	/**preInstrument*/
	@Excel(name = "preInstrument", width = 15)
    @ApiModelProperty(value = "preInstrument")
	private java.lang.String preInstrument;
	/**trnValueTest*/
	@Excel(name = "trnValueTest", width = 15)
    @ApiModelProperty(value = "trnValueTest")
	private java.lang.Integer trnValueTest;
	/**createBy*/
	@Excel(name = "createBy", width = 15)
    @ApiModelProperty(value = "createBy")
	private java.lang.String createBy;
	/**createTime*/
    @ApiModelProperty(value = "createTime")
	private java.util.Date createTime;
	/**updateBy*/
	@Excel(name = "updateBy", width = 15)
    @ApiModelProperty(value = "updateBy")
	private java.lang.String updateBy;
	/**updateTime*/
    @ApiModelProperty(value = "updateTime")
	private java.util.Date updateTime;
	/**sampleWidth*/
	@Excel(name = "sampleWidth", width = 15)
    @ApiModelProperty(value = "sampleWidth")
	private java.lang.Integer sampleWidth;
	/**sampleHeight*/
	@Excel(name = "sampleHeight", width = 15)
    @ApiModelProperty(value = "sampleHeight")
	private java.lang.Integer sampleHeight;
	/**postImageType*/
	@Excel(name = "postImageType", width = 15)
    @ApiModelProperty(value = "postImageType")
	private java.lang.String postImageType;
	/**postImageChannels*/
	@Excel(name = "postImageChannels", width = 15)
    @ApiModelProperty(value = "postImageChannels")
	private java.lang.Integer postImageChannels;
	/**postImageResolution*/
	@Excel(name = "postImageResolution", width = 15)
    @ApiModelProperty(value = "postImageResolution")
	private java.lang.String postImageResolution;
	/**postInstrument*/
	@Excel(name = "postInstrument", width = 15)
    @ApiModelProperty(value = "postInstrument")
	private java.lang.String postInstrument;
}
