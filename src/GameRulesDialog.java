import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class GameRulesDialog extends Dialog<Node> {

    public GameRulesDialog() {
        DialogPane dialogPane = new DialogPane();
        this.setDialogPane(dialogPane);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        dialogPane.setContent(gridPane);
        gridPane.getStylesheets().add("styles.css");
        Button button = new Button("Close this window");
        Text text1 = new Text("""
                                                                                                    In the game,
                the player controls a long and thin dachshund-like creature that moves around the framed board collecting bones,
                                                  trying not to bang his head against the walls surrounding the board,
                                                                              as well as against part of his own body.""");
        Text text2 = new Text("As the dachshund eats a bone, its body becomes longer and longer, making it difficult to play.");
        Text text3 = new Text("The player controls the direction of movement of the snake with the arrow keys (up, down, left, right).");
        Text text4 = new Text("The player cannot stop the snake during the game.");
        text1.setStyle("-fx-font-size: 15pt;");
        text2.setStyle("-fx-font-size: 15pt;");
        text3.setStyle("-fx-font-size: 15pt;");
        text4.setStyle("-fx-font-size: 15pt;");
        ImageView imageView1 = new ImageView(new Image("imgs/jamnik_head.png", 64, 64, false, false));
        ImageView imageView2 = new ImageView(new Image("imgs/bone.png", 64, 64, false, false));
        ImageView imageView3 = new ImageView(new Image("imgs/arrowKeysGIF.gif"));
        gridPane.add(text1, 0, 0);
        gridPane.add(text2, 0, 2);
        gridPane.add(text3, 0, 4);
        gridPane.add(text4, 0, 6);
        gridPane.add(imageView1, 0, 1);
        gridPane.add(imageView2, 0, 3);
        gridPane.add(imageView3, 0, 5);
        gridPane.add(button, 0, 7);
        gridPane.setVgap(15);
        dialogPane.setBackground(new Background(new BackgroundFill(Color.GRAY, null, null)));
        for (Node node : gridPane.getChildren()) {
            GridPane.setHalignment(node, HPos.CENTER);
            GridPane.setValignment(node, VPos.CENTER);
        }
        this.setTitle("Jamnik - The GAME");

        button.setOnAction(event -> this.getDialogPane().getScene().getWindow().hide());
        this.getDialogPane().getScene().getWindow().setOnCloseRequest(windowEvent -> GameRulesDialog.super.close());
    }
}
