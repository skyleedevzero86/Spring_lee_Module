import type { NextConfig } from "next";
import path from 'path';
import bundleAnalyzer from '@next/bundle-analyzer';

const withBundleAnalyzer = bundleAnalyzer({
  enabled: process.env.ANALYZE === 'true',
});

const projectRoot = path.resolve(__dirname);

const nextConfig: NextConfig = {
  compress: true,
  poweredByHeader: false,
  reactStrictMode: true,
  outputFileTracingRoot: projectRoot,
  images: {
    formats: ['image/avif', 'image/webp'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    minimumCacheTTL: 60,
  },
  experimental: {
    optimizePackageImports: ['lucide-react', '@radix-ui/react-slot'],
  },
  webpack: (config, { isServer, dev }) => {
    if (dev) {
      config.cache = false;
      config.watchOptions = {
        poll: 1000,
        aggregateTimeout: 300,
        ignored: /node_modules/,
      };
      config.snapshot = {
        ...config.snapshot,
        managedPaths: [],
      };
      if (isServer) {
        const originalExternals = config.externals;
        if (Array.isArray(originalExternals)) {
          config.externals = [...originalExternals, '@tanstack/react-query-devtools'];
        } else if (typeof originalExternals === 'function') {
          config.externals = [
            originalExternals,
            ({ request }: { request: string }, callback: any) => {
              if (request && request.includes('@tanstack/react-query-devtools')) {
                return callback(null, 'commonjs ' + request);
              }
              callback();
            },
          ];
        } else {
          config.externals = [originalExternals, '@tanstack/react-query-devtools'];
        }
      }
    }
    
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
            lib: {
              test(module: { resource?: string }) {
                return module.resource?.includes('node_modules') && !module.resource?.includes('react');
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
    return config;
  },
};

export default withBundleAnalyzer(nextConfig);
