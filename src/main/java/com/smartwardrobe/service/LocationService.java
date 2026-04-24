package com.smartwardrobe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobe.entity.Location;
import com.smartwardrobe.mapper.LocationMapper;
import com.smartwardrobe.vo.LocationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationMapper locationMapper;

    public List<LocationVO> getLocationTree(Long userId) {
        List<Location> allLocations = locationMapper.findAllByUserId(userId);
        return buildLocationTree(allLocations);
    }

    private List<LocationVO> buildLocationTree(List<Location> locations) {
        List<Location> parents = locations.stream()
                .filter(l -> l.getParentId() == null)
                .toList();

        return parents.stream().map(parent -> {
            LocationVO vo = new LocationVO();
            vo.setId(parent.getId());
            vo.setName(parent.getName());
            vo.setParentId(null);

            List<LocationVO> children = locations.stream()
                    .filter(l -> parent.getId().equals(l.getParentId()))
                    .map(child -> {
                        LocationVO childVo = new LocationVO();
                        childVo.setId(child.getId());
                        childVo.setName(child.getName());
                        childVo.setParentId(child.getParentId());
                        return childVo;
                    })
                    .collect(Collectors.toList());

            vo.setChildren(children);
            return vo;
        }).collect(Collectors.toList());
    }

    public Long createLocation(Long userId, String name, Long parentId) {
        Location location = new Location();
        location.setUserId(userId);
        location.setName(name);
        location.setParentId(parentId);
        location.setSortOrder(0);
        locationMapper.insert(location);
        return location.getId();
    }

    public void updateLocation(Long id, Long userId, String name) {
        Location location = locationMapper.selectById(id);
        if (location == null || !location.getUserId().equals(userId)) {
            throw new IllegalArgumentException("位置不存在或无权限");
        }
        location.setName(name);
        locationMapper.updateById(location);
    }

    public void deleteLocation(Long id, Long userId) {
        Location location = locationMapper.selectById(id);
        if (location == null || !location.getUserId().equals(userId)) {
            throw new IllegalArgumentException("位置不存在或无权限");
        }
        locationMapper.deleteById(id);
    }

    public String getLocationName(Long locationId) {
        if (locationId == null) {
            return null;
        }
        Location location = locationMapper.selectById(locationId);
        if (location == null) {
            return null;
        }

        if (location.getParentId() != null) {
            Location parent = locationMapper.selectById(location.getParentId());
            if (parent != null) {
                return parent.getName() + "-" + location.getName();
            }
        }
        return location.getName();
    }
}
