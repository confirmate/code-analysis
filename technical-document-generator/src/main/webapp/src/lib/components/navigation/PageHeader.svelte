<script lang="ts">
  interface Props {
    title: string;
    subtitle?: string;
  }

  let { title, subtitle }: Props = $props();
</script>

<div class="bg-white border-b border-gray-200">
  <div class="px-4 py-4 sm:px-6 lg:px-8">
    <div>
      <h1 class="text-xl font-semibold text-gray-900">
        {title}
      </h1>
      {#if subtitle}
        <p class="mt-1 text-sm text-gray-500">{subtitle}</p>
      {/if}
    </div>
  </div>
</div>