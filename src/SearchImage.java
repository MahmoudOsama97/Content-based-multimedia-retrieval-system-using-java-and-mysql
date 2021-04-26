
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

public class SearchImage {
    static void meanQuery(Connection conn ,Mat src,Mat rHist,Mat gHist,Mat bHist,int id ){

        int meanR,sumR=0,meanG,sumG=0,meanB,sumB=0;
        String Q="";
        for (int i = 0 ; i < 256; i++) {

            sumR+= i*((int)rHist.get(i,0)[0]);
            sumG+= i*((int)gHist.get(i,0)[0]);
            sumB+= i*((int)bHist.get(i,0)[0]);

        }
        meanR=sumR/(src.size(0)*src.size(1));
        meanG=sumG/(src.size(0)*src.size(1));
        meanB=sumB/(src.size(0)*src.size(1));
        Q="update images set "+"red_mean="+meanR+", green_mean="+meanG+", blue_mean="+meanB+" where id="+id;
        //System.out.println(Q);
        try{
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(Q);
        }catch (SQLException ex) {
            //System.out.println("shit ");
            System.out.println(ex.getMessage());
        }



    }


    public void hist(String args ,Connection conn) throws SQLException {

        //String filename = args.length > 0 ? args[0] : "C:\\Users\\osama\\Desktop\\multimedia\\download.jpg";
        String filename= args ;
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        String Q="";

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



            }


        Q="select * from images";
        Statement stmt = conn.createStatement();
        ResultSet R = stmt.executeQuery(Q);
        int tempRI,tempGI,tempBI,tempRM,tempGM,tempBM;
        float sum=0,value=0;
        while(R.next()){
            for( int i=0; i <16;i++) {
                tempRM=R.getInt(i+3);
                tempGM=R.getInt(i+19);
                tempBM=R.getInt(i+35);
                //System.out.println(tempRM);
                //System.out.println(tempGM);
                //System.out.println(tempBM);

                tempRI= ((int)rHist16.get(i,0)[0]);
                tempGI= ((int)gHist16.get(i,0)[0]);
                tempBI= ((int)bHist16.get(i,0)[0]);
                //System.out.println(tempRI);
                //System.out.println(tempGM);
                //System.out.println(tempBM);

                sum=sum+Math.min(tempRI,tempRM)+Math.min(tempGI,tempGM)+Math.min(tempBI,tempBM);
                //System.out.println(sum);
            }
            value=sum/(3*(R.getInt("pixel")));
            System.out.println(value);
            sum=0;
            //HighGui.imshow( "Source image", src );
            //break;
        }


    }
}

