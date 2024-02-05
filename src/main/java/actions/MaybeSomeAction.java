package actions;

import data.Batch;
import data.BatchItem;
import integration.BackendResult;
import integration.HttpClient;
import integration.Json;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class MaybeSomeAction implements Action {

    private final String name;
    private final HttpClient httpClient;
    private final URI endpoint;

    public MaybeSomeAction(String name, HttpClient httpClient, URI endpoint) {
        this.name = name;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public Batch process(Batch batch) {
        BackendResult result = postAll(batch.items());
        return switch (result.status()) {
            case SUCCESS -> markSucceeded(batch, result.successResponse());
            case FAILURE -> allFailed(batch, result.failureResponse());
            case FATAL -> allFailed(batch, result.fatalResponse());
        };
    }

    private Batch markSucceeded(Batch batch, String jsonResponse) {
        Response response = Json.read(Response.class, jsonResponse);
        return batch.processed(
            batch.items().stream()
                .map(item -> succeeded(item, response)
                    ? item.actionSucceeded(name)
                    : item.actionFailed(name, errorMessage(item, response)))
                .toList()
        );
    }

    private static boolean succeeded(BatchItem item, Response response) {
        return !response.failed.containsKey(item.id());
    }

    private static String errorMessage(BatchItem item, Response response) {
        return response.failed.get(item.id());
    }

    private Batch allFailed(Batch batch, Object error) {
        return batch.processed(
            batch.items().stream()
                .map(item -> item.actionFailed(name, error))
                .toList()
        );
    }

    private BackendResult postAll(List<BatchItem> items) {
        return httpClient.post(endpoint, jsonRequest(items));
    }

    private String jsonRequest(List<BatchItem> items) {
        return Json.write(new Request(
            items.stream()
                .map(item -> new RequestItem(
                    item.id(),
                    item.data().get(name).toString()
                )).toList()
        ));
    }

    private record Request(List<RequestItem> items) {
    }

    private record RequestItem(String id, String data) {
    }

    private record Response(Map<String, String> failed) {
    }
}
