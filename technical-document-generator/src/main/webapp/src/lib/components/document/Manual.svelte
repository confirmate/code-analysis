<script lang="ts">
  interface Props {
    value: string;
    placeholder: string;
  }
  let { value, placeholder }: Props = $props();
</script>

{#if value}
  <span data-field="filled">{value}</span>
{:else}
  <span
    data-field="missing"
    class="inline rounded-[4px] px-1.5 py-0.5 bg-amber-50/80 text-amber-500 ring-1 ring-amber-200 text-[12px] font-medium"
  >
    {placeholder}
  </span>
{/if}