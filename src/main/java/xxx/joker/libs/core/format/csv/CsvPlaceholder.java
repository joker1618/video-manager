package xxx.joker.libs.core.format.csv;

public class CsvPlaceholder {

    private String separator;
    private String placeholder;

    public CsvPlaceholder(String separator, String placeholder) {
        this.separator = separator;
        this.placeholder = placeholder;
    }

    public String getSeparator() {
        return separator;
    }

    public String getPlaceholder() {
        return placeholder;
    }

}
