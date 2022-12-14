package cn.whu.geois.modules.system.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.common.system.query.QueryGenerator;
import cn.whu.geois.common.system.vo.DictModel;
import cn.whu.geois.common.util.oConvertUtils;
import cn.whu.geois.modules.system.entity.SysDict;
import cn.whu.geois.modules.system.entity.SysDictItem;
import cn.whu.geois.modules.system.model.SysDictTree;
import cn.whu.geois.modules.system.service.ISysDictItemService;
import cn.whu.geois.modules.system.service.ISysDictService;
import cn.whu.geois.modules.system.vo.SysDictPage;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * ????????? ???????????????
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@RestController
@RequestMapping("/sys/dict")
@Slf4j
public class SysDictController {

	@Autowired
	private ISysDictService sysDictService;

	@Autowired
	private ISysDictItemService sysDictItemService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDict>> queryPageList(SysDict sysDict, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
		Result<IPage<SysDict>> result = new Result<IPage<SysDict>>();
		QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, req.getParameterMap());
		//queryWrapper.eq("del_flag", "1"); ????????????????????????
		Page<SysDict> page = new Page<SysDict>(pageNo, pageSize);
		IPage<SysDict> pageList = sysDictService.page(page, queryWrapper);
		log.info("??????????????????"+pageList.getCurrent());
		log.info("????????????????????????"+pageList.getSize());
		log.info("?????????????????????"+pageList.getRecords().size());
		log.info("???????????????"+pageList.getTotal());
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	 * @?????????????????????????????????
	 * @param sysDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/treeList", method = RequestMethod.GET)
	public Result<List<SysDictTree>> treeList(SysDict sysDict, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
		Result<List<SysDictTree>> result = new Result<>();
		LambdaQueryWrapper<SysDict> query = new LambdaQueryWrapper<>();
		// ??????????????????
		String dictName = sysDict.getDictName();
		if(oConvertUtils.isNotEmpty(dictName)) {
			query.like(true, SysDict::getDictName, dictName);
		}
		//query.eq(true, SysDict::getDelFlag, "1");
		query.orderByDesc(true, SysDict::getCreateTime);
		List<SysDict> list = sysDictService.list(query);
		List<SysDictTree> treeList = new ArrayList<>();
		for (SysDict node : list) {
			treeList.add(new SysDictTree(node));
		}
		result.setSuccess(true);
		result.setResult(treeList);
		return result;
	}

	/**
	 * ??????????????????
	 * @param dictCode ??????code
	 * @param dictCode ??????,????????????,code??????  | ?????????sys_user,realname,id
	 * @return
	 */
	@RequestMapping(value = "/getDictItems/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> getDictItems(@PathVariable String dictCode) {
		log.info(" dictCode : "+ dictCode);
		Result<List<DictModel>> result = new Result<List<DictModel>>();
		List<DictModel> ls = null;
		try {
			if(dictCode.indexOf(",")!=-1) {
				//???????????????????????????sys_user,realname,id???
				String[] params = dictCode.split(",");
				if(params.length!=3) {
					result.error500("??????Code??????????????????");
					return result;
				}
				ls = sysDictService.queryTableDictItemsByCode(params[0],params[1],params[2]);
			}else {
				//?????????
				 ls = sysDictService.queryDictItemsByCode(dictCode);
			}

			 result.setSuccess(true);
			 result.setResult(ls);
			 log.info(result.toString());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
			return result;
		}

		return result;
	}

	/**
	 * ??????????????????
	 * @param dictCode
	 * @return
	 */
	@RequestMapping(value = "/getDictText/{dictCode}/{key}", method = RequestMethod.GET)
	public Result<String> getDictItems(@PathVariable("dictCode") String dictCode, @PathVariable("key") String key) {
		log.info(" dictCode : "+ dictCode);
		Result<String> result = new Result<String>();
		String text = null;
		try {
			text = sysDictService.queryDictTextByKey(dictCode, key);
			 result.setSuccess(true);
			 result.setResult(text);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
			return result;
		}
		return result;
	}

	/**
	 * @???????????????
	 * @param sysDict
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysDict> add(@RequestBody SysDict sysDict) {
		Result<SysDict> result = new Result<SysDict>();
		try {
			sysDict.setCreateTime(new Date());
			sysDict.setDelFlag(0);
			sysDictService.save(sysDict);
			result.success("???????????????");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
		}
		return result;
	}

	/**
	 * @???????????????
	 * @param sysDict
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.PUT)
	public Result<SysDict> edit(@RequestBody SysDict sysDict) {
		Result<SysDict> result = new Result<SysDict>();
		SysDict sysdict = sysDictService.getById(sysDict.getId());
		if(sysdict==null) {
			result.error500("?????????????????????");
		}else {
			sysDict.setUpdateTime(new Date());
			boolean ok = sysDictService.updateById(sysDict);
			//TODO ??????false???????????????
			if(ok) {
				result.success("????????????!");
			}
		}
		return result;
	}

	/**
	 * @???????????????
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value="dictCache", allEntries=true)
	public Result<SysDict> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysDict> result = new Result<SysDict>();
		SysDict sysDict = sysDictService.getById(id);
		if(sysDict==null) {
			result.error500("?????????????????????");
		}else {
			//update-begin--Author:huangzhilin  Date:20140417 for???[bugfree???]????????????????????????????????????--------------------
			boolean ok = sysDictService.deleteByDictId(sysDict);
			//update-begin--Author:huangzhilin  Date:20140417 for???[bugfree???]????????????????????????????????????--------------------
			if(ok) {
				result.success("????????????!");
			}
		}
		return result;
	}

	/**
	 * @?????????????????????
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value="dictCache", allEntries=true)
	public Result<SysDict> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysDict> result = new Result<SysDict>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("??????????????????");
		}else {
			String[] id=ids.split(",");
			for(int i=0;i<id.length;i++) {
				SysDict sysDict = sysDictService.getById(id[i]);
				sysDict.setDelFlag(2);
				sysDictService.updateById(sysDict);
			}
			result.success("????????????!");
		}
		return result;
	}

	/**
	 * ??????excel
	 *
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysDict sysDict,HttpServletRequest request) {
		// Step.1 ??????????????????
		QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, request.getParameterMap());
		//Step.2 AutoPoi ??????Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<SysDictPage> pageList = new ArrayList<SysDictPage>();

		List<SysDict> sysDictList = sysDictService.list(queryWrapper);
		for (SysDict dictMain : sysDictList) {
			SysDictPage vo = new SysDictPage();
			BeanUtils.copyProperties(dictMain, vo);
			// ????????????
			List<SysDictItem> sysDictItemList = sysDictItemService.selectItemsByMainId(dictMain.getId());
			vo.setSysDictItemList(sysDictItemList);
			pageList.add(vo);
		}

		// ??????????????????
		mv.addObject(NormalExcelConstants.FILE_NAME, "????????????");
		// ????????????Class
		mv.addObject(NormalExcelConstants.CLASS, SysDictPage.class);
		// ?????????????????????
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("??????????????????", "?????????:Jeecg", "????????????"));
		// ??????????????????
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * ??????excel????????????
	 *
	 * @param request
	 * @param
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
			params.setHeadRows(2);
			params.setNeedSave(true);
			try {
				List<SysDictPage> list = ExcelImportUtil.importExcel(file.getInputStream(), SysDictPage.class, params);
				for (SysDictPage page : list) {
					SysDict po = new SysDict();
					BeanUtils.copyProperties(page, po);
					if(page.getDelFlag()==null){
					    po.setDelFlag(1);
                    }
					sysDictService.saveMain(po, page.getSysDictItemList());
				}
				return Result.ok("?????????????????????");
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("??????????????????:"+e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("?????????????????????");
	}

}
