package cn.whu.geois.modules.rssample.analysis;


import cn.whu.geois.modules.rssample.entity.*;

import cn.whu.geois.modules.rssample.service.IRssOrderInfoService;
import cn.whu.geois.modules.rssample.service.IRssOrderItemService;
import cn.whu.geois.modules.rssample.service.RssMysqlService;
import cn.whu.geois.modules.rssample.service.RssPostgresqlService;
import cn.whu.geois.modules.rssample.util.RssMinioUtil;
import cn.whu.geois.modules.rssample.util.RssOrderUtil;
import cn.whu.geois.modules.rssample.xml.RssOdAnn;
import cn.whu.geois.modules.system.service.ISysUserService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.minio.ObjectStat;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Array;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author czp
 * @version 1.0
 * @date 2021/3/13 16:36
 */
@Component
public class RssQuery {
    @Autowired
    private RssPostgresqlService rssPostgresqlService;
    @Autowired
    private RssOrderUtil rssOrderUtil;
    @Autowired
    private IRssOrderInfoService iRssOrderInfoService;
    @Autowired
    private IRssOrderItemService iRssOrderItemService;
    @Autowired
    private RssMysqlService rssMysqlService;
    @Autowired
    private RssMinioUtil rssMinioUtil;
    @Autowired
    private ISysUserService iSysUserService;

    public List<RssScSample> queryScSampleDetail(String id) {
        List<RssScSample> rssScSamples = new LinkedList<>();
        String combinationParam = "";
        //查询条件
        if (!id.isEmpty()) {
            String idParam = "id=" + id;
            combinationParam = combinationParam + " AND " + idParam;
        }
        rssScSamples = rssPostgresqlService.queryScSampleDetail(combinationParam);
        return rssScSamples;
    }
    public IPage<RssScSampleSimple> getScSamples(String[] codes,Integer[] datasetId, String imageType,
                                                 String[] instrument, String wkt,Integer pageSize,Integer pageNo){
        QueryWrapper<RssScSampleSimple> queryWrapper = new QueryWrapper<>();
        System.out.println("datasetId.length:"+datasetId.length);
        if (datasetId.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<datasetId.length;i++){
                    if (i==datasetId.length-1){
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]);
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }else {
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]).or();
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        if(!imageType.isEmpty()){
            queryWrapper.eq("scsample.image_type",imageType);
        }
        if (instrument.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<instrument.length;i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        String combinationParam = "";
        if(wkt!=null&&wkt.length()!=0){
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;"+wkt+"')";
            combinationParam = combinationParam +" AND "+ wktParam;
        }

        if(codes.length!=0){
            for (int i = 0; i < codes.length; i++) {
                if(i==codes.length-1){
                    if(!codes[i].isEmpty()||!codes[i].equals("")){
                        queryWrapper.eq("code",codes[i]);
                    }else{
                        System.out.println("WARNING:CODE INPUT NUM["+i+"] IS NULL!!!!");
                    }
                }else{
                    if(!codes[i].isEmpty()||!codes[i].equals("")){
                        queryWrapper.eq("code",codes[i]).or();
                    }else{
                        System.out.println("WARNING:CODE INPUT NUM["+i+"] IS NULL!!!!");
                    }
                }
            }
        }

        Page<RssScSampleSimple> page = new Page<>(pageNo, pageSize);
        IPage<RssScSampleSimple> scSamples = rssPostgresqlService.getScSamples(page, queryWrapper, combinationParam);

        return  scSamples;
    }
    public RssFilter getScSamplesFilter(String[]codes, Integer[] datasetId, String imageType,
                                        String[] instrument,
                                        String wkt){
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate=null;
        Date endDate=null;
        int flag=0;
        System.out.println("datasetId.length:"+datasetId.length);
        for (int i=0;i<datasetId.length;i++){
            if (datasetId[i]!=null){
                flag=1;
            }
        }
        if (datasetId.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<datasetId.length;i++){
                    if (i==datasetId.length-1){
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]);
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }else {
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]).or();
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }

        if (!imageType.isEmpty()){
            queryWrapper.eq("scsample.image_type",imageType);
            flag=1;
        }
        System.out.println("instrument.length:"+instrument.length);
        for (int i=0;i<instrument.length;i++){
            if (!instrument[i].isEmpty()||!instrument[i].equals("")){
                flag=1;
            }
        }
        if (instrument.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<instrument.length;i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }

        System.out.println("codes.length:"+codes.length);
        for (int i=0;i<codes.length;i++){
            if (!codes[i].isEmpty()||!codes[i].equals("")){
                flag=1;
            }
        }
        if (codes.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<codes.length;i++){
                    if (i==codes.length-1){
                        if (!codes[i].isEmpty()||!codes[i].equals("")){
                            queryWrapper1.eq("code",codes[i]);
                            System.out.println("codes["+i+"]:"+codes[i]);
                        }else {
                            System.out.println("codes["+i+"]为空");
                        }
                    }else {
                        if (!codes[i].isEmpty()||!codes[i].equals("")){
                            queryWrapper1.eq("code",codes[i]).or();
                            System.out.println("codes["+i+"]:"+codes[i]);
                        }else {
                            System.out.println("codes["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam="";
        if (flag==0){
            combinationParam="WHERE 1=1";
        }
        if (wkt!=null&&wkt.length()!=0){
            String wktParam="ST_Intersects(sample_area,'SRID=4326;"+wkt+"')";
            combinationParam=combinationParam+" AND "+wktParam;
        }


        RssFilter rssFilter=rssPostgresqlService.getScSamplesFilter(queryWrapper,combinationParam);
        return rssFilter;
    }
    public List<RssCdSample> queryCdSampleDetail(String id) {
        List<RssCdSample> rssCdSamples = new LinkedList<>();
        String combinationParam = "";
        //查询条件
        if (!id.isEmpty()) {
            String idParam = "id=" + id;
            combinationParam = combinationParam + " AND " + idParam;
        }
        rssCdSamples = rssPostgresqlService.queryCdSampleDetail(combinationParam);
        return rssCdSamples;
    }
    public IPage<RssCdSampleSimple> getCdSamples(String[] codes, Integer[] datasetId,
                                                 String startTime, String endTime,
                                                 String sampleQuality, String sampleLabeler,
                                                 String preImageType,String postImageType,
                                                 String[] preInstrument,String[] postInstrument, Integer trnValueTest,
                                                 String wkt, Integer pageSize, Integer pageNo) {
        QueryWrapper<RssCdSampleSimple> queryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        int flag = 0;
        System.out.println("datasetId.length:" + datasetId.length);
        for (int i = 0; i < datasetId.length; i++) {
            if (datasetId[i] != null) {
                flag = 1;
            }
        }
        if (datasetId.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < datasetId.length; i++) {
                    if (i == datasetId.length - 1) {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]);
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    } else {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]).or();
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime != null && !startTime.isEmpty()) {
                startDate = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDate = sdf.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null) {
            Timestamp timestamp = new Timestamp(startDate.getTime());
            queryWrapper.ge("pre_sample_date", timestamp);
            flag = 1;
        }
        if (endDate != null) {
            Timestamp timestamp = new Timestamp(endDate.getTime());
            queryWrapper.le("post_sample_date", timestamp);
            flag = 1;
        }

        if (!sampleQuality.isEmpty()) {
            queryWrapper.eq("sample_quality", sampleQuality);
            flag = 1;
        }
        if (!sampleLabeler.isEmpty()) {
            queryWrapper.eq("sample_labler", sampleLabeler);
            flag = 1;
        }
        if (!preImageType.isEmpty()) {
            queryWrapper.eq("pre_image_type", preImageType);
            flag = 1;
        }
        if (!postImageType.isEmpty()) {
            queryWrapper.eq("post_image_type", postImageType);
            flag = 1;
        }
        System.out.println("preInstrument.length:" + preInstrument.length);
        for (int i = 0; i < preInstrument.length; i++) {
            if (!preInstrument[i].isEmpty() || !preInstrument[i].equals("")) {
                flag = 1;
            }
        }
        if (preInstrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < preInstrument.length; i++) {

                    if (i == preInstrument.length - 1) {

                        if (!preInstrument[i].isEmpty() || !preInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.pre_instrument", preInstrument[i]);
                            System.out.println("preInstrument[" + i + "]:" + preInstrument[i]);
                        } else {
                            System.out.println("preInstrument[" + i + "]为空");
                        }
                    } else {
                        if (!preInstrument[i].isEmpty() || !preInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.pre_instrument", preInstrument[i]).or();
                            System.out.println("preInstrument[" + i + "]:" + preInstrument[i]);
                        } else {
                            System.out.println("preInstrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        System.out.println("postInstrument.length:" + postInstrument.length);
        for (int i = 0; i < postInstrument.length; i++) {
            if (!postInstrument[i].isEmpty() || !postInstrument[i].equals("")) {
                flag = 1;
            }
        }
        if (postInstrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < postInstrument.length; i++) {

                    if (i == postInstrument.length - 1) {

                        if (!postInstrument[i].isEmpty() || !postInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.post_instrument", postInstrument[i]);
                            System.out.println("postInstrument[" + i + "]:" + postInstrument[i]);
                        } else {
                            System.out.println("postInstrument[" + i + "]为空");
                        }
                    } else {
                        if (!postInstrument[i].isEmpty() || !postInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.post_instrument", postInstrument[i]).or();
                            System.out.println("postInstrument[" + i + "]:" + postInstrument[i]);
                        } else {
                            System.out.println("postInstrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }

        if (trnValueTest != null) {
            queryWrapper.eq("trn_value_test", trnValueTest);
            flag = 1;
        }
        System.out.println("codes.length:" + codes.length);
        for (int i = 0; i < codes.length; i++) {
            if (!codes[i].isEmpty() || !codes[i].equals("")) {
                flag = 1;
            }
        }
        if (codes.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < codes.length; i++) {
                    if (i == codes.length - 1) {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]);
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    } else {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]).or();
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam = "";
        if (flag == 0) {
            combinationParam = "WHERE 1=1";
        }
        if (wkt != null && wkt.length() != 0) {
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;" + wkt + "')";
            combinationParam = combinationParam + " AND " + wktParam;
        }

        Page<RssCdSampleSimple> page = new Page<RssCdSampleSimple>(pageNo, pageSize);
        IPage<RssCdSampleSimple> pageList = rssPostgresqlService.getCdSamples(page, queryWrapper, combinationParam);
        return pageList;
    }
    public RssFilter getCdSamplesFilter(String[] codes, Integer[] datasetId,
                                        String startTime, String endTime,
                                        String sampleQuality, String sampleLabeler,
                                        String preImageType,String postImageType,
                                        String[] preInstrument,String[] postInstrument, Integer trnValueTest,
                                        String wkt) {
        QueryWrapper<String> queryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        int flag = 0;
        System.out.println("datasetId.length:" + datasetId.length);
        for (int i = 0; i < datasetId.length; i++) {
            if (datasetId[i] != null) {
                flag = 1;
            }
        }
        if (datasetId.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < datasetId.length; i++) {
                    if (i == datasetId.length - 1) {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]);
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    } else {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]).or();
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime != null && !startTime.isEmpty()) {
                startDate = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDate = sdf.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null) {
            Timestamp timestamp = new Timestamp(startDate.getTime());
            queryWrapper.ge("pre_sample_date", timestamp);
            flag = 1;
        }
        if (endDate != null) {
            Timestamp timestamp = new Timestamp(endDate.getTime());
            queryWrapper.le("post_sample_date", timestamp);
            flag = 1;
        }

        if (!sampleQuality.isEmpty()) {
            queryWrapper.eq("sample_quality", sampleQuality);
            flag = 1;
        }
        if (!sampleLabeler.isEmpty()) {
            queryWrapper.eq("sample_labler", sampleLabeler);
            flag = 1;
        }
        if (!preImageType.isEmpty()) {
            queryWrapper.eq("pre_image_type", preImageType);
            flag = 1;
        }
        if (!postImageType.isEmpty()) {
            queryWrapper.eq("post_image_type", postImageType);
            flag = 1;
        }
        System.out.println("preInstrument.length:" + preInstrument.length);
        for (int i = 0; i < preInstrument.length; i++) {
            if (!preInstrument[i].isEmpty() || !preInstrument[i].equals("")) {
                flag = 1;
            }
        }
        if (preInstrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < preInstrument.length; i++) {

                    if (i == preInstrument.length - 1) {

                        if (!preInstrument[i].isEmpty() || !preInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.pre_instrument", preInstrument[i]);
                            System.out.println("preInstrument[" + i + "]:" + preInstrument[i]);
                        } else {
                            System.out.println("preInstrument[" + i + "]为空");
                        }
                    } else {
                        if (!preInstrument[i].isEmpty() || !preInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.pre_instrument", preInstrument[i]).or();
                            System.out.println("preInstrument[" + i + "]:" + preInstrument[i]);
                        } else {
                            System.out.println("preInstrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        System.out.println("postInstrument.length:" + postInstrument.length);
        for (int i = 0; i < postInstrument.length; i++) {
            if (!postInstrument[i].isEmpty() || !postInstrument[i].equals("")) {
                flag = 1;
            }
        }
        if (postInstrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < postInstrument.length; i++) {

                    if (i == postInstrument.length - 1) {

                        if (!postInstrument[i].isEmpty() || !postInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.pre_instrument", postInstrument[i]);
                            System.out.println("postInstrument[" + i + "]:" + postInstrument[i]);
                        } else {
                            System.out.println("postInstrument[" + i + "]为空");
                        }
                    } else {
                        if (!postInstrument[i].isEmpty() || !postInstrument[i].equals("")) {
                            queryWrapper1.eq("rcs.pre_instrument", postInstrument[i]).or();
                            System.out.println("postInstrument[" + i + "]:" + postInstrument[i]);
                        } else {
                            System.out.println("postInstrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest != null) {
            queryWrapper.eq("trn_value_test", trnValueTest);
            flag = 1;
        }
        System.out.println("codes.length:" + codes.length);
        for (int i = 0; i < codes.length; i++) {
            if (!codes[i].isEmpty() || !codes[i].equals("")) {
                flag = 1;
            }
        }
        if (codes.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < codes.length; i++) {
                    if (i == codes.length - 1) {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]);
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    } else {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]).or();
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam = "";
        if (flag == 0) {
            combinationParam = "WHERE 1=1";
        }
        if (wkt != null && wkt.length() != 0) {
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;" + wkt + "')";
            combinationParam = combinationParam + " AND " + wktParam;
        }


        RssFilter rssFilter = rssPostgresqlService.getCdSamplesFilter(queryWrapper, combinationParam);
        return rssFilter;
    }
    public List<RssLcSample> queryLcSampleDetail(String id) {
        List<RssLcSample> rssLcSamples = new LinkedList<>();
        String combinationParam = "";
        //查询条件
        if (!id.isEmpty()) {
            String idParam = "id=" + id;
            combinationParam = combinationParam + " AND " + idParam;
        }
        rssLcSamples = rssPostgresqlService.queryLcSampleDetail(combinationParam);
        return rssLcSamples;
    }
    public IPage<RssLcSampleSimple> getLcSamples(String[] codes, Integer[] datasetId,
                                                 String startTime, String endTime,
                                                 String sampleQuality, String sampleLabeler,
                                                 String imageType,
                                                 String[] instrument, Integer trnValueTest,
                                                 String wkt, Integer pageSize, Integer pageNo) {
        QueryWrapper<RssLcSampleSimple> queryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        int flag = 0;
        System.out.println("datasetId.length:" + datasetId.length);
        for (int i = 0; i < datasetId.length; i++) {
            if (datasetId[i] != null) {
                flag = 1;
            }
        }
        if (datasetId.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < datasetId.length; i++) {
                    if (i == datasetId.length - 1) {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]);
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    } else {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]).or();
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime != null && !startTime.isEmpty()) {
                startDate = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDate = sdf.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null) {
            Timestamp timestamp = new Timestamp(startDate.getTime());
            queryWrapper.ge("sample_date", timestamp);
            flag = 1;
        }
        if (endDate != null) {
            Timestamp timestamp = new Timestamp(endDate.getTime());
            queryWrapper.le("sample_date", timestamp);
            flag = 1;
        }

        if (!sampleQuality.isEmpty()) {
            queryWrapper.eq("sample_quality", sampleQuality);
            flag = 1;
        }
        if (!sampleLabeler.isEmpty()) {
            queryWrapper.eq("sample_labler", sampleLabeler);
            flag = 1;
        }
        if (!imageType.isEmpty()) {
            queryWrapper.eq("rls.image_type", imageType);
            flag = 1;
        }
        System.out.println("instrument.length:" + instrument.length);
        for (int i = 0; i < instrument.length; i++) {
            if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                flag = 1;
            }
        }
        if (instrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < instrument.length; i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("rls.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("rls.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest != null) {
            queryWrapper.eq("trn_value_test", trnValueTest);
            flag = 1;
        }
        System.out.println("codes.length:" + codes.length);
        for (int i = 0; i < codes.length; i++) {
            if (!codes[i].isEmpty() || !codes[i].equals("")) {
                flag = 1;
            }
        }
        if (codes.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < codes.length; i++) {
                    if (i == codes.length - 1) {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]);
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    } else {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]).or();
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam = "";
        if (flag == 0) {
            combinationParam = "WHERE 1=1";
        }
        if (wkt != null && wkt.length() != 0) {
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;" + wkt + "')";
            combinationParam = combinationParam + " AND " + wktParam;
        }

        Page<RssLcSampleSimple> page = new Page<RssLcSampleSimple>(pageNo, pageSize);
        IPage<RssLcSampleSimple> pageList = rssPostgresqlService.getLcSamples(page, queryWrapper, combinationParam);
        return pageList;
    }
    public RssFilter getLcSamplesFilter(String[] codes, Integer[] datasetId,
                                        String startTime, String endTime,
                                        String sampleQuality, String sampleLabeler,
                                        String imageType,
                                        String[] instrument, Integer trnValueTest,
                                        String wkt) {
        QueryWrapper<String> queryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        int flag = 0;
        System.out.println("datasetId.length:" + datasetId.length);
        for (int i = 0; i < datasetId.length; i++) {
            if (datasetId[i] != null) {
                flag = 1;
            }
        }
        if (datasetId.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < datasetId.length; i++) {
                    if (i == datasetId.length - 1) {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]);
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    } else {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]).or();
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime != null && !startTime.isEmpty()) {
                startDate = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDate = sdf.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null) {
            Timestamp timestamp = new Timestamp(startDate.getTime());
            queryWrapper.ge("sample_date", timestamp);
            flag = 1;
        }
        if (endDate != null) {
            Timestamp timestamp = new Timestamp(endDate.getTime());
            queryWrapper.le("sample_date", timestamp);
            flag = 1;
        }

        if (!sampleQuality.isEmpty()) {
            queryWrapper.eq("sample_quality", sampleQuality);
            flag = 1;
        }
        if (!sampleLabeler.isEmpty()) {
            queryWrapper.eq("sample_labler", sampleLabeler);
            flag = 1;
        }
        if (!imageType.isEmpty()) {
            queryWrapper.eq("rls.image_type", imageType);
            flag = 1;
        }
        System.out.println("instrument.length:" + instrument.length);
        for (int i = 0; i < instrument.length; i++) {
            if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                flag = 1;
            }
        }
        if (instrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < instrument.length; i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest != null) {
            queryWrapper.eq("trn_value_test", trnValueTest);
            flag = 1;
        }
        System.out.println("codes.length:" + codes.length);
        for (int i = 0; i < codes.length; i++) {
            if (!codes[i].isEmpty() || !codes[i].equals("")) {
                flag = 1;
            }
        }
        if (codes.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < codes.length; i++) {
                    if (i == codes.length - 1) {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]);
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    } else {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]).or();
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam = "";
        if (flag == 0) {
            combinationParam = "WHERE 1=1";
        }
        if (wkt != null && wkt.length() != 0) {
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;" + wkt + "')";
            combinationParam = combinationParam + " AND " + wktParam;
        }


        RssFilter rssFilter = rssPostgresqlService.getLcSamplesFilter(queryWrapper, combinationParam);
        return rssFilter;
    }
    /***
     * query classes including union class name and it's code
     * @param taskType
     * @param datasetId
     * @return
     */
    public String[][] queryClasses(String taskType,Integer datasetId,boolean isEnglish){
        //先根据任务类型和数据集id获得classMap
        QueryWrapper<RssClassMapSimple> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("task_type",taskType);
        if (datasetId!=null){
            queryWrapper.eq("dataset_id",datasetId);
        }

        List<RssClassMapSimple> rssClassMapSimples=rssPostgresqlService.getClassMaps(queryWrapper);
        //遍历删除重复的
        List<String> unionNames=new LinkedList<>();
        List<RssClassMapSimple> rssClassMapSimplesUniq=new LinkedList<>();
        System.out.println("rssClassMapSimples.size():"+rssClassMapSimples.size());
        for (int i=0;i<rssClassMapSimples.size();i++){
            RssClassMapSimple rssClassMapSimple=rssClassMapSimples.get(i);
            String uniClassName=rssClassMapSimple.getUniClassName();
            if (!unionNames.contains(uniClassName)){
                rssClassMapSimplesUniq.add(rssClassMapSimple);
                unionNames.add(uniClassName);
            }else {
                System.out.println("uniClassName:"+uniClassName+"重复");
            }
        }
        System.out.println("rssClassMapSimplesUniq.size():"+rssClassMapSimplesUniq.size());


        List<String[]> classesList=new LinkedList<>();
        for (int i=0;i<rssClassMapSimplesUniq.size();i++){
            RssClassMapSimple rssClassMapSimple=rssClassMapSimplesUniq.get(i);
            String uniClassName=rssClassMapSimple.getUniClassName();
//            System.out.println("uniClassName:"+uniClassName);
            RssParam rssParam=new RssParam();
            rssParam.setObjectName(uniClassName);
            //根据任务类型查询不同的类别表
            switch (taskType){
                case "od":
                    List<RssOdClass> rssOdClassList=getOdClass(rssParam);

                    if (rssOdClassList.size()==0){
                        System.out.println("od_class不存在"+uniClassName);
                    }
                    if (isEnglish==true){
                        String code =rssOdClassList.get(0).getCode();
                        //String description=rssOdClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,code};
                        classesList.add(classPair);
                    }else {
                        String code =rssOdClassList.get(0).getCode();
                        String description=rssOdClassList.get(0).getDescription().toString();
                        String[] classPair={description,code};
                        classesList.add(classPair);
                    }
                    break;
                case "sc":
                    List<RssScClass> rssScClassList=getScClass(rssParam);

                    if (rssScClassList.size()==0){
                        System.out.println("sc_class不存在"+uniClassName);
                    }
                    if (isEnglish==true){

                        String code =rssScClassList.get(0).getCode();
                        //String description=rssScClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,code};
                        classesList.add(classPair);
                    }
                    else {
                        String code =rssScClassList.get(0).getCode();
                        String description=rssScClassList.get(0).getDescription().toString();
                        String[] classPair={description,code};
                        classesList.add(classPair);
                    }
                    break;
                case "lc":
                    List<RssGqjcClass> rssGqjcClassList=getGqjcClass(rssParam);

                    if (rssGqjcClassList.size()==0){
                        System.out.println("gqjc_class不存在"+uniClassName);
                    }
                    if(isEnglish==true) {
                        String code =rssGqjcClassList.get(0).getCode();
                        //String description=rssGqjcClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,code};
                        classesList.add(classPair);
                    }
                    else {
                        String code =rssGqjcClassList.get(0).getCode();
                        String description=rssGqjcClassList.get(0).getDescription().toString();
                        String[] classPair={description,code};
                        classesList.add(classPair);
                    }
                    break;
                case "cd":
                    List<RssGqjcClass> rssGqjcCdClassList=getGqjcClass(rssParam);

                    if (rssGqjcCdClassList.size()==0){
                        System.out.println("gqjc_class不存在"+uniClassName);
                    }
                    if (isEnglish==true)
                    {
                        String code =rssGqjcCdClassList.get(0).getCode();
                        //String description=rssGqjcCdClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,code};
                        classesList.add(classPair);
                    }
                    else {
                        String code =rssGqjcCdClassList.get(0).getCode();
                        String description=rssGqjcCdClassList.get(0).getDescription().toString();
                        String[] classPair={description,code};
                        classesList.add(classPair);
                    }
                    break;
                default:
                    break;
            }
        }
        String[][] pureClassesArr=classesList.toArray(new String[classesList.size()][]);
        return pureClassesArr;
    }
    public String[][] queryClasses(String taskType,Integer datasetId){
        //先根据任务类型和数据集id获得classMap
        QueryWrapper<RssClassMapSimple> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("task_type",taskType);
        if (datasetId!=null){
            queryWrapper.eq("dataset_id",datasetId);
        }

        List<RssClassMapSimple> rssClassMapSimples=rssPostgresqlService.getClassMaps(queryWrapper);
        //遍历删除重复的
        List<String> unionNames=new LinkedList<>();
        List<RssClassMapSimple> rssClassMapSimplesUniq=new LinkedList<>();
        System.out.println("rssClassMapSimples.size():"+rssClassMapSimples.size());
        for (int i=0;i<rssClassMapSimples.size();i++){
            RssClassMapSimple rssClassMapSimple=rssClassMapSimples.get(i);
            String uniClassName=rssClassMapSimple.getUniClassName();
            if (!unionNames.contains(uniClassName)){
                rssClassMapSimplesUniq.add(rssClassMapSimple);
                unionNames.add(uniClassName);
            }else {
                System.out.println("uniClassName:"+uniClassName+"重复");
            }
        }
        System.out.println("rssClassMapSimplesUniq.size():"+rssClassMapSimplesUniq.size());


        List<String[]> classesList=new LinkedList<>();
        for (int i=0;i<rssClassMapSimplesUniq.size();i++){
            RssClassMapSimple rssClassMapSimple=rssClassMapSimplesUniq.get(i);
            String uniClassName=rssClassMapSimple.getUniClassName();
//            System.out.println("uniClassName:"+uniClassName);
            RssParam rssParam=new RssParam();
            rssParam.setObjectName(uniClassName);
            //根据任务类型查询不同的类别表
            switch (taskType){
                case "od":
                    List<RssOdClass> rssOdClassList=getOdClass(rssParam);

                    if (rssOdClassList.size()==0){
                        System.out.println("od_class不存在"+uniClassName);
                    }else {

                        String code =rssOdClassList.get(0).getCode();
                        String description=rssOdClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,description,code};
                        classesList.add(classPair);
                    }
                    break;
                case "sc":
                    List<RssScClass> rssScClassList=getScClass(rssParam);

                    if (rssScClassList.size()==0){
                        System.out.println("sc_class不存在"+uniClassName);
                    }else {

                        String code =rssScClassList.get(0).getCode();
                        String description=rssScClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,description,code};
                        classesList.add(classPair);


                    }
                    break;
                case "lc":
                    List<RssGqjcClass> rssGqjcClassList=getGqjcClass(rssParam);

                    if (rssGqjcClassList.size()==0){
                        System.out.println("gqjc_class不存在"+uniClassName);
                    }else {

                        String code =rssGqjcClassList.get(0).getCode();
                        String description=rssGqjcClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,description,code};
                        classesList.add(classPair);

                    }
                    break;
                case "cd":
                    List<RssGqjcClass> rssGqjcCdClassList=getGqjcClass(rssParam);

                    if (rssGqjcCdClassList.size()==0){
                        System.out.println("gqjc_class不存在"+uniClassName);
                    }else {

                        String code =rssGqjcCdClassList.get(0).getCode();
                        String description=rssGqjcCdClassList.get(0).getDescription().toString();
                        String[] classPair={uniClassName,description,code};
                        classesList.add(classPair);

                    }
                    break;
                default:
                    break;
            }
        }
        String[][] pureClassesArr=classesList.toArray(new String[classesList.size()][]);
        return pureClassesArr;
    }
    public List<RssOdClass> getOdClass(RssParam rssParam){
        List<RssOdClass> rssOdClasses=new LinkedList<>();
        String combinationParam="";
        //查询条件
        String objectName=rssParam.getObjectName();
//        System.out.println("objectName:"+objectName);
        if (objectName!=null){
            String objectNameParam="name='"+objectName+"'";
            combinationParam=combinationParam+" AND "+objectNameParam;
        }
//        System.out.println("combinationParam"+combinationParam);
        rssOdClasses=rssPostgresqlService.getOdClass(combinationParam);
        return rssOdClasses;
    }
    public List<RssScClass> getScClass(RssParam rssParam){
        List<RssScClass> rssScClasses=new LinkedList<>();
        String combinationParam="";
        //查询条件
        String objectName=rssParam.getObjectName();
//        System.out.println("objectName:"+objectName);
        if (objectName!=null){
            String objectNameParam="name='"+objectName+"'";
            combinationParam=combinationParam+" AND "+objectNameParam;
        }
//        System.out.println("combinationParam"+combinationParam);
        rssScClasses=rssPostgresqlService.getScClass(combinationParam);
        return rssScClasses;
    }

    public List<RssGqjcClass> getGqjcClass(RssParam rssParam){
        List<RssGqjcClass> rssScClasses=new LinkedList<>();
        String combinationParam="";
        //查询条件
        String objectName=rssParam.getObjectName();
//        System.out.println("objectName:"+objectName);
        if (objectName!=null){
            String objectNameParam="name='"+objectName+"'";
            combinationParam=combinationParam+" AND "+objectNameParam;
        }
//        System.out.println("combinationParam"+combinationParam);
        rssScClasses=rssPostgresqlService.getGqjcClass(combinationParam);
        return rssScClasses;
    }

    public List<RssOdSample> queryOdSampleDetail(String id){
        List<RssOdSample> rssOdSamples=new LinkedList<>();
        String combinationParam="";
        //查询条件
        if (!id.isEmpty()){
            String idParam="id="+id;
            combinationParam=combinationParam+" AND "+idParam;
        }
        rssOdSamples=rssPostgresqlService.queryOdSampleDetail(combinationParam);
        return rssOdSamples;
    }


    public List<RssDataset> queryDatasetDetail(String datasetId){
        List<RssDataset> rssDatasets=new LinkedList<>();
        String combinationParam="";
        //查询条件

        if (!datasetId.isEmpty()){
            String datasetIdParam="id="+datasetId;
            combinationParam=combinationParam+" AND "+datasetIdParam;
        }
        rssDatasets=rssPostgresqlService.getDatasetDetail(combinationParam);
        return rssDatasets;
    }
    public List<RssDataset> combQueryDataset(String datasetName,String version){
        List<RssDataset> rssDatasets=new LinkedList<>();
        String combinationParam="";
        //查询条件

        if (!datasetName.isEmpty()){
            String datasetIdParam="name='"+datasetName+"'";
            combinationParam=combinationParam+" AND "+datasetIdParam;
        }
        if (!version.isEmpty()){
            String versionParam="dataset_version='"+version+"'";
            combinationParam=combinationParam+" AND "+versionParam;
        }
        rssDatasets=rssPostgresqlService.getDatasetDetail(combinationParam);
        return rssDatasets;
    }
    public List<RssScSample> combQuerySC(String sampleId,String classId,
                                         String datasetId, String sampleSize,
                                         String startTime, String endTime,
                                         String sampleQuality, String sampleLabeler,
                                         String imageType,
                                         String instrument, String trnValueTest,
                                         String wkt, int pageSize, int pageNo){
        List<RssScSample> rssScSamples=new LinkedList<>();
        String combinationParam="";
        //查询条件
        if (!sampleId.isEmpty()){
            String sampleIdParam="sample_id='"+sampleId+"'";
            combinationParam=combinationParam+" AND "+sampleIdParam;
        }

        if (!classId.isEmpty()){
            String classIdParam="class_id='"+classId+"'";
            combinationParam=combinationParam+" AND "+classIdParam;
        }
        if (!datasetId.isEmpty()){
            String datasetIdParam="dataset_id="+datasetId;
            combinationParam=combinationParam+" AND "+datasetIdParam;
        }
        if (!sampleSize.isEmpty()){
            String sampleSizeParam="sample_size='"+sampleSize+"'";
            combinationParam=combinationParam+" AND "+sampleSizeParam;
        }
        if (!wkt.isEmpty()){
            String wktParam="ST_Intersects(sample_area,'SRID=4326;"+wkt+"')";
            combinationParam=combinationParam+" AND "+wktParam;
        }
        if (!startTime.isEmpty()){
            String startTimeParam="sample_date >= '"+startTime+"'";
            combinationParam=combinationParam+" AND "+startTimeParam;
        }
        if (!endTime.isEmpty()){
            String endTimeParam="sample_date <='"+endTime+"'";
            combinationParam=combinationParam+" AND "+endTimeParam;
        }
        if (!sampleQuality.isEmpty()){
            String sampleQualityParam="sample_quality ='"+sampleQuality+"'";
            combinationParam=combinationParam+" AND "+sampleQualityParam;
        }
        if (!sampleLabeler.isEmpty()){
            String sampleLablerParam="sample_labler ='"+sampleLabeler+"'";
            combinationParam=combinationParam+" AND "+sampleLablerParam;
        }
        if (!imageType.isEmpty()){
            String imageTypeParam="image_type ='"+imageType+"'";
            combinationParam=combinationParam+" AND "+imageTypeParam;
        }
        if (!instrument.isEmpty()){
            String instrumentParam="instrument ='"+instrument+"'";
            combinationParam=combinationParam+" AND "+instrumentParam;
        }
        if (!trnValueTest.isEmpty()){
            String trnValueTestParam="trn_value_test ="+trnValueTest;
            combinationParam=combinationParam+" AND "+trnValueTestParam;
        }
        if (pageSize>0&&pageNo>0){
            int Offset=(pageNo-1)*10;
            String pageParam="LIMIT "+String.valueOf(pageSize)+" OFFSET "+String.valueOf(Offset);
            combinationParam=combinationParam+" "+pageParam;
        }
        rssScSamples=rssPostgresqlService.getScSample(combinationParam);
        return rssScSamples;
    }

    public HashMap<String,List<String>> queryUserNotes(String taskType,String[] sampleId){
        HashMap<String,List<String>>datasetNameCitesMap=new HashMap<>();
        HashMap<String,String>sampleIdDatasetMap=new HashMap<>();
        List<RssOrderItem> rssOrderItemList=rssPostgresqlService.getSampleIdDatasetMap(taskType);
        for (RssOrderItem rssOrderItem:rssOrderItemList){
            sampleIdDatasetMap.put(rssOrderItem.getSampleId().toString(),rssOrderItem.getDatasetName());
        }
        for (String subSampleId:sampleId){
            String datasetName= sampleIdDatasetMap.get(subSampleId);
            if(datasetName!=null){
                if (!datasetNameCitesMap.containsKey(datasetName)){
                    datasetNameCitesMap.put(datasetName,null);
                }
            }else {
                System.out.println("subSampleId:"+subSampleId+"不存在");
            }

        }
        for (String datasetName:datasetNameCitesMap.keySet()){
            //获得datasetName，通过datasetName查询cites
            String pureDatasetName=datasetName.substring(0,datasetName.lastIndexOf("-"));
            String version=datasetName.substring(datasetName.lastIndexOf("-")+1);
            System.out.println("pureDatasetName"+pureDatasetName);
            System.out.println("version"+version);
            List<RssDataset> datasets=combQueryDataset(pureDatasetName,version);
            System.out.println(datasets.size());
            if (datasets.size()==1){
                RssDataset rssDataset=datasets.get(0);

                if (rssDataset.getDatasetCite()!=null){
                    String datasetCite=rssDataset.getDatasetCite().toString();
                    List<String> cites= Arrays.asList(datasetCite.split(","));
                    datasetNameCitesMap.put(datasetName,cites);
                }else {
                    System.out.println("datasetName"+datasetName+"没有引用");
                }
            }else {
                System.out.println("数据集信息有误！！！");
                return null;
            }
        }
        return datasetNameCitesMap;
    }

    public String createOrder(String userId,String taskType,String[] sampleId){
        List<RssOrderInfo> rssOrderInfos=new LinkedList<>();
        List<RssOrderItem> rssOrderItems=new LinkedList<>();
        //获取当前sc_sample最大id
        Integer orderInfoId= rssPostgresqlService.getMaxOrderInfoId();
        if(orderInfoId==null){
            orderInfoId = 0;
        }
        Integer orderItemId= rssPostgresqlService.getMaxOrderItemId();
        if(orderItemId==null){
            orderItemId = 0;
        }
        HashMap<String,String>sampleIdDatasetMap=new HashMap<>();
        List<RssOrderItem> rssOrderItemList=rssPostgresqlService.getSampleIdDatasetMap(taskType);
        for (RssOrderItem rssOrderItem:rssOrderItemList){
            sampleIdDatasetMap.put(rssOrderItem.getSampleId().toString(),rssOrderItem.getDatasetName());
        }

        RssOrderInfo rssOrderInfo=new RssOrderInfo();

//        Integer userIdInt=Integer.parseInt(userId);
//        String orderNum=rssOrderUtil.getOrderCode(userIdInt);
        String orderNum=rssOrderUtil.getOrderCode(userId);
        orderInfoId++;
        rssOrderInfo.setId(orderInfoId);
        rssOrderInfo.setUserId(userId);
        rssOrderInfo.setOrderNum(orderNum);
        rssOrderInfo.setPayStatus(0);
        rssOrderInfo.setTotalAmount(new BigDecimal(0));
        rssOrderInfo.setTradeStatus(0);
        rssOrderInfo.setCreateBy(userId);
        rssOrderInfo.setCreateTime(new Date(System.currentTimeMillis()));
        rssOrderInfo.setUpdateBy(null);
        rssOrderInfo.setUpdateTime(null);
        rssOrderInfo.setTaskType(taskType);

        Integer sampleNum=0;
        for (String subSampleId:sampleId){
            String datasetName= sampleIdDatasetMap.get(subSampleId);
            if(datasetName!=null){
                orderItemId++;
                sampleNum++;
                RssOrderItem rssOrderItem=new RssOrderItem();
                rssOrderItem.setId(orderItemId);
                rssOrderItem.setOrderNum(orderNum);
                rssOrderItem.setTaskType(taskType);
                rssOrderItem.setSampleId(Integer.parseInt(subSampleId));
                rssOrderItem.setDatasetName(datasetName);
                rssOrderItem.setCreateBy(userId);
                rssOrderItem.setCreateTime(new Date(System.currentTimeMillis()));
                rssOrderItem.setUpdateBy(null);
                rssOrderItem.setUpdateTime(null);
                rssOrderItems.add(rssOrderItem);
            }else {
                System.out.println("subSampleId:"+subSampleId+"不存在");
            }

        }
        rssOrderInfo.setSampleNum(sampleNum);
        rssOrderInfos.add(rssOrderInfo);
        boolean insertInfo= iRssOrderInfoService.insertBatch(rssOrderInfos);
        boolean insertItem= iRssOrderItemService.insertBatch(rssOrderItems);
        if (insertInfo&&insertItem){
            return "订单编号:"+orderNum;
        }else {
            return "创建失败！！！";
        }
    }
    public String deleteOrder(String[] orderNum){
        try {
            QueryWrapper<RssOrderItem> queryWrapperItem =new QueryWrapper<>();
            if (orderNum.length!=0){
                queryWrapperItem.and(queryWrapper1 -> {
                    for (int i=0;i<orderNum.length;i++){
                        if (i==orderNum.length-1){
                            if (orderNum[i]!=null){
                                queryWrapper1.eq("order_num",orderNum[i]);
                                System.out.println("orderNum["+i+"]:"+orderNum[i]);
                            }else {
                                System.out.println("orderNum["+i+"]为空");
                            }
                        }else {
                            if (orderNum[i]!=null){
                                queryWrapper1.eq("order_num",orderNum[i]).or();
                                System.out.println("orderNum["+i+"]:"+orderNum[i]);
                            }else {
                                System.out.println("orderNum["+i+"]为空");
                            }
                        }
                    }
                    return queryWrapper1;
                });
            }
            iRssOrderItemService.remove(queryWrapperItem);
            QueryWrapper<RssOrderInfo> queryWrapperInfo =new QueryWrapper<>();
            if (orderNum.length!=0){
                queryWrapperInfo.and(queryWrapper1 -> {
                    for (int i=0;i<orderNum.length;i++){
                        if (i==orderNum.length-1){
                            if (orderNum[i]!=null){
                                queryWrapper1.eq("order_num",orderNum[i]);
                                System.out.println("orderNum["+i+"]:"+orderNum[i]);
                            }else {
                                System.out.println("orderNum["+i+"]为空");
                            }
                        }else {
                            if (orderNum[i]!=null){
                                queryWrapper1.eq("order_num",orderNum[i]).or();
                                System.out.println("orderNum["+i+"]:"+orderNum[i]);
                            }else {
                                System.out.println("orderNum["+i+"]为空");
                            }
                        }
                    }
                    return queryWrapper1;
                });
            }
            iRssOrderInfoService.remove(queryWrapperInfo);
        }catch (Exception e){
            e.printStackTrace();
            return "删除失败！！！";
        }
        return "删除成功！！！";

    }

    public IPage<RssOrderInfo> queryOrderInfo(String userId,Integer[] tradeStatus,Boolean isAsc,Integer pageSize,Integer pageNo) {
        //获得用户的角色信息
//        String role=rssMysqlService.getUserRole(userId);
        QueryWrapper<RssOrderInfo> queryWrapper =new QueryWrapper<>();
        if (userId!=null&&!userId.isEmpty()){
            queryWrapper.eq("user_id",userId);
        }else {
            return null;
        }
//        if (role!=null){
//            if (!role.equals("admin")){
//                if (userId!=null&&!userId.isEmpty()){
//                    queryWrapper.eq("user_id",userId);
//                }
//            }
//        }else {
//            System.err.println("用户未注册！！！");
//            if (userId!=null&&!userId.isEmpty()){
//                queryWrapper.eq("user_id",userId);
//            }
//        }
        if (tradeStatus.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<tradeStatus.length;i++){
                    if (i==tradeStatus.length-1){
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]);
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }else {
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]).or();
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (isAsc!=null){
            if (isAsc){
                queryWrapper.orderByAsc("create_time");
            }else {
                queryWrapper.orderByDesc("create_time");
            }
        }else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<RssOrderInfo> page = new Page<RssOrderInfo>(pageNo, pageSize);
        IPage<RssOrderInfo> pageList = rssPostgresqlService.getOrderInfo(page, queryWrapper);
        return pageList;
    }
    public IPage<RssOrderInfo> queryOrderInfoAdmin(String[] userId,Integer[] tradeStatus,Boolean isAsc,Integer pageSize,Integer pageNo) {
        //获得用户的角色信息
//        String role=rssMysqlService.getUserRole(userId);
        QueryWrapper<RssOrderInfo> queryWrapper =new QueryWrapper<>();
        if (userId.length!=0){
            queryWrapper.and(queryWrapper2 -> {
                for (int i=0;i<userId.length;i++){
                    if (i==userId.length-1){
                        if (userId[i]!=null){
                            queryWrapper2.eq("user_id",userId[i]);
                            System.out.println("userId["+i+"]:"+userId[i]);
                        }else {
                            System.out.println("userId["+i+"]为空");
                        }
                    }else {
                        if (userId[i]!=null){
                            queryWrapper2.eq("user_id",userId[i]).or();
                            System.out.println("userId["+i+"]:"+userId[i]);
                        }else {
                            System.out.println("userId["+i+"]为空");
                        }
                    }
                }
                return queryWrapper2;
            });
        }
        if (tradeStatus.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<tradeStatus.length;i++){
                    if (i==tradeStatus.length-1){
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]);
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }else {
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]).or();
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (isAsc!=null){
            if (isAsc){
                queryWrapper.orderByAsc("create_time");
            }else {
                queryWrapper.orderByDesc("create_time");
            }
        }else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<RssOrderInfo> page = new Page<RssOrderInfo>(pageNo, pageSize);
        IPage<RssOrderInfo> pageList = rssPostgresqlService.getOrderInfo(page, queryWrapper);
        return pageList;
    }
    public RssFilter queryOrderInfoFilter(String userId,Integer[] tradeStatus) {
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        if (userId!=null&&!userId.isEmpty()){
            queryWrapper.eq("user_id",userId);
        }
        if (tradeStatus.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<tradeStatus.length;i++){
                    if (i==tradeStatus.length-1){
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]);
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }else {
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]).or();
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        RssFilter rssFilter = rssPostgresqlService.getOrderInfoFilter(queryWrapper);
        return rssFilter;
    }
    public RssFilter queryOrderInfoFilterAdmin(String[] userId,Integer[] tradeStatus) {
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        if (userId.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<userId.length;i++){
                    if (i==userId.length-1){
                        if (userId[i]!=null){
                            queryWrapper1.eq("user_id",userId[i]);
                            System.out.println("userId["+i+"]:"+userId[i]);
                        }else {
                            System.out.println("userId["+i+"]为空");
                        }
                    }else {
                        if (userId[i]!=null){
                            queryWrapper1.eq("user_id",userId[i]).or();
                            System.out.println("userId["+i+"]:"+userId[i]);
                        }else {
                            System.out.println("userId["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (tradeStatus.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<tradeStatus.length;i++){
                    if (i==tradeStatus.length-1){
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]);
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }else {
                        if (tradeStatus[i]!=null){
                            queryWrapper1.eq("trade_status",tradeStatus[i]).or();
                            System.out.println("tradeStatus["+i+"]:"+tradeStatus[i]);
                        }else {
                            System.out.println("tradeStatus["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        RssFilter rssFilter = rssPostgresqlService.getOrderInfoFilter(queryWrapper);
        return rssFilter;
    }
    public List<RssTdSample> queryTdSampleDetail(String id) {
        List<RssTdSample> rssTdSamples = new LinkedList<>();
        String combinationParam = "";
        //查询条件
        if (!id.isEmpty()) {
            String idParam = "id=" + id;
            combinationParam = combinationParam + " AND " + idParam;
        }
        rssTdSamples = rssPostgresqlService.queryTdSampleDetail(combinationParam);
        return rssTdSamples;
    }
    public IPage<RssTdSampleSimple> getTdSamples(String[] codes, Integer[] datasetId,
                                                 String startTime, String endTime,
                                                 String sampleQuality, String sampleLabeler,
                                                 String imageType,
                                                 String[] instrument, Integer trnValueTest,
                                                 String wkt, Integer pageSize, Integer pageNo) {
        QueryWrapper<RssTdSampleSimple> queryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        int flag = 0;
        System.out.println("datasetId.length:" + datasetId.length);
        for (int i = 0; i < datasetId.length; i++) {
            if (datasetId[i] != null) {
                flag = 1;
            }
        }
        if (datasetId.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < datasetId.length; i++) {
                    if (i == datasetId.length - 1) {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]);
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    } else {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]).or();
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime != null && !startTime.isEmpty()) {
                startDate = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDate = sdf.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null) {
            Timestamp timestamp = new Timestamp(startDate.getTime());
            queryWrapper.ge("sample_date", timestamp);
            flag = 1;
        }
        if (endDate != null) {
            Timestamp timestamp = new Timestamp(endDate.getTime());
            queryWrapper.le("sample_date", timestamp);
            flag = 1;
        }

        if (!sampleQuality.isEmpty()) {
            queryWrapper.eq("sample_quality", sampleQuality);
            flag = 1;
        }
        if (!sampleLabeler.isEmpty()) {
            queryWrapper.eq("sample_labler", sampleLabeler);
            flag = 1;
        }
        if (!imageType.isEmpty()) {
            queryWrapper.eq("ros.image_type", imageType);
            flag = 1;
        }
        System.out.println("instrument.length:" + instrument.length);
        for (int i = 0; i < instrument.length; i++) {
            if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                flag = 1;
            }
        }
        if (instrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < instrument.length; i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest != null) {
            queryWrapper.eq("trn_value_test", trnValueTest);
            flag = 1;
        }
        System.out.println("codes.length:" + codes.length);
        for (int i = 0; i < codes.length; i++) {
            if (!codes[i].isEmpty() || !codes[i].equals("")) {
                flag = 1;
            }
        }
        if (codes.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < codes.length; i++) {
                    if (i == codes.length - 1) {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]);
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    } else {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]).or();
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam = "";
        if (flag == 0) {
            combinationParam = "WHERE 1=1";
        }
        if (wkt != null && wkt.length() != 0) {
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;" + wkt + "')";
            combinationParam = combinationParam + " AND " + wktParam;
        }

        Page<RssTdSampleSimple> page = new Page<RssTdSampleSimple>(pageNo, pageSize);
        IPage<RssTdSampleSimple> pageList = rssPostgresqlService.getTdSamples(page, queryWrapper, combinationParam);
        return pageList;
    }

    public RssFilter getTdSamplesFilter(String[] codes, Integer[] datasetId,
                                        String startTime, String endTime,
                                        String sampleQuality, String sampleLabeler,
                                        String imageType,
                                        String[] instrument, Integer trnValueTest,
                                        String wkt) {
        QueryWrapper<String> queryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        int flag = 0;
        System.out.println("datasetId.length:" + datasetId.length);
        for (int i = 0; i < datasetId.length; i++) {
            if (datasetId[i] != null) {
                flag = 1;
            }
        }
        if (datasetId.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < datasetId.length; i++) {
                    if (i == datasetId.length - 1) {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]);
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    } else {
                        if (datasetId[i] != null) {
                            queryWrapper1.eq("dataset_id", datasetId[i]).or();
                            System.out.println("datasetId[" + i + "]:" + datasetId[i]);
                        } else {
                            System.out.println("datasetId[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime != null && !startTime.isEmpty()) {
                startDate = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDate = sdf.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null) {
            Timestamp timestamp = new Timestamp(startDate.getTime());
            queryWrapper.ge("sample_date", timestamp);
            flag = 1;
        }
        if (endDate != null) {
            Timestamp timestamp = new Timestamp(endDate.getTime());
            queryWrapper.le("sample_date", timestamp);
            flag = 1;
        }

        if (!sampleQuality.isEmpty()) {
            queryWrapper.eq("sample_quality", sampleQuality);
            flag = 1;
        }
        if (!sampleLabeler.isEmpty()) {
            queryWrapper.eq("sample_labler", sampleLabeler);
            flag = 1;
        }
        if (!imageType.isEmpty()) {
            queryWrapper.eq("ros.image_type", imageType);
            flag = 1;
        }
        System.out.println("instrument.length:" + instrument.length);
        for (int i = 0; i < instrument.length; i++) {
            if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                flag = 1;
            }
        }
        if (instrument.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < instrument.length; i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest != null) {
            queryWrapper.eq("trn_value_test", trnValueTest);
            flag = 1;
        }
        System.out.println("codes.length:" + codes.length);
        for (int i = 0; i < codes.length; i++) {
            if (!codes[i].isEmpty() || !codes[i].equals("")) {
                flag = 1;
            }
        }
        if (codes.length != 0) {
            queryWrapper.and(queryWrapper1 -> {
                for (int i = 0; i < codes.length; i++) {
                    if (i == codes.length - 1) {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]);
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    } else {
                        if (!codes[i].isEmpty() || !codes[i].equals("")) {
                            queryWrapper1.eq("code", codes[i]).or();
                            System.out.println("codes[" + i + "]:" + codes[i]);
                        } else {
                            System.out.println("codes[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam = "";
        if (flag == 0) {
            combinationParam = "WHERE 1=1";
        }
        if (wkt != null && wkt.length() != 0) {
            String wktParam = "ST_Intersects(sample_area,'SRID=4326;" + wkt + "')";
            combinationParam = combinationParam + " AND " + wktParam;
        }


        RssFilter rssFilter = rssPostgresqlService.getTdSamplesFilter(queryWrapper, combinationParam);
        return rssFilter;
    }
    public IPage<RssOrderItem> queryOrderItem(String orderNum,Integer pageSize,Integer pageNo) {
        QueryWrapper<RssOrderItem> queryWrapper =new QueryWrapper<>();
        if (orderNum!=null&&!orderNum.isEmpty()){
            queryWrapper.eq("order_num",orderNum);
        }
        queryWrapper.orderByAsc("id");
        Page<RssOrderItem> page = new Page<RssOrderItem>(pageNo, pageSize);
        IPage<RssOrderItem> pageList = rssPostgresqlService.getOrderItem(page, queryWrapper);
        return pageList;
    }
    public List<RssPair> queryOrderItemPath(String orderNum,String taskType) {
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        if (orderNum!=null&&!orderNum.isEmpty()){
            queryWrapper.eq("order_num",orderNum);
        }
        queryWrapper.orderByAsc("rss_"+taskType+"_sample.id");
        String columns="image_path,label_path";
        switch (taskType){
            case "sc":
                columns="image_path";
                break;
            case "cd":
                columns="pre_image_path,post_image_path,label_path";
                break;
            default:
                break;
        }
        List<RssPair> pairs = rssPostgresqlService.getOrderItemPath(queryWrapper,taskType,columns);
        return pairs;
    }
    public String queryOrderInfoTaskType(String orderNum) {
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        if (orderNum!=null&&!orderNum.isEmpty()){
            queryWrapper.eq("order_num",orderNum);
        }
        String taskType = rssPostgresqlService.getOrderInfoTaskType(queryWrapper);
        return taskType;
    }
    public String queryOrderInfoDownloadUrl(String orderNum) {
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        if (orderNum!=null&&!orderNum.isEmpty()){
            queryWrapper.eq("order_num",orderNum);
        }
        String downloadUrl = rssPostgresqlService.getOrderInfoDownloadUrl(queryWrapper);
        return downloadUrl;
    }

    public IPage<RssDatasetSimple> getDatasets(String taskType,String keyword,String startTime,String endTime,String sortMode,Integer pageSize,Integer pageNo){
        QueryWrapper<RssDatasetSimple> queryWrapper =new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate=null;
        Date endDate=null;
        try {
            if (startTime!=null&&!startTime.isEmpty()){
                startDate = sdf.parse(startTime);
            }
            if (endTime!=null&&!endTime.isEmpty()){
                endDate= sdf.parse(endTime);
            }
        }catch (ParseException e){
            e.printStackTrace();
        }

        if (taskType!=null&&!taskType.isEmpty()){
            queryWrapper.eq("task_type",taskType);
        }
        if (startDate!=null){
            Timestamp timestamp=new Timestamp(startDate.getTime());
            queryWrapper.ge("create_time",timestamp);
        }
        if (endDate!=null){
            Timestamp timestamp=new Timestamp(endDate.getTime());
            queryWrapper.le("create_time",timestamp);
        }
        if (keyword!=null&&!keyword.isEmpty()){
            queryWrapper.like("keyword",keyword).or().like("name", keyword).or().like("instrument",keyword).or().like("image_type",keyword);
        }
        if (sortMode.equals("pageView")) {
            queryWrapper.orderByDesc("visit");
        }else{
            queryWrapper.orderByAsc("id");
        }


        Page<RssDatasetSimple> page = new Page<RssDatasetSimple>(pageNo, pageSize);
        IPage<RssDatasetSimple> pageList = rssPostgresqlService.getDatasets(page, queryWrapper);
        return pageList;
    }

    public List<RssDatasetSimple> listDatasets(){
        List<RssDatasetSimple> rssDatasetSimples = rssPostgresqlService.listDatasets();
        return rssDatasetSimples;
    }
    public IPage<RssOdSampleSimple> getOdSamples(String[] codes, Integer[] datasetId,
                                                 String startTime, String endTime,
                                                 String sampleQuality,String sampleLabeler,
                                                 String labelBbox,String imageType,
                                                 String[] instrument,Integer trnValueTest,
                                                 String wkt,Integer pageSize,Integer pageNo){

        QueryWrapper<RssOdSampleSimple> queryWrapper =new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate=null;
        Date endDate=null;
        int flag=0;
        System.out.println("datasetId.length:"+datasetId.length);
        for (int i=0;i<datasetId.length;i++){
            if (datasetId[i]!=null){
                flag=1;
            }
        }
        if (datasetId.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<datasetId.length;i++){
                    if (i==datasetId.length-1){
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]);
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }else {
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]).or();
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime!=null&&!startTime.isEmpty()){
                startDate = sdf.parse(startTime);
            }
            if (endTime!=null&&!endTime.isEmpty()){
                endDate= sdf.parse(endTime);
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        if (startDate!=null){
            Timestamp timestamp=new Timestamp(startDate.getTime());
            queryWrapper.ge("sample_date",timestamp);
            flag=1;
        }
        if (endDate!=null){
            Timestamp timestamp=new Timestamp(endDate.getTime());
            queryWrapper.le("sample_date",timestamp);
            flag=1;
        }

        if (!sampleQuality.isEmpty()){
            queryWrapper.eq("sample_quality",sampleQuality);
            flag=1;
        }
        if (!sampleLabeler.isEmpty()){
            queryWrapper.eq("sample_labler",sampleLabeler);
            flag=1;
        }
        if (!labelBbox.isEmpty()){
            queryWrapper.eq("label_bbox",labelBbox);
            flag=1;
        }
        if (!imageType.isEmpty()){
            queryWrapper.eq("ros.image_type",imageType);
            flag=1;
        }
        System.out.println("instrument.length:"+instrument.length);
        for (int i=0;i<instrument.length;i++){
            if (!instrument[i].isEmpty()||!instrument[i].equals("")){
                flag=1;
            }
        }
        if (instrument.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<instrument.length;i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest!=null){
            queryWrapper.eq("trn_value_test",trnValueTest);
            flag=1;
        }
        System.out.println("codes.length:"+codes.length);
        for (int i=0;i<codes.length;i++){
            if (!codes[i].isEmpty()||!codes[i].equals("")){
                flag=1;
            }
        }
        if (codes.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<codes.length;i++){
                    if (i==codes.length-1){
                        if (!codes[i].isEmpty()||!codes[i].equals("")){
                            queryWrapper1.eq("code",codes[i]);
                            System.out.println("codes["+i+"]:"+codes[i]);
                        }else {
                            System.out.println("codes["+i+"]为空");
                        }
                    }else {
                        if (!codes[i].isEmpty()||!codes[i].equals("")){
                            queryWrapper1.eq("code",codes[i]).or();
                            System.out.println("codes["+i+"]:"+codes[i]);
                        }else {
                            System.out.println("codes["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam="";
        if (flag==0){
            combinationParam="WHERE 1=1";
        }
        if (wkt!=null&&wkt.length()!=0){
            String wktParam="ST_Intersects(sample_area,'SRID=4326;"+wkt+"')";
            combinationParam=combinationParam+" AND "+wktParam;
        }

        Page<RssOdSampleSimple> page = new Page<RssOdSampleSimple>(pageNo, pageSize);
        IPage<RssOdSampleSimple> pageList=rssPostgresqlService.getOdSamples(page,queryWrapper,combinationParam);
        return pageList;
    }

    public RssFilter getOdSamplesFilter(String[]codes, Integer[] datasetId,
                                                 String startTime, String endTime,
                                                 String sampleQuality,String sampleLabeler,
                                                 String labelBbox,String imageType,
                                                 String[] instrument,Integer trnValueTest,
                                                 String wkt){
        QueryWrapper<String> queryWrapper =new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate=null;
        Date endDate=null;
        int flag=0;
        System.out.println("datasetId.length:"+datasetId.length);
        for (int i=0;i<datasetId.length;i++){
            if (datasetId[i]!=null){
                flag=1;
            }
        }
        if (datasetId.length!=0){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<datasetId.length;i++){
                    if (i==datasetId.length-1){
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]);
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }else {
                        if (datasetId[i]!=null){
                            queryWrapper1.eq("dataset_id",datasetId[i]).or();
                            System.out.println("datasetId["+i+"]:"+datasetId[i]);
                        }else {
                            System.out.println("datasetId["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        try {
            if (startTime!=null&&!startTime.isEmpty()){
                startDate = sdf.parse(startTime);
            }
            if (endTime!=null&&!endTime.isEmpty()){
                endDate= sdf.parse(endTime);
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        if (startDate!=null){
            Timestamp timestamp=new Timestamp(startDate.getTime());
            queryWrapper.ge("sample_date",timestamp);
            flag=1;
        }
        if (endDate!=null){
            Timestamp timestamp=new Timestamp(endDate.getTime());
            queryWrapper.le("sample_date",timestamp);
            flag=1;
        }

        if (!sampleQuality.isEmpty()){
            queryWrapper.eq("sample_quality",sampleQuality);
            flag=1;
        }
        if (!sampleLabeler.isEmpty()){
            queryWrapper.eq("sample_labler",sampleLabeler);
            flag=1;
        }
        if (!labelBbox.isEmpty()){
            queryWrapper.eq("label_bbox",labelBbox);
            flag=1;
        }
        if (!imageType.isEmpty()){
            queryWrapper.eq("ros.image_type",imageType);
            flag=1;
        }
        System.out.println("instrument.length:"+instrument.length);
        for (int i=0;i<instrument.length;i++){
            if (!instrument[i].isEmpty()||!instrument[i].equals("")){
                flag=1;
            }
        }
        if (instrument.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<instrument.length;i++) {

                    if (i == instrument.length - 1) {

                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]);
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    } else {
                        if (!instrument[i].isEmpty() || !instrument[i].equals("")) {
                            queryWrapper1.eq("ros.instrument", instrument[i]).or();
                            System.out.println("instrument[" + i + "]:" + instrument[i]);
                        } else {
                            System.out.println("instrument[" + i + "]为空");
                        }
                    }
                }
                return queryWrapper1;
            });
        }
        if (trnValueTest!=null){
            queryWrapper.eq("trn_value_test",trnValueTest);
            flag=1;
        }
        System.out.println("codes.length:"+codes.length);
        for (int i=0;i<codes.length;i++){
            if (!codes[i].isEmpty()||!codes[i].equals("")){
                flag=1;
            }
        }
        if (codes.length!=0 ){
            queryWrapper.and(queryWrapper1 -> {
                for (int i=0;i<codes.length;i++){
                    if (i==codes.length-1){
                        if (!codes[i].isEmpty()||!codes[i].equals("")){
                            queryWrapper1.eq("code",codes[i]);
                            System.out.println("codes["+i+"]:"+codes[i]);
                        }else {
                            System.out.println("codes["+i+"]为空");
                        }
                    }else {
                        if (!codes[i].isEmpty()||!codes[i].equals("")){
                            queryWrapper1.eq("code",codes[i]).or();
                            System.out.println("codes["+i+"]:"+codes[i]);
                        }else {
                            System.out.println("codes["+i+"]为空");
                        }
                    }
                }
                return queryWrapper1;
            });

        }
        String combinationParam="";
        if (flag==0){
            combinationParam="WHERE 1=1";
        }
        if (wkt!=null&&wkt.length()!=0){
            String wktParam="ST_Intersects(sample_area,'SRID=4326;"+wkt+"')";
            combinationParam=combinationParam+" AND "+wktParam;
        }


        RssFilter rssFilter=rssPostgresqlService.getOdSamplesFilter(queryWrapper,combinationParam);
        return rssFilter;
    }
//    public List<JSONObject> queryClassNumByDataset(String taskType, Integer datasetId,Boolean isEnglish) {
////        List<JSONObject> jsonObjectList = new ArrayList<>();
////
//        String[][] classSCMapNames = queryClasses(taskType, datasetId,isEnglish);
//        HashMap<String,String> enChnNameMap=new HashMap<>();
//        for (int i = 0; i < classSCMapNames.length; i++) {
//            String enName=classSCMapNames[i][0];
//            String chnName=classSCMapNames[i][1];
//            enChnNameMap.put(enName,chnName);
//        }
////        for (int i = 0; i < classSCMapNames.length; i++) {
////
////            String uniClassId = classSCMapNames[i][2];
////            Integer uniClassCount = 0;
////            switch(taskType){
////                case "sc":
////                    uniClassCount = rssPostgresqlService.getSCClassNum(datasetId, uniClassId);
////                    break;
////                case "od":
////                    uniClassCount = rssPostgresqlService.getODClassNum(datasetId, uniClassId);
////                    break;
////                default:
////                    break;
////            }
////
////            JSONObject jsonObject = new JSONObject();
////            jsonObject.put("x", classSCMapNames[i][1]);
////            jsonObject.put("y", uniClassCount);
////            jsonObjectList.add(jsonObject);
////
////        }
//        List<JSONObject> jsonObjectList = new ArrayList<>();
////        List<HashMap<String,Integer>> ClassCountList = null;
//        List<HashMap<String,String>> ClassCountList = null;
//
//
//        switch(taskType){
//            case "sc":
//                ClassCountList = rssPostgresqlService.getSCClassNum(datasetId);
//                break;
//            case "od":
//                ClassCountList = rssPostgresqlService.getODClassNum(datasetId);
//            default:
//                break;
//
//        }
//
//
//        System.out.println(ClassCountList);
//        HashMap<String,String> isnotContClassName=enChnNameMap;
//        for(int i = 0;i < ClassCountList.size();i++){
//            JSONObject jsonObject = new JSONObject();
//            if (ClassCountList.get(i)!=null){
//                String enName=ClassCountList.get(i).get("name");
//                if (enName!=null){
//                    String chnName=enChnNameMap.get(enName);
//                    jsonObject.put("x", chnName);
//                    jsonObject.put("y", ClassCountList.get(i).get("count"));
//                    System.out.println(jsonObject);
//                    jsonObjectList.add(jsonObject);
//                    isnotContClassName.remove(enName);
//                }
//            }else {
//                System.out.println("没有类别！！！");
//            }
//
//        }
//        System.out.println("isnotContClassName:"+isnotContClassName);
//        return jsonObjectList;
//
//    }
    public List<JSONObject> queryClassNumByDataset(String taskType, Integer datasetId, boolean isEnglish) {

        String[][] classSCMapNames = queryClasses(taskType, datasetId,isEnglish);
//        HashMap<String,String> enChnNameMap=new HashMap<>();
//        for (int i = 0; i < classSCMapNames.length; i++) {
//            String enName=classSCMapNames[i][0];
//            String chnName=classSCMapNames[i][1];
//            enChnNameMap.put(enName,chnName);
//        }

        List<JSONObject> jsonObjectList = new ArrayList<>();

        List<HashMap<String,String>> ClassCountList = null;


        switch(taskType){
            case "sc":
                ClassCountList = rssPostgresqlService.getSCClassNum(datasetId);
                break;
            case "od":
                ClassCountList = rssPostgresqlService.getODClassNum(datasetId);
            default:
                break;

        }


        System.out.println(ClassCountList);
//        HashMap<String,String> isnotContClassName=enChnNameMap;
        for(int i = 0;i < ClassCountList.size();i++){
            JSONObject jsonObject = new JSONObject();
            if (ClassCountList.get(i)!=null){
                String className=ClassCountList.get(i).get("name");
                if (className!=null){
                    jsonObject.put("x", className);
                    jsonObject.put("y", ClassCountList.get(i).get("count"));
                    System.out.println(jsonObject);
                    jsonObjectList.add(jsonObject);
//                    isnotContClassName.remove(enName);
//                    if(isEnglish){
//                        String chnName=enChnNameMap.get(enName);
//
//                    } else{
//                        String chnName=enChnNameMap.get(enName);
//                        jsonObject.put("x", chnName);
//                        jsonObject.put("y", ClassCountList.get(i).get("count"));
//                        System.out.println(jsonObject);
//                        jsonObjectList.add(jsonObject);
//                        isnotContClassName.remove(enName);
//                    }
                }
            }else {
                System.out.println("没有类别！！！");
            }

        }
//        System.out.println("isnotContClassName:"+isnotContClassName);
        return jsonObjectList;

    }
//    public List<JSONObject> queryClassNumByDataset(String taskType, Integer datasetId, Boolean isEnglish) {
//
//        String[][] classSCMapNames = queryClasses(taskType, datasetId,isEnglish);
//        HashMap<String,String> enChnNameMap=new HashMap<>();
//        for (int i = 0; i < classSCMapNames.length; i++) {
//            String enName=classSCMapNames[i][0];
//            String chnName=classSCMapNames[i][1];
//            enChnNameMap.put(enName,chnName);
//        }
//
//        List<JSONObject> jsonObjectList = new ArrayList<>();
//
//        List<HashMap<String,String>> ClassCountList = null;
//
//
//        switch(taskType){
//            case "sc":
//                ClassCountList = rssPostgresqlService.getSCClassNum(datasetId);
//                break;
//            case "od":
//                ClassCountList = rssPostgresqlService.getODClassNum(datasetId);
//            default:
//                break;
//
//        }
//
//
//        System.out.println(ClassCountList);
//        HashMap<String,String> isnotContClassName=enChnNameMap;
//        for(int i = 0;i < ClassCountList.size();i++){
//            JSONObject jsonObject = new JSONObject();
//            if (ClassCountList.get(i)!=null){
//                String enName=ClassCountList.get(i).get("name");
//                if (enName!=null){
//                    if(isEnglish){
//                        String chnName=enChnNameMap.get(enName);
//                        jsonObject.put("x", chnName);
//                        jsonObject.put("y", ClassCountList.get(i).get("count"));
//                        System.out.println(jsonObject);
//                        jsonObjectList.add(jsonObject);
//                        isnotContClassName.remove(enName);
//                    }
//                    else{
//                        String chnName=enChnNameMap.get(enName);
//                        jsonObject.put("x", chnName);
//                        jsonObject.put("y", ClassCountList.get(i).get("count"));
//                        System.out.println(jsonObject);
//                        jsonObjectList.add(jsonObject);
//                        isnotContClassName.remove(enName);
//                    }}
//            }else {
//                System.out.println("没有类别！！！");
//            }
//
//        }
//        System.out.println("isnotContClassName:"+isnotContClassName);
//        return jsonObjectList;
//
//    }
    public JSONObject getDataNumInfo(){
//        JSONObject jsonObject = new JSONObject();
//        Integer datasetCount = rssPostgresqlService.getDatasetCount();
//        Integer dataCount = rssPostgresqlService.getDataCount();
//        jsonObject.put("datasetNum", datasetCount);
//        jsonObject.put("dataNum",dataCount);
//        return jsonObject;
        JSONObject jsonObject = new JSONObject();
        //总数据集
        Integer datasetCount = rssPostgresqlService.getDatasetCount();
        //场景样本数量
        Integer scDataCount = rssPostgresqlService.getSCDataCount();
        //目标检测总目标数
        Integer odDataTargetSum = rssPostgresqlService.getODDataTargetSum();
        //变化检测样本数
        Integer cdDataCount = rssPostgresqlService.getDataCountByWidthHeigth("cd");
        //地物分类样本数
        Integer lcDataCount = rssPostgresqlService.getDataCountByWidthHeigth("lc");
        //多视三维样本数
        Integer tdDataCount = rssPostgresqlService.getDataCountByWidthHeigth("td");
        jsonObject.put("datasetNum", datasetCount);
        jsonObject.put("scNum",scDataCount);
        jsonObject.put("odNum",odDataTargetSum);
        jsonObject.put("lcNum",lcDataCount);
        jsonObject.put("cdNum",cdDataCount);
        jsonObject.put("tdNum",tdDataCount);
        return jsonObject;

    }
    public Double getDatasetSize(Integer datasetId){
        //获得数据集任务类型
        List<RssDataset> rssDatasets =queryDatasetDetail(datasetId.toString());
        String taskType="";
        if (rssDatasets.get(0).getDataSize()!=null&&rssDatasets.get(0).getDataSize()!=0){
            return rssDatasets.get(0).getDataSize().doubleValue();
        }
        if (rssDatasets.size()!=0){
            taskType=rssDatasets.get(0).getTaskType();
        }
        String bucketName=null;
        String imagePath=null;
        List<String> imagePaths=new LinkedList<>();
        List<String> wrongImagePaths=new LinkedList<>();
        switch (taskType){
            case "od":
                bucketName="object-detection";
                QueryWrapper<RssOdSampleSimple> queryWrapperOd =new QueryWrapper<>();
                queryWrapperOd.eq("dataset_id",datasetId);
                String combinationParam="";
                List<RssOdSampleSimple> rssOdSampleSimples=rssPostgresqlService.getOdSamples(queryWrapperOd,combinationParam);
                if (rssOdSampleSimples!=null&&rssOdSampleSimples.size()!=0){
                    for (RssOdSampleSimple rssOdSampleSimple:rssOdSampleSimples){
                        //获得对应的影像数据
                        imagePath=rssOdSampleSimple.getImagePath();
                        imagePaths.add(imagePath);
                    }
                }
                break;
            case "sc":
                bucketName="scene-classification";
                QueryWrapper<RssScSampleSimple> queryWrapperSc = new QueryWrapper<>();
                queryWrapperSc.eq("dataset_id", datasetId);
                String combinationParamSc = "";
                List<RssScSampleSimple> rssScSampleSimples = rssPostgresqlService.getScSamples(queryWrapperSc, combinationParamSc);
                if (rssScSampleSimples != null && rssScSampleSimples.size() != 0) {
                    for (RssScSampleSimple rssScSampleSimple : rssScSampleSimples) {
                        //获得对应的影像数据
                        String path=rssScSampleSimple.getImagePath().toString();
//                        System.out.println("path"+path);
//                        System.out.println("rssScSampleSimple.getImagePath()"+rssScSampleSimple.getImagePath());
                        String replace="scene-classification/";
                        imagePath=path.replace(replace,"");
//                        System.out.println("imagePath"+imagePath);
//                        if (rssScSampleSimple.getImagePath()!=null){
//
//                        }else {
//                            System.out.println("rssScSampleSimple.getId():"+rssScSampleSimple.getId()+"影像文件路径为空！！！");
//                            wrongImagePaths.add(rssScSampleSimple.getId().toString());
//                        }
////                        System.out.println("imagePath:"+imagePath);
                        imagePaths.add(imagePath);
                    }
                }
                break;
            case "lc":
                bucketName="land-cover";
                QueryWrapper<RssLcSampleSimple> queryWrapperLc = new QueryWrapper<>();
                queryWrapperLc.eq("dataset_id", datasetId);
                String combinationParamLc = "";
                List<RssLcSampleSimple> rssLcSampleSimples = rssPostgresqlService.getLcSamples(queryWrapperLc, combinationParamLc);
                if (rssLcSampleSimples != null && rssLcSampleSimples.size() != 0) {
                    for (RssLcSampleSimple rssLcSampleSimple : rssLcSampleSimples) {
                        String path=rssLcSampleSimple.getImagePath().toString();
                        String replace="land-cover/";
                        imagePath=path.replace(replace,"");
//                        System.out.println("imagePath:"+imagePath);
                        imagePaths.add(imagePath);
                    }
                }
                break;
            case "cd":
                bucketName="change-detection";
                QueryWrapper<RssCdSampleSimple> queryWrapperCd = new QueryWrapper<>();
                queryWrapperCd.eq("dataset_id", datasetId);
                String combinationParamCd = "";
                List<RssCdSampleSimple> rssCdSampleSimples = rssPostgresqlService.getCdSamples(queryWrapperCd, combinationParamCd);
                if (rssCdSampleSimples != null && rssCdSampleSimples.size() != 0) {
                    for (RssCdSampleSimple rssCdSampleSimple : rssCdSampleSimples) {
                        String prePath=rssCdSampleSimple.getPreImagePath().toString();
                        String postPath=rssCdSampleSimple.getPostImagePath().toString();
                        String replace="change-detection/";
                        String preImagePath=prePath.replace(replace,"");
                        String postImagePath=postPath.replace(replace,"");


                        imagePaths.add(preImagePath);
                        imagePaths.add(postImagePath);

                    }
                }
                break;
            default:
                break;
        }
        Double dataSize=0.0;
        long byteSize=0;

        for(String path:imagePaths){
            //获取存储捅名
            //获取对象信息和对象的元数据。
            try {
                ObjectStat objectStat = rssMinioUtil.statObject(bucketName, path);
                //setContentType 设置发送到客户机的响应的内容类型
                //设置文件大小
                byteSize=byteSize+objectStat.length();
//                dataSize=dataSize+(double)(objectStat.length()/(1024*1024));
            }catch (Exception e){
                e.printStackTrace();
                wrongImagePaths.add(path);
                System.out.println("文件："+path+"有问题!!!");
            }
        }
        dataSize=((double)byteSize/(1024*1024));
        if (wrongImagePaths.size()!=0){
            System.out.println("===========nodata path===========");
            for (String path:wrongImagePaths){
                System.out.println(path);
            }
            System.out.println("===========nodata path===========");
        }else {
            boolean update=rssPostgresqlService.updateDatasetSize(datasetId,dataSize);
        }
        boolean update=rssPostgresqlService.updateDatasetSize(datasetId,dataSize);
        return dataSize;
    }
    public String queryEmailByOrderNum(String orderNum){

        String userId = rssPostgresqlService.getUserIdOfOrder(orderNum);
        System.out.println(userId);
        String EmailAdress = iSysUserService.getUserEmailAdress(userId);
        return EmailAdress;


    }

}
