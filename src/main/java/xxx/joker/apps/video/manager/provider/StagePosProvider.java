package xxx.joker.apps.video.manager.provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.utils.JkConverter;
import xxx.joker.libs.core.utils.JkFiles;
import xxx.joker.libs.core.utils.JkStreams;
import xxx.joker.libs.core.utils.JkStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StagePosProvider {

	private static final Logger logger = LoggerFactory.getLogger(StagePosProvider.class);

	private static final List<VideoStagesPosition> videoPosList = readPosFile();

	private static List<VideoStagesPosition> readPosFile() {
		try {
			List<String> lines = JkFiles.readLines(StagePosProvider.class.getResourceAsStream(Config.CSV_STAGE_FILEPATH));

			String strLines = JkStreams.join(lines, "\n", l -> l.replaceAll("#.*", ""));
			List<String> elems = JkStrings.splitFieldsList(strLines, "}", true, false);
			elems.removeIf(StringUtils::isBlank);

			List<VideoStagesPosition> videoStagesPositionList = new ArrayList<>();
			for (String elem : elems) {
				String name = JkStrings.splitAllFields(elem, "{", true)[0];
				int nrow = JkConverter.stringToInteger(StringUtils.substringBetween(elem, "nrow=", ";"));
				int ncol = JkConverter.stringToInteger(StringUtils.substringBetween(elem, "ncol=", ";"));
				VideoStagesPosition vspos = new VideoStagesPosition(name, nrow, ncol);
				String[] vpos = StringUtils.substringsBetween(elem, "[", "]");
				Arrays.stream(vpos).forEach(s -> {
					String[] strNums = JkStrings.splitAllFields(s, ",", true);
					Integer[] nums = JkConverter.stringToInteger(strNums);
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

	public static List<String> getPositionNames() {
		return videoPosList.stream().map(VideoStagesPosition::getName).sorted().collect(Collectors.toList());
	}

	public static VideoStagesPosition getStagesPosition(String name) {
		List<VideoStagesPosition> filter = JkStreams.filter(videoPosList, v -> name.equalsIgnoreCase(v.getName()));
		return filter.isEmpty() ? null : filter.get(0);
	}

}
