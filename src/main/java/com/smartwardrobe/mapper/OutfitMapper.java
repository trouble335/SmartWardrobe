package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobe.entity.Outfit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OutfitMapper extends BaseMapper<Outfit> {

    @Select("SELECT o.* FROM outfit o " +
            "INNER JOIN outfit_item oi ON o.id = oi.outfit_id " +
            "WHERE oi.item_id = #{itemId} AND o.deleted = 0 " +
            "ORDER BY o.wear_count DESC LIMIT 5")
    List<Outfit> findByItemId(@Param("itemId") Long itemId);

    @Select("SELECT * FROM outfit WHERE user_id = #{userId} AND is_recommended = 1 AND deleted = 0")
    List<Outfit> findRecommendedByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM outfit WHERE user_id = #{userId} AND deleted = 0 ORDER BY wear_count DESC LIMIT 10")
    List<Outfit> findTopWornByUserId(@Param("userId") Long userId);
}
