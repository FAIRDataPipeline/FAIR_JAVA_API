package uk.ramp.metadata;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@Ignore
public class MetadataItemVersionComparisonTest {
  @Parameterized.Parameter(0)
  public String v1;

  @Parameterized.Parameter(1)
  public String v2;

  @Parameterized.Parameters(name = "{index}: Test with v1={0}, v2 ={1}")
  public static Collection<Object[]> data() {
    Object[][] data =
        new Object[][] {
          {"1.0.0", "2.0.0"},
          {"0.1.0", "0.2.0"},
          {"0.0.1", "0.0.2"},
          {"1.0.0", "11.0.0"},
          {"1.9", "1.10"},
          {"0.0.1", "1"},
          {"0", "0.1"},
          {"0", "0.0.1"}
        };
    return Arrays.asList(data);
  }
}
