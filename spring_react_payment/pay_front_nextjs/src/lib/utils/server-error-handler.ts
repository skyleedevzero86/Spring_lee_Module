/**
 * Server-side error handler for Windows-specific Next.js file access issues
 * This file should be imported early in the server process
 */

if (typeof window === 'undefined' && process.platform === 'win32') {
  const originalConsoleError = console.error;
  
  console.error = (...args: any[]) => {
    const errorMessage = args[0]?.toString() || '';
    const errorObj = args[0];
    
    // Suppress Windows-specific file access errors for client-reference-manifest
    // This is a known issue on Windows and doesn't affect functionality
    if (
      errorMessage.includes('page_client-reference-manifest.js') ||
      (errorObj && typeof errorObj === 'object' && 
       errorObj.code === 'UNKNOWN' && 
       errorObj.errno === -4094 &&
       errorObj.path?.includes('client-reference-manifest'))
    ) {
      // Silently suppress this Windows file access error
      return;
    }
    
    // Call original console.error for all other errors
    originalConsoleError.apply(console, args);
  };
}

// Import this in server-side code
export {};


