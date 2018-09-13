package xxx.joker.apps.video.manager.data.dao;



import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface VideoDao {

	List<Category> getCategories() throws IOException;
	List<Video> getVideos() throws IOException;

	void persistCategories(Collection<Category> categories) throws IOException;
	void persistVideos(Collection<Video> videos) throws IOException;

}
