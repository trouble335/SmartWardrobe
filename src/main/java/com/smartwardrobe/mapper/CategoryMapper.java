package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobe.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM category WHERE parent_id IS NULL ORDER BY sort_order")
    List<Category> findAllParents();

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} ORDER BY sort_order")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM category ORDER BY sort_order")
    List<Category> findAllWithChildren();
}
