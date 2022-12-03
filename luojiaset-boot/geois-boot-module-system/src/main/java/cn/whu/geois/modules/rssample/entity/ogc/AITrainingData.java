package cn.whu.geois.modules.rssample.entity.ogc;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengis.feature.Feature;

import java.util.ArrayList;

/**
 * @author czp
 * @version 1.0
 * @date 2021/8/19 15:54
 */
@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AITrainingData {
    private String id;
    private String[] dataSourceId;
    private String dataUrl;
    private DateTime createdTime;
    private DateTime updatedTime;
//    private Size size;
    private String trainingTypeCode;
    private ArrayList<AILabel> labels;
    private Double[] bbox;
    private Integer numberOfLabels;
    public  void setTrainingTypeCode(String trainingTypeCode){
        if (trainingTypeCode.equals(AITrainingTypeCode.train)||trainingTypeCode.equals(AITrainingTypeCode.validation)||trainingTypeCode.equals(AITrainingTypeCode.test)){
            this.trainingTypeCode=trainingTypeCode;
        }else {
            System.err.println("error purpose value!!!");
        }
    }

    public final class AITrainingTypeCode{
        public static final String train="training";
        public static final String validation="validation";
        public static final String test="test";
    }


    @NoArgsConstructor
    @Data
//    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Size {
        private Integer width;
        private Integer height;
        private Integer depth;
    }
    @NoArgsConstructor
    @Data
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class AILabel {
        private Boolean isNegative;
        private String className;
//        private String object;
        private JSONObject object;

        private String geometryType;
        private Boolean isDiffDetectable;
        private String imageURL;
    }
}
