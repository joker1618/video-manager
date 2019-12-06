package xxx.joker.libs.core.runtime.wrapper;

import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkReflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static xxx.joker.libs.core.runtime.JkReflection.isInstanceOf;
import static xxx.joker.libs.core.runtime.JkReflection.toClass;
import static xxx.joker.libs.core.util.JkConvert.toList;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class TypeWrapper {

    protected Type type;
    protected List<TypeWrapper> paramTypes = new ArrayList<>();

    public TypeWrapper(Type type) {
        this.type = type;
        if(isInstanceOf(type.getClass(), ParameterizedType.class)) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            paramTypes.addAll(JkStreams.map(toList(types), TypeWrapper::new));
        }
    }

    public boolean isParametrized() {
        return !paramTypes.isEmpty();
    }

    public Class<?> getTypeClass() {
        return toClass(type);
    }
    public Class<?> getTypeClass(int childNum) {
        TypeWrapper ft = getParamType(childNum);
        return toClass(ft.type);
    }

    public List<TypeWrapper> getParamTypes() {
        return paramTypes;
    }
    public TypeWrapper getParamType(int childNum) {
        return paramTypes.get(childNum);
    }

    public boolean containsGenericType(Class<?> clazz) {
        for (TypeWrapper twChild : paramTypes) {
            if(twChild.instanceOf(clazz))   return true;
            if(twChild.containsGenericType(clazz))  return true;
        }
        return false;
    }

    public boolean isOfClass(Class<?>... expectedClasses) {
        return JkReflection.isOfClass(getTypeClass(), expectedClasses);
    }
    public boolean isOfClass(Collection<Class<?>> expectedClasses) {
        return JkReflection.isOfClass(getTypeClass(), expectedClasses);
    }

    public boolean instanceOf(Class<?>... expectedClasses) {
        return JkReflection.isInstanceOf(getTypeClass(), expectedClasses);
    }
    public boolean instanceOf(Collection<Class<?>> expectedClasses) {
        return JkReflection.isInstanceOf(getTypeClass(), expectedClasses);
    }
    public boolean instanceOfFlat(Class<?>... expectedClasses) {
        if(instanceOf(expectedClasses)) {
            return true;
        }
        for (TypeWrapper pt : getParamTypes()) {
            if(pt.instanceOfFlat(expectedClasses)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMap() {
        return instanceOf(Map.class);
    }
    public boolean isCollection() {
        return instanceOf(Collection.class);
    }
    public boolean isList() {
        return instanceOf(List.class);
    }
    public boolean isSet() {
        return instanceOf(Set.class);
    }

    public boolean isNumber() {
        return instanceOf(Number.class, short.class, int.class, long.class, float.class, double.class);
    }

    public boolean isComparable() {
        return instanceOf(Comparable.class);
    }
    public boolean isComparableFlat() {
        if(isCollection() || isMap()) {
            TypeWrapper tw = getParamTypes().get(0);
            return tw.isComparable();
        }
        return isComparable();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public  String toString(boolean useCallSimpleName) {
        String str = useCallSimpleName ? getTypeClass().getSimpleName() : getTypeClass().getName();
        if(isParametrized()) {
            String join = JkStreams.join(getParamTypes(), ",", pt -> pt.toString(useCallSimpleName));
            str += strf("<{}>", join);
        }
        return str;
    }
}