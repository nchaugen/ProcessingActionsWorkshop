package utils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.contains;

public class BatchArgumentMatchers {
    public static String containsAll(String... substrings) {
        if (substrings.length == 1) return contains(substrings[0]);
        return containsAll(substrings[0], List.of(dropFirst(substrings)));
    }

    private static String containsAll(String head, List<String> tail) {
        if (tail.isEmpty()) return contains(head);
        return and(contains(head), containsAll(tail.getFirst(), dropFirst(tail)));
    }

    private static String[] dropFirst(String[] substrings) {
        return Arrays.copyOfRange(substrings, 1, substrings.length);
    }

    private static List<String> dropFirst(List<String> substrings) {
        return substrings.subList(1, substrings.size());
    }
}
