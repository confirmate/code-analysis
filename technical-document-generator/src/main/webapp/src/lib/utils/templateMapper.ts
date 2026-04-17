import type { Evidence, ResourceType } from '$lib/types/evidence';
import { getResourceType } from '$lib/types/evidence';

export interface ManufacturerInfo {
  name: string;
  tradeName: string;
  postalAddress: string;
  generalEmail: string;
  securityEmail: string;
  website: string;
  securityPortalUrl: string;
  model: string;
  automaticUpdateMethod: string;
  dataRemovalMethod: string;
  disableUpdatesPath: string;
  integrationDocUrl: string;
}

export function emptyManufacturerInfo(): ManufacturerInfo {
  return {
    name: '',
    tradeName: '',
    postalAddress: '',
    generalEmail: '',
    securityEmail: '',
    website: '',
    securityPortalUrl: '',
    model: '',
    automaticUpdateMethod: '',
    dataRemovalMethod: '',
    disableUpdatesPath: '',
    integrationDocUrl: ''
  };
}

/** Find the first evidence whose resource is of the given type. */
export function findEvidenceByType<K extends ResourceType>(
  evidences: Evidence[],
  type: K
): Evidence | null {
  return evidences.find((e) => getResourceType(e) === type) ?? null;
}
