package utils;

import data.Batch;
import data.BatchItem;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ThrowingConsumer;


@SuppressWarnings("UnusedReturnValue")
public class BatchAssert extends AbstractAssert<BatchAssert, Batch> {
    protected BatchAssert(Batch actual) {
        super(actual, BatchAssert.class);
    }

    public static BatchAssert assertThat(Batch actual) {
        return new BatchAssert(actual);
    }

    public BatchAssert hasNumberOfItems(int expected) {
        isNotNull();
        if (actual.items().size() != expected) {
            failWithMessage("Expected number of items in batch to be <%s> but was <%s>",
                expected, actual.items().size());
        }
        return this;
    }

    public ListAssert<BatchItem> allItemsSatisfy(ThrowingConsumer<BatchItem> requirements) {
        return Assertions.assertThat(actual.items()).allSatisfy(requirements);
    }

    @SafeVarargs
    public final ListAssert<BatchItem> itemsSatisfyExactlyInAnyOrder(ThrowingConsumer<BatchItem>... requirements) {
        return Assertions.assertThat(actual.items()).satisfiesExactlyInAnyOrder(requirements);
    }
}
