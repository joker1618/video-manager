package stuff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.javalibs.utils.JkConsole.display;

public class TempTest {

	@Test
	public void test() throws IOException {
        String str = "^^fe^$de$$";
        display(str.replace("^", "").replace("$", ""));
        display(str.replaceAll("^\\^", "").replaceAll("\\$$", ""));

	}

}
