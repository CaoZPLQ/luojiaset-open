export const AdjustHeightMixin = {
    props: {
        headerHeight: Number
    },
    data() {
        return {
            screenHeight: this.headerHeight == undefined ? document.documentElement.clientHeight - 164 : document.documentElement.clientHeight - this.headerHeight,
            resizeListener: null
        }
    },
    mounted() {
        //动态获取浏览器高度
        const that = this
        this.resizeListener = ()=>{
            var currentHeight = this.headerHeight == undefined ? document.documentElement.clientHeight - 164 : document.documentElement.clientHeight - this.headerHeight
            this.screenHeight = this.screenHeight < currentHeight? currentHeight: this.screenHeight
            // console.log(this);
        }
        window.addEventListener('resize', this.resizeListener)
    },
    beforeDestroy() {
        window.removeEventListener('resize',this.resizeListener)
    },

}