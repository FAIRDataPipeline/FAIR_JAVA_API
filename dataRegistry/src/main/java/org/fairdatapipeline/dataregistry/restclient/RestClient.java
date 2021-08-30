package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.databind.deser.impl.JavaUtilCollectionsDeserializers;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.fairdatapipeline.dataregistry.content.Registry_ObjectList;
import org.fairdatapipeline.dataregistry.content.Registry_RootObject;
import org.fairdatapipeline.dataregistry.content.Registry_Updateable;
import org.fairdatapipeline.dataregistry.oauth2token.OAuth2ClientSupport;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

public class RestClient {
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
    try (java.io.FileReader fileReader = new java.io.FileReader(new File("D:\\SCRCtokenLocal"))) {
      init(registry_url, IOUtils.toString(fileReader));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public RestClient(String registry_url, String token) {
    init(registry_url, token);
  }

  /*
   * retrieve only the first FDP registry entry found when searching for entries of
   * Class c matching all constraints in Map m
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
    Registry_ObjectList<?> o =
        (Registry_ObjectList)
            wt2.request(MediaType.APPLICATION_JSON).get(new GenericType<Registry_ObjectList>(p));
    if (o.getCount() == 0) {
      return null;
    }
    return (Registry_RootObject) o.getResults().get(0);
  }

  /*
   * retrieve a list of all FDP registry entries of Class c matching all the constraints in Map m
   */
  public Registry_ObjectList<?> getList(Class<?> c, Map<String, String> m) {
    // c is the class contained within the ObjectList
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      throw new IllegalArgumentException("Given class is not an FDP_RootObject.");
    }
    WebTarget wt2 = wt.path(Registry_RootObject.get_django_path(c.getSimpleName()));
    for (Map.Entry<String, String> e : m.entrySet()) {
      wt2 = wt2.queryParam(e.getKey(), e.getValue());
    }
    ParameterizedType p = TypeUtils.parameterize(Registry_ObjectList.class, c);
    GenericType<Registry_ObjectList> gt = new GenericType<Registry_ObjectList>(p);
    Registry_ObjectList<?> o =
        (Registry_ObjectList<?>) wt2.request(MediaType.APPLICATION_JSON).get(gt);
    return o;
  }

  /*
   * retrieve the FDP registry entry of Class c with ID i
   */
  public Registry_RootObject get(Class<?> c, int i) {
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      throw new IllegalArgumentException("Given class is not an FDP_RootObject.");
    }
    WebTarget wt2 =
        wt.path(Registry_RootObject.get_django_path(c.getSimpleName())).path(Integer.toString(i));
    try {
      Registry_RootObject o = (Registry_RootObject) wt2.request(MediaType.APPLICATION_JSON).get(c);
      return o;
    } catch (NotFoundException e) {
      return null;
    }
  }

  /*
   * retrieve the FDP registry entry with URI URI
   */
  public Registry_RootObject get(Class<?> c, String URI) {
    if (!Registry_RootObject.class.isAssignableFrom(c)) {
      throw new IllegalArgumentException("Given class is not an FDP_RootObject.");
    }
    WebTarget wt2 = client.target(URI);
    try {
      Registry_RootObject o = (Registry_RootObject) wt2.request(MediaType.APPLICATION_JSON).get(c);
      return o;
    } catch (NotFoundException e) {
      return null;
    }
  }

  /*
   * submit the new FDP registry object o;
   * return the created object (with the URL, updated_by, last_updated fields filled in by the registry)
   */
  public Registry_Updateable post(Registry_Updateable o) {
    WebTarget wt2 = wt.path(o.get_django_path());
    if (o.getUrl() != null)
      throw (new IllegalArgumentException(
          "trying to post an already existing object "
              + o.get_django_path()
              + " (object already has an URL entry)"));
    Response r =
        wt2.request(MediaType.APPLICATION_JSON).post(Entity.entity(o, MediaType.APPLICATION_JSON));
    if (r.getStatus() != 201) {
      // in case we failed to create the object, log the error message
      // TODO: should go to logger
      InputStream i = (InputStream) r.getEntity();
      try {
        String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
        System.out.println(text);
      } catch (IOException e) {
        System.out.println("IOException " + e);
      }
      return null;
    } else {
      return (Registry_Updateable) r.readEntity(o.getClass());
    }
  }

  public Registry_Updateable patch(Registry_Updateable o) {
    if (!o.allow_method("PATCH"))
      throw (new IllegalArgumentException(
          "trying to use PATCH method on an object that can't be patched." + o));
    if (o.getUrl() == null) throw (new IllegalArgumentException("can't patch an obj without URL"));

    Response r =
        client
            .target(o.getUrl())
            .request(MediaType.APPLICATION_JSON)
            .build("PATCH", Entity.entity(o, MediaType.APPLICATION_JSON))
            .invoke();
    if (r.getStatus() >= 200 && r.getStatus() < 300)
      return (Registry_Updateable) r.readEntity(o.getClass());
    InputStream i = (InputStream) r.getEntity();
    try {
      String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
      System.out.println(text);
    } catch (IOException e) {
      System.out.println("IOException " + e);
    }
    return null;
  }

  public Registry_Updateable put(Registry_Updateable o) {
    if (!o.allow_method("PUT"))
      throw (new IllegalArgumentException(
          "trying to use PUT method on an object that doesn't support PUT." + o));
    if (o.getUrl() == null) throw (new IllegalArgumentException("can't PUT an object without URL"));
    System.out.println(Entity.entity(o, MediaType.APPLICATION_JSON).getEncoding());
    Response r =
        client
            .target(o.getUrl())
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(o, MediaType.APPLICATION_JSON));
    if (r.getStatus() >= 200 && r.getStatus() < 300)
      return (Registry_Updateable) r.readEntity(o.getClass());
    InputStream i = (InputStream) r.getEntity();
    try {
      String text = IOUtils.toString(i, StandardCharsets.UTF_8.name());
      System.out.println(text);
    } catch (IOException e) {
      System.out.println("IOException " + e);
    }
    return null;
  }

  public void delete(Class<?> c, int i) {
    if (!Registry_Updateable.class.isAssignableFrom(
        c)) { // i can only delete updateables; can't delete 'User'.
      throw new IllegalArgumentException("Given class is not an Registry_Updateable.");
    }
    Registry_Updateable o = (Registry_Updateable) get(c, i);
    delete(o);
  }

  public void delete(Registry_Updateable o) {
    if (!o.allow_method("DELETE"))
      throw (new IllegalArgumentException(
          "trying to DELETE an object " + o.getClass().getSimpleName()));
    if (o.getUrl() == null)
      throw (new IllegalArgumentException("can't DELETE an object without a set URL."));
    Response r = client.target(o.getUrl()).request(MediaType.APPLICATION_JSON).delete();
    if (r.getStatus() != 204)
      throw (new IllegalArgumentException("failed to delete " + o.getUrl()));
  }
}
