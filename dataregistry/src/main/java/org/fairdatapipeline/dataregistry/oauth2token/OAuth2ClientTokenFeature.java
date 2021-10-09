package org.fairdatapipeline.dataregistry.oauth2token;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

/**
 * to register the tokenfilter as a feature. (after example by Miroslav Fuksa on
 * org.glassfish.jersey.client.oauth2)
 */
public class OAuth2ClientTokenFeature implements Feature {

  private final OAuth2ClientTokenFilter filter;

  /**
   * Initialize an OAuth2ClientTokenFilter with given accessToken.
   *
   * @param accessToken the OAuth2 token to use. (it will be used with a 'Token' prefix)
   */
  public OAuth2ClientTokenFeature(String accessToken) {
    this.filter = new OAuth2ClientTokenFilter(accessToken);
  }

  @Override
  public boolean configure(FeatureContext context) {
    context.register(filter);
    return true;
  }
}
