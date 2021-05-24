package uk.ramp.dataregistry.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import uk.ramp.dataregistry.content.FDP_RootObject;

public class  FDP_RootObjectReader implements MessageBodyReader<FDP_RootObject> {

    @Override
    public boolean isReadable(Class type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
        Boolean r = FDP_RootObject.class.isAssignableFrom(type);
        System.out.println("RootObject.isReadable(" + type.getName() + "): " + r);
        return r;
    }

    @Override
    public FDP_RootObject readFrom(Class<FDP_RootObject> type,
                                   Type genericType,
                                   Annotation[] annotations, MediaType mediaType,
                                   MultivaluedMap<String, String> httpHeaders,
                                   InputStream entityStream)
            throws IOException, WebApplicationException {
        System.out.println("FTR readFrom.. generictype: " + genericType.getTypeName());

        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            return  (FDP_RootObject) om.readValue(entityStream, type);
        } catch (Exception e) {
            throw new ProcessingException("Error deserializing File_type",
                    e);
        }
    }
}
