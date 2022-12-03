/**
 * @param {Cesium.Viewer} viewer Cesium Viewer
 * @param {function(Array)=>any} callback 回调函数，输入参数为绘制图形的坐标数组
 * @example
 * //声明回调函数，用以接收绘制图形实例并做后续处理
 * function drawCallback(positions){
 *     console.log(positions)
 * }
 * //实例化Drawer
 * var drawer = new Drawer(viewer, drawCallback)
 * //开启绘制开关，指定类型（'rectangle','circle','polygon','line'），默认为rectangle
 * drawer.switch(true, 'rectangle')
 * //鼠标交互绘制（左键点选范围，右键确认即完成绘制）
 * //关闭绘制开关
 * drawer.switch(false)
 */

// var drawer = new Drawer(viewer)
// drawer.switch(true)

let Cesium = require('cesium/Cesium');
// require('cesium/Build/Cesium/Widgets/widgets.css');

class ShapeDrawer {
    constructor(viewer, callback=(positions)=>{console.log(positions);}) {
        this.viewer = viewer
        this.datasource = new Cesium.CustomDataSource('draw')
        this.viewer.dataSources.add(this.datasource)
        this.callback = callback
        this.handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas);
        this.mode = 'rectangle'
        this.activeShapePoints = []
        this.activeShape = null
        this.floatingPoint = null
    
        this.switch(false)
    }  
    switch(state = false, mode = 'rectangle') {
        if(state == true){
            this.initDraw()
            this.clearShapes()
        }else if(state == false){
            this.removeDrawer()
        }
        this.mode = mode
        this.activeShapePoints = []
        this.activeShape = null
        this.floatingPoint = null
    }
    
    initDraw() {
        //鼠标左键
        this.handler.setInputAction((event)=>{
            var earthPosition = this.viewer.camera.pickEllipsoid(event.position)
            // `earthPosition` will be undefined if our mouse is not over the globe.
            if (Cesium.defined(earthPosition)) {
                if (this.activeShapePoints.length === 0) {
                    this.clearShapes();//清除重新绘制
                    this.floatingPoint = this.createPoint(earthPosition);
                    this.activeShapePoints.push(earthPosition);
                    var dynamicPositions = new Cesium.CallbackProperty(()=>{
                        if (this.mode === 'polygon') {
                            return new Cesium.PolygonHierarchy(this.activeShapePoints);
                        }
                        return this.activeShapePoints;
                    }, false);
                    this.activeShape = this.drawShape(dynamicPositions);//绘制动态图
                }
                this.activeShapePoints.push(earthPosition);
                this.createPoint(earthPosition);
            }
        }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
        //鼠标移动
        this.handler.setInputAction((event)=>{
            if (Cesium.defined(this.floatingPoint)) {
                var newPosition = this.viewer.camera.pickEllipsoid(event.endPosition)
    
                if (Cesium.defined(newPosition)) {
                    this.floatingPoint.position.setValue(newPosition);
                    this.activeShapePoints.pop();
                    this.activeShapePoints.push(newPosition);
                }
            }
    
            var windowPos = event.endPosition;
            var cartesian = this.viewer.camera.pickEllipsoid(windowPos)
    
            if (!Cesium.defined(this.datasource.entities.getById('label')) && Cesium.defined(cartesian)) {
                var label = new Cesium.Entity({
                    label: {
                        show: true,
                        text: '左键绘制，右键结束',
                        showBackground : true,
                        scale: 0.5,
                        horizontalOrigin: Cesium.HorizontalOrigin.LEFT,
                        verticalOrigin: Cesium.VerticalOrigin.TOP,
                        pixelOffset: new Cesium.Cartesian2(15, 0)
                    },
                    id: 'label',
                    position: cartesian,
                })
                this.datasource.entities.add(label)
            } else {
                if (Cesium.defined(cartesian)) {
                    var label = this.datasource.entities.getById('label')
                    label.position = cartesian
                }
            }    
    
        }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
        
        //鼠标右键
        this.handler.setInputAction((event)=>{
            //this.activeShapePoints.pop();//去除最后一个动态点
            if(this.activeShapePoints.length){
                var entity = this.drawShape(this.activeShapePoints)//绘制最终图
                if(Cesium.defined(entity.rectangle)){
                    var rec = entity.rectangle.coordinates.getValue()
                    this.callback([Cesium.Math.toDegrees(rec.west), Cesium.Math.toDegrees(rec.south), Cesium.Math.toDegrees(rec.east), Cesium.Math.toDegrees(rec.north)])
                }else if(Cesium.defined(entity.ellipse)){
                    var longitude = Cesium.Math.toDegrees(Cesium.Cartographic.fromCartesian(entity.position.getValue(0)).longitude)
                    var latitude = Cesium.Math.toDegrees(Cesium.Cartographic.fromCartesian(entity.position.getValue(0)).latitude)
                    this.callback([longitude, latitude, entity.ellipse.semiMajorAxis.getValue()])
                }else if(Cesium.defined(entity.polyline)){
                    this.callback(entity.polyline.positions.getValue().map((prop)=>{
                        return [Cesium.Math.toDegrees(Cesium.Cartographic.fromCartesian(prop).longitude),Cesium.Math.toDegrees(Cesium.Cartographic.fromCartesian(prop).latitude)]
                    }))
                }else if(Cesium.defined(entity.polygon)){
                    this.callback(entity.polygon.hierarchy.getValue().positions.map((prop)=>{
                        return [Cesium.Math.toDegrees(Cesium.Cartographic.fromCartesian(prop).longitude), Cesium.Math.toDegrees(Cesium.Cartographic.fromCartesian(prop).latitude)]
                    }))
                }
                //this.clearHander()
                this.datasource.entities.removeById('label')
            }
            this.datasource.entities.remove(this.floatingPoint);//去除动态点图形（当前鼠标点）
            this.datasource.entities.remove(this.activeShape);//去除动态图形
            this.floatingPoint = undefined;
            this.activeShape = undefined;
            this.activeShapePoints = [];
        },Cesium.ScreenSpaceEventType.RIGHT_CLICK)
    }
    
    removeDrawer() {
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK)
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType.RIGHT_CLICK)
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE)
        this.clearShapes()
        this.floatingPoint = undefined;
        this.activeShape = undefined;
        this.activeShapePoints = [];
    }
    
    clearHander(){
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK)
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType.RIGHT_CLICK)
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE)
        this.floatingPoint = undefined;
        this.activeShape = undefined;
        this.activeShapePoints = [];
    }
    
    clearShapes() {
        this.datasource.entities.removeAll()
    }
    
    createPoint(worldPosition) {
        var point = this.datasource.entities.add({
            position: worldPosition,
            point: {
                color: Cesium.Color.WHITE,
                pixelSize: 1,
                heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
            }
        });
        return point;
    }
    
    drawShape(positionData){
        var shape;
        if (this.mode === 'line') {
            shape = this.datasource.entities.add({
                polyline: {
                    positions: positionData,
                    clampToGround: true,
                    width: 3
                }
            });
        }
        else if (this.mode === 'polygon') {
            shape = this.datasource.entities.add({
                polygon: {
                    hierarchy: positionData,
                    material: new Cesium.ColorMaterialProperty(Cesium.Color.WHITE.withAlpha(0.7))
                }
            });
        } else if (this.mode === 'circle') {
            //当positionData为数组时绘制最终图，如果为function则绘制动态图
            var value = typeof positionData.getValue === 'function' ? positionData.getValue(0) : positionData;
            //var start = activeShapePoints[0];
            //var end = activeShapePoints[activeShapePoints.length - 1];
            //var r = Math.sqrt(Math.pow(start.x - end.x, 2) + Math.pow(start.y - end.y, 2));
            //r = r ? r : r + 1;
            shape = this.datasource.entities.add({
                position: this.activeShapePoints[0],
                name: 'Blue translucent, rotated, and extruded ellipse with outline',
                type:'Selection tool',
                ellipse: {
                    semiMinorAxis: new Cesium.CallbackProperty(function () {
                        //半径 两点间距离
                        var r = Math.sqrt(Math.pow(value[0].x - value[value.length - 1].x, 2) + Math.pow(value[0].y - value[value.length - 1].y, 2));
                        return r ? r : r + 1;
                    }, false),
                    semiMajorAxis: new Cesium.CallbackProperty(function () {
                        var r = Math.sqrt(Math.pow(value[0].x - value[value.length - 1].x, 2) + Math.pow(value[0].y - value[value.length - 1].y, 2));
                        return r ? r : r + 1;
                    }, false),
                    material: Cesium.Color.BLUE.withAlpha(0.5),
                    outline: true
                }
            });
        } else if (this.mode === 'rectangle') {
            //当positionData为数组时绘制最终图，如果为function则绘制动态图
            var arr = typeof positionData.getValue === 'function' ? positionData.getValue(0) : positionData;
            shape = this.datasource.entities.add({
                name: 'Blue translucent, rotated, and extruded ellipse with outline',
                rectangle : {
                    coordinates :  new Cesium.CallbackProperty(function () {
                        var obj=Cesium.Rectangle.fromCartesianArray(arr);
                        //if(obj.west==obj.east){ obj.east+=0.000001};
                        //if(obj.south==obj.north){obj.north+=0.000001};
                        return obj;
                    }, false),
                    material : Cesium.Color.BLUE.withAlpha(0.2)
                }
            });
        }
        return shape;
    }
    
    destroy(){
        this.removeDrawer()
    }
    
    setCallback(callback){
        this.callback = callback
    }  
}

export default ShapeDrawer;