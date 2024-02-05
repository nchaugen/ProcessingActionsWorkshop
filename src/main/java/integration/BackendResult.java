package integration;

import static integration.BackendResult.Type.FAILURE;
import static integration.BackendResult.Type.FATAL;
import static integration.BackendResult.Type.SUCCESS;

public record BackendResult(Type status, Integer statusCode, Object response) {

    public enum Type {SUCCESS, FAILURE, FATAL}

    public static BackendResult success(Integer statusCode, String responseBody) {
        return new BackendResult(SUCCESS, statusCode, responseBody);
    }

    public static BackendResult success(Integer statusCode) {
        return success(statusCode, "");
    }

    public static BackendResult failure(Integer statusCode, String responseBody) {
        return new BackendResult(FAILURE, statusCode, responseBody);
    }

    public static BackendResult fatal(Exception exception) {
        return new BackendResult(FATAL, null, exception);
    }

    public String successResponse() {
        if (status == SUCCESS) {
            return response == null ? null : response.toString();
        }
        throw new IllegalStateException(this + " was not successful");
    }

    public String failureResponse() {
        if (status == FAILURE) {
            return response == null ? null : response.toString();
        }
        throw new IllegalStateException(this + " was not failure");
    }

    public Exception fatalResponse() {
        if (status == FATAL && response instanceof Exception) {
            return (Exception) response;
        }
        throw new IllegalStateException(this + " has non-Exception response");
    }
}
