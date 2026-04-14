<script lang="ts">
  import type { Evidence } from '$lib/types/evidence';
  import { getResourceData } from '$lib/types/evidence';
  import { tick } from 'svelte';

  interface Props {
    evidence: Evidence | null;
    resourceType: string;
    field: string;
    anchor: HTMLElement | null;
    onClose: () => void;
  }

  let { evidence, resourceType, field, anchor, onClose }: Props = $props();

  let popoverEl: HTMLDivElement | undefined = $state();
  let position = $state({ top: 0, left: 0 });
  let openRight = $state(true);

  const resourceData = $derived(evidence ? getResourceData(evidence) : null);

  $effect(() => {
    if (evidence && anchor) {
      tick().then(updatePosition);
    }
  });

  // Close on click outside, reposition on scroll
  $effect(() => {
    if (!evidence) return;

    function handleClick(e: MouseEvent) {
      const target = e.target as Node;
      if (popoverEl && !popoverEl.contains(target) && anchor && !anchor.contains(target)) {
        onClose();
      }
    }

    document.addEventListener('mousedown', handleClick);
    document.addEventListener('scroll', updatePosition, true);

    return () => {
      document.removeEventListener('mousedown', handleClick);
      document.removeEventListener('scroll', updatePosition, true);
    };
  });

  function updatePosition() {
    if (!anchor || !popoverEl) return;

    const anchorRect = anchor.getBoundingClientRect();
    const popoverRect = popoverEl.getBoundingClientRect();
    const viewportW = window.innerWidth;
    const viewportH = window.innerHeight;
    const gap = 12;

    let left = anchorRect.right + gap;
    openRight = true;

    if (left + popoverRect.width > viewportW - 16) {
      left = anchorRect.left - popoverRect.width - gap;
      openRight = false;
    }

    const anchorMidY = anchorRect.top + anchorRect.height / 2;
    let top = anchorMidY - popoverRect.height / 2;

    if (top + popoverRect.height > viewportH - 16) {
      top = viewportH - popoverRect.height - 16;
    }
    if (top < 16) top = 16;
    if (left < 16) left = 16;

    position = { top, left };
  }

  function formatTimestamp(ts: string): string {
    return new Date(ts).toISOString().split('T')[0];
  }

  function formatValue(value: any): string {
    if (value === null || value === undefined) return '—';
    if (typeof value === 'object') return JSON.stringify(value, null, 2);
    return String(value);
  }
</script>

{#if evidence}
  <!-- Popover -->
  <div
    bind:this={popoverEl}
    class="fixed z-50 w-[320px] max-h-[55vh] bg-white rounded-lg shadow-lg ring-1 ring-gray-200 flex flex-col overflow-hidden animate-in"
    style="top: {position.top}px; left: {position.left}px;"
  >
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-2.5 border-b border-gray-100 bg-gray-50/50">
      <div class="flex items-center gap-2">
        <span class="inline-flex items-center px-1.5 py-0.5 rounded bg-gray-800 text-[10px] font-medium text-white tracking-wide">
          {evidence.toolId}
        </span>
        <span class="text-[11px] text-gray-400 font-medium">{resourceType}</span>
      </div>
      <button
        type="button"
        class="p-0.5 text-gray-300 hover:text-gray-500 transition-colors rounded"
        onclick={onClose}
        aria-label="Close"
      >
        <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Meta -->
    <div class="flex gap-4 px-4 py-2 border-b border-gray-50 text-[11px]">
      <div class="flex flex-col">
        <span class="text-gray-400 font-medium">Timestamp</span>
        <span class="text-gray-600 tabular-nums">{formatTimestamp(evidence.timestamp)}</span>
      </div>
      <div class="flex flex-col">
        <span class="text-gray-400 font-medium">Evidence ID</span>
        <span class="text-gray-600 font-mono" title={evidence.id}>{evidence.id.slice(0, 8)}</span>
      </div>
    </div>

    <!-- Properties -->
    <div class="flex-1 overflow-y-auto px-3 py-2.5">
      {#if resourceData}
        <div class="space-y-px">
          {#each Object.entries(resourceData) as [key, value]}
            <div class="rounded-md px-2.5 py-1.5 {key === field ? 'bg-indigo-50' : 'hover:bg-gray-50'} transition-colors">
              <div class="text-[10px] font-medium uppercase tracking-wider {key === field ? 'text-indigo-500' : 'text-gray-300'}">
                {key}
              </div>
              {#if typeof value === 'object' && value !== null}
                <pre class="text-[12px] text-gray-700 whitespace-pre-wrap break-all mt-0.5 leading-relaxed">{JSON.stringify(value, null, 2)}</pre>
              {:else}
                <div class="text-[13px] {key === field ? 'text-indigo-900 font-medium' : 'text-gray-700'} mt-0.5">
                  {formatValue(value)}
                </div>
              {/if}
            </div>
          {/each}
        </div>
      {/if}
    </div>
  </div>
{/if}

<style>
  .animate-in {
    animation: popover-in 120ms ease-out;
  }
  @keyframes popover-in {
    from {
      opacity: 0;
      transform: scale(0.97) translateY(2px);
    }
    to {
      opacity: 1;
      transform: scale(1) translateY(0);
    }
  }
</style>