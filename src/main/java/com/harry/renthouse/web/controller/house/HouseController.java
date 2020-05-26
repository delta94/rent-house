package com.harry.renthouse.web.controller.house;

import com.fasterxml.jackson.annotation.JsonView;
import com.harry.renthouse.base.ApiResponse;
import com.harry.renthouse.entity.House;
import com.harry.renthouse.entity.SupportAddress;
import com.harry.renthouse.entity.User;
import com.harry.renthouse.service.ServiceMultiResult;
import com.harry.renthouse.service.auth.UserService;
import com.harry.renthouse.service.house.AddressService;
import com.harry.renthouse.service.house.HouseService;
import com.harry.renthouse.service.search.HouseElasticSearchService;
import com.harry.renthouse.web.dto.*;
import com.harry.renthouse.web.form.SearchHouseForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Harry Xu
 * @date 2020/5/14 10:18
 */
@RestController
@Api(tags = "房源接口")
@RequestMapping("house")
public class HouseController {

    @Resource
    private HouseService houseService;

    @Resource
    private UserService userService;

    @Resource
    private ModelMapper modelMapper;

    @Resource
    private HouseElasticSearchService houseElasticSearchService;

    @PostMapping("houses")
    @ApiOperation(value = "按条件搜索房源")
    public ApiResponse<ServiceMultiResult> searchHouses(@RequestBody @Validated SearchHouseForm searchHouseForm){
        ServiceMultiResult<HouseDTO> result = houseService.search(searchHouseForm);
        return ApiResponse.ofSuccess(result);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "获取房源完整信息")
    public ApiResponse<HouseCompleteInfoDetailDTO> getHouseById(@ApiParam(value = "房屋id") @PathVariable Long id){
        HouseCompleteInfoDTO houseInfo = houseService.findCompleteHouse(id);
        HouseCompleteInfoDetailDTO result = modelMapper.map(houseInfo, HouseCompleteInfoDetailDTO.class);
        // 获取用户信息
        UserDTO agent = userService.findUserById(houseInfo.getHouse().getAdminId());
        // 获取小区出租房屋数
        int houseCountInDistrict = 10;
        // 返回结果
        result.setAgent(agent);
        result.setHouseCountInDistrict(houseCountInDistrict);
        return ApiResponse.ofSuccess(result);
    }

    @GetMapping("search/autocomplete")
    @ApiOperation("房源搜索自动补全")
    public ApiResponse<List<String>> searchAutoComplete(@ApiParam("关键词前缀") @RequestParam String prefix){
        ServiceMultiResult<String> result = houseElasticSearchService.suggest(prefix);
        return ApiResponse.ofSuccess(result.getList());
    }
}