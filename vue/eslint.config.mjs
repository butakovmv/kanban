import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'

/**
 * Базовые браузерные и ES2021 глобалы, доступные в исходниках.
 * Используются для типов DOM-событий и API браузера в `<script setup>`.
 */
const browserGlobals = {
  window: 'readonly',
  document: 'readonly',
  console: 'readonly',
  fetch: 'readonly',
  FormData: 'readonly',
  File: 'readonly',
  Blob: 'readonly',
  FileReader: 'readonly',
  DragEvent: 'readonly',
  Event: 'readonly',
  MouseEvent: 'readonly',
  KeyboardEvent: 'readonly',
  HTMLInputElement: 'readonly',
  HTMLTextAreaElement: 'readonly',
  HTMLElement: 'readonly',
  FileList: 'readonly',
  DataTransfer: 'readonly',
  URL: 'readonly',
  URLSearchParams: 'readonly',
  setTimeout: 'readonly',
  clearTimeout: 'readonly',
  setInterval: 'readonly',
  clearInterval: 'readonly',
  globalThis: 'readonly',
  Promise: 'readonly',
  Date: 'readonly',
  Math: 'readonly',
  JSON: 'readonly',
  Error: 'readonly',
  TypeError: 'readonly',
  Array: 'readonly',
  Object: 'readonly',
  String: 'readonly',
  Number: 'readonly',
  Boolean: 'readonly',
  Map: 'readonly',
  Set: 'readonly',
  WeakMap: 'readonly',
  WeakSet: 'readonly',
  Symbol: 'readonly',
  Proxy: 'readonly',
  Reflect: 'readonly',
  Intl: 'readonly',
  encodeURIComponent: 'readonly',
  decodeURIComponent: 'readonly',
}

export default [
  js.configs.recommended,
  ...tseslint.configs.recommended.map(c => ({
    ...c,
    files: ['**/*.ts', '**/*.tsx'],
  })),
  ...pluginVue.configs['flat/recommended'].map(c => ({
    ...c,
    files: c.files ?? ['**/*.vue'],
    languageOptions: c.languageOptions ?? {},
  })),
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
      globals: browserGlobals,
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/html-self-closing': ['warn', { html: { void: 'always' } }],
      'vue/max-attributes-per-line': 'off',
    },
  },
  {
    files: ['**/*.ts', '**/*.tsx'],
    languageOptions: {
      globals: browserGlobals,
    },
  },
  {
    ignores: ['dist/', 'node_modules/'],
  },
]
