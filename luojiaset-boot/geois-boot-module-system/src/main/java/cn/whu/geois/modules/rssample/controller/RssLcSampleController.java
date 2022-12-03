package cn.whu.geois.modules.rssample.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.common.system.query.QueryGenerator;
import cn.whu.geois.common.aspect.annotation.AutoLog;
import cn.whu.geois.common.util.oConvertUtils;
import cn.whu.geois.modules.rssample.entity.RssLcSample;
import cn.whu.geois.modules.rssample.service.IRssLcSampleService;
import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 地物分类样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-02
 * @Version: V1.0
 */
@Slf4j
//@Api(tags="地物分类样本元数据表")
@RestController
@RequestMapping("/rssample/rssLcSample")
public class RssLcSampleController {
	@Autowired
	private IRssLcSampleService rssLcSampleService;
	
	/**
	  * 分页列表查询
	 * @param rssLcSample
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "地物分类样本元数据表-分页列表查询")
	//@ApiOperation(value="地物分类样本元数据表-分页列表查询", notes="地物分类样本元数据表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<RssLcSample>> queryPageList(RssLcSample rssLcSample,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<RssLcSample>> result = new Result<IPage<RssLcSample>>();
		QueryWrapper<RssLcSample> queryWrapper = QueryGenerator.initQueryWrapper(rssLcSample, req.getParameterMap());
		Page<RssLcSample> page = new Page<RssLcSample>(pageNo, pageSize);
		IPage<RssLcSample> pageList = rssLcSampleService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  *   添加
	 * @param rssLcSample
	 * @return
	 */
	@AutoLog(value = "地物分类样本元数据表-添加")
	//@ApiOperation(value="地物分类样本元数据表-添加", notes="地物分类样本元数据表-添加")
	@PostMapping(value = "/add")
	public Result<RssLcSample> add(@RequestBody RssLcSample rssLcSample) {
		Result<RssLcSample> result = new Result<RssLcSample>();
		try {
			rssLcSampleService.save(rssLcSample);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  *  编辑
	 * @param rssLcSample
	 * @return
	 */
	@AutoLog(value = "地物分类样本元数据表-编辑")
	//@ApiOperation(value="地物分类样本元数据表-编辑", notes="地物分类样本元数据表-编辑")
	@PutMapping(value = "/edit")
	public Result<RssLcSample> edit(@RequestBody RssLcSample rssLcSample) {
		Result<RssLcSample> result = new Result<RssLcSample>();
		RssLcSample rssLcSampleEntity = rssLcSampleService.getById(rssLcSample.getId());
		if(rssLcSampleEntity==null) {
			result.error500("未找到对应实体");
		}else {
			boolean ok = rssLcSampleService.updateById(rssLcSample);
			//TODO 返回false说明什么？
			if(ok) {
				result.success("修改成功!");
			}
		}
		
		return result;
	}
	
	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@AutoLog(value = "地物分类样本元数据表-通过id删除")
	//@ApiOperation(value="地物分类样本元数据表-通过id删除", notes="地物分类样本元数据表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		try {
			rssLcSampleService.removeById(id);
		} catch (Exception e) {
			log.error("删除失败",e.getMessage());
			return Result.error("删除失败!");
		}
		return Result.ok("删除成功!");
	}
	
	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "地物分类样本元数据表-批量删除")
	//@ApiOperation(value="地物分类样本元数据表-批量删除", notes="地物分类样本元数据表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<RssLcSample> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<RssLcSample> result = new Result<RssLcSample>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		}else {
			this.rssLcSampleService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}
	
	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "地物分类样本元数据表-通过id查询")
	//@ApiOperation(value="地物分类样本元数据表-通过id查询", notes="地物分类样本元数据表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<RssLcSample> queryById(@RequestParam(name="id",required=true) String id) {
		Result<RssLcSample> result = new Result<RssLcSample>();
		RssLcSample rssLcSample = rssLcSampleService.getById(id);
		if(rssLcSample==null) {
			result.error500("未找到对应实体");
		}else {
			result.setResult(rssLcSample);
			result.setSuccess(true);
		}
		return result;
	}

  /**
      * 导出excel
   *
   * @param request
   * @param response
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, HttpServletResponse response) {
      // Step.1 组装查询条件
      QueryWrapper<RssLcSample> queryWrapper = null;
      try {
          String paramsStr = request.getParameter("paramsStr");
          if (oConvertUtils.isNotEmpty(paramsStr)) {
              String deString = URLDecoder.decode(paramsStr, "UTF-8");
              RssLcSample rssLcSample = JSON.parseObject(deString, RssLcSample.class);
              queryWrapper = QueryGenerator.initQueryWrapper(rssLcSample, request.getParameterMap());
          }
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }

      //Step.2 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<RssLcSample> pageList = rssLcSampleService.list(queryWrapper);
      //导出文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "地物分类样本元数据表列表");
      mv.addObject(NormalExcelConstants.CLASS, RssLcSample.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("地物分类样本元数据表列表数据", "导出人:Jeecg", "导出信息"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
  }

  /**
      * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
      for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          MultipartFile file = entity.getValue();// 获取上传文件对象
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<RssLcSample> listRssLcSamples = ExcelImportUtil.importExcel(file.getInputStream(), RssLcSample.class, params);
              rssLcSampleService.saveBatch(listRssLcSamples);
              return Result.ok("文件导入成功！数据行数:" + listRssLcSamples.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("文件导入失败:"+e.getMessage());
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.ok("文件导入失败！");
  }

}
