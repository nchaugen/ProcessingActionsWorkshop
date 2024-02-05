package utils;

import data.ActionResult;
import data.BatchItem;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ThrowingConsumer;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchItemAssert extends AbstractAssert<BatchItemAssert, BatchItem> {
    protected BatchItemAssert(BatchItem actual) {
        super(actual, BatchItem.class);
    }

    @SafeVarargs
    public static ThrowingConsumer<BatchItem> singleResult(ThrowingConsumer<ActionResult>... assertions) {
        return actual -> assertThat(actual.results()).singleElement().satisfies(assertions);
    }

    @SafeVarargs
    public static ThrowingConsumer<BatchItem> hasResults(ThrowingConsumer<ActionResult>... requirements) {
        return actual -> assertThat(actual.results()).satisfiesExactlyInAnyOrder(requirements);
    }

}
