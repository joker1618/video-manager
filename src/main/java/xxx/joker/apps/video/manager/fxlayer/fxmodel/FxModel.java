package xxx.joker.apps.video.manager.fxlayer.fxmodel;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;

import java.nio.file.Path;

public interface FxModel {

    static FxModel getModel() {
        return FxModelImpl.getInstance();
    }

    ObservableList<Video> getVideos();
    ObservableList<Category> getCategories();

    ObservableList<Video> getSelectedVideos();

    Path getVideoFile(Video video);
    FxVideo addVideoFile(Path videoPath);

    FxSnapshot getSnapshot(Video video, JkDuration snapTime);
    void addSnapshot(Video video, JkDuration snapTime, Path snapPath);
    void removeSnapshot(Video video, JkDuration snapTime);

    void persistData();
}
