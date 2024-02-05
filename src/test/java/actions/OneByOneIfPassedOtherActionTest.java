package actions;

import data.Batch;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static data.ActionResult.failure;
import static data.ActionResult.skipped;
import static data.ActionResult.success;
import static integration.BackendResult.failure;
import static integration.BackendResult.success;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.ActionResultAssert.withFailure;
import static utils.ActionResultAssert.withSkipped;
import static utils.ActionResultAssert.withSuccess;
import static utils.BatchAssert.assertThat;
import static utils.BatchBuilder.batchOf;
import static utils.BatchItemAssert.hasResults;
import static utils.MockHttpClient.HTTP_CLIENT;

class OneByOneIfPassedOtherActionTest {

    private static final URI ENDPOINT = URI.create("/endpoint");
    private static final String ACTION = "ACTION";
    private static final String PRIOR_ACTION = "PRIOR_ACTION";
    private final Action action = new OneByOneIfPassedOtherAction(ACTION, PRIOR_ACTION, HTTP_CLIENT, ENDPOINT);

    @Test
    void shouldPostEachItemSeparately() {
        Batch input = batchOf("001", "002", "003")
            .withData(ACTION, "ABC", "DEF", "GHI")
            .withResult(
                success(PRIOR_ACTION),
                success(PRIOR_ACTION),
                success(PRIOR_ACTION))
            .build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), contains("ABC")))
            .thenReturn(success(200));

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), contains("DEF")))
            .thenReturn(failure(400, "Invalid data"));

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), contains("GHI")))
            .thenReturn(success(200));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .itemsSatisfyExactlyInAnyOrder(
                hasResults(withSuccess(PRIOR_ACTION), withSuccess(ACTION)),
                hasResults(withSuccess(PRIOR_ACTION), withFailure(ACTION, "Invalid data")),
                hasResults(withSuccess(PRIOR_ACTION), withSuccess(ACTION))
            );
    }

    @Test
    void shouldSkipItemsWithoutSuccessfulPriorAction() {
        Batch input = batchOf("001", "002", "003")
            .withData(ACTION, "ABC", "DEF", "GHI")
            .withResult(
                success(PRIOR_ACTION),
                failure(PRIOR_ACTION, "Invalid"),
                skipped(PRIOR_ACTION))
            .build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), contains("ABC")))
            .thenReturn(success(200));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .itemsSatisfyExactlyInAnyOrder(
                hasResults(withSuccess(PRIOR_ACTION), withSuccess(ACTION)),
                hasResults(withFailure(PRIOR_ACTION, "Invalid"), withSkipped(ACTION)),
                hasResults(withSkipped(PRIOR_ACTION), withSkipped(ACTION)));
    }

}
