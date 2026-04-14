import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
  kit: {
    adapter: adapter({
      pages: '../resources/static',
      assets: '../resources/static',
      fallback: 'index.html',
      precompress: false,
      strict: true
    })
  }
};

export default config;