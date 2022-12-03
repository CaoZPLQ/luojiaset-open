package cn.whu.geois.modules.rssample.util;

import cn.whu.geois.modules.rssample.entity.RssGqjcClass;
import cn.whu.geois.modules.rssample.entity.RssOdClass;
import cn.whu.geois.modules.rssample.entity.RssScClass;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/3/1
 */
@Component
public class RssExcelUtil {

    /**
     * 定义不同的excel类型
     */
    public static final String OFFICE_EXCEL_XLS="xls";
    public static final String OFFICE_EXCEL_XLSX="xlsx";
    /**
     *  地物分类体系入库
     * @param filePath
     * @param sheetNum
     * @return
     */
    public static List<RssGqjcClass> readLcExcel(String filePath, Integer sheetNum) throws Exception {
        Workbook workbook = getWorkbook(filePath);
        ArrayList<RssGqjcClass> rssGqjcClassList = new ArrayList<>();

        Integer id = 0;

        if(workbook != null){
            Sheet sheet = workbook.getSheetAt(sheetNum);
            String sheetName = sheet.getSheetName();
            if(sheetName.contains("地物分类")){
                List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
                ArrayList<CellRangeAddress> level1 = new ArrayList<>();
                ArrayList<CellRangeAddress> level1Chinese = new ArrayList<>();

                for (int i = 0; i < mergedRegions.size(); i++) {
                    CellRangeAddress cellRangeAddress = mergedRegions.get(i);
                    switch (cellRangeAddress.getFirstColumn()){
                        case 0:
                            level1.add(cellRangeAddress);
                            break;
                        case 1:
                            level1Chinese.add(cellRangeAddress);
                            break;
                        default:
                            break;
                    }
                }
                level1 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(level1);
                level1Chinese = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(level1Chinese);

                //一级类编码
                for (int i = 0; i < level1.size(); i++) {
                    RssGqjcClass rssGqjcClass = new RssGqjcClass();
                    Date date = new Date(System.currentTimeMillis());
                    String code;
                    id++;
                    if((i+1)<10){
                        code ="0"+(i+1)+"0000";
                    }else{
                        code=(i+1)+"0000";
                    }
                    CellRangeAddress cellRange = level1.get(i);
                    String cellRangeValue = getCellRangeValue(sheet, cellRange);

                    rssGqjcClass.setId(id);
                    rssGqjcClass.setCode(code);
                    rssGqjcClass.setName(cellRangeValue);
                    rssGqjcClass.setLevel(1);
                    rssGqjcClass.setParentId(0);
                    rssGqjcClass.setDescription(getCellRangeValue(sheet,level1Chinese.get(i)));
                    rssGqjcClass.setCreateBy("admin");
                    rssGqjcClass.setCreateTime(date);
                    rssGqjcClassList.add(rssGqjcClass);
                }

                //二级类编码
                int flag1,flag2,flag3,flag4,flag5,flag6,flag7,flag8,flag9;
                flag1=flag2=flag3=flag4=flag5=flag6=flag7=flag8=flag9=0;

                for(int i=1;i<sheet.getLastRowNum()+1;i++) {
                    RssGqjcClass rssGqjcClass = new RssGqjcClass();
                    Date date = new Date(System.currentTimeMillis());
                    String code = null;
                    Cell cell = sheet.getRow(i).getCell(2);
//                    if (cell != null) {
                    String value = cell.getStringCellValue();
                    String description = sheet.getRow(i).getCell(3).getStringCellValue();
                    if (i < 12) {
                        if (value != "") {
                            id++;

                            flag1 += 1;
                            code = "010" + flag1 + "00";

                            rssGqjcClass.setId(id);
                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 11 && i < 23) {
                        if (value != "") {
                            id++;
                            flag2 += 1;
                            code = "020" + flag2 + "00";

                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 22 && i < 30) {
                        if (value != "") {
                            flag3 += 1;
                            code = "030" + flag3 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 29 && i < 39) {
                        if (value != "") {
                            flag4 += 1;
                            code = "040" + flag4 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 38 && i < 66) {
                        if (value != "") {
                            flag5 += 1;
                            code = "050" + flag5 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 65 && i < 72) {
                        if (value != "") {
                            flag6 += 1;
                            code = "060" + flag6 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 71 && i < 75) {
                        if (value != "") {
                            flag7 += 1;
                            code = "070" + flag7 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else if (i > 74 && i < 90) {
                        if (value != "") {
                            flag8 += 1;
                            code = "080" + flag8 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    } else {
                        if (value != "") {
                            flag9 += 1;
                            code = "090" + flag9 + "00";
                            id++;
                            rssGqjcClass.setId(id);

                            rssGqjcClass.setCode(code);
                            rssGqjcClass.setName(value);
                            rssGqjcClass.setLevel(2);
                            rssGqjcClass.setParentId(1);
                            rssGqjcClass.setDescription(description);
                            rssGqjcClass.setCreateBy("admin");
                            rssGqjcClass.setCreateTime(date);
                            rssGqjcClassList.add(rssGqjcClass);
                        }
                    }
//                        }
                }

                //三级类编码
                int flag11,flag12;
                flag11=flag12=0;
                int flag21,flag22;
                flag21=flag22=0;
                int flag31,flag32,flag33;
                flag31=flag32=flag33=0;
                int flag46;
                flag46=0;
                int flag51,flag52,flag53,flag54,flag55;
                flag51=flag52=flag53=flag54=flag55=0;
                int flag61,flag62;
                flag61=flag62=0;
                int flag71,flag72,flag73;
                flag71=flag72=flag73=0;
                int flag81,flag84;
                flag81=flag84=0;
                int flag92;
                flag92=0;

                for(int i=1; i<sheet.getLastRowNum()+1;i++){
                    RssGqjcClass rssGqjcClass = new RssGqjcClass();
                    Date date = new Date(System.currentTimeMillis());
                    String code = null;
                    if(i<12){
                        if(i<5){
                            flag11+=1;
                            String formatFlag = codeFormat(flag11);
                            code = "0101"+formatFlag;
                        }else{
                            flag12+=1;
                            String formatFlag = codeFormat(flag12);
                            code = "0102"+formatFlag;
                        }
                    }
                    else if(i>11&&i<23){
                        if(i<20){
                            flag21+=1;
                            String formatFlag = codeFormat(flag21);
                            code = "0201"+formatFlag;
                        }else{
                            flag22+=1;
                            String formatFlag = codeFormat(flag22);
                            code = "0202"+formatFlag;
                        }
                    }
                    else if(i>22&&i<30){
                        if(i<25){
                            flag31+=1;
                            String formatFlag = codeFormat(flag31);
                            code = "0301"+formatFlag;
                        }else if(i>24&&i<27){
                            flag32+=1;
                            String formatFlag = codeFormat(flag32);
                            code = "0302"+formatFlag;
                        }else{
                            flag33+=1;
                            String formatFlag = codeFormat(flag33);
                            code = "0303"+formatFlag;
                        }
                    }
                    else if(i>29&&i<39){
                        if (i>34&&i<38) {
                            flag46+=1;
                            String formatFlag = codeFormat(flag46);
                            code = "0406"+formatFlag;
                        }
                    }
                    else if(i>38&&i<66){
                        if(i<41){
                            flag51+=1;
                            String formatFlag = codeFormat(flag51);
                            code = "0501"+formatFlag;
                        }else if(i>40&&i<50){
                            flag52+=1;
                            String formatFlag = codeFormat(flag52);
                            code = "0502"+formatFlag;
                        }else if(i>49&&i<54){
                            flag53+=1;
                            String formatFlag = codeFormat(flag53);
                            code = "0503"+formatFlag;
                        }else if(i>53&&i<63){
                            flag54+=1;
                            String formatFlag = codeFormat(flag54);
                            code = "0504"+formatFlag;
                        }else if(i>62&&i<66){
                            flag55+=1;
                            String formatFlag = codeFormat(flag55);
                            code = "0505"+formatFlag;
                        }
                    }
                    else if(i>65&&i<72){
                        if(i<69){
                            flag61+=1;
                            String formatFlag = codeFormat(flag61);
                            code = "0601"+formatFlag;
                        }else if(i>68&&i<71){
                            flag62+=1;
                            String formatFlag = codeFormat(flag62);
                            code = "0602"+formatFlag;
                        }
                    }
                    else if(i>71&&i<75){
                        if(i==72){
                            flag71+=1;
                            String formatFlag = codeFormat(flag71);
                            code = "0701"+formatFlag;
                        }if(i==73){
                            flag72+=1;
                            String formatFlag = codeFormat(flag72);
                            code = "0702"+formatFlag;
                        }if(i==74){
                            flag73+=1;
                            String formatFlag = codeFormat(flag73);
                            code = "0703"+formatFlag;
                        }
                    }
                    else if(i>74&&i<90){
                        if(i<79){
                            flag81+=1;
                            String formatFlag = codeFormat(flag81);
                            code = "0801"+formatFlag;
                        }else if(i>82){
                            flag84+=1;
                            String formatFlag = codeFormat(flag84);
                            code = "0804"+formatFlag;
                        }
                    }
                    else if(i>89&&i<93){
                        if(i>90){
                            flag92+=1;
                            String formatFlag = codeFormat(flag92);
                            code = "0902"+formatFlag;
                        }
                    }
                    if(code!=null) {
                        id++;
                        Row row = sheet.getRow(i);

                        rssGqjcClass.setId(id);
                        rssGqjcClass.setCode(code);
                        //三级类英文名位于7列
                        rssGqjcClass.setName(row.getCell(4).getStringCellValue());
                        rssGqjcClass.setLevel(3);
                        rssGqjcClass.setParentId(2);
                        //中文
                        rssGqjcClass.setDescription(row.getCell(5).getStringCellValue());

                        rssGqjcClass.setCreateBy("admin");
                        rssGqjcClass.setCreateTime(date);
                        rssGqjcClassList.add(rssGqjcClass);
                    }

                }

            }

        }


        return rssGqjcClassList;
    }
    /***
     *
     * @param filePath
     * @param sheetNum 读取的表序号，若为null则为读取全表
     *
     */
    public static List<RssScClass> readScExcel(String filePath,Integer sheetNum) throws Exception{

        /**
         * HSSFWorkbook for xls
         * XSSFWorkbook for xlsx
         */
//        File file = new File(filePath);
//        FileInputStream fileInputStream = new FileInputStream(file.getAbsoluteFile());
//        HSSFWorkbook workbook1 = new HSSFWorkbook(fileInputStream);
//        XSSFWorkbook workbook2 = new XSSFWorkbook(fileInputStream);

        //利用workbookFactory
//        Workbook workbook = WorkbookFactory.create(file);
        Workbook workbook = getWorkbook(filePath);
        //设置全局变量id
        Integer id = 0;

        ArrayList<RssScClass> rssScClassesList = new ArrayList<>();

        if(workbook!=null){
            //考虑sheetNum为空的情况
//            if(sheetNum == null){
//                int numOfSheet = workbook.getNumberOfSheets();
//                for(int i=0; i<numOfSheet; i++){
//                    Sheet sheet = workbook.getSheetAt(i);
//                    if(sheet == null){
//                        continue;
//                    }
//                    //执行读取excel程序
//
//                }
//            }
            Sheet sheet = workbook.getSheetAt(sheetNum);
            String sheetName = sheet.getSheetName();
            ArrayList<CellRangeAddress> listLevel1 = new ArrayList<>();
            ArrayList<CellRangeAddress> listLevel2 = new ArrayList<>();
            ArrayList<CellRangeAddress> listLevel1Chinese = new ArrayList<>();

//            HashMap<String, String> codedLevel1 = new HashMap<>();
//            HashMap<String, String> codedLevel2 = new HashMap<>();
            HashMap<String, String> codedLevel3 = new HashMap<>();
            if(sheetName.contains("场景分类")){

                //解析一级类和二级类
                List<CellRangeAddress> mergedRegionsList = sheet.getMergedRegions();

                int size = mergedRegionsList.size();
                for (int i = 0; i < size; i++) {
                    CellRangeAddress mergedRegion = mergedRegionsList.get(i);
                    switch(mergedRegion.getFirstColumn()){
                        case 0:
                            listLevel1.add(mergedRegion);
                            break;
                        case 1:
                            listLevel1Chinese.add(mergedRegion);
                            break;
                        case 2:
                            listLevel2.add(mergedRegion);
                            break;
                    }
                }

                //对list进行重排序
                listLevel1 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel1);
                listLevel2 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel2);
                listLevel1Chinese = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel1Chinese);

                //采用5位数编码
                //一级类编码
                for (int i = 0; i < listLevel1.size(); i++) {
                    id++;
                    Date date = new Date(System.currentTimeMillis());
                    RssScClass rssScClass = new RssScClass();
                    String code;
                    if((i+1)<10){
                        code ="0"+(i+1)+"000";
                    }else{
                        code=(i+1)+"000";
                    }
                    CellRangeAddress cellRange = listLevel1.get(i);
                    String cellRangeValue = getCellRangeValue(sheet, cellRange);

                    rssScClass.setId(id);
                    rssScClass.setCode(code);
                    rssScClass.setName(cellRangeValue);
                    rssScClass.setLevel(1);
                    rssScClass.setParentId(0);
                    rssScClass.setDescription(getCellRangeValue(sheet,listLevel1Chinese.get(i)));
                    rssScClass.setCreateBy("admin");
                    rssScClass.setCreateTime(date);
                    rssScClassesList.add(rssScClass);

                    //codedLevel1.put(code,cellRangeValue);
                }

                //二级类编码
                int flag1,flag2,flag3,flag4,flag5,flag6,flag7,flag8,flag9;
                flag1=flag2=flag3=flag4=flag5=flag6=flag7=flag8=flag9=0;
                //mergedRegion存在问题 单行二级类无法解析
//                for (int i = 0; i < listLevel2.size(); i++) {
//                    RssScClass rssScClass = new RssScClass();
//                    Date date = new Date(System.currentTimeMillis());
//                    String code =null;
//                    if(i<2){
//                        flag1+=1;
//                        code="01"+flag1+"00";
//                    }
//                    else if(i>1&&i<4){
//                        flag2+=1;
//                        code="02"+flag2+"00";
//                    }
//                    else if(i>3&&i<7){
//                        flag3+=1;
//                        code="03"+flag3+"00";
//                    }
//                    else if(i>6&&i<9){
//                        flag4+=1;
//                        code="04"+flag4+"00";
//                    }
//                    else if(i>8&&i<15){
//                        flag5+=1;
//                        code="05"+flag5+"00";
//                    }
//                    else if(i>14&&i<16){
//                        flag6+=1;
//                        code="06"+flag6+"00";
//                    }
//                    else if(i>15&&i<19){
//                        flag7+=1;
//                        code="07"+flag7+"00";
//                    }
//                    else if(i>18&&i<21){
//                        flag8+=1;
//                        code="08"+flag8+"00";
//                    }
//                    else{
//                        flag9+=1;
//                        code="09"+flag9+"00";
//                    }
//                    String cellRangeValue = getCellRangeValue(sheet, listLevel2.get(i));
//                    rssScClass.setCode(code);
//                    rssScClass.setName(cellRangeValue);
//                    rssScClass.setLevel(2);
//                    rssScClass.setParentId(1);
//                    rssScClass.setCreateBy("admin");
//                    rssScClass.setCreateTime(date);
//                    rssScClassesList.add(rssScClass);
//
//                   //codedLevel2.put(code,cellRangeValue);
//                }
                //逐行读取解析二级类
                for(int i=1;i<sheet.getLastRowNum()+1;i++) {
                    RssScClass rssScClass = new RssScClass();
                    Date date = new Date(System.currentTimeMillis());
                    String code = null;
                    Cell cell = sheet.getRow(i).getCell(2);
                    String value = cell.getStringCellValue();
                    String description = sheet.getRow(i).getCell(3).getStringCellValue();
                    if (i < 21) {
                        if (value != "") {
                            id++;

                            flag1 += 1;
                            code = "01" + flag1 + "00";

                            rssScClass.setId(id);
                            rssScClass.setCode(code);
                            rssScClass.setName(value);
                            rssScClass.setLevel(2);
                            rssScClass.setParentId(1);
                            rssScClass.setDescription(description);
                            rssScClass.setCreateBy("admin");
                            rssScClass.setCreateTime(date);
                            rssScClassesList.add(rssScClass);
                        }
                    }
                    else if (i > 20 && i < 42) {
                            if (value != "") {
                                id++;
                                flag2 += 1;
                                code = "02" + flag2 + "00";

                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else if(i>41&&i<74){
                            if (value != "") {
                                flag3 += 1;
                                code = "03" + flag3 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else if(i>73&&i<100){
                            if (value != "") {
                                flag4 += 1;
                                code = "04" + flag4 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else if(i>99&&i<130){
                            if (value != "") {
                                flag5 += 1;
                                code = "05" + flag5 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else if(i>129&&i<136){
                            if (value != "") {
                                flag6 += 1;
                                code = "06" + flag6 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else if(i>135&&i<139){
                            if (value != "") {
                                flag7 += 1;
                                code = "07" + flag7 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else if(i>138&&i<158){
                            if (value != "") {
                                flag8 += 1;
                                code = "08" + flag8 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                    }
                    else{
                            if (value != "") {
                                flag9 += 1;
                                code = "09" + flag9 + "00";
                                id++;
                                rssScClass.setId(id);

                                rssScClass.setCode(code);
                                rssScClass.setName(value);
                                rssScClass.setLevel(2);
                                rssScClass.setParentId(1);
                                rssScClass.setDescription(description);
                                rssScClass.setCreateBy("admin");
                                rssScClass.setCreateTime(date);
                                rssScClassesList.add(rssScClass);
                            }
                        }
                    }


                //获取三级类、编码
                int flag11,flag12;
                flag11=flag12=0;
                int flag21,flag22;
                flag21=flag22=0;
                int flag31,flag32,flag33;
                flag31=flag32=flag33=0;
                int flag41,flag42;
                flag41=flag42=0;
                int flag51,flag52,flag53,flag54,flag55,flag56;
                flag51=flag52=flag53=flag54=flag55=flag56=0;
                int flag61;
                flag61=0;
                int flag71,flag72,flag73;
                flag71=flag72=flag73=0;
                int flag81,flag82;
                flag81=flag82=0;
                int flag91,flag92;
                flag91=flag92=0;

                for(int i=1; i<sheet.getLastRowNum()+1;i++){
                    RssScClass rssScClass = new RssScClass();
                    Date date = new Date(System.currentTimeMillis());
                    String code = null;
                    if(i<21){
                        if(i<16){
                            flag11+=1;
                            String formatFlag = codeFormat(flag11);
                            code = "011"+formatFlag;
                        }else{
                            flag12+=1;
                            String formatFlag = codeFormat(flag12);
                            code = "012"+formatFlag;
                        }
                    }
                    else if(i>20&&i<42){
                        if(i<40){
                            flag21+=1;
                            String formatFlag = codeFormat(flag21);
                            code = "021"+formatFlag;
                        }else{
                            flag22+=1;
                            String formatFlag = codeFormat(flag22);
                            code = "022"+formatFlag;
                        }
                    }
                    else if(i>41&&i<74){
                        if(i<52){
                            flag31+=1;
                            String formatFlag = codeFormat(flag31);
                            code = "031"+formatFlag;
                        }else if(i>51&&i<60){
                            flag32+=1;
                            String formatFlag = codeFormat(flag32);
                            code = "032"+formatFlag;
                        }else{
                            flag33+=1;
                            String formatFlag = codeFormat(flag33);
                            code = "033"+formatFlag;
                        }
                    }
                    else if(i>73&&i<100){
                        if (i < 76) {
                            flag41+=1;
                            String formatFlag = codeFormat(flag41);
                            code = "041"+formatFlag;
                        } else {
                            flag42+=1;
                            String formatFlag = codeFormat(flag42);
                            code = "042"+formatFlag;
                        }
                    }
                    else if(i>99&&i<130){
                        if(i<101){
                            flag51+=1;
                            String formatFlag = codeFormat(flag51);
                            code = "051"+formatFlag;
                        }else if(i>100&&i<102){
                            flag52+=1;
                            String formatFlag = codeFormat(flag52);
                            code = "052"+formatFlag;
                        }else if(i>101&&i<116){
                            flag53+=1;
                            String formatFlag = codeFormat(flag53);
                            code = "053"+formatFlag;
                        }else if(i>115&&i<125){
                            flag54+=1;
                            String formatFlag = codeFormat(flag54);
                            code = "054"+formatFlag;
                        }else if(i>124&&i<126){
                            flag55+=1;
                            String formatFlag = codeFormat(flag55);
                            code = "055"+formatFlag;
                        }else{
                            flag56+=1;
                            String formatFlag = codeFormat(flag56);
                            code = "056"+formatFlag;
                        }
                    }
                    else if(i>129&&i<136){
                            flag61+=1;
                            String formatFlag = codeFormat(flag61);
                            code = "061"+formatFlag;

                    }
                    else if(i>135&&i<139){
                        if(i==136){
                            flag71+=1;
                            String formatFlag = codeFormat(flag71);
                            code = "071"+formatFlag;
                        }if(i==137){
                            flag72+=1;
                            String formatFlag = codeFormat(flag72);
                            code = "072"+formatFlag;
                        }if(i==138){
                            flag73+=1;
                            String formatFlag = codeFormat(flag73);
                            code = "073"+formatFlag;
                        }
                    }
                    else if(i>138&&i<158){
                        if(i<155){
                            flag81+=1;
                            String formatFlag = codeFormat(flag81);
                            code = "081"+formatFlag;
                        }else{
                            flag82+=1;
                            String formatFlag = codeFormat(flag82);
                            code = "082"+formatFlag;
                        }
                    }
                    else if(i>157&&i<168){
                        if(i<162){
                            flag91+=1;
                            String formatFlag = codeFormat(flag91);
                            code = "091"+formatFlag;
                        }else{
                            flag92+=1;
                            String formatFlag = codeFormat(flag92);
                            code = "092"+formatFlag;
                        }
                    }
                    id++;
                    Row row = sheet.getRow(i);

                    rssScClass.setId(id);
                    rssScClass.setCode(code);
                    //三级类英文名位于7列
                    rssScClass.setName(row.getCell(6).getStringCellValue());
                    rssScClass.setLevel(3);
                    rssScClass.setParentId(2);
                    //中文
                    rssScClass.setDescription(row.getCell(7).getStringCellValue());
                    //三级类分类标准英文位于4列
                    if(isMergedRegion(sheet,i,4)){
                            rssScClass.setStandard(getMergedRegionValue(sheet, i, 4));
                    }else{
                            rssScClass.setStandard(row.getCell(4).getStringCellValue());
                    }
                    rssScClass.setCreateBy("admin");
                    rssScClass.setCreateTime(date);
                    rssScClassesList.add(rssScClass);

//                    codedLevel3.put(code,sheet.getRow(i).getCell(4).getStringCellValue());
                }

                    }
                }
            return rssScClassesList;
            }


    /**
     *
      * @param filePath
     * @param sheetNum
     * @return
     * @throws Exception
     */

    public static List<RssOdClass> readOdExcel(String filePath,Integer sheetNum) throws Exception{

        Workbook workbook = getWorkbook(filePath);
        ArrayList<RssOdClass> rssOdClassesList = new ArrayList<>();
        //全局变量id
        Integer id = 0;

        if(workbook!=null){
            Sheet sheet = workbook.getSheetAt(sheetNum);
            String sheetName = sheet.getSheetName();
            if(sheetName.contains("目标检测")){
                //解析一级类 二级类
                List<CellRangeAddress> mergedRegionsList = sheet.getMergedRegions();
                ArrayList<CellRangeAddress> listLevel1 = new ArrayList<>();
                ArrayList<CellRangeAddress> listLevel2 = new ArrayList<>();
                ArrayList<CellRangeAddress> listLevel1Chinese = new ArrayList<>();

                int size = mergedRegionsList.size();
                for (int i = 0; i < size; i++) {
                    CellRangeAddress mergedRegion = mergedRegionsList.get(i);
                    switch (mergedRegion.getFirstColumn()) {
                        case 0:
                            listLevel1.add(mergedRegion);
                            break;
                        case 1:
                            listLevel1Chinese.add(mergedRegion);
                            break;
                        case 2:
                            listLevel2.add(mergedRegion);
                            break;
                    }
                }
                //对list进行重排序
                listLevel1 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel1);
                listLevel2 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel2);
                listLevel1Chinese = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel1Chinese);

                //采用五位数编码
                //一级类编码
                for (int i = 0; i < listLevel1.size(); i++) {
                RssOdClass rssOdClass = new RssOdClass();
                Date date = new Date(System.currentTimeMillis());
                String code;
                id++;
                if((i+1)<10){
                    code ="0"+(i+1)+"000";
                }else{
                    code=(i+1)+"000";
                }
                CellRangeAddress cellRange = listLevel1.get(i);
                String cellRangeValue = getCellRangeValue(sheet, cellRange);

                rssOdClass.setId(id);
                rssOdClass.setCode(code);
                rssOdClass.setName(cellRangeValue);
                rssOdClass.setLevel(1);
                rssOdClass.setParentId(0);
                rssOdClass.setDescription(getCellRangeValue(sheet,listLevel1Chinese.get(i)));
                rssOdClass.setCreateBy("admin");
                rssOdClass.setCreateTime(date);
                rssOdClassesList.add(rssOdClass);
            }

            //二级类编码

            int flag1,flag2;
            flag1=0;
            flag2=0;

            //使用mergedRegion方法 部分解析存在问题
//            for (int i = 0; i < listLevel2.size(); i++) {
//                    RssOdClass rssOdClass = new RssOdClass();
//                    Date date = new Date(System.currentTimeMillis());
//                    String code = null;
//                    if(i<3){
//                        flag1+=1;
//                        code = "01"+flag1+"00";
//                    }else{
//                        flag2+=1;
//                        code = "02"+flag2+"00";
//                    }
//                String cellRangeValue = getCellRangeValue(sheet, listLevel2.get(i));
//                rssOdClass.setCode(code);
//                rssOdClass.setName(cellRangeValue);
//                rssOdClass.setLevel(2);
//                rssOdClass.setParentId(1);
//                rssOdClass.setCreateBy("admin");
//                rssOdClass.setCreateTime(date);
//                rssOdClassesList.add(rssOdClass);
//
//                }
                //使用按行读取
                for(int i = 1; i<sheet.getLastRowNum()+1; i++) {
                    String code = null;
                    String stringCellValue = null;
                    RssOdClass rssOdClass = new RssOdClass();
                    Date date = new Date(System.currentTimeMillis());
                    String description = sheet.getRow(i).getCell(3).getStringCellValue();
                    if (i < 61) {
                        Cell cell = sheet.getRow(i).getCell(2);
                        stringCellValue = cell.getStringCellValue();
                        if (stringCellValue != "") {
                            id++;
                            flag1 += 1;
                            code = "01" + flag1 + "00";

                            rssOdClass.setId(id);
                            rssOdClass.setCode(code);
                            rssOdClass.setName(stringCellValue);
                            rssOdClass.setLevel(2);
                            rssOdClass.setParentId(1);
                            rssOdClass.setDescription(description);
                            rssOdClass.setCreateBy("admin");
                            rssOdClass.setCreateTime(date);
                            rssOdClassesList.add(rssOdClass);
                        }
                    } else {
                        Cell cell = sheet.getRow(i).getCell(2);
                        stringCellValue = cell.getStringCellValue();
                        if (stringCellValue != "") {
                            id++;
                            flag2 += 1;
                            code = "02" + flag2 + "00";

                            rssOdClass.setId(id);
                            rssOdClass.setCode(code);
                            rssOdClass.setName(stringCellValue);
                            rssOdClass.setLevel(2);
                            rssOdClass.setParentId(1);
                            rssOdClass.setDescription(description);
                            rssOdClass.setCreateBy("admin");
                            rssOdClass.setCreateTime(date);
                            rssOdClassesList.add(rssOdClass);
                        }
                    }

                }

            //三级类编码
            int flag11,flag12,flag13,flag14,flag15;
            flag11=flag12=flag13=flag14=flag15=0;
            int flag21,flag22,flag23,flag24,flag25;
            flag21=flag22=flag23=flag24=flag25=0;

            for (int i = 1; i < sheet.getLastRowNum()+1; i++) {
                RssOdClass rssOdClass = new RssOdClass();
                Date date = new Date(System.currentTimeMillis());
                String code = null;
                if(i<61){
                    if(i==1){
                        flag11+=1;
                        String formatFlag = codeFormat(flag11);
                        code = "011"+formatFlag;
                    }else if(i>1&&i<6){
                        flag12+=1;
                        String formatFlag = codeFormat(flag12);
                        code = "012"+formatFlag;
                    }else if(i>5&&i<20){
                        flag13+=1;
                        String formatFlag = codeFormat(flag13);
                        code = "013"+formatFlag;
                    }else if(i>19&&i<21){
                        flag14+=1;
                        String formatFlag = codeFormat(flag14);
                        code = "014"+formatFlag;
                    }else{
                        flag15+=1;
                        String formatFlag = codeFormat(flag15);
                        code = "015"+formatFlag;
                    }
                }
                else{
                    if(i<69){
                        flag21+=1;
                        String formatFlag = codeFormat(flag21);
                        code = "021"+formatFlag;
                    }else if(i>68&&i<81){
                        flag22+=1;
                        String formatFlag = codeFormat(flag22);
                        code = "022"+formatFlag;
                    }else if(i>80&&i<87){
                        flag23+=1;
                        String formatFlag = codeFormat(flag23);
                        code = "023"+formatFlag;
                    }else if(i>86&&i<93){
                        flag24+=1;
                        String formatFlag = codeFormat(flag24);
                        code = "024"+formatFlag;
                    }else{
                        flag25+=1;
                        String formatFlag = codeFormat(flag25);
                        code = "025"+formatFlag;
                    }
                }
                Row row = sheet.getRow(i);
                id++;

                rssOdClass.setId(id);
                rssOdClass.setCode(code);
                //获取名字
                rssOdClass.setName(row.getCell(6).getStringCellValue());
                rssOdClass.setLevel(3);
                rssOdClass.setParentId(2);
                //中文
                rssOdClass.setDescription(row.getCell(7).getStringCellValue());
                //获取分类标准
                if(isMergedRegion(sheet,i,4)){
                        rssOdClass.setStandard(getMergedRegionValue(sheet, i, 4));
                }else{
                        rssOdClass.setStandard(getCellValue(row.getCell(4)));
                }
                rssOdClass.setCreateBy("admin");
                rssOdClass.setCreateTime(date);
                rssOdClassesList.add(rssOdClass);

            }
            }
        }
        return  rssOdClassesList;

    }


    /**
     * getClassMap between original source and our standard
     * @param filePath
     * @param sheetNum
     * @return
     * @throws Exception
     */
    public static Map<String,String> getClassMap(String filePath,Integer sheetNum) throws Exception {
        Workbook workbook = getWorkbook(filePath);
        Sheet sheet = workbook.getSheetAt(sheetNum);
        HashMap<String, String> classMap = new HashMap<>();

        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum+1; i++) {
            Row row = sheet.getRow(i);
            String key = row.getCell(1).getStringCellValue();
            String value = row.getCell(0).getStringCellValue();
            classMap.put(key,value);
            classMap.put(key,value);
        }

        return  classMap;

    }




    /**
     * 获取工作簿
     * @param filepath
     * @return
     * @throws Exception
     */
    public static Workbook getWorkbook(String filepath) throws Exception {
        Workbook workbook = null;
        InputStream inputStream = null;
        if (StringUtils.isBlank(filepath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        } else {
            String suffix = getSuffix(filepath);
            if(StringUtils.isBlank(suffix)) {
                throw new IllegalArgumentException("文件后缀不能为空");
            }
                if(OFFICE_EXCEL_XLS.equals(suffix)||OFFICE_EXCEL_XLSX.equals(suffix)){
                    try {
                        inputStream=new FileInputStream(filepath);
                        workbook = WorkbookFactory.create(inputStream);
                    }finally {
                        if(inputStream!=null){
                            inputStream.close();
                        }
                        if(workbook!=null){
                            workbook.close();
                        }
                    }
                }else{
                    throw new IllegalArgumentException("该文件非Excel文件");
                }
            }

    return workbook;
    }

    /**
     * 获取前缀
     * @param filePath
     * @return
     */
    public static String getSuffix(String filePath){
        if(StringUtils.isBlank(filePath)){
            return "";
        }

        int index = filePath.lastIndexOf(".");
        if(index == -1){
            return "";
        }
        return filePath.substring(index+1, filePath.length());
    }

    /**
     * 读取sheet
     * @param sheet
     * @return
     */
    public static String readExcelSheet(Sheet sheet){
        StringBuilder stringBuilder = new StringBuilder();

        if(sheet!=null){
            int rowNum = sheet.getLastRowNum();
            for(int i=0 ;i<rowNum; i++){
                Row row = sheet.getRow(i);
                if(row!=null){
                    int columnNum = row.getLastCellNum();
                    for(int j=0; j<columnNum; j++ ){
                        Cell cell = row.getCell(j);
                        if(cell!=null){
                            cell.setCellType(CellType.STRING);
                            stringBuilder.append(cell.getStringCellValue()+" ");
                        }
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 判断是否为合并表格
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static boolean isMergedRegion(Sheet sheet,int row,int column){
        int sheetMergeCount = sheet.getNumMergedRegions();

        for(int i=0; i<sheetMergeCount; i++){
            CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
            int firstRow = cellRangeAddress.getFirstRow();
            int lastRow = cellRangeAddress.getLastRow();
            int firstColumn = cellRangeAddress.getFirstColumn();
            int lastColumn = cellRangeAddress.getLastColumn();
            if(row>=firstRow&&row<=lastRow){
                if(column>=firstColumn&&column<=lastColumn){
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 读取确定行、列的合并区域值
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static String getMergedRegionValue(Sheet sheet,int row,int column){
        int numMergedRegions = sheet.getNumMergedRegions();

        for(int i=0; i<numMergedRegions; i++){
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            int firstRow = mergedRegion.getFirstRow();
            int lastRow = mergedRegion.getLastRow();
            int firstColumn = mergedRegion.getFirstColumn();
            int lastColumn = mergedRegion.getLastColumn();

            if(row>=firstRow&&row<=lastRow){
                if(column>=firstColumn&&column<=lastColumn){
                    Row row1 = sheet.getRow(firstRow);
                    Cell cell = row1.getCell(firstColumn);
                    if(cell==null){
                        return null;
                    }else {
                        return cell.getStringCellValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取确定cellrangeaddress的值
     * @param sheet
     * @param cellRangeAddress
     * @return
     */
    public static String getCellRangeValue(Sheet sheet,CellRangeAddress cellRangeAddress){
        int firstRow = cellRangeAddress.getFirstRow();
        int firstColumn = cellRangeAddress.getFirstColumn();

        Row row = sheet.getRow(firstRow);
        Cell cell = row.getCell(firstColumn);
        return cell.getStringCellValue();
    }

    public static String codeFormat(int i){
//        DecimalFormat decimalFormat = new DecimalFormat("00");
//        String formatStr = decimalFormat.format(i);
        String formatStr = String.format("%02d",i);
        return formatStr;
    }

    public static String getCellValue(Cell cell) {
        String cellValue;
        // 以下是判断数据的类型
        if(cell==null){
            return null;
        }
        switch (cell.getCellTypeEnum()) {
            case NUMERIC: // 数字
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    cellValue = sdf.format(DateUtil.getJavaDate(cell.getNumericCellValue())).toString();
                } else {
                    DataFormatter dataFormatter = new DataFormatter();
                    cellValue = dataFormatter.formatCellValue(cell);
                }
                break;
            case STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                break;
            case FORMULA: // 公式
                cellValue = cell.getCellFormula() + "";
                break;
            case BLANK: // 空值
                cellValue = "";
                break;
            case ERROR: // 故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }






}
