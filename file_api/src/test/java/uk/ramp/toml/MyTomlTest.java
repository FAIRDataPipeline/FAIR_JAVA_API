package uk.ramp.toml;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import uk.ramp.file.CleanableFileChannel;
import uk.ramp.parameters.ParameterDataWriter;
import uk.ramp.parameters.ParameterDataWriterImpl;
import uk.ramp.samples.ImmutableSamples;
import uk.ramp.samples.Samples;

public class MyTomlTest {
  /*
   * it turned out the toml writer closed the filechannel after writing.
   * So here i'm testing writing two components separately by re-opening
   * the filechannel for APPEND
   */
  @Test
  void test() throws IOException {
    Runnable onClose = this::onclose;
    CleanableFileChannel f =
        new CleanableFileChannel(
            FileChannel.open(
                Path.of("D:\\Datastore\\test.toml"),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE),
            onClose);
    RandomGenerator rng = new RandomDataGenerator().getRandomGenerator();
    ParameterDataWriter p = new ParameterDataWriterImpl(new TomlWriter(new TOMLMapper(rng)));
    Samples s1 = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    p.write(f, "pief", s1);
    if (!f.isOpen()) {
      f =
          new CleanableFileChannel(
              FileChannel.open(Path.of("D:\\Datastore\\test.toml"), StandardOpenOption.APPEND),
              onClose);
      System.out.println("reopened the filechannel");
    }
    Samples s2 = ImmutableSamples.builder().addSamples(4, 5, 6).rng(rng).build();
    p.write(f, "pief", s2);
  }

  void onclose() {
    System.out.println("onclose()");
  }
}
