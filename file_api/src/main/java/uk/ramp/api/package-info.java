/**
 * The main class for the FAIR data pipeline JAVA API is {@link uk.ramp.api.FileApi}
 *
 * <p>
 *     <b>Usage example</b>
 *     <blockquote><pre>
 *    try (var fileApi = new FileApi(configPath, scriptPath)) {
 *       ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
 *       String dataProduct = "animal/dodo";
 *       String component1 = "example-samples-dodo1";
 *       Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
 *       Object_component_write oc1 = dp.getComponent(component1);
 *       oc1.raise_issue("something is terribly wrong with this component", 10);
 *       oc1.writeSamples(samples);
 *     }
 *     </pre></blockquote>
 */
package uk.ramp.api;