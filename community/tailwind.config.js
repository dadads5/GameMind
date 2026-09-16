/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#ff4d00',
        secondary: '#0f5132',
        dark: '#1a1a2e',
      },
    },
  },
  plugins: [],
  darkMode: 'class',
}
