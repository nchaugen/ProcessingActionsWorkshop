package actions;

import data.ActionResult;
import data.Batch;
import data.BatchItem;
import integration.BackendResult;
import integration.HttpClient;
import integration.Json;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class MaybeSomeIfNotFailedAction implements Action {

    private final String name;
    private final HttpClient httpClient;
    private final URI endpoint;

    public MaybeSomeIfNotFailedAction(String name, HttpClient httpClient, URI endpoint) {
        this.name = name;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public Batch process(Batch batch) {
        List<BatchItem> skippedItems = batch.items().stream()
            .filter(MaybeSomeIfNotFailedAction::hasPriorErrors)
            .map(item -> item.actionSkipped(name))
            .toList();

        List<BatchItem> applicableItems = batch.items().stream()
            .filter(MaybeSomeIfNotFailedAction::hasNoPriorErrors)
            .toList();

        BackendResult result = putAll(applicableItems);
        List<BatchItem> updatedItems = switch (result.status()) {
            case SUCCESS -> markSucceeded(applicableItems, result.successResponse());
            case FAILURE -> allFailed(applicableItems, result.failureResponse());
            case FATAL -> allFailed(applicableItems, result.fatalResponse());
        };

        return batch.processed(updatedItems, skippedItems);
    }

    private static boolean hasNoPriorErrors(BatchItem item) {
        return !hasPriorErrors(item);
    }

    private static boolean hasPriorErrors(BatchItem item) {
        return item.results().stream()
            .anyMatch(result -> result.type() == ActionResult.Type.FAILURE);
    }

    private List<BatchItem> markSucceeded(List<BatchItem> items, String jsonResponse) {
        Response response = Json.read(Response.class, jsonResponse);
        return items.stream()
            .map(item -> succeeded(item, response)
                ? item.actionSucceeded(name)
                : item.actionFailed(name, errorMessage(item, response)))
            .toList();
    }

    private static boolean succeeded(BatchItem item, Response response) {
        return response.succeeded.contains(item.id());
    }

    private static String errorMessage(BatchItem item, Response response) {
        return response.failed.get(item.id());
    }

    private List<BatchItem> allFailed(List<BatchItem> items, Object error) {
        return items.stream()
            .map(item -> item.actionFailed(name, error))
            .toList();
    }

    private BackendResult putAll(List<BatchItem> items) {
        return httpClient.put(endpoint, jsonRequest(items));
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

    private record Response(List<String> succeeded, Map<String, String> failed) {
    }
}
