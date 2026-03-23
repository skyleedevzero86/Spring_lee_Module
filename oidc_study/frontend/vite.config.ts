import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
	root: path.resolve(__dirname, 'src'),
	plugins: [svelte({ configFile: path.resolve(__dirname, 'svelte.config.js') })],
	server: {
		proxy: {
			'/api': 'http://localhost:9090',
			'/oauth2': 'http://localhost:9090',
			'/logout': 'http://localhost:9090'
		}
	},
	build: {
		outDir: path.resolve(__dirname, '../backend/src/main/resources/static'),
		emptyOutDir: true
	}
});