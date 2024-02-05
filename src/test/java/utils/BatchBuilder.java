package utils;

import data.ActionResult;
import data.Batch;
import data.BatchItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class BatchBuilder {

    public static BatchBuilder batchOf(String... ids) {
        return new BatchBuilder(Arrays.stream(ids).map(BatchItem::new).toList());
    }

    private List<BatchItem> items;

    private BatchBuilder(List<BatchItem> items) {
        this.items = items;
    }

    public BatchBuilder withData(String name, String... data) {
        this.items = IntStream.range(0, data.length)
            .mapToObj(index -> {
                BatchItem item = items.get(index);
                return data[index] == null
                    ? item
                    : new BatchItem(item.id(), Map.of(name, data[index]), item.results());
            })
            .toList();
        return this;
    }

    public BatchBuilder withResult(ActionResult... results) {
        this.items = IntStream.range(0, results.length)
            .mapToObj(index -> {
                BatchItem item = items.get(index);
                return results[index] == null
                    ? item
                    : new BatchItem(item.id(), item.data(), List.of(results[index]));
            })
            .toList();
        return this;
    }

    public Batch build() {
        return new Batch(1L, this.items);
    }
}
