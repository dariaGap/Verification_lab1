package util;

import java.util.*;

public class Util {
    private Util(){

    }

    public static Map<String, Set<Integer>> mergeVersions(
            Map<String, Set<Integer>> versions1,
            Map<String, Set<Integer>> versions2) {
        Map<String, Set<Integer>> mergedVersions = new HashMap<>(versions1);
        for (Map.Entry<String, Set<Integer>> entry : versions2.entrySet()) {
            String label = entry.getKey();
            Set<Integer> list = entry.getValue();
            mergedVersions.merge(label, list, (l1, l2) -> {
                Set result = new HashSet<>(l1);
                result.addAll(l2);
                return new HashSet<>(result);
            });
        }
        return mergedVersions;
    }
}
