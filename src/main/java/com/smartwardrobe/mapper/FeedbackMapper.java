package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobe.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    @Select("SELECT COUNT(*) FROM feedback WHERE user_id = #{userId} AND outfit_id = #{outfitId} AND type = #{type}")
    Long countByUserIdAndOutfitIdAndType(@Param("userId") Long userId, @Param("outfitId") Long outfitId, @Param("type") Integer type);
}
