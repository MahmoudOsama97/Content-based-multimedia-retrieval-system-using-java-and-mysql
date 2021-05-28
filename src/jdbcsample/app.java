/*
 * How To Connect To Mysql Database In Java Using Netbeans And Create New Database Jdbc
 */
//package connecttoserverandcreatedatabas


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.opencv.core.CvType.*;


public class app {

    private static int id=0;



    public static void PushDataBase(File[] files,Connection conn, String name) throws SQLException {
        for (File file : files) {

            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getAbsolutePath());
                String name2=file.getName();
                PushDataBase(file.listFiles(), conn, name2); // Calls same method again.
            } else {
                new InsertImage().run(file.getAbsolutePath(),conn,
                        id,name+ String.valueOf(id), "");
                id++;
                System.out.println("File: " + file.getAbsolutePath());
            }
        }
    }
    public static void main(String[] args) throws SQLException {
        // Declaring all variables
        String jdbcDriver = "com.mysql.cj.jdbc.Driver";
        Connection conn = null;
        String username = "root";
        String password = "MySQL_2021";
        String serverUrl = "jdbc:mysql://localhost:3306/multimedia";
        String dbName = "products_db2";
        String checkDb = "SELECT SCHEMA_NAME FROM `information_schema`.`SCHEMATA` WHERE `SCHEMA_NAME` = '" + dbName + "'";
        ResultSet rs = null;
        String Q="select * from film";
        boolean dbFound = false;

        try {
            //check jdbc driver (mysql connector / j). Make sure the connector is configured correctly (added to libraries) before checking it.
            Class.forName(jdbcDriver);
            System.out.println("Driver Loaded");
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver Failed To Load");
            System.out.println(ex.getMessage());
        }

        try {
            //connecting to xampp server (Apache Server)
            System.out.println("here");
            conn = DriverManager.getConnection(serverUrl,username,password);
            System.out.println("Connected To Server Successfully");
        } catch (SQLException ex) {
            System.out.println("Failed To Connect To Server Successfully");
            System.out.println(ex.getMessage());

        }

//        Q="select * from film";
//        Statement stmt = conn.createStatement();
//        ResultSet R = stmt.executeQuery(Q);
//        while(R.next()){
//            System.out.println(R.getString("title"));
//        }

//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        //Mat x= new Mat(3,3, CV_8UC1);
//        Mat x=Mat.ones(3,3,CV_8UC1);
//        Mat mat = Mat.eye(3, 3, CV_8UC1);
//        System.out.println("mat = " + mat.dump()+"\n"+x.dump());

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //new InsertImage().run("C:\\Users\\osama\\Desktop\\multimedia\\min.jpg",conn,3,"minions","");
        //new SearchImage().hist("C:\\Users\\osama\\Desktop\\multimedia\\sunflower1.jpg",conn,  0.6f);
        //new SearchImage().mean("C:\\Users\\osama\\Desktop\\multimedia\\sunflower4.jpg" , conn ) ;
        //new SearchImage().grid("C:\\Users\\osama\\Desktop\\multimedia\\daisy1.jpg" , conn,3,3 ) ;
        //new InsertVideo().run("C:\\Users\\osama\\Desktop\\multimedia\\video5.mp4",conn,5,"video5","");
        new SearchVideo().Search("C:\\Users\\osama\\Desktop\\multimedia\\video4.mp4",conn);





        //Mat x=Mat.eye(4 ,4,CV_8UC1);
        //System.out.println(new SearchImage().getMean(x));
        //File dir = new File("C:\\Users\\osama\\Desktop\\multimedia\\flowers");
        //PushDataBase(dir.listFiles(), conn,"flower");

        }

}
