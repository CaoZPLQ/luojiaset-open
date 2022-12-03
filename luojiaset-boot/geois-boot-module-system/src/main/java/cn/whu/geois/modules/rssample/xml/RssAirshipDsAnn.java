package cn.whu.geois.modules.rssample.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/6/17 11:36
 */
@Data
@XmlRootElement(name = "annotation")
public class RssAirshipDsAnn {
    private Source source;
    private Research research;
    private Objects objects;

    @NoArgsConstructor
    @Data
    public static class Source{
        private String filename;
        private String origin;
    }

    @NoArgsConstructor
    @Data
    public static class Research{
        private String version;
        private String provider;
        private String author;
        private String pluginname;
        private String time;
    }

    @NoArgsConstructor
    @Data
    public static class Objects{
        private List<Object> object;
        @NoArgsConstructor
        @Data
        public static class Object{
            private String coordinate;
            private String type;
            private String description;
            private Possibleresult possibleresult;
            private Points points;
            @NoArgsConstructor
            @Data
            public static class Possibleresult{
                private String name;
            }

            @NoArgsConstructor
            @Data
            public static class Points{
                private List<String> point;
            }
        }
    }
}
