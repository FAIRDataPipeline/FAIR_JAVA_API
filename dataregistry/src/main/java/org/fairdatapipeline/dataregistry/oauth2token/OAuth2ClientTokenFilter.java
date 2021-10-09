package org.fairdatapipeline.dataregistry.oauth2token;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

/** */
@Priority(Priorities.AUTHENTICATION)
class OAuth2ClientTokenFilter implements ClientRequestFilter {

  private final String accessToken;

  /**
   * Create a new filter with predefined access token.
   *
   * @param accessToken Access token.
   */
  public OAuth2ClientTokenFilter(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public void filter(ClientRequestContext request) {
    String authentication = "Token " + this.accessToken;

    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
      request.getHeaders().add(HttpHeaders.AUTHORIZATION, authentication);
    }
  }
}
