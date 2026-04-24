package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartwardrobe.entity.WearRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WearRecordMapper extends BaseMapper<WearRecord> {

    @Select("SELECT * FROM wear_record WHERE item_id = #{itemId} ORDER BY wear_date DESC")
    List<WearRecord> findByItemId(@Param("itemId") Long itemId);

    @Select("SELECT * FROM wear_record WHERE outfit_id = #{outfitId} ORDER BY wear_date DESC")
    List<WearRecord> findByOutfitId(@Param("outfitId") Long outfitId);

    @Select("SELECT * FROM wear_record WHERE user_id = #{userId} ORDER BY wear_date DESC LIMIT #{limit}")
    List<WearRecord> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(DISTINCT wear_date) FROM wear_record WHERE user_id = #{userId} " +
            "AND wear_date >= #{startDate} AND wear_date <= #{endDate}")
    Long countWearDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM wear_record WHERE user_id = #{userId} AND item_id = #{itemId}")
    Long countByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    List<WearRecord> findListByUserId(Page<WearRecord> page, @Param("userId") Long userId,
                                      @Param("itemId") Long itemId, @Param("outfitId") Long outfitId);
}
