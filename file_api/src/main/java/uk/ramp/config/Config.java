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

  // @JsonProperty("register")
  // List<ImmutableConfigItem> registerItems();

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigItem {
    @JsonProperty
    Optional<String> data_product();

    @JsonProperty
    Optional<String> external_object();

    @JsonProperty
    Optional<ImmutableConfigUseItem> use();

    // @JsonProperty
    // List<ImmutableConfigComponent> components();

    @JsonProperty
    Optional<String> doi_or_unique_name();
    /*
    @JsonProperty
    Optional<String> object();

    @JsonProperty
    Optional<String> description();

    @JsonProperty
    Optional<String> component();

    @JsonProperty
    Optional<String> source_name();

    @JsonProperty
    Optional<String> source_abbreviation();

    @JsonProperty
    Optional<String> source_website();

    @JsonProperty
    Optional<String> root_name();

    @JsonProperty
    Optional<String> root();

    @JsonProperty
    Optional<String> path();

    @JsonProperty
    Optional<String> title();

    @JsonProperty
    Optional<String> unique_name();

    @JsonProperty
    Optional<String> product_name();

    @JsonProperty
    Optional<String> file_type();

    @JsonProperty
    Optional<String> release_date();

    @JsonProperty
    Optional<String> version();

    @JsonProperty
    Optional<Boolean> primary();

    @JsonProperty
    Optional<String> accessibility();*/

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
    Optional<String> component();

    @JsonProperty
    Optional<String> version();

    @JsonProperty
    Optional<String> doi_or_unique_name();

  /*

    @JsonProperty
    Optional<String> cache();

    @JsonProperty
    Optional<String> doi();

    @JsonProperty
    Optional<String> title();

    @JsonProperty
    Optional<String> hash();*/

  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigComponent {
    @JsonProperty
    Optional<String> component();

    @JsonProperty
    Optional<String> description();
  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface WriteItem {
    Optional<ImmutableConfigDataProduct> data_product();

    Optional<ImmutableConfigExternalObject> external_object();

    Optional<ImmutableConfigObject> object();
  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface RegisterItem {
    Optional<ImmutableConfigDataProduct> data_product();

    Optional<ImmutableConfigExternalObject> external_object();

    Optional<ImmutableConfigObject> object();
  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigDataProduct {
    Optional<String> description();

    Optional<String> component();
  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigExternalObject {
    Optional<String> description();

    Optional<String> component();
  }

  @Immutable
  @JsonSerialize
  @JsonDeserialize
  public interface ConfigObject {
    Optional<String> description();

    Optional<String> component();
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
    Optional<String> default_data_store();

    @JsonProperty
    Optional<Boolean> always_copy_to_store();

    @JsonProperty
    Optional<String> local_repo();

    @JsonProperty
    Optional<String> script();
  }
}
