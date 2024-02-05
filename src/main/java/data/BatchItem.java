package data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static data.ActionResult.failure;
import static data.ActionResult.skipped;
import static data.ActionResult.success;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public record BatchItem(String id, Map<String, Object> data, List<ActionResult> results) {

    public BatchItem(String id, Map<String, Object> data) {
        this(id, data, emptyList());
    }

    public BatchItem(String id) {
        this(id, emptyMap());
    }

    public BatchItem actionSucceeded(String actionName) {
        return new BatchItem(id, data, append(results, success(actionName)));
    }

    public BatchItem actionFailed(String actionName, Object error) {
        return new BatchItem(id, data, append(results, failure(actionName, error)));
    }

    public BatchItem actionSkipped(String actionName) {
        return new BatchItem(id, data, append(results, skipped(actionName)));
    }

    private static <T> List<T> append(List<T> list, T element) {
        if (element == null) {
            return list;
        }
        LinkedList<T> augmented = list == null ? new LinkedList<>() : new LinkedList<>(list);
        augmented.add(element);
        return augmented;
    }
}
