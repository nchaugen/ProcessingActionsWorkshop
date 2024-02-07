package integration;

import static integration.HttpResponse.Type.FAILURE;
import static integration.HttpResponse.Type.FATAL;
import static integration.HttpResponse.Type.SUCCESS;

public record HttpResponse(Type status, Integer statusCode, Object response) {

    public enum Type {SUCCESS, FAILURE, FATAL}

    public static HttpResponse success(Integer statusCode, String responseBody) {
        return new HttpResponse(SUCCESS, statusCode, responseBody);
    }

    public static HttpResponse success(Integer statusCode) {
        return success(statusCode, "");
    }

    public static HttpResponse failure(Integer statusCode, String responseBody) {
        return new HttpResponse(FAILURE, statusCode, responseBody);
    }

    public static HttpResponse fatal(Exception exception) {
        return new HttpResponse(FATAL, null, exception);
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
