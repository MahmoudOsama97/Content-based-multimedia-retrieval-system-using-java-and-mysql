package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import java.util.Arrays;
import java.util.Objects;

public class CBVR {
    public static int start = 0;
    private static final int videosPerView = 2;
    private static final String videosPath = "C:\\Users\\osama\\Desktop\\multimedia\\videos\\";
    private static final URI videosURI = new File(videosPath).toURI();
    private static final HBox hVideos = new HBox();
    private static String searchVideoPath = "";
    public static void display (Connection conn) throws Exception {
        Stage primaryStage = new Stage();
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("CBVR System");
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(30, 0, 30, 0));
        vbox.setSpacing(40);
        vbox.setAlignment(Pos.CENTER);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Video");

        HBox hUploadedMedia = new HBox();
        hUploadedMedia.setSpacing(20);
        hUploadedMedia.setAlignment(Pos.CENTER);

        VBox uploadControls = new VBox();
        uploadControls.setPadding(new Insets(5, 5, 5, 5));
        uploadControls.setSpacing(10);
        uploadControls.setAlignment(Pos.CENTER);
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
        uploadControls.getChildren().addAll(buttonUpload, buttonAddToDb, buttonSearch, buttonPlay, buttonPause, buttonMute, buttonStop);

        MediaView uploadMediaView = new MediaView(new MediaPlayer(new Media(videosURI + "video2.mp4")));
        uploadMediaView.setPreserveRatio(true);
        uploadMediaView.setFitWidth(400);

        buttonUpload.setOnAction(e -> {
            try {
                File video = fileChooser.showOpenDialog(primaryStage);
                uploadMediaView.getMediaPlayer().dispose();
                uploadMediaView.setMediaPlayer(new MediaPlayer(new Media(video.toURI().toString())));
                searchVideoPath = video.getAbsolutePath();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        buttonAddToDb.setOnAction(e -> {
            if (searchVideoPath.equals("")){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Error");
                errorAlert.setContentText("No video was uploaded!");
                errorAlert.showAndWait();
            } else {
                System.out.println("Added To DB!");
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

        });

        hUploadedMedia.getChildren().addAll(uploadControls, uploadMediaView);

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
                viewer.getMediaPlayer().play();
            }
        });
        viewerButtonPause.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                viewer.getMediaPlayer().pause();
            }
        });
        viewerButtonMute.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                if (viewer.getMediaPlayer().isMute()) {
                    viewer.getMediaPlayer().setMute(false);
                    viewerButtonMute.setText("Mute");
                } else if (!viewer.getMediaPlayer().isMute()) {
                    viewer.getMediaPlayer().setMute(true);
                    viewerButtonMute.setText("Unmute");
                }
            }
        });
        viewerButtonStop.setOnAction(e -> {
            MediaView[] viewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: viewers) {
                viewer.getMediaPlayer().stop();
            }
        });

        initHVideos();
        MediaPlayer[] players;
        try{
            players = getVideos(videosPath, getVideosNames(videosPath, start));
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

        Button buttonMinus = new Button("<<");
        Button buttonPlus = new Button(">>");
        buttonMinus.setFont(Font.font ("Verdana", 18));
        buttonPlus.setFont(Font.font ("Verdana", 18));
        hViewer.getChildren().addAll(buttonMinus, hVideos, buttonPlus);

        buttonMinus.setOnAction(e -> {
            modifyStart(false);
            MediaPlayer[] minusPlayers = new MediaPlayer[videosPerView];
            try {
                minusPlayers = getVideos(videosPath, getVideosNames(videosPath, start));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            MediaView[] minusViewers= hVideos.getChildren().toArray(new MediaView[0]);
            int minusI = 0;
            for (MediaView viewer: minusViewers) {
                viewer.getMediaPlayer().dispose();
                viewer.setMediaPlayer(minusPlayers[minusI]);
                minusI += 1;
            }
        });
        buttonPlus.setOnAction(e -> {
            modifyStart(true);
            MediaPlayer[] plusPlayers = new MediaPlayer[videosPerView];
            try {
                plusPlayers = getVideos(videosPath, getVideosNames(videosPath, start));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            MediaView[] plusViewers= hVideos.getChildren().toArray(new MediaView[0]);
            int plusI = 0;
            for (MediaView viewer: plusViewers) {
                viewer.getMediaPlayer().dispose();
                viewer.setMediaPlayer(plusPlayers[plusI]);
                plusI += 1;
            }
        });

        vbox.getChildren().addAll(hUploadedMedia, hViewer, hViewerButtons);
        Scene scene = new Scene(vbox,900, 700);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            uploadMediaView.getMediaPlayer().dispose();
            MediaView[] closedViewers= hVideos.getChildren().toArray(new MediaView[0]);
            for (MediaView viewer: closedViewers) {
                viewer.getMediaPlayer().dispose();
            }
        });
        primaryStage.showAndWait();
    }
    private static void initHVideos() {
        hVideos.setAlignment(Pos.CENTER);
        hVideos.setSpacing(20);
        hVideos.setPadding(new Insets(5, 5, 5, 5));
        if (hVideos.getChildren().isEmpty())
        {
            MediaView left = new MediaView();
            MediaView right = new MediaView();
            left.setFitWidth(300);
            left.setFitHeight(300*9/16);
            right.setFitWidth(300);
            right.setFitHeight(300*9/16);
            hVideos.getChildren().addAll(left, right);
        }
    }
    private static MediaPlayer[] getVideos(String videosPath, String[] videosNames) throws FileNotFoundException {
        MediaPlayer[] players = new MediaPlayer[videosPerView];
        int i = 0;
        for (String videoName: videosNames) {
            players[i] = new MediaPlayer(new Media(new File(videosPath + videoName).toURI().toString()));
            i++;
        }
        return players;
    }
    private static String[] getVideosNames(String videosPath, int start) {
        return Arrays.copyOfRange(Objects.requireNonNull(new File(videosPath).list()), start, start + videosPerView);
    }
    private static int getMaxFileCount(String videosPath) {
        return Objects.requireNonNull(new File(videosPath).list()).length;
    }
    private static void modifyStart(boolean increment) {
        if (increment) {
            if (!((start + videosPerView) > getMaxFileCount(videosPath))) {
                start += videosPerView;
            }
        } else {
            if (!((start - videosPerView) < 0)) {
                start -= videosPerView;
            }
        }
    }
}
