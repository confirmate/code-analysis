<script lang="ts">
  import type { DocumentLine } from '$lib/utils/templateSegments';
  import type { Evidence } from '$lib/types/evidence';

  interface Props {
    lines: DocumentLine[];
    onEvidenceClick: (evidence: Evidence, resourceType: string, field: string, target: HTMLElement) => void;
    activeEvidenceField: string | null;
  }

  let { lines, onEvidenceClick, activeEvidenceField = null }: Props = $props();

  function hasBullet(line: DocumentLine): boolean {
    return line.length > 0 && line[0].type === 'bullet';
  }
</script>

<div class="text-[13.5px] leading-[1.8] text-gray-600 tracking-[0.01em]">
  {#each lines as line}
    <div class="min-h-2 {hasBullet(line) ? 'flex gap-2 pl-4' : ''}">
      {#each line as segment}
        {#if segment.type === 'heading'}
          {#if segment.level === 1}
            <h2 class="text-base font-semibold text-gray-900 mt-10 mb-2 tracking-tight">{segment.content}</h2>
          {:else if segment.level === 2}
            <h3 class="text-[13.5px] font-semibold text-gray-800 mt-7 mb-1">{segment.content}</h3>
          {:else}
            <h4 class="text-[13.5px] font-medium text-gray-700 mt-5 mb-1">{segment.content}</h4>
          {/if}
        {:else if segment.type === 'bullet'}
          <span class="text-gray-400 select-none shrink-0">•</span>
        {:else if segment.type === 'text'}
          <span>{segment.content}</span>
        {:else if segment.type === 'evidence'}
          {@const isActive = activeEvidenceField === `${segment.resourceType}.${segment.field}`}
          <button
            type="button"
            class="inline rounded-[4px] px-1 py-0.5 text-[length:inherit] font-medium transition-all duration-150 cursor-pointer
              {isActive
                ? 'bg-indigo-50 text-indigo-800 ring-1 ring-indigo-300'
                : 'bg-gray-50 text-gray-800 ring-1 ring-gray-200 hover:bg-indigo-50/60 hover:text-indigo-700 hover:ring-indigo-200'}"
            onclick={(e) => onEvidenceClick(segment.evidence, segment.resourceType, segment.field, e.currentTarget)}
          >
            {segment.content}
          </button>
        {:else if segment.type === 'placeholder'}
          <span class="inline rounded-[4px] px-1.5 py-0.5 bg-amber-50/80 text-amber-500 ring-1 ring-amber-200 text-[12px] font-medium">
            {segment.content}
          </span>
        {:else if segment.type === 'bold'}
          <span class="font-semibold text-gray-600">{segment.content}</span>
        {/if}
      {/each}
    </div>
  {/each}
</div>