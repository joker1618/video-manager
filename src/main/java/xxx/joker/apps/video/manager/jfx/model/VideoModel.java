package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;

public interface VideoModel {

	ObservableList<Category> getCategories();
	ObservableList<Video> getVideos();

	ObservableList<Video> getSelectedVideos();

	PlayOptions getPlayOptions();

	void persistData();

}
