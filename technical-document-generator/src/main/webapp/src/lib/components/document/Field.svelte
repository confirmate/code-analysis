<script lang="ts">
  import type { Evidence } from '$lib/types/evidence';
  import { getResourceType, getResourceData } from '$lib/types/evidence';
  import { openPopover, popover } from './popoverStore.svelte';

  interface Props {
    evidence: Evidence | null;
    field: string;
    placeholder: string;
    format?: (value: unknown) => string;
  }

  let { evidence, field, placeholder, format }: Props = $props();

  const resourceType = $derived(evidence ? getResourceType(evidence) : '');
  const rawValue = $derived(evidence ? (getResourceData(evidence) as Record<string, unknown> | null)?.[field] : undefined);
  const display = $derived(
    rawValue !== undefined && rawValue !== null && rawValue !== ''
      ? format
        ? format(rawValue)
        : String(rawValue)
      : null
  );
  const isActive = $derived(
    popover.evidence === evidence &&
      popover.resourceType === resourceType &&
      popover.field === field
  );

  function onClick(e: MouseEvent) {
    if (!evidence) return;
    openPopover(evidence, resourceType, field, e.currentTarget as HTMLElement);
  }
</script>

{#if evidence && display !== null}
  <button
    type="button"
    data-field="filled"
    class="inline rounded-[4px] px-1 py-0.5 text-[length:inherit] font-medium transition-all duration-150 cursor-pointer
      {isActive
        ? 'bg-indigo-50 text-indigo-800 ring-1 ring-indigo-300'
        : 'bg-gray-50 text-gray-800 ring-1 ring-gray-200 hover:bg-indigo-50/60 hover:text-indigo-700 hover:ring-indigo-200'}"
    onclick={onClick}
  >
    {display}
  </button>
{:else}
  <span
    data-field="missing"
    class="inline rounded-[4px] px-1.5 py-0.5 bg-amber-50/80 text-amber-500 ring-1 ring-amber-200 text-[12px] font-medium"
  >
    {placeholder}
  </span>
{/if}