package cn.whu.geois.modules.system.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.common.system.query.QueryGenerator;
import cn.whu.geois.common.aspect.annotation.AutoLog;
import cn.whu.geois.common.util.oConvertUtils;
import cn.whu.geois.modules.system.entity.SysCountryList;
import cn.whu.geois.modules.system.service.ISysCountryListService;

import com.alibaba.fastjson.JSONObject;
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
 * @Description: 国家地区表
 * @Author: jeecg-boot
 * @Date:   2021-12-08
 * @Version: V1.0
 */
@Slf4j
//@Api(tags="国家地区表")
@RestController
@RequestMapping("/system/sysCountryList")
public class SysCountryListController {
	@Autowired
	private ISysCountryListService sysCountryListService;

	/**
	  * 分页列表查询
	 * @param sysCountryList
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "国家地区表-分页列表查询")
//	@ApiOperation(value="国家地区表-分页列表查询", notes="国家地区表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<SysCountryList>> queryPageList(SysCountryList sysCountryList,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<SysCountryList>> result = new Result<IPage<SysCountryList>>();
		QueryWrapper<SysCountryList> queryWrapper = QueryGenerator.initQueryWrapper(sysCountryList, req.getParameterMap());
		Page<SysCountryList> page = new Page<SysCountryList>(pageNo, pageSize);
		IPage<SysCountryList> pageList = sysCountryListService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	  *   添加
	 * @param sysCountryList
	 * @return
	 */
	@AutoLog(value = "国家地区表-添加")
//	@ApiOperation(value="国家地区表-添加", notes="国家地区表-添加")
	@PostMapping(value = "/add")
	public Result<SysCountryList> add(@RequestBody SysCountryList sysCountryList) {
		Result<SysCountryList> result = new Result<SysCountryList>();
		try {
			sysCountryListService.save(sysCountryList);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}

	/**
	  *  编辑
	 * @param sysCountryList
	 * @return
	 */
	@AutoLog(value = "国家地区表-编辑")
//	@ApiOperation(value="国家地区表-编辑", notes="国家地区表-编辑")
	@PutMapping(value = "/edit")
	public Result<SysCountryList> edit(@RequestBody SysCountryList sysCountryList) {
		Result<SysCountryList> result = new Result<SysCountryList>();
		SysCountryList sysCountryListEntity = sysCountryListService.getById(sysCountryList.getId());
		if(sysCountryListEntity==null) {
			result.error500("未找到对应实体");
		}else {
			boolean ok = sysCountryListService.updateById(sysCountryList);
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
	@AutoLog(value = "国家地区表-通过id删除")
//	@ApiOperation(value="国家地区表-通过id删除", notes="国家地区表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		try {
			sysCountryListService.removeById(id);
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
	@AutoLog(value = "国家地区表-批量删除")
	//@ApiOperation(value="国家地区表-批量删除", notes="国家地区表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysCountryList> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysCountryList> result = new Result<SysCountryList>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		}else {
			this.sysCountryListService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}

	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "国家地区表-通过id查询")
	//@ApiOperation(value="国家地区表-通过id查询", notes="国家地区表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<SysCountryList> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysCountryList> result = new Result<SysCountryList>();
		SysCountryList sysCountryList = sysCountryListService.getById(id);
		if(sysCountryList==null) {
			result.error500("未找到对应实体");
		}else {
			result.setResult(sysCountryList);
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
      QueryWrapper<SysCountryList> queryWrapper = null;
      try {
          String paramsStr = request.getParameter("paramsStr");
          if (oConvertUtils.isNotEmpty(paramsStr)) {
              String deString = URLDecoder.decode(paramsStr, "UTF-8");
              SysCountryList sysCountryList = JSON.parseObject(deString, SysCountryList.class);
              queryWrapper = QueryGenerator.initQueryWrapper(sysCountryList, request.getParameterMap());
          }
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }

      //Step.2 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<SysCountryList> pageList = sysCountryListService.list(queryWrapper);
      //导出文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "国家地区表列表");
      mv.addObject(NormalExcelConstants.CLASS, SysCountryList.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("国家地区表列表数据", "导出人:Jeecg", "导出信息"));
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
              List<SysCountryList> listSysCountryLists = ExcelImportUtil.importExcel(file.getInputStream(), SysCountryList.class, params);
              sysCountryListService.saveBatch(listSysCountryLists);
              return Result.ok("文件导入成功！数据行数:" + listSysCountryLists.size());
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

	 @RequestMapping(value="/queryCountryList")
	 public Result<JSONObject> queryCountryList(@RequestParam("locale")String locale){
		 Result result = new Result();
		 List<JSONObject> jsonObjectList = new ArrayList<>();
		 jsonObjectList = sysCountryListService.getCountryList(locale);
		 result.setResult(jsonObjectList);
		 result.setCode(200);
		 return result;

	 }

}
