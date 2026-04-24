package com.smartwardrobe.controller;

import com.smartwardrobe.dto.Result;
import com.smartwardrobe.service.LocationService;
import com.smartwardrobe.vo.LocationVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public Result<List<LocationVO>> getLocations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LocationVO> locations = locationService.getLocationTree(userId);
        return Result.success(locations);
    }

    @PostMapping
    public Result<Long> createLocation(HttpServletRequest request,
                                       @RequestParam String name,
                                       @RequestParam(required = false) Long parentId) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = locationService.createLocation(userId, name, parentId);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> updateLocation(HttpServletRequest request, @PathVariable Long id,
                                       @RequestParam String name) {
        Long userId = (Long) request.getAttribute("userId");
        locationService.updateLocation(id, userId, name);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteLocation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        locationService.deleteLocation(id, userId);
        return Result.success();
    }
}
