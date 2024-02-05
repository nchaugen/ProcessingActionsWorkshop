package data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record Batch(Long number, List<BatchItem> items) {

    @SafeVarargs
    public final Batch processed(List<BatchItem>... itemLists) {
        return new Batch(
            this.number,
            Arrays.stream(itemLists)
                .flatMap(Collection::stream)
                .toList()
        );
    }

    @Override
    public String toString() {
        return Batch.class.getSimpleName() + "[\n  "
            + String.join("\n  ", items.stream().map(BatchItem::toString).toList())
            + "\n]";
    }
}
