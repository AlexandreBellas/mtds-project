const path = require('path')
const HtmlWebPackPlugin = require('html-webpack-plugin')

module.exports = {
     entry: [
          path.join(process.cwd(), 'index.tsx') // or whatever the path of your root file is
     ],
     output: {
          path: path.resolve(__dirname, 'build'),
          filename: 'bundle.js'
     },
     resolve: {
          modules: [path.join(__dirname, 'src'), 'node_modules'],
          alias: {
               react: path.join(__dirname, 'node_modules', 'react')
          },
          extensions: ['.js', '.jsx', '.react.js', '.ts', '.tsx']
     },
     module: {
          rules: [
               {
                    test: /\.(js|jsx)$/,
                    exclude: /node_modules/,
                    use: {
                         loader: 'babel-loader',
                         options: {
                              presets: [
                                   '@babel/preset-env',
                                   '@babel/preset-react',
                                   '@babel/preset-typescript'
                              ]
                         }
                    }
               },
               {
                    test: /\.css$/,
                    use: [
                         {
                              loader: 'style-loader'
                         },
                         {
                              loader: 'css-loader'
                         }
                    ]
               },
               {
                    test: /\.tsx?$/,
                    loader: 'awesome-typescript-loader',
                    exclude: /node_modules/
               }
          ]
     },
     plugins: [
          new HtmlWebPackPlugin({
               template: 'index.html'
          })
     ],
     performance: {
          hints: 'warning',
          // Calculates sizes of gziped bundles.
          assetFilter: function (assetFilename) {
               return assetFilename.endsWith('.js.gz')
          }
     }
}
