package uk.ramp.dataregistry.content;

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

public class Object_componentReader implements MessageBodyReader<Object_component> {
    Class myClass = Object_component.class;

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
        return type == myClass;
    }

    @Override
    public Object_component readFrom(Class<Object_component> type,
                              Type genericType,
                              Annotation[] annotations, MediaType mediaType,
                              MultivaluedMap<String, String> httpHeaders,
                              InputStream entityStream)
            throws IOException, WebApplicationException {

        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            return (Object_component) om.readValue(entityStream, myClass);
        } catch (Exception e) {
            throw new ProcessingException("Error deserializing " + myClass.getSimpleName(),
                    e);
        }
    }
}
