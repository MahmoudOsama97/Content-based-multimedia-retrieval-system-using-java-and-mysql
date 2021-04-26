import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.opencv.core.CvType;
import static org.opencv.core.CvType.*;

class InsertImage {

    static void meanQuery(Connection conn ,Mat src,Mat rHist,Mat gHist,Mat bHist,int id ){
        int width=src.size(0);
        int height=src.size(1);
        int pixel=width*height;
        int meanR,sumR=0,meanG,sumG=0,meanB,sumB=0;
        String Q="";
        for (int i = 0 ; i < 256; i++) {

            sumR+= i*((int)rHist.get(i,0)[0]);
            sumG+= i*((int)gHist.get(i,0)[0]);
            sumB+= i*((int)bHist.get(i,0)[0]);

        }
        meanR=sumR/(pixel);
        meanG=sumG/(pixel);
        meanB=sumB/(pixel);

        Q="update images set "+"width="+width+", height= "+ height+", pixel="+pixel+"," +
                " red_mean="+meanR+", green_mean="+meanG+", blue_mean="+meanB+" where id="+id;
        //System.out.println(Q);
        try{
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(Q);
        }catch (SQLException ex) {
            //System.out.println("shit ");
            System.out.println(ex.getMessage());
        }


    }
    public void run(String args ,Connection conn,int id,String title,String info) throws SQLException {

        //String filename = args.length > 0 ? args[0] : "C:\\Users\\osama\\Desktop\\multimedia\\download.jpg";
        String filename= args ;
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        String Q="";
        try {
          Q = "insert into images(id,title,url,info) values("+ id + ", " + "\""+title+ "\"" + ", " + "\""+args+ "\""  + ", " +  "\""+info+ "\""+ ")" ;
            System.out.println(Q);
          Statement stmt = conn.createStatement();
          stmt.executeUpdate(Q);
      }catch (SQLException ex) {
          System.out.println("shit ");
          System.out.println(ex.getMessage());
      }


        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        int histSize = 256;
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        boolean accumulate = false;
        Mat bHist = new Mat(), gHist = new Mat(), rHist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(1), new Mat(), gHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(2), new Mat(), rHist, new MatOfInt(histSize), histRange, accumulate);

        Mat rHist16 =Mat.zeros(16,1,CV_16UC1);
        Mat gHist16 =Mat.zeros(16,1,CV_16UC1);
        Mat bHist16 =Mat.zeros(16,1,CV_16UC1);

        meanQuery( conn , src, rHist, gHist, bHist,id);



        Q ="update images set" ;

        for (int i=0;i<16;i++){
            // take  16 consecutive elements from the orginal matrix of 256 element
           Mat temp= rHist.submat( 16*i,16*i+15,0,1 );
           //sum the 16 element
            Scalar sum = Core.sumElems(temp);
            //insert the sum val in the 16 element mat
            rHist16.put(i,0, sum.val);
            int valueR=(int)sum.val[0];

            // take  16 consecutive elements from the orginal matrix of 256 element
            temp= gHist.submat( 16*i,16*i+15,0,1 );
            //sum the 16 element
            sum = Core.sumElems(temp);
            //insert the sum val in the 16 element mat
            gHist16.put(i,0, sum.val);
            int valueG=(int)sum.val[0];

            // take  16 consecutive elements from the orginal matrix of 256 element
            temp= bHist.submat( 16*i,16*i+15,0,1 );
            //sum the 16 element
            sum = Core.sumElems(temp);
            //insert the sum val in the 16 element mat
            bHist16.put(i,0, sum.val);
            int valueB=(int)sum.val[0];

            int start=16*i;
            int end=16*i+15;

            Q +=" red"+start+"_"+end+ "=" +valueR+" ,"  ;
            Q +=" green"+start+"_"+end+ "=" +valueG+" ,"  ;
            Q +=" blue"+start+"_"+end+ "=" +valueB;
            if(i!=15)Q+=" ,";

        }
        Q +=" where id="+id;
        System.out.println(Q);
        try{
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(Q);
        }catch (SQLException ex) {
            System.out.println("shit ");
            System.out.println(ex.getMessage());
        }



//        int histW = 512, histH = 400;
//        int binW = (int) Math.round((double) histW / histSize);
//        Mat histImage = new Mat( histH, histW, CV_8UC3, new Scalar( 0,0,0) );
//        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
//        Core.normalize(gHist, gHist, 0, histImage.rows(), Core.NORM_MINMAX);
//        Core.normalize(rHist, rHist, 0, histImage.rows(), Core.NORM_MINMAX);
//        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
//        bHist.get(0, 0, bHistData);
//        float[] gHistData = new float[(int) (gHist.total() * gHist.channels())];
//        gHist.get(0, 0, gHistData);
//        float[] rHistData = new float[(int) (rHist.total() * rHist.channels())];
//        rHist.get(0, 0, rHistData);
//
//        for( int i = 1; i < histSize; i++ ) {
//            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
//                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(255, 0, 0), 2);
//            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(gHistData[i - 1])),
//                    new Point(binW * (i), histH - Math.round(gHistData[i])), new Scalar(0, 255, 0), 2);
//            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(rHistData[i - 1])),
//                    new Point(binW * (i), histH - Math.round(rHistData[i])), new Scalar(0, 0, 255), 2);
//        }
//        HighGui.imshow( "Source image", src );
//        HighGui.imshow( "calcHist Demo", histImage );
//        HighGui.waitKey(0);
//        System.exit(0);
  }
}
