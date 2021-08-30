package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.fairdatapipeline.dataregistry.content.Registry_RootObject;

class FDP_RootObjectReader implements MessageBodyReader<Registry_RootObject> {

  @Override
  public boolean isReadable(
      Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    Boolean r = Registry_RootObject.class.isAssignableFrom(type);
    return r;
  }

  @Override
  public Registry_RootObject readFrom(
      Class<Registry_RootObject> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());
      return (Registry_RootObject) om.readValue(entityStream, type);
    } catch (Exception e) {
      throw new ProcessingException("Error deserializing ", e);
    }
  }
}
