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
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.fairdatapipeline.dataregistry.content.Registry_ObjectList;
import org.fairdatapipeline.dataregistry.content.Registry_RootObject;
import org.fairdatapipeline.dataregistry.content.Registry_Updateable;
import org.fairdatapipeline.dataregistry.oauth2token.OAuth2ClientSupport;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {
  private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
  private WebTarget wt;
  private Client client;

  private void init(String registry_url, String token) {
    client =
        ClientBuilder.newBuilder()
            .register(FDP_RootObjectReader.class)
            .register(FDP_RootObjectWriter.class)
            .register(FDP_ObjectListReader.class)
            .register(new JavaUtilCollectionsDeserializers() {})
            .register(OAuth2ClientSupport.feature(token))
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .build();
    wt = client.target(registry_url);
  }

  public RestClient(String registry_url) {
    try (java.io.FileReader fileReader = new java.io.FileReader("D:\\SCRCtokenLocal")) {
      init(registry_url, IOUtils.toString(fileReader));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

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
  public Registry_RootObject getFirst(Class<?> c, Map<String, String> m) {
    // c = the class contained within the objectlist
    // return the first item found.
    WebTarget wt2 = wt.path(Registry_RootObject.get_django_path(c.getSimpleName()));

    for (Map.Entry<String, String> e : m.entrySet()) {
      wt2 = wt2.queryParam(e.getKey(), e.getValue());
    }
    // TODO: try retrieving only a single item:
    // wt2 = wt2.queryParam("page_size", 1);
    ParameterizedType p = TypeUtils.parameterize(Registry_ObjectList.class, c);

    Registry_ObjectList<?> o;
    try {
      o = wt2.request(MediaType.APPLICATION_JSON).get(new GenericType<>(p));
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new ConnectException(msg, e));
      } else {
        logger.error("getFirst(Class, Map) -- " + e);
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
    if (o.getCount() == 0) {
      logger.trace("getFirst(" + c.getSimpleName() + ", " + m + ") returned 0 items");
      return null;
    }
    return o.getResults().get(0);
  }

  /**
   * retrieve a list of all FDP registry entries of Class c matching all the constraints in Map m
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param m The key-value constraint pairs
   * @return the Registry_ObjectList containing all items matching the constraints in the Map m
   */
  public Registry_ObjectList<?> getList(Class<?> c, Map<String, String> m) {
    // c is the class contained within the ObjectList
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      String msg = "getList(Class, Map) -- Given Class is not an FDP_RootObject.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    WebTarget wt2 = wt.path(Registry_RootObject.get_django_path(c.getSimpleName()));
    for (Map.Entry<String, String> e : m.entrySet()) {
      wt2 = wt2.queryParam(e.getKey(), e.getValue());
    }
    ParameterizedType p = TypeUtils.parameterize(Registry_ObjectList.class, c);
    GenericType<Registry_ObjectList<?>> gt = new GenericType<>(p);
    try {
      return wt2.request(MediaType.APPLICATION_JSON).get(gt);
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
  }

  /**
   * retrieve the FDP registry entry of Class c with ID i
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param i The ID of the FDP Object we're trying to retrieve
   * @return the FDP Object (or null if not found)
   */
  public Registry_RootObject get(Class<?> c, int i) {
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      String msg = "get(Class, Int) -- Given Class is not an FDP_RootObject.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    WebTarget wt2 =
        wt.path(Registry_RootObject.get_django_path(c.getSimpleName())).path(Integer.toString(i));
    try {
      return (Registry_RootObject) wt2.request(MediaType.APPLICATION_JSON).get(c);
    } catch (NotFoundException e) {
      logger.warn("get(Class, Int) " + e);
      return null;
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
  }

  /**
   * retrieve the FDP registry entry with given APIURL
   *
   * @param c The Class of the FDP Object we're trying to retrieve
   * @param apiurl The APIURL of the Object we are trying to retrieve
   * @return The Object, or null if not found.
   */
  public Registry_RootObject get(Class<?> c, APIURL apiurl) {
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      String msg = "get(Class, URL) -- Given Class is not an FDP_RootObject.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    WebTarget wt2 = client.target(apiurl.toString());
    try {
      return (Registry_RootObject) wt2.request(MediaType.APPLICATION_JSON).get(c);
    } catch (NotFoundException e) {
      logger.warn("get(Class, APIURL) " + e);
      return null;
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
  }

  /**
   * submit the new FDP registry object o; return the created object (with the APIURL, updated_by,
   * last_updated fields filled in by the registry)
   *
   * @param o The FDP Object we are submitting (should have no URL yet)
   * @return the FDP Object we get back from the registry, with its URL set, or null upon error.
   */
  public Registry_Updateable post(Registry_Updateable o) {
    WebTarget wt2 = wt.path(o.get_django_path());
    if (o.getUrl() != null) {
      String msg =
          "post(Registry_Updateable) -- trying to post an already existing object "
              + o.get_django_path()
              + " (object already has an URL entry)";
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
    Response r;
    try {
      r =
          wt2.request(MediaType.APPLICATION_JSON)
              .post(Entity.entity(o, MediaType.APPLICATION_JSON));
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
    if (r.getStatus() != 201) {
      // in case we failed to create the object, log the error message

      InputStream i = (InputStream) r.getEntity();
      try {
        String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
        logger.error("post(Registry_Updateable) -- error: " + text);
      } catch (IOException e) {
        logger.error(
            "post(Registry_Updateable) -- IOException trying to read response entity: " + e);
      }
      return null;
    } else {
      return r.readEntity(o.getClass());
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
  public Registry_Updateable patch(Registry_Updateable o) {
    if (!o.allow_method("PATCH")) {
      String msg =
          "patch(Registry_Updateable) -- trying to use PATCH method on an object that can't be patched."
              + o;
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
    if (o.getUrl() == null) {
      String msg = "patch(Registry_Updateable) -- can't patch an obj without URL";
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }

    Response r;
    try {
      r =
          client
              .target(o.getUrl().toString())
              .request(MediaType.APPLICATION_JSON)
              .build("PATCH", Entity.entity(o, MediaType.APPLICATION_JSON))
              .invoke();
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
    if (r.getStatus() >= 200 && r.getStatus() < 300) return r.readEntity(o.getClass());
    InputStream i = (InputStream) r.getEntity();
    try {
      String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
      logger.error("patch(Registry_Updateable) -- error: " + text);
    } catch (IOException e) {
      logger.error(
          "patch(Registry_Updateable) -- IOException trying to read response entity: " + e);
    }
    return null;
  }

  /**
   * put Object o; this will replace the object at the set URL.
   *
   * @param o the Object to PUT; its URL must be set.
   * @return the Object returned from the registry, or null upon error.
   */
  public Registry_Updateable put(Registry_Updateable o) {
    if (!o.allow_method("PUT")) {
      String msg =
          "put(Registry_Updateable) -- trying to use PUT method on an object that doesn't support PUT."
              + o;
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
    if (o.getUrl() == null) {
      String msg = "put(Registry_Updateable) -- can't PUT an object without URL";
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
    Response r;
    try {
      r =
          client
              .target(o.getUrl().toString())
              .request(MediaType.APPLICATION_JSON)
              .put(Entity.entity(o, MediaType.APPLICATION_JSON));
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
    if (r.getStatus() >= 200 && r.getStatus() < 300) return r.readEntity(o.getClass());
    InputStream i = (InputStream) r.getEntity();
    try {
      String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
      logger.error("put(Registry_Updateable) -- error: " + text);
    } catch (IOException e) {
      logger.error("put(Registry_Updateable) -- IOException trying to read response entity: " + e);
    }
    return null;
  }

  /**
   * delete an Object by its ID number
   *
   * @param c The Class of the FDP Object we're trying to delete
   * @param i the ID of the FDP Object we're trying to delete
   */
  public void delete(Class<?> c, int i) {
    if (!Registry_Updateable.class.isAssignableFrom(
        c)) { // i can only delete updateables; can't delete 'User'.
      String msg =
          "delete(Class, Int) -- Whatever you're trying to delete must be a subclass of Registry_Updateable.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    Registry_Updateable o = (Registry_Updateable) get(c, i);
    delete(o);
  }

  /**
   * delete an Object
   *
   * @param o the Object we are trying to delete
   */
  public void delete(Registry_Updateable o) {
    if (!o.allow_method("DELETE")) {
      String msg =
          "delete(Registry_Updateable) -- trying to use DELETE on an object that doesn't support DELETE."
              + o;
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
    if (o.getUrl() == null) {
      String msg = "delete(Registry_Updateable) -- can't DELETE an object without a set URL.";
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
    Response r;
    try {
      r = client.target(o.getUrl().toString()).request(MediaType.APPLICATION_JSON).delete();
    } catch (ProcessingException e) {
      if (e.getCause().getClass() == java.net.ConnectException.class) {
        String msg = "Can't connect to registry at " + wt + "\nIs the local registry running?";
        logger.error(msg);
        throw (new org.fairdatapipeline.dataregistry.restclient.ConnectException(msg, e));
      } else if (e.getCause()
          .getClass()
          .getCanonicalName()
          .startsWith("com.fasterxml.jackson.databind.exc.")) {
        String msg =
            "Error processing JSON response from registry.\nAre you using the correct registry version?";
        logger.error(msg + "\n" + e);
        throw (new RegistryJSONException(msg, e));
      } else {
        logger.error(e.toString());
        throw (e);
      }
    } catch (jakarta.ws.rs.ForbiddenException e) {
      String msg = "HTTP 403 Forbidden -- is the token wrong?";
      logger.error(msg);
      throw (new ForbiddenException(msg, e));
    }
    if (r.getStatus() != 204) {
      String msg = "delete(Registry_Updateable) -- failed to delete " + o.getUrl();
      logger.error(msg);
      throw (new IllegalArgumentException(msg));
    }
  }

  public APIURL makeAPIURL(Class c, int i) {
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      String msg = "makeAPIURL(Class, Int) -- Given Class is not an FDP_RootObject.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    WebTarget wt2 =
        wt.path(Registry_RootObject.get_django_path(c.getSimpleName())).path(Integer.toString(i));
    try {
      return new APIURL(wt2.getUri() + "/");
    } catch (MalformedURLException e) {
      logger.error(e.toString());
      return null;
    }
  }
}
