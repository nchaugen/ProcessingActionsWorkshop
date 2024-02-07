package integration;

import java.net.URI;

public interface HttpClient {

    HttpResponse get(URI path);

    HttpResponse post(URI path, String body);

    HttpResponse put(URI path, String body);
}
