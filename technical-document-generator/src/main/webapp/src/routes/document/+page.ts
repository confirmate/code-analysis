import type { PageLoad } from './$types';
import type { SchemaEvidence as Evidence } from '$lib/api/openapi/evidence';

export const load: PageLoad = async ({ fetch }) => {
  try {
    const response = await fetch('/api/evidences');

    if (response.ok) {
      const data: { evidences: Evidence[] } = await response.json();
      return { evidences: data.evidences };
    }

    return { evidences: [] };
  } catch (error) {
    console.error('Error loading evidences:', error);
    return { evidences: [] };
  }
};