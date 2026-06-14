/**
 * Tiny wrapper around fetch that surfaces backend error bodies in a
 * useful form. RFC 7807 ProblemDetail responses expose `detail` (or `title`)
 * plus an `errors` map for @Valid validation failures; we splice those into
 * a single Error.message string for the toast layer.
 *
 * `credentials: 'include'` is the default so session cookies set by
 * Spring Security are sent on every request. JSON requests add the
 * Content-Type header automatically.
 */
export async function apiCall(path, opts = {}) {
  const headers = { ...(opts.headers || {}) };
  let body = opts.body;
  if (body && typeof body === 'object' && !(body instanceof FormData) && !(body instanceof URLSearchParams)) {
    headers['Content-Type'] = headers['Content-Type'] || 'application/json';
    body = JSON.stringify(body);
  }
  const res = await fetch(path, {
    method: opts.method || 'GET',
    credentials: 'include',
    headers,
    body,
  });
  // For 204 / non-JSON responses, return undefined rather than throwing
  // on res.json().
  if (res.status === 204) return undefined;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const head = data && (data.detail || data.title) || `HTTP ${res.status}`;
    const tail = data && data.errors
      ? ' (' + Object.values(data.errors).join(', ') + ')'
      : '';
    const err = new Error(head + tail);
    err.status = res.status;
    throw err;
  }
  return data;
}

/** Download a file from the API (returns the Response so the caller
 *  can stream it to a blob). Adds credentials so the session cookie
 *  travels with the request. */
export async function apiDownload(path) {
  const res = await fetch(path, { credentials: 'include' });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status} downloading ${path}`);
  }
  return res;
}
