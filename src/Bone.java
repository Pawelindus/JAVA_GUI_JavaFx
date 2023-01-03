import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

class Bone extends StackPane {
    public Bone() {
        ImageView imageView = new ImageView(new Image("imgs/bone.png"));
        getChildren().add(imageView);

    }

}