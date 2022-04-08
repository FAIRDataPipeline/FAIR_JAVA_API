package org.fairdatapipeline.file;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CleanableFileChannelIntegrationTest {

  private FileChannel fileChannelReadable;
  private FileChannel fileChannelWritable;
  private AtomicBoolean runnableExecuted;

  @BeforeAll
  public void setUp() throws Exception {
    String parentPath =
        Paths.get(Objects.requireNonNull(getClass().getResource("/config.yaml")).toURI())
            .getParent()
            .toString();
    this.fileChannelReadable =
        FileChannel.open(Path.of(parentPath, "folder/data/parameter/example1.toml"), READ);
    this.fileChannelWritable =
        FileChannel.open(Path.of(parentPath, "folder/data/parameter/example1.toml"), WRITE);
    runnableExecuted = new AtomicBoolean(false);
  }

  @Test
  void testRunnableExecutesOnClose() {
    var cleanableFileChannel =
        new CleanableFileChannel(fileChannelReadable, () -> runnableExecuted.set(true));
    cleanableFileChannel.close();
    assertThat(runnableExecuted).isTrue();
  }

  @Test
  void testOpenForRead() throws IOException {
    var cleanableFileChannel =
        new CleanableFileChannel(fileChannelReadable, () -> runnableExecuted.set(true));
    cleanableFileChannel.read(ByteBuffer.allocate(64));
    assertThat(fileChannelReadable.isOpen()).isTrue();
  }

  @Test
  void testOpenForWrite() throws IOException {
    var cleanableFileChannel =
        new CleanableFileChannel(fileChannelWritable, () -> runnableExecuted.set(true));
    var buffer = ByteBuffer.allocate(64);
    buffer.flip();
    cleanableFileChannel.write(buffer);
    assertThat(fileChannelWritable.isOpen()).isTrue();
  }

  @Test
  void testReadWithWriteFileHandle() {
    var cleanableFileChannel =
        new CleanableFileChannel(fileChannelWritable, () -> runnableExecuted.set(true));
    var bbuffer = ByteBuffer.allocate(64);
    assertThatExceptionOfType(NonReadableChannelException.class)
        .isThrownBy(() -> cleanableFileChannel.read(bbuffer));
  }

  @Test
  void testWriteWithReadFileHandle() {
    var cleanableFileChannel =
        new CleanableFileChannel(fileChannelReadable, () -> runnableExecuted.set(true));
    var bbuffer = ByteBuffer.allocate(64);
    assertThatExceptionOfType(NonWritableChannelException.class)
        .isThrownBy(() -> cleanableFileChannel.write(bbuffer));
  }
}
