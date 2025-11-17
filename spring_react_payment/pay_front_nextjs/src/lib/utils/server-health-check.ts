export async function checkServerHealth(baseURL: string): Promise<boolean> {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2000);
    
    try {
      const response = await fetch(`${baseURL}/actuator/health`, {
        method: 'GET',
        signal: controller.signal,
        headers: {
          'Accept': 'application/json',
        },
      });
      clearTimeout(timeoutId);
      return response.ok;
    } catch (healthError) {
      clearTimeout(timeoutId);
      
      const optionsController = new AbortController();
      const optionsTimeoutId = setTimeout(() => optionsController.abort(), 2000);
      
      try {
        const optionsResponse = await fetch(`${baseURL}/api/v1/payments/page`, {
          method: 'OPTIONS',
          signal: optionsController.signal,
        });
        clearTimeout(optionsTimeoutId);
        return optionsResponse.status === 200 || optionsResponse.status === 204;
      } catch (optionsError) {
        clearTimeout(optionsTimeoutId);
        return false;
      }
    }
  } catch (error) {
    return false;
  }
}

export async function checkApiEndpoint(baseURL: string, endpoint: string): Promise<boolean> {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);
    
    const response = await fetch(`${baseURL}${endpoint}`, {
      method: 'OPTIONS',
      signal: controller.signal,
      headers: {
        'Origin': window.location.origin,
        'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'X-User-Id,X-User-Role',
      },
    });
    
    clearTimeout(timeoutId);
    return response.status === 200 || response.status === 204;
  } catch (error) {
    return false;
  }
}

