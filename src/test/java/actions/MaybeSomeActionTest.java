package actions;

import data.Batch;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static integration.HttpResponse.failure;
import static integration.HttpResponse.success;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.ActionResultAssert.withFailure;
import static utils.ActionResultAssert.withSuccess;
import static utils.BatchArgumentMatchers.containsAll;
import static utils.BatchAssert.assertThat;
import static utils.BatchBuilder.batchOf;
import static utils.BatchItemAssert.singleResult;
import static utils.MockHttpClient.HTTP_CLIENT;

class MaybeSomeActionTest {

    private static final URI ENDPOINT = URI.create("/endpoint");
    private static final String ACTION = "ACTION";
    private final Action action = new MaybeSomeAction(ACTION, HTTP_CLIENT, ENDPOINT);

    @Test
    void shouldPostAllItemsTogether() {
        Batch input = batchOf("001", "002", "003").withData(ACTION, "ABC", "DEF", "GHI").build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(success(200, """
                {"failed":{}}
                """));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .allItemsSatisfy(singleResult(withSuccess(ACTION)));
    }

    @Test
    void shouldSucceedOnlySuccessful() {
        Batch input = batchOf("001", "002", "003").withData(ACTION, "ABC", "DEF", "GHI").build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(success(200, """
                {"failed":{"002":"Invalid data"}}
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
        Batch input = batchOf("001", "002", "003").withData(ACTION, "ABC", "DEF", "GHI").build();

        when(HTTP_CLIENT
            .post(eq(ENDPOINT), containsAll("ABC", "DEF", "GHI")))
            .thenReturn(failure(400, "Invalid data"));

        Batch result = action.process(input);

        assertThat(result)
            .hasNumberOfItems(3)
            .allItemsSatisfy(singleResult(withFailure(ACTION, "Invalid data")));
    }

}
