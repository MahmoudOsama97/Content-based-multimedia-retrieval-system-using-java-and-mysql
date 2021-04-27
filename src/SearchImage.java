
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
    static double getMean(Mat image){

        double sum=0;
        for(int i=0; i<image.size(0); i++){
            for(int j=0; j<image.size(1);j++ ){
                sum+=((double)image.get(i,j)[0]);
            }


        }
        return sum/(image.size(0)*image.size(1));
    }


    static void grid(String args ,Connection conn ,int width,int height) throws SQLException {

        //String filename = args.length > 0 ? args[0] : "C:\\Users\\osama\\Desktop\\multimedia\\download.jpg";
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
        int wStep=(src.size(0)/width);
        int hStep=(src.size(1)/height);
         List<Mat> tempSrcR = new ArrayList<Mat>();
         List<Mat> tempSrcG = new ArrayList<Mat>();
         List<Mat> tempSrcB = new ArrayList<Mat>();

        for( int w =0;w<src.size(0);w+=wStep){
            for( int h =0;h<src.size(1);h+=hStep){
                tempSrcR.add(bgrPlanes.get(0).submat(w,(w+wStep),h,(h+hStep)));
                tempSrcG.add(bgrPlanes.get(1).submat(w,(w+wStep),h,(h+hStep)));
                tempSrcB.add(bgrPlanes.get(2).submat(w,(w+wStep),h,(h+hStep)));
            }
        }

        String Q="select * from images";
        Statement stmt = conn.createStatement();
        ResultSet R= stmt.executeQuery(Q);
        while(R.next()){

           String modelUrl= R.getString("url");
            Mat model = Imgcodecs.imread(modelUrl);
            if (model.empty()) {
                System.err.println("Cannot read image: " + modelUrl);
                System.exit(0);
            }

            List<Mat> bgrPlanesModel = new ArrayList<>();
            Core.split(src, bgrPlanesModel);
            int sizeM= model.size(0)*model.size(1);
            int number_of_blocksM=size/(width*height);
            int wStepM=(model.size(0)/width);
            int hStepM=(model.size(1)/height);
            //List<Mat> tempModel= new ArrayList<Mat>();
            Mat tempMR,tempMG,tempMB, tempSR,tempSG,tempSB;
            int counter=0;
            int counter_blocks=0;
            for( int w =0;w<src.size(0);w+=wStep){
                for( int h =0;h<src.size(1);h+=hStep){

                    tempMR=(bgrPlanesModel.get(0).submat(w,(w+wStep),h,(h+hStep)));
                    tempMG=(bgrPlanesModel.get(1).submat(w,(w+wStep),h,(h+hStep)));
                    tempMB=(bgrPlanesModel.get(2).submat(w,(w+wStep),h,(h+hStep)));

                    counter++;
                    tempSR=tempSrcR.get(counter);
                    tempSG=tempSrcG.get(counter);
                    tempSB=tempSrcB.get(counter);

                    double meanred  =Math.abs(getMean(tempMR)-getMean(tempSR));
                    double meangreen=Math.abs(getMean(tempMG)-getMean(tempSG));
                    double meanblue =Math.abs(getMean(tempMB)-getMean(tempSB));

                    if((meanred<=0.1*getMean(tempSR))&&(meangreen<=0.1*getMean(tempSG))&&(meanblue<=0.1*getMean(tempSB))){

                        counter_blocks++;
                    }



                }
            }
            if(counter_blocks>=number_of_blocks/2){
                System.out.println(R.getString("title"));
            }

        }


    }

    static void mean(String args ,Connection conn ) throws SQLException {

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

                  System.out.println(R.getString("title"));

              } else{
                  System.out.println(" 555 atfo");


            }

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

