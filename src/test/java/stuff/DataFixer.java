package stuff;

import org.junit.Test;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;

import static xxx.joker.libs.core.utils.JkConsole.display;

public class DataFixer {

    @Test
    public void fixVideoValues() throws Exception {
        VideoModel model = VideoModelImpl.getInstance();

        // Fix values
//		model.getCategories().clear();
        model.getVideos().forEach(v -> {
            display("%s", v);
        });

        // Persist
        model.persistData();
    }
}
