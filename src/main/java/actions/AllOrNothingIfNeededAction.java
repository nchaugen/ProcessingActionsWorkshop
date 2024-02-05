package actions;

import data.Batch;
import data.BatchItem;
import integration.BackendResult;
import integration.HttpClient;
import integration.Json;

import java.net.URI;
import java.util.List;

public class AllOrNothingIfNeededAction implements Action {

    private final String name;
    private final HttpClient httpClient;
    private final URI endpoint;

    public AllOrNothingIfNeededAction(String name, HttpClient httpClient, URI endpoint) {
        this.name = name;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public Batch process(Batch batch) {
        List<BatchItem> skippedItems = batch.items().stream()
            .filter(this::hasNoData)
            .map(item -> item.actionSkipped(name))
            .toList();

        List<BatchItem> applicableItems = batch.items().stream()
            .filter(this::hasData)
            .toList();

        BackendResult result = postAll(applicableItems);
        List<BatchItem> updatedItems = switch (result.status()) {
            case SUCCESS -> allSucceeded(applicableItems);
            case FAILURE -> allFailed(applicableItems, result.failureResponse());
            case FATAL -> allFailed(applicableItems, result.fatalResponse());
        };

        return batch.processed(updatedItems, skippedItems);
    }

    private boolean hasData(BatchItem item) {
        return item.data().containsKey(name);
    }

    private boolean hasNoData(BatchItem item) {
        return !hasData(item);
    }

    private List<BatchItem> allSucceeded(List<BatchItem> items) {
        return items.stream()
            .map(item -> item.actionSucceeded(name))
            .toList();
    }

    private List<BatchItem> allFailed(List<BatchItem> items, Object error) {
        return items.stream()
            .map(item -> item.actionFailed(name, error))
            .toList();
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
}
