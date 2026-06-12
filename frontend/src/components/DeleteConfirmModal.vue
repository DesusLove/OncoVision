<template>
  <div v-if="target" class="modal" @click.self="$emit('cancel')">
    <div class="modal-card">
      <h3>{{ t('del_confirm_title') }}</h3>
      <p>{{ t('del_confirm_msg').replace('{name}', target.fullName || target.patientId) }}</p>
      <div class="modal-actions">
        <button class="btn ghost" @click="$emit('cancel')">{{ t('btn_cancel') }}</button>
        <button class="btn danger-btn" @click="$emit('confirm')">{{ t('btn_delete') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ target: { type: Object, default: null }, t: { type: Function, required: true } });
defineEmits(['cancel', 'confirm']);
</script>
