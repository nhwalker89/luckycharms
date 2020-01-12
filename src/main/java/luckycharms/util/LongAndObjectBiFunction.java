package luckycharms.util;

@FunctionalInterface
public interface LongAndObjectBiFunction<I, O> {

   O apply(long index, I value);
}
