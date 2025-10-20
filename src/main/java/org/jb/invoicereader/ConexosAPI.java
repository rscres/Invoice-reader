package org.jb.invoicereader;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.List;

public enum ConexosAPI {
    INSTANCE;

    private String USR;
    private String PWD;
    private HttpRequest.Builder _builder;
    private final HttpClient client;
    private String baseUrl;

    ConexosAPI() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        client = HttpClient.newHttpClient();
        try {
            getConfig();
            login();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static ConexosAPI getInstance() {
        return INSTANCE;
    }

    private void getConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(Paths.get("src/main/resources/config.json").toFile());
            USR = String.valueOf(data.get("conexos").get("usuario")).replaceAll("\"", "");
            PWD = String.valueOf(data.get("conexos").get("senha")).replaceAll("\"", "");
            baseUrl = String.valueOf(data.get("conexos").get("base_url")).replaceAll("\"", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------LOGIN FUNCTIONS---------------

    private boolean login() throws IOException, InterruptedException {
        String requestBody = "{\"username\":\"" + USR + "\",\"password\":\"" + PWD + "\"}";
        System.out.println("Fazendo login/api");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl +  "login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        if (response.statusCode() == 200) {
            _builder = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Cookie", getAuthCookie(response.headers()))
                    .header("Cnx-usnCod", json.get("usnCod").asString())
                    .header("Cnx-filCod", json.get("filiais").get(0).get("filCod").asString());
            System.out.println("Login realizado");
            return true;
        } else if (response.statusCode() == 401) {
            return retryLogin(json.get("sessions"));
        } else {
            System.out.println("Erro ao realizar login: " + response.statusCode());
            return false;
        }
    }

    private String getAuthCookie(HttpHeaders headers) {
        List<String> cookies = headers.allValues("Set-Cookie");
        for (String cookie : cookies) {
            if (cookie.startsWith("sid=")) {
                return cookie.split(";")[0];
            }
        }
        return null;
    }

    private boolean retryLogin(JsonNode data) throws IOException, InterruptedException {
        long loginTime = 0;
        String sessionId = null;
        for (JsonNode login : data) {
            if (loginTime < login.get("sessionLastAccessedTime").asLong() || loginTime == 0) {
                loginTime = login.get("sessionLastAccessedTime").asLong();
                sessionId = login.get("sessionId").asString();
            }
        }

        if (sessionId == null) return false;

        String requestBody2 = "{\"username\": \"" + USR + "\", " +
                "\"password\": \"" + PWD + "\", " +
                "\"sessionToKill\": \"" + sessionId + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody2))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        _builder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Cookie", getAuthCookie(response.headers()))
                .header("Cnx-usnCod", json.get("usnCod").asString())
                .header("Cnx-filCod", json.get("filiais").get(0).get("filCod").asString());
        return true;
    }

    //---------------END OF LOGIN FUNCTIONS---------------
    //---------------REQUEST HELPER FUNCTIONS-------------

    public HttpResponse<String> GetRequest(String uri) throws IOException, InterruptedException {
        HttpRequest request = _builder
                .uri(URI.create(baseUrl + uri))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            login();
            response = GetRequest(uri);
        } else if (response.statusCode() != 200) {
            return null;
        }
        return response;
    }

    public HttpResponse<String> PostRequest(String uri, String body) throws IOException, InterruptedException {
        HttpRequest request = _builder
                .uri(URI.create(baseUrl + uri))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            login();
            response = PostRequest(uri, body);
        }
        return response;
    }

    public HttpResponse<String> PutRequest(String uri, String body) throws IOException, InterruptedException {
        HttpRequest request = _builder
                .uri(URI.create(baseUrl + uri))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            login();
            response = PutRequest(uri, body);
        }
        return response;
    }

    //---------------END OF REQUEST HELPER FUNCTIONS-------------
}
