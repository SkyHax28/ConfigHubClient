package com.dew.system.altmanager;

import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class HttpUtils {

    private static final OkHttpClient client = new OkHttpClient();

    public static URI createURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static BUILDER builder(String url) {
        return new BUILDER(url);
    }

    public static BUILDER builder(URI uri) {
        return new BUILDER(uri.toString());
    }

    public static class BUILDER {
        private final Request.Builder requestBuilder;
        private String method = "GET";
        private RequestBody body = null;
        private final String url;

        public BUILDER(String url) {
            this.url = url;
            this.requestBuilder = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
        }

        public BUILDER header(String key, String value) {
            requestBuilder.header(key, value);
            return this;
        }

        public BUILDER bearer(String token) {
            return header("Authorization", "Bearer " + token);
        }

        public BUILDER plaintext() {
            return header("Content-Type", "text/plain");
        }

        public BUILDER acceptJson() {
            return header("Accept", "application/json");
        }

        public BUILDER json() {
            return header("Content-Type", "application/json");
        }

        public BUILDER form() {
            return header("Content-Type", "application/x-www-form-urlencoded");
        }

        public Optional<String> get() {
            this.method = "GET";
            this.body = null;
            return execute();
        }

        public Optional<String> post(String payload) {
            this.method = "POST";
            this.body = RequestBody.create(payload, guessMediaType());
            return execute();
        }

        public Optional<String> put(String payload) {
            this.method = "PUT";
            this.body = RequestBody.create(payload, guessMediaType());
            return execute();
        }

        public Optional<String> delete() {
            this.method = "DELETE";
            this.body = null;
            return execute();
        }

        private Optional<String> execute() {
            try {
                Request request = requestBuilder.method(method, needsBody(method) ? (body != null ? body : RequestBody.create("", null)) : null).build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    return Optional.empty();
                }

                ResponseBody responseBody = response.body();
                return Optional.ofNullable(responseBody != null ? responseBody.string() : null);
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        // ヘッダーから Content-Type を推測して MediaType を返す
        private MediaType guessMediaType() {
            String contentType = requestBuilder.build().header("Content-Type");
            if (contentType == null) contentType = "text/plain";
            return MediaType.parse(contentType);
        }

        // GET, DELETE はボディ不要
        private boolean needsBody(String method) {
            return method.equals("POST") || method.equals("PUT");
        }
    }
}
