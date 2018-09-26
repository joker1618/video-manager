package xxx.joker.apps.video.manager.data.dao;

import xxx.joker.apps.video.manager.config.Config;
import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.libs.javalibs.repository.JkRepository;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VideoDaoImpl implements VideoDao {

	@Override
	public List<Category> getCategories() throws Exception {
//		try {
//			return JkRepository.load(Config.CSV_CATEGORIES);
//		} catch(NoSuchFileException ex) {
//			return new ArrayList<>();
//		}
		return null;
	}

	@Override
	public List<Video> getVideos() throws Exception {
//		try {
//			return JkRepository.load(Config.CSV_VIDEOS);
//		} catch(NoSuchFileException ex) {
//			return new ArrayList<>();
//		}
		return null;
	}

	@Override
	public void persistCategories(Collection<Category> categories) throws Exception {
//		JkRepository.save(Config.CSV_CATEGORIES, categories);
	}

	@Override
	public void persistVideos(Collection<Video> videos) throws Exception {
//        JkRepository.save(Config.CSV_VIDEOS, videos);
	}
}
