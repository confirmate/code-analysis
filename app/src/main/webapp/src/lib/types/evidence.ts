export interface Evidence {
  id: string;
  timestamp: string;
  targetOfEvaluationId: string;
  toolId: string;
  resource: Record<string, Record<string, any>>;
}

export interface EvidenceWrapper {
  evidence: Evidence;
}

export interface EvidencesResponse {
  evidences: EvidenceWrapper[];
}

/**
 * Extract the resource type key from an evidence (e.g. "product", "application", "contactPerson").
 */
export function getResourceType(evidence: Evidence): string {
  return Object.keys(evidence.resource)[0] ?? 'unknown';
}

/**
 * Extract the resource data from an evidence.
 */
export function getResourceData(evidence: Evidence): Record<string, any> {
  const type = getResourceType(evidence);
  return evidence.resource[type] ?? {};
}