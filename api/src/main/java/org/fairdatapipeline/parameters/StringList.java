package org.fairdatapipeline.parameters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
public interface StringList extends Component {
  List<String> strings();

  @JsonIgnore
  default List<String> getStrings() {
    return strings();
  }
}
