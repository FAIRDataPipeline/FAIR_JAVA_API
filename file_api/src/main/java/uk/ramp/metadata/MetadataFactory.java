package uk.ramp.metadata;

/*public class MetadataFactory {
  public MetadataSelector metadataSelector(YamlReader yamlReader, String dataDirectory) {
    List<MetadataItem> metadataItems =
        Stream.of(new MetaDataReader(yamlReader, dataDirectory).read())
            .flatMap(Collection::stream)
            .map(i -> i.withDataDirectory(dataDirectory))
            .collect(Collectors.toList());
    return new MatchingMetadataSelector(metadataItems);
  }

  public RunMetadata runMetadata() {
    return new RunMetadata(new TreeMap<>());
  }
}
*/
