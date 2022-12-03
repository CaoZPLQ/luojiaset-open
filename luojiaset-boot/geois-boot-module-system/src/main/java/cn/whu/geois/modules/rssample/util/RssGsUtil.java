package cn.whu.geois.modules.rssample.util;

import cn.whu.geois.modules.rssample.xml.RssOdAnn;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.util.factory.Hints;

import org.locationtech.jts.geom.Geometry;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author czp
 * @version 1.0
 * @date 2021/7/8 10:05
 */
@Component
public class RssGsUtil {
    public String DoubleToWKT(double minx,double miny,double maxx,double maxy){
        String WKT_rec="POLYGON(("+minx+" "+miny+", "+minx+" "+maxy+", "+maxx+" "+maxy+", "+maxx+" "+miny+","+minx+" "+miny+"))";
        return WKT_rec;
    }
    public String bndBoxToWKT(RssOdAnn.Object.Bndbox bndbox){
        String WKT_rec=null;
        if (bndbox.getXmax()!=null){
            String minx=bndbox.getXmin();
            String miny=bndbox.getYmin();
            String maxx=bndbox.getXmax();
            String maxy=bndbox.getYmax();
            WKT_rec="POLYGON(("+minx+" "+miny+", "+minx+" "+maxy+", "+maxx+" "+maxy+", "+maxx+" "+miny+","+minx+" "+miny+"))";
        }else {
            String x0=bndbox.getX0();
            String y0=bndbox.getY0();
            String x1=bndbox.getX1();
            String y1=bndbox.getY1();
            String x2=bndbox.getX2();
            String y2=bndbox.getY2();
            String x3=bndbox.getX3();
            String y3=bndbox.getY3();
            WKT_rec="POLYGON(("+x0+" "+y0+", "+x1+" "+y1+", "+x2+" "+y2+", "+x3+" "+y3+","+x0+" "+y0+"))";
        }
        return WKT_rec;
    }
    public void transform(String wkt)throws IOException, FactoryException, ParseException, TransformException {
//        String wkt = "POLYGON((100.02715479879 32.168082192159,102.76873121104 37.194305614622,107.0334056301 34.909658604412,105.96723702534 30.949603786713,100.02715479879 32.168082192159))";

        WKTReader wktReader = new WKTReader();
//        Polygon geom = (Polygon) wktReader.read(wkt);
        Geometry geom =  wktReader.read(wkt);
//        Geometry boundary = geom.getBoundary();
//        String geometryType = geom.getGeometryType();
//        System.out.println(geometryType);
//        System.out.println(boundary);

        //ressample参数修改
        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
        CoordinateReferenceSystem outputCRS = factory.createCoordinateReferenceSystem("EPSG:4326");
        CoordinateReferenceSystem crsTarget = CRS.decode("EPSG:3857");


//        MathTransform transform = CRS.findMathTransform(sourceCRS, outputCRS);
        MathTransform transform = CRS.findMathTransform(outputCRS, crsTarget,true);

        Geometry result = JTS.transform(geom, transform);
        System.out.println(result);
    }
}
