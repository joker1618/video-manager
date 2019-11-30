package xxx.joker.apps.video.manager.jfx.model;

import javafx.scene.image.Image;
import xxx.joker.libs.core.datetime.JkDuration;

import java.nio.file.Path;

public class FxSnapshot implements Comparable<FxSnapshot> {

    private Path path;
    private Image image;
    private JkDuration time;

    public FxSnapshot() {
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public JkDuration getTime() {
        return time;
    }

    public void setTime(JkDuration time) {
        this.time = time;
    }

    @Override
    public int compareTo(FxSnapshot o) {
        return time.compareTo(o.time);
    }
}
