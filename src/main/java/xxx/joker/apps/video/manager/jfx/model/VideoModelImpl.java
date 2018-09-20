package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.apps.video.manager.data.dao.VideoDao;
import xxx.joker.apps.video.manager.data.dao.VideoDaoImpl;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;

import java.nio.file.Files;
import java.util.Collections;

public class VideoModelImpl implements VideoModel {

	private static final Logger logger = LoggerFactory.getLogger(VideoDaoImpl.class);

	private static final VideoModelImpl instance = new VideoModelImpl();

	private final VideoDao videoDao;
	private final ObservableList<Category> categories;
	private final ObservableList<Video> videos;

	private final ObservableList<Video> selectedVideos;

	private final PlayOptions playOptions;

	private VideoModelImpl() {
		try {
			this.videoDao = new VideoDaoImpl();
			this.categories = FXCollections.observableArrayList(videoDao.getCategories());
			Collections.sort(categories);
			this.videos = FXCollections.observableArrayList(videoDao.getVideos());
			Collections.sort(this.videos);
			this.selectedVideos = FXCollections.observableArrayList();
			this.videos.addListener((ListChangeListener<? super Video>) c -> selectedVideos.removeIf(v -> !videos.contains(v)));
			this.playOptions = new PlayOptions();
			performInitVideoChecks();

		} catch (Exception e) {
			logger.error("Unable to create dao instance: {}", e);
			throw new RuntimeException(e);
		}

	}
	private void performInitVideoChecks() {
		// Remove non existing videos
		videos.removeIf(v -> !Files.exists(v.getPath()));
		// Remove non existing categories
		for(Video video : videos) {
		    if(!video.getCategories().isEmpty())
			    video.getCategories().removeIf(sc -> !categories.contains(sc));
		}
	}

	public static VideoModel getInstance() {
		return instance;
	}

	@Override
	public ObservableList<Category> getCategories() {
		return categories;
	}

	@Override
	public ObservableList<Video> getVideos() {
		return videos;
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
	public void persistData() throws Exception {
		videoDao.persistCategories(categories);
		logger.info("Categories CSV saved");

		videoDao.persistVideos(videos);
		logger.info("Videos CSV saved");

	}
}
