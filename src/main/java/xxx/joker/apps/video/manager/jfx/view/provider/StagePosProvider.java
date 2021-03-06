package xxx.joker.apps.video.manager.jfx.view.provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.core.util.JkStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StagePosProvider {

	private static final Logger logger = LoggerFactory.getLogger(StagePosProvider.class);

	private static final List<VideoStagesPosition> videoPosList = readPosFile();

	private static List<VideoStagesPosition> readPosFile() {
		try {
			List<String> lines = JkFiles.readLines(StagePosProvider.class.getResourceAsStream(Config.CSV_STAGE_FILEPATH));

			String strLines = JkStreams.join(lines, "\n", l -> l.replaceAll("#.*", ""));
			List<String> elems = JkStrings.splitList(strLines, "}", true, false);
			elems.removeIf(StringUtils::isBlank);

			List<VideoStagesPosition> videoStagesPositionList = new ArrayList<>();
			for (String elem : elems) {
				String name = JkStrings.splitArr(elem, "{", true)[0];
				int nrow = JkConvert.toInt(StringUtils.substringBetween(elem, "nrow=", ";"));
				int ncol = JkConvert.toInt(StringUtils.substringBetween(elem, "ncol=", ";"));
				VideoStagesPosition vspos = new VideoStagesPosition(name, nrow, ncol);
				String[] vpos = StringUtils.substringsBetween(elem, "[", "]");
				Arrays.stream(vpos).forEach(s -> {
					String[] strNums = JkStrings.splitArr(s, ",", true);
					Integer[] nums = JkConvert.toInts(strNums);
					vspos.addVideoPos(nums[0], nums[1], nums[2], nums[3]);
				});
				videoStagesPositionList.add(vspos);
			}

			return videoStagesPositionList;

		} catch (JkRuntimeException ex) {
			logger.error("Unable to read video stage positions from file {}", Config.CSV_STAGE_FILEPATH);
			return null;
		}
	}

	public static List<VideoStagesPosition> getVideoPosList() {
		return videoPosList;
	}

	@Deprecated
	public static List<String> getPositionNames() {
		return JkStreams.map(videoPosList, VideoStagesPosition::getName);
	}

	public static VideoStagesPosition getStagesPosition(String name) {
		return JkStreams.findUnique(videoPosList, v -> name.equalsIgnoreCase(v.getName()));
	}

}
