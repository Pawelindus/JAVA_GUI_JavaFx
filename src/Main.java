import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {

    private static final MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File("src/sounds/music.mp3").toURI().toString()));
    private static final MediaPlayer fxPlayer = new MediaPlayer(new Media(new File("src/sounds/eat.mp3").toURI().toString()));
    private static final MediaPlayer explodePlayer = new MediaPlayer(new Media(new File("src/sounds/boom.mp3").toURI().toString()));
    private static final MediaPlayer barkPlayer = new MediaPlayer(new Media(new File("src/sounds/bark.mp3").toURI().toString()));
    private final Alert dialogPause = new Alert(Alert.AlertType.INFORMATION);
    private final Alert dialogGameOver = new Alert(Alert.AlertType.WARNING);
    private final TextInputDialog dialogScoreInput = new TextInputDialog();
    private final String jamnik_tail_IMG = "imgs/jamnik_tail_LONG.png";
    private final String jamnik_body_IMG = "imgs/jamnik_body_LONG.png";
    private final String jamnik_head_IMG = "imgs/jamnik_head_LONG.png";
    private final int COL_COUNT = 32;
    private final int ROW_COUNT = 32;
    private final int SCENE_WIDTH = 820;
    private final int SCENE_HEIGHT = 820;
    private final double GAME_SPEED = 0.2;
    private final FileChooser fileChooser = new FileChooser();
    private final Tile[][] board = new Tile[COL_COUNT][ROW_COUNT];
    private final MenuBar menuBarG = new MenuBar();
    private final MenuBar menuBarM = new MenuBar();
    private final MenuBar menuBarS = new MenuBar();
    private final VBox vBoxMenu = new VBox();
    private final VBox vBoxGame = new VBox();
    private final VBox vBoxScore = new VBox();
    private final GridPane root = new GridPane();
    private final Bone bone = new Bone();
    private final Jamnik[] jamniks = new Jamnik[1024];
    private final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Jamnik save file (*.jsav)", "*.jsav");
    private final ListView<String> listView = new ListView<>();
    private final AtomicReference<Direction> direction = new AtomicReference<>(null);
    private final int[] oldJamnikPosI = new int[jamniks.length];
    private final int[] oldJamnikPosJ = new int[jamniks.length];
    private final double[] oldJamnikRotation = new double[jamniks.length];
    private final Menu menuG_File = new Menu("File");
    private final Menu menuM_File = new Menu("File");
    private final Menu menuGame = new Menu("Game");
    private final Menu menuTemp = new Menu("");
    private final MenuItem saveItem = new MenuItem("Save game");
    private final MenuItem loadG_Item = new MenuItem("Load game");
    private final MenuItem loadM_Item = new MenuItem("Load game");
    private final MenuItem backToMenu_Item = new MenuItem("Main menu");
    private final MenuItem muteMusicItem = new MenuItem("Mute music");
    private final MenuItem muteFxItem = new MenuItem("Mute FX");
    private final Timeline musicTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> playMusic()));
    private final Timeline fxTimeline = new Timeline(new KeyFrame(Duration.seconds(0.01), event -> playFx()));
    private final Timeline explodeTimeline = new Timeline(new KeyFrame(Duration.seconds(0.01), event -> playExplodePlayer()));
    private int currentGameScore = 0;
    private final Menu menuScore = new Menu("Score: " + currentGameScore);
    private String playerName;
    private boolean isGamePaused = false;
    private File fileScore = new File("HighScore.score");
    private Scene sceneGame;
    private Scene sceneMenu;
    private Scene sceneScore;
    private Button startButton;
    private Button infoButton;
    private Button quitButton;
    private Button nextButton;
    private int oldBoneI = 999;
    private int oldBoneJ = 999;

    Timeline timer = new Timeline(
            new KeyFrame(Duration.seconds(GAME_SPEED),
                    event -> {
                        movement();
                        if (!checkIfBoneOnBoard()) {
                            spawnBone();
                        }
                        eatBone();
                        setJamnikTaiImg();
                        isCrashed();
                        isDead();
                        barkRandom();
                    }
            ));

    public static void main(String[] args) {
        launch(args);
    }

    private Parent createGame() {
        vBoxGame.getChildren().add(menuBarG);
        vBoxGame.getChildren().add(root);


        root.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                Tile tile;
                if (i == 0 || i == 31 || j == 0 || j == 31) {
                    tile = new Tile(false);
                } else {
                    tile = new Tile(true);
                }

                root.add(tile, i, j);
                board[i][j] = tile;
            }
        }
        root.getColumnConstraints().removeAll();
        for (int col = 0; col < COL_COUNT; ++col) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100);
            root.getColumnConstraints().add(columnConstraints);
        }

        for (int row = 0; row < ROW_COUNT; ++row) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100);
            root.getRowConstraints().add(rowConstraints);
        }
        return vBoxGame;
    }

    private Parent createMenu() {
        vBoxMenu.getChildren().add(menuBarM);
        GridPane pane = new GridPane();
        BorderPane pane1 = new BorderPane();
        vBoxMenu.getChildren().add(pane1);
        pane1.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        Image image = new Image("imgs/jamnikGIF_LOGO.gif", 800, 245, false, false);
        ImageView imageView = new ImageView(image);
        startButton = new Button("Start");
        infoButton = new Button("Game rules");
        quitButton = new Button("Exit");
        pane.setAlignment(Pos.CENTER);
        pane1.setBackground(new Background(new BackgroundImage(new Image("imgs/backImage.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT)));
        startButton.setPrefSize(300, 150);
        infoButton.setPrefSize(300, 150);
        quitButton.setPrefSize(300, 150);
        pane.setVgap(15);
        pane.add(startButton, 0, 0);
        pane.add(infoButton, 0, 1);
        pane.add(quitButton, 0, 2);
        BorderPane.setMargin(imageView, new Insets(0, 0, 0, 25));
        pane1.setTop(imageView);
        pane1.setCenter(pane);

        return vBoxMenu;
    }

    private Parent createScore() {
        vBoxScore.getChildren().add(menuBarS);
        BorderPane pane = new BorderPane();
        vBoxScore.getChildren().add(pane);
        pane.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        nextButton = new Button("Next");
        pane.setBackground(new Background(new BackgroundImage(new Image("imgs/backImage.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT)));
        nextButton.setPrefSize(400, 100);
        Text text = new Text("High Score");
        text.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 40));
        pane.setTop(text);
        pane.setCenter(listView);
        pane.setBottom(nextButton);
        BorderPane.setMargin(listView, new Insets(12, 12, 12, 12));
        BorderPane.setMargin(nextButton, new Insets(0, 0, 0, 12));
        BorderPane.setAlignment(nextButton, Pos.CENTER);
        BorderPane.setAlignment(text, Pos.CENTER);
        try {
            int input;
            StringBuilder s = new StringBuilder();
            if (fileScore.exists()) {
                FileReader fileReader = new FileReader(fileScore);
                while ((input = fileReader.read()) != -1) {
                    if (input != '\n') {
                        s.append((char) input);
                    }
                    if (input == '\n') {
                        listView.getItems().add(s.toString());
                        s.delete(0, s.length());
                    }
                }
                fileReader.close();
            } else {
                fileScore = new File("HighScore.score");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vBoxScore;
    }

    @Override
    public void start(Stage primaryStage) {
        fileChooser.getExtensionFilters().add(extFilter);
        vBoxGame.getStylesheets().add("styles.css");
        vBoxMenu.getStylesheets().add("styles.css");
        vBoxScore.getStylesheets().add("styles.css");
        menuBarG.getMenus().add(menuG_File);
        menuBarG.getMenus().add(menuGame);
        menuBarG.getMenus().add(menuScore);
        menuBarM.getMenus().add(menuM_File);
        menuBarS.getMenus().add(menuTemp);
        menuG_File.getItems().addAll(backToMenu_Item, saveItem, loadG_Item);
        menuM_File.getItems().addAll(loadM_Item);
        menuGame.getItems().addAll(muteMusicItem);
        menuGame.getItems().addAll(muteFxItem);

        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        loadG_Item.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        loadM_Item.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        muteMusicItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
        muteFxItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN));

        sceneMenu = new Scene(createMenu(), SCENE_WIDTH, SCENE_HEIGHT + 20);
        sceneGame = new Scene(createGame(), SCENE_WIDTH, SCENE_HEIGHT + 20);
        sceneScore = new Scene(createScore(), SCENE_WIDTH, SCENE_HEIGHT + 20);
        primaryStage.setTitle("Jamnik - The GAME");
        primaryStage.setScene(sceneMenu);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("imgs/jamnik_head.png"));
        primaryStage.show();
        primaryStage.setOnCloseRequest(windowEvent -> System.exit(0));

        sceneEventHandlers(primaryStage);

        saveAction(primaryStage);
        loadAction(primaryStage);
        mainMenuAction(primaryStage);
        muteAction();
    }

    //METHODS =================

    private void saveAction(Stage primaryStage) {
        saveItem.setOnAction(event -> {
            timer.stop();
            File option = fileChooser.showSaveDialog(primaryStage);
            if (option != null) {
                String temp = option.getPath();
                String filename;
                if (option.getName().endsWith(".jsav")) {
                    filename = temp;
                } else {
                    filename = (temp + ".jsav");
                }
                try(PrintWriter printWriter = new PrintWriter(new FileWriter(filename))) {
                    for (int i = 0; i < board.length; i++) {
                        for (int j = 0; j < board.length; j++) {
                            for (int k = 0; k < howLongIsJamnik(); k++) {
                                if (board[i][j].getChildren().contains(jamniks[k])) {
                                    printWriter.println(i + " " + j + " " + "jamnik" + " " + k + " ");
                                    break;
                                }
                            }
                            if (board[i][j].getChildren().contains(bone)) {
                                printWriter.println(i + " " + j + " " + "bone" + " ");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            timer.play();
        });
    }

    private void loadAction(Stage primaryStage) {
        Alert alert = showPauseDialog();
        EventHandler<ActionEvent> event = actionEvent -> {
            timer.stop();
            File option = fileChooser.showOpenDialog(primaryStage);
            if (option != null) {
                if (option.canRead()) {
                    if (option.getPath().endsWith(".jsav")) {

                        if (primaryStage.getScene() != sceneGame) {
                            changeScene(primaryStage, sceneGame);
                        }
                        musicTimeline.stop();
                        Arrays.fill(jamniks, null);
                        direction.set(null);
                        for (int i = 0; i < board.length; i++) {
                            for (int j = 0; j < board.length; j++) {
                                if (i == 0 || i == 31 || j == 0 || j == 31) {
                                    board[i][j].getChildren().clear();
                                    board[i][j].deathTile();
                                } else {
                                    board[i][j].getChildren().clear();
                                    board[i][j].normalTile();
                                }
                            }
                        }
                        readingFile(option);
                        currentGameScore = howLongIsJamnik();
                        menuScore.setText("Score: " + currentGameScore);
                        timer.setCycleCount(Timeline.INDEFINITE);
                        musicTimeline.play();
                        pauseGame(timer);
                        isGamePaused = false;
                        alert.show();
                    } else {
                        customisableDialog("Błąd rozszerzenia", "Wybrany plik nie jest poprawnym plikiem zapisu .jsav");
                    }
                } else {
                    customisableDialog("Błąd odczytu", "Nie można odczytac pliku");
                }
            } else {
                if (primaryStage.getScene() == sceneGame) {
                    timer.play();
                }
            }
        };

        loadG_Item.setOnAction(event);
        loadM_Item.setOnAction(event);
    }

    private void mainMenuAction(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Back to main menu");
        alert.setHeaderText("If you return to the menu, you will abandon current game.");
        backToMenu_Item.setOnAction(event -> alert.showAndWait().ifPresent(option -> {
            if (option.equals(ButtonType.YES)) {
                changeScene(primaryStage, sceneMenu);
                timer.stop();
                mediaPlayer.stop();
            }
        }));
    }

    private void readingFile(File option) {
        int i;
        int posI = 0;
        int posJ = 0;
        int spaceN = 0;
        boolean isJamnik = false;
        String text = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(option.getPath()) {
            });
            while ((i = reader.read()) != -1) {
                if (spaceN == 0 && i != ' ') {
                    text += (char) i;
                }
                if (i == ' ' && spaceN == 0) {
                    posI = Integer.parseInt(text);
                }
                if (spaceN == 1 && i != ' ') {
                    text += (char) i;
                }
                if (i == ' ' && spaceN == 1) {
                    posJ = Integer.parseInt(text);
                }
                if (spaceN == 2 && i != ' ') {
                    text += (char) i;
                }
                if (i == ' ' && spaceN == 2) {
                    if (text.equals("bone")) {
                        board[posI][posJ].getChildren().add(bone);
                        spaceN = 0;
                        text = "";
                    }
                }
                if (i == ' ' && spaceN == 2) {
                    if (text.equals("jamnik")) {
                        isJamnik = true;
                    }
                }
                if (spaceN == 3 && i != ' ') {
                    text += (char) i;
                }
                if (i == ' ' && spaceN == 3 && isJamnik) {
                    if (jamniks[Integer.parseInt(text)] == null) {
                        if (Integer.parseInt(text) == 0) {
                            jamniks[Integer.parseInt(text)] = new Jamnik(new Image(jamnik_head_IMG), 0);
                        } else {
                            jamniks[Integer.parseInt(text)] = new Jamnik(new Image(jamnik_body_IMG), 0);
                        }
                        jamniks[Integer.parseInt(text)].setJamnikI(posI);
                        jamniks[Integer.parseInt(text)].setJamnikJ(posJ);
                    }
                    board[posI][posJ].getChildren().add(jamniks[Integer.parseInt(text)]);
                }
                if (i == ' ') {
                    spaceN++;
                    text = "";
                }
                if (i == '\n') {
                    spaceN = 0;
                    text = "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void muteAction() {
        muteMusicItem.setOnAction(event -> {
            if (mediaPlayer.getVolume() != 0.0) {
                mediaPlayer.setVolume(0.0);
                muteMusicItem.setText("UnMute Music");
            } else {
                mediaPlayer.setVolume(1.0);
                muteMusicItem.setText("Mute Music");
            }
        });

        muteFxItem.setOnAction(event -> {
            if (fxPlayer.getVolume() != 0.0) {
                fxPlayer.setVolume(0.0);
                explodePlayer.setVolume(0.0);
                muteFxItem.setText("UnMute FX");
            } else {
                fxPlayer.setVolume(1.0);
                explodePlayer.setVolume(1.0);
                muteFxItem.setText("Mute FX");
            }
        });
    }

    private void sceneEventHandlers(Stage primaryStage) {
        startButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            for (Tile[] tiles : board) {
                for (int j = 0; j < board.length; j++) {
                    tiles[j].getChildren().removeAll(jamniks);
                }
            }
            currentGameScore = 0;
            explodePlayer.stop();
            menuScore.setText("Score: " + currentGameScore);
            direction.set(null);
            changeScene(primaryStage, sceneGame);
            timer.setCycleCount(Timeline.INDEFINITE);
            Arrays.fill(jamniks, null);
            timer.play();
            musicTimeline.play();
            pauseGame(timer);
            isGamePaused = false;
            jamniks[0] = new Jamnik(new Image(jamnik_head_IMG), 0);
            Random random = new Random();
            int i = random.nextInt(10) + 10;
            int j = random.nextInt(10) + 10;
            jamniks[0].setJamnikI(i);
            jamniks[0].setJamnikJ(j);
            board[i][j].getChildren().add(jamniks[0]);
        });

        quitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> System.exit(0));

        dialogGameOver.setOnHiding(event -> {
            dialogGameOver.close();
            changeScene(primaryStage, sceneScore);
            showScoreDialog().show();
        });

        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> changeScene(primaryStage, sceneMenu));

        dialogScoreInput.setOnHiding(dialogEvent -> {
            playerName = dialogScoreInput.getEditor().getText();
            if (!playerName.equals("")) {
                listView.getItems().add(currentGameScore + "\t" + playerName);
            }
            listView.getItems().sort((o1, o2) -> {
                StringBuilder builder1 = new StringBuilder();
                StringBuilder builder2 = new StringBuilder();
                int i = 0;
                while (o1.charAt(i) != '\t') {
                    builder1.append(o1.charAt(i));
                    i++;
                }
                i = 0;
                while (o2.charAt(i) != '\t') {
                    builder2.append(o2.charAt(i));
                    i++;
                }
                int i1 = Integer.parseInt(builder1.toString());
                int i2 = Integer.parseInt(builder2.toString());
                return (i2 - i1);
            });
            try {
                FileWriter fileWriter = new FileWriter(fileScore);
                for (int i = 0; i < listView.getItems().size(); i++) {
                    fileWriter.write(listView.getItems().get(i) + '\n');
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        infoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            GameRulesDialog dialog = new GameRulesDialog();
            dialog.show();
        });

    }

    private void changeScene(Stage stage, Scene newscene) {
        stage.setScene(newscene);
    }

    private Direction playerInputOnSake() {
        if (!isGamePaused) {
            sceneGame.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.UP && direction.get() != Direction.SOUTH) {
                    direction.set(Direction.NORTH);
                }
                if (keyEvent.getCode() == KeyCode.DOWN && direction.get() != Direction.NORTH) {
                    direction.set(Direction.SOUTH);
                }
                if (keyEvent.getCode() == KeyCode.LEFT && direction.get() != Direction.EAST) {
                    direction.set(Direction.WEST);
                }
                if (keyEvent.getCode() == KeyCode.RIGHT && direction.get() != Direction.WEST) {
                    direction.set(Direction.EAST);
                }

            });
        }
        return direction.get();
    }

    private void movement() {
        int column = jamniks[0].getJamnikI();
        int row = jamniks[0].getJamnikJ();
        Direction direction = playerInputOnSake();
        if (direction == Direction.NORTH) {
            if (row > 0) {
                moveJamnikOnBoard(0, -1, 180);
            }
        }
        if (direction == Direction.SOUTH) {
            if (row < 31) {
                moveJamnikOnBoard(0, 1, 0);
            }
        }
        if (direction == Direction.WEST) {
            if (column > 0) {
                moveJamnikOnBoard(-1, 0, 90);
            }
        }
        if (direction == Direction.EAST) {
            if (column < 31) {
                moveJamnikOnBoard(1, 0, 270);
            }
        }


    }

    private void moveJamnikOnBoard(int I, int J, double rotation) {
        for (int i = 0; i < howLongIsJamnik(); i++) {
            oldJamnikPosI[i] = jamniks[i].getJamnikI();
            oldJamnikPosJ[i] = jamniks[i].getJamnikJ();
            oldJamnikRotation[i] = jamniks[i].getJamnikRotation();
        }
        headMovedBreak:
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].getChildren().contains(jamniks[0])) {
                    board[i][j].getChildren().remove(jamniks[0]);
                    jamniks[0].setJamnikRotation(rotation);
                    jamniks[0].setRotate(rotation);
                    board[i + I][j + J].getChildren().add(jamniks[0]);
                    jamniks[0].setJamnikI(i + I);
                    jamniks[0].setJamnikJ(j + J);
                    break headMovedBreak;
                }

            }
        }
        for (int i = 1; i < howLongIsJamnik(); i++) {
            jamniks[i].setJamnikI(oldJamnikPosI[i - 1]);
            jamniks[i].setJamnikJ(oldJamnikPosJ[i - 1]);
            jamniks[i].setRotate(oldJamnikRotation[i - 1]);
            jamniks[i].setJamnikRotation(oldJamnikRotation[i - 1]);
            board[jamniks[i].getJamnikI()][jamniks[i].getJamnikJ()].getChildren().add(jamniks[i]);
        }
    }

    private Alert showPauseDialog() {
        dialogPause.setTitle("Pause");
        dialogPause.setHeaderText("Game is Paused");
        dialogPause.setContentText("Take your time and take a break");

        dialogPause.setOnCloseRequest(event -> {
            dialogPause.close();
            timer.play();
            mediaPlayer.play();
            isGamePaused = false;
        });

        return dialogPause;
    }

    private Alert showGameOverDialog() {
        dialogGameOver.setTitle("Game Over");
        dialogGameOver.setHeaderText("Game Over, your score is: " + currentGameScore);
        dialogGameOver.setContentText("Mission Failed, We'll get them next time");
        return dialogGameOver;
    }

    private TextInputDialog showScoreDialog() {
        dialogScoreInput.getEditor().setText("");
        dialogScoreInput.setTitle("Congratulations!");
        dialogScoreInput.setHeaderText("Input your name below");
        dialogScoreInput.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true);
        return dialogScoreInput;
    }

    private void customisableDialog(String title, String header) {
        final Alert dialogCustomisable = new Alert(Alert.AlertType.ERROR);

        dialogCustomisable.setTitle(title);
        dialogCustomisable.setHeaderText(header);

        dialogCustomisable.show();
    }

    private void pauseGame(Timeline timer) {
        Alert dialog = showPauseDialog();
        sceneGame.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SPACE) {
                if (!isGamePaused) {
                    timer.stop();
                    mediaPlayer.pause();
                    dialog.show();
                    isGamePaused = true;
                }
            }
        });
    }

    private int[] bonePOS() {
        int[] bonePOS = new int[2];
        mybreak:
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                {
                    if (board[i][j].getChildren().contains(bone)) {
                        bonePOS[0] = i;
                        bonePOS[1] = j;
                        break mybreak;
                    }
                }
            }
        }
        return bonePOS;
    }

    private void spawnBone() {
        Random random = new Random();
        int spawnI = random.nextInt(30) + 1;
        int spawnJ = random.nextInt(30) + 1;

        while (hasJamnik(spawnI, spawnJ)) {
            spawnI = random.nextInt(30) + 1;
            spawnJ = random.nextInt(30) + 1;
        }
        board[spawnI][spawnJ].getChildren().add(bone);
    }

    private boolean checkIfBoneOnBoard() {
        for (Tile[] tiles : board) {
            for (int j = 0; j < board.length; j++) {
                {
                    if (tiles[j].getChildren().contains(bone)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasJamnik(int i, int j) {
        for (int k = 0; k < howLongIsJamnik(); k++) {
            if (jamniks[k].getJamnikI() == i && jamniks[k].getJamnikJ() == j) {
                return true;
            }
        }
        return false;
    }

    private void eatBone() {
        if (jamniks[0].getJamnikI() == bonePOS()[0] && jamniks[0].getJamnikJ() == bonePOS()[1]) {
            oldBoneI = bonePOS()[0];
            oldBoneJ = bonePOS()[1];
            board[bonePOS()[0]][bonePOS()[1]].getChildren().remove(bone);
            fxTimeline.play();
            currentGameScore += 1;
            menuScore.setText("Score: " + currentGameScore);
        }
        if (jamniks[howLongIsJamnik() - 1].getJamnikI() == oldBoneI && jamniks[howLongIsJamnik() - 1].getJamnikJ() == oldBoneJ) {
            addTail();
            oldBoneI = 999;
            oldBoneJ = 999;
        }
    }

    private void addTail() {
        Jamnik jamnikTail = new Jamnik(new Image(jamnik_body_IMG), 0);
        setJamnikTaiImg();
        jamniks[howLongIsJamnik()] = jamnikTail;
    }

    private int howLongIsJamnik() {
        int sum = 0;
        for (int i = 0; i < 1024; i++) {
            if (jamniks[i] != null) {
                sum++;
            }
        }
        return sum;
    }

    private void isCrashed() {
        Alert dialog = showGameOverDialog();
        for (int i = 1; i < howLongIsJamnik(); i++) {
            if (jamniks[0].getJamnikI() == jamniks[i].getJamnikI() && jamniks[0].getJamnikJ() == jamniks[i].getJamnikJ()) {
                deathEvent(dialog);
            }
        }
    }

    private void isDead() {
        Alert dialog = showGameOverDialog();
        if (jamniks[0].getJamnikI() == 0 || jamniks[0].getJamnikI() == 31 || jamniks[0].getJamnikJ() == 0 || jamniks[0].getJamnikJ() == 31) {
            deathEvent(dialog);
        }
    }

    private void deathEvent(Alert dialog) {
        mediaPlayer.stop();
        timer.stop();
        for (int j = 0; j < howLongIsJamnik(); j++) {
            jamniks[j].setJamnikImg(new Image("imgs/boomGIF.gif"));
        }
        explodeTimeline.play();
        dialog.show();

    }

    private void setJamnikTaiImg() {
        if (howLongIsJamnik() > 1) {
            jamniks[howLongIsJamnik() - 1].setJamnikImg(new Image(jamnik_tail_IMG));
        }
        if (howLongIsJamnik() > 2) {
            jamniks[howLongIsJamnik() - 1].setJamnikImg(new Image(jamnik_tail_IMG));
            jamniks[howLongIsJamnik() - 2].setJamnikImg(new Image(jamnik_body_IMG));
        }
    }

    private void playMusic() {
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        });

    }

    private void playFx() {
        fxPlayer.play();
        fxPlayer.setOnEndOfMedia(fxPlayer::stop);

    }

    private void playExplodePlayer() {
        explodePlayer.play();
        explodePlayer.setOnEndOfMedia(explodePlayer::stop);

    }

    private void barkRandom() {
        int bark = (int) (Math.random() * 1000);
        if (bark <= 1) {
            barkPlayer.play();
            barkPlayer.setOnEndOfMedia(barkPlayer::stop);
        }

    }

}
