package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.smartwardrobe.entity.Category;
import com.smartwardrobe.mapper.CategoryMapper;
import com.smartwardrobe.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_TREE_KEY = "category:tree";

    public List<CategoryVO> getCategoryTree() {
        // 先从缓存获取
        Object cached = redisTemplate.opsForValue().get(CATEGORY_TREE_KEY);
        if (cached != null) {
            return (List<CategoryVO>) cached;
        }

        // 查询所有分类
        List<Category> allCategories = categoryMapper.selectList(null);

        // 构建树形结构
        List<CategoryVO> tree = buildCategoryTree(allCategories);

        // 缓存
        redisTemplate.opsForValue().set(CATEGORY_TREE_KEY, tree, 24, TimeUnit.HOURS);

        return tree;
    }

    private List<CategoryVO> buildCategoryTree(List<Category> categories) {
        // 找出所有父分类
        List<Category> parents = categories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        return parents.stream().map(parent -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(parent.getId());
            vo.setName(parent.getName());
            vo.setParentId(null);
            vo.setLevel(parent.getLevel());
            vo.setIcon(parent.getIcon());

            // 查找子分类
            List<CategoryVO> children = categories.stream()
                    .filter(c -> parent.getId().equals(c.getParentId()))
                    .map(child -> {
                        CategoryVO childVo = new CategoryVO();
                        childVo.setId(child.getId());
                        childVo.setName(child.getName());
                        childVo.setParentId(child.getParentId());
                        childVo.setLevel(child.getLevel());
                        childVo.setIcon(child.getIcon());
                        return childVo;
                    })
                    .collect(Collectors.toList());

            vo.setChildren(children);
            return vo;
        }).collect(Collectors.toList());
    }
}
