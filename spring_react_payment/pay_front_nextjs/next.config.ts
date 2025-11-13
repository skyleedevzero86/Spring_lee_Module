import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  devIndicators: {
    buildActivity: false,
    appIsrStatus: false,
  },
  experimental: {
    turbo: {
      root: process.cwd(),
    },
  },
};

export default nextConfig;
