import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 9000,
    host: '127.0.0.1',
    strictPort: false,
  }
});
