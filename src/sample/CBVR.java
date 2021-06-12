package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

public class CBVR {
    /*
        The main class that displays the GUI of CBVR system.
    */
    private static Vector<String> resultVec = new Vector<String>();
    public static int start = 0;
    private static final int videosPerView = 1;
    private static final String videosPath = "C:\\Users\\osama\\Desktop\\multimedia\\videos\\";
    private static final URI videosURI = new File(videosPath).toURI();
    private static final HBox hVideos = new HBox();
    private static String searchVideoPath = "";
    public static void display (Connection conn) throws Exception {
        /*
            The main method that displays the GUI of the CBVR System.
        */
        Stage primaryStage = new Stage();
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("CBVR System");
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(30, 0, 30, 0));
        vbox.setSpacing(40);
        vbox.setAlignment(Pos.CENTER);

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("videos", "*.mp4");

        fileChooser.setTitle("Choose Video");

        HBox hUploadedMedia = new HBox();
        hUploadedMedia.setSpacing(20);
        hUploadedMedia.setAlignment(Pos.CENTER);

        HBox hTitle = new HBox();
        HBox hInfo = new HBox();
        HBox hCompare = new HBox();
        Label titleL = new Label("Title");
        Label infoL = new Label("Info");
        Label compareL = new Label("Threshold");
        TextField titleT = new TextField ();
        TextField infoT = new TextField ();
        TextField compareT = new TextField();
        hTitle.getChildren().addAll(titleL,titleT);
        hTitle.setAlignment(Pos.CENTER_RIGHT);
        hInfo.getChildren().addAll(infoL,infoT);
        hInfo.setAlignment(Pos.CENTER_RIGHT);
        hCompare.getChildren().addAll(compareL, compareT);
        hCompare.setAlignment(Pos.CENTER_RIGHT);
        hTitle.setSpacing(10);
        hInfo.setSpacing(10);
        hCompare.setSpacing(10);
        final String[] title = {null};
        final String[] info = {null};
        final String[] compareRatio = {null};

        VBox videoControls = new VBox();
        videoControls.setPadding(new Insets(5, 5, 5, 5));
        videoControls.setSpacing(10);
        videoControls.setAlignment(Pos.CENTER_RIGHT);
        VBox uploadControls = new VBox();
        uploadControls.setPadding(new Insets(5, 5, 5, 5));
        uploadControls.setSpacing(10);
        uploadControls.setAlignment(Pos.CENTER_RIGHT);
        Button buttonUpload = new Button("Upload");
        buttonUpload.setPrefWidth(100);
        Button buttonAddToDb = new Button("Add To DB");
        buttonAddToDb.setPrefWidth(100);
        Button buttonPlay = new Button("Play");
        buttonPlay.setPrefWidth(100);
        Button buttonPause = new Button("Pause");
        buttonPause.setPrefWidth(100);
        Button buttonMute = new Button("Mute");
        buttonMute.setPrefWidth(100);
        Button buttonStop = new Button("Stop");
        buttonStop.setPrefWidth(100);
        Button buttonSearch = new Button("Search");
        buttonSearch.setPrefWidth(100);
        videoControls.getChildren().addAll(buttonPlay, buttonPause, buttonMute, buttonStop);
        uploadControls.getChildren().addAll(buttonUpload, hTitle, hInfo, buttonAddToDb, hCompare, buttonSearch);

        MediaView uploadMediaView = new MediaView(new MediaPlayer(new Media(videosURI + "video2.mp4")));
        uploadMediaView.setPreserveRatio(true);
        uploadMediaView.setFitWidth(400);

        buttonUpload.setOnAction(e -> {
            try {
                fileChooser.getExtensionFilters().add(extFilter);
                File video = fileChooser.showOpenDialog(primaryStage);
                if (uploadMediaView.getMediaPlayer() != null) {
                    uploadMediaView.getMediaPlayer().dispose();
                    if(video != null) {
                        uploadMediaView.setMediaPlayer(new MediaPlayer(new Media(video.toURI().toString())));
                    } else {
                        createErrorAlert("Error Uploading File!");
                    }
                }
                searchVideoPath = video != null? video.getAbsolutePath(): "";
            } catch (Exception exception) {
                createErrorAlert("Error Uploading File!");
                exception.printStackTrace();
            }
        });
        buttonAddToDb.setOnAction(e -> {
            if (searchVideoPath.equals("")){
                createErrorAlert("No video was uploaded!");
            } else {
                title[0] = titleT.getText();
                info[0] = infoT.getText();
                if (title[0].equals("")) {
                    createErrorAlert("You must input a title!");
                } else {
                    System.out.println(title[0]);
                    System.out.println(info[0]);

                    try {
                        new InsertVideo().run(searchVideoPath,conn,title[0],info[0]);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    System.out.println("Added To DB!");
                }
            }
        });
        buttonPlay.setOnAction(e -> {
            uploadMediaView.getMediaPlayer().play();
        });
        buttonPause.setOnAction(e -> {
            uploadMediaView.getMediaPlayer().pause();
        });
        buttonMute.setOnAction(e -> {
            MediaPlayer player = uploadMediaView.getMediaPlayer();
            if(player.isMute()) {
                player.setMute(false);
                buttonMute.setText("Mute");
            } else if(!player.isMute()) {
                player.setMute(true);
                buttonMute.setText("Unmute");
            }
        });
        buttonStop.setOnAction(e -> {
            uploadMediaView.getMediaPlayer().stop();
        });
        buttonSearch.setOnAction(e -> {
            if (searchVideoPath.equals("")){
                createErrorAlert("No video was uploaded!");
            } else {
                compareRatio[0] = compareT.getText();
                if (compareRatio[0].equals("")) {
                    createErrorAlert("Enter Threshold!");
                } else {
                    try {
                        start=0;
                        resultVec.clear();
                        resultVec = new SearchVideo().Search(searchVideoPath, conn, Float.parseFloat(compareRatio[0]));
                    } catch (SQLException throwables) {
                        createErrorAlert("No video is found!");
                        throwables.printStackTrace();
                    }
                    MediaPlayer[] players;
                    try{
                        players = getVideos(getVideosNames(resultVec, start));
                        int i = 0;
                        MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
                        for (MediaView viewer: viewers) {
                            viewer.setMediaPlayer(players[i]);
                            i += 1;
                        }
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });

        hUploadedMedia.getChildren().addAll(uploadControls, uploadMediaView, videoControls);

        HBox hViewer = new HBox();
        hViewer.setSpacing(20);
        hViewer.setAlignment(Pos.CENTER);
        HBox hViewerButtons = new HBox();
        hViewerButtons.setSpacing(20);
        hViewerButtons.setAlignment(Pos.CENTER);

        Button viewerButtonPlay = new Button("Play");
        viewerButtonPlay.setPrefWidth(100);
        Button viewerButtonPause = new Button("Pause");
        viewerButtonPause.setPrefWidth(100);
        Button viewerButtonMute = new Button("Mute");
        viewerButtonMute.setPrefWidth(100);
        Button viewerButtonStop = new Button("Stop");
        viewerButtonStop.setPrefWidth(100);
        hViewerButtons.getChildren().addAll(viewerButtonPlay, viewerButtonPause, viewerButtonMute, viewerButtonStop);

        viewerButtonPlay.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                if (viewer.getMediaPlayer() == null) {
                    createErrorAlert("No video to play!");
                } else {
                    viewer.getMediaPlayer().play();
                }
            }
        });
        viewerButtonPause.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                if (viewer.getMediaPlayer() == null) {
                    createErrorAlert("No video to pause!");
                } else {
                    viewer.getMediaPlayer().pause();
                }
            }
        });
        viewerButtonMute.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                if (viewer.getMediaPlayer() == null) {
                    createErrorAlert("No video to mute!");
                } else {
                    if (viewer.getMediaPlayer().isMute()) {
                        viewer.getMediaPlayer().setMute(false);
                        viewerButtonMute.setText("Mute");
                    } else if (!viewer.getMediaPlayer().isMute()) {
                        viewer.getMediaPlayer().setMute(true);
                        viewerButtonMute.setText("Unmute");
                    }
                }
            }
        });
        viewerButtonStop.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                if (viewer.getMediaPlayer() == null) {
                    createErrorAlert("No video");
                } else {
                    viewer.getMediaPlayer().stop();
                }
            }
        });

        initHVideos();
        Button buttonMinus = new Button("<<");
        Button buttonPlus = new Button(">>");
        buttonMinus.setFont(Font.font ("Verdana", 18));
        buttonPlus.setFont(Font.font ("Verdana", 18));
        hViewer.getChildren().addAll(buttonMinus, hVideos, buttonPlus);

        buttonMinus.setOnAction(e -> {
            modifyStart(false);
            MediaPlayer[] minusPlayers = new MediaPlayer[videosPerView];
            try {
                minusPlayers = getVideos( getVideosNames(resultVec, start));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            MediaView[] minusViewers= hVideos.getChildren().toArray(new MediaView[0]);
            int minusI = 0;
            for (MediaView viewer: minusViewers) {
                if(viewer.getMediaPlayer()==null) {
                    continue;
                }
                viewer.getMediaPlayer().dispose();
                viewer.setMediaPlayer(minusPlayers[minusI]);
                minusI += 1;
            }
        });
        buttonPlus.setOnAction(e -> {
            modifyStart(true);
            MediaPlayer[] plusPlayers = new MediaPlayer[videosPerView];
            try {
                plusPlayers = getVideos(getVideosNames(resultVec, start));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            MediaView[] plusViewers= hVideos.getChildren().toArray(new MediaView[0]);
            int plusI = 0;
            for (MediaView viewer: plusViewers) {
                if( viewer.getMediaPlayer()==null){
                    continue;
                }
                viewer.getMediaPlayer().dispose();
                viewer.setMediaPlayer(plusPlayers[plusI]);
                plusI += 1;
            }
        });

        vbox.getChildren().addAll(hUploadedMedia, hViewer, hViewerButtons);
        Scene scene = new Scene(vbox, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            uploadMediaView.getMediaPlayer().dispose();
            MediaView[] closedViewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: closedViewers) {
                MediaPlayer player = viewer.getMediaPlayer();
                if(player == null){
                    continue;
                }
                viewer.getMediaPlayer().dispose();
            }
        });
        primaryStage.showAndWait();
    }
    private static void initHVideos() {
        /*
            Initializes the place where the result videos are placed.
        */
        hVideos.setAlignment(Pos.CENTER);
        hVideos.setSpacing(20);
        hVideos.setPadding(new Insets(5, 5, 5, 5));
        if (hVideos.getChildren().isEmpty())
        {
            if(videosPerView==1){
                MediaView left = new MediaView();
                left.setFitWidth(300);
                left.setFitHeight(300*9/16);
                hVideos.getChildren().addAll( left);
            } else {
                MediaView left = new MediaView();
                MediaView right = new MediaView();
                left.setFitWidth(300);
                left.setFitHeight(300 * 9 / 16);
                right.setFitWidth(300);
                right.setFitHeight(300 * 9 / 16);
                hVideos.getChildren().addAll(left, right);
            }
        }
    }
    private static MediaPlayer[] getVideos(String[] videosNames) throws FileNotFoundException {
        /*
            Gets the media players that contains the result videos from video names
        */
        MediaPlayer[] players = new MediaPlayer[videosPerView];
        int i = 0;
        for (String videoName: videosNames) {
            if (videoName == null) continue;
            players[i] = new MediaPlayer(new Media(new File(videoName).toURI().toString()));
            i++;
        }
        return players;
    }
    private static String[] getVideosNames(Vector <String> vectorRes, int start) {
        /*
            Gets the names of the result videos from the returned vector of results returned from the DB.
        */
        return  Arrays.copyOfRange(vectorRes.toArray(new String[vectorRes.size()]), start, start + videosPerView);
    }
    private static int getMaxFileCount() {
        /*
            Gets the count of the videos in the returned vector.
        */
        return resultVec.size();
    }
    private static void modifyStart(boolean increment) {
        /*
            Modifies the start pointer to point on the next group of videos of the returned vector of videos.
        */
        if (increment) {
            if (!((start + videosPerView) >= getMaxFileCount())) {
                start += videosPerView;
            }
        } else {
            if (!((start - videosPerView) <= 0)) {
                start -= videosPerView;
            } else {
                start = 0;
            }
        }
    }
    private static void createErrorAlert(String message) {
        /*
            Creates an error modal in case of error.
        */
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}
