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
public class RssOdAnn {
    private String folder;
    private String filename;
    private Source source;
    private Owner owner;
    private Size size;
    private String segmented;
    private ArrayList<Object> object;
    private Double minLongitude;
    private Double minLatitude;
    private Double maxLongitude;
    private Double maxLatitude;


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
        private String unitName;
        private String pose;
        private String truncated;
        private String difficult;
        private Bndbox bndbox;
        @NoArgsConstructor
        @Data
        public static class Bndbox {
            private String xmin;
            private String ymin;
            private String xmax;
            private String ymax;
            private String center_x;
            private String center_y;
            private String box_width;
            private String box_height;
            private String box_angle;
            private String coordinates;
            private String ingestTime;
            private String x0;
            private String y0;
            private String x1;
            private String y1;
            private String x2;
            private String y2;
            private String x3;
            private String y3;
        }
    }
}
