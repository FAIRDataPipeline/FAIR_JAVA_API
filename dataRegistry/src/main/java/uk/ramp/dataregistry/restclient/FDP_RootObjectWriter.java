package uk.ramp.dataregistry.restclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import uk.ramp.dataregistry.content.FDP_RootObject;

class FDP_RootObjectWriter implements MessageBodyWriter<FDP_RootObject> {

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    Boolean r = FDP_RootObject.class.isAssignableFrom(type);
    System.out.println("FDP_RootObjectWriter - isWriteable() -> " + r);
    return r;
  }

  @Override
  public void writeTo(
      FDP_RootObject o,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream outStream)
      throws IOException, WebApplicationException {

    try {
      System.out.println("FDP_RootObjectWriter - writing " + o);
      ObjectMapper om = new ObjectMapper();
      JavaTimeModule jtm = new JavaTimeModule();
      jtm.addSerializer(
          LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
      om.registerModule(jtm);
      om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      Writer w = new PrintWriter(outStream);
      String s = om.writeValueAsString(o);
      System.out.println(s);
      w.write(s);
      w.flush();
      w.close();
    } catch (Exception e) {
      throw new ProcessingException("Error serializing " + type.getSimpleName(), e);
    }
  }
}
