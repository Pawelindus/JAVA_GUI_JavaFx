import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class Jamnik extends StackPane {
    private final ImageView imageView;
    private int jamnikI;
    private int jamnikJ;
    private double jamnikRotation;

    public Jamnik(Image img, double jamnikRotation) {
        imageView = new ImageView(img);
        this.jamnikRotation = jamnikRotation;
        imageView.setRotate(jamnikRotation);
        getChildren().add(imageView);
    }

    public int getJamnikI() {
        return jamnikI;
    }

    public void setJamnikI(int jamnikI) {
        this.jamnikI = jamnikI;
    }

    public int getJamnikJ() {
        return jamnikJ;
    }

    public void setJamnikJ(int jamnikJ) {
        this.jamnikJ = jamnikJ;
    }

    public double getJamnikRotation() {
        return jamnikRotation;
    }

    public void setJamnikRotation(double jamnikRotation) {
        this.jamnikRotation = jamnikRotation;
    }

    public void setJamnikImg(Image jamnikImg) {
        imageView.setImage(jamnikImg);
    }
}
