package actions;

import data.Batch;
import data.BatchItem;
import integration.BackendResult;
import integration.HttpClient;
import integration.Json;

import java.net.URI;
import java.util.List;

import static data.ActionResult.Type.SUCCESS;

public class OneByOneIfPassedOtherAction implements Action {

    private final String name;
    private final String prerequisiteName;
    private final HttpClient httpClient;
    private final URI endpoint;

    public OneByOneIfPassedOtherAction(String name, String prerequisiteName, HttpClient httpClient, URI endpoint) {
        this.name = name;
        this.prerequisiteName = prerequisiteName;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public Batch process(Batch batch) {
        List<BatchItem> skippedItems = batch.items().stream()
            .filter(this::hasNotPassedPrerequisite)
            .map(item -> item.actionSkipped(name))
            .toList();

        List<BatchItem> applicableItems = batch.items().stream()
            .filter(this::hasPassedPrerequisite)
            .toList();

        List<BatchItem> updatedItems = applicableItems.stream()
            .map(this::postItem)
            .toList();

        return batch.processed(updatedItems, skippedItems);
    }

    private boolean hasPassedPrerequisite(BatchItem item) {
        return item.results().stream()
            .filter(result -> prerequisiteName.equals(result.actionName()))
            .anyMatch(result -> SUCCESS == result.type());
    }

    private boolean hasNotPassedPrerequisite(BatchItem item) {
        return !hasPassedPrerequisite(item);
    }

    private BatchItem postItem(BatchItem item) {
        BackendResult result = httpClient.post(endpoint, jsonRequest(item));
        return switch (result.status()) {
            case SUCCESS -> item.actionSucceeded(name);
            case FAILURE -> item.actionFailed(name, result.failureResponse());
            case FATAL -> item.actionFailed(name, result.fatalResponse());
        };
    }

    private String jsonRequest(BatchItem item) {
        return Json.write(new Request(
            item.id(),
            item.data().get(name).toString()
        ));
    }

    private record Request(String id, String data) {
    }
}
