package cn.whu.geois.modules.system.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.whu.geois.common.constant.CacheConstant;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.common.system.query.QueryGenerator;
import cn.whu.geois.common.system.util.JwtUtil;
import cn.whu.geois.common.system.vo.LoginUser;
import cn.whu.geois.modules.system.entity.SysDepart;
import cn.whu.geois.modules.system.model.DepartIdModel;
import cn.whu.geois.modules.system.model.SysDepartTreeModel;
import cn.whu.geois.modules.system.service.ISysDepartService;
import cn.whu.geois.modules.system.service.ISysUserDepartService;
import cn.whu.geois.modules.system.service.ISysUserService;
import cn.whu.geois.modules.system.util.FindsDepartsChildrenUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ????????? ???????????????
 * <p>
 *
 * @Author: Steve @Since??? 2019-01-22
 */
@RestController
@RequestMapping("/sys/sysDepart")
@Slf4j
public class SysDepartController {

	@Autowired
	private ISysDepartService sysDepartService;

	/**
	 * ???????????? ??????????????????,??????????????????????????????????????????
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryTreeList() {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			// ??????????????????
//			List<SysDepartTreeModel> list =FindsDepartsChildrenUtil.getSysDepartTreeList();
//			if (CollectionUtils.isEmpty(list)) {
//				list = sysDepartService.queryTreeList();
//			}
			List<SysDepartTreeModel> list = sysDepartService.queryTreeList();
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * ??????????????? ???????????????????????????????????????,?????????????????????
	 *
	 * @param sysDepart
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
    @CacheEvict(value= {CacheConstant.DEPART_INFO_CACHE,CacheConstant.DEPART_IDMODEL_CACHE}, allEntries=true)
	public Result<SysDepart> add(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		Result<SysDepart> result = new Result<SysDepart>();
		String username = JwtUtil.getUserNameByToken(request);
		try {
			sysDepart.setCreateBy(username);
			sysDepartService.saveDepartData(sysDepart, username);
            //?????????????????????
            // FindsDepartsChildrenUtil.clearSysDepartTreeList();
            // FindsDepartsChildrenUtil.clearDepartIdModel();
			result.success("???????????????");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
		}
		return result;
	}

	/**
	 * ???????????? ???????????????????????????,?????????????????????
	 *
	 * @param sysDepart
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.PUT)
    @CacheEvict(value= {CacheConstant.DEPART_INFO_CACHE,CacheConstant.DEPART_IDMODEL_CACHE}, allEntries=true)
	public Result<SysDepart> edit(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		String username = JwtUtil.getUserNameByToken(request);
		sysDepart.setUpdateBy(username);
		Result<SysDepart> result = new Result<SysDepart>();
		SysDepart sysDepartEntity = sysDepartService.getById(sysDepart.getId());
		if (sysDepartEntity == null) {
			result.error500("?????????????????????");
		} else {
			boolean ok = sysDepartService.updateDepartDataById(sysDepart, username);
			// TODO ??????false???????????????
			if (ok) {
                //?????????????????????
                //FindsDepartsChildrenUtil.clearSysDepartTreeList();
                //FindsDepartsChildrenUtil.clearDepartIdModel();
				result.success("????????????!");
			}
		}
		return result;
	}

	/**
	 *   ??????id??????
	 * @param id
	 * @return
	 */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @CacheEvict(value= {CacheConstant.DEPART_INFO_CACHE,CacheConstant.DEPART_IDMODEL_CACHE}, allEntries=true)
	public Result<SysDepart> delete(@RequestParam(name="id",required=true) String id) {

		Result<SysDepart> result = new Result<SysDepart>();
		SysDepart sysDepart = sysDepartService.getById(id);
		if(sysDepart==null) {
			result.error500("?????????????????????");
		}else {
			boolean ok = sysDepartService.delete(id);
			if(ok) {
                //?????????????????????
                //FindsDepartsChildrenUtil.clearSysDepartTreeList();
                // FindsDepartsChildrenUtil.clearDepartIdModel();
				result.success("????????????!");
			}
		}
		return result;
	}


	/**
	 * ???????????? ???????????????????????????ID,???????????????????????????????????????????????????
	 *
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    @CacheEvict(value= {CacheConstant.DEPART_INFO_CACHE,CacheConstant.DEPART_IDMODEL_CACHE}, allEntries=true)
	public Result<SysDepart> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

		Result<SysDepart> result = new Result<SysDepart>();
		if (ids == null || "".equals(ids.trim())) {
			result.error500("??????????????????");
		} else {
            this.sysDepartService.deleteBatchWithChildren(Arrays.asList(ids.split(",")));
			result.success("????????????!");
		}
		return result;
	}

	/**
	 * ???????????? ?????????????????????????????????????????????,?????????????????????????????????????????????,?????????????????????
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryIdTree", method = RequestMethod.GET)
	public Result<List<DepartIdModel>> queryIdTree() {
//      Result<List<DepartIdModel>> result = new Result<List<DepartIdModel>>();
//		List<DepartIdModel> idList;
//		try {
//			idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//			if (idList != null && idList.size() > 0) {
//				result.setResult(idList);
//				result.setSuccess(true);
//			} else {
//				sysDepartService.queryTreeList();
//				idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//				result.setResult(idList);
//				result.setSuccess(true);
//			}
//			return result;
//		} catch (Exception e) {
//			log.error(e.getMessage(),e);
//			result.setSuccess(false);
//			return result;
//		}
        Result<List<DepartIdModel>> result = new Result<>();
		try {
            List<DepartIdModel> list = sysDepartService.queryDepartIdTreeList();
            result.setResult(list);
            result.setSuccess(true);
		} catch (Exception e) {
            log.error(e.getMessage(),e);
		}
        return result;
	}

	/**
	 * <p>
	 * ????????????????????????,???????????????????????????????????????
	 * </p>
	 *
	 * @param keyWord
	 * @return
	 */
	@RequestMapping(value = "/searchBy", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> searchBy(@RequestParam(name = "keyWord", required = true) String keyWord) {
		Result<List<SysDepartTreeModel>> result = new Result<List<SysDepartTreeModel>>();
		try {
			List<SysDepartTreeModel> treeList = this.sysDepartService.searhBy(keyWord);
			if (treeList.size() == 0 || treeList == null) {
				throw new Exception();
			}
			result.setSuccess(true);
			result.setResult(treeList);
			return result;
		} catch (Exception e) {
			e.fillInStackTrace();
			result.setSuccess(false);
			result.setMessage("?????????????????????????????????????????????!");
			return result;
		}
	}


	/**
	 * ??????excel
	 *
	 * @param request
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysDepart sysDepart,HttpServletRequest request) {
		// Step.1 ??????????????????
		QueryWrapper<SysDepart> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, request.getParameterMap());
		//Step.2 AutoPoi ??????Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<SysDepart> pageList = sysDepartService.list(queryWrapper);
		//???????????????
		Collections.sort(pageList, new Comparator<SysDepart>() {
			@Override
			public int compare(SysDepart arg0, SysDepart arg1) {
				return arg0.getOrgCode().compareTo(arg1.getOrgCode());
			}
		});
		//??????????????????
		mv.addObject(NormalExcelConstants.FILE_NAME, "????????????");
		mv.addObject(NormalExcelConstants.CLASS, SysDepart.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("??????????????????", "?????????:"+user.getRealname(), "????????????"));
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
				// orgCode????????????
				int codeLength = 3;
				List<SysDepart> listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), SysDepart.class, params);
				//???????????????
				Collections.sort(listSysDeparts, new Comparator<SysDepart>() {
					@Override
					public int compare(SysDepart arg0, SysDepart arg1) {
						return arg0.getOrgCode().length() - arg1.getOrgCode().length();
					}
				});
				for (SysDepart sysDepart : listSysDeparts) {
					String orgCode = sysDepart.getOrgCode();
					if(orgCode.length() > codeLength) {
						String parentCode = orgCode.substring(0, orgCode.length()-codeLength);
						QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<SysDepart>();
						queryWrapper.eq("org_code", parentCode);
						try {
							SysDepart parentDept = sysDepartService.getOne(queryWrapper);
							if(!parentDept.equals(null)) {
								sysDepart.setParentId(parentDept.getId());
							} else {
								sysDepart.setParentId("");
							}
						}catch (Exception e) {
							//???????????????parentDept
						}
					}else{
						sysDepart.setParentId("");
					}
					sysDepartService.save(sysDepart);
				}
				return Result.ok("????????????????????????????????????" + listSysDeparts.size());
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("??????????????????:"+e.getMessage());
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
