package stuff;

import org.junit.Test;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImplNEW;
import xxx.joker.apps.video.manager.jfx.model.VideoModelNEW;

import static xxx.joker.libs.javalibs.utils.JkConsole.display;

public class DataFixer {

    @Test
    public void fixVideoValues() throws Exception {
        VideoModelNEW model = VideoModelImplNEW.getInstance();

        // Fix values
//		model.getCategories().clear();
        model.getVideos().forEach(v -> {
            display("%s", v);
        });

        // Persist
        model.persistData();
    }
}
