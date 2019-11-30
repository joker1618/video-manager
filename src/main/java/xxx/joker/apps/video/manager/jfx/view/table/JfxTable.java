package xxx.joker.apps.video.manager.jfx.view.table;

import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import xxx.joker.libs.core.util.JkStrings;

import java.util.Arrays;

public class JfxTable<T> extends TableView<T> {

    public static final String CSS_CLASS_FIXED_WIDTH = "fixedWidth";

    private static final String CSS_FILEPATH = "/css/JfxTable.css";
    private static final double EXTRA_COL_WIDTH = 30d;

    public JfxTable(String... styleClasses) {
        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        getStyleClass().addAll("jfxTable");
        getStyleClass().addAll(JkStrings.splitFlat(styleClasses));
        getStylesheets().add(getClass().getResource(CSS_FILEPATH).toExternalForm());
    }

    public void addColumn(JfxTableCol... cols) {
        Arrays.stream(cols).forEach(c -> {
            getColumns().add(c);
        });

        if(!getColumns().isEmpty()){
            for(int i = 0; i < getColumns().size(); i++) {
                JfxTableCol<T, ?> col = getJfxCol(i);
                col.getStyleClass().removeIf(s -> s.startsWith("col") || s.equals("jfxTableCol"));
                col.getStyleClass().addAll("jfxTableCol", "col", "col" + i);
                col.getStyleClass().add(i % 2 == 0 ? "col-odd" : "col-even");
                col.getStyleClass().add(i == 0 ? "col-first" : i == getColumns().size() - 1 ? "col-last" : "col-middle");
            }
        }
    }

    public void resizeWidth() {
        double tablePrefWidth = 2d + 20d;

        //Set the right policy
        for(int idx = 0; idx < getColumns().size(); idx++) {
            JfxTableCol<T, ?> col = getJfxCol(idx);

            if(col.isVisible()) {
                double wcol;
                if (col.getStyleClass().contains(CSS_CLASS_FIXED_WIDTH)) {
                    wcol = col.getPrefWidth();
                } else {
                    //Minimal width = columnheader
                    Text t = new Text(col.getText());
                    double max = t.getLayoutBounds().getWidth();
                    for (int i = 1; i < getItems().size(); i++) {
                        //cell must not be empty
                        if (col.getCellData(i) != null) {
                            t = new Text(col.formatCellData(i));
                            double calcwidth = t.getLayoutBounds().getWidth();
                            if (calcwidth > max) {
                                max = calcwidth;
                            }
                        }
                    }
                    wcol = max + EXTRA_COL_WIDTH;
                }
                col.setPrefWidth(wcol);
                // add extra space
                tablePrefWidth += wcol;
            }
        }

        setPrefWidth(tablePrefWidth);
    }

    private JfxTableCol<T,?> getJfxCol(int index) {
        return (JfxTableCol<T,?>) getColumns().get(index);
    }
}