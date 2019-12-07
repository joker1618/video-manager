package xxx.joker.apps.video.manager.jfx.model;

import xxx.joker.apps.video.manager.datalayer.entities.Video;

import java.nio.file.Path;

public class FxVideo {

    private Video video;

    public FxVideo() {
    }

    public FxVideo(Video video) {
        this.video = video;
    }

    public Video getVideo() {
        return video;
    }

    public Path getPath() {
        return video.getVideoResource().getPath();
    }

}
