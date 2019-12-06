package xxx.joker.libs.core.format.csv;

public class CsvConst {

    public static final CsvPlaceholder SEP_FIELD = new CsvPlaceholder("|", "@PIPE@");
    public static final CsvPlaceholder SEP_LIST = new CsvPlaceholder(";", "@SCL@");
    public static final CsvPlaceholder SEP_MAP_ENTRIES = new CsvPlaceholder(",", "@COM@");
    public static final CsvPlaceholder SEP_KEY_VALUE = new CsvPlaceholder("=", "@EQ@");
    public static final CsvPlaceholder TAB = new CsvPlaceholder("\t", "@TAB@");
    public static final CsvPlaceholder NEWLINE = new CsvPlaceholder("\n", "@LF@");

    public static final String PH_NULL = "@NUL@";

    public static final int DEF_COL_DISTANCE = 2;


}