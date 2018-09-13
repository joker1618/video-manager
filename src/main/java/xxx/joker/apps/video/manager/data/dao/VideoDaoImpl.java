package xxx.joker.apps.video.manager.data.dao;

import xxx.joker.apps.video.manager.config.Config;
import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.libs.javalibs.dao.csv.JkCsvDao;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VideoDaoImpl implements VideoDao {

	private final JkCsvDao<Video> daoVideo;
	private final JkCsvDao<Category> daoCategory;

	public VideoDaoImpl() {
		this.daoVideo = new JkCsvDao<>(Config.CSV_VIDEOS, Video.class);
		this.daoCategory = new JkCsvDao<>(Config.CSV_CATEGORIES, Category.class);
	}

	@Override
	public List<Category> getCategories() throws IOException {
		try {
			return daoCategory.readAll();
		} catch(NoSuchFileException ex) {
			return new ArrayList<>();
		}
	}

	@Override
	public List<Video> getVideos() throws IOException {
		try {
			return daoVideo.readAll();
		} catch(NoSuchFileException ex) {
			return new ArrayList<>();
		}
	}

	@Override
	public void persistCategories(Collection<Category> categories) throws IOException {
		daoCategory.persist(categories);
	}

	@Override
	public void persistVideos(Collection<Video> videos) throws IOException {
		daoVideo.persist(videos);
	}
}
