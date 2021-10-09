/**
 * Use {@link org.fairdatapipeline.estimate.Estimate ImmutableEstimate} to read/write simple number
 * parameters to/from the FAIR Data Pipeline.
 *
 * <p>Immutable Usage Example:
 *
 * <pre>
 *     var data = ImmutableEstimate.builder().internalValue(5).rng(rng).build();
 *     object_component.writeEstimate(data);
 * </pre>
 */
package org.fairdatapipeline.estimate;
