package actions;

import data.Batch;
import data.BatchItem;
import integration.HttpResponse;
import integration.HttpClient;
import integration.Json;

import java.net.URI;
import java.util.List;

public class AllOrNothingAction implements Action {

    private final String name;
    private final HttpClient httpClient;
    private final URI endpoint;

    public AllOrNothingAction(String name, HttpClient httpClient, URI endpoint) {
        this.name = name;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public Batch process(Batch batch) {
        HttpResponse result = postAll(batch.items());
        return switch (result.status()) {
            case SUCCESS -> allSucceeded(batch);
            case FAILURE -> allFailed(batch, result.failureResponse());
            case FATAL -> allFailed(batch, result.fatalResponse());
        };
    }

    private Batch allSucceeded(Batch batch) {
        return batch.processed(
            batch.items().stream()
                .map(item -> item.actionSucceeded(name))
                .toList()
        );
    }

    private Batch allFailed(Batch batch, Object error) {
        return batch.processed(
            batch.items().stream()
                .map(item -> item.actionFailed(name, error))
                .toList()
        );
    }

    private HttpResponse postAll(List<BatchItem> items) {
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
}
