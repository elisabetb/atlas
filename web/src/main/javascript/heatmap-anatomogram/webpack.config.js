var webpack = require('webpack');
var path = require('path');
var CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = {
    entry: {
        heatmapAnatomogram: './index.js',
        internalHeatmapAnatomogram: './src/internal-heatmap-anatomogram.js',
        heatmapAnatomogramDemo: './html/demo.js',
        dependencies: ['react', 'react-dom', 'react-radio-group',
                       'jquery', 'jquery-ui-bundle', 'jquery.browser', 'fancybox', 'jquery-hc-sticky', 'jquery-toolbar', 'jQuery-ajaxTransport-XDomainRequest', 'jquery-hc-sticky',
                       'urijs', 'imports-loader?this=>window,fix=>module.exports=0!snapsvg/dist/snap.svg.js', 'atlas-modernizr',
                       'highcharts-more', 'react-highcharts',
                       'events']
    },

    output: {
        libraryTarget: 'var',
        library: '[name]',
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].bundle.js',
        publicPath: '/dist/'
    },

    plugins: [
        new CleanWebpackPlugin(['dist'], {verbose: true, dry: false}),
        new webpack.optimize.DedupePlugin(),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'dependencies',
            filename: 'vendor.bundle.js',
            minChunks: Infinity     // Explicit definition-based split. Don’t put shared modules between main and demo entries in vendor.bundle.js
        })
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
