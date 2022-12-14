package cn.whu.geois.modules.demo.test.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.whu.geois.common.api.vo.Result;
import cn.whu.geois.common.system.query.QueryGenerator;
import cn.whu.geois.modules.demo.test.entity.JeecgOrderCustomer;
import cn.whu.geois.modules.demo.test.entity.JeecgOrderMain;
import cn.whu.geois.modules.demo.test.entity.JeecgOrderTicket;
import cn.whu.geois.modules.demo.test.service.IJeecgOrderCustomerService;
import cn.whu.geois.modules.demo.test.service.IJeecgOrderMainService;
import cn.whu.geois.modules.demo.test.service.IJeecgOrderTicketService;
import cn.whu.geois.modules.demo.test.vo.JeecgOrderMainPage;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Title: Controller
 * @Description: ?????? 
 * @Author: jeecg-boot 
 * @Date:2019-02-15 
 * @Version: V1.0
 */
@RestController
@RequestMapping("/test/jeecgOrderMain")
@Slf4j
public class JeecgOrderMainController {
	@Autowired
	private IJeecgOrderMainService jeecgOrderMainService;
	@Autowired
	private IJeecgOrderCustomerService jeecgOrderCustomerService;
	@Autowired
	private IJeecgOrderTicketService jeecgOrderTicketService;

	/**
	 * ??????????????????
	 * 
	 * @param jeecgOrderMain
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<IPage<JeecgOrderMain>> queryPageList(JeecgOrderMain jeecgOrderMain, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		Result<IPage<JeecgOrderMain>> result = new Result<IPage<JeecgOrderMain>>();
		QueryWrapper<JeecgOrderMain> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderMain, req.getParameterMap());
		Page<JeecgOrderMain> page = new Page<JeecgOrderMain>(pageNo, pageSize);
		IPage<JeecgOrderMain> pageList = jeecgOrderMainService.page(page, queryWrapper);
		// log.debug("??????????????????"+pageList.getCurrent());
		// log.debug("????????????????????????"+pageList.getSize());
		// log.debug("?????????????????????"+pageList.getRecords().size());
		// log.debug("???????????????"+pageList.getTotal());
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	 * ??????
	 * 
	 * @param jeecgOrderMain
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<JeecgOrderMain> add(@RequestBody JeecgOrderMainPage jeecgOrderMainPage) {
		Result<JeecgOrderMain> result = new Result<JeecgOrderMain>();
		try {
			JeecgOrderMain jeecgOrderMain = new JeecgOrderMain();
			BeanUtils.copyProperties(jeecgOrderMainPage, jeecgOrderMain);
			jeecgOrderMainService.saveMain(jeecgOrderMain, jeecgOrderMainPage.getJeecgOrderCustomerList(), jeecgOrderMainPage.getJeecgOrderTicketList());
			result.success("???????????????");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
		}
		return result;
	}

	/**
	 * ??????
	 * 
	 * @param jeecgOrderMain
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<JeecgOrderMain> eidt(@RequestBody JeecgOrderMainPage jeecgOrderMainPage) {
		Result<JeecgOrderMain> result = new Result<JeecgOrderMain>();
		JeecgOrderMain jeecgOrderMain = new JeecgOrderMain();
		BeanUtils.copyProperties(jeecgOrderMainPage, jeecgOrderMain);
		JeecgOrderMain jeecgOrderMainEntity = jeecgOrderMainService.getById(jeecgOrderMain.getId());
		if (jeecgOrderMainEntity == null) {
			result.error500("?????????????????????");
		} else {
			jeecgOrderMainService.updateMain(jeecgOrderMain, jeecgOrderMainPage.getJeecgOrderCustomerList(), jeecgOrderMainPage.getJeecgOrderTicketList());
			result.success("????????????!");
		}

		return result;
	}

	/**
	 * ??????id??????
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		try {
			jeecgOrderMainService.delMain(id);
		} catch (Exception e) {
			log.error("????????????",e.getMessage());
			return Result.error("????????????!");
		}
		return Result.ok("????????????!");
		
	}

	/**
	 * ????????????
	 * 
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<JeecgOrderMain> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		Result<JeecgOrderMain> result = new Result<JeecgOrderMain>();
		if (ids == null || "".equals(ids.trim())) {
			result.error500("??????????????????");
		} else {
			this.jeecgOrderMainService.delBatchMain(Arrays.asList(ids.split(",")));
			result.success("????????????!");
		}
		return result;
	}

	/**
	 * ??????id??????
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<JeecgOrderMain> queryById(@RequestParam(name = "id", required = true) String id) {
		Result<JeecgOrderMain> result = new Result<JeecgOrderMain>();
		JeecgOrderMain jeecgOrderMain = jeecgOrderMainService.getById(id);
		if (jeecgOrderMain == null) {
			result.error500("?????????????????????");
		} else {
			result.setResult(jeecgOrderMain);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 * ??????id??????
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryOrderCustomerListByMainId")
	public Result<List<JeecgOrderCustomer>> queryOrderCustomerListByMainId(@RequestParam(name = "id", required = true) String id) {
		Result<List<JeecgOrderCustomer>> result = new Result<List<JeecgOrderCustomer>>();
		List<JeecgOrderCustomer> jeecgOrderCustomerList = jeecgOrderCustomerService.selectCustomersByMainId(id);
		result.setResult(jeecgOrderCustomerList);
		result.setSuccess(true);
		return result;
	}

	/**
	 * ??????id??????
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryOrderTicketListByMainId")
	public Result<List<JeecgOrderTicket>> queryOrderTicketListByMainId(@RequestParam(name = "id", required = true) String id) {
		Result<List<JeecgOrderTicket>> result = new Result<List<JeecgOrderTicket>>();
		List<JeecgOrderTicket> jeecgOrderTicketList = jeecgOrderTicketService.selectTicketsByMainId(id);
		result.setResult(jeecgOrderTicketList);
		result.setSuccess(true);
		return result;
	}

	/**
	 * ??????excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, JeecgOrderMain jeecgOrderMain) {
		// Step.1 ??????????????????
		QueryWrapper<JeecgOrderMain> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderMain, request.getParameterMap());
		//Step.2 AutoPoi ??????Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<JeecgOrderMainPage> pageList = new ArrayList<JeecgOrderMainPage>();

		List<JeecgOrderMain> jeecgOrderMainList = jeecgOrderMainService.list(queryWrapper);
		for (JeecgOrderMain orderMain : jeecgOrderMainList) {
			JeecgOrderMainPage vo = new JeecgOrderMainPage();
			BeanUtils.copyProperties(orderMain, vo);
			// ????????????
			List<JeecgOrderTicket> jeecgOrderTicketList = jeecgOrderTicketService.selectTicketsByMainId(orderMain.getId());
			vo.setJeecgOrderTicketList(jeecgOrderTicketList);
			// ????????????
			List<JeecgOrderCustomer> jeecgOrderCustomerList = jeecgOrderCustomerService.selectCustomersByMainId(orderMain.getId());
			vo.setJeecgOrderCustomerList(jeecgOrderCustomerList);
			pageList.add(vo);
		}

		// ??????????????????
		mv.addObject(NormalExcelConstants.FILE_NAME, "???????????????????????????");
		// ????????????Class
		mv.addObject(NormalExcelConstants.CLASS, JeecgOrderMainPage.class);
		// ?????????????????????
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("???????????????Excel????????????", "?????????:Jeecg", "?????????Sheet??????"));
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
				List<JeecgOrderMainPage> list = ExcelImportUtil.importExcel(file.getInputStream(), JeecgOrderMainPage.class, params);
				for (JeecgOrderMainPage page : list) {
					JeecgOrderMain po = new JeecgOrderMain();
					BeanUtils.copyProperties(page, po);
					jeecgOrderMainService.saveMain(po, page.getJeecgOrderCustomerList(), page.getJeecgOrderTicketList());
				}
				return Result.ok("?????????????????????");
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("?????????????????????"+e.getMessage());
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
