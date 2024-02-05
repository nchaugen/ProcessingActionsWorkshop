package actions;

import data.Batch;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static data.ActionResult.failure;
import static data.ActionResult.skipped;
import static data.ActionResult.success;
import static integration.BackendResult.failure;
import static integration.BackendResult.success;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.ActionResultAssert.withFailure;
import static utils.ActionResultAssert.withSkipped;
import static utils.ActionResultAssert.withSuccess;
import static utils.BatchArgumentMatchers.containsAll;
import static utils.BatchAssert.assertThat;
import static utils.BatchBuilder.batchOf;
import static utils.BatchItemAssert.hasResults;
import static utils.BatchItemAssert.singleResult;
import static utils.MockHttpClient.HTTP_CLIENT;

class MaybeSomeIfNotFailedActionTest {

    private static final URI ENDPOINT = URI.create("/endpoint");
    private static final String ACTION = "ACTION";
    public static final String PRIOR_ACTION = "PRIOR_ACTION";
    private final Action action = new MaybeSomeIfNotFailedAction(ACTION, HTTP_CLIENT, ENDPOINT);

    @Test
    void shouldPutAllItemsTogether() {
        Batch input = batchOf("001", "002", "003")
            .withData(ACTION, "ABC", "DEF", "GHI")
            .build();

        when(HTTP_CLIENT
            .put(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(success(200, """
                {"succeeded":["001","002","003"],"failed":{}}
                """));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .allItemsSatisfy(singleResult(withSuccess(ACTION)));
    }

    @Test
    void shouldSucceedOnlySuccessful() {
        Batch input = batchOf("001", "002", "003")
            .withData(ACTION, "ABC", "DEF", "GHI")
            .build();

        when(HTTP_CLIENT
            .put(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(success(200, """
                {"succeeded":["001","003"],"failed":{"002":"Invalid data"}}
                """));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .itemsSatisfyExactlyInAnyOrder(
                singleResult(withSuccess(ACTION)),
                singleResult(withFailure(ACTION, "Invalid data")),
                singleResult(withSuccess(ACTION)));
    }

    @Test
    void shouldFailAllOnFailure() {
        Batch input = batchOf("001", "002", "003")
            .withData(ACTION, "ABC", "DEF", "GHI")
            .build();

        when(HTTP_CLIENT
            .put(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(failure(400, "Invalid data"));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .allItemsSatisfy(singleResult(withFailure(ACTION, "Invalid data")));
    }

    @Test
    void shouldSubmitOnlyItemsWithNoPreviousFailures() {
        Batch input = batchOf("001", "002", "003")
            .withData(ACTION, "ABC", "DEF", "GHI")
            .withResult(
                success(PRIOR_ACTION),
                failure(PRIOR_ACTION, "Invalid"),
                skipped(PRIOR_ACTION))
            .build();

        when(HTTP_CLIENT
            .put(eq(ENDPOINT),
                and(containsAll("001", "ABC", "003", "GHI"), not(contains("002")))))
            .thenReturn(success(200, """
                {"succeeded":["001","003"],"failed":{}}
                """));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .itemsSatisfyExactlyInAnyOrder(
                hasResults(withSuccess(PRIOR_ACTION), withSuccess(ACTION)),
                hasResults(withFailure(PRIOR_ACTION, "Invalid"), withSkipped(ACTION)),
                hasResults(withSkipped(PRIOR_ACTION), withSuccess(ACTION)));
    }
}
