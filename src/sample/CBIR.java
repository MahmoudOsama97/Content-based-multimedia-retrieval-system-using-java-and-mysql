package sample;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

public class CBIR {
    /*
       Responsible for displaying the GUI of CBIR system.
    */
    private static Vector<String> resultVec = new Vector<String>();
    private static int start = 0;
    private static final int imagesPerView = 8;
    private static final String imagesPath = "C:\\Users\\osama\\Desktop\\multimedia\\images\\";
    private static final FlowPane flowPane = new FlowPane();
    private static String searchImagePath = "";
    public static void display (Connection conn) throws FileNotFoundException {
        /*
           Main method. Displays the GUI of the CBVR system.
        */
        Stage primaryStage = new Stage();
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("CBIR System");
        HBox hViewer = new HBox();
        HBox hControl = new HBox();
        HBox hSearch = new HBox();
        VBox vControl = new VBox();
        VBox vBox = new VBox();
        Label searchLabel = new Label("Search Options");
        ChoiceBox<String> searchOptions = new ChoiceBox();
        FileChooser fileChooser = new FileChooser()  ;
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("images", "*.jpg","*.png","*.jpeg");
        fileChooser.setTitle("Choose Image");

        hSearch.setSpacing(10);
        hSearch.setAlignment(Pos.CENTER_RIGHT);
        hSearch.getChildren().addAll(searchLabel, searchOptions);

        HBox hTitle= new HBox();
        HBox hInfo = new HBox();
        HBox hAttribute = new HBox();

        Label titleL = new Label("Title");
        Label infoL = new Label("Info");
        Label attributeL = new Label();
        TextField titleT=new TextField ();
        TextField infoT=new TextField ();
        TextField attributeT = new TextField();

        hTitle.setSpacing(10);
        hTitle.setAlignment(Pos.CENTER_RIGHT);
        hTitle.getChildren().addAll(titleL,titleT);
        hInfo.setSpacing(10);
        hInfo.setAlignment(Pos.CENTER_RIGHT);
        hInfo.getChildren().addAll(infoL,infoT);
        hTitle.setSpacing(10);
        hInfo.setSpacing(10);

        hAttribute.setSpacing(10);
        hAttribute.setAlignment(Pos.CENTER_RIGHT);
        hAttribute.getChildren().addAll(attributeL, attributeT);
        hAttribute.setVisible(false);

        final String[] title = {null};
        final String[] info = {null};
        searchOptions.getItems().add("Color Layout");
        searchOptions.getItems().add("Histogram");
        searchOptions.getItems().add("Global Mean");
        searchOptions.setValue("Global Mean");

        searchOptions.valueProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue.equals("Histogram")) {
                attributeL.setText("Threshold");
                hAttribute.setVisible(true);

            } else if (newValue.equals("Color Layout")) {
                attributeL.setText("# Blocks");
                hAttribute.setVisible(true);
            } else {
                hAttribute.setVisible(false);
            }

        });

        File imageFile = new File(imagesPath+"test0.jpg");
        ImageView imageView = new ImageView(new Image(new FileInputStream(imageFile)));

        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        Button buttonUpload = new Button("Upload");
        buttonUpload.setPrefWidth(100);
        Button buttonAddToDb = new Button("Add To DB");
        buttonAddToDb.setPrefWidth(100);
        Button buttonSearch = new Button("Search");
        buttonSearch.setPrefWidth(100);
     
        buttonUpload.setOnAction(e -> {
            try {
                fileChooser.getExtensionFilters().add(extFilter);
                File image = fileChooser.showOpenDialog(primaryStage);
                if (image != null) {
                    imageView.setImage(new Image(new FileInputStream(image)));
                    searchImagePath = image.getAbsolutePath();
                } else {
                    createErrorAlert("Error Uploading Image!");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        buttonAddToDb.setOnAction(e -> {
            if (searchImagePath.equals("")){
                createErrorAlert("No image was uploaded");
            } else {
                title[0] =titleT.getText();
                info[0] =infoT.getText();
                if (!title[0].equals("")) {
                    try {
                        new InsertImage().run(searchImagePath,conn,title[0],info[0]);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    System.out.println("Added To DB!");
                } else {
                    createErrorAlert("Enter Title!");
                }
            }
        });
        buttonSearch.setOnAction(e -> {
            if (!searchImagePath.equals("")){
                String searchTech = searchOptions.getValue();
                if(searchTech.equals("Global Mean")) {
                    try {
                        resultVec.clear();
                        start=0;
                        resultVec = new SearchImage().mean(searchImagePath , conn );
                        if (resultVec.isEmpty()) {
                            createErrorAlert("No image was found!");
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                } else if (searchTech.equals("Histogram")) {
                    try {
                        float thresh = Float.parseFloat(attributeT.getText());
                        try {
                            start=0;
                            resultVec.clear();

                            resultVec = new SearchImage().hist(searchImagePath, conn,  thresh);
                            if (resultVec.isEmpty()) {
                                createErrorAlert("No image was found!");
                            }
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    } catch(NumberFormatException exception) {
                        createErrorAlert("Threshold must be a number!");
                    }

                } else if (searchTech.equals("Color Layout")) {
                    try {
                        int n_blocks = Integer.parseInt(attributeT.getText());
                        try {
                            start=0;
                            resultVec.clear();
                            resultVec = SearchImage.grid(searchImagePath, conn, n_blocks, n_blocks);
                            if (resultVec.isEmpty()) {
                                createErrorAlert("No image was found!");
                            }
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } catch(NumberFormatException exception) {
                        createErrorAlert("# Blocks must be an integer number!");
                    }

                }
                if (!resultVec.isEmpty()) {
                    try {
                        addFlowPane(getImagesNames(resultVec, start));
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }
                }
            } else {
                createErrorAlert("Enter image to search!");
            }
        });

        vControl.setPadding(new Insets(5, 5, 5, 5));
        vControl.getChildren().addAll(buttonUpload, hTitle, hInfo, buttonAddToDb, hSearch, hAttribute, buttonSearch);
        vControl.setSpacing(10);
        vControl.setAlignment(Pos.CENTER_RIGHT);
        hControl.getChildren().addAll(vControl, imageView);

        hControl.setAlignment(Pos.CENTER);
        hControl.setSpacing(40);

        Button buttonMinus = new Button("<<");
        Button buttonPlus = new Button(">>");
        buttonMinus.setFont(Font.font ("Verdana", 18));
        buttonPlus.setFont(Font.font ("Verdana", 18));
        buttonMinus.setOnAction(e -> {
            modifyStart(false);
            try {
                addFlowPane(getImagesNames(resultVec, start));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        buttonPlus.setOnAction(e -> {
            modifyStart(true);
            try {
                addFlowPane(getImagesNames(resultVec, start));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        hViewer.getChildren().add(buttonMinus);
        initFlowPane();

        hViewer.getChildren().add(flowPane);
        hViewer.getChildren().add(buttonPlus);

        hViewer.setAlignment(Pos.CENTER);
        hViewer.setSpacing(20);

        vBox.getChildren().add(hControl);
        vBox.getChildren().add(hViewer);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(50);
        Scene scene = new Scene(vBox, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.showAndWait();
    }

    private static void initFlowPane() {
        /*
           Initialize the place where the result images will be placed.
        */
        flowPane.setPadding(new Insets(5, 5, 5, 5));
        flowPane.setVgap(5);
        flowPane.setHgap(25);
        flowPane.setPrefHeight(320);
        flowPane.setPrefWidth(700);
        flowPane.setMaxSize(700, 320);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setStyle("-fx-background-color: DAE6F3;");
    }
    public static void addFlowPane(String[] imagesNames) throws FileNotFoundException {
        /*
           Adds a place to put the result images are displayed. 
        */
        flowPane.getChildren().clear();
        ImageView[] images = getImages(imagesNames);
        for (int i=0; i<imagesPerView; i++) {
            if (images[i] == null) continue;
            flowPane.getChildren().add(images[i]);
        }
    }
    private static ImageView[] getImages(String[] imagesNames) throws FileNotFoundException {
        /*
           Returns the image view in which the images will fit.
        */
        ImageView[] pages = new ImageView[imagesPerView];
        int i = 0;
        for (String imageName: imagesNames) {
            if (imageName == null) continue;
            ImageView imageView = new ImageView(new Image(new FileInputStream(imageName)));
            imageView.setFitHeight(150);
            imageView.setFitWidth(150);
            pages[i] = imageView;
            i++;
        }
        return pages;
    }

    private static String[] getImagesNames(Vector <String> vectorRes, int start) {
        /*
           Gets the names of the result images so the system could display them. 
        */
        return  Arrays.copyOfRange(vectorRes.toArray(new String[vectorRes.size()]), start, start + imagesPerView);
    }
    private static int getMaxFileCount() {
        /*
           Gets the count of files in the returned vector of images.
        */
        return resultVec.size();
    }
    private static void modifyStart(boolean increment) {
        /*
           Modifies the start pointer to point on the next group of picture to be displayed.
        */
        if (increment) {
            if (!((start + imagesPerView) >= getMaxFileCount())) {
                start += imagesPerView;
            }
        } else {
            if (!((start - imagesPerView) <= 0)) {
                start -= imagesPerView;
            } else {
                start=0;
            }
        }
    }
    private static void createErrorAlert(String message) {
        /*
           Creates error modal in case of error.
        */
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}
