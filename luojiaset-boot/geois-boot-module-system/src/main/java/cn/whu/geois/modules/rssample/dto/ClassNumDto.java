package cn.whu.geois.modules.rssample.dto;

import lombok.Data;

/**
 * @Author WuSiKun
 * @Date 2021-12-29 09:36
 * @Description
 */
@Data
public class ClassNumDto {
    /**
     * 编号
     */
    private String classCode;
    /**
     * 名称
     */
    private String className;
    /**
     * 数量
     */
    private String dataNum;
}
