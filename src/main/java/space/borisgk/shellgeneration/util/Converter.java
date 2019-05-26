package space.borisgk.shellgeneration.util;

public interface Converter<F, T> {
    T convert(F o);
}
