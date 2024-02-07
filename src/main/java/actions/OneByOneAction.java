package actions;

import data.Batch;
import data.BatchItem;
import integration.HttpResponse;
import integration.HttpClient;
import integration.Json;

import java.net.URI;

public class OneByOneAction implements Action {

    private final String name;
    private final HttpClient httpClient;
    private final URI endpoint;

    public OneByOneAction(String name, HttpClient httpClient, URI endpoint) {
        this.name = name;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public Batch process(Batch batch) {
        return batch.processed(batch.items().stream()
            .map(this::postItem)
            .toList());
    }

    private BatchItem postItem(BatchItem item) {
        HttpResponse result = httpClient.post(endpoint, jsonRequest(item));
        return switch (result.status()) {
            case SUCCESS -> item.actionSucceeded(name);
            case FAILURE -> item.actionFailed(name, result.failureResponse());
            case FATAL -> item.actionFailed(name, result.fatalResponse());
        };
    }

    private String jsonRequest(BatchItem item) {
        return Json.write(new Request(item.id(), item.data().get(name).toString()));
    }

    private record Request(String id, String data) {
    }
}
