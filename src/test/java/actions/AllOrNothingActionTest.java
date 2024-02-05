package actions;

import data.Batch;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static integration.BackendResult.failure;
import static integration.BackendResult.success;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.ActionResultAssert.withFailure;
import static utils.ActionResultAssert.withSuccess;
import static utils.BatchArgumentMatchers.containsAll;
import static utils.BatchAssert.assertThat;
import static utils.BatchBuilder.batchOf;
import static utils.BatchItemAssert.singleResult;
import static utils.MockHttpClient.HTTP_CLIENT;

class AllOrNothingActionTest {

    private static final URI ENDPOINT = URI.create("/endpoint");
    private static final String ACTION = "ACTION";
    private final Action action = new AllOrNothingAction(ACTION, HTTP_CLIENT, ENDPOINT);

    @Test
    void shouldPostAllItemsTogether() {
        Batch input = batchOf("001", "002", "003").withData(ACTION, "ABC", "DEF", "GHI").build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(success(201));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .allItemsSatisfy(singleResult(withSuccess(ACTION)));
    }

    @Test
    void shouldMarkAllItemsWithResult() {
        Batch input = batchOf("001", "002", "003").withData(ACTION, "ABC", "DEF", "GHI").build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(failure(400, "Invalid data"));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .allItemsSatisfy(singleResult(withFailure(ACTION,"Invalid data")));
    }

}
