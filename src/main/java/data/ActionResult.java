package data;

public record ActionResult(String actionName, Type type, Object error) {

    public String errorMessage() {
        return error() instanceof Exception e
            ? e.getMessage()
            : String.valueOf(error());
    }

    public enum Type { SUCCESS, FAILURE, SKIPPED }

    public static ActionResult success(String actionName) {
        return new ActionResult(actionName, Type.SUCCESS, null);
    }

    public static ActionResult skipped(String actionName) {
        return new ActionResult(actionName, Type.SKIPPED, null);
    }

    public static ActionResult failure(String actionName, Object error) {
        return new ActionResult(actionName, Type.FAILURE, error);
    }
}
