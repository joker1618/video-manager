package stuff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.utils.JkConsole.display;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class TempTest {

	@Test
	public void test() throws IOException {
        String str = "^^fe^$de$$";
        display(str.replace("^", "").replace("$", ""));
        display(str.replaceAll("^\\^", "").replaceAll("\\$$", ""));
        Integer num = null;
        display("Invalid number 1: {}", num);
        display(strf("Invalid number 1: {}", num));
	}

}
