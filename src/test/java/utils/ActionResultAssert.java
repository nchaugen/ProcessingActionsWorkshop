package utils;

import data.ActionResult;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ThrowingConsumer;

@SuppressWarnings("UnusedReturnValue")
public class ActionResultAssert extends AbstractAssert<ActionResultAssert, ActionResult> {
    protected ActionResultAssert(ActionResult actual) {
        super(actual, ActionResultAssert.class);
    }

    public static ActionResultAssert assertThat(ActionResult actual) {
        return new ActionResultAssert(actual);
    }

    public ActionResultAssert hasActionName(String expected) {
        isNotNull();
        if (!actual.actionName().equals(expected)) {
            failWithMessage("Expected action name of result to be <%s> but was <%s>",
                expected, actual.actionName());
        }
        return this;
    }

    public ActionResultAssert hasType(ActionResult.Type expected) {
        isNotNull();
        if (!actual.type().equals(expected)) {
            failWithMessage("Expected type of result to be <%s> but was <%s>",
                expected, actual.type());
        }
        return this;
    }

    public ActionResultAssert hasErrorMessage(String expected) {
        isNotNull();
        if (!actual.errorMessage().equals(expected)) {
            failWithMessage("Expected error message of result to be <%s> but was <%s>",
                expected, actual.errorMessage());
        }
        return this;
    }

    public static ThrowingConsumer<ActionResult> withSuccess(String action) {
        return actual -> assertThat(actual)
            .hasActionName(action)
            .hasType(ActionResult.Type.SUCCESS);
    }

    public static ThrowingConsumer<ActionResult> withSkipped(String action) {
        return actual -> assertThat(actual)
            .hasActionName(action)
            .hasType(ActionResult.Type.SKIPPED);
    }

    public static ThrowingConsumer<ActionResult> withFailure(String action, String errorMessage) {
        return actual -> assertThat(actual)
            .hasActionName(action)
            .hasErrorMessage(errorMessage)
            .hasType(ActionResult.Type.FAILURE);
    }

}
