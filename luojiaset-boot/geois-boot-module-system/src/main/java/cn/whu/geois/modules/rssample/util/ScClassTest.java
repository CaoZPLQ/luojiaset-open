package cn.whu.geois.modules.rssample.util;

import cn.whu.geois.modules.rssample.entity.RssScClass;
import cn.whu.geois.modules.rssample.service.IRssScClassService;
import cn.whu.geois.modules.rssample.service.impl.RssScClassServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/3/11
 */

public class ScClassTest {
//    @Autowired
//    private IRssScClassService iRssScClassService;
    public static void main(String[] args) {
//        IRssScClassService RssScClassService = new RssScClassServiceImpl();
//        String path ="/Users/tanhaofeng/Desktop/样本库/分类体系/分类体系最新20210310加英文.xlsx";
        String path ="D:/studyofPostgraduate/项目/样本库/遥感影像样本数据库设计/分类体系最新20210310加英文.xlsx";
        List<RssScClass> rssScClasses = null;
        try {
            rssScClasses = RssExcelUtil.readScExcel(path, 0);
            RssScClass rssScClass = rssScClasses.get(1);
            System.out.println(rssScClass);
            ScClassTest scClassTest=new ScClassTest();
            scClassTest.save(rssScClasses);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public boolean save(List<RssScClass> rssScClasses){
        IRssScClassService iRssScClassService=new RssScClassServiceImpl();
        boolean insert=iRssScClassService.insertBatch(rssScClasses);
        return insert;
    }
}
