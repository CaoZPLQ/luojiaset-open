package cn.whu.geois.modules.rssample.entity;

import lombok.Data;

/**
 * @author czp
 * @version 1.0
 * @date 2021/6/23 17:02
 */
@Data
public class RssPair {
    private String imagePath;
    private String labelPath;
    private String preImagePath;
    private String postImagePath;
}
