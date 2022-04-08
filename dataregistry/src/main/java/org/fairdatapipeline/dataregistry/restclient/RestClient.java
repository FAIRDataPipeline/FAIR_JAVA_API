package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.databind.deser.impl.JavaUtilCollectionsDeserializers;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.fairdatapipeline.dataregistry.content.RegistryUsers;
import org.fairdatapipeline.dataregistry.content.Registry_ObjectList;
import org.fairdatapipeline.dataregistry.content.Registry_RootObject;
import org.fairdatapipeline.dataregistry.content.Registry_Updateable;
import org.fairdatapipeline.dataregistry.oauth2token.OAuth2ClientTokenFeature;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Jakarta WS client implementation for interacting with the FAIR Data Registry */
public class RestClient {
  private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
  private WebTarget wt;
  private Client client;
  private final MediaType jsonWithVersion =
      new MediaType("application", "json", Collections.singletonMap("version", "1.0.0"));

  private void init(String registry_url, String token) {
    client =
        ClientBuilder.newBuilder()
            .register(Registry_RootObjectReader.class)
            .register(Registry_RootObjectWriter.class)
            .register(Registry_ObjectListReader.class)
            .register(JavaUtilCollectionsDeserializers.class)
            .register(new OAuth2ClientTokenFeature(token))
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .build();
    wt = client.target(registry_url);
  }

  /**
   * Constructor
   *
   * @param registry_url The URL of the FAIR Data Registry (usually http://127.0.0.1:8000/api/)
   * @param token The authentication token to use to access the FAIR Data Registry.
   */
  public RestClient(String registry_url, String token) {
    init(registry_url, token);
  }

  /**
   * retrieve only the first FDP registry entry found when searching for entries of Class c matching
   * all constraints in Map m
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param m The key-value constraint pairs
   * @return The first item found, or null if none are found.
   */
  public Registry_RootObject getFirst(Class<? extends Registry_RootObject> c, Map<String, String> m)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    // c = the class contained within the objectlist
    // return the first item found.
    WebTarget wt2 = wt.path(Registry_RootObject.get_django_path(c.getSimpleName()));

    for (Map.Entry<String, String> e : m.entrySet()) {
      wt2 = wt2.queryParam(e.getKey(), e.getValue());
    }
    if (c != RegistryUsers.class) {
      /* and not RegistryGroup if we ever implement it */
      wt2 = wt2.queryParam("page_size", 1);
    }
    ParameterizedType p = TypeUtils.parameterize(Registry_ObjectList.class, c);

    try {
      Registry_ObjectList<?> o = wt2.request(this.getJsonMediaType()).get(new GenericType<>(p));
      if (o.getCount() == 0) {
        logger.trace("getFirst({}, {}) returned 0 items", c.getSimpleName(), m);
        return null;
      }
      return o.getResults().get(0);
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
    }
    return null;
  }

  void deal_with_jakarta_http_exceptions(Exception e)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    if (e.getClass() == ProcessingException.class
        || e.getClass() == ResponseProcessingException.class) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        throw (new ConnectException(
            "Can't connect to registry at " + wt + "\nIs the local registry running?", e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        throw (new RegistryJSONException("Error processing JSON response from registry."));
      } else {
        throw (new RestClientException(
            "RestClient encountered unexpected Jakarta Processing exception", e));
      }
    } else if (e.getClass() == jakarta.ws.rs.ForbiddenException.class) {
      throw (new ForbiddenException("HTTP 403 Forbidden -- is the token wrong?", e));
    } else if (e.getClass() == jakarta.ws.rs.NotAcceptableException.class) {
      throw (new RegistryVersionException(
          "HTTP 406 Not Acceptable -- incompatible version of registry"));
    }
  }

  /**
   * retrieve a list of all FDP registry entries of Class c matching all the constraints in Map m
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param m The key-value constraint pairs
   * @return the Registry_ObjectList containing all items matching the constraints in the Map m
   */
  public Registry_ObjectList<Registry_RootObject> getList(
      Class<? extends Registry_RootObject> c, Map<String, String> m)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    // c is the class contained within the ObjectList
    WebTarget wt2 = wt.path(Registry_RootObject.get_django_path(c.getSimpleName()));
    for (Map.Entry<String, String> e : m.entrySet()) {
      wt2 = wt2.queryParam(e.getKey(), e.getValue());
    }
    ParameterizedType p = TypeUtils.parameterize(Registry_ObjectList.class, c);
    GenericType<Registry_ObjectList<Registry_RootObject>> gt = new GenericType<>(p);
    try {
      return wt2.request(this.getJsonMediaType()).get(gt);
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
    }
    return null;
  }

  /**
   * retrieve the FDP registry entry of Class c with ID i
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param i The ID of the FDP Object we're trying to retrieve
   * @return the FDP Object (or null if not found)
   */
  public Registry_RootObject get(Class<? extends Registry_RootObject> c, int i)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    WebTarget wt2 =
        wt.path(Registry_RootObject.get_django_path(c.getSimpleName())).path(Integer.toString(i));
    try {
      return wt2.request(this.getJsonMediaType()).get(c);
    } catch (NotFoundException e) {
      logger.warn("get(Class, Int) {0}", e);
      return null;
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
    }
    return null;
  }

  /**
   * retrieve the FDP registry entry with given APIURL
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param apiurl The APIURL of the Object we are trying to retrieve
   * @return The Object, or null if not found.
   */
  public Registry_RootObject get(Class<? extends Registry_RootObject> c, APIURL apiurl)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    WebTarget wt2 = client.target(apiurl.toString());
    try {
      return wt2.request(this.getJsonMediaType()).get(c);
    } catch (NotFoundException e) {
      logger.warn("get(Class, APIURL) {0}", e);
      return null;
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
    }
    return null;
  }

  /**
   * submit the new FDP registry object o; return the created object (with the APIURL, updated_by,
   * last_updated fields filled in by the registry)
   *
   * @param o The FDP Object we are submitting (should have no URL yet)
   * @return the FDP Object we get back from the registry, with its URL set, or null upon error.
   */
  public Registry_Updateable post(Registry_Updateable o)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    WebTarget wt2 = wt.path(o.get_django_path());
    if (o.getUrl() != null) {
      throw (new RestClientException(
          "post(Registry_Updateable) -- trying to post an already existing object "
              + o.get_django_path()
              + " (object already has an URL entry)"));
    }
    Response r;
    try {
      r = wt2.request(this.getJsonMediaType()).post(Entity.entity(o, this.getJsonMediaType()));
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
      return null;
    }
    if (r.getStatus() != 201) {
      // in case we failed to create the object, log the error message

      InputStream i = (InputStream) r.getEntity();
      try {
        String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
        logger.error("post(Registry_Updateable) -- error: {}", text);
      } catch (IOException e) {
        logger.error(
            "post(Registry_Updateable) -- IOException trying to read response entity: {0}", e);
      }
      return null;
    } else {
      try {
        return r.readEntity(o.getClass());
      } catch (ProcessingException e) {
        throw (new RegistryJSONException("post() fails to read response JSON", e));
      }
    }
  }

  /**
   * patch Object o; this means change the fields set on O, leave the rest untouched. It seems to me
   * (from the tests I did) that patch and put do the same thing, and both appear to behave the way
   * I expect PATCH should behave: NULL fields don't get overwritten.
   *
   * @param o the Object to PATCH; its URL must be set.
   * @return the Object returned from the registry, or null upon error.
   */
  public Registry_Updateable patch(Registry_Updateable o)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    if (!o.allow_method("PATCH")) {
      throw (new IllegalArgumentException(
          "patch(Registry_Updateable) -- trying to use PATCH method on an object that can't be patched."
              + o));
    }
    if (o.getUrl() == null) {
      throw (new RestClientException(
          "patch(Registry_Updateable) -- can't patch an obj without URL"));
    }

    Response r;
    try {
      r =
          client
              .target(o.getUrl().toString())
              .request(this.getJsonMediaType())
              .build("PATCH", Entity.entity(o, this.getJsonMediaType()))
              .invoke();
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
      return null;
    }
    if (r.getStatus() >= 200 && r.getStatus() < 300) {
      try {
        return r.readEntity(o.getClass());
      } catch (ProcessingException e) {
        throw (new RegistryJSONException("patch() fails to read response JSON", e));
      }
    }
    InputStream i = (InputStream) r.getEntity();
    try {
      String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
      logger.error("patch(Registry_Updateable) -- error: {}", text);
    } catch (IOException e) {
      logger.error(
          "patch(Registry_Updateable) -- IOException trying to read response entity: {0}", e);
    }
    return null;
  }

  /**
   * put Object o; this will replace the object at the set URL.
   *
   * @param o the Object to PUT; its URL must be set.
   * @return the Object returned from the registry, or null upon error.
   */
  public Registry_Updateable put(Registry_Updateable o)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    if (!o.allow_method("PUT")) {
      throw (new IllegalArgumentException(
          "put(Registry_Updateable) -- trying to use PUT method on an object that doesn't support PUT."
              + o));
    }
    if (o.getUrl() == null) {
      throw (new RestClientException(
          "put(Registry_Updateable) -- can't PUT an object without URL"));
    }
    Response r;
    try {
      r =
          client
              .target(o.getUrl().toString())
              .request(this.getJsonMediaType())
              .put(Entity.entity(o, this.getJsonMediaType()));
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
      return null;
    }
    if (r.getStatus() >= 200 && r.getStatus() < 300) {
      try {
        return r.readEntity(o.getClass());
      } catch (ProcessingException e) {
        throw (new RegistryJSONException("put() fails to read response JSON", e));
      }
    }
    InputStream i = (InputStream) r.getEntity();
    try {
      String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
      logger.error("put(Registry_Updateable) -- error: {}", text);
    } catch (IOException e) {
      logger.error(
          "put(Registry_Updateable) -- IOException trying to read response entity: {0}", e);
    }
    return null;
  }

  /**
   * delete an Object by its ID number
   *
   * @param c The Class of the FDP Object we're trying to delete
   * @param i the ID of the FDP Object we're trying to delete
   */
  public void delete(Class<? extends Registry_Updateable> c, int i)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    Registry_Updateable o = (Registry_Updateable) get(c, i);
    delete(o);
  }

  /**
   * delete an Object
   *
   * @param o the Object we are trying to delete
   */
  public void delete(Registry_Updateable o)
      throws ConnectException, RegistryVersionException, RegistryJSONException, ForbiddenException,
          RestClientException {
    if (!o.allow_method("DELETE")) {
      throw (new IllegalArgumentException(
          "delete(Registry_Updateable) -- trying to use DELETE on an object that doesn't support DELETE."
              + o));
    }
    if (o.getUrl() == null) {
      throw (new RestClientException(
          "delete(Registry_Updateable) -- can't DELETE an object without a set URL."));
    }
    Response r;
    try {
      r = client.target(o.getUrl().toString()).request(this.getJsonMediaType()).delete();
    } catch (Exception e) {
      deal_with_jakarta_http_exceptions(e);
      return;
    }
    if (r.getStatus() != 204) {
      throw (new RestClientException(
          "delete(Registry_Updateable) -- failed to delete " + o.getUrl()));
    }
  }

  /**
   * Create an APIURL (only for testing, I think)
   *
   * @param c The RegistryXXX class for the resource we are trying to access.
   * @param i the ID of the resource we are creating an APIURL for.
   * @return the APIURL to access the Registry resource.
   */
  public APIURL makeAPIURL(Class<? extends Registry_RootObject> c, int i) {
    WebTarget wt2 =
        wt.path(Registry_RootObject.get_django_path(c.getSimpleName())).path(Integer.toString(i));
    try {
      return new APIURL(wt2.getUri() + "/");
    } catch (URISyntaxException e) {
      logger.error(e.toString());
      return null;
    }
  }

  MediaType getJsonMediaType() {
    return this.jsonWithVersion;
  }
}
