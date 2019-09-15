package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config12;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.beans.PlayOptions;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.datalayer.design.RepoEntity;

import java.nio.file.Path;
import java.util.*;

public class VideoModelImpl implements VideoModel {

	private static final Logger logger = LoggerFactory.getLogger(VideoModelImpl.class);

	private static final VideoModelImpl instance = new VideoModelImpl();
	private final Map<Class<?>, ObservableList<? extends RepoEntity>> dataMap = new HashMap<>();
	private final ObservableList<Video> selectedVideos = FXCollections.observableArrayList();
	private final PlayOptions playOptions = new PlayOptions();
	private final VideoRepo repo = VideoRepo.getRepo();


	private VideoModelImpl() {
		try {
			dataMap.put(Category.class, FXCollections.observableArrayList(repo.getDataSet(Category.class)));
			ObservableList<Video> videos = FXCollections.observableArrayList(repo.getDataSet(Video.class));
			dataMap.put(Video.class, videos);
			videos.addListener((ListChangeListener<? super Video>) c -> selectedVideos.removeIf(v -> !videos.contains(v)));
			performInitVideoChecks();

		} catch (Exception e) {
			logger.error("Unable to create dao instance: {}", e);
			throw new RuntimeException(e);
		}

	}
	private void performInitVideoChecks() {
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
		List<Path> paths = JkFiles.findFiles(
				Config12.SNAPSHOT_FOLDER, false,
				p -> p.getFileName().toString().startsWith(video.getMd5()),
				p -> p.getFileName().toString().endsWith(".png")
		);
		paths.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString().replaceAll("^[a-zA-Z0-9]*_|\\.snap.*\\.png$", ""))));
		return paths;
	}

	@Override
	public void persistData() {
		Set<Category> categories = repo.getDataSet(Category.class);
		categories.clear();
		categories.addAll(getCategories());

		Set<Video> videos = repo.getDataSet(Video.class);
		videos.clear();
		videos.addAll(getVideos());

		repo.commit();
	}

	@Override
	public VideoRepo getRepo() {
		return repo;
	}
}
