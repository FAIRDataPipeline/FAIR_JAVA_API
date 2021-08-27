package org.fairdatapipeline.parameters;

import static java.nio.channels.Channels.newWriter;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.toml.TomlWriter;

public class ParameterDataWriterImpl implements ParameterDataWriter {
  private final TomlWriter tomlWriter;

  public ParameterDataWriterImpl(TomlWriter tomlWriter) {
    this.tomlWriter = tomlWriter;
  }

  @Override
  public void write(CleanableFileChannel fileChannel, String component, Component data) {
    System.out.println("component: " + component);
    try {
      fileChannel.position(fileChannel.size());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Components components = ImmutableComponents.builder().putComponents(component, data).build();
    tomlWriter.write(newWriter(fileChannel, Charsets.UTF_8), components);
  }
}
