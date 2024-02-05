package actions;

import data.Batch;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static integration.BackendResult.failure;
import static integration.BackendResult.success;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.ActionResultAssert.withFailure;
import static utils.ActionResultAssert.withSuccess;
import static utils.BatchAssert.assertThat;
import static utils.BatchBuilder.batchOf;
import static utils.BatchItemAssert.singleResult;
import static utils.MockHttpClient.HTTP_CLIENT;

class OneByOneActionTest {

    private static final URI ENDPOINT = URI.create("/endpoint");
    private static final String ACTION = "ACTION";
    private final Action action = new OneByOneAction(ACTION, HTTP_CLIENT, ENDPOINT);

    @Test
    void shouldPostEachItemSeparately() {
        Batch input = batchOf("001", "002", "003").withData(ACTION, "ABC", "DEF", "GHI").build();

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
                singleResult(withSuccess(ACTION)),
                singleResult(withFailure(ACTION, "Invalid data")),
                singleResult(withSuccess(ACTION))
            );
    }

}
