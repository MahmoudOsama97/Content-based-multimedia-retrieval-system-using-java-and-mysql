/*
 * How To Connect To Mysql Database In Java Using Netbeans And Create New Database Jdbc
 */
//package connecttoserverandcreatedatabas


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

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
        //new InsertImage().run("C:\\Users\\osama\\Desktop\\multimedia\\sunflower6.jpg",conn,12,"sunflower6","");
        //new SearchImage().hist("C:\\Users\\osama\\Desktop\\multimedia\\sunflower4.jpg",conn);
        //new SearchImage().mean("C:\\Users\\osama\\Desktop\\multimedia\\sunflower4.jpg" , conn ) ;
        new SearchImage().grid("C:\\Users\\osama\\Desktop\\multimedia\\sunflower4.jpg" , conn,2,2 ) ;

        //Mat x=Mat.eye(4 ,4,CV_8UC1);
        //System.out.println(new SearchImage().getMean(x));

        }

}


/*
        try {
            // Output sql query for checking if the database exists
            System.out.println("Check if database exists query - " + checkDb);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(checkDb);
            // Output the resultset value
            System.out.println("Result Set Value " + rs.next());
            //Move the cursor one row from its current position
            if (rs.next()) {
                //If the database is found in the information_schema, set the boolean value to true
                dbFound = true;
            }

            //If the database is no found create new database
        } catch (SQLException ex) {
            System.out.println("Error " + ex.getMessage());
        }


*/




//public class app {
//    public static void main(String[] args) {
//        // Load the native OpenCV library
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        new CalcHist().run(args);
//
//    }
//}

/*
public class app {
    public static void main(String[] args) {
        // Load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String[] x=new String[3];

        x[0]="C:\\Users\\osama\\Desktop\\multimedia\\download.jpg";
        x[1]="C:\\Users\\osama\\Desktop\\multimedia\\meme.jpg";
        x[2]="C:\\Users\\osama\\Desktop\\multimedia\\min.jpg";

        new CompareHist().run(x);
    }
}

 */