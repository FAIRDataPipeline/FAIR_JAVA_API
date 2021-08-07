package uk.ramp.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
public interface Config {

  @JsonProperty("fail_on_hash_mismatch")
  Optional<Boolean> internalFailOnHashMisMatch();

  @Derived
  @JsonIgnore
  default boolean failOnHashMisMatch() {
    return internalFailOnHashMisMatch().orElse(true);
  }

  @JsonProperty("run_metadata")
  ImmutableConfigRunMetadata run_metadata();

  @JsonProperty("read")
  List<ImmutableConfigItem> readItems();

  @JsonProperty("write")
  List<ImmutableConfigItem> writeItems();

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigItem {
    @JsonProperty
    String data_product();

    Optional<String> description();

    @JsonProperty
    ImmutableConfigUseItem use();

    @JsonProperty
    Optional<String> file_type();

  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigUseItem {
    @JsonProperty
    Optional<String> namespace();

    @JsonProperty
    Optional<String> data_product();

    @JsonProperty
    String version();
  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigRunMetadata {
    @JsonProperty
    Optional<String> description();

    @JsonProperty
    Optional<String> local_data_registry_url();

    @JsonProperty
    Optional<String> remote_data_registry_url();

    @JsonProperty
    Optional<String> default_input_namespace();

    @JsonProperty
    Optional<String> default_output_namespace();

    @JsonProperty
    Optional<String> write_data_store();

    @JsonProperty
    Optional<String> local_repo();

    @JsonProperty
    Optional<String> script_path();
  }
}
