<template>
  <a-modal
    :title="title"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭">
    
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
      
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="datasetId">
          <a-input placeholder="请输入datasetId" v-decorator="['datasetId', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="sampleSize">
          <a-input placeholder="请输入sampleSize" v-decorator="['sampleSize', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="sampleArea">
          <a-input placeholder="请输入sampleArea" v-decorator="['sampleArea', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="sampleDate">
          <a-input placeholder="请输入sampleDate" v-decorator="['sampleDate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="sampleQuality">
          <a-input placeholder="请输入sampleQuality" v-decorator="['sampleQuality', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="sampleLabeler">
          <a-input placeholder="请输入sampleLabeler" v-decorator="['sampleLabeler', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="annotationDate">
          <a-input placeholder="请输入annotationDate" v-decorator="['annotationDate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="mutiViewPaths">
          <a-input placeholder="请输入mutiViewPaths" v-decorator="['mutiViewPaths', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="mutiViewDepthPaths">
          <a-input placeholder="请输入mutiViewDepthPaths" v-decorator="['mutiViewDepthPaths', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="mutiViewParmPaths">
          <a-input placeholder="请输入mutiViewParmPaths" v-decorator="['mutiViewParmPaths', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="imageMode">
          <a-input placeholder="请输入imageMode" v-decorator="['imageMode', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="parmMode">
          <a-input placeholder="请输入parmMode" v-decorator="['parmMode', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="depthMode">
          <a-input placeholder="请输入depthMode" v-decorator="['depthMode', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="imageType">
          <a-input placeholder="请输入imageType" v-decorator="['imageType', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="imageChannels">
          <a-input placeholder="请输入imageChannels" v-decorator="['imageChannels', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="imageResolution">
          <a-input placeholder="请输入imageResolution" v-decorator="['imageResolution', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="instrument">
          <a-input placeholder="请输入instrument" v-decorator="['instrument', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="trnValueTest">
          <a-input placeholder="请输入trnValueTest" v-decorator="['trnValueTest', {}]" />
        </a-form-item>
		
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>
  import { httpAction } from '@/api/manage'
  import pick from 'lodash.pick'
  import moment from "moment"

  export default {
    name: "RssTdSampleModal",
    data () {
      return {
        title:"操作",
        visible: false,
        model: {},
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },

        confirmLoading: false,
        form: this.$form.createForm(this),
        validatorRules:{
        },
        url: {
          add: "/rssample/rssTdSample/add",
          edit: "/rssample/rssTdSample/edit",
        },
      }
    },
    created () {
    },
    methods: {
      add () {
        this.edit({});
      },
      edit (record) {
        this.form.resetFields();
        this.model = Object.assign({}, record);
        this.visible = true;
        this.$nextTick(() => {
          this.form.setFieldsValue(pick(this.model,'datasetId','sampleSize','sampleArea','sampleDate','sampleQuality','sampleLabeler','annotationDate','mutiViewPaths','mutiViewDepthPaths','mutiViewParmPaths','imageMode','parmMode','depthMode','imageType','imageChannels','imageResolution','instrument','trnValueTest'))
		  //时间格式化
        });

      },
      close () {
        this.$emit('close');
        this.visible = false;
      },
      handleOk () {
        const that = this;
        // 触发表单验证
        this.form.validateFields((err, values) => {
          if (!err) {
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if(!this.model.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
               method = 'put';
            }
            let formData = Object.assign(this.model, values);
            //时间格式化
            
            console.log(formData)
            httpAction(httpurl,formData,method).then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
              that.close();
            })



          }
        })
      },
      handleCancel () {
        this.close()
      },


    }
  }
</script>

<style lang="less" scoped>

</style>