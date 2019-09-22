package xxx.joker.apps.video.manager.fxlayer.fxview.table;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.utils.JkStrings;

import java.util.function.Function;

public class JfxTableCol<T, V> extends TableColumn<T, V> {

    private String varName;
    private Function<T, V> extractor;
    private Function<V, String> formatter;

    public JfxTableCol() {
        getStyleClass().addAll("jfxTableCol");
    }

    public static <T,V> JfxTableCol<T,V> createCol(String header, String varName, String... styleClasses) {
        return createCol(header, varName, null, null,styleClasses);
    }
    public static <T,V> JfxTableCol<T,V> createCol(String header, Function<T, V> extractor, String... styleClasses) {
        return createCol(header, null, extractor, null,styleClasses);
    }
    public static <T,V> JfxTableCol<T,V> createCol(String header, String varName, Function<V, String> formatter, String... styleClasses) {
        return createCol(header, varName, null, formatter, styleClasses);
    }
    public static <T,V> JfxTableCol<T,V> createCol(String header, Function<T, V> extractor, Function<V, String> formatter, String... styleClasses) {
        return createCol(header, null, extractor, formatter, styleClasses);
    }
    public static <T,V> JfxTableCol<T,V> createCol(String header, String varName, Function<T, V> extractor, Function<V, String> formatter, String... styleClasses) {
        JfxTableCol<T,V> col = new JfxTableCol<>();

        for (String str : styleClasses) {
            col.getStyleClass().addAll(JkStrings.splitList(str, " ", true));
        }

        if(StringUtils.isNotBlank(header)) {
            col.setText(header);
        }

        if(StringUtils.isNotBlank(varName)) {
            col.setCellValueFactory(new PropertyValueFactory<>(varName));
        } else {
            col.extractor = extractor;
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(extractor.apply(param.getValue())));
        }

        if(formatter != null) {
            col.formatter = formatter;
            col.setCellFactory(param -> new TableCell<T, V>() {
                @Override
                protected void updateItem (V item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(formatter.apply(item));
                    }
                }
            });
        }

        return col;
    }

    public String formatCellData(int i) {
        V cellData = super.getCellData(i);
        return formatter == null ? cellData.toString() : formatter.apply(cellData);
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public Function<T, V> getExtractor() {
        return extractor;
    }

    public void setExtractor(Function<T, V> extractor) {
        this.extractor = extractor;
    }

    public Function<V, String> getFormatter() {
        return formatter;
    }

    public void setFormatter(Function<V, String> formatter) {
        this.formatter = formatter;
    }

    public void setFixedPrefWidth(double prefWidth) {
        getStyleClass().add(JfxTable.CSS_CLASS_FIXED_WIDTH);
        setMinWidth(prefWidth);
        setPrefWidth(prefWidth);
    }


}
