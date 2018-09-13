package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;

public interface VideoModel {

	ObservableList<Category> getCategories();
	ObservableList<Video> getVideos();

	ObservableList<Video> getSelectedVideos();

	PlayOptions getPlayOptions();

	void persistData() throws Exception;

}
