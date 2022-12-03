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
 * @Description: 目标检测样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-07-17
 * @Version: V1.0
 */
@Data
@TableName("rss_od_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_od_sample对象", description="目标检测样本元数据表")
public class RssOdSample {
    
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
	/**sampleDate*/
    @ApiModelProperty(value = "sampleDate")
	private java.util.Date sampleDate;
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
	/**labelBbox*/
	@Excel(name = "labelBbox", width = 15)
    @ApiModelProperty(value = "labelBbox")
	private java.lang.String labelBbox;
	/**imagePath*/
	@Excel(name = "imagePath", width = 15)
    @ApiModelProperty(value = "imagePath")
	private java.lang.Object imagePath;
	/**labelPath*/
	@Excel(name = "labelPath", width = 15)
    @ApiModelProperty(value = "labelPath")
	private java.lang.Object labelPath;
	/**imageType*/
	@Excel(name = "imageType", width = 15)
    @ApiModelProperty(value = "imageType")
	private java.lang.String imageType;
	/**imageChannels*/
	@Excel(name = "imageChannels", width = 15)
    @ApiModelProperty(value = "imageChannels")
	private java.lang.Integer imageChannels;
	/**imageResolution*/
	@Excel(name = "imageResolution", width = 15)
    @ApiModelProperty(value = "imageResolution")
	private java.lang.String imageResolution;
	/**instrument*/
	@Excel(name = "instrument", width = 15)
    @ApiModelProperty(value = "instrument")
	private java.lang.Object instrument;
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
	/**thumb*/
	@Excel(name = "thumb", width = 15)
    @ApiModelProperty(value = "thumb")
	private java.lang.Object thumb;
}
