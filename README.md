# javaDataPipeline
![test with reg](https://github.com/FAIRDataPipeline/javaDataPipeline/actions/workflows/build-test-with-registry.yml/badge.svg)
![partial test](https://github.com/FAIRDataPipeline/javaDataPipeline/actions/workflows/build-test.yml/badge.svg)
[![License: GPL-3.0](https://img.shields.io/badge/licence-GPL--3-yellow)](https://opensource.org/licenses/GPL-3.0)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5562079.svg)](https://doi.org/10.5281/zenodo.5562079)


## Java implementation of the FAIR Data Pipeline API


Documentation can be found on https://fairdatapipeline.org/docs/API/Java/

JavaDocs are automatically published on https://fairdatapipeline.org/javaDataPipeline/

To use a release version (latest: 1.0.0-beta) include the following dependency (available from mavenCentral):

```gradle
group: 'org.fairdatapipeline',
name: 'api',
version: '1.0.0-beta'
```

## javaSimpleModel

Please have a look at https://github.com/FAIRDataPipeline/javaSimpleModel
for a simple example on how to use the javaDataPipeline, including an example of the <a href="https://www.fairdatapipeline.org/docs/interface/config/">user written config.yaml</a>, 
and how to integrate it with the <a href="https://www.fairdatapipeline.org/docs/interface/fdp/">FAIR CLI</a> command line interface.

## What does it implement?

* It reads the *config.yaml* that is re-written by <a href="https://www.fairdatapipeline.org/docs/interface/fdp/">FAIR CLI</a> (not to be confused with the <a href="https://www.fairdatapipeline.org/docs/interface/config/">user written config.yaml</a>)
* It registers a model Coderun session in the <a href="https://www.fairdatapipeline.org/docs/data_registry/">FAIR Data Registry</a>, including all the data inputs and/or outputs, from the model, and its code repository, its config.yaml, and its submission (startup) script.
* It allows the Coderun session to raise Issues with Object_components, codeRepo, config file, and submission script, and these Issues are registered in the Data Registry.
* It allows reading and writing of Data_products whose Object contains either 1 whole_object unnamed Object_component (for reading and writing 'external' whole-file file formats). 
* It allows reading and writing of Data_products containing a FAIR Data Pipeline (TOML) Parameter File. 
* It does not yet support HDF5 files.
