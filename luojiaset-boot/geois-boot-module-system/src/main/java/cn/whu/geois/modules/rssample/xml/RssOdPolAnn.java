package cn.whu.geois.modules.rssample.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * @author czp
 * @version 1.0
 * @date 2021/3/27 15:00
 */
@Data
@XmlRootElement(name = "annotation")
public class RssOdPolAnn {
    private String folder;
    private String filename;
    private Source source;
    private Owner owner;
    private Size size;
    private String segmented;
    private ArrayList<Object> object;


    @NoArgsConstructor
    @Data
    public static class Source{
        private String database;
        private String annotation;
        private String image;
    }
    @NoArgsConstructor
    @Data
    public static class Owner{
        private String flickrid;
        private String name;
    }
    @NoArgsConstructor
    @Data
    public static class Size {
        private String width;
        private String height;
        private String depth;
    }
    @NoArgsConstructor
    @Data
    public static class Object {
        private String name;
        private String pose;
        private String truncated;
        private String difficult;
        private String bndbox;
    }
}
