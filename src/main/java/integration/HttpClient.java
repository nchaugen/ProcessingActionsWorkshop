package integration;

import java.net.URI;

public interface HttpClient {

    BackendResult get(URI path);

    BackendResult post(URI path, String body);

    BackendResult put(URI path, String body);
}
