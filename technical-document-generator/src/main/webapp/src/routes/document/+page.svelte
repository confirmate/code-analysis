<script lang="ts">
  import type { PageData } from './$types';
  import PageHeader from '$lib/components/navigation/PageHeader.svelte';
  import TabNavigation from '$lib/components/navigation/TabNavigation.svelte';
  import DocumentBody from '$lib/components/document/DocumentBody.svelte';
  import EvidencePopover from '$lib/components/document/EvidencePopover.svelte';
  import { emptyManufacturerInfo } from '$lib/utils/templateMapper';

  let { data }: { data: PageData } = $props();

  let manufacturer = $state(emptyManufacturerInfo());
  let showManufacturerForm = $state(false);

  let docHtmlElement: HTMLDivElement | undefined = $state();
  let fieldCount = $state({ total: 0, filled: 0 });

  $effect(() => {
    if (!docHtmlElement) return;
    const recount = () => {
      const filled = docHtmlElement!.querySelectorAll('[data-field="filled"]').length;
      const missing = docHtmlElement!.querySelectorAll('[data-field="missing"]').length;
      fieldCount = { total: filled + missing, filled };
    };
    recount();
    const observer = new MutationObserver(recount);
    observer.observe(docHtmlElement, { childList: true, subtree: true, attributes: true, attributeFilter: ['data-field'] });
    return () => observer.disconnect();
  });

  const progressPercent = $derived(
    fieldCount.total > 0 ? Math.round((fieldCount.filled / fieldCount.total) * 100) : 0
  );
</script>

<PageHeader
  title="Document Generator"
  subtitle="CRA-compliant technical documentation"
/>

<TabNavigation />

<!-- Progress bar -->
<div class="mb-6 bg-white shadow-sm rounded-lg border border-gray-100 px-5 py-4">
  <div class="flex items-center justify-between mb-2.5">
    <span class="text-[13px] font-medium text-gray-600">Field completion</span>
    <span class="text-[13px] tabular-nums text-gray-400">{fieldCount.filled} / {fieldCount.total} ({progressPercent}%)</span>
  </div>
  <div class="w-full bg-gray-100 rounded-full h-1.5">
    <div
      class="h-1.5 rounded-full transition-all duration-500 ease-out {progressPercent === 100 ? 'bg-emerald-500' : 'bg-gray-800'}"
      style="width: {progressPercent}%"
    ></div>
  </div>
  <div class="flex items-center gap-5 mt-3 text-[11px] text-gray-400">
    <span class="flex items-center gap-1.5">
      <span class="inline-block w-2.5 h-2.5 rounded-[3px] bg-gray-100 ring-1 ring-gray-200"></span>
      Evidence value
    </span>
    <span class="flex items-center gap-1.5">
      <span class="inline-block w-2.5 h-2.5 rounded-[3px] bg-amber-50 ring-1 ring-dashed ring-amber-300"></span>
      Missing
    </span>
  </div>
</div>

<!-- Manufacturer Information -->
<div class="mb-6 bg-white shadow rounded-lg">
  <button
    class="w-full flex items-center justify-between px-6 py-4 text-left"
    onclick={() => (showManufacturerForm = !showManufacturerForm)}
  >
    <div class="flex items-center gap-2">
      <svg
        class="w-4 h-4 text-gray-500 transition-transform duration-200 {showManufacturerForm ? 'rotate-90' : ''}"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
      <span class="text-sm font-semibold text-gray-700">Manufacturer Information</span>
    </div>
    <span class="text-xs text-gray-400">Manual input — not available from evidences</span>
  </button>

  {#if showManufacturerForm}
    <div class="px-6 pb-5 border-t border-gray-100">
      <form class="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-4" autocomplete="off">
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Manufacturer name</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="Your Company" bind:value={manufacturer.name} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Trade name / trademark</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="Trade Name" bind:value={manufacturer.tradeName} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Postal address</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="123 Main Street, City" bind:value={manufacturer.postalAddress} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">General contact email</span>
          <input type="email" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="info@example.com" bind:value={manufacturer.generalEmail} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Security contact email</span>
          <input type="email" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="security@example.com" bind:value={manufacturer.securityEmail} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Website</span>
          <input type="url" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="https://example.com" bind:value={manufacturer.website} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Alternative reporting channel</span>
          <input type="url" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" placeholder="https://security.example.com" bind:value={manufacturer.securityPortalUrl} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Model</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" bind:value={manufacturer.model} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Update installation method</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" bind:value={manufacturer.automaticUpdateMethod} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Data removal method</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" bind:value={manufacturer.dataRemovalMethod} />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-gray-700">Disable auto-updates path</span>
          <input type="text" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" bind:value={manufacturer.disableUpdatesPath} />
        </label>
        <label class="flex flex-col gap-1.5 sm:col-span-2">
          <span class="text-sm font-medium text-gray-700">Integration documentation URL</span>
          <input type="url" class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" bind:value={manufacturer.integrationDocUrl} />
        </label>
      </form>
    </div>
  {/if}
</div>

<!-- Document -->
<div bind:this={docHtmlElement} class="bg-white shadow-sm rounded-lg border border-gray-100 px-10 py-8">
  <DocumentBody evidences={data.evidences} {manufacturer} />
</div>

<EvidencePopover />