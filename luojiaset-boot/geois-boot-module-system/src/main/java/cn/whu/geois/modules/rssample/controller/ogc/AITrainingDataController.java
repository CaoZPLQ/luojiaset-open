package cn.whu.geois.modules.rssample.controller.ogc;

import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.modules.rssample.analysis.ogc.AITrainingDataQuery;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingData;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingDataCollection;
import cn.whu.geois.modules.rssample.util.RssMinioUtil;
import io.minio.ObjectStat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/8/6 9:36
 */
@CrossOrigin(origins = "*",maxAge = 3600)//解决跨域问题
@Api(tags="TD API")
@Slf4j
@RestController
@RequestMapping("/ogc")
public class AITrainingDataController {

    @Autowired
    private AITrainingDataQuery trainingDataQuery;
    @Autowired
    private RssMinioUtil rssMinioUtil;

    @ApiOperation(value="collections query", notes="collections query")
    @GetMapping("/collections")
    @ResponseBody
    public List<AITrainingDataCollection> collections(){
        List<AITrainingDataCollection> trainingDataCollections=new LinkedList<>();
        trainingDataCollections=trainingDataQuery.getCollections();
        return trainingDataCollections;
    }
    @ApiOperation(value="collection query", notes="query collection by id")
    @GetMapping("/collections/{collectionId}")
    @ResponseBody
    public AITrainingDataCollection getCollectionById(@PathVariable("collectionId")String collectionId){
        AITrainingDataCollection trainingDataCollection=trainingDataQuery.getCollectionById(collectionId);
        return trainingDataCollection;
    }
    @ApiOperation(value="training data query", notes="query training data")
    @GetMapping("/collections/{collectionId}/trainingData")
    @ResponseBody
    public List<AITrainingData> getTrainingData(@PathVariable("collectionId")String collectionId){
        List<AITrainingData> trainingDataList=trainingDataQuery.getTrainingData(collectionId);
        return trainingDataList;
    }
    @ApiOperation(value="training data query", notes="query training data by id")
    @GetMapping("/collections/{collectionId}/trainingData/{trainingDataId}")
    @ResponseBody
    public AITrainingData getTrainingDataById(@PathVariable("collectionId")String collectionId,@PathVariable("trainingDataId")String trainingDataId){
        AITrainingData trainingData=trainingDataQuery.getTrainingDataById(collectionId,trainingDataId);
        return trainingData;
    }
    @ApiOperation(value="label query", notes="query label")
    @GetMapping("/collections/{collectionId}/trainingData/{trainingDataId}/labels")
    @ResponseBody
    public List<AITrainingData.AILabel> getLabels(@PathVariable("collectionId")String collectionId, @PathVariable("trainingDataId")String trainingDataId){
        List<AITrainingData.AILabel> labels=trainingDataQuery.getLabels(collectionId,trainingDataId);
        return labels;
    }
    //ogc image download
    @ApiOperation(value="image download", notes="download image")
    @GetMapping("/collections/{collectionId}/trainingData/{trainingDataId}/image")
    @ResponseBody
    public Result<String> getImage(HttpServletResponse response, @PathVariable("collectionId")String collectionId, @PathVariable("trainingDataId")String trainingDataId){
        Result<String> result = new Result<>();
        try {
            response=trainingDataQuery.getImage(response,collectionId,trainingDataId);
            result.setSuccess(true);
            result.setResult("下载完成！");
            return result;
        } catch (Exception e) {
            log.error("下载文件失败,错误信息: " + e.getMessage());
            result.error500("下载文件失败");
            return result;
        }
    }
}
