package uk.ramp.overrides;

// import uk.ramp.metadata.ImmutableMetadataItem;
// import uk.ramp.metadata.MetadataItem;

/*
public class OverridesApplier {
  private final Config config;

  public OverridesApplier(Config config) {
    this.config = config;
  }

  public MetadataItem applyWriteOverrides(MetadataItem query) {
    OverrideItem runIdOverride =
        ImmutableOverrideItem.builder().use(ImmutableMetadataItem.builder().build()).build();
    /*Supplier<String> generatedFilename =
    () ->
        Paths.get(
                query.dataProduct().orElseThrow(),
                query.runId().orElse(config.runId().orElseThrow())
                    + "."
                    + query.extension().orElseThrow())
            .toString();*/
  /*  OverrideItem filenameOverride =
          ImmutableOverrideItem.builder()
              .use(
                  ImmutableMetadataItem.builder()
                      // .internalFilename(query.internalFilename().orElseGet(generatedFilename))
                      .internalFilename(query.internalFilename())
                      .build())
              .build();

      OverrideItem dataDirectoryOverride =
          ImmutableOverrideItem.builder()
              .use(
                  ImmutableMetadataItem.builder()
                      .dataDirectory(config.normalisedDataDirectory())
                      .build())
              .build();

      return query
          .applyOverrides(new ArrayList<>(config.writeQueryOverrides()))
          .applyOverrides(List.of(runIdOverride))
          .applyOverridesIfEmpty(List.of(filenameOverride))
          .applyOverrides(List.of(dataDirectoryOverride));
    }

    public MetadataItem applyReadOverrides(MetadataItem query) {
      OverrideItem dataDirectoryOverride =
          ImmutableOverrideItem.builder()
              .use(
                  ImmutableMetadataItem.builder()
                      .dataDirectory(config.normalisedDataDirectory())
                      .build())
              .build();

      return query
          .applyOverrides(new ArrayList<>(config.readQueryOverrides()))
          .applyOverrides(List.of(dataDirectoryOverride));
    }
  }
  */
