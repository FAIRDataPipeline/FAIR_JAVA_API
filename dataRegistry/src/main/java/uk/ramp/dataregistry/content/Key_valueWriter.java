package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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


public class Key_valueWriter implements MessageBodyWriter<Key_value> {
    Class myClass = Key_value.class;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return type == myClass;
    }

    @Override
    public void writeTo(Key_value o, Class<?> type,
                        Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream outStream)
            throws IOException, WebApplicationException {

        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            Writer w = new PrintWriter(outStream);
            w.write(om.writeValueAsString(o));
            w.flush();
            w.close();
        } catch (Exception e) {
            throw new ProcessingException("Error deserializing " + myClass.getSimpleName(),
                    e);
        }
    }
}