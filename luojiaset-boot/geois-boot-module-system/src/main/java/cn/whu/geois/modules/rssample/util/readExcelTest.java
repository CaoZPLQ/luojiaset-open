package cn.whu.geois.modules.rssample.util;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/3/3
 */
public class readExcelTest {

    public static void main(String[] args) throws Exception {
        File file = new File("D:/studyofPostgraduate/项目/样本库/谭浩峰文件/【附录一】场景分类、【附录二】目标检测分类体系.xlsx");
        FileInputStream fileInputStream = new FileInputStream(file);

        Workbook workbook = WorkbookFactory.create(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        //读取行
//        Row row = sheet.getRow(1);
//        short lastCellNum = row.getLastCellNum();
//
//        for(int i=0; i<lastCellNum; i++ ){
//            Cell cell = row.getCell(i);
//            System.out.println(cell.getStringCellValue());
//        }

        //遍历合并单元
//        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
//        CellRangeAddress cellRangeAddress = mergedRegions.get(1);
//        System.out.println(cellRangeAddress);
//        int firstColumn = cellRangeAddress.getFirstColumn();
//        System.out.println(firstColumn);
//        String cellRangeValue = RssExcelUtil.getCellRangeValue(sheet, cellRangeAddress);
//        System.out.println(cellRangeValue);
//        boolean tf ;
//        if(sheet.getSheetName().contains("场景分类")){
//            tf = true;
//            System.out.println(tf);
//        }

//        Iterator<CellRangeAddress> iterator = mergedRegions.iterator();
//        while(iterator.hasNext()){
//            System.out.println(iterator.next());
//        }

        String sheetName = sheet.getSheetName();
        if (sheetName.contains("场景分类")) {
            //解析一级类和二级类
            List<CellRangeAddress> mergedRegionsList = sheet.getMergedRegions();
            ArrayList<CellRangeAddress> listLevel1 = new ArrayList<>();
            ArrayList<CellRangeAddress> listLevel2 = new ArrayList<>();

            int size = mergedRegionsList.size();
            for (int i = 0; i < size; i++) {
                CellRangeAddress mergedRegion = mergedRegionsList.get(i);
                switch (mergedRegion.getFirstColumn()) {
                    case 0:
                        listLevel1.add(mergedRegion);
                        break;
                    case 2:
                        listLevel2.add(mergedRegion);
                        break;

                }
            }
            //对list进行重排序
            listLevel1 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel1);
            listLevel2 = (ArrayList<CellRangeAddress>) ListUtil.sortEmergedRegions(listLevel2);




            //采用四位数编码
            //一级类编码
//            HashMap<String, String> codedLevel1 = new HashMap<>();
//            for (int i = 0; i < listLevel1.size(); i++) {
//                String code;
//                if((i+1)<10){
//                    code ="0"+(i+1)+"00";
//                }else{
//                    code=(i+1)+"00";
//                }
//                CellRangeAddress cellRange = listLevel1.get(i);
//                String cellRangeValue = RssExcelUtil.getCellRangeValue(sheet, cellRange);
//                codedLevel1.put(code,cellRangeValue);
//            }
//
//            //二级类编码
//            HashMap<String, String> codedLevel2 = new HashMap<>();
//            int flag1,flag2,flag3,flag4,flag5,flag6,flag7,flag8;
//            flag1=flag2=flag3=flag4=flag5=flag6=flag7=flag8=0;
//            for (int i = 0; i < listLevel2.size(); i++) {
//                String code =null;
//                if(i<3){
//                    flag1+=1;
//                    code="01"+flag1+"0";
//                }
//                else if(i>2&&i<5){
//                    flag2+=1;
//                    code="02"+flag2+"0";
//                }
//                else if(i>4&&i<7){
//                    flag3+=1;
//                    code="03"+flag3+"0";
//                }
//                else if(i>6&&i<10){
//                    flag4+=1;
//                    code="04"+flag4+"0";
//                }
//                else if(i>9&&i<12){
//                    flag5+=1;
//                    code="05"+flag5+"0";
//                }
//                else if(i>11&&i<14){
//                    flag6+=1;
//                    code="06"+flag6+"0";
//                }
//                else if(i>13&&i<16){
//                    flag7+=1;
//                    code="07"+flag6+"0";
//                }
//                else if(i>15&&i<18){
//                    flag8+=1;
//                    code="08"+flag6+"0";
//                }
//                String cellRangeValue = RssExcelUtil.getCellRangeValue(sheet, listLevel2.get(i));
//                codedLevel2.put(code,cellRangeValue);
//            }

            //三级类编码
            //获取三级类、编码
            int flag11,flag12,flag13;
            flag11=flag12=flag13=0;
            int flag21,flag22;
            flag21=flag22=0;
            int flag31,flag32;
            flag31=flag32=0;
            int flag41,flag42,flag43;
            flag41=flag42=flag43=0;
            int flag51,flag52;
            flag51=flag52=0;
            int flag61,flag62;
            flag61=flag62=0;
            int flag71,flag72;
            flag71=flag72=0;
            int flag81,flag82;
            flag81=flag82=0;
            HashMap<String, String> codedLevel3 = new HashMap<>();

            for(int i=1; i<sheet.getLastRowNum();i++){
                String code = null;
                if(i<27){
                    if(i<18){
                        flag11+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag11);
                        code = "011"+formatFlag;
                    }else if(i>17&&i<23){
                        flag12+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag12);
                        code = "012"+formatFlag;
                    }else if(i>22){
                        flag13+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag13);
                        code = "013"+formatFlag;
                    }
                }
                else if(i>26&&i<55){
                    if(i<50){
                        flag21+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag21);
                        code = "021"+formatFlag;
                    }else{
                        flag22+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag22);
                        code = "022"+formatFlag;
                    }
                }
                else if(i>54&&i<75){
                    if(i<65){
                        flag31+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag31);
                        code = "031"+formatFlag;
                    }else{
                        flag32+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag32);
                        code = "032"+formatFlag;
                    }
                }
                else if(i>74&&i<106){
                    if (i < 95) {
                        flag41+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag41);
                        code = "041"+formatFlag;
                    } else if (i > 94 && i < 97) {
                        flag42+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag42);
                        code = "042"+formatFlag;
                    }else{
                        flag43+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag43);
                        code = "043"+formatFlag;
                    }
                }
                else if(i>105&&i<132){
                    if(i<125){
                        flag51+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag51);
                        code = "051"+formatFlag;
                    }else{
                        flag52+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag52);
                        code = "052"+formatFlag;
                    }
                }
                else if(i>131&&i<165){
                    if(i<135){
                        flag61+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag51);
                        code = "061"+formatFlag;
                    }else{
                        flag62+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag52);
                        code = "062"+formatFlag;
                    }
                }
                else if(i>164&&i<191){
                    if(i<188){
                        flag71+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag71);
                        code = "071"+formatFlag;
                    }else{
                        flag72+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag72);
                        code = "072"+formatFlag;
                    }
                }
                else if(i>190&&i<204){
                    if(i<198){
                        flag81+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag81);
                        code = "081"+formatFlag;
                    }else{
                        flag82+=1;
                        String formatFlag = RssExcelUtil.codeFormat(flag82);
                        code = "082"+formatFlag;
                    }
                }
                codedLevel3.put(code,sheet.getRow(i).getCell(4).getStringCellValue());
            }

            //遍历list
//            Iterator<CellRangeAddress> iterator1 = listLevel1.iterator();
//            while(iterator1.hasNext())
//            System.out.println(iterator1.next());

            //遍历map
            Iterator<Map.Entry<String, String>> iteratorMap = codedLevel3.entrySet().iterator();
            while(iteratorMap.hasNext()){
                Map.Entry<String, String> next = iteratorMap.next();
                String key = next.getKey();
                String value = next.getValue();
                System.out.println(key+":"+value);
            }

        }
    }
}
