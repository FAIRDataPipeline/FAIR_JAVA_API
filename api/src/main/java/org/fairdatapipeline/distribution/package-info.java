/**
 * Use {@link org.fairdatapipeline.distribution.Distribution ImmutableDistribution} to read/write
 * distributions to/from the FAIR Data Pipeline.
 *
 * <p>Immutable usage example:
 *
 * <pre>
 *     gammaDistribution =
 *         ImmutableDistribution.builder()
 *             .internalType(DistributionType.gamma)
 *             .internalShape(1)
 *             .internalScale(2)
 *             .rng(rng)
 *             .build();
 *     object_component.writeDistribution(gammaDistribution);
 * </pre>
 */
package org.fairdatapipeline.distribution;
