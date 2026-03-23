export async function requestJson(url, options = {}) {
	const { method = 'GET', body, allowAnonymous = false } = options;
	const response = await fetch(url, {
		method,
		headers: body ? { 'Content-Type': 'application/json' } : {},
		body
	});

	if (allowAnonymous && response.status === 401) {
		return { authenticated: false };
	}

	if (!response.ok) {
		let message = `Request failed with ${response.status}`;
		try {
			message = await response.text();
		} catch {
			
		}
		throw new Error(message || `Request failed with ${response.status}`);
	}

	return response.json();
}