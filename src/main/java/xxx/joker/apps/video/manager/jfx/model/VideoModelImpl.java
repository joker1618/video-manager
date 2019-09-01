package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.libs.core.repository.JkDataModel;
import xxx.joker.libs.core.repository.entity.JkEntity;
import xxx.joker.libs.core.utils.JkFiles;
import xxx.joker.libs.core.utils.JkStreams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VideoModelImpl extends JkDataModel implements VideoModel {

	private static final Logger logger = LoggerFactory.getLogger(VideoModelImpl.class);

	private static final VideoModelImpl instance = new VideoModelImpl();
	private final Map<Class<?>, ObservableList<? extends JkEntity>> dataMap = new HashMap<>();
	private final ObservableList<Video> selectedVideos = FXCollections.observableArrayList();
	private final PlayOptions playOptions = new PlayOptions();


	private VideoModelImpl() {
		super(Config.DB_FOLDER, Config.DB_NAME, "xxx.joker.apps.video.manager.model.entity");
		try {
			dataMap.put(Category.class, FXCollections.observableArrayList(super.getData(Category.class)));
			ObservableList<Video> videos = FXCollections.observableArrayList(super.getData(Video.class));
			dataMap.put(Video.class, videos);
			videos.addListener((ListChangeListener<? super Video>) c -> selectedVideos.removeIf(v -> !videos.contains(v)));
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
		for(Video video : getVideos()) {
			if(!video.getCategories().isEmpty())
				video.getCategories().removeIf(sc -> !getCategories().contains(sc));
		}
	}

	public static VideoModel getInstance() {
		return instance;
	}

	@Override
	public ObservableList<Category> getCategories() {
		return (ObservableList<Category>)dataMap.get(Category.class);
	}

	@Override
	public ObservableList<Video> getVideos() {
		return (ObservableList<Video>)dataMap.get(Video.class);
	}

	@Override
	public ObservableList<Video> getSelectedVideos() {
		return selectedVideos;
	}

	@Override
	public PlayOptions getPlayOptions() {
		return playOptions;
	}

	@Override
	public List<Path> findSnapshots(Video video) {
		if(video == null)	return Collections.emptyList();
		return JkFiles.findFiles(
				Config.SNAPSHOT_FOLDER, false,
				p -> p.getFileName().toString().startsWith(video.getMd5()),
				p -> p.getFileName().toString().endsWith(".png")
		);
	}

	@Override
	public void persistData() {
		Set<Category> categories = super.getData(Category.class);
		categories.clear();
		categories.addAll(getCategories());

		Set<Video> videos = super.getData(Video.class);
		videos.clear();
		videos.addAll(getVideos());

		super.commit();
	}
}
