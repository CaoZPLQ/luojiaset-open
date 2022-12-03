package cn.whu.geois.modules.rssample.entity;

import lombok.Data;

import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/5/12 11:18
 */
@Data
public class RssFilter {
    private List<Integer> ids;
    private List<String> instruments;
    private List<Integer> datasetIds;
    private List<String> uniCLassNames;
    private List<String> uniCLassCodes;
    private List<Integer> tradeStatus;
    private List<String> userId;
    private List<String> preInstruments;
    private List<String> postInstruments;
}
