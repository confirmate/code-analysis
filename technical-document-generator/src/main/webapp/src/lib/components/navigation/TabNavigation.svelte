<script lang="ts">
  import { page } from '$app/stores';

  const currentPath = $derived($page.url.pathname);

  interface Tab {
    name: string;
    href: string;
  }

  const tabs: Tab[] = [
    { name: 'Document Generator', href: '/document' }
  ];

  function isActive(tabPath: string): boolean {
    return currentPath === tabPath;
  }
</script>

<div class="mb-6">
  <div class="border-b border-gray-200">
    <nav class="-mb-px flex space-x-2" aria-label="Tabs">
      {#each tabs as tab}
        <a
          href={tab.href}
          class="relative inline-flex items-center px-5 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition-all duration-200 no-underline
            {isActive(tab.href)
              ? 'text-gray-900 font-semibold border-gray-500'
              : 'text-gray-500 border-transparent hover:text-gray-700 hover:border-gray-300'}"
          aria-current={isActive(tab.href) ? 'page' : undefined}
        >
          {tab.name}
        </a>
      {/each}
    </nav>
  </div>
</div>
