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
 * @Description: 数据集元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-22
 * @Version: V1.0
 */
@Data
@TableName("rss_dataset")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_dataset对象", description="数据集元数据表")
public class RssDataset {

	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**name*/
	@Excel(name = "name", width = 15)
    @ApiModelProperty(value = "name")
	private java.lang.String name;
	/**datasetVersion*/
	@Excel(name = "datasetVersion", width = 15)
    @ApiModelProperty(value = "datasetVersion")
	private java.lang.String datasetVersion;
	/**taskType*/
	@Excel(name = "taskType", width = 15)
    @ApiModelProperty(value = "taskType")
	private java.lang.String taskType;
	/**datasetLink*/
	@Excel(name = "datasetLink", width = 15)
    @ApiModelProperty(value = "datasetLink")
	private java.lang.Object datasetLink;
	/**datasetCopy*/
	@Excel(name = "datasetCopy", width = 15)
    @ApiModelProperty(value = "datasetCopy")
	private java.lang.Object datasetCopy;
	/**datasetCite*/
	@Excel(name = "datasetCite", width = 15)
    @ApiModelProperty(value = "datasetCite")
	private java.lang.Object datasetCite;
	/**datasetMode*/
	@Excel(name = "datasetMode", width = 15)
    @ApiModelProperty(value = "datasetMode")
	private java.lang.String datasetMode;
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
	/**keyword*/
	@Excel(name = "keyword", width = 15)
    @ApiModelProperty(value = "keyword")
	private java.lang.String keyword;
	/**sampleSum*/
	@Excel(name = "sampleSum", width = 15)
    @ApiModelProperty(value = "sampleSum")
	private java.lang.Integer sampleSum;
	/**dataSize*/
	@Excel(name = "dataSize", width = 15)
    @ApiModelProperty(value = "dataSize")
	private java.lang.Float dataSize;
	/**sampleSize*/
	@Excel(name = "sampleSize", width = 15)
    @ApiModelProperty(value = "sampleSize")
	private java.lang.String sampleSize;
	/**imageType*/
	@Excel(name = "imageType", width = 15)
    @ApiModelProperty(value = "imageType")
	private java.lang.String imageType;
	/**resolution*/
	@Excel(name = "resolution", width = 15)
    @ApiModelProperty(value = "resolution")
	private java.lang.String resolution;
	/**bandSize*/
	@Excel(name = "bandSize", width = 15)
    @ApiModelProperty(value = "bandSize")
	private java.lang.Integer bandSize;
	/**imageForm*/
	@Excel(name = "imageForm", width = 15)
    @ApiModelProperty(value = "imageForm")
	private java.lang.String imageForm;
	/**instrument*/
	@Excel(name = "instrument", width = 15)
    @ApiModelProperty(value = "instrument")
	private java.lang.String instrument;
	/**contacter*/
	@Excel(name = "contacter", width = 15)
    @ApiModelProperty(value = "contacter")
	private java.lang.String contacter;
	/**phoneNumber*/
	@Excel(name = "phoneNumber", width = 15)
    @ApiModelProperty(value = "phoneNumber")
	private java.lang.String phoneNumber;
	/**email*/
	@Excel(name = "email", width = 15)
    @ApiModelProperty(value = "email")
	private java.lang.String email;
	/**address*/
	@Excel(name = "address", width = 15)
    @ApiModelProperty(value = "address")
	private java.lang.Object address;
	/**description*/
	@Excel(name = "description", width = 15)
    @ApiModelProperty(value = "description")
	private java.lang.Object description;
	/**overview*/
	@Excel(name = "overview", width = 15)
    @ApiModelProperty(value = "overview")
	private java.lang.Object overview;
	/**location*/
	@Excel(name = "location", width = 15)
    @ApiModelProperty(value = "location")
	private java.lang.Object location;
	/**thumb*/
	@Excel(name = "thumb", width = 15)
    @ApiModelProperty(value = "thumb")
	private java.lang.Object thumb;
	/**subType*/
	@Excel(name = "subType", width = 15)
    @ApiModelProperty(value = "subType")
	private java.lang.String subType;
	/**createDate*/
	@Excel(name = "createDate", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "createDate")
	private java.util.Date createDate;
	/**visit*/
	@Excel(name = "visit", width = 15)
	@ApiModelProperty(value = "visit")
	private java.lang.Integer visit;

	/**classes**/
	@Excel(name = "classes", width = 15)
	@ApiModelProperty(value = "classes")
	private java.lang.String classes;
}
