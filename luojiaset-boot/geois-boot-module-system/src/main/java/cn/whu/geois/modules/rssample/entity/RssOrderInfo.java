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
 * @Description: 订单信息表
 * @Author: jeecg-boot
 * @Date:   2021-07-05
 * @Version: V1.0
 */
@Data
@TableName("rss_order_info")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_order_info对象", description="订单信息表")
public class RssOrderInfo {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**orderNum*/
	@Excel(name = "orderNum", width = 15)
    @ApiModelProperty(value = "orderNum")
	private java.lang.String orderNum;
	/**userId*/
	@Excel(name = "userId", width = 15)
    @ApiModelProperty(value = "userId")
	private java.lang.String userId;
	/**totalAmount*/
	@Excel(name = "totalAmount", width = 15)
    @ApiModelProperty(value = "totalAmount")
	private java.math.BigDecimal totalAmount;
	/**payStatus*/
	@Excel(name = "payStatus", width = 15)
    @ApiModelProperty(value = "payStatus")
	private java.lang.Integer payStatus;
	/**tradeStatus*/
	@Excel(name = "tradeStatus", width = 15)
    @ApiModelProperty(value = "tradeStatus")
	private java.lang.Integer tradeStatus;
	/**downloadUrl*/
	@Excel(name = "downloadUrl", width = 15)
    @ApiModelProperty(value = "downloadUrl")
	private java.lang.Object downloadUrl;
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
	/**taskType*/
	@Excel(name = "taskType", width = 15)
    @ApiModelProperty(value = "taskType")
	private java.lang.String taskType;
	/**sampleNum*/
	@Excel(name = "sampleNum", width = 15)
    @ApiModelProperty(value = "sampleNum")
	private java.lang.Integer sampleNum;
}
