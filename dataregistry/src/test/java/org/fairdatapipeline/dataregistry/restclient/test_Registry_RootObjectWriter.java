package org.fairdatapipeline.dataregistry.restclient;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class test_Registry_RootObjectWriter {

  public String write_json(Registry_RootObject o, Class<?> type) {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    new Registry_RootObjectWriter().writeTo(o, type, null, null, null, null, ba);
    return ba.toString();
  }

  @Test
  void test_File_type() {
    RegistryFile_type ft = new RegistryFile_type("bla", "blow");
    Assertions.assertEquals(
        "{\"name\":\"bla\",\"extension\":\"blow\"}", write_json(ft, ft.getClass()));
  }

  @Test
  void test_Namespace() throws MalformedURLException {
    RegistryNamespace ns = new RegistryNamespace();
    ns.setName("name");
    ns.setFull_name("Name Potter");
    ns.setWebsite(new URL("http://www.nos.nl/"));
    Assertions.assertEquals(
        "{\"name\":\"name\",\"full_name\":\"Name Potter\",\"website\":\"http://www.nos.nl/\"}",
        write_json(ns, ns.getClass()));
  }

  @Test
  void test_Storage_location() {
    RegistryStorage_location sl = new RegistryStorage_location();
    sl.setPath("/bla/blow");
    sl.setHash("ciouchsduichs");
    sl.setIs_public(true);
    RestClient rc = new RestClient("https://127.0.0.1:8000/api/", "dummy token");
    sl.setStorage_root(rc.makeAPIURL(RegistryStorage_root.class, 1));
    Assertions.assertEquals(
        "{\"path\":\"/bla/blow\",\"hash\":\"ciouchsduichs\",\"storage_root\":\"https://127.0.0.1:8000/api/storage_root/1/\",\"public\":1}",
        write_json(sl, sl.getClass()));
  }
}
