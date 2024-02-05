import actions.Action;
import actions.AllOrNothingAction;
import actions.AllOrNothingIfNeededAction;
import actions.MaybeSomeAction;
import actions.MaybeSomeIfNotFailedAction;
import actions.OneByOneAction;
import actions.OneByOneIfPassedOtherAction;
import data.Batch;
import data.BatchItem;
import integration.BackendResult;
import integration.HttpClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

public class Main {

    public static void main(String[] args) {
        List<Action> actions = List.of(
            new AllOrNothingAction(STEP_1, HAPPY_CLIENT, ENDPOINT_1),
            new OneByOneAction(STEP_2, HAPPY_CLIENT, ENDPOINT_2),
            new MaybeSomeAction(STEP_3, HAPPY_CLIENT, ENDPOINT_3),
            new AllOrNothingIfNeededAction(STEP_4, HAPPY_CLIENT, ENDPOINT_4),
            new OneByOneIfPassedOtherAction(STEP_5, STEP_4, HAPPY_CLIENT, ENDPOINT_5),
            new MaybeSomeIfNotFailedAction(STEP_6, HAPPY_CLIENT, ENDPOINT_6)
        );

        Batch input = new Batch(1L, List.of(
            new BatchItem("001", Map.of(STEP_1, "en", STEP_2, "annen", STEP_3, "gang", STEP_4, "gikk", STEP_5, "hun", STEP_6, "ensom")),
            new BatchItem("002", Map.of(STEP_1, "fem", STEP_2, "fine", STEP_3, "frøkner", STEP_4, "farget", STEP_5, "frakken", STEP_6, "ferdig")),
            new BatchItem("003", Map.of(STEP_1, "denne", STEP_2, "trenger", STEP_3, "ikke", STEP_5, "steg", STEP_6, "4"))
        ));

        Batch result = actions.stream()
            .reduce(input,
                (Batch batch, Action action) -> action.process(batch),
                NO_COMBINE);

        System.out.println(result);
    }

    public static final BinaryOperator<Batch> NO_COMBINE = (b1, b2) -> b1;

    public static final HttpClient HAPPY_CLIENT = new HttpClient() {
        @Override
        public BackendResult get(URI path) {
            return BackendResult.success(200, "{}");
        }

        @Override
        public BackendResult post(URI path, String body) {
            return BackendResult.success(200, """
                {"failed":{"002":"Invalid data"}}""");
        }

        @Override
        public BackendResult put(URI path, String body) {
            return BackendResult.success(200, """
                {"succeeded":["002","003"],"failed":{"001":"Not found"}}""");
        }
    };

    public static final String STEP_1 = "Step 1";
    public static final String STEP_2 = "Step 2";
    public static final String STEP_3 = "Step 3";
    public static final String STEP_4 = "Step 4";
    public static final String STEP_5 = "Step 5";
    public static final String STEP_6 = "Step 6";
    public static final URI ENDPOINT_1 = URI.create("/serviceA");
    public static final URI ENDPOINT_2 = URI.create("/serviceB");
    public static final URI ENDPOINT_3 = URI.create("/serviceC");
    public static final URI ENDPOINT_4 = URI.create("/serviceD");
    public static final URI ENDPOINT_5 = URI.create("/serviceE");
    public static final URI ENDPOINT_6 = URI.create("/serviceF");
}
