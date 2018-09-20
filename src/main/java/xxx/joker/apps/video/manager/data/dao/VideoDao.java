package xxx.joker.apps.video.manager.data.dao;


import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;

import java.util.Collection;
import java.util.List;

public interface VideoDao {

	List<Category> getCategories() throws Exception;
	List<Video> getVideos() throws Exception;

	void persistCategories(Collection<Category> categories) throws Exception;
	void persistVideos(Collection<Video> videos) throws Exception;

}
