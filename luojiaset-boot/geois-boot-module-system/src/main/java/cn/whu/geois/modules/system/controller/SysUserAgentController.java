package cn.whu.geois.modules.system.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.common.system.query.QueryGenerator;
import cn.whu.geois.common.system.vo.LoginUser;
import cn.whu.geois.common.util.oConvertUtils;
import cn.whu.geois.modules.system.entity.SysUserAgent;
import cn.whu.geois.modules.system.service.ISysUserAgentService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

 /**
 * @Title: Controller
 * @Description: ?????????????????????
 * @Author: jeecg-boot
 * @Date:  2019-04-17
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/sysUserAgent")
@Slf4j
public class SysUserAgentController {
	@Autowired
	private ISysUserAgentService sysUserAgentService;
	
	/**
	  * ??????????????????
	 * @param sysUserAgent
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<IPage<SysUserAgent>> queryPageList(SysUserAgent sysUserAgent,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<SysUserAgent>> result = new Result<IPage<SysUserAgent>>();
		QueryWrapper<SysUserAgent> queryWrapper = QueryGenerator.initQueryWrapper(sysUserAgent, req.getParameterMap());
		Page<SysUserAgent> page = new Page<SysUserAgent>(pageNo, pageSize);
		IPage<SysUserAgent> pageList = sysUserAgentService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  *   ??????
	 * @param sysUserAgent
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<SysUserAgent> add(@RequestBody SysUserAgent sysUserAgent) {
		Result<SysUserAgent> result = new Result<SysUserAgent>();
		try {
			sysUserAgentService.save(sysUserAgent);
			result.success("????????????????????????");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
		}
		return result;
	}
	
	/**
	  *  ??????
	 * @param sysUserAgent
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<SysUserAgent> edit(@RequestBody SysUserAgent sysUserAgent) {
		Result<SysUserAgent> result = new Result<SysUserAgent>();
		SysUserAgent sysUserAgentEntity = sysUserAgentService.getById(sysUserAgent.getId());
		if(sysUserAgentEntity==null) {
			result.error500("?????????????????????");
		}else {
			boolean ok = sysUserAgentService.updateById(sysUserAgent);
			//TODO ??????false???????????????
			if(ok) {
				result.success("?????????????????????!");
			}
		}
		
		return result;
	}
	
	/**
	  *   ??????id??????
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<SysUserAgent> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysUserAgent> result = new Result<SysUserAgent>();
		SysUserAgent sysUserAgent = sysUserAgentService.getById(id);
		if(sysUserAgent==null) {
			result.error500("?????????????????????");
		}else {
			boolean ok = sysUserAgentService.removeById(id);
			if(ok) {
				result.success("????????????!");
			}
		}
		
		return result;
	}
	
	/**
	  *  ????????????
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysUserAgent> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysUserAgent> result = new Result<SysUserAgent>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("??????????????????");
		}else {
			this.sysUserAgentService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("????????????!");
		}
		return result;
	}
	
	/**
	  * ??????id??????
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<SysUserAgent> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysUserAgent> result = new Result<SysUserAgent>();
		SysUserAgent sysUserAgent = sysUserAgentService.getById(id);
		if(sysUserAgent==null) {
			result.error500("?????????????????????");
		}else {
			result.setResult(sysUserAgent);
			result.setSuccess(true);
		}
		return result;
	}
	
	/**
	  * ??????userName??????
	 * @param userName
	 * @return
	 */
	@GetMapping(value = "/queryByUserName")
	public Result<SysUserAgent> queryByUserName(@RequestParam(name="userName",required=true) String userName) {
		Result<SysUserAgent> result = new Result<SysUserAgent>();
		LambdaQueryWrapper<SysUserAgent> queryWrapper = new LambdaQueryWrapper<SysUserAgent>();
		queryWrapper.eq(SysUserAgent::getUserName, userName);
		SysUserAgent sysUserAgent = sysUserAgentService.getOne(queryWrapper);
		if(sysUserAgent==null) {
			result.error500("?????????????????????");
		}else {
			result.setResult(sysUserAgent);
			result.setSuccess(true);
		}
		return result;
	}

  /**
      * ??????excel
   *
   * @param request
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(SysUserAgent sysUserAgent,HttpServletRequest request) {
      // Step.1 ??????????????????
      QueryWrapper<SysUserAgent> queryWrapper = QueryGenerator.initQueryWrapper(sysUserAgent, request.getParameterMap());
      //Step.2 AutoPoi ??????Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<SysUserAgent> pageList = sysUserAgentService.list(queryWrapper);
      //??????????????????
      mv.addObject(NormalExcelConstants.FILE_NAME, "???????????????????????????");
      mv.addObject(NormalExcelConstants.CLASS, SysUserAgent.class);
      LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("?????????????????????????????????", "?????????:"+user.getRealname(), "????????????"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
  }

  /**
      * ??????excel????????????
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
          MultipartFile file = entity.getValue();// ????????????????????????
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<SysUserAgent> listSysUserAgents = ExcelImportUtil.importExcel(file.getInputStream(), SysUserAgent.class, params);
              for (SysUserAgent sysUserAgentExcel : listSysUserAgents) {
                  sysUserAgentService.save(sysUserAgentExcel);
              }
              return Result.ok("????????????????????????????????????" + listSysUserAgents.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("?????????????????????");
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.error("?????????????????????");
  }

}
