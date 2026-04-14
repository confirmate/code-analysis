import type { PageLoad } from './$types';

export const load: PageLoad = async ({ parent }) => {
  const { evidences } = await parent();
  return { evidences };
};