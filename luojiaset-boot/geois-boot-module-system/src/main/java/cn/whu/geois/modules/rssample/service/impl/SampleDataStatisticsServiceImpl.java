package cn.whu.geois.modules.rssample.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.whu.geois.modules.rssample.dto.ClassNumDto;
import cn.whu.geois.modules.rssample.dto.TaskDatasetDto;
import cn.whu.geois.modules.rssample.enums.TaskTypeEnum;
import cn.whu.geois.modules.rssample.mapper.RssPostgresqlMapper;
import cn.whu.geois.modules.rssample.service.ISampleDataStatisticsService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author WuSiKun
 * @Date 2021-12-28 15:12
 * @Description
 */
@Service
@DS("postgres")
public class SampleDataStatisticsServiceImpl implements ISampleDataStatisticsService {
    @Autowired
    private RssPostgresqlMapper rssPostgresqlMapper;

    @Override
    public Map<String, Object> getTotalDataStatistics() {
        //数据集总数
        Integer datasetCount = rssPostgresqlMapper.getDatasetCount();
        //od-目标识别 lc-地物分类 cd-变化检测 td-多视三维
        List<String> taskTypeList = Arrays.asList("od", "lc", "cd", "td");
        //总样本数量
        Integer sum = 0;
        for (String taskType : taskTypeList) {
            sum += rssPostgresqlMapper.getDataCount(taskType);
        }
        //场景样本数
        Integer scDataCount = rssPostgresqlMapper.getSCDataCount();
        sum += scDataCount;
        Map<String, Object> map = new HashMap<>(2);
        map.put("datasetNum",datasetCount);
        map.put("sampleNum",sum);
        return map;
    }

    @Override
    public List<TaskDatasetDto> getDatasetNumOfTaskType() {
        List<TaskDatasetDto> datasetGroupByTaskType = rssPostgresqlMapper.getDatasetGroupByTaskType();
        if (CollectionUtil.isNotEmpty(datasetGroupByTaskType)) {
            for (TaskDatasetDto taskDatasetDto : datasetGroupByTaskType) {
                taskDatasetDto.setName(TaskTypeEnum.getEnumByType(taskDatasetDto.getName()).getDesc());
            }
        }
        return datasetGroupByTaskType;
    }

    @Override
    public List<Map<String, Object>> getSampleNumOfTaskType() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获取场景检索的样本数量
        Integer scDataCount = rssPostgresqlMapper.getSCDataCount();
        //获取目标识别的样本数量
        Integer odDataCount = rssPostgresqlMapper.getDataCount("od");
        //获取变化检测的样本数量
        Integer cdDataCount = rssPostgresqlMapper.getDataCount("cd");
        //获取地物分类的样本数量
        Integer lcDataCount = rssPostgresqlMapper.getDataCount("lc");
        //获取多视三维的样本数量
        Integer tdDataCount = rssPostgresqlMapper.getDataCount("td");

        Map<String,Object> scMap = new HashMap<>(2);
        scMap.put("name",TaskTypeEnum.SC.getDesc());
        scMap.put("dataNum",scDataCount);
        resultList.add(scMap);

        Map<String,Object> odMap = new HashMap<>(2);
        odMap.put("name",TaskTypeEnum.OD.getDesc());
        odMap.put("dataNum",odDataCount);
        resultList.add(odMap);

        Map<String,Object> cdMap = new HashMap<>(2);
        cdMap.put("name",TaskTypeEnum.CD.getDesc());
        cdMap.put("dataNum",cdDataCount);
        resultList.add(cdMap);

        Map<String,Object> lcMap = new HashMap<>(2);
        lcMap.put("name",TaskTypeEnum.LC.getDesc());
        lcMap.put("dataNum",lcDataCount);
        resultList.add(lcMap);

        Map<String,Object> tdMap = new HashMap<>(2);
        tdMap.put("name",TaskTypeEnum.TD.getDesc());
        tdMap.put("dataNum",tdDataCount);
        resultList.add(tdMap);

        return resultList;
    }

    /**
     * 统计各种任务类型的样本上的分类
     * @return
     */
    @Override
    public List<Map<String, Object>> getClassNumOfTaskType() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //场景检索在样本上的总分类数
        Integer classNumOfSCClassed = rssPostgresqlMapper.getClassNumOfSCClassed();
        //目标识别在样本上的总分类数
        Integer classNumOfODClassed = rssPostgresqlMapper.getClassNumOfODClassed();
        //变化检测在样本上的总分类数
        Integer classNumOfCDClassed = rssPostgresqlMapper.getClassNumOfCDClassed();
        //地物分类在样本上的总分类数
        Integer classNumOfLCClassed = rssPostgresqlMapper.getClassNumOfLCClassed();

        Map<String,Object> scMap = new HashMap<>(2);
        scMap.put("name",TaskTypeEnum.SC.getDesc());
        scMap.put("dataNum",classNumOfSCClassed);
        resultList.add(scMap);

        Map<String,Object> odMap = new HashMap<>(2);
        odMap.put("name",TaskTypeEnum.OD.getDesc());
        odMap.put("dataNum",classNumOfODClassed);
        resultList.add(odMap);

        Map<String,Object> cdMap = new HashMap<>(2);
        cdMap.put("name",TaskTypeEnum.CD.getDesc());
        cdMap.put("dataNum",classNumOfCDClassed);
        resultList.add(cdMap);

        Map<String,Object> lcMap = new HashMap<>(2);
        lcMap.put("name",TaskTypeEnum.LC.getDesc());
        lcMap.put("dataNum",classNumOfLCClassed);
        resultList.add(lcMap);

        List<Map<String, Object>> mapList = resultList.stream().sorted((m1, m2) -> {
            Integer dataNum1 = Objects.isNull(m1.get("dataNum")) ? 0 : (Integer)m1.get("dataNum");
            Integer dataNum2 = Objects.isNull(m2.get("dataNum")) ? 0 : (Integer)m2.get("dataNum");
            return dataNum2 - dataNum1;
        }).collect(Collectors.toList());

        return mapList;
    }

    @Override
    public List<ClassNumDto> getTop5ClassNumOfOdSample() {
        return rssPostgresqlMapper.getTop5ClassNameOfOdSample();
    }

    @Override
    public List<ClassNumDto> getTop5ClassNumOfLcSample() {
        return rssPostgresqlMapper.getTop5ClassNameOfLcSample();
    }
}
