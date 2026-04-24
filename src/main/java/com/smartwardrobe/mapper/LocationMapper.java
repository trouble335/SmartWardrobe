package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobe.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LocationMapper extends BaseMapper<Location> {

    @Select("SELECT * FROM location WHERE user_id = #{userId} AND parent_id IS NULL ORDER BY sort_order")
    List<Location> findAllParentsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM location WHERE user_id = #{userId} AND parent_id = #{parentId} ORDER BY sort_order")
    List<Location> findByParentIdAndUserId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    @Select("SELECT * FROM location WHERE user_id = #{userId} ORDER BY sort_order")
    List<Location> findAllByUserId(@Param("userId") Long userId);
}
