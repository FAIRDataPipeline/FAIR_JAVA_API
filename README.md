# javaDataPipeline

## Java implementation of the FAIR Data Pipeline API


Documentation can be found on https://fairdatapipeline.github.io/


The main class for the FAIR Data Pipeline JAVA API is `Coderun`

Users should initialise this library using a try-with-resources block or ensure that .close() is explicitly closed when the required file handles have been accessed.

## Usage example

```
try (var coderun = new Coderun(configPath, scriptPath)) {
       ImmutableSamples samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
       String dataProduct = "animal/dodo";
       String component1 = "example-samples-dodo1";
       Data_product_write dp = coderun.get_dp_for_write(dataProduct, "toml");
       Object_component_write oc1 = dp.getComponent(component1);
       oc1.raise_issue("something is terribly wrong with this component", 10);
       oc1.writeSamples(samples);
     }
```
