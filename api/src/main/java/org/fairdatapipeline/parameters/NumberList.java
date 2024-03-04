package org.fairdatapipeline.parameters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.stream.Collectors;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
public interface NumberList extends Component {
  List<Number> numbers();

  @JsonIgnore
  default List<Number> getNumbers() {
    return numbers();
  }

  @Value.Check
  default NumberList avoidHeterogeneous() {
    // count the number of integers in this list:
    int i = (int) numbers().stream().filter((x) -> ((Number) x.intValue()) == x).count();
    if (i != 0 && i < numbers().size()) {
      return ImmutableNumberList.builder()
          .numbers(numbers().stream().map(Number::doubleValue).collect(Collectors.toList()))
          .build();
    }
    return this;
  }
}
