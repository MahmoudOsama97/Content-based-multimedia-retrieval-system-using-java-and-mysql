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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sample.CBIR;
import sample.CBVR;
import sample.InsertImage;
import sample.SearchImage;

import java.io.FileInputStream;

public class Main extends Application {
    static Connection conn = null;
    String logoPath = "C:\\Users\\osama\\Desktop\\multimedia\\images\\";
    @Override
    public void start(Stage primaryStage) throws Exception{
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(200, 0, 0, 0));
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(15);
        StackPane stackPane = new StackPane();
        ImageView logo = new ImageView(new Image(new FileInputStream(logoPath + "logo.png")));
        logo.setFitWidth(400);
        logo.setFitHeight(400);
        Button buttonCbir = new Button("CBIR System");
        Button buttonCbvr = new Button("CBVR System");
        buttonCbir.setPrefWidth(125);
        buttonCbvr.setPrefWidth(125);

        buttonCbir.setOnAction(e -> {
            try{
                CBIR.display(conn);
            } catch (Exception exception) {
                System.out.println(exception);
            }
        });
        buttonCbvr.setOnAction(e -> {
            try{
                CBVR.display(conn);
            } catch (Exception exception) {
                System.out.println(exception);
            }
        });
        vbox.getChildren().addAll(buttonCbir, buttonCbvr);
        stackPane.getChildren().addAll(logo, vbox);
        Scene scene = new Scene(stackPane, 400, 400);
        primaryStage.setTitle("Multimedia Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static int id=0;
    public static void PushDataBase(File[] files,Connection conn, String name) throws SQLException {
        for (File file : files) {

            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getAbsolutePath());
                String name2=file.getName();
                PushDataBase(file.listFiles(), conn, name2); // Calls same method again.
            } else {
                new InsertImage().run(file.getAbsolutePath(),conn,name+ String.valueOf(id), "");
                id++;
                System.out.println("File: " + file.getAbsolutePath());
            }
        }
    }
    public static void main(String[] args) throws SQLException {
        // Declaring all variables
        String jdbcDriver = "com.mysql.cj.jdbc.Driver";
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
        System.load("C:\\Users\\osama\\Downloads\\opencv\\build\\java\\x64\\opencv_java452.dll");
        launch(args);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
