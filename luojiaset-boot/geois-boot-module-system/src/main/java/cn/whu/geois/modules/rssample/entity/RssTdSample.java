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
 * @Description: 三维多视样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-03-14
 * @Version: V1.0
 */
@Data
@TableName("rss_td_sample")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_td_sample对象", description="三维多视样本元数据表")
public class RssTdSample {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private Integer id;
	/**datasetId*/
	@Excel(name = "datasetId", width = 15)
    @ApiModelProperty(value = "datasetId")
	private Integer datasetId;
	/**sampleSize*/
	@Excel(name = "sampleSize", width = 15)
    @ApiModelProperty(value = "sampleSize")
	private String sampleSize;
	/**sampleArea*/
	@Excel(name = "sampleArea", width = 15)
    @ApiModelProperty(value = "sampleArea")
	private Object sampleArea;
	/**sampleDate*/
    @ApiModelProperty(value = "sampleDate")
	private Date sampleDate;
	/**sampleQuality*/
	@Excel(name = "sampleQuality", width = 15)
    @ApiModelProperty(value = "sampleQuality")
	private String sampleQuality;
	/**sampleLabeler*/
	@Excel(name = "sampleLabeler", width = 15)
    @ApiModelProperty(value = "sampleLabeler")
	private String sampleLabeler;
	/**annotationDate*/
    @ApiModelProperty(value = "annotationDate")
	private Date annotationDate;
	/**mutiViewPaths*/
	@Excel(name = "mutiViewPaths", width = 15)
    @ApiModelProperty(value = "mutiViewPaths")
	private Object mutiViewPaths;
	/**mutiViewDepthPaths*/
	@Excel(name = "mutiViewDepthPaths", width = 15)
    @ApiModelProperty(value = "mutiViewDepthPaths")
	private Object mutiViewDepthPaths;
	/**mutiViewParmPaths*/
	@Excel(name = "mutiViewParmPaths", width = 15)
    @ApiModelProperty(value = "mutiViewParmPaths")
	private Object mutiViewParmPaths;
	/**imageMode*/
	@Excel(name = "imageMode", width = 15)
    @ApiModelProperty(value = "imageMode")
	private String imageMode;
	/**parmMode*/
	@Excel(name = "parmMode", width = 15)
    @ApiModelProperty(value = "parmMode")
	private String parmMode;
	/**depthMode*/
	@Excel(name = "depthMode", width = 15)
    @ApiModelProperty(value = "depthMode")
	private String depthMode;
	/**imageType*/
	@Excel(name = "imageType", width = 15)
    @ApiModelProperty(value = "imageType")
	private String imageType;
	/**imageChannels*/
	@Excel(name = "imageChannels", width = 15)
    @ApiModelProperty(value = "imageChannels")
	private Integer imageChannels;
	/**imageResolution*/
	@Excel(name = "imageResolution", width = 15)
    @ApiModelProperty(value = "imageResolution")
	private String imageResolution;
	/**instrument*/
	@Excel(name = "instrument", width = 15)
    @ApiModelProperty(value = "instrument")
	private String instrument;
	/**trnValueTest*/
	@Excel(name = "trnValueTest", width = 15)
    @ApiModelProperty(value = "trnValueTest")
	private Integer trnValueTest;
	/**createBy*/
	@Excel(name = "createBy", width = 15)
    @ApiModelProperty(value = "createBy")
	private String createBy;
	/**createTime*/
    @ApiModelProperty(value = "createTime")
	private Date createTime;
	/**updateBy*/
	@Excel(name = "updateBy", width = 15)
    @ApiModelProperty(value = "updateBy")
	private String updateBy;
	/**updateTime*/
    @ApiModelProperty(value = "updateTime")
	private Date updateTime;
}
