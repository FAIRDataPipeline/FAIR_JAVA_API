package uk.ramp.dataregistry.restclient;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ramp.dataregistry.content.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class test_registry {
    String registry = "http://localhost:8000/api/";
    RestClient lc;
    Map<String, String> m;

    @BeforeAll
    public void setUp() throws Exception {
        this.lc = new RestClient(registry);
        this.m = Collections.emptyMap();
    }

    @ParameterizedTest
    @ValueSource(classes = {Author.class, Code_repo_release.class, Code_run.class, Data_product.class, External_object.class, FDPObject.class, File_type.class, Issue.class, Key_value.class, Keyword.class, Namespace.class, Object_component.class, Storage_location.class, Storage_root.class, Users.class})
    public void get_object(Class c) {
        FDP_RootObject n =  lc.getFirst(c, m);
        Assertions.assertNotNull(n.getUrl());
    }

}
