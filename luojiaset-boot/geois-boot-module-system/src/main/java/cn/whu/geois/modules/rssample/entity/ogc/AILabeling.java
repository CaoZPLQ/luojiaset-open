package cn.whu.geois.modules.rssample.entity.ogc;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author czp
 * @version 1.0
 * @date 2021/8/22 17:05
 */
@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AILabeling {
    private String id;
    private AILabeler labeler;
    private AILabelingProcedure procedure;
    @NoArgsConstructor
    @Data
    public static class AILabeler{
        private String id;
        private String name;
    }
    @NoArgsConstructor
    @Data
    public static class AILabelingProcedure{
        private String id;
        private String[] method;
        private String[] tools;
    }
}
