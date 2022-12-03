package cn.whu.geois.modules.rssample.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * @author czp
 * @version 1.0
 * @date 2021/6/17 11:25
 */
@Data
@XmlRootElement(name = "HRSC_Image")
public class RssHrscAnn {
    private String Img_ID;
    private String Place_ID;
    private String Source_ID;
    private String Img_NO;
    private String Img_FileName;
    private String Img_FileFmt;
    private String Img_Date;
    private String Img_CusType;
    private String Img_Des;
    private String Img_Location;
    private String Img_SizeWidth;
    private String Img_SizeHeight;
    private String Img_SizeDepth;
    private String Img_Resolution;
    private String Img_Resolution_Layer;
    private String Img_Scale;
    private String Img_SclPxlNum;
    private String segmented;
    private String Img_Havemask;
    private String Img_MaskFileName;
    private String Img_MaskFileFmt;
    private String Img_MaskType;
    private String Img_SegFileName;
    private String Img_SegFileFmt;
    private String Img_Rotation;
    private String Annotated;
    private HRSC_OBJECTS HRSC_Objects;

    @NoArgsConstructor
    @Data
    public static class HRSC_OBJECTS {
        private ArrayList<HRSC_OBJECT> HRSC_Object;

        @NoArgsConstructor
        @Data
        public static class HRSC_OBJECT {
            private String Object_ID;
            private String Class_ID;
            private String Object_NO;
            private String truncated;
            private String difficult;
            private String box_xmin;
            private String box_ymin;
            private String box_xmax;
            private String box_ymax;
            private String mbox_cx;
            private String mbox_cy;
            private String mbox_w;
            private String mbox_h;
            private String mbox_ang;
            private String segmented;
            private String seg_color;
            private String header_x;
            private String header_y;
        }
    }

}
