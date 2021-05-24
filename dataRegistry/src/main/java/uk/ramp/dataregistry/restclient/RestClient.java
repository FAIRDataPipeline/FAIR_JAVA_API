package uk.ramp.dataregistry.restclient;

import org.apache.commons.lang3.reflect.TypeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate;
import com.fasterxml.jackson.databind.deser.impl.JavaUtilCollectionsDeserializers;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.core.GenericType;
import org.w3c.dom.Text;
import uk.ramp.dataregistry.oauth2token.OAuth2ClientSupport;
import uk.ramp.dataregistry.content.*;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class RestClient {
    private WebTarget wt;
    private Client client;

    private void init(String registry_url, String token) {
        client = ClientBuilder.newBuilder()
                .register(FDP_RootObjectReader.class)
                .register(FDP_ObjectListReader.class)
                /*.register(AuthorReader.class)
                .register(AuthorWriter.class)
                .register(Code_repo_releaseReader.class)
                .register(Code_repo_releaseWriter.class)
                .register(Code_runReader.class)
                .register(Code_runWriter.class)
                .register(Data_productReader.class)
                .register(Data_productWriter.class)
                .register(External_objectReader.class)
                .register(External_objectWriter.class)
                .register(FDPObjectReader.class)
                .register(FDPObjectWriter.class)
                .register(File_typeWriter.class)
                .register(File_typeReader.class)
                .register(IssueReader.class)
                .register(IssueWriter.class)
                .register(Key_valueReader.class)
                .register(Key_valueWriter.class)
                .register(KeywordReader.class)
                .register(KeywordWriter.class)
                .register(NamespaceReader.class)
                .register(NamespaceWriter.class)
                .register(Object_componentReader.class)
                .register(Object_componentWriter.class)
                .register(SourceReader.class)
                .register(SourceWriter.class)
                .register(Storage_locationReader.class)
                .register(Storage_locationWriter.class)
                .register(Storage_rootReader.class)
                .register(Storage_rootWriter.class)
                .register(Text_fileReader.class)
                .register(Text_fileWriter.class)
                .register(UserReader.class)*/
                .register(new JavaUtilCollectionsDeserializers() {
                })
                .register(OAuth2ClientSupport.feature(token))
                .build();
        wt = client.target(registry_url);
    }
    public RestClient(String registry_url) {
        try (java.io.FileReader fileReader = new java.io.FileReader(new File("D:\\SCRCtokenLocal"))) {
             init(registry_url,IOUtils.toString(fileReader));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public RestClient(String registry_url, String token){
        init(registry_url, token);
    }

    public FDP_RootObject getFirst(Class<?> c, Map<String, String> m) {
        // c = the class contained within the objectlist
        // return the first item found.
        WebTarget wt2 = wt.path(c.getSimpleName().toLowerCase(Locale.ROOT));
        System.out.println("RestClient.get(Class, Map) " + wt2.getUri());
        for ( Map.Entry<String, String> e : m.entrySet() ) {
            wt2.queryParam(e.getKey(), e.getValue());
        }
        String s = (String) wt2.request(MediaType.APPLICATION_JSON).get(String.class);
        System.out.println("get("+ c.getName() + ", " + m + ") --> " + s);
        ParameterizedType p =  TypeUtils.parameterize(FDP_ObjectList.class, c);
        GenericType<?> gt = (GenericType) TypeUtils.parameterize(GenericType.class, p);
        FDP_ObjectList<?> o =  (FDP_ObjectList) wt2.request(MediaType.APPLICATION_JSON).get(gt);

        return (FDP_RootObject) o.getResults().get(0);
    }

    public FDP_ObjectList<?> getList(GenericType<FDP_ObjectList<FDP_RootObject>> c, Map<String, String> m) {
        // c is the parameterized generictype
        System.out.println("RestClient.getlist() - type = " + c.getClass().getSimpleName() + "; " + c.toString());
        System.out.println("rawType: " + c.getRawType());
        System.out.println("class: " + c.getClass());
        System.out.println("type: " + c.getType());
        Type MyTypeParameter = ((ParameterizedType) c.getType()).getActualTypeArguments()[0];
        System.out.println("FDP_ObjectList() - typename: " + MyTypeParameter.getTypeName());
        String my_path = "";
        try {
            my_path = Class.forName(MyTypeParameter.getTypeName()).getSimpleName().toLowerCase(Locale.ROOT);
        }catch (ClassNotFoundException e) {
            System.out.println("class not found exception!");
            return null;
        }
        WebTarget wt2 = wt.path(my_path + "/");
        System.out.println("RestClient.getList(" + my_path + ", " + m + ")");
        System.out.println("RestClient.getList(Class, Map) " + wt2.getUri());
        for ( Map.Entry<String, String> e : m.entrySet() ) {
            wt2.queryParam(e.getKey(), e.getValue());
        }
        String s = (String) wt2.request(MediaType.APPLICATION_JSON).get(String.class);

        FDP_ObjectList<?> o =  (FDP_ObjectList) wt2.request(MediaType.APPLICATION_JSON).get(c);
        return o;
    }

    public FDP_RootObject get(Class<?> c, int i)  {
        //if(c.getSuperclass() != FDP_Object.class && c.getSuperclass().getSuperclass() != FDP_Object.class){
        if(!FDP_RootObject.class.isAssignableFrom(c)) {
            throw new IllegalArgumentException("Given class is not an FDP_Object.");
        }
        WebTarget wt2 = wt.path(c.getSimpleName().toLowerCase(Locale.ROOT));
        WebTarget wt3 = wt2.path(Integer.toString(i));
        System.out.println("get(Class, Int) URI: " + wt3.getUri());
        String s = (String) wt.path(c.getSimpleName().toLowerCase(Locale.ROOT)).path(Integer.toString(i)).request(MediaType.APPLICATION_JSON).get(String.class);
        System.out.println("RestClient.get("+ c.getName() + ", " + i + ") --> " + s);

        FDP_RootObject o =  (FDP_RootObject) wt3.request(MediaType.APPLICATION_JSON).get(c);
        return o;
    }

    public Response post(FDP_Updateable o) {
        Response r = wt.path(o.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "/").request(MediaType.APPLICATION_JSON).post(Entity.entity(o, MediaType.APPLICATION_JSON));
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        try {
            System.out.println("post() " + om.writeValueAsString(o));
        } catch(JsonProcessingException e) {
            System.out.println("post() - JsonProcessingException " + e);
        }
        return r;
    }

    /* put is not allowed
    public void put(FDP_Updateable o) {
        Response r = client.target(o.getUrl()).request(MediaType.APPLICATION_JSON).put(Entity.entity(o, MediaType.APPLICATION_JSON));
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        try {
            System.out.println("put() " + om.writeValueAsString(o));
        } catch(JsonProcessingException e) {
            System.out.println("put() - JsonProcessingException " + e);
        }
        // if status != 201: throw exception?
        System.out.println(r.getStatus());
        System.out.println(r.getStatusInfo());
        System.out.println(r.getEntity());
    }
    */


    /* delete is not allowed
    public void delete(Class<?> c, int i) {
        if(!FDP_Updateable.class.isAssignableFrom(c)) { // i can only delete updateables; can't delete 'User'.
            throw new IllegalArgumentException("Given class is not an FDP_Updateable.");
        }
        try (Response r = wt.path(c.getSimpleName().toLowerCase(Locale.ROOT)).path(Integer.toString(i)).request(MediaType.APPLICATION_JSON).delete()) {
            System.out.println("delete(" + c.getName() + ", " + i + ") - response: " + r.getStatus());
        } catch (Exception e) {
            System.out.println("delete(" + c.getName() + ", " + i + ") - exception: " + e);
        }

    }*/

    /* delete is not allowed
    public void delete(FDP_Updateable o) {
        Response r = client.target(o.getUrl()).request(MediaType.APPLICATION_JSON).delete();
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        try {
            System.out.println("delete() " + om.writeValueAsString(o));
        } catch(JsonProcessingException e) {
            System.out.println("delete() - JsonProcessingException " + e);
        }
        // if status != 201: throw exception?
        System.out.println(r.getStatus());
        System.out.println(r.getStatusInfo());
        System.out.println(r.getEntity());
    }
     */
}
