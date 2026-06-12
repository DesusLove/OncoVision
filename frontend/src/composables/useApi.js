/**
 * Tiny wrapper around fetch that surfaces backend error bodies in a
 * useful form. RFC 7807 ProblemDetail responses expose `detail` (or `title`)
 * plus an `errors` map for @Valid validation failures; we splice those into
 * a single Error.message string for the toast layer.
 */
export async function apiCall(path, opts) {
  const res = await fetch(path, opts);
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const head = data && (data.detail || data.title) || `HTTP ${res.status}`;
    const tail = data && data.errors
      ? ' (' + Object.values(data.errors).join(', ') + ')'
      : '';
    throw new Error(head + tail);
  }
  return data;
}
