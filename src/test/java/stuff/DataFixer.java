package stuff;

import org.junit.Test;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;

public class DataFixer {

	@Test
	public void fixVideoValues() throws Exception {
		VideoModel model = VideoModelImpl.getInstance();

		// Fix values
//		model.getCategories().clear();
		model.getVideos().forEach(v -> {
			v.setPlayTimes(0);
			v.setCataloged(false);
//			v.getCategories().clear();
		});

		// Persist
		model.persistData();
	}
}
