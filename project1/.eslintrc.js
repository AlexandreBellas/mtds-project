const path = require('path')

module.exports = {
     root: true,
     parser: '@typescript-eslint/parser',
     parserOptions: {
          tsconfigRootDir: __dirname,
          project: ['./tsconfig.json']
     },
     env: {
          browser: true,
          node: true
     },
     rules: {
          //eslint
          'no-undef': 'off',
          'spaced-comment': [
               'warn',
               'always',
               {
                    markers: ['/']
               }
          ],
          'prefer-arrow-callback': 'warn',
          'no-empty': 'warn',
          'prefer-const': 'warn', // should be increased to error
          'id-length': 'off',
          'no-only-tests/no-only-tests': 'error',
          'no-shadow': 'off',
          'no-param-reassign': 'off',
          'filenames/match-exported': 'off',
          complexity: ['error', 15],
          'no-unmodified-loop-condition': 'off',
          //import
          'import/default': 'warn',
          'import/no-default-export': 'warn',
          'import/no-cycle': 'warn',
          'import/order': [
               'warn',
               {alphabetize: {order: 'asc', caseInsensitive: true}, 'newlines-between': 'always'}
          ],
          //react
          'react/display-name': 'off',
          'react/prop-types': 'off',
          'react/no-unescaped-entities': 'warn', // should be increased to error
          //"@typescript-eslint
          '@typescript-eslint/naming-convention': [
               'warn',
               {
                    selector: 'default',
                    format: ['camelCase']
               },
               {
                    selector: 'function',
                    format: ['PascalCase', 'camelCase']
               },
               {
                    selector: 'variable',
                    format: ['PascalCase', 'camelCase', 'UPPER_CASE'],
                    leadingUnderscore: 'allow'
               },
               {
                    selector: 'parameter',
                    format: ['PascalCase', 'camelCase'],
                    leadingUnderscore: 'allow'
               },
               {
                    selector: 'memberLike',
                    format: ['PascalCase', 'camelCase', 'UPPER_CASE']
               },
               {
                    selector: 'property',
                    format: ['PascalCase', 'camelCase', 'UPPER_CASE', 'snake_case']
               },
               {
                    selector: 'property',
                    filter: {
                         regex: '__html',
                         match: true
                    },
                    format: null
               },
               {
                    selector: 'typeLike',
                    format: ['PascalCase']
               }
          ],
          '@typescript-eslint/prefer-regexp-exec': 'warn',
          '@typescript-eslint/explicit-module-boundary-types': 'off',
          '@typescript-eslint/no-floating-promises': 'warn',
          '@typescript-eslint/require-await': 'warn',
          '@typescript-eslint/ban-ts-comment': 'warn',
          '@typescript-eslint/await-thenable': 'warn',
          '@typescript-eslint/no-explicit-any': 'warn',
          '@typescript-eslint/no-unsafe-call': 'off', // should be activated to improve Codebase
          '@typescript-eslint/no-unsafe-member-access': 'off', // should be activated to improve Codebase
          '@typescript-eslint/no-unsafe-assignment': 'off', // should be activated to improve Codebase
          '@typescript-eslint/no-unsafe-return': 'off', // should be activated to improve Codebase
          '@typescript-eslint/restrict-template-expressions': 'off',
          '@typescript-eslint/restrict-plus-operands': 'off',
          '@typescript-eslint/no-empty-function': 'warn',
          '@typescript-eslint/no-empty-interface': 'warn',
          '@typescript-eslint/ban-types': 'warn', // should be increased to error
          '@typescript-eslint/no-unnecessary-type-assertion': 'warn', // should be increased to error
          //jsx-a11y
          'jsx-a11y/click-events-have-key-events': 'off',
          'jsx-a11y/anchor-is-valid': 'off',
          'jsx-a11y/no-noninteractive-element-to-interactive-role': 'off',
          'jsx-a11y/no-static-element-interactions': 'off',
          'jsx-a11y/no-autofocus': 'warn',
          //prettier
          'prettier/prettier': 'warn'
     },
     plugins: ['@typescript-eslint', 'react', 'jsx-a11y', 'react-hooks'],
     extends: [
          'eslint:recommended',
          'airbnb',
          'airbnb-typescript'
          // 'plugin:@typescript-eslint/recommended', // Uses the recommended rules from @typescript-eslint/eslint-plugin
          // 'plugin:@typescript-eslint/recommended-requiring-type-checking'
     ]
}
