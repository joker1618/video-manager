package xxx.joker.apps.video.manager.jfx.controller.videoplayer;

import xxx.joker.apps.video.manager.datalayer.entities.Video;

import java.nio.file.Path;

public class VideoWrapper {

    private Video video;
    private Path path;

    public VideoWrapper() {
    }

    public VideoWrapper(Video video, Path path) {
        this.video = video;
        this.path = path;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
