package com.shenke.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.tomcat.jni.Procattr;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shenke.entity.JiTai;
import com.shenke.entity.JitaiProductionAllot;
import com.shenke.entity.Log;
import com.shenke.entity.ProductionProcess;
import com.shenke.entity.SaleListProduct;
import com.shenke.service.JiTaiService;
import com.shenke.service.JitaiProductionAllotService;
import com.shenke.service.LogService;
import com.shenke.service.ProductionProcessService;
import com.shenke.service.SaleListProductService;
import com.shenke.util.StringUtil;

/**
 * 机台生产分配Controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/admin/jitaiProductionAllot")
public class JitaiProductionAllotController {

	@Resource
	private JitaiProductionAllotService jitaiProductionAllotService;

	@Resource
	private LogService logService;

	@Resource
	private JiTaiService jiTaiService;

	@Resource
	private SaleListProductService saleListProductService;

	@Resource
	private ProductionProcessService productionProcessService;

	/**
	 * 查询所有生产通知单
	 * 
	 * @return
	 */
	@RequestMapping("/list")
	public List<JitaiProductionAllot> list() {
		logService.save(new Log(Log.SEARCH_ACTION, "查询生产通知单"));
		return jitaiProductionAllotService.list();
	}

	/**
	 * 分组查询所有生产通知单
	 * 
	 * @return
	 */
	@RequestMapping("/listGroubBy")
	public List<JitaiProductionAllot> listGroubBy() {
		logService.save(new Log(Log.SEARCH_ACTION, "查询生产通知单"));
		return jitaiProductionAllotService.listGroubBy();
	}

	/**
	 * 根据条件查询生产通知单信息
	 * 
	 * @return
	 */
	@RequestMapping("/screen")
	public Map<String, Object> screen(String allorTime, Integer jitai) {
		Map<String, Object> map = new HashMap<String, Object>();
		logService.save(new Log(Log.SEARCH_ACTION, "查询生产通知单"));
		map.put("rows", jitaiProductionAllotService.screen(allorTime, jitai));
		map.put("success", true);
		return map;
	}

	/**
	 * 生产通知单下发
	 * 
	 * @return
	 */
	@RequestMapping("/issue")
	public Map<String, Object> issue(String idStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] ids = idStr.split(",");
		for (int i = 0; i < ids.length; i++) {
			logService.save(new Log(Log.UPDATE_ACTION, "下发机台"));
			List<JitaiProductionAllot> jitaiProductionAllots = jitaiProductionAllotService
					.findBySaleListProductId(Integer.parseInt(ids[i]));
			for (JitaiProductionAllot jitaiProductionAllot2 : jitaiProductionAllots) {
				productionProcessService.updateState("下发机台：" + jitaiProductionAllot2.getJiTai().getName(),
						jitaiProductionAllot2.getSaleListProduct().getId());
				saleListProductService.updateState("下发机台：" + jitaiProductionAllot2.getJiTai().getName(),
						jitaiProductionAllot2.getSaleListProduct().getId());
				jitaiProductionAllotService.issue(" 下发机台：" + jitaiProductionAllot2.getJiTai().getName(),
						jitaiProductionAllot2.getId());
				productionProcessService.updateStateBySaleListProductId(" 下发机台：" + jitaiProductionAllot2.getJiTai().getName(),
						jitaiProductionAllot2.getSaleListProduct().getId());
			}
		}
		map.put("success", true);
		return map;
	}

	/**
	 * 根据通知单号修改所分配的机台
	 * 
	 * @return
	 */
	@RequestMapping("/alertJitai")
	public Map<String, Object> alertJitai(String informNumberStr, Integer jitai) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] informNumbers = informNumberStr.split(",");
		System.out.println(informNumbers.length);
		List<JitaiProductionAllot> jitaiProductionAllots = new ArrayList<JitaiProductionAllot>();
		JiTai jiTai2 = jiTaiService.findById(jitai);
		Set<Integer> saleListIds = new HashSet<Integer>();
		for (int i = 0; i < informNumbers.length; i++) {
			logService.save(new Log(Log.UPDATE_ACTION, "修改分配机台"));
			jitaiProductionAllots
					.addAll(jitaiProductionAllotService.findByImformNubers(Integer.parseInt(informNumbers[i])));
			jitaiProductionAllotService.updateJitai(Integer.parseInt(informNumbers[i]), jitai);
		}
		for (JitaiProductionAllot jitaiProductionAllot : jitaiProductionAllots) {
			saleListIds.add(jitaiProductionAllot.getSaleListProduct().getId());
		}
		for (Integer integer : saleListIds) {
			saleListProductService.updateState("分配机台：" + jiTai2.getName(), integer);
		}
		map.put("success", true);
		return map;
	}

	/**
	 * 根据机台id查询所有分配到该机台的销售商品信息
	 * 
	 * @param jitaiId
	 * @return
	 */
	@RequestMapping("/selectSaleListProductByJitaiId")
	public Map<String, Object> selectSaleListProductByJitaiId(Integer jitaiId) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<SaleListProduct> saleListProducts = jitaiProductionAllotService.selectSaleListProductByJitaiId(jitaiId);
		map.put("success", true);
		map.put("rows", saleListProducts);
		return map;
	}

	/**
	 * 查询该机台下的所有通知单号
	 * 
	 * @return
	 */
	@RequestMapping("/selectAllInformByJitaiId")
	public Map<String, Object> selectAllInformByJitaiId(Integer jitaiId) {
		Map<String, Object> map = new HashMap<String, Object>();
		Set<Integer> selectAllInformByJitaiId = jitaiProductionAllotService.selectAllInformByJitaiId(jitaiId);
		map.put("success", true);
		map.put("informs", selectAllInformByJitaiId);
		return map;
	}

	/**
	 * 根据机台id和通知单号查询对应的销售商品信息
	 * 
	 * @return
	 */
	@RequestMapping("/selectAllByInformAndJitaiId")
	public Map<String, Object> selectAllByInformAndJitaiId(Integer jitai, String informNumber) {
		System.out.println("jitai" + "===" + jitai);
		System.out.println("informNumber" + "===" + informNumber);
		Map<String, Object> map = new HashMap<String, Object>();
		List<SaleListProduct> saleListProducts = jitaiProductionAllotService.selectAllByInformAndJitaiId(jitai,
				informNumber);
		map.put("success", true);
		map.put("rows", saleListProducts);
		return map;
	}

	/**
	 * 根据SaleListProductId查询生产通知单
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/selectBySaleListProductId")
	public Map<String, Object> selectBySaleListProductId(Integer id) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
		map.put("rows", jitaiProductionAllotService.selectBySaleListProductId(id));
		return map;
	}

}
