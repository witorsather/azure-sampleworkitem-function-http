package com.fabrikam;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

  /**
   * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
   * 1. curl -d "HTTP Body" {your host}/api/HttpExample
   * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
   */
  @FunctionName("HttpExample")
  public HttpResponseMessage run(
    @HttpTrigger(
      name = "req",
      methods = { HttpMethod.POST },
      authLevel = AuthorizationLevel.ANONYMOUS
    ) HttpRequestMessage<Optional<String>> request,
    final ExecutionContext context
  ) {
    // Item list
    context
      .getLogger()
      .info("Request body is: " + request.getBody().orElse(""));

    String functionUrl =
      "https://funcapp-sampleworkitem-validacao-http.azurewebsites.net/api/httpvalidacao";

    // Check request body
    if (!request.getBody().isPresent()) {
      return request
        .createResponseBuilder(HttpStatus.BAD_REQUEST)
        .body("Document not found.")
        .build();
    } else {
      // return JSON from to the client
      // Generate document
      final String body = request.getBody().get();
      final String jsonDocument =
        "{\"function 2 recebendo \":\"123456\", " +
        "\"description\": \"" +
        body +
        "\"}";

      // Create an HTTP client
      HttpClient httpClient = HttpClient.newHttpClient();

      // Create an HTTP request to call Function 2
      HttpRequest httpRequest = HttpRequest
        .newBuilder()
        .uri(URI.create(functionUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
        .build();

      // Send the HTTP request and retrieve the response
      try {
        HttpResponse<String> httpResponse = httpClient.send(
          httpRequest,
          HttpResponse.BodyHandlers.ofString()
        );

        // Return the response from Function 2
        return request
          .createResponseBuilder(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(httpResponse.body())
          .build();
      } catch (Exception e) {
        // Handle exceptions
        return request
          .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error occurred while calling Function 2.")
          .build();
      }
    }
  }
}
