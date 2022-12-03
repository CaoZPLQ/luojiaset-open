package cn.whu.geois.modules.rssample.controller;

import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.modules.rssample.dto.ClassNumDto;
import cn.whu.geois.modules.rssample.dto.TaskDatasetDto;
import cn.whu.geois.modules.rssample.service.ISampleDataStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author WuSiKun
 * @Date 2021-12-28 17:12
 * @Description 统计数据库的数据
 */
@Slf4j
@RestController
@RequestMapping("/rssample/sampleDataStatistics")
public class SampleDataStatisticsController {
    @Autowired
    private ISampleDataStatisticsService sampleDataStatisticsService;

    @GetMapping("/getTotalDataStatistics")
    public Result<Map> getTotalDataStatistics() {
        Map<String, Object> totalDataStatistics = sampleDataStatisticsService.getTotalDataStatistics();
        Result<Map> result = new Result<>();
        result.setSuccess(true);
        result.setResult(totalDataStatistics);
        return result;
    }

    @GetMapping("/getDatasetNumOfTaskType")
    public Result<List<TaskDatasetDto>> getDatasetNumOfTaskType() {
        List<TaskDatasetDto> classNumOfTaskType = sampleDataStatisticsService.getDatasetNumOfTaskType();
        Result<List<TaskDatasetDto>> result = new Result<>();
        result.setSuccess(true);
        result.setResult(classNumOfTaskType);
        return result;
    }

    @GetMapping("/getSampleNumOfTaskType")
    public Result<List<Map<String, Object>>> getSampleNumOfTaskType() {
        List<Map<String, Object>> sampleNumOfTaskType = sampleDataStatisticsService.getSampleNumOfTaskType();
        Result<List<Map<String, Object>>> result = new Result<>();
        result.setSuccess(true);
        result.setResult(sampleNumOfTaskType);
        return result;
    }

    @GetMapping("/getClassNumOfTaskType")
    public Result<List<Map<String, Object>>> getClassNumOfTaskType() {
        List<Map<String, Object>> classNumOfTaskType = sampleDataStatisticsService.getClassNumOfTaskType();
        Result<List<Map<String, Object>>> result = new Result<>();
        result.setSuccess(true);
        result.setResult(classNumOfTaskType);
        return result;
    }

    @GetMapping("/getTop5ClassNumOfOdSample")
    public Result<List<ClassNumDto>> getTop5ClassNumOfOdSample() {
        List<ClassNumDto> top5ClassNumOfOdSample = sampleDataStatisticsService.getTop5ClassNumOfOdSample();
        Result<List<ClassNumDto>> result = new Result<>();
        result.setSuccess(true);
        result.setResult(top5ClassNumOfOdSample);
        return result;
    }

    @GetMapping("/getTop5ClassNumOfLcSample")
    public Result<List<ClassNumDto>> getTop5ClassNumOfLcSample() {
        List<ClassNumDto> top5ClassNumOfLcSample = sampleDataStatisticsService.getTop5ClassNumOfLcSample();
        Result<List<ClassNumDto>> result = new Result<>();
        result.setSuccess(true);
        result.setResult(top5ClassNumOfLcSample);
        return result;
    }
}
