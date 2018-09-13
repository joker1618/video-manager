package stuff;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static xxx.joker.libs.javalibs.utils.JkConsole.display;

public class TempTest {

	@Test
	public void test() throws IOException {
        LocalTime now = LocalTime.now();
        LocalTime plusDT = LocalTime.of(2, 2, 2, 22);
        LocalTime nowPlus = now.plus(plusDT.toNanoOfDay(), ChronoUnit.NANOS);
        display("%s\t%d\t%d", DateTimeFormatter.ISO_TIME.format(now), now.get(ChronoField.MILLI_OF_SECOND), now.getNano());
        display("%s\t%d\t%d", now, now.toNanoOfDay(), now.getNano());
        display("%s\t%d\t%d", nowPlus, nowPlus.toNanoOfDay(), nowPlus.getNano());

        display("%s", Instant.from(now).get(ChronoField.MILLI_OF_SECOND));
//		List<String> lines = Files.readAllLines(Config.CSV_STAGE_POSITIONS);
//		String strLines = JkStreams.join(lines, "\n", l -> l.replaceAll("#.*", ""));
//		String[] elems = JkStrings.splitAllFields(strLines, "}", true, false);
//
//		Arrays.stream(elems).forEach(JkConsole::display);
	}

}
