package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class testPatchAndDelete {
  String localReg = "http://localhost:8000/api/";
  String remoteReg = "https://data.scrc.uk/api/";
  RestClient lc, lcr;
  RegistryCode_run crput, crpatch;

  @BeforeAll
  public void setUp() throws Exception {
    lc = new RestClient(localReg);
    lcr = new RestClient(remoteReg);

    this.crput = new RegistryCode_run();
    this.crput.setDescription("test coderun for PUT");
    this.crput.setRun_date(LocalDateTime.now());
    this.crput.setInputs(List.of(getObject_component().getUrl()));
    RegistryObject o = getObject();
    this.crput.setCode_repo(o.getUrl());
    this.crput.setSubmission_script(o.getUrl());
    this.crput = (RegistryCode_run) lc.post(this.crput);
    if (this.crput == null) throw (new IllegalArgumentException("failed to post a fresh Coderun"));

    this.crpatch = new RegistryCode_run();
    this.crpatch.setDescription("test coderun for PATCH");
    this.crpatch.setRun_date(LocalDateTime.now());
    this.crpatch.setInputs(List.of(getObject_component().getUrl()));
    // RegistryObject o = getObject();
    this.crpatch.setCode_repo(o.getUrl());
    this.crpatch.setSubmission_script(o.getUrl());
    this.crpatch = (RegistryCode_run) lc.post(this.crpatch);
    if (this.crpatch == null)
      throw (new IllegalArgumentException("failed to post a fresh Coderun"));
  }

  @Test
  void putCoderun() {
    crput.setDescription("test coderun for PatchAndDelete after put");
    crput.setInputs(new ArrayList<>());
    RegistryCode_run cr2 = (RegistryCode_run) lc.put(crput);
    Assertions.assertEquals(crput.getUrl(), cr2.getUrl());
    Assertions.assertEquals(crput.getUuid(), cr2.getUuid());
    Assertions.assertEquals(crput.getDescription(), cr2.getDescription());
    // Assertions.assertNull(cr2.getInputs());
    System.out.println("input on cr2 returned from the put method: " + cr2.getInputs().size());
    // the null inputs does not override the existing inputs, so PUT behaves like PATCH
    Assertions.assertEquals(crput.getOutputs().size(), cr2.getOutputs().size());
    Assertions.assertTrue(crput.getOutputs().containsAll(cr2.getOutputs()));

    RegistryCode_run cr3 = (RegistryCode_run) lc.get(RegistryCode_run.class, crput.getUrl());
    Assertions.assertEquals(crput.getUrl(), cr3.getUrl());
    Assertions.assertEquals(crput.getUuid(), cr3.getUuid());
    Assertions.assertEquals(crput.getDescription(), cr3.getDescription());
    Assertions.assertNotEquals(crput.getLast_updated(), cr3.getLast_updated());
    // Assertions.assertNull(cr3.getInputs());
    System.out.println("input on cr3 retrieved after put method: " + cr3.getInputs().size());
    Assertions.assertEquals(crput.getOutputs().size(), cr3.getOutputs().size());
    Assertions.assertTrue(crput.getOutputs().containsAll(cr3.getOutputs()));
  }

  String get_json(Registry_RootObject o) {
    ObjectMapper om = new ObjectMapper();
    JavaTimeModule jtm = new JavaTimeModule();
    jtm.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
    om.registerModule(jtm);
    om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    try {
      return om.writeValueAsString(o);
    } catch (Exception e) {
      return "**FAIL**";
    }
  }

  @Test
  void PutAndPatchCoderun() {
    RegistryCode_run cr2 = new RegistryCode_run();
    cr2.setUrl(crpatch.getUrl());
    cr2.setDescription("changed desc");
    cr2.setInputs(getObject_components_2());
    System.out.println("JSON to patch or put:\n" + get_json(cr2));
    System.out.println("PATCH URL: " + cr2.getUrl());
    lc.patch(cr2);
  }

  @Test
  void delete_object() {
    lc.delete(crput);
  }

  @Test
  void delete_by_id() {
    lc.delete(RegistryCode_run.class, crpatch.get_id());
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

  List<String> getObject_components_2() {
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
      sr.setRoot("http://bikehub.com");
      sr.setLocal(true);
      sr = (RegistryStorage_root) lc.post(sr);
      if (sr == null) throw (new IllegalArgumentException("couldn't post storageRoot"));
    }
    return sr;
  }
}
