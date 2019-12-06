package xxx.joker.libs.core.format;


import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public abstract class JkSortFormattable<T> implements JkFormattable<T> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JkSortFormattable other = (JkSortFormattable) o;
        return StringUtils.equalsIgnoreCase(format(), other.format());
    }

    @Override
    public int hashCode() {
        return Objects.hash(format().toLowerCase());
    }

    @Override
    public int compareTo(T other) {
        return StringUtils.compareIgnoreCase(format(), ((JkFormattable)other).format());
    }

    @Override
    public String toString() {
        return format();
    }

}
