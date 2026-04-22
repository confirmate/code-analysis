import type { PageLoad } from './$types';
import type { SchemaEvidence as Evidence } from '$lib/api/openapi/evidence';
import type {
  SchemaListTargetsOfEvaluationResponse as ListTargetsOfEvaluationResponse
} from '$lib/api/openapi/orchestrator';

export const load: PageLoad = async ({ fetch }) => {
  try {
    const response = await fetch('/api/targets-of-evaluation');

    if (response.ok) {
      const data: ListTargetsOfEvaluationResponse = await response.json();
      return { targetsOfEvaluation: data.targetsOfEvaluation ?? [], evidences: [] as Evidence[] };
    }

    return { targetsOfEvaluation: [], evidences: [] as Evidence[] };
  } catch (error) {
    console.error('Error loading targets of evaluation:', error);
    return { targetsOfEvaluation: [], evidences: [] as Evidence[] };
  }
};