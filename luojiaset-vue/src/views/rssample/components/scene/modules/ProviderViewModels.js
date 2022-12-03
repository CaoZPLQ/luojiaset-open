let Cesium = require('cesium/Cesium');
// require('cesium/Build/Cesium/Widgets/widgets.css');
// Cesium.buildModuleUrl.setBaseUrl('/static/Cesium-1.71/')//设置静态资源目录

var imageryViewModels = []; //存放需要显示的imagery图层服务

//天地图img_w影像服务,墨卡托投影
let tianDTimg_w_imageryProvier = new Cesium.WebMapTileServiceImageryProvider({
    url: "http://{s}.tianditu.gov.cn/img_w/wmts?service=wmts&request=GetTile&version=1.0.0" +
    "&LAYER=img&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
    "&style=default&format=tiles&tk=c2d39e4f978c9f18d849a609f73c3072",
    layer: "img_w",	//WMTS请求的层名称
    style: "default",//WMTS请求的样式名称
    format: "tiles",//MIME类型，用于从服务器检索图像
    tileMatrixSetID: "GoogleMapsCompatible",//	用于WMTS请求的TileMatrixSet的标识符
    subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],//天地图8个服务器
    minimumLevel: 0,//最小层级
    maximumLevel: 18,//最大层级
});
let tianDTimg_w_viewModel = new Cesium.ProviderViewModel({
    name : '天地图\u00ad影像服务',
    iconUrl : Cesium.buildModuleUrl('Widgets/Images/ImageryProviders/openStreetMap.png'),
    tooltip : '天地图国家地理信息公共服务平台.\nhttps://www.tianditu.gov.cn/',
    creationFunction : function() {
        return tianDTimg_w_imageryProvier;
    }
});
imageryViewModels.push(tianDTimg_w_viewModel);

//OSM地图服务
let osm_imageryProvider = new Cesium.OpenStreetMapImageryProvider({
    url : 'https://a.tile.openstreetmap.org/'
});
let osg_viewModel = new Cesium.ProviderViewModel({
    name : 'Open\u00adStreet\u00adMap',
    iconUrl : Cesium.buildModuleUrl('Widgets/Images/ImageryProviders/openStreetMap.png'),
    tooltip : 'OpenStreetMap (OSM) is a collaborative project to create a free editable \
map of the world.\nhttp://www.openstreetmap.org',
    creationFunction : function() {
        return osm_imageryProvider;
    }
});
imageryViewModels.push(osg_viewModel);

//Arcgis地图服务
//Widgets/Images/ImageryProviders/bingAerial.png
let arcgis_imageryProvider =  new Cesium.ArcGisMapServerImageryProvider({
    url: "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer",
})
let arcgis_viewModel = new Cesium.ProviderViewModel({
    name: 'Arcgis\u00adMap',
    iconUrl : Cesium.buildModuleUrl('Widgets/Images/ImageryProviders/esriWorldStreetMap.png'),
    tooltip: 'Arcgis Map',
    creationFunction : function() {
        return arcgis_imageryProvider;
    }
})
imageryViewModels.push(arcgis_viewModel);

//Bing地图服务
let bing_imageryProvider = new Cesium.BingMapsImageryProvider({
    url: "https://dev.virtualearth.net",
    key: 'Amzbjhd07UcY2iS-Ae4-6i26tIQzydGkOLLHWHfgZIyR8WqmBvDDTZACcQ2LSRmA',
    mapStyle : Cesium.BingMapsStyle.AERIAL
})
let bing_viewModel = new Cesium.ProviderViewModel({
    name: 'Bing\u00adMap',
    iconUrl: Cesium.buildModuleUrl('Widgets/Images/ImageryProviders/bingAerial.png'),
    tooltip: 'Bing Map',
    creationFunction : function(){
        return bing_imageryProvider
    }
})
imageryViewModels.push(bing_viewModel)

export default imageryViewModels