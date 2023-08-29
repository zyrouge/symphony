const Colors = require("tailwindcss/colors");

/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx,vue}"],
    theme: {
        extend: {
            fontFamily: {
                sans: ["DM Sans", "sans-serif"],
                display: ["Dela Gothic One", "cursive"],
            },
            colors: {
                primary: Colors.violet,
                secondary: Colors.zinc,
            },
        },
    },
    plugins: [],
};
