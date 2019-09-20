package xxx.joker.apps.video.manager.ffmpeg;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.commonOK.Config;
import xxx.joker.libs.core.adapter.JkProcess;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.lambdas.JkStreams;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class FFMPEGAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(FFMPEGAdapter.class);

    private FFMPEGAdapter() {

    }

    public static Path cutVideo(Path videoPath, double startMilli, double lengthMilli) {
        Path outPath = JkFiles.safePath(videoPath);
        String ss = JkDuration.toStringElapsed((long)startMilli, ChronoUnit.HOURS);
        String tt = JkDuration.toStringElapsed((long)lengthMilli, ChronoUnit.HOURS);
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
