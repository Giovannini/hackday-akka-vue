module.exports = {
  entry: './scripts/app.js',
  output: {
    filename: 'bundle.js'
  },
  resolve: {
      alias: {
          vue: 'vue/dist/vue.js',
      }
  },
  module: {
    loaders: [
      { test: /\.js$/, loader: 'babel-loader', query: { presets: ['es2015', 'react', 'stage-2'] }, exclude: /node_modules/ }
    ]
  }
}
