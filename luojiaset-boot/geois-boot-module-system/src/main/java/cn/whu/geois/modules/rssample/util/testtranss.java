package cn.whu.geois.modules.rssample.util;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/4/28
 */
public class testtranss {
    private static final String APP_ID = "20210427000805129";
    private static final String SECURITY_KEY = "WBkPVRTAB8XaquZkwFC6";

    public static void main(String[] args) throws Exception {

        FileInputStream fileInputStream = new FileInputStream("/Users/tanhaofeng/Desktop/样本库/分类体系/20210424样本标注类别体系.xlsx");
        Workbook workbook = WorkbookFactory.create(fileInputStream);
        Sheet sheetAt = workbook.getSheetAt(2);
        int lastRowNum = sheetAt.getLastRowNum();

        for(int j=1; j<6; j+=2) {
            for (int i = 1; i < lastRowNum + 1; i++) {
                Row row = sheetAt.getRow(i);
                Cell cell = row.getCell(j);
                if(cell!=null){
                    String stringCellValue = cell.getStringCellValue();
                    if(stringCellValue !="") {
                        System.out.println(stringCellValue);
                        RssTransUtil rssTransUtil = new RssTransUtil(APP_ID, SECURITY_KEY);
                        String transResult = rssTransUtil.getTransResult(stringCellValue, "zh", "en");
                        row.createCell(j-1).setCellValue(transResult);
                        System.out.println(row.getCell(j-1).getStringCellValue());
                        Thread thread = Thread.currentThread();
                        //反爬虫？
                        thread.sleep(1000);
                    }
                }

            }
        }
            FileOutputStream out = new FileOutputStream("/Users/tanhaofeng/Desktop/样本库/分类体系/20210424样本标注类别体系.xlsx");
            workbook.write(out);
            workbook.close();
    }
}
