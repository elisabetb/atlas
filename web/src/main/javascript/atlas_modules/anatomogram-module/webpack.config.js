var webpack = require('webpack');
var path = require('path');

module.exports = {
    context: __dirname,

    entry: {
        "anatomogramModule": './index.js'
    },

    output: {
        library: '[name]',
        path: path.resolve(__dirname, 'dist'),
        publicPath: '/dist/',
        filename: '[name].bundle.js'
    },

    plugins: [
        new webpack.optimize.DedupePlugin()
    ],

    module: {
        loaders: [
            {test: /\.jsx$/, loader: 'babel'}
        ]
    },

    devServer: {
        port: 9000
    }
};
