package haidnor.log.common.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Stream
 */
public class Stream {

    /**
     * 数据分页
     *
     * @param list 数据集合
     * @param page 页码
     * @param size 页面容量
     */
    public static <T> List<T> paging(List<T> list, Integer page, Integer size) {
        return list.stream().skip((long) (page - 1) * size).limit(size).
                collect(Collectors.toList());
    }

    /**
     * 提取集合中对象某个字段的 value,以 List 数据结构的形式返回
     */
    public static <T, R> List<R> toList(Collection<T> collection, Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * 提取集合中对象某个字段的 value,以 Set 数据结构的形式返回
     */
    public static <T, R> Set<R> toSet(Collection<T> collection, Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    /**
     * 将集合按照指定的 k,v 构建 Map 数据结构
     */
    public static <T, K, U> Map<K, U> toMap(Collection<T> collection, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return collection.stream().collect(Collectors.toMap(keyMapper, valueMapper, (o1, o2) -> o1));
    }

    /**
     * 将集合按照指定的 k 构建 Map 数据结构
     */
    public static <T, K> Map<K, T> toMap(Collection<T> collection, Function<? super T, ? extends K> keyMapper) {
        return collection.stream().collect(Collectors.toMap(keyMapper, Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 从流中抽取<R>，根据R的不同值生成Map<R,List<T>>.
     *
     * @param list 集合
     * @param func 传入的函数.
     * @param <T>  函数的入参.
     * @param <R>  函数的出参.
     * @return Map<R, List < T>>.
     */
    public static <T, R> Map<R, List<T>> grouping(List<T> list, Function<T, R> func) {
        return list.stream().collect(Collectors.groupingBy(func));
    }

    /**
     * 从流中抽取<R>，根据R的不同值生成Map<R,List<T>>.
     *
     * @param set  集合
     * @param func 传入的函数.
     * @param <T>  函数的入参.
     * @param <R>  函数的出参.
     * @return Map<R, List < T>>.
     */
    public static <T, R> Map<R, List<T>> grouping(Set<T> set, Function<T, R> func) {
        return set.stream().collect(Collectors.groupingBy(func));
    }

    /**
     * 把流中的某一元素用连接标志连接成一个字符串.
     *
     * @param list     集合.
     * @param func     函数.
     * @param joinFlag 连接标志.
     * @param <T>      函数的入参.
     * @return String.
     */
    public static <T> String join(List<T> list, Function<T, String> func, String joinFlag) {
        return list.stream().map(func).collect(Collectors.joining(joinFlag));
    }

    /**
     * 把流中的某一元素用连接标志连接成一个字符串.
     *
     * @param set      集合.
     * @param func     函数.
     * @param joinFlag 连接标志.
     * @param <T>      函数的入参.
     * @return String.
     */
    public static <T> String join(Set<T> set, Function<T, String> func, String joinFlag) {
        return set.stream().map(func).collect(Collectors.joining(joinFlag));
    }

    /**
     * 数据分页
     *
     * @param set  数据集合
     * @param page 页码
     * @param size 页面容量
     */
    public <T> Set<T> paging(Set<T> set, Integer page, Integer size) {
        return set.stream().skip((long) (page - 1) * size).limit(size).
                collect(Collectors.toSet());
    }

    /**
     * 过滤数据
     *
     * @param list       数据集合
     * @param predicates 函数
     */
    @SafeVarargs
    public static <T> List<T> filter(List<T> list, Predicate<? super T>... predicates) {
        java.util.stream.Stream<T> stream = list.stream();
        for (Predicate<? super T> predicate : predicates) {
            stream = stream.filter(predicate);
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * 过滤数据
     *
     * @param set        数据集合
     * @param predicates 函数
     */
    @SafeVarargs
    public static <T> Set<T> filter(Set<T> set, Predicate<? super T>... predicates) {
        java.util.stream.Stream<T> stream = set.stream();
        for (Predicate<? super T> predicate : predicates) {
            stream = stream.filter(predicate);
        }
        return stream.collect(Collectors.toSet());
    }

    /**
     * 数据排序
     *
     * @param list       数据集合
     * @param comparator 函数
     *                   <p>
     *                   1. Comparator.comparingInt(Student::getAge).reversed()
     *                   2. Comparator.comparingInt(Student::getAge).thenComparing(Student::getGrade)
     *                   3. Comparator.comparing(v -> v.getAge(), Comparator.reverseOrder())
     */
    public static <T> List<T> sorted(Collection<T> collection, Comparator<? super T> comparator) {
        return collection.stream().sorted(comparator).collect(Collectors.toList());
    }

}
