import type { SchemaEvidence, SchemaResource } from '$lib/api/openapi/evidence';

export type ResourceType = keyof SchemaResource;

export function getResourceType(evidence: SchemaEvidence): ResourceType | 'unknown' {
  const resource = evidence.resource;
  if (!resource) return 'unknown';
  const key = Object.keys(resource)[0] as ResourceType | undefined;
  return key ?? 'unknown';
}

export function getResourceData(evidence: SchemaEvidence): NonNullable<SchemaResource[ResourceType]> | null {
  const resource = evidence.resource;
  if (!resource) return null;
  const key = getResourceType(evidence);
  if (key === 'unknown') return null;
  return resource[key] ?? null;
}