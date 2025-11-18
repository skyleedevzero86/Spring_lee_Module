import type { NextConfig } from "next";
import path from 'path';
import bundleAnalyzer from '@next/bundle-analyzer';

if (typeof window === 'undefined' && process.platform === 'win32') {
  const originalConsoleError = console.error;
  console.error = (...args: any[]) => {
    const errorMessage = args[0]?.toString() || '';
    const errorObj = args[0];
    
    if (
      errorMessage.includes('page_client-reference-manifest.js') ||
      errorMessage.includes('page.js') ||
      (errorObj && typeof errorObj === 'object' && 
       errorObj.code === 'UNKNOWN' && 
       errorObj.errno === -4094 &&
       (errorObj.path?.includes('client-reference-manifest') || errorObj.path?.includes('page.js')))
    ) {
      return;
    }
    
    originalConsoleError.apply(console, args);
  };
}

const withBundleAnalyzer = bundleAnalyzer({
  enabled: process.env.ANALYZE === 'true',
});

const projectRoot = path.resolve(__dirname);

const nextConfig: NextConfig = {
  compress: true,
  poweredByHeader: false,
  reactStrictMode: true,
  outputFileTracingRoot: projectRoot,
  generateEtags: false,
  onDemandEntries: {
    maxInactiveAge: 60 * 1000,
    pagesBufferLength: 5,
  },
  images: {
    formats: ['image/avif', 'image/webp'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    minimumCacheTTL: 60,
  },
  experimental: {
    optimizePackageImports: ['lucide-react', '@radix-ui/react-slot', '@tanstack/react-query'],
  },
  turbopack: {},
  webpack: (config, { isServer, dev }) => {
    if (dev) {
      config.watchOptions = {
        poll: 1000,
        aggregateTimeout: 300,
        ignored: /node_modules/,
        followSymlinks: false,
      };
      config.infrastructureLogging = {
        level: 'error',
      };
      if (isServer) {
        const originalExternals = config.externals;
        if (Array.isArray(originalExternals)) {
          config.externals = [...originalExternals, '@tanstack/react-query-devtools'];
        } else if (typeof originalExternals === 'function') {
          config.externals = [
            originalExternals,
            ({ request }: { request: string }, callback: (err: Error | null, result?: string) => void) => {
              if (request && request.includes('@tanstack/react-query-devtools')) {
                return callback(null, 'commonjs ' + request);
              }
              callback(null);
            },
          ];
        } else {
          config.externals = [originalExternals, '@tanstack/react-query-devtools'];
        }
      }
      
      if (!isServer) {
        config.optimization = {
          ...config.optimization,
          splitChunks: {
            chunks: 'all',
            cacheGroups: {
              default: {
                minChunks: 2,
                priority: -20,
                reuseExistingChunk: true,
              },
              vendors: {
                test: /[\\/]node_modules[\\/]/,
                priority: -10,
                reuseExistingChunk: true,
              },
            },
          },
        };
      }
    } else {
      if (!isServer) {
        config.optimization = {
          ...config.optimization,
          splitChunks: {
            chunks: 'all',
            cacheGroups: {
              default: false,
              vendors: false,
              framework: {
                name: 'framework',
                chunks: 'all',
                test: /(?<!node_modules.*)[\\/]node_modules[\\/](react|react-dom|scheduler|prop-types|use-subscription)[\\/]/,
                priority: 40,
                enforce: true,
              },
              reactQuery: {
                name: 'react-query',
                test: /[\\/]node_modules[\\/]@tanstack[\\/]react-query[\\/]/,
                priority: 35,
                chunks: 'all',
                enforce: true,
              },
              lib: {
                test(module: { resource?: string }) {
                  return (
                    module.resource?.includes('node_modules') &&
                    !module.resource?.includes('react') &&
                    !module.resource?.includes('@tanstack/react-query')
                  );
                },
                name(module: { resource?: string }) {
                  const packageName = module.resource?.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)?.[1];
                  return `npm.${packageName?.replace('@', '')}`;
                },
                priority: 30,
                minChunks: 1,
                reuseExistingChunk: true,
              },
              commons: {
                name: 'commons',
                minChunks: 2,
                priority: 20,
              },
              shared: {
                name(module: { resource?: string }, chunks: { name: string }[]) {
                  return chunks.map((chunk) => chunk.name).join('~');
                },
                priority: 10,
                minChunks: 2,
                reuseExistingChunk: true,
              },
            },
          },
        };
      }
    }
    return config;
  },
};

export default withBundleAnalyzer(nextConfig);
