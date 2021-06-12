package sample;


import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import static org.opencv.core.CvType.CV_16UC1;

//class is used to Search for videos by Naive Video Similarity
public class SearchVideo {
    /*
    function is used to calculate histogram for image and return it
     */
    public static Mat calcHist(Mat src){
        Mat frameGrey = new Mat();
        Imgproc.cvtColor(src,frameGrey,Imgproc.COLOR_RGB2GRAY);
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(frameGrey, bgrPlanes);
        int histSize = 256;
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        boolean accumulate = false;
        Mat Hist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), Hist, new MatOfInt(histSize), histRange, accumulate);
        Scalar S=new Scalar(src.size(0) * src.size(1));
        Core.divide(Hist,S,Hist);
        return Hist ;
    }

    /*
    function is used to search for the videos in the database buy using naive video similarity in which we divide the
    two sequence into keyframes and find the total number of frames in each sequence then find the number of similar frames
    divide the number of similar frames over the total number of frames
     */
    public Vector<String> Search(String args , Connection conn, float compare) throws SQLException {
        Vector<String> vec = new Vector<String>();
        int x=0;
        String Q = "";
        Mat frame = new Mat();
        VideoCapture camera = new VideoCapture(args);
        Mat histold = Mat.zeros(256, 1, 5);
        Mat frameGrey = new Mat();
        int count = 0;
        double k1 = 0.1, k2 = 0.8;
        List<Mat> keyFrames = new ArrayList<>();
        while (true) {
            if (camera.read(frame)) {
                Imgproc.cvtColor(frame, frameGrey, Imgproc.COLOR_RGB2GRAY);
                List<Mat> bgrPlanes = new ArrayList<>();
                Core.split(frameGrey, bgrPlanes);
                int histSize = 256;
                float[] range = {0, 256}; //the upper boundary is exclusive
                MatOfFloat histRange = new MatOfFloat(range);
                boolean accumulate = false;
                Mat Hist = new Mat();
                Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), Hist, new MatOfInt(histSize), histRange, accumulate);
                Mat diff = Mat.zeros(256, 1, 5);
                Mat diffSquare = Mat.zeros(256, 1, 5);
                Core.pow(diff, 2, diffSquare);
                Scalar sumSquare = Core.sumElems(diffSquare);
                double meanSquare = ((double) sumSquare.val[0]) / 256;
                Core.absdiff(Hist, histold, diff);
                Scalar sum = Core.sumElems(diff);
                double mean = ((double) sum.val[0]) / 256;
                double sd = Math.sqrt(Math.abs(meanSquare - mean));
                double threshold = k1 * mean + k2 * sd;
                histold = Hist;
                if (threshold > 15) {
                    Imgcodecs.imwrite("C:\\Users\\osama\\Desktop\\multimedia\\test"+String.valueOf(x)+".jpg",frame);
                    x++;
                    count++;
                }
            }
            else {
                for(int i=0;i<x;i++) {
                    Mat src = Imgcodecs.imread("C:\\Users\\osama\\Desktop\\multimedia\\test"+String.valueOf(i)+".jpg");
                    keyFrames.add(src);
                }
                break;
            }
        }
        int Count2=0;
        Q="select * from videos";
        Statement stmt = conn.createStatement();
        ResultSet R= stmt.executeQuery(Q);
        while(R.next()){
            int videoId= R.getInt("id");
            Q="select * from images where videoId = "+String.valueOf(videoId);
            //System.out.println(Q);
            Statement stmt2 = conn.createStatement();
            ResultSet R2= stmt2.executeQuery(Q);
            List<Mat> QueryKeyFrames = new ArrayList<>();
            while(R2.next()){
                String frameUrl= R2.getString("url");
                Mat qFrame = Imgcodecs.imread(frameUrl);
                QueryKeyFrames.add(qFrame);
            }
            for (Mat keyFrameI : keyFrames)
            {
                for (Mat keyFrameQ : QueryKeyFrames)
                {
                    Mat Histi= calcHist(keyFrameQ);
                    Mat Histq= calcHist(keyFrameI);
                    Mat diff = Mat.zeros(256, 1, 5);
                    Core.absdiff(Histi,Histq,diff);
                    Scalar sum=Core.sumElems(diff);
                    double mean=(((double)sum.val[0]))/256;
                    if(mean<=0.002){
                       Count2++;
                   }
                }
            }
            if((Count2*1.0/(QueryKeyFrames.size()))>=compare){
                String s = R.getString("url");
                vec.add(s);
            }
            Count2=0;
        }
    return vec;

    }
}
