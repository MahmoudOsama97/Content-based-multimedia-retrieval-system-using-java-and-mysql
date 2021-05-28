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

import static org.opencv.core.CvType.CV_16UC1;


public class SearchVideo {

    public static Mat calcHist(Mat src){

        //System.out.println(src.size());


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

    public void Search(String args , Connection conn) throws SQLException {
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
                    HighGui.imshow("frame", frame);
                    HighGui.waitKey(100);
                    count++;
                }

            }
            else {
                for(int i=0;i<x;i++) {
                    Mat src = Imgcodecs.imread("C:\\Users\\osama\\Desktop\\multimedia\\test"+String.valueOf(i)+".jpg");
                    HighGui.imshow("frame", src);

                    keyFrames.add(src);
                    //System.out.println(keyFrames.get(i).size());
                    //System.out.println(keyFrames.size());

                }
                break;

            }

        }
        int Count2=0;


        Q="select * from videos";
        Statement stmt = conn.createStatement();
        ResultSet R= stmt.executeQuery(Q);
        //System.out.println(Q);
        while(R.next()){
            int videoId= R.getInt("id");
            System.out.println(videoId);
            Q="select * from images where videoId = "+String.valueOf(videoId);
            //System.out.println(Q);
            Statement stmt2 = conn.createStatement();
            ResultSet R2= stmt2.executeQuery(Q);

            List<Mat> QueryKeyFrames = new ArrayList<>();

            while(R2.next()){
                String frameUrl= R2.getString("url");

                //System.out.println(frameUrl);
                Mat qFrame = Imgcodecs.imread(frameUrl);

                QueryKeyFrames.add(qFrame);
            }

            //System.out.println(keyFrames.size());
            //System.out.println(QueryKeyFrames.size());
            for (Mat keyFrameI : keyFrames)
            {
                for (Mat keyFrameQ : QueryKeyFrames)
                {
                    Mat Histi= calcHist(keyFrameQ);
                    //System.out.println(keyFrameI.size());
                    Mat Histq= calcHist(keyFrameI);


                   Mat diff = Mat.zeros(256, 1, 5);
                   Core.absdiff(Histi,Histq,diff);
                   Scalar sum=Core.sumElems(diff);
                    //System.out.println(sum.val[0]);
                   double mean=(((double)sum.val[0]))/256;
                    //System.out.println("mean"+String.valueOf(mean));
                   if(mean<=0.002){

                       Count2++;
                   }

                }

            }

            if(Count2/(keyFrames.size())*2>=1){
                System.out.println("wslt"+String.valueOf(videoId));
            }
            Count2=0;
        }

        //System.out.println("count  "+ String.valueOf(Count2));



    }
}
