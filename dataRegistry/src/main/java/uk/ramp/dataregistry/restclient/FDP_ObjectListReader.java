package uk.ramp.dataregistry.restclient;

import com.fasterxml.jackson.databind.JavaType;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import uk.ramp.dataregistry.content.FDP_ObjectList;

public class FDP_ObjectListReader implements MessageBodyReader<FDP_ObjectList> {

  @Override
  public boolean isReadable(
      Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    System.out.println("OLR isReadable.. type: " + type.getName());
    System.out.println("OLR isReadable.. generictype: " + genericType.getTypeName());
    return true;
  }

  @Override
  public FDP_ObjectList<?> readFrom(
      Class<FDP_ObjectList> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {
    System.out.println("OLR readFrom.. generictype: " + genericType.getTypeName());

    try {
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());
      Type MyTypeParameter = ((ParameterizedType) genericType).getActualTypeArguments()[0];
      JavaType ptype =
          om.getTypeFactory()
              .constructParametricType(FDP_ObjectList.class, (Class) MyTypeParameter);
      return (FDP_ObjectList) om.readValue(entityStream, ptype);
    } catch (Exception e) {
      throw new ProcessingException("Error deserializing FDP_ObjectList", e);
    }
  }
}
