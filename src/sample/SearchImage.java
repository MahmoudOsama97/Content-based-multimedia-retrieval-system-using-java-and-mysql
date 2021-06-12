package sample;
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
import java.util.Vector;

import org.opencv.core.CvType;
import static org.opencv.core.CvType.*;

//class is used to Search for image by mean color, histogram and color layout
public class SearchImage {
    //function used to get the mean value of the image and return it as double value
    static double getMean(Mat image){
        double sum=0;
        for(int i=0; i<image.size(0); i++){
            for(int j=0; j<image.size(1);j++ ){
                sum+=((double)image.get(i,j)[0]);
            }
        }
        return sum/(image.size(0)*image.size(1));
    }
    /*
    function used to search images in the database by using color layout (grid) takes the string path, the
    connection to the database and the number of blocks in both vertical and horizontal axis
     */
    static Vector<String> grid(String args ,Connection conn ,int width,int height) throws SQLException {
        Vector<String> vec = new Vector<String>();
        String filename = args;
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        int size= src.size(0)*src.size(1);
        int number_of_blocks=size/(width*height);
        int wStep=(src.size(1)/width);
        int hStep=(src.size(0)/height);
         List<Mat> tempSrcR = new ArrayList<Mat>();
         List<Mat> tempSrcG = new ArrayList<Mat>();
         List<Mat> tempSrcB = new ArrayList<Mat>();
        for( int w =0;w<((src.size(1)/width)*width);w+=wStep){
            for( int h =0;h<((src.size(0)/height)*height);h+=hStep){
                tempSrcR.add(bgrPlanes.get(0).submat(h,(h+hStep),w,(w+wStep)));
                tempSrcG.add(bgrPlanes.get(1).submat(h,(h+hStep),w,(w+wStep)));
                tempSrcB.add(bgrPlanes.get(2).submat(h,(h+hStep),w,(w+wStep)));
            }
        }
        String Q="select * from images";
        Statement stmt = conn.createStatement();
        ResultSet R= stmt.executeQuery(Q);
        while(R.next()){
            int c=0;
           String modelUrl= R.getString("url");
            Mat model = Imgcodecs.imread(modelUrl);
            if (model.empty()) {
                System.err.println("Cannot read image: " + modelUrl);
                System.exit(0);
            }
            List<Mat> bgrPlanesModel = new ArrayList<>();
            Core.split(model, bgrPlanesModel);
            int sizeM= model.size(0)*model.size(1);
            int number_of_blocksM=sizeM/(width*height);
            int wStepM=(model.size(1)/width);
            int hStepM=(model.size(0)/height);
            Mat tempMR,tempMG,tempMB, tempSR,tempSG,tempSB;
            int counter=0;
            int counter_blocks=0;
            for( int w =0;w<(model.size(1)/width)*width;w+=wStepM){
                for( int h =0;h<(model.size(0)/height)*height;h+=hStepM){
                    tempMR=(bgrPlanesModel.get(0).submat(h,(h+hStepM),w,(w+wStepM)));
                    tempMG=(bgrPlanesModel.get(1).submat(h,(h+hStepM),w,(w+wStepM)));
                    tempMB=(bgrPlanesModel.get(2).submat(h,(h+hStepM),w,(w+wStepM)));
                    tempSR=tempSrcR.get(counter);
                    tempSG=tempSrcG.get(counter);
                    tempSB=tempSrcB.get(counter);
                    counter++;
                    double meanred  =Math.abs(getMean(tempMR)-getMean(tempSR));
                    double meangreen=Math.abs(getMean(tempMG)-getMean(tempSG));
                    double meanblue =Math.abs(getMean(tempMB)-getMean(tempSB));
                    if((meanred<=0.2*getMean(tempSR))&&(meangreen<=0.2*getMean(tempSG))&&(meanblue<=0.2*getMean(tempSB))){
                        counter_blocks++;
                    }
                }
            }
            System.out.println(counter_blocks);
            if(counter_blocks>=(width+height)/2){
                String title=R.getString("title");
                if(!title.contains("video")) {
                    String s = R.getString("url");
                    vec.add(s);
                }
                c++;
            }
        }
    return vec;
    }
    /*
    function used to search images in the database by using mean takes the string path, the
    connection to the database and the number of blocks in both vertical and horizontal axis
    */
    public static Vector<String> mean(String args, Connection conn) throws SQLException {

        System.out.println(args);
        //String filename = args.length > 0 ? args[0] : "C:\\Users\\osama\\Desktop\\multimedia\\download.jpg";
        String filename= args ;
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        String Q="";
        Vector<String> vec = new Vector<String>();
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
        int meanR,sumR=0,meanG,sumG=0,meanB,sumB=0;
        for (int i = 0 ; i < 256; i++) {
            sumR+= i*((int)rHist.get(i,0)[0]);
            sumG+= i*((int)gHist.get(i,0)[0]);
            sumB+= i*((int)bHist.get(i,0)[0]);
        }
        meanR=sumR/(src.size(0)*src.size(1));
        meanG=sumG/(src.size(0)*src.size(1));
        meanB=sumB/(src.size(0)*src.size(1));
        Q="select * from images";
        Statement stmt = conn.createStatement();
        ResultSet R= stmt.executeQuery(Q);
        while(R.next()){
           int meanRD= R.getInt("red_mean");
           int meanGD= R.getInt("green_mean");
           int meanBD= R.getInt("blue_mean");
              if( Math.abs(meanR-meanRD)<=0.1*meanR && Math.abs(meanG-meanGD)<=0.1*meanG && Math.abs(meanB-meanBD)<=0.1*meanB){
                  String title=R.getString("title");
                  if(!title.contains("video")) {
                      String s = R.getString("url");
                      vec.add(s);
                  }
              } else{
            }
        }
        return vec;
    }

    /*
    function used to search images in the database by using Histogram takes the string path, the
    connection to the database and the number of blocks in both vertical and horizontal axis
    */
    public  Vector<String> hist(String args ,Connection conn,float compare) throws SQLException {
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
        Vector<String> vec = new Vector<String>();
        Q="select * from images";
        Statement stmt = conn.createStatement();
        ResultSet R = stmt.executeQuery(Q);
        int tempRI,tempGI,tempBI,tempRM,tempGM,tempBM;
        float sum=0,value=0;
        int counter=0;
        while(R.next()) {
            for (int i = 0; i < 16; i++) {
                tempRM = R.getInt(i + 3);
                tempGM = R.getInt(i + 19);
                tempBM = R.getInt(i + 35);
                tempRI = ((int) rHist16.get(i, 0)[0]);
                tempGI = ((int) gHist16.get(i, 0)[0]);
                tempBI = ((int) bHist16.get(i, 0)[0]);
                sum = sum + Math.min(tempRI, tempRM) + Math.min(tempGI, tempGM) + Math.min(tempBI, tempBM);
            }
            value = sum / (3 * (R.getInt("pixel")));
            if (value > compare) {
                counter++;
                String title=R.getString("title");
                if(!title.contains("video")) {
                    String s = R.getString("url");
                    vec.add(s);
                }
            }
            sum = 0;
        }
        System.out.println(counter);
        return vec;
    }
}

