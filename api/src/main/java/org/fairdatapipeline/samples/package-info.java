/**
 * Use {@link org.fairdatapipeline.samples.Samples ImmutableSamples} to read/write Samples (lists of
 * Numbers) to/from the FAIR Data Pipeline.
 *
 * <p>Immutable Usage Example:
 *
 * <pre>
 *     var samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
 *     object_component.writeSamples(samples);
 * </pre>
 */
package org.fairdatapipeline.samples;
