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


		List<String> slist = new ArrayList<>();
		slist.add("fede");
		ObservableList<String> obslist = FXCollections.observableArrayList(slist);
		display(slist.toString());
		display(obslist.toString());
		display("");

		slist.add("ciccio");
		slist.set(0,  "can");
		display(slist.toString());
		display(obslist.toString());
		display("");

		obslist.add("natale");
		obslist.set(0, "giorgio");
		display(slist.toString());
		display(obslist.toString());
		display("");
	}

}
