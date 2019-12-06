package xxx.joker.libs.core.object;

import org.apache.commons.lang3.tuple.MutablePair;
import xxx.joker.libs.core.util.JkStrings;

public class JkPoints<T extends Number> extends MutablePair<T,T> {

    private Class<?> elemClass;

    public static JkPoints<Double> fromStr(String str) {
        String[] split = JkStrings.splitArr(str, "x");
        return new JkPoints<>(Double.valueOf(split[0]), Double.valueOf(split[1]));
    }

    public JkPoints(T left, T right) {
        super(left, right);
        elemClass = left.getClass();
    }

    public T getDiff() {
        if(elemClass == Short.class){
            return (T) Short.valueOf((short)(getLeft().shortValue() - getRight().shortValue()));
        }
        if(elemClass == Integer.class){
            return (T) Integer.valueOf(getLeft().intValue() - getRight().intValue());
        }
        if(elemClass == Long.class){
            return (T) Long.valueOf(getLeft().longValue() - getRight().longValue());
        }
        if(elemClass == Float.class){
            return (T) Float.valueOf(getLeft().floatValue() - getRight().floatValue());
        }
        return (T) Double.valueOf(getLeft().doubleValue() - getRight().doubleValue());
    }

}