package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.config.Config;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.CategoryNW;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.model.entity.VideoNEW;
import xxx.joker.apps.video.manager.repository.JkDataModel;
import xxx.joker.apps.video.manager.repository.entity.JkEntity;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class VideoModelImplNEW extends JkDataModel implements VideoModelNEW {

	private static final Logger logger = LoggerFactory.getLogger(VideoModelImplNEW.class);

	private static final VideoModelImplNEW instance = new VideoModelImplNEW();
	private final Map<Class<?>, ObservableList<? extends JkEntity>> dataMap = new HashMap<>();
	private final ObservableList<VideoNEW> selectedVideos = FXCollections.observableArrayList();
	private final PlayOptions playOptions = new PlayOptions();


	private VideoModelImplNEW() {
		super(Config.DB_FOLDER, Config.DB_NAME+"NEWFMT", "xxx.joker.apps.video.manager.model.entity");
		try {
			dataMap.put(CategoryNW.class, FXCollections.observableArrayList(super.getData(CategoryNW.class)));
			ObservableList<VideoNEW> videos = FXCollections.observableArrayList(super.getData(VideoNEW.class));
			dataMap.put(VideoNEW.class, videos);
			videos.addListener((ListChangeListener<? super VideoNEW>) c -> selectedVideos.removeIf(v -> !videos.contains(v)));
			performInitVideoChecks();

		} catch (Exception e) {
			logger.error("Unable to create dao instance: {}", e);
			throw new RuntimeException(e);
		}

	}
	private void performInitVideoChecks() {
		// Remove non existing videos
		getVideos().removeIf(v -> !Files.exists(v.getPath()));
		// Remove non existing categories
		for(VideoNEW video : getVideos()) {
			if(!video.getCategories().isEmpty())
				video.getCategories().removeIf(sc -> !getCategories().contains(sc));
		}
	}

	public static VideoModelNEW getInstance() {
		return instance;
	}

	@Override
	public ObservableList<CategoryNW> getCategories() {
		return (ObservableList<CategoryNW>)dataMap.get(CategoryNW.class);
	}

	@Override
	public ObservableList<VideoNEW> getVideos() {
		return (ObservableList<VideoNEW>)dataMap.get(VideoNEW.class);
	}

	@Override
	public ObservableList<VideoNEW> getSelectedVideos() {
		return selectedVideos;
	}

	@Override
	public PlayOptions getPlayOptions() {
		return playOptions;
	}

	@Override
	public void persistData() {
		TreeSet<CategoryNW> categories = super.getData(CategoryNW.class);
		categories.clear();
		categories.addAll(getCategories());

		TreeSet<VideoNEW> videos = super.getData(VideoNEW.class);
		videos.clear();
		videos.addAll(getVideos());

		super.commit();
	}
}
