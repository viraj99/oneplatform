package com.oneplatform.system.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.common.model.SelectOption;
import com.jeesuite.security.client.LoginContext;
import com.jeesuite.springweb.model.WrapperResponse;
import com.oneplatform.base.GlobalContants;
import com.oneplatform.base.GlobalContants.ModuleType;
import com.oneplatform.base.annotation.ApiPermOptions;
import com.oneplatform.base.constants.PermissionType;
import com.oneplatform.base.model.SwitchParam;
import com.oneplatform.platform.PlatformConfigManager;
import com.oneplatform.system.dao.entity.ModuleEntity;
import com.oneplatform.system.service.ModuleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Controller
@RequestMapping("/module")
@Api("Module Management API")
@ApiPermOptions(perms = PermissionType.Authorized)
public class ModuleController {

	private @Autowired ModuleService moduleService;
	private @Autowired PlatformConfigManager configManager;
	
	@ApiOperation(value = "查询所有模块")
	@RequestMapping(value = "list", method = RequestMethod.GET)
    public @ResponseBody WrapperResponse<List<ModuleEntity>> getAllModules() {
		List<ModuleEntity> modules = moduleService.findEnabledModules();
		return new WrapperResponse<>(modules);
	}
	
	@ApiOperation(value = "按id查询")
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
    public @ResponseBody WrapperResponse<ModuleEntity> getById(@PathVariable("id") int id) {
		ModuleEntity entity = moduleService.getmoduleDetails(id);
		return new WrapperResponse<>(entity);
	}

	@ApiOperation(value = "启用/停止模块")
	@RequestMapping(value = "switch", method = RequestMethod.POST)
    public @ResponseBody WrapperResponse<String> switchModule(@RequestBody SwitchParam param) {
		moduleService.switchModule(LoginContext.getIntFormatUserId(), param.getId(),param.getValue());
		return new WrapperResponse<>();
	}
	
	@ApiOperation(value = "查询所有模块（下拉框选项）")
	@RequestMapping(value = "options", method = RequestMethod.GET)
	@ApiPermOptions(perms = PermissionType.Logined)
    public @ResponseBody WrapperResponse<List<SelectOption>> getAllModulesAsSelectOption() {
		List<ModuleEntity> modules = moduleService.findEnabledModules();
		
		List<SelectOption> options = new ArrayList<>();
		for (ModuleEntity moduleEntity : modules) {
			if(!moduleEntity.getEnabled())continue;
			options.add(new SelectOption(moduleEntity.getId().toString(), moduleEntity.getName()));
		}
		return new WrapperResponse<>(options);
	}
	
	@ApiOperation(value = "查询所有模块路径")
	@RequestMapping(value = "basepaths", method = RequestMethod.GET)
	@ApiPermOptions(perms = PermissionType.Logined)
    public @ResponseBody WrapperResponse<Map<String, String>> getAllBasePaths() {
		Map<String, String> resDatas = new HashMap<>();
		resDatas.put("_default", configManager.getContextpth());
		List<ModuleEntity> modules = moduleService.findEnabledModules();
		String path;
		for (ModuleEntity module : modules) {
			if(ModuleType.plugin.name().equals(module.getModuleType()))continue;
			if(module.getServiceId().equalsIgnoreCase(GlobalContants.MODULE_NAME))continue;
			if(module.isIndependentDeploy()){				
				path = configManager.getContextpth() + "/" + module.getRouteName();
			}else{
				path = configManager.getContextpth();
			}
			resDatas.put(module.getRouteName(), path);
		}
		return new WrapperResponse<>(resDatas);
	}
    
}
