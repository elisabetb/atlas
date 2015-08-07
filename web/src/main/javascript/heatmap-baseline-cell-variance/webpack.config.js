var webpack = require('webpack');
var path = require('path');

module.exports = {
    context: __dirname,
    entry: {
        "heatmap-baseline-cell-variance": "./index.js",
        "demo": [
            "webpack-dev-server/client?http://localhost:9000", // WebpackDevServer host and port
            "webpack/hot/only-dev-server",
            "./html/demo.js",
        ],
        "test": [
            "webpack-dev-server/client?http://localhost:9000", // WebpackDevServer host and port
            "webpack/hot/only-dev-server",
            'mocha!./test/test.js'
        ],
        "vendor": ['react', 'react-highcharts/more'],
    },
    output: {
        path: __dirname + "/dist",
        publicPath: "/dev-server/",
        filename: "[name].bundle.js"
    },

    plugins: [
        new webpack.HotModuleReplacementPlugin(),
        new webpack.optimize.CommonsChunkPlugin(/* chunkName= */"vendor", /* filename= */"vendor.bundle.js"),
    ],

    module: {
        loaders: [
            {test: /\.jsx?$/, loaders: ['react-hot', "jsx?harmony"], include: path.join(__dirname, "src")},
            {test: /\.jsx$/, loader: 'jsx-loader'},
            {test: /demo.js$/, loader: 'expose?exposed'}
        ]
    },

    devServer: {
        port: 9000
    }
};
