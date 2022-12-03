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
import cn.whu.geois.modules.rssample.entity.RssGqjcClass;
import cn.whu.geois.modules.rssample.service.IRssGqjcClassService;
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
 * @Description: 地理国情监测分类体系表
 * @Author: jeecg-boot
 * @Date:   2021-01-29
 * @Version: V1.0
 */
@Slf4j
//@Api(tags="地理国情监测分类体系表")
@RestController
@RequestMapping("/rssample/rssGqjcClass")
public class RssGqjcClassController {
	@Autowired
	private IRssGqjcClassService rssGqjcClassService;
	
	/**
	  * 分页列表查询
	 * @param rssGqjcClass
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "地理国情监测分类体系表-分页列表查询")
	//@ApiOperation(value="地理国情监测分类体系表-分页列表查询", notes="地理国情监测分类体系表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<RssGqjcClass>> queryPageList(RssGqjcClass rssGqjcClass,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<RssGqjcClass>> result = new Result<IPage<RssGqjcClass>>();
		QueryWrapper<RssGqjcClass> queryWrapper = QueryGenerator.initQueryWrapper(rssGqjcClass, req.getParameterMap());
		Page<RssGqjcClass> page = new Page<RssGqjcClass>(pageNo, pageSize);
		IPage<RssGqjcClass> pageList = rssGqjcClassService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  *   添加
	 * @param rssGqjcClass
	 * @return
	 */
	@AutoLog(value = "地理国情监测分类体系表-添加")
	//@ApiOperation(value="地理国情监测分类体系表-添加", notes="地理国情监测分类体系表-添加")
	@PostMapping(value = "/add")
	public Result<RssGqjcClass> add(@RequestBody RssGqjcClass rssGqjcClass) {
		Result<RssGqjcClass> result = new Result<RssGqjcClass>();
		try {
			rssGqjcClassService.save(rssGqjcClass);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  *  编辑
	 * @param rssGqjcClass
	 * @return
	 */
	@AutoLog(value = "地理国情监测分类体系表-编辑")
	//@ApiOperation(value="地理国情监测分类体系表-编辑", notes="地理国情监测分类体系表-编辑")
	@PutMapping(value = "/edit")
	public Result<RssGqjcClass> edit(@RequestBody RssGqjcClass rssGqjcClass) {
		Result<RssGqjcClass> result = new Result<RssGqjcClass>();
		RssGqjcClass rssGqjcClassEntity = rssGqjcClassService.getById(rssGqjcClass.getId());
		if(rssGqjcClassEntity==null) {
			result.error500("未找到对应实体");
		}else {
			boolean ok = rssGqjcClassService.updateById(rssGqjcClass);
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
	@AutoLog(value = "地理国情监测分类体系表-通过id删除")
	//@ApiOperation(value="地理国情监测分类体系表-通过id删除", notes="地理国情监测分类体系表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		try {
			rssGqjcClassService.removeById(id);
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
	@AutoLog(value = "地理国情监测分类体系表-批量删除")
	//@ApiOperation(value="地理国情监测分类体系表-批量删除", notes="地理国情监测分类体系表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<RssGqjcClass> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<RssGqjcClass> result = new Result<RssGqjcClass>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		}else {
			this.rssGqjcClassService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}
	
	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "地理国情监测分类体系表-通过id查询")
	//@ApiOperation(value="地理国情监测分类体系表-通过id查询", notes="地理国情监测分类体系表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<RssGqjcClass> queryById(@RequestParam(name="id",required=true) String id) {
		Result<RssGqjcClass> result = new Result<RssGqjcClass>();
		RssGqjcClass rssGqjcClass = rssGqjcClassService.getById(id);
		if(rssGqjcClass==null) {
			result.error500("未找到对应实体");
		}else {
			result.setResult(rssGqjcClass);
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
      QueryWrapper<RssGqjcClass> queryWrapper = null;
      try {
          String paramsStr = request.getParameter("paramsStr");
          if (oConvertUtils.isNotEmpty(paramsStr)) {
              String deString = URLDecoder.decode(paramsStr, "UTF-8");
              RssGqjcClass rssGqjcClass = JSON.parseObject(deString, RssGqjcClass.class);
              queryWrapper = QueryGenerator.initQueryWrapper(rssGqjcClass, request.getParameterMap());
          }
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }

      //Step.2 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<RssGqjcClass> pageList = rssGqjcClassService.list(queryWrapper);
      //导出文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "地理国情监测分类体系表列表");
      mv.addObject(NormalExcelConstants.CLASS, RssGqjcClass.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("地理国情监测分类体系表列表数据", "导出人:Jeecg", "导出信息"));
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
              List<RssGqjcClass> listRssGqjcClasss = ExcelImportUtil.importExcel(file.getInputStream(), RssGqjcClass.class, params);
              rssGqjcClassService.saveBatch(listRssGqjcClasss);
              return Result.ok("文件导入成功！数据行数:" + listRssGqjcClasss.size());
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
