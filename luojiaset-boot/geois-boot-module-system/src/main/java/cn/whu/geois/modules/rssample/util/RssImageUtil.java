package cn.whu.geois.modules.rssample.util;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import it.geosolutions.imageio.stream.output.ImageOutputStreamAdapter;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.*;
import java.security.Key;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/3/23
 */
@Component
public class RssImageUtil {
    /**
     * only for
     * @param path
     * @return
     * @throws JpegProcessingException
     * @throws IOException
     */
    public static Timestamp getJepgCreateTime(String path) throws JpegProcessingException, IOException {
        File file = new File(path);
        Metadata metadata = JpegMetadataReader.readMetadata(file);
        HashMap<String, String> map = new HashMap<>();

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                String key = tag.getTagName();
                String value = tag.getDescription();
                map.put(key,value);
            }
        }

        String strTime = map.get("Date/Time");
        StringBuilder sb = new StringBuilder(strTime);
        sb.replace(4,5,"-");
        sb.replace(7,8,"-");
        String formatTime = sb.toString();
        Timestamp createTime = Timestamp.valueOf(formatTime);
        return createTime;
    }

    public void tifToPng(String oldImageFilePath,String outputDir){
        try {
            byte[] rev = null;
            File oldImageFile=new File(oldImageFilePath);

            String fileName=oldImageFile.getName();
            String purefileName=fileName.substring(0,fileName.lastIndexOf("."));
//            GeoTiffReader reader=new GeoTiffReader(oldImageFilePath);
//            GridCoverage2D coverage = reader.read(null);
//            ByteArrayOutputStream fileOut=new ByteArrayOutputStream();
//            ImageEncoder pngEncoder = ImageCodec.createImageEncoder("png", fileOut, null);
//
//            pngEncoder.encode(coverage.getRenderedImage());
//            rev = fileOut.toByteArray();

//            String outFile="F:\\studyofPostgraduate\\code\\java\\rssample\\rssample-boot\\geois-boot\\output\\test.png";
//            File imageFile=new File(outFile);
//            if (!imageFile.exists()){
//                imageFile.createNewFile();
//            }
//            FileOutputStream fos=new FileOutputStream(outFile);
//            fos.write(rev);
//            fos.close();

//            InputStream inputStream=new ByteArrayInputStream(rev);



            InputStream inputStream=new FileInputStream(oldImageFile);
            OutputStream output = new FileOutputStream(outputDir+File.separator+"renderPng"+File.separator+purefileName+".png");
            resizeImage(inputStream,output,256,"png");
//            fileOut.close();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    /**
     * 改变图片的大小到宽为size，然后高随着宽等比例变化
     * @param is 上传的图片的输入流
     * @param os 改变了图片的大小后，把图片的流输出到目标OutputStream
     * @param size 新图片的宽
     * @param format 新图片的格式
     * @throws IOException
     */
    public static void resizeImage(InputStream is, OutputStream os, int size, String format) throws IOException {
        BufferedImage prevImage = ImageIO.read(is);
        double width = prevImage.getWidth();
        double height = prevImage.getHeight();
        double percent = size/width;
        int newWidth = (int)(width * percent);
        int newHeight = (int)(height * percent);
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = image.createGraphics();
        graphics.drawImage(prevImage, 0, 0, newWidth, newHeight, null);
        ImageIO.write(image, format, os);
        os.flush();
        is.close();
        os.close();
    }
}
