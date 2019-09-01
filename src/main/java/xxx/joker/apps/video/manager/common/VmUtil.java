package xxx.joker.apps.video.manager.common;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.utils.JkFiles;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VmUtil {

    public static void takeSnapshot(Node node, Path outPath) {
        try {
            WritableImage image = node.snapshot(new SnapshotParameters(), null);
            Files.createDirectories(JkFiles.getParent(outPath));
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outPath.toFile());
        } catch (IOException e) {
            throw new JkRuntimeException(e);
        }
    }
}
