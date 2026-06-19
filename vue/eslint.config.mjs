import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'

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
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/html-self-closing': ['warn', { html: { void: 'always' } }],
      'vue/max-attributes-per-line': 'off',
    },
  },
  {
    ignores: ['dist/', 'node_modules/'],
  },
]
