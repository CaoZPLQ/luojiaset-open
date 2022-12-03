/////////////////////////////////////////////////////////////////////////////////
// 去console插件
// const UglifyJsPlugin = require('uglifyjs-webpack-plugin')
const CompressionPlugin = require("compression-webpack-plugin")
// 拷贝文件插件
const CopyWebpackPlugin = require('copy-webpack-plugin')
const webpack = require('webpack')

let cesiumSource = './node_modules/cesium/Source'
let cesiumWorkers = '../Build/Cesium/Workers'

const path = require('path')
const resolve = dir => {
  return path.join(__dirname, dir)
}

module.exports = {
  publicPath: '/luojiaSet',//部署应用包时的基本 URL
  lintOnSave: undefined,
  productionSourceMap: false,// 打包时不生成.map文件
  devServer:{//配置服务器
    host:"localhost",
    port: 3000,
    proxy: {
      '/rssample-boot': {
        target: 'http://xxx.xxx.xxx.xxx:10086', //请求本地 rssample-boot后台项目
        // target: 'localhost:10086', //请求本地 rssample-boot后台项目
        ws: false,
        changeOrigin: true
      },
    }
  },
  chainWebpack: config => {
    config.resolve.alias
      .set('@$', resolve('src'))
      .set('@api', resolve('src/api'))
      .set('@assets', resolve('src/assets'))
      .set('@comp', resolve('src/components'))
      .set('@views', resolve('src/views'))
      .set('@layout', resolve('src/layout'))
      .set('@static', resolve('src/static'))
      .set('@mobile', resolve('src/modules/mobile'))

    //生产环境，开启js\css压缩
    if (process.env.NODE_ENV === 'production') {
      config.plugin('compressionPlugin').use(new CompressionPlugin({
        test: /\.js$|.\css|.\less/, // 匹配文件名
        threshold: 10240, // 对超过10k的数据压缩
        deleteOriginalAssets: false // 不删除源文件
      }))
    }
  },
  configureWebpack: config => {
    //生产环境取消 console.log
    if (process.env.NODE_ENV === 'production') {
      config.optimization.minimizer[0].options.terserOptions.compress.drop_console = true
    }
  },
  configureWebpack: {//配置Webpack
    // devtool: 'source-map',//在浏览器中显示vue源码用来调试
    output: {
      sourcePrefix: ' '
    },
    amd: {
      toUrlUndefined: true
    },
    resolve: {
      alias: {
        'cesium': path.resolve(__dirname, cesiumSource)
      }
    },
    plugins: [
      new webpack.DefinePlugin({
        CESIUM_BASE_URL: JSON.stringify('/luojiaSet/Cesium')
      })
    ],
    // externals: {
    //   Cesium: "Cesium",
    // },
    optimization: {
      splitChunks: {
        cacheGroups: {
          commons: {
            name: 'Cesium',
            test: /[\\/]node_modules[\\/]cesium/,
            chunks: 'all'
          }
        }
      }
    },
    module: {
      unknownContextCritical: /^.\/.*$/,
      unknownContextCritical: false
    }
  },
  css: {
    sourceMap: false,
    // css预设器配置项
    loaderOptions: {
      less: {
        modifyVars: {
          /* less 变量覆盖，用于自定义 ant design 主题 */
          'primary-color': '#1890FF',
          'link-color': '#1890FF',
          'border-radius-base': '4px',
          'layout': 'topmenu',
          'fixed-header': true,
          'multipage': false,
        },
        javascriptEnabled: true,
      }
    }
  }
}