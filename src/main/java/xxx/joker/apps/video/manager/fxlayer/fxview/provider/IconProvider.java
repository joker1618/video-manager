package xxx.joker.apps.video.manager.fxlayer.fxview.provider;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class IconProvider {

    public static final String BACKWARD_5 = "backward5.png";
    public static final String BACKWARD_10 = "backward10.png";
    public static final String BACKWARD_30 = "backward30.png";
    public static final String CAMERA = "camera.png";
    public static final String CLOSE = "close.png";
    public static final String DELETE_RED = "deleteRed.png";
    public static final String FORWARD_5 = "forward5.png";
    public static final String FORWARD_10 = "forward10.png";
    public static final String FORWARD_30 = "forward30.png";
    public static final String MARKED = "marked.png";
    public static final String MARKED_GREEN = "markedGreen.png";
    public static final String NEXT = "next.png";
    public static final String PAUSE = "pause.png";
    public static final String PLAY = "play.png";
    public static final String PREVIOUS = "previous.png";
    public static final String UNMARKED = "unmarked.png";

    public ImageView getIcon(String iconName) {
        return getIcon(iconName, null, null, true);
    }
    public ImageView getIcon(String iconName, Double fitWidth, Double fitHeight) {
        return getIcon(iconName, fitWidth, fitHeight, true);
    }
    public ImageView getIcon(String iconName, Double fitWidth, Double fitHeight, boolean preserveRatio) {
        URL url = getClass().getResource(strf("/icons/{}", iconName));
        Image iconImg = new Image(url.toExternalForm());
        ImageView iv = new ImageView(iconImg);
        if(fitWidth != null)    iv.setFitWidth(fitWidth);
        if(fitHeight != null)   iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(preserveRatio);
        return iv;
    }
}
