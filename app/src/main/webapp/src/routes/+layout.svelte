<script lang="ts">
  import '../app.css';
  import { Navbar } from '$lib/components/navigation';
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { page } from '$app/stores';
  import type { Snippet } from 'svelte';

  let { children }: { children: Snippet } = $props();

  onMount(async () => {
    if ($page.url.pathname === '/') {
      goto('/overview');
    }
  });
</script>

<div class="flex h-screen flex-col bg-gray-50">
  <Navbar />

  <main class="flex-1 overflow-auto">
    <div class="mx-auto max-w-7xl px-6 py-6">
      {@render children()}
    </div>
  </main>
</div>