package xxx.joker.libs.core.javafx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class JfxGridPaneBuilder {

    // row, cols
    private Map<Integer, Map<Integer, GpBox>> boxMap = new HashMap<>();

    public JfxGridPaneBuilder add(int row, int col, String lbl, Object... params) {
        return add(row, col, 1, 1, lbl, params);
    }
    public JfxGridPaneBuilder add(int row, int col, int rowSpan, int colSpan, String lbl, Object... params) {
        return add(row, col, rowSpan, colSpan, new Label(strf(lbl, params)));
    }
    public JfxGridPaneBuilder add(int row, int col, Node node) {
        return add(row, col, 1, 1, node);
    }
    public JfxGridPaneBuilder add(int row, int col, int rowSpan, int colSpan, Node node) {
        HBox hBox = new HBox(node);
        GpBox gpBox = new GpBox(hBox, rowSpan, colSpan);
        boxMap.putIfAbsent(row, new HashMap<>());
        boxMap.get(row).put(col, gpBox);
        return this;
    }

    public JfxGridPaneBuilder removeRow(int row) {
        boxMap.remove(row);
        return this;
    }
    public JfxGridPaneBuilder removeColumn(int col) {
        boxMap.forEach((r,map) -> map.remove(col));
        return this;
    }
    public JfxGridPaneBuilder removeCell(int row, int col) {
        if(boxMap.containsKey(row)) {
            boxMap.get(row).remove(col);
        }
        return this;
    }

    public GridPane createGridPane() {
        GridPane gp = new GridPane();
        createGridPane(gp);
        return gp;
    }

    public void createGridPane(GridPane gp) {
        gp.getChildren().clear();
        if(!gp.getStyleClass().contains("jfxGridBox")) {
            gp.getStyleClass().addAll("jfxGridBox");
        }

        // Compute column number
        int maxRow = 1 + boxMap.keySet().stream().mapToInt(i -> i).max().orElse(-1);
        int maxCol = -1;
        for (Map<Integer, GpBox> map : boxMap.values()) {
            Integer max = map.keySet().stream().mapToInt(i -> i).max().orElse(-1);
            if(max > maxCol) {
                maxCol = max;
            }
        }
        maxCol++;


        // Analyze row/col span to get the positions that are spanned,
        // in order to avoid to create an HBox in that position
        Map<Integer, List<Integer>> spannedPos = new HashMap<>();
        for(int nr = 0; nr < maxRow; nr++)  spannedPos.put(nr, new ArrayList<>());
        boxMap.forEach((rowNum,map) -> {
            map.forEach((colNum,gbox) -> {
                for(int cn = 0; cn < gbox.getColSpan(); cn++) {
                    for(int rn = 0; rn < gbox.getRowSpan(); rn++) {
                        if(cn > 0 || rn > 0) {
                            spannedPos.putIfAbsent(rn + rowNum, new ArrayList<>());
                            spannedPos.get(rn + rowNum).add(cn + colNum);
                        }
                    }
                }
            });
        });

        // Create grid pane
        for(int r = 0; r < maxRow; r++) {
            for(int c = 0; c < maxCol; c++) {
                if(!spannedPos.get(r).contains(c)) {
                    boolean emptyCell;
                    GpBox gpBox;
                    if(boxMap.containsKey(r) && boxMap.get(r).containsKey(c)) {
                        gpBox = boxMap.get(r).get(c);
                        emptyCell = false;
                    } else {
                        gpBox = new GpBox();
                        emptyCell = true;
                    }

                    HBox hbox = gpBox.getHbox();
                    if(emptyCell)   hbox.getStyleClass().add("cellEmpty");
                    hbox.getStyleClass().addAll("row" + r, "col" + c, "cellBox");
                    hbox.getStyleClass().add(r % 2 == 0 ? "row-odd" : "row-even");
                    hbox.getStyleClass().add(c % 2 == 0 ? "col-odd" : "col-even");
                    if (c == maxCol - 1) hbox.getStyleClass().add("col-last");
                    else if (c > 0) hbox.getStyleClass().add("col-middle");
                    if (r == maxRow - 1) hbox.getStyleClass().add("row-last");
                    else if (r > 0) hbox.getStyleClass().add("row-middle");
                    gp.add(hbox, c, r, gpBox.getColSpan(), gpBox.getRowSpan());
                }
            }
        }
    }

    private class GpBox {
        private int rowSpan = 1;
        private int colSpan = 1;
        private HBox hbox;

        public GpBox() {
            this.hbox = new HBox(new Label(""));
        }

        public GpBox(HBox hbox, int rowSpan, int colSpan) {
            this.hbox = hbox;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }

        public int getRowSpan() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;
        }

        public int getColSpan() {
            return colSpan;
        }

        public void setColSpan(int colSpan) {
            this.colSpan = colSpan;
        }

        public HBox getHbox() {
            return hbox;
        }

        public void setHbox(HBox hbox) {
            this.hbox = hbox;
        }
    }

}
