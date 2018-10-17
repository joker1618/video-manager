package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.CategoryNW;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.model.entity.VideoNEW;

public interface VideoModelNEW {

	ObservableList<CategoryNW> getCategories();
	ObservableList<VideoNEW> getVideos();

	ObservableList<VideoNEW> getSelectedVideos();

	PlayOptions getPlayOptions();

	void persistData();

}
