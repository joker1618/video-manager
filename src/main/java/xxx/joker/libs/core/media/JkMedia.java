package xxx.joker.libs.core.media;

import xxx.joker.libs.core.exception.JkRuntimeException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class JkMedia {

    public static JkImage parseImage(Path imgPath) {
        try {
            JkImage res = new JkImage();
            res.setPath(imgPath);
            BufferedImage img = ImageIO.read(imgPath.toFile());
            res.setWidth(img.getWidth());
            res.setHeight(img.getHeight());
            return res;

        } catch (IOException ex) {
            throw new JkRuntimeException(ex, "Error parsing image file {}", imgPath);
        }
    }

}
