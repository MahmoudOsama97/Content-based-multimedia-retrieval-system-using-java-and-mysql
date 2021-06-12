import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import java.lang.Math;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_16UC1;
import static org.opencv.core.CvType.CV_8UC1;

public class InsertVideo {

    String framesPath="C:\\Users\\osama\\Desktop\\multimedia\\frames";

    public void run(String args, Connection conn, int id, String title, String info) throws SQLException {
        Mat frame = new Mat();
        VideoCapture camera = new VideoCapture(args);
        String Q="";
        try {
            Q = "insert into videos(id,title,url,info) values("+ id + ", " + "\""+title+ "\"" + ", " + "\""+args.replace("\\", "\\\\") + "\""  + ", " +  "\""+info+ "\""+ ")" ;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(Q);
        }catch (SQLException ex) {
        }
        double k1=0.1 ,k2=0.8;
        Mat histold = Mat.zeros(256, 1, 5);
        Mat frameGrey = new Mat();
        int count=0;
        ResultSet R = null;
        try {
            Q = "select max(id) from images ;" ;
            System.out.println(Q);
            Statement stmt = conn.createStatement();
            R = stmt.executeQuery(Q);
        }catch (SQLException ex) {
        }
        int idMax=0;
        if(R.next()) {
             idMax = R.getInt(1);
        }
        while (true) {
            if (camera.read(frame)) {

                Imgproc.cvtColor(frame,frameGrey,Imgproc.COLOR_RGB2GRAY);
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
                Core.pow(diff,2,diffSquare);
                Scalar sumSquare=Core.sumElems(diffSquare);
                double meanSquare=((double)sumSquare.val[0])/256;
                Core.absdiff(Hist,histold,diff);
                Scalar sum=Core.sumElems(diff);
                double mean=((double)sum.val[0])/256;
                double sd=Math.sqrt(Math.abs(meanSquare-mean));
                double threshold=k1*mean+k2*sd;
                histold=Hist;
                if(threshold>15) {
                    String ImagePath=framesPath+"\\"+ title +"_"+String.valueOf(count+1)+".jpg";
                    Imgcodecs.imwrite(ImagePath,frame);
                    new InsertImage().run(ImagePath,conn,idMax+count+1,title+"_"+String.valueOf(count+1),info);
                    try {
                        Q = "update images set videoId= "+String.valueOf(id)+" where id="+String.valueOf(idMax+count+1) ;
                         stmt.executeUpdate(Q);
                    }catch (SQLException ex) {
                    }
                    count++;
                }
            }
            else{
                break;
            }
        }
    }
}
