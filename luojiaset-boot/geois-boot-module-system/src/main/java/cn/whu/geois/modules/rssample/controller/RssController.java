package cn.whu.geois.modules.rssample.controller;

import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.modules.rssample.analysis.RssQuery;
import cn.whu.geois.modules.rssample.config.RssMinioConfig;
import cn.whu.geois.modules.rssample.entity.*;
import cn.whu.geois.modules.rssample.service.RssFileService;
import cn.whu.geois.modules.rssample.service.RssPostgresqlService;
import cn.whu.geois.modules.rssample.util.MailUtil;
import cn.whu.geois.modules.rssample.util.PBECoder;
import cn.whu.geois.modules.rssample.util.RssMinioUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.minio.ObjectStat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/1/27 21:28
 */
@CrossOrigin(origins = "*",maxAge = 3600)//解决跨域问题
@Slf4j
//@Api(tags="Rssample API")
@RestController
@RequestMapping("/rssample")
public class RssController {
    @Autowired
    private RssFileService rssFileService;
    @Autowired
    private RssPostgresqlService rssPostgresqlService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RssController.class);
    @Autowired
    private RssQuery rssQuery;

    @Autowired
    private RssMinioUtil rssMinioUtil;

    @GetMapping("/updateDatasetImage")
    @ResponseBody
    public Result<String> updateDatasetImage(@RequestParam("field")String field,
                                             @RequestParam("imagePath")String imagePath,
                                                @RequestParam("datasetName")String datasetName,
                                                @RequestParam("version")String version){
        Result<String> result=new Result<>();
        boolean insert=rssFileService.updateDatasetImage(field,imagePath,datasetName,version);
        if (!insert){
            result.error500("更新失败");
        }else {
            result.setResult("更新成功");
            result.setSuccess(true);
        }
        return result;
    }


    //缩略图上传接口
    @GetMapping("/uploadSampleThumb")
    @ResponseBody
    public Result<String> uploadSampleThumb(@RequestParam("filePath")String filePath,
                                      @RequestParam("dataSetName")String dataSetName){
        Result<String> result=new Result<>();
        boolean insert=rssFileService.uploadSampleThumb(filePath,dataSetName);
        if (!insert){
            result.error500("插入失败！");
        }else {
            result.setResult("插入成功！");
            result.setSuccess(true);
        }
        return result;
    }


    //图像转png接口
    @GetMapping("/toPng")
    @ResponseBody
    public Result<String> toPng(@RequestParam("filePath")String filePath,@RequestParam("outputPath")String outputPath)throws IOException,Exception {
        Result<String> result=new Result<>();
        boolean trans=rssFileService.toPng(filePath,outputPath);
        if (!trans){
            result.error500("转换失败！");
        }else {
            result.setResult("转换成功！");
            result.setSuccess(true);
        }
        return result;
    }

    //场景分类数据集导入接口
    @RequestMapping("/insertSCSample")
    @ResponseBody
    public Result<String> insertSCSample(@RequestParam("path") String path,@RequestParam("classPath") String classPath,@RequestParam("sheetNum") int sheetNum) throws Exception {
        Result<String> result = new Result<>();
        boolean insert = rssFileService.insertSCSample(path,classPath,sheetNum);
        if(!insert){
            result.error500("插入失败！");
        }else{
            result.setResult("插入成功！");
            result.success("操作成功！");
        }
        return result;
    }
    //目标检测数据集导入接口
//    @ApiOperation(value="目标检测数据集导入", notes="目标检测数据集导入")
    @GetMapping("/insertODSample")
    @ResponseBody
    public Result<String> insertODSample(@RequestParam("path")String path,@RequestParam("dataSetName")String dataSetName,
                                         @RequestParam("classMapPath")String classMapPath)throws IOException,Exception {
        Result<String> result=new Result<>();
        boolean insert=rssFileService.insertODSample(path,dataSetName,classMapPath);
        if (!insert){
            result.error500("插入失败！");
        }else {
            result.setResult("插入成功！");
            result.setSuccess(true);
        }
        return result;
    }
    //变化检测数据集导入接口
    @RequestMapping("/insertCDSample")
    @ResponseBody
    public Result<String> insertCDSample(@RequestParam("path")String path, @RequestParam("classMapPath")String classMapPath, @RequestParam("sheetNum")int sheetNum, @RequestParam("labelSuffix")String labelSuffix){
        Result<String> result = new Result<>();
        boolean insert = rssFileService.insertCDSample(path, classMapPath, sheetNum,labelSuffix);
        if(!insert){
            result.error500("插入失败！");
        }else{
            result.setResult("插入成功！");
            result.success("操作成功");
        }

        return result;
    }

    @GetMapping("/toUnitOdAnn")
    @ResponseBody
    public Result<String> toUnitOdAnn(@RequestParam("path")String path,@RequestParam("dataSetName")String dataSetName,
                                         @RequestParam("classMapPath")String classMapPath)throws IOException,Exception {
        Result<String> result=new Result<>();
        boolean insert=rssFileService.toUnitOdAnn(path,dataSetName,classMapPath);
        if (!insert){
            result.error500("插入失败！");
        }else {
            result.setResult("插入成功！");
            result.setSuccess(true);
        }
        return result;
    }
    @GetMapping("/toUnitOdClass")
    @ResponseBody
    public Result<String> toUnitOdClass(@RequestParam("path")String path,@RequestParam("dataSetName")String dataSetName,
                                      @RequestParam("classMapPath")String classMapPath)throws IOException,Exception {
        Result<String> result=new Result<>();
        boolean insert=rssFileService.toUnitOdClass(path,dataSetName,classMapPath);
        if (!insert){
            result.error500("插入失败！");
        }else {
            result.setResult("插入成功！");
            result.setSuccess(true);
        }
        return result;
    }

    //3D数据集组合查询接口
    @GetMapping("/queryTdSampleDetail")
    @ResponseBody
    public List<RssTdSample> queryTdSampleDetail(@RequestParam("id") String id) {
        List<RssTdSample> rssTdSampleList = rssQuery.queryTdSampleDetail(id);
        if (rssTdSampleList.size() == 0) {
            System.out.println("无查询结果！");
        }
        return rssTdSampleList;
    }
    //3D简单信息查询接口
    @GetMapping("/queryTdSamples")
    @ResponseBody
    public  Result<IPage<RssTdSampleSimple>> queryTdSamples(@RequestParam("codes")String[] codes,
                                                            @RequestParam("datasetId")Integer[] datasetId,
                                                            @RequestParam("startTime")String startTime,
                                                            @RequestParam("endTime")String endTime,
                                                            @RequestParam("sampleQuality")String sampleQuality,
                                                            @RequestParam("sampleLabeler")String sampleLabeler,
                                                            @RequestParam("imageType")String imageType,
                                                            @RequestParam("instrument")String[] instrument,
                                                            @RequestParam("trnValueTest")Integer trnValueTest,
                                                            @RequestParam("wkt")String wkt,
                                                            @RequestParam("pageSize")Integer pageSize,
                                                            @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssTdSampleSimple>> result = new Result<IPage<RssTdSampleSimple>>();
        IPage<RssTdSampleSimple> pageList = rssQuery.getTdSamples(codes,datasetId,startTime,endTime,sampleQuality,sampleLabeler,imageType,instrument,trnValueTest,wkt,pageSize,pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    //3D过滤项查询接口
    @GetMapping("/queryTdSamplesFilter")
    @ResponseBody
    public Result<RssFilter> queryTdSamplesFilter(@RequestParam("codes") String[] codes,
                                                  @RequestParam("datasetId") Integer[] datasetId,
                                                  @RequestParam("startTime") String startTime,
                                                  @RequestParam("endTime") String endTime,
                                                  @RequestParam("sampleQuality") String sampleQuality,
                                                  @RequestParam("sampleLabeler") String sampleLabeler,
                                                  @RequestParam("imageType") String imageType,
                                                  @RequestParam("instrument") String[] instrument,
                                                  @RequestParam("trnValueTest") Integer trnValueTest,
                                                  @RequestParam("wkt") String wkt) {
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.getTdSamplesFilter(codes, datasetId, startTime, endTime, sampleQuality, sampleLabeler, imageType, instrument, trnValueTest, wkt);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }

    //目标检测数据集类别检测接口
    @GetMapping("/checkODClass")
    @ResponseBody
    public Result<String> checkODClass(@RequestParam("path")String path,@RequestParam("dataSetName")String dataSetName)throws IOException,Exception {
        Result<String> result=new Result<>();
        boolean insert=rssFileService.checkODClass(path,dataSetName);
        if (!insert){
            result.error500("插入失败！");
        }else {
            result.setResult("插入成功！");
            result.setSuccess(true);
        }
        return result;
    }


    //场景分类数据集组合查询接口
    @GetMapping("/combQuerySC")
    @ResponseBody
    public List<RssScSample> combQuerySC(@RequestParam("sampleId")String sampleId,
                                         @RequestParam("classId")String classId,
                                         @RequestParam("datasetId")String datasetId,
                                         @RequestParam("sampleSize")String sampleSize,
                                         @RequestParam("startTime")String startTime,
                                         @RequestParam("endTime")String endTime,
                                         @RequestParam("sampleQuality")String sampleQuality,
                                         @RequestParam("sampleLabeler")String sampleLabeler,
                                         @RequestParam("imageType")String imageType,
                                         @RequestParam("instrument")String instrument,
                                         @RequestParam("trnValueTest")String trnValueTest,
                                         @RequestParam("wkt")String wkt,
                                         @RequestParam("pageSize")int pageSize,
                                         @RequestParam("pageNo")int pageNo){
        List<RssScSample> rssScSampleList=rssQuery.combQuerySC(sampleId,classId,datasetId,sampleSize,startTime,endTime,sampleQuality,sampleLabeler,imageType,instrument,trnValueTest,wkt,pageSize,pageNo);
        if (rssScSampleList.size()==0){
            System.out.println("无查询结果！");
        }
        return rssScSampleList;
    }

    //目标检测数据集组合查询接口
    @GetMapping("/queryOdSampleDetail")
    @ResponseBody
    public List<RssOdSample> queryOdSampleDetail(@RequestParam("id")String id){
        List<RssOdSample> rssOdSampleList=rssQuery.queryOdSampleDetail(id);
        if (rssOdSampleList.size()==0){
            System.out.println("无查询结果！");
        }
        return rssOdSampleList;
    }

    //变化检测数据集组合查询接口
    @GetMapping("/queryCdSampleDetail")
    @ResponseBody
    public List<RssCdSample> queryCdSampleDetail(@RequestParam("id") String id) {
        List<RssCdSample> rssCdSampleList = rssQuery.queryCdSampleDetail(id);
        if (rssCdSampleList.size() == 0) {
            System.out.println("无查询结果！");
        }
        return rssCdSampleList;
    }
    //变化检测简单信息查询接口
    @GetMapping("/queryCdSamples")
    @ResponseBody
    public Result<IPage<RssCdSampleSimple>> queryCdSamples(@RequestParam("codes") String[] codes,
                                                           @RequestParam("datasetId") Integer[] datasetId,
                                                           @RequestParam("startTime") String startTime,
                                                           @RequestParam("endTime") String endTime,
                                                           @RequestParam("sampleQuality") String sampleQuality,
                                                           @RequestParam("sampleLabeler") String sampleLabeler,
                                                           @RequestParam("preImageType") String preImageType,
                                                           @RequestParam("postImageType") String postImageType,
                                                           @RequestParam("preInstrument") String[] preInstrument,
                                                           @RequestParam("postInstrument") String[] postInstrument,
                                                           @RequestParam("trnValueTest") Integer trnValueTest,
                                                           @RequestParam("wkt") String wkt,
                                                           @RequestParam("pageSize") Integer pageSize,
                                                           @RequestParam("pageNo") Integer pageNo) {
        Result<IPage<RssCdSampleSimple>> result = new Result<IPage<RssCdSampleSimple>>();
        IPage<RssCdSampleSimple> pageList = rssQuery.getCdSamples(codes, datasetId, startTime, endTime, sampleQuality, sampleLabeler, preImageType,postImageType, preInstrument, postInstrument,trnValueTest, wkt, pageSize, pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    //变化检测过滤项查询接口
    @GetMapping("/queryCdSamplesFilter")
    @ResponseBody
    public Result<RssFilter> queryCdSamplesFilter(@RequestParam("codes") String[] codes,
                                                  @RequestParam("datasetId") Integer[] datasetId,
                                                  @RequestParam("startTime") String startTime,
                                                  @RequestParam("endTime") String endTime,
                                                  @RequestParam("sampleQuality") String sampleQuality,
                                                  @RequestParam("sampleLabeler") String sampleLabeler,
                                                  @RequestParam("preImageType") String preImageType,
                                                  @RequestParam("postImageType") String postImageType,
                                                  @RequestParam("preInstrument") String[] preInstrument,
                                                  @RequestParam("postInstrument") String[] postInstrument,
                                                  @RequestParam("trnValueTest") Integer trnValueTest,
                                                  @RequestParam("wkt") String wkt) {
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.getCdSamplesFilter(codes, datasetId, startTime, endTime, sampleQuality, sampleLabeler, preImageType,postImageType, preInstrument,postInstrument, trnValueTest, wkt);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }




    //数据集详细信息查询接口
    @GetMapping("/queryDatasetDetail")
    @ResponseBody
    public List<RssDataset> queryDatasetDetail(@RequestParam("datasetId")String datasetId){
        List<RssDataset> rssDatasetList=rssQuery.queryDatasetDetail(datasetId);
        if (rssDatasetList.size()==0){
            System.out.println("无查询结果！");
        }else {
            System.out.println("查询到"+rssDatasetList.size()+"个结果！");
        }
        return rssDatasetList;
    }
    //数据集简单信息查询接口
    @GetMapping("/queryDatasets")
    @ResponseBody
    public  Result<IPage<RssDatasetSimple>> queryDatasets(@RequestParam("taskType")String taskType,
                                                                          @RequestParam("keyword")String keyword,
                                                                          @RequestParam("startTime")String startTime,
                                                                          @RequestParam("endTime")String endTime,
                                                                          @RequestParam("sortMode")String sortMode,
                                                                          @RequestParam("pageSize")Integer pageSize,
                                                                          @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssDatasetSimple>> result = new Result<IPage<RssDatasetSimple>>();
        IPage<RssDatasetSimple> pageList = rssQuery.getDatasets(taskType,keyword,startTime,endTime,sortMode,pageSize,pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    //用户须知信息查询接口
    @PostMapping("/queryUserNotes")
    @ResponseBody
    public Result<HashMap<String,List<String>>> queryUserNotes(@RequestBody RssOrderRequest rssOrderRequest){
        Result<HashMap<String,List<String>>> result=new Result<>();
        String taskType=rssOrderRequest.getTaskType();
        String[] sampleId=rssOrderRequest.getSampleId();
        HashMap<String,List<String>> datasetNameCitesMap=rssQuery.queryUserNotes(taskType,sampleId);
        result.setSuccess(true);
        result.setResult(datasetNameCitesMap);
        return result;
    }

    //订单生成接口
    @PostMapping("/createOrder")
    @ResponseBody
    public Result<String> createOrder(@RequestBody RssOrderRequest rssOrderRequest){
        Result<String> result=new Result<>();

        String userId=rssOrderRequest.getUserId();
        String taskType=rssOrderRequest.getTaskType();
        String[] sampleId=rssOrderRequest.getSampleId();
        String create = rssQuery.createOrder(userId,taskType,sampleId);
        result.setSuccess(true);
        result.setResult(create);
        return result;
    }
    @GetMapping("/deleteOrder")
    @ResponseBody
    public Result<String> delectOrder(@RequestParam("orderNum")String[] orderNum){
        Result<String> result=new Result<>();
        String delect = rssQuery.deleteOrder(orderNum);
        if (delect.equals("删除成功！！！")){
            result.setSuccess(true);
        }else {
            result.error500("删除成功！！！");
        }
        result.setResult(delect);
        return result;
    }
    //普通用户订单信息查询接口
    @GetMapping("/queryOrderInfo")
    @ResponseBody
    public  Result<IPage<RssOrderInfo>> queryOrderInfo(@RequestParam("userId")String userId,
                                                       @RequestParam("tradeStatus")Integer[] tradeStatus,
                                                       @RequestParam("isAsc")Boolean isAsc,
                                                          @RequestParam("pageSize")Integer pageSize,
                                                          @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssOrderInfo>> result = new Result<IPage<RssOrderInfo>>();
        IPage<RssOrderInfo> pageList = rssQuery.queryOrderInfo(userId,tradeStatus,isAsc,pageSize,pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    //管理员订单信息查询接口
    @GetMapping("/queryOrderInfoAdmin")
    @ResponseBody
    public  Result<IPage<RssOrderInfo>> queryOrderInfoAdmin(@RequestParam("userId")String[] userId,
                                                       @RequestParam("tradeStatus")Integer[] tradeStatus,
                                                       @RequestParam("isAsc")Boolean isAsc,
                                                       @RequestParam("pageSize")Integer pageSize,
                                                       @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssOrderInfo>> result = new Result<IPage<RssOrderInfo>>();
        IPage<RssOrderInfo> pageList = rssQuery.queryOrderInfoAdmin(userId,tradeStatus,isAsc,pageSize,pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    @GetMapping("/queryOrderInfoFilter")
    @ResponseBody
    public  Result<RssFilter> queryOrderInfoFilter(@RequestParam("userId")String userId,
                                                       @RequestParam("tradeStatus")Integer[] tradeStatus){
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.queryOrderInfoFilter(userId,tradeStatus);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }
    //管理员订单信息查过滤项询接口
    @GetMapping("/queryOrderInfoFilterAdmin")
    @ResponseBody
    public  Result<RssFilter> queryOrderInfoFilterAdmin(@RequestParam("userId")String[] userId,
                                                   @RequestParam("tradeStatus")Integer[] tradeStatus){
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.queryOrderInfoFilterAdmin(userId,tradeStatus);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }
    //订单明细查询接口
    @GetMapping("/queryOrderItem")
    @ResponseBody
    public  Result<IPage<RssOrderItem>> queryOrderItem(@RequestParam("orderNum")String orderNum,
                                                       @RequestParam("pageSize")Integer pageSize,
                                                       @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssOrderItem>> result = new Result<IPage<RssOrderItem>>();
        IPage<RssOrderItem> pageList = rssQuery.queryOrderItem(orderNum,pageSize,pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    //数据集简单信息列举接口
    @GetMapping("/listDatasets")
    @ResponseBody
    public  Result<List<RssDatasetSimple>> listDatasets(){
        Result<List<RssDatasetSimple>> result = new Result<List<RssDatasetSimple>>();
        List<RssDatasetSimple> rssDatasetSimples = rssQuery.listDatasets();
//        System.out.println("rssDatasetSimples.size()"+rssDatasetSimples.size());
        result.setSuccess(true);
        result.setResult(rssDatasetSimples);
        return result;
    }

    //目标检测简单信息查询接口
    @GetMapping("/queryOdSamples")
    @ResponseBody
    public  Result<IPage<RssOdSampleSimple>> queryOdSamples(@RequestParam("codes")String[] codes,
                                                            @RequestParam("datasetId")Integer[] datasetId,
                                                              @RequestParam("startTime")String startTime,
                                                              @RequestParam("endTime")String endTime,
                                                              @RequestParam("sampleQuality")String sampleQuality,
                                                              @RequestParam("sampleLabeler")String sampleLabeler,
                                                              @RequestParam("labelBbox")String labelBbox,
                                                              @RequestParam("imageType")String imageType,
                                                              @RequestParam("instrument")String[] instrument,
                                                              @RequestParam("trnValueTest")Integer trnValueTest,
                                                              @RequestParam("wkt")String wkt,
                                                              @RequestParam("pageSize")Integer pageSize,
                                                             @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssOdSampleSimple>> result = new Result<IPage<RssOdSampleSimple>>();
        IPage<RssOdSampleSimple> pageList = rssQuery.getOdSamples(codes,datasetId,startTime,endTime,sampleQuality,sampleLabeler,labelBbox,imageType,instrument,trnValueTest,wkt,pageSize,pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    //目标检测过滤项查询接口
    @GetMapping("/queryOdSamplesFilter")
    @ResponseBody
    public  Result<RssFilter> queryOdSamplesFilter(@RequestParam("codes")String[] codes,
                                                            @RequestParam("datasetId")Integer[] datasetId,
                                                            @RequestParam("startTime")String startTime,
                                                            @RequestParam("endTime")String endTime,
                                                            @RequestParam("sampleQuality")String sampleQuality,
                                                            @RequestParam("sampleLabeler")String sampleLabeler,
                                                            @RequestParam("labelBbox")String labelBbox,
                                                            @RequestParam("imageType")String imageType,
                                                            @RequestParam("instrument")String[] instrument,
                                                            @RequestParam("trnValueTest")Integer trnValueTest,
                                                            @RequestParam("wkt")String wkt){
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.getOdSamplesFilter(codes,datasetId,startTime,endTime,sampleQuality,sampleLabeler,labelBbox,imageType,instrument,trnValueTest,wkt);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }
    //场景数据集组合查询接口
    @GetMapping("/queryScSampleDetail")
    @ResponseBody
    public List<RssScSample> queryScSampleDetail(@RequestParam("id") String id) {
        List<RssScSample> rssScSampleList = rssQuery.queryScSampleDetail(id);
        if (rssScSampleList.size() == 0) {
            System.out.println("无查询结果！");
        }
        return rssScSampleList;
    }

    //场景分类简单信息查询接口
        @GetMapping("queryScSamples")
    @ResponseBody
    public Result<IPage<RssScSampleSimple>> queryScSamples(@RequestParam("codes")String[] codes,
                                                           @RequestParam("datasetId")Integer[] datasetId,
                                                           @RequestParam("imageType")String imageType,
                                                           @RequestParam("instrument")String[] instrument,
                                                           @RequestParam("wkt")String wkt,
                                                           @RequestParam("pageSize")Integer pageSize,
                                                           @RequestParam("pageNo")Integer pageNo){
        Result<IPage<RssScSampleSimple>> result = new Result<>();
        IPage<RssScSampleSimple> scSamples = rssQuery.getScSamples(codes, datasetId, imageType, instrument, wkt, pageSize, pageNo);
        result.setSuccess(true);
        result.setResult(scSamples);
        return result;
    }
    //场景分类过滤项查询接口
    @GetMapping("/queryScSamplesFilter")
    @ResponseBody
    public  Result<RssFilter> queryScSamplesFilter(@RequestParam("codes")String[] codes,
                                                   @RequestParam("datasetId")Integer[] datasetId,
                                                   @RequestParam("imageType")String imageType,
                                                   @RequestParam("instrument")String[] instrument,
                                                   @RequestParam("wkt")String wkt){
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.getScSamplesFilter(codes,datasetId,imageType,instrument,wkt);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }
    //地物分类数据集组合查询接口
    @GetMapping("/queryLcSampleDetail")
    @ResponseBody
    public List<RssLcSample> queryLcSampleDetail(@RequestParam("id") String id) {
        List<RssLcSample> rssLcSampleList = rssQuery.queryLcSampleDetail(id);
        if (rssLcSampleList.size() == 0) {
            System.out.println("无查询结果！");
        }
        return rssLcSampleList;
    }
    //地物分类简单信息查询接口
    @GetMapping("/queryLcSamples")
    @ResponseBody
    public Result<IPage<RssLcSampleSimple>> queryLcSamples(@RequestParam("codes") String[] codes,
                                                           @RequestParam("datasetId") Integer[] datasetId,
                                                           @RequestParam("startTime") String startTime,
                                                           @RequestParam("endTime") String endTime,
                                                           @RequestParam("sampleQuality") String sampleQuality,
                                                           @RequestParam("sampleLabeler") String sampleLabeler,
                                                           @RequestParam("imageType") String imageType,
                                                           @RequestParam("instrument") String[] instrument,
                                                           @RequestParam("trnValueTest") Integer trnValueTest,
                                                           @RequestParam("wkt") String wkt,
                                                           @RequestParam("pageSize") Integer pageSize,
                                                           @RequestParam("pageNo") Integer pageNo) {
        Result<IPage<RssLcSampleSimple>> result = new Result<IPage<RssLcSampleSimple>>();
        IPage<RssLcSampleSimple> pageList = rssQuery.getLcSamples(codes, datasetId, startTime, endTime, sampleQuality, sampleLabeler, imageType, instrument, trnValueTest, wkt, pageSize, pageNo);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    //地物分类过滤项查询接口
    @GetMapping("/queryLcSamplesFilter")
    @ResponseBody
    public Result<RssFilter> queryOdSamplesFilter(@RequestParam("codes") String[] codes,
                                                  @RequestParam("datasetId") Integer[] datasetId,
                                                  @RequestParam("startTime") String startTime,
                                                  @RequestParam("endTime") String endTime,
                                                  @RequestParam("sampleQuality") String sampleQuality,
                                                  @RequestParam("sampleLabeler") String sampleLabeler,
                                                  @RequestParam("imageType") String imageType,
                                                  @RequestParam("instrument") String[] instrument,
                                                  @RequestParam("trnValueTest") Integer trnValueTest,
                                                  @RequestParam("wkt") String wkt) {
        Result<RssFilter> result = new Result<RssFilter>();
        RssFilter rssFilter = rssQuery.getLcSamplesFilter(codes, datasetId, startTime, endTime, sampleQuality, sampleLabeler, imageType, instrument, trnValueTest, wkt);
        result.setSuccess(true);
        result.setResult(rssFilter);
        return result;
    }
    //类别映射查询接口
//    @GetMapping("/queryClasses")
//    @ResponseBody
//    public  Result<String[][]> queryClasses(@RequestParam("taskType")String taskType,
//                                                         @RequestParam("datasetId")Integer datasetId){
//        Result<String[][]> result = new Result<String[][]>();
//        if (taskType!=null&& !taskType.isEmpty()){
//            String[][] classesArr=rssQuery.queryClasses(taskType,datasetId);
//            result.setSuccess(true);
//            result.setResult(classesArr);
//        }else {
//            result.error500("查询类别失败,任务类型不能为空！");
//        }
//        return result;
//    }
    @GetMapping("/queryClasses")
    @ResponseBody
    public  Result<String[][]> queryClasses(@RequestParam("taskType")String taskType,
                                            @RequestParam("datasetId")Integer datasetId,@RequestParam("isEnglish")boolean isEnglish){
        Result<String[][]> result = new Result<String[][]>();
        if (taskType!=null&& !taskType.isEmpty()){
            String[][] classesArr=rssQuery.queryClasses(taskType,datasetId,isEnglish);
            result.setSuccess(true);
            result.setResult(classesArr);
        }else {
            result.error500("查询类别失败,任务类型不能为空！");
        }
        return result;
    }
    //场景分类体系表导入
    @GetMapping("/insertSCClass")
    @ResponseBody
    public Result<String> insertSCClass(@RequestParam("filePath")String filePath){
        Result<String> result=new Result<>();
        boolean insert=rssFileService.insertSCClass(filePath);
        if (!insert){
            result.error500("插入失败");
        }else {
            result.setResult("插入成功");
            result.setSuccess(true);
        }
        return result;
    }
    //目标识别体系表导入
    @GetMapping("/insertODClass")
    @ResponseBody
    public Result<String> insertODClass(@RequestParam("filePath")String filePath){
        Result<String> result = new Result<>();
        boolean insert = rssFileService.insertODClass(filePath);
        if(!insert){
            result.error500("插入失败");
        }else{
            result.setResult("插入成功");
            result.setSuccess(true);
        }
        return result;
    }



    @GetMapping("init")
    public String init() {
        return "file";
    }

    /**
     * 上传
     *
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request) {
        JSONObject res = null;
        try {
            res = rssMinioUtil.uploadFile(file, "product");
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "上传失败");
        }
        return res.toJSONString();
    }
    //国情监测体系入库
    @GetMapping("/insertGQJCClass")
    @ResponseBody
    public Result<String> insertGQJCClass(@RequestParam("filePath")String filePath){
        Result<String> result = new Result<>();
        boolean insert = rssFileService.insertGQJCClass(filePath);
        if(!insert){
            result.error500("插入失败");
        }else{
            result.setResult("插入成功");
            result.setSuccess(true);
        }
        return result;
    }

    //将minio文件压缩并放到minio系统
    @GetMapping("/compress")
    public Result<Boolean> compress(@RequestParam("bucketName")String bucketName,@RequestParam("filePaths")String[] filePaths,@RequestParam("outPath")String outPath) throws IOException{
        Result<Boolean> result = new Result<>();
        Boolean compress = rssFileService.compressMinio(bucketName,filePaths,outPath);
        if(!compress){
            result.error500("压缩并上传失败");
        }else{
            result.setResult(true);
            result.setSuccess(true);
        }
        return result;
    }
    //根据订单编号，将文件压缩上传到minio系统，更新订单状态
    //输入订单编号，输出下载url
    @GetMapping("compressSystem")
    public Result<String> compressSystem(@RequestParam("orderNum")String orderNum) throws IOException, MessagingException {
        Result<String> result = new Result<>();

        String url = rssFileService.compressSystem(orderNum);
        MailUtil mailUtil = new MailUtil();
        String userEmail = rssQuery.queryEmailByOrderNum(orderNum);
        String subject = "[LuojiaSET] Order Messages";
        String html = "The data order you submitted on the LuojiaSET remote sensing image sharing service platform has been reviewed and the order number is " +  orderNum + ", please download it in time.";
        mailUtil.sendHtmlMail(userEmail,subject,html);
        if(url==null){
            result.error500("压缩并上传失败");
        }else{
            result.setResult(url);
            result.setSuccess(true);
        }
        return result;

    }

    @GetMapping("/file")
    public Result<String> download(HttpServletResponse response, @RequestParam("fileName") String fileName,
                                   @RequestParam("taskType") String taskType) {
        Result<String> result = new Result<>();
        try {

            String bucketName=null;
            switch (taskType){
                case "lc":
                    bucketName="land-cover";
                    break;
                case "od":
                    bucketName="object-detection";
                    break;
                case "sc":
                    bucketName="scene-classification";
                    break;
                default:
                    break;
            }
            //获取存储捅名
            //获取对象信息和对象的元数据。
            ObjectStat objectStat = rssMinioUtil.statObject(bucketName, fileName);
            System.out.println("fileName"+fileName);
            //setContentType 设置发送到客户机的响应的内容类型
            response.setContentType(objectStat.contentType());
            System.out.println("objectStat.name()"+objectStat.name());
            //设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(objectStat.name(), "UTF-8"));
            //文件流
            InputStream object = rssMinioUtil.getObject(bucketName, fileName);
            //设置文件大小
            response.setHeader("Content-Length", String.valueOf(objectStat.length()));
            IOUtils.copy(object, response.getOutputStream());
            //关闭流
            object.close();
            result.setSuccess(true);
            result.setResult("下载完成！");
            return result;
        } catch (Exception e) {
            log.error("下载文件失败,错误信息: " + e.getMessage());
            result.error500("下载文件失败");
            return result;
        }
    }
    @GetMapping("/download/{pbecode}")
    public Result<String> download(HttpServletResponse response, @PathVariable("pbecode") String pbecode) {
        Result<String> result = new Result<>();
        try {
            System.out.println("pbecode:"+pbecode);
            String newPbecode= pbecode.replace("_","/");
            System.out.println("newPbecode:"+newPbecode);

            //获取存储捅名
            //获取对象信息和对象的元数据。
            byte[] saltNew="xxx".getBytes();
            String pwd = "efg";

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] data = decoder.decodeBuffer(newPbecode);
            byte[] output = PBECoder.decrypt(data, pwd, saltNew);
            String outputStr = new String(output);
            String[] outputs=outputStr.split("&");
            String fileName=outputs[0];
            System.out.println("fileName"+fileName);
            String bucketName=outputs[1];
            ObjectStat objectStat = rssMinioUtil.statObject(bucketName, fileName);
            System.out.println("objectStat.name()"+objectStat.name());
            //setContentType 设置发送到客户机的响应的内容类型
            response.setContentType(objectStat.contentType());
            //设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(objectStat.name(), "UTF-8"));
            //文件流
            InputStream object = rssMinioUtil.getObject(bucketName, fileName);
            //设置文件大小
            response.setHeader("Content-Length", String.valueOf(objectStat.length()));
            IOUtils.copy(object, response.getOutputStream());
            //关闭流
            object.close();
            result.setSuccess(true);
            result.setResult("下载完成！");
            return result;
        } catch (Exception e) {
            log.error("下载文件失败,错误信息: " + e.getMessage());
            result.error500("下载文件失败");
            return result;
        }
    }

    @GetMapping("/queryClassNumByDataset")
    public Result<JSONObject[]> querySCClassNumByDataset(@RequestParam("taskType") String taskType, @RequestParam("datasetId")  Integer datasetId, @RequestParam("isEnglish")  Boolean isEnglish) {

        Result<JSONObject[]> result = new Result<>();
        List<JSONObject> jsonObjectList = rssQuery.queryClassNumByDataset(taskType, datasetId,isEnglish);
        result.setSuccess(true);
        result.setResult(jsonObjectList.toArray(new JSONObject[jsonObjectList.size()]));
        return result;
    }

    @GetMapping("/getDataNumInfo")
    @ResponseBody
    public Result<JSONObject> getDataNumInfo(){
        Result<JSONObject> result = new Result<>();
        JSONObject jsonObject = rssQuery.getDataNumInfo();
        result.setSuccess(true);
        result.setResult(jsonObject);
        return result;

    }
    //统计数据集的存储大小,除了td
    @GetMapping("/getDatasetSize")
    @ResponseBody
    public Result<Double> getDatasetSize(@RequestParam("datasetId")  Integer datasetId){
        Result<Double> result = new Result<>();
        Double datasetSize = rssQuery.getDatasetSize(datasetId);
        result.setSuccess(true);
        result.setResult(datasetSize);
        return result;

    }
    @GetMapping("/emailTest")
    @ResponseBody
    public void emailTest(@RequestParam("orderNum") String orderNum){
        MailUtil mailUtil = new MailUtil();
        String userEmail = rssQuery.queryEmailByOrderNum(orderNum);
        System.out.println("userEmail " + userEmail);
    }

    @PostMapping(value="/UserDatasetUpload")
    @ResponseBody
    public Result<JSONObject> userDatasetUpload(@RequestParam("file") MultipartFile file){
        Result<JSONObject> result = new Result<>();
        JSONObject res = new JSONObject();

        try{
//            res = rssOBSUtil.uploadFile(file,"user-tmp-dataset");
            res = rssMinioUtil.uploadFile(file,"user-tmp-dataset");

        }catch (Exception e) {
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "上传失败");
        }

        result.setSuccess(true);
        result.setResult(res);
        return result;

    }

    @GetMapping("insertUserDatasetInfo")
    @ResponseBody
    public Result<Boolean> insertUserDatasetInfo(@RequestParam("filePath")String filePath, @RequestParam("userCode") String userCode,@RequestParam("datasetName")String datasetName,  @RequestParam("isPublic")String isPublic,
                                                 @RequestParam("datasetTask")String datasetTask, @RequestParam("datasetSensor")String datasetSensor,
                                                 @RequestParam("datasetResolution")String datasetResolution, @RequestParam("datasetImageType")String datasetImageType,
                                                 @RequestParam("datasetRef")String datasetRef,@RequestParam("datasetKeyword")String datasetKeyword,@RequestParam("datasetContactor")String datasetContactor,
                                                 @RequestParam("datasetContact")String datasetContact,@RequestParam("datasetEmail")String datasetEmail,@RequestParam("datasetAddress")String datasetAddress,
                                                 @RequestParam("datasetRemark")String datasetRemark){
        Result<Boolean> result = new Result<>();
        Boolean res = false;
        try{
            res =rssPostgresqlService.insertUserDatasetInfo(filePath,userCode,datasetName,isPublic,datasetTask,datasetSensor,datasetResolution,datasetImageType,
                    datasetRef,datasetKeyword,datasetContactor,datasetContact,datasetEmail,datasetAddress,datasetRemark);


        }catch (Exception e){
            e.printStackTrace();

        }
        result.setResult(res);
        return result;









    }



}
