import { mockEvidences } from '$lib/data/mockEvidences';
import type { Evidence } from '$lib/types/evidence';

export function load() {
  // TODO: Replace with actual API call to Evidence Store
  // GET /v1/evidence_store/evidences?filter.targetOfEvaluationId=...
  const evidences: Evidence[] = mockEvidences.evidences.map((w) => w.evidence);

  return {
    evidences
  };
}