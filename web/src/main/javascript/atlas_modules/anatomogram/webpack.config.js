var webpack = require('webpack');
var CleanWebpackPlugin = require('clean-webpack-plugin');
var path = require('path');

module.exports = {
    context: __dirname,

    entry: {
	    anatomogram: './index.js',
        dependencies: ['react', 'jquery', 'jquery-hc-sticky', 'jquery-ui-bundle', 'snapsvg'],

        demo: './html/demo.js'
    },

    output: {
        libraryTarget: 'var',
        library: 'Anatomogram',
        path: __dirname + '/dist',
	    filename: '[name].bundle.js',
        publicPath: '../dist/'
    },

    plugins: [
        new CleanWebpackPlugin(['dist'], {verbose: true, dry: false}),
        new webpack.optimize.DedupePlugin(),
        new webpack.optimize.CommonsChunkPlugin('dependencies', 'vendor.bundle.js')
    ],

    module: {
        loaders: [
            {test: /\.jsx$/, loader: 'babel'}
        ]
    }
};
