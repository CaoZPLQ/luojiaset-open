package cn.whu.geois.modules.rssample.entity.ogc;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/8/5 16:13
 */
@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AITrainingDataCollection {
    private String id;
    private String name;
    private String description;
    private String version;
    private Integer amountOfTrainingData;
    private String createdTime;
    private String updatedTime;
    private String licence;
    private String[] keywords;
//    private String purpose;
    private List<Provider> providers;
    private String[] classes;
//    private String labelType;
    private List<DataSource> dataSources;
    private List<AILabeling> labelings;
    private Task task;
//    private List<AITrainingData> data;
    private String data;
    @NoArgsConstructor
    @Data
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Task{
        private String description;
        private String taskType;
    }
    @NoArgsConstructor
    @Data
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Provider{
        private String name;
    }
    @NoArgsConstructor
    @Data
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class DataSource{
        private String id;
        private String dataType;
        private SourceCitation sourceCitation;
        private String sensor;
        private Double resolution;
        @NoArgsConstructor
        @Data
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public static class SourceCitation{
            private String title;
        }

    }
//    private void setPurpose(String purpose){
//        if (purpose.equals(AISamplePurposeCode.train)||purpose.equals(AISamplePurposeCode.validation)||purpose.equals(AISamplePurposeCode.test)){
//            this.purpose=purpose;
//        }else {
//            System.err.println("error purpose value!!!");
//        }
//    }
//
//    public final class AISamplePurposeCode{
//        public static final String train="train";
//        public static final String validation="validation";
//        public static final String test="test";
//    }
}
