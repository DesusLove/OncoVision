import { createApp } from 'vue';
import App from './App.vue';
import { router } from './router.js';

const app = createApp(App);

// Global error handler. A render error in any view would otherwise
// crash the whole app; with this handler Vue logs the error and the
// app keeps running. In production you might forward to an error
// tracking service here.
app.config.errorHandler = (err, instance, info) => {
  // eslint-disable-next-line no-console
  console.error('[OncoVision] Unhandled error:', err, '\nInfo:', info);
  // Render a fallback so the user sees something instead of a blank page.
  const root = document.getElementById('app');
  if (root && root.childElementCount === 0) {
    root.innerHTML = `
      <div style="display:flex;align-items:center;justify-content:center;height:100vh;font-family:Inter,system-ui,sans-serif;padding:20px;text-align:center;color:#0F172A">
        <div>
          <h2 style="margin:0 0 8px;font-weight:600">Something went wrong</h2>
          <p style="margin:0;color:#64748B">Please refresh the page to continue.</p>
        </div>
      </div>`;
  }
};

app.use(router).mount('#app');
