let Cesium = require('cesium/Cesium');

class CesiumController {
  constructor(viewer) {
    this.viewer = viewer
    this.envDataSource = new Cesium.CustomDataSource('envDataSource')
    this.viewer.dataSources.add(this.envDataSource)
    //添加自定义数据源，管理overlay image
    this.overlayDatasource = new Cesium.CustomDataSource('overlayDatasource')
    this.viewer.dataSources.add(this.overlayDatasource)
  }
  drawEnvelopes(envelope, id) {
    // this.envDataSource.entities.removeAll()
    // if(envelope)
    envelope.map((v) => {
      v[0] = parseFloat(v[0])
      v[1] = parseFloat(v[1])
      return v
    })
    var centerLon = (parseFloat(envelope[0][0]) + parseFloat(envelope[2][0])) / 2
    var centerLat = (parseFloat(envelope[0][1]) + parseFloat(envelope[1][1])) / 2
    // console.log(...envelope[0])
    let positions = Cesium.Cartesian3.fromDegreesArray([...envelope[0], ...envelope[1], ...envelope[2], ...envelope[3], ...envelope[0]])
    var envEntity = new Cesium.Entity({
      id: id,
      polyline: {
        width: 4,
        material: new Cesium.ColorMaterialProperty(Cesium.Color.AQUA),
        positions: positions,
        show: true
      }
    })
    this.envDataSource.entities.add(envEntity)

    this.flyTO(centerLon, centerLat, 4000)
  }
  drawPoint(img, bbox, id) {
    var long = bbox[0]
    var lat = bbox[1]
    var locMarker = new Cesium.Entity({
      id: id,
      name: 'Citizens Bank Park',
      position: Cesium.Cartesian3.fromDegrees(long, lat),
      // point : {
      //     pixelSize : 5,
      //     color : Cesium.Color.RED,
      //     outlineColor : Cesium.Color.WHITE,
      //     outlineWidth : 2
      // },
      billboard: {
        image: img,
        width: 32,
        height: 32
      },
      // label : {
      //     text : 'Citizens Bank Park',
      //     font : '14pt monospace',
      //     style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      //     outlineWidth : 2,
      //     verticalOrigin : Cesium.VerticalOrigin.BOTTOM,
      //     pixelOffset : new Cesium.Cartesian2(0, -9)
      // }
    });
    this.envDataSource.entities.add(locMarker)

    this.flyTO(long, lat, 5000)

  }
  removeEnvelopes(id) {
    this.envDataSource.entities.removeById(id)
  }
  removeAll() {
    this.envDataSource.entities.removeAll()
  }
  flyTO(lon, lat, h) {
    this.viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(lon, lat, h),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-90),
        roll: Cesium.Math.toRadians(0)
      },
      duration: 2
    });
  }
}
export default CesiumController