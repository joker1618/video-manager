package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;

import java.nio.file.Path;
import java.util.List;

public interface VideoModel {

	ObservableList<Category> getCategories();
	ObservableList<Video> getVideos();

	ObservableList<Video> getSelectedVideos();

	PlayOptions getPlayOptions();

	List<Path> findSnapshots(Video video);

	void persistData();

	VideoRepo getRepo();
}
