package com.example.lestour;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javafx.scene.text.FontWeight;
import netscape.javascript.JSObject;

public class HelloApplication extends Application {
    private MediaPlayer mediaPlayer;
    private Label quizLabel;
    private Label resultLabel;
    private VBox quizBox;
    private MediaView mediaView;
    private Label locationTitle;
    private Label locationDescription;
    private HBox mediaTypeSelector;
    private ImageView imageView;
    private int userScore = 0;
    private Label scoreLabel;
    private Map<String, Boolean> completedLocations = new HashMap<>();
    private ProgressBar progressBar;
    private String currentLocationId;
    private Button favoriteButton;
    private Map<String, Boolean> favoriteLocations = new HashMap<>();
    private TextArea triviaArea;
    private Random random = new Random();
    private Map<String, List<String>> locationTrivia = new HashMap<>();
    private Map<String, List<QuizQuestion>> locationQuizzes = new HashMap<>();
    private WebView webView;
    private Button playPauseButton;
    private Slider mediaSlider;
    private Label timeLabel;
    private Button backButton;
    private HBox mediaControls;
    private ToggleGroup mediaToggleGroup;

    private class QuizQuestion {
        String question;
        String[] options;
        int correctAnswer;

        public QuizQuestion(String question, String opt1, String opt2, String opt3, int correctAnswer) {
            this.question = question;
            this.options = new String[]{opt1, opt2, opt3};
            this.correctAnswer = correctAnswer;
        }
    }

    public class JavaConnector {
        public void showLocation(String locationId, String title, String description) {
            Platform.runLater(() -> {
                if (currentLocationId != null && currentLocationId.equals(locationId)) {
                    return; // Avoid unnecessary reloads
                }

                currentLocationId = locationId;
                locationTitle.setText(title);
                locationDescription.setText(description.replaceAll("<br>", "\n"));
                mediaTypeSelector.setVisible(true);
                quizBox.setVisible(true);
                backButton.setVisible(true);

                boolean isFavorite = favoriteLocations.getOrDefault(locationId, false);
                favoriteButton.setText(isFavorite ? "★ Remove Favorite" : "★ Add to Favorites");
                favoriteButton.setStyle(isFavorite ?
                        "-fx-background-color: #e74c3c; -fx-text-fill: white;" :
                        "-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50;");

                loadLocationMedia(locationId, "video");
                showNextQuizQuestion();
            });
        }

        public void log(String message) {
            System.out.println("JS Log: " + message);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Lesotho Virtual Tour Guide");
        initializeQuizzes();
        initializeTrivia();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");

        HBox headerBox = new HBox(10);
        Label titleLabel = new Label("Lesotho Tour Guide");
        titleLabel.setFont(Font.font(18));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font(14));
        scoreLabel.setStyle("-fx-text-fill: white;");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(150);
        progressBar.setStyle("-fx-accent: #27ae60;");
        headerBox.getChildren().addAll(titleLabel, scoreLabel, progressBar);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: #2c3e50; -fx-padding: 8;");
        root.setTop(headerBox);

        setupMainContent(root);

        HBox footer = new HBox(10);
        footer.setPadding(new Insets(5));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #2c3e50;");
        Button helpButton = new Button("Help");
        helpButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
        helpButton.setOnAction(e -> showHelpDialog());
        footer.getChildren().add(helpButton);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1000, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeTrivia() {
        // Maseru trivia
        List<String> maseruTrivia = new ArrayList<>();
        maseruTrivia.add("Maseru was established as a police camp in 1869.");
        maseruTrivia.add("The name 'Maseru' means 'place of the red sandstone' in Sesotho.");
        maseruTrivia.add("Maseru is the only city in Lesotho with a population over 100,000.");
        locationTrivia.put("Maseru", maseruTrivia);

        // Thaba-Bosiu trivia
        List<String> thabaTrivia = new ArrayList<>();
        thabaTrivia.add("Thaba Bosiu means 'mountain at night' in Sesotho.");
        thabaTrivia.add("According to legend, the mountain grows taller at night to protect its inhabitants.");
        thabaTrivia.add("Thaba Bosiu was never conquered during Moshoeshoe I's reign.");
        locationTrivia.put("Thaba-Bosiu", thabaTrivia);

        // Maletsunyane trivia
        List<String> maletsunyaneTrivia = new ArrayList<>();
        maletsunyaneTrivia.add("Maletsunyane Falls is one of the highest single-drop waterfalls in Africa.");
        maletsunyaneTrivia.add("The falls freeze in winter, creating spectacular ice formations.");
        maletsunyaneTrivia.add("The abseil down the falls is one of the longest commercial abseils in the world.");
        locationTrivia.put("Maletsunyane", maletsunyaneTrivia);

        // Pioneer Mall trivia
        List<String> mallTrivia = new ArrayList<>();
        mallTrivia.add("Pioneer Mall was built on the site of the former Maseru Club sports grounds.");
        mallTrivia.add("It was the first modern shopping mall in Lesotho.");
        mallTrivia.add("The mall has over 70 stores and a food court.");
        locationTrivia.put("Pioneer-Mall", mallTrivia);

        // Sani Pass trivia
        List<String> saniTrivia = new ArrayList<>();
        saniTrivia.add("Sani Pass connects Lesotho with South Africa's KwaZulu-Natal province.");
        saniTrivia.add("The pass reaches an altitude of 2,876 meters (9,436 feet) above sea level.");
        saniTrivia.add("The Sani Top Chalet is home to 'the highest pub in Africa'.");
        locationTrivia.put("Sani-Pass", saniTrivia);

        // Katse Dam trivia
        List<String> katseTrivia = new ArrayList<>();
        katseTrivia.add("Katse Dam is part of the Lesotho Highlands Water Project.");
        katseTrivia.add("The dam wall is 185 meters high and 710 meters long.");
        katseTrivia.add("The project provides water to South Africa's Gauteng province.");
        locationTrivia.put("Katse-Dam", katseTrivia);
    }

    private void setupMainContent(BorderPane root) {
        // Location title display
        locationTitle = new Label("Select a location on the map");
        locationTitle.setFont(Font.font(16));
        locationTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        locationTitle.setAlignment(Pos.CENTER);
        locationTitle.setTextAlignment(TextAlignment.CENTER);
        locationTitle.setMaxWidth(Double.MAX_VALUE);

        backButton = new Button("← Back to Map");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
        backButton.setOnAction(e -> returnToMapView());
        backButton.setVisible(false);

        webView = new WebView();
        webView.setMinSize(500, 350);
        webView.setPrefSize(600, 400);
        webView.getEngine().setJavaScriptEnabled(true);

        String htmlContent = """
<!DOCTYPE html>
<html>
<head>
    <title>Lesotho Map</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/> 
    <style>
        html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; overflow: hidden; }
        .custom-popup .leaflet-popup-content-wrapper { border-radius: 8px; padding: 5px; }
    </style>
</head>
<body>
<div id="map"></div>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script> 
<script>
function debug(message) {
    console.log(message);
    if (window.javaConnector) {
        window.javaConnector.log(message);
    }
}
debug("Initializing map...");
var map;
function initMap() {
    map = L.map('map').setView([-29.61, 28.23], 7);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',  {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
    }).addTo(map);

    var customIcon = L.icon({
        iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png', 
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34]
    });

    function addMarker(lat, lng, title, locationId, description) {
        var marker = L.marker([lat, lng], {icon: customIcon})
            .addTo(map)
            .bindPopup('<div class="custom-popup"><b>' + title + '</b><br>' + description + '</div>');
        marker.on('click', function() {
            debug("Clicked: " + locationId);
            if (window.javaConnector && window.javaConnector.showLocation) {
                window.javaConnector.showLocation(locationId, title, description);
            }
        });
    }

    debug("Adding markers...");
    addMarker(-29.31, 27.48, "Maseru", "Maseru", "Capital city of Lesotho<br>Founded in 1869<br>Elevation: 1,600m");
    addMarker(-29.36, 27.70, "Thaba Bosiu", "Thaba-Bosiu", "Mountain fortress of King Moshoeshoe I<br>Founded in 1824<br>UNESCO World Heritage Site");
    addMarker(-29.90, 28.10, "Maletsunyane Falls", "Maletsunyane", "192m high waterfall<br>One of highest single-drop falls in Africa<br>Popular abseiling site");
    addMarker(-29.32, 27.50, "Pioneer Mall", "Pioneer-Mall", "Largest shopping mall in Lesotho<br>Opened in 2010<br>Over 70 stores");
    addMarker(-29.59, 29.29, "Sani Pass", "Sani-Pass", "Mountain pass to South Africa<br>Highest pub in Africa<br>Spectacular views");
    addMarker(-29.33, 28.50, "Katse Dam", "Katse-Dam", "Part of the Lesotho Highlands Water Project<br>710m long, 185m high<br>Provides water to South Africa");

    debug("Map initialization complete");
}
initMap();

// JavaScript-Java Bridge
window.addEventListener('DOMContentLoaded', () => {
    try {
        window.javaConnector = window.webkit.messageHandlers.javaConnector || {};
    } catch (e) {}
});
</script>
</body>
</html>
""";

        webView.getEngine().getLoadWorker().exceptionProperty().addListener(
                (obs, oldExc, newExc) -> {
                    if (newExc != null) {
                        System.err.println("WebView Error: " + newExc.getMessage());
                        showError("Failed to load map. Please check your internet connection.");
                    }
                });

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webView.getEngine().executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                    System.out.println("JavaScript bridge established");
                } catch (Exception e) {
                    System.err.println("Bridge error: " + e.getMessage());
                    showError("Failed to initialize map features.");
                }
            }
        });

        webView.getEngine().loadContent(htmlContent);

        locationDescription = new Label();
        locationDescription.setFont(Font.font(12));
        locationDescription.setStyle("-fx-text-fill: #555;");
        locationDescription.setWrapText(true);
        locationDescription.setPadding(new Insets(5, 0, 5, 0));

        mediaView = new MediaView();
        mediaView.setFitWidth(300);
        mediaView.setFitHeight(180);
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        mediaView.setVisible(false);

        imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setVisible(false);

        favoriteButton = new Button("★ Add to Favorites");
        favoriteButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50; -fx-font-size: 12px;");
        favoriteButton.setOnAction(e -> toggleFavorite());

        playPauseButton = new Button("▶");
        playPauseButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        playPauseButton.setOnAction(e -> togglePlayPause());

        mediaSlider = new Slider();
        mediaSlider.setMin(0);
        mediaSlider.setMax(100);
        mediaSlider.setValue(0);
        mediaSlider.setDisable(true);

        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-font-size: 10px;");

        mediaControls = new HBox(5, playPauseButton, mediaSlider, timeLabel);
        mediaControls.setAlignment(Pos.CENTER);
        mediaControls.setPadding(new Insets(5));
        mediaControls.setVisible(false);

        mediaTypeSelector = new HBox(5);
        mediaTypeSelector.setAlignment(Pos.CENTER);
        mediaTypeSelector.setPadding(new Insets(5));
        mediaToggleGroup = new ToggleGroup();

        RadioButton videoButton = new RadioButton("Video");
        videoButton.setToggleGroup(mediaToggleGroup);
        videoButton.setSelected(true);
        videoButton.setUserData("video");
        videoButton.setStyle("-fx-font-size: 11px;");
        videoButton.setOnAction(e -> switchMediaType("video"));

        RadioButton imageButton = new RadioButton("Image");
        imageButton.setToggleGroup(mediaToggleGroup);
        imageButton.setUserData("image");
        imageButton.setStyle("-fx-font-size: 11px;");
        imageButton.setOnAction(e -> switchMediaType("image"));

        mediaTypeSelector.getChildren().addAll(videoButton, imageButton);
        mediaTypeSelector.setVisible(false);

        setupQuizSection();

        VBox rightPane = new VBox(5);
        rightPane.setPadding(new Insets(8));
        rightPane.setPrefWidth(350);
        rightPane.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-border-color: #ddd;");

        StackPane mediaContainer = new StackPane();
        mediaContainer.getChildren().addAll(mediaView, imageView);
        mediaContainer.setStyle("-fx-background-color: #000; -fx-padding: 2;");

        HBox topButtonBox = new HBox(5, favoriteButton, backButton);
        topButtonBox.setAlignment(Pos.CENTER_LEFT);

        rightPane.getChildren().addAll(locationDescription, topButtonBox, mediaTypeSelector,
                mediaContainer, mediaControls, quizBox);

        HBox contentBox = new HBox(5);
        contentBox.setPadding(new Insets(5));
        contentBox.getChildren().addAll(webView, rightPane);
        HBox.setHgrow(webView, Priority.ALWAYS);

        VBox mainContent = new VBox(5, locationTitle, contentBox);
        mainContent.setPadding(new Insets(5));
        root.setCenter(mainContent);
    }

    private void returnToMapView() {
        currentLocationId = null;
        locationTitle.setText("Select a location on the map");
        locationDescription.setText("");

        mediaTypeSelector.setVisible(true);
        mediaView.setVisible(false);
        imageView.setVisible(false);
        quizBox.setVisible(true);
        backButton.setVisible(false);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        mediaControls.setVisible(false);
        mediaToggleGroup.selectToggle(mediaToggleGroup.getToggles().get(0));
        resultLabel.setText("");
    }

    private void initializeQuizzes() {
        // Maseru quizzes
        List<QuizQuestion> maseruQuizzes = new ArrayList<>();
        maseruQuizzes.add(new QuizQuestion("What is Maseru's elevation above sea level?",
                "1,000 meters", "1,600 meters", "2,000 meters", 2));
        maseruQuizzes.add(new QuizQuestion("When was Maseru established as the capital?",
                "1824", "1869", "1902", 2));
        maseruQuizzes.add(new QuizQuestion("What does 'Maseru' mean in Sesotho?",
                "Place of mountains", "Place of red sandstone", "Place of kings", 2));
        locationQuizzes.put("Maseru", maseruQuizzes);

        // Thaba-Bosiu quizzes
        List<QuizQuestion> thabaQuizzes = new ArrayList<>();
        thabaQuizzes.add(new QuizQuestion("What is the significance of Thaba Bosiu?",
                "It is a shopping center",
                "It is the birthplace of Moshoeshoe I",
                "It is the historical mountain fortress of the Basotho",
                3));
        thabaQuizzes.add(new QuizQuestion("What does 'Thaba Bosiu' mean?",
                "Mountain of gold", "Mountain at night", "Mountain of kings", 2));
        thabaQuizzes.add(new QuizQuestion("Who is buried at Thaba Bosiu?",
                "King Moshoeshoe I", "King Letsie III", "Chief Jonathan", 1));
        locationQuizzes.put("Thaba-Bosiu", thabaQuizzes);

        // Maletsunyane quizzes
        List<QuizQuestion> maletsunyaneQuizzes = new ArrayList<>();
        maletsunyaneQuizzes.add(new QuizQuestion("What makes Maletsunyane Falls famous?",
                "It is the tallest waterfall in southern Africa",
                "It is a volcano",
                "It is Lesotho's largest lake",
                1));
        maletsunyaneQuizzes.add(new QuizQuestion("How high is Maletsunyane Falls?",
                "95 meters", "192 meters", "250 meters", 2));
        maletsunyaneQuizzes.add(new QuizQuestion("What activity is popular at the falls?",
                "Skiing", "Abseiling", "Surfing", 2));
        locationQuizzes.put("Maletsunyane", maletsunyaneQuizzes);

        // Pioneer Mall quizzes
        List<QuizQuestion> mallQuizzes = new ArrayList<>();
        mallQuizzes.add(new QuizQuestion("When was Pioneer Mall opened?",
                "2005", "2010", "2015", 2));
        mallQuizzes.add(new QuizQuestion("What was on the mall site before construction?",
                "A stadium", "A farm", "A government building", 1));
        mallQuizzes.add(new QuizQuestion("How many stores does the mall have?",
                "About 40", "Over 70", "More than 100", 2));
        locationQuizzes.put("Pioneer-Mall", mallQuizzes);

        // Sani Pass quizzes
        List<QuizQuestion> saniQuizzes = new ArrayList<>();
        saniQuizzes.add(new QuizQuestion("What is unique about Sani Pass?",
                "Highest pub in Africa",
                "Steepest road in Africa",
                "Both of these",
                3));
        saniQuizzes.add(new QuizQuestion("What type of vehicle is advised for Sani Pass?",
                "Motorcycle", "4x4 vehicle", "Any vehicle", 2));
        saniQuizzes.add(new QuizQuestion("How high is Sani Pass?",
                "1,876m", "2,876m", "3,876m", 2));
        locationQuizzes.put("Sani-Pass", saniQuizzes);

        // Katse Dam quizzes
        List<QuizQuestion> katseQuizzes = new ArrayList<>();
        katseQuizzes.add(new QuizQuestion("What is the primary purpose of Katse Dam?",
                "Hydroelectric power",
                "Water supply to South Africa",
                "Tourist attraction",
                2));
        katseQuizzes.add(new QuizQuestion("How tall is Katse Dam?",
                "125m", "185m", "225m", 2));
        katseQuizzes.add(new QuizQuestion("Which country receives water from Katse Dam?",
                "Botswana", "South Africa", "Zimbabwe", 2));
        locationQuizzes.put("Katse-Dam", katseQuizzes);
    }

    private void toggleFavorite() {
        if (currentLocationId != null) {
            boolean isFavorite = !favoriteLocations.getOrDefault(currentLocationId, false);
            favoriteLocations.put(currentLocationId, isFavorite);
            favoriteButton.setText(isFavorite ? "★ Remove Favorite" : "★ Add to Favorites");
            favoriteButton.setStyle(isFavorite ?
                    "-fx-background-color: #e74c3c; -fx-text-fill: white;" :
                    "-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50;");
        }
    }

    private void switchMediaType(String type) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        mediaView.setVisible(type.equals("video"));
        imageView.setVisible(type.equals("image"));
        mediaControls.setVisible(type.equals("video"));
        loadLocationMedia(currentLocationId, type);
    }

    private void loadLocationMedia(String locationId, String mediaType) {
        playMedia(locationId, mediaType);
    }

    private void playMedia(String locationId, String mediaType) {
        if (locationId == null) return;

        try {
            String mediaPath = "";
            switch(locationId) {
                case "Maseru":
                    if (mediaType.equals("video")) {
                        mediaPath = "/com/example/lestour/media/msu.mp4";
                    } else {
                        mediaPath = "/com/example/lestour/media/maseru.jpg";
                    }
                    break;

                case "Thaba-Bosiu":
                    if (mediaType.equals("video")) {
                        mediaPath = "/com/example/lestour/media/TB.mp4";
                    } else {
                        mediaPath = "/com/example/lestour/media/thaba.jpg";
                    }
                    break;

                case "Maletsunyane":
                    if (mediaType.equals("video")) {
                        mediaPath = "/com/example/lestour/media/Mfall.mp4";
                    } else {
                        mediaPath = "/com/example/lestour/media/falls.jpg";
                    }
                    break;

                case "Pioneer-Mall":
                    if (mediaType.equals("video")) {
                        mediaPath = "/com/example/lestour/media/pioneer.mp4";
                    } else {
                        mediaPath = "/com/example/lestour/media/pioneer.jpg";
                    }
                    break;

                case "Sani-Pass":
                    if (mediaType.equals("video")) {
                        mediaPath = "/com/example/lestour/media/Spass.mp4";
                    } else {
                        mediaPath = "/com/example/lestour/media/pass.jpg";
                    }
                    break;

                case "Katse-Dam":
                    if (mediaType.equals("video")) {
                        mediaPath = "/com/example/lestour/media/dam.mp4";
                    } else {
                        mediaPath = "/com/example/lestour/media/katse.jpg";
                    }
                    break;
            }

            if (!mediaPath.isEmpty()) {
                if (mediaType.equals("video")) {
                    Media media = new Media(getClass().getResource(mediaPath).toExternalForm());
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);

                    // Setup media controls
                    mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        if (!mediaSlider.isValueChanging()) {
                            mediaSlider.setValue(newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
                        }
                        updateTimeLabel(newTime.toSeconds(), mediaPlayer.getTotalDuration().toSeconds());
                    });

                    mediaSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (mediaSlider.isValueChanging()) {
                            mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newVal.doubleValue() / 100));
                        }
                    });

                    mediaPlayer.setOnReady(() -> {
                        mediaSlider.setDisable(false);
                        mediaSlider.setMax(100);
                        mediaView.setVisible(true);
                        mediaControls.setVisible(true);
                    });

                    mediaPlayer.setOnPlaying(() -> playPauseButton.setText("❚❚"));
                    mediaPlayer.setOnPaused(() -> playPauseButton.setText("▶"));

                    mediaPlayer.play();
                } else {
                    Image image = new Image(getClass().getResourceAsStream(mediaPath));
                    imageView.setImage(image);
                    imageView.setVisible(true);
                }
            }
        } catch (Exception e) {
            showError("Media file not found for " + locationId + ". Please ensure all media files are in the correct location.");
            System.err.println("Error loading media: " + e.getMessage());
        }
    }

    private void setupQuizSection() {
        quizLabel = new Label();
        quizLabel.setStyle("-fx-font-size: 12px; -fx-wrap-text: true;");
        quizLabel.setMaxWidth(Double.MAX_VALUE);

        RadioButton option1 = new RadioButton();
        RadioButton option2 = new RadioButton();
        RadioButton option3 = new RadioButton();
        option1.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");
        option2.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");
        option3.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");

        ToggleGroup group = new ToggleGroup();
        option1.setToggleGroup(group);
        option2.setToggleGroup(group);
        option3.setToggleGroup(group);

        Button submitAnswer = new Button("Submit Answer");
        submitAnswer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");

        Button nextQuestionButton = new Button("Next Question");
        nextQuestionButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px;");
        nextQuestionButton.setOnAction(e -> showNextQuizQuestion());

        resultLabel = new Label();
        resultLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        submitAnswer.setOnAction(e -> checkAnswer(group, submitAnswer));

        // Create an HBox to hold the buttons side by side
        HBox buttonBox = new HBox(5, submitAnswer, nextQuestionButton);
        buttonBox.setAlignment(Pos.CENTER);

        quizBox = new VBox(5, quizLabel, option1, option2, option3, buttonBox, resultLabel);
        quizBox.setVisible(false);
        quizBox.setPadding(new Insets(8));
        quizBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-radius: 8; -fx-border-color: #ddd;");
        quizLabel.setUserData(new RadioButton[]{option1, option2, option3});
    }

    private void showNextQuizQuestion() {
        if (currentLocationId == null || !locationQuizzes.containsKey(currentLocationId)) {
            return;
        }

        List<QuizQuestion> questions = locationQuizzes.get(currentLocationId);
        if (questions.isEmpty()) {
            return;
        }

        QuizQuestion question = questions.get(random.nextInt(questions.size()));
        showQuiz(question.question, question.options[0], question.options[1], question.options[2], question.correctAnswer);
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("▶");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("❚❚");
            }
        }
    }

    private void updateTimeLabel(double currentTime, double totalTime) {
        int currentMinutes = (int) (currentTime / 60);
        int currentSeconds = (int) (currentTime % 60);
        int totalMinutes = (int) (totalTime / 60);
        int totalSeconds = (int) (totalTime % 60);
        timeLabel.setText(String.format("%02d:%02d / %02d:%02d",
                currentMinutes, currentSeconds, totalMinutes, totalSeconds));
    }

    private void showQuiz(String question, String opt1, String opt2, String opt3, int correctAnswer) {
        quizLabel.setText(question);
        RadioButton[] options = (RadioButton[]) quizLabel.getUserData();
        options[0].setText(opt1);
        options[1].setText(opt2);
        options[2].setText(opt3);
        options[0].setSelected(false);
        options[1].setSelected(false);
        options[2].setSelected(false);
        quizBox.setVisible(true);

        // Correct way to set the correct answer in the submit button
        HBox buttonBox = (HBox) quizBox.getChildren().get(4);
        Button submitButton = (Button) buttonBox.getChildren().get(0);
        submitButton.setUserData(correctAnswer);

        resultLabel.setText("");
    }

    private void checkAnswer(ToggleGroup group, Button submitButton) {
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        if (selected != null) {
            RadioButton[] options = (RadioButton[]) quizLabel.getUserData();
            int selectedIndex = selected == options[0] ? 1 :
                    selected == options[1] ? 2 : 3;
            int correctIndex = (int) submitButton.getUserData();

            if (selectedIndex == correctIndex) {
                resultLabel.setText("Correct! ✔ +10 points");
                resultLabel.setTextFill(Color.GREEN);
                userScore += 10;
                updateScore();

                // Mark location as completed
                if (!completedLocations.containsKey(currentLocationId)) {
                    completedLocations.put(currentLocationId, true);
                    updateProgress();
                }
            } else {
                resultLabel.setText("Incorrect. The correct answer was: " + options[correctIndex-1].getText());
                resultLabel.setTextFill(Color.RED);
            }
        } else {
            resultLabel.setText("Please select an answer!");
            resultLabel.setTextFill(Color.ORANGE);
        }
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + userScore);
    }

    private void updateProgress() {
        double progress = (double) completedLocations.size() / 6;
        progressBar.setProgress(progress);
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Lesotho Virtual Tour Guide Help");
        alert.setContentText("1. Click on map markers to select locations\n" +
                "2. Use the media selector to switch between video and images\n" +
                "3. Answer quizzes to earn points\n" +
                "4. Mark locations as favorites\n" +
                "5. Track your progress with the progress bar\n" +
                "6. Use play/pause button to control video playback\n" +
                "7. Drag slider to seek through videos\n" +
                "8. Click 'Next Question' for a new quiz question\n" +
                "9. Click 'Back to Map' to return to the map view");
        alert.showAndWait();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}