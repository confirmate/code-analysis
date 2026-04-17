import type { SchemaEvidence, SchemaResource } from '$lib/api/openapi/evidence';

export type Evidence = SchemaEvidence;
export type Resource = SchemaResource;

/** Keys of the Resource*/
export type ResourceType = keyof Resource;

/**
 * Value type for a given resource key, e.g. ResourceData<'product'> = SchemaProduct.
 * Non-null because the format only includes the key that is actually set.
 */
export type ResourceData<K extends ResourceType> = NonNullable<Resource[K]>;

/**
 * Extract the resource type key from an evidence (e.g. "product", "application", "contactPerson").
 * The ontology Resource schema is a oneOf where exactly one key is set.
 */
export function getResourceType(evidence: Evidence): ResourceType | 'unknown' {
  const resource = evidence.resource;
  if (!resource) return 'unknown';
  const key = Object.keys(resource)[0] as ResourceType | undefined;
  return key ?? 'unknown';
}

/** Extract the resource data from an evidence, preserving the specific resource type. */
export function getResourceData(evidence: Evidence): ResourceData<ResourceType> | null {
  const resource = evidence.resource;
  if (!resource) return null;
  const key = getResourceType(evidence);
  if (key === 'unknown') return null;
  return resource[key] ?? null;
}