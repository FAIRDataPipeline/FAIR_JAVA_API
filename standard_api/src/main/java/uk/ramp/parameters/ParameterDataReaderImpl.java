package uk.ramp.parameters;

import static com.google.common.base.Charsets.UTF_8;
import static java.nio.channels.Channels.newReader;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import uk.ramp.file.CleanableFileChannel;
import uk.ramp.toml.TomlReader;

public class ParameterDataReaderImpl implements ParameterDataReader {
  private final TomlReader tomlReader;

  public ParameterDataReaderImpl(TomlReader tomlReader) {
    this.tomlReader = tomlReader;
  }

  @Override
  public ReadComponent read(CleanableFileChannel fileChannel, String component) {
    System.out.println("ParameterDataReaderImpl.read()\n\n");
    Map<String, Component> c =
        tomlReader
            .read(newReader(fileChannel, UTF_8), new TypeReference<Components>() {})
            .components();
    System.out.println("Looking for component called '" + component + "'");
    System.out.println("COMPONENTS LENGTH: " + c.size());
    String found_key = c.keySet().stream().findFirst().get();
    String acomponent = "l";
    try {
      acomponent = new String(component.getBytes(StandardCharsets.UTF_8), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      System.out.println("unsupportedEncoding");
    }
    System.out.println(component.getBytes(StandardCharsets.UTF_8).length);
    System.out.println(acomponent.getBytes(StandardCharsets.UTF_8).length);
    System.out.println(found_key.getBytes(StandardCharsets.UTF_8).length);
    System.out.println(component.getBytes().length);
    System.out.println(found_key.getBytes().length);
    System.out.println("c contains key (component): " + c.containsKey(component));
    System.out.println("c contains key (found_key): " + c.containsKey(found_key));
    c.forEach(
        (key, entry) -> {
          System.out.println("KEY: " + key);
          System.out.println("equal? " + component.equals(key));
        });
    ReadComponent rc = c.get(component);
    if (rc == null) {
      System.out.println("get(" + component + ") [component] returns null");
      rc = c.get(found_key);
      if (rc == null) {
        System.out.println("get(" + found_key + ") [found_key] returns null");
      } else {
        System.out.println("get(" + found_key + ") [found_key] returns something");
      }
    }
    return rc;
    // return c.get(component);
    /*return tomlReader
    .read(newReader(fileChannel, UTF_8), new TypeReference<Components>() {})
    .components()
    .get(component);*/
  }
}
