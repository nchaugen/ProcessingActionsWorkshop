package utils;

import integration.HttpClient;

import static integration.BackendResult.fatal;
import static org.mockito.Mockito.mock;

public class MockHttpClient {
    public static final HttpClient HTTP_CLIENT = mock(HttpClient.class,
        invocation -> fatal(new IllegalAccessException("Unexpected " + invocation)));

}
