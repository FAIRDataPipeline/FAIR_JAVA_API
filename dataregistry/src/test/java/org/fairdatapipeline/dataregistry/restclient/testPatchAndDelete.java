package org.fairdatapipeline.dataregistry.restclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "LOCALREG", matches = "FRESHASADAISY")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class testPatchAndDelete {
  String localReg = "http://localhost:8000/api/";
  RestClient lc;
  RegistryCode_run cr;

  @BeforeAll
  public void setUp() {
    lc = new RestClient(localReg);
  }

  private void fresh_cr() {
    this.cr = new RegistryCode_run();
    this.cr.setDescription("test code_run for put patch delete");
    this.cr.setRun_date(LocalDateTime.now());
    this.cr.setInputs(List.of(getObject_component().getUrl()));
    this.cr.setOutputs(List.of(getObject_component().getUrl()));
    RegistryObject o = getObject();
    this.cr.setCode_repo(o.getUrl());
    this.cr.setSubmission_script(o.getUrl());
    this.cr = (RegistryCode_run) lc.post(this.cr);
    if (this.cr == null)
      throw (new IllegalArgumentException("failed to post a fresh RegistryCode_run"));
  }

  @Test
  @Order(1)
  void putCode_run() {
    fresh_cr();
    RegistryCode_run cr2 = new RegistryCode_run();
    cr2.setUrl(cr.getUrl());
    cr2.setRun_date(LocalDateTime.now());
    cr2.setSubmission_script(getObject().getUrl());
    cr2.setDescription("changed desc for put");
    cr2.setInputs(getObject_components_2());
    RegistryCode_run cr3 = (RegistryCode_run) lc.put(cr2);
    assertThat(
        cr3,
        samePropertyValuesAs(
            cr2,
            "last_updated",
            "inputs",
            "outputs",
            "code_repo",
            "prov_report",
            "run_date",
            "updated_by",
            "uuid"));
    Assertions.assertEquals(
        cr3.getCode_repo(), cr.getCode_repo()); // this indicates PATCH behaviour.
    Assertions.assertEquals(
        cr3.getInputs().size(), cr2.getInputs().size()); // inputs have been over-written
    Assertions.assertEquals(
        cr3.getOutputs().size(),
        cr.getOutputs().size()); // outputs have not. this indicates PATCH behaviour.
    //
  }

  @Test
  @Order(2)
  void patchCode_run() {
    fresh_cr();
    RegistryCode_run cr2 = new RegistryCode_run();
    cr2.setUrl(cr.getUrl());
    cr2.setRun_date(LocalDateTime.now());
    cr2.setSubmission_script(getObject().getUrl());
    cr2.setDescription("changed desc for patch");
    cr2.setInputs(getObject_components_2());
    RegistryCode_run cr3 = (RegistryCode_run) lc.patch(cr2);
    assertThat(
        cr3,
        samePropertyValuesAs(
            cr2,
            "last_updated",
            "inputs",
            "outputs",
            "code_repo",
            "prov_report",
            "run_date",
            "updated_by",
            "uuid"));
    Assertions.assertEquals(
        cr3.getCode_repo(), cr.getCode_repo()); // this indicates PATCH behaviour.
    Assertions.assertEquals(
        cr3.getInputs().size(), cr2.getInputs().size()); // inputs have been over-written
    Assertions.assertEquals(
        cr3.getOutputs().size(),
        cr.getOutputs().size()); // outputs have not. this indicates PATCH behaviour.
  }

  @Test
  @Order(3)
  void delete_object() {
    fresh_cr();
    URL url = cr.getUrl();
    lc.delete(cr);
    Assertions.assertNull(lc.get(RegistryCode_run.class, url));
  }

  @Test
  @Order(4)
  void delete_by_id() {
    fresh_cr();
    URL url = cr.getUrl();
    lc.delete(RegistryCode_run.class, cr.get_id());
    Assertions.assertNull(lc.get(RegistryCode_run.class, url));
  }

  RegistryObject_component getObject_component() {
    RegistryObject_component oc =
        (RegistryObject_component)
            lc.getFirst(RegistryObject_component.class, Collections.emptyMap());
    if (oc == null) {
      getObject(); // this should create an object as well as an object component!
      oc =
          (RegistryObject_component)
              lc.getFirst(RegistryObject_component.class, Collections.emptyMap());
      if (oc == null)
        throw (new IllegalArgumentException(
            "failed to find object_component, even after creating an object"));
    }
    return oc;
  }

  List<URL> getObject_components_2() {
    RegistryObject o = getObject();
    RegistryObject_component wo =
        (RegistryObject_component)
            lc.getFirst(
                RegistryObject_component.class, Collections.singletonMap("whole_object", "true"));
    RegistryObject_component nowo =
        (RegistryObject_component)
            lc.getFirst(
                RegistryObject_component.class, Collections.singletonMap("whole_object", "false"));
    if (nowo == null) {
      RegistryObject_component oc = new RegistryObject_component();
      oc.setObject(o.getUrl());
      oc.setDescription("an extra oc");
      oc.setName("some measurement");
      nowo = (RegistryObject_component) lc.post(oc);
    }
    return List.of(wo.getUrl(), nowo.getUrl());
  }

  RegistryObject getObject() {
    RegistryObject o = (RegistryObject) lc.getFirst(RegistryObject.class, Collections.emptyMap());
    if (o == null) {
      o = new RegistryObject();
      o.setDescription("one random object");
      o.setStorage_location(getStorage_location().getUrl());
      o = (RegistryObject) lc.post(o);
      if (o == null) throw (new IllegalArgumentException("couldn't post Object"));
    }
    return o;
  }

  RegistryStorage_location getStorage_location() {
    RegistryStorage_location sl =
        (RegistryStorage_location)
            lc.getFirst(RegistryStorage_location.class, Collections.emptyMap());
    if (sl == null) {
      sl = new RegistryStorage_location();
      sl.setHash("70af0e7749896a9aadf7c999ca26b59c7bcf69af");
      sl.setPath("this/is/where/we/keep/the/bikes.txt");
      sl.setStorage_root(getStorage_root().getUrl());
      sl = (RegistryStorage_location) lc.post(sl);
      if (sl == null) throw (new IllegalArgumentException("couldn't post Storage_location"));
    }
    return sl;
  }

  RegistryStorage_root getStorage_root() {
    RegistryStorage_root sr =
        (RegistryStorage_root) lc.getFirst(RegistryStorage_root.class, Collections.emptyMap());
    if (sr == null) {
      sr = new RegistryStorage_root();
      try {
        sr.setRoot(new URI("http://bikehub.com"));
      } catch (URISyntaxException e) {

      }
      sr.setLocal(true);
      sr = (RegistryStorage_root) lc.post(sr);
      if (sr == null) throw (new IllegalArgumentException("couldn't post storageRoot"));
    }
    return sr;
  }
}
