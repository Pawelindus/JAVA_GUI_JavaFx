import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

class Tile extends StackPane {
    private Color color = null;

    public Tile(boolean isNormal) {
        if (isNormal) {
            normalTile();
        } else {
            deathTile();
        }


    }

    public void normalTile() {
        Region border = new Region();
        color = Color.FORESTGREEN;
        border.setBackground(new Background(new BackgroundFill(color, null, null)));
        border.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0.1))));
        setAlignment(Pos.CENTER);
        getChildren().add(0, border);
    }

    public void deathTile() {
        Region border = new Region();
        color = Color.GRAY;
        border.setBackground(new Background(new BackgroundFill(color, null, null)));
        border.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0.1))));
        setAlignment(Pos.CENTER);
        getChildren().add(0, border);
    }
}