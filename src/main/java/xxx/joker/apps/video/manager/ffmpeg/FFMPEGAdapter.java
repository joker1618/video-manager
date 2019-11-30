package xxx.joker.apps.video.manager.ffmpeg;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.libs.core.adapter.JkProcess;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class FFMPEGAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(FFMPEGAdapter.class);

    static {
        if(!Files.exists(Config.FFMPEG_EXE_PATH)) {
            LOG.error(strf("FFMPEG exe does not exists at path {}", Config.FFMPEG_EXE_PATH));
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("FFMPEG exe not found");
            alert.setContentText(strf("Exe path: {}", Config.FFMPEG_EXE_PATH.toAbsolutePath()));
            alert.showAndWait();
            throw new JkRuntimeException("FFMPEG exe does not exists at path {}", Config.FFMPEG_EXE_PATH);
        }
    }

    private FFMPEGAdapter() {

    }

    public static Path cutVideo(Path videoPath, double startMilli, double lengthMilli) {
        Path outPath = JkFiles.safePath(videoPath);
        String ss = JkDuration.strElapsed((long)startMilli, ChronoUnit.HOURS);
        String tt = JkDuration.strElapsed((long)lengthMilli, ChronoUnit.HOURS);
        String cmd = strf("{} -ss {} -t {} -i {} -acodec copy -vcodec copy {}",
                Config.FFMPEG_EXE_PATH,
                ss, tt,
                videoPath.getFileName(),
                outPath.getFileName()
        );
        JkProcess process = JkProcess.execute(JkFiles.getParent(videoPath), cmd);
        if(process.getExitCode() == 0) {
            LOG.info(process.toStringResult(0));
            return outPath;
        } else {
            LOG.error(process.toStringResult());
            throw new JkRuntimeException(process.toStringResult());
        }
    }

    public static Path concat(List<Path> piecePaths) {
        if(piecePaths.size() < 2)   return null;

        Path outPath = JkFiles.safePath(piecePaths.get(0));
        Path listPath = JkFiles.safePath(JkFiles.getParent(piecePaths.get(0)).resolve("fileList.txt"));

        try {
            List<String> pathsLines = JkStreams.map(piecePaths, p -> strf("file {}", p.getFileName()));
            JkFiles.writeFile(listPath, pathsLines);

            String cmd = strf("{} -f concat -i {} -acodec copy -vcodec copy {}",
                    Config.FFMPEG_EXE_PATH,
                    listPath.getFileName(),
                    outPath.getFileName()
            );
            JkProcess process = JkProcess.execute(JkFiles.getParent(outPath), cmd);
            if (process.getExitCode() == 0) {
                LOG.info(process.toStringResult(0));
                return outPath;
            } else {
                LOG.error(process.toStringResult());
                throw new JkRuntimeException(process.toStringResult());
            }

        } finally {
            JkFiles.delete(listPath);
        }
    }


}
