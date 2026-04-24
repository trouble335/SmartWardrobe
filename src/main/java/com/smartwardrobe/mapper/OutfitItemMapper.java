package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobe.entity.OutfitItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OutfitItemMapper extends BaseMapper<OutfitItem> {

    @Select("SELECT oi.*, i.* FROM outfit_item oi " +
            "INNER JOIN item i ON oi.item_id = i.id " +
            "WHERE oi.outfit_id = #{outfitId} AND i.deleted = 0 " +
            "ORDER BY oi.position")
    List<OutfitItem> findByOutfitIdWithItems(@Param("outfitId") Long outfitId);

    @Select("SELECT * FROM outfit_item WHERE outfit_id = #{outfitId}")
    List<OutfitItem> findByOutfitId(@Param("outfitId") Long outfitId);
}
