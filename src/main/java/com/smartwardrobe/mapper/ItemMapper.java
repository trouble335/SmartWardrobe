package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartwardrobe.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    @Select("SELECT i.*, c1.name as category_name, c2.name as sub_category_name, l.name as location_name " +
            "FROM item i " +
            "LEFT JOIN category c1 ON i.category_id = c1.id " +
            "LEFT JOIN category c2 ON i.sub_category_id = c2.id " +
            "LEFT JOIN location l ON i.location_id = l.id " +
            "WHERE i.id = #{id} AND i.deleted = 0")
    Item findDetailById(@Param("id") Long id);

    @Update("UPDATE item SET wear_count = wear_count + 1, last_worn = #{date} WHERE id = #{id}")
    int incrementWearCount(@Param("id") Long id, @Param("date") LocalDate date);

    @Select("SELECT * FROM item WHERE user_id = #{userId} AND deleted = 0 " +
            "AND (last_worn IS NULL OR DATEDIFF(CURDATE(), last_worn) >= #{days}) " +
            "ORDER BY last_worn ASC")
    List<Item> findSleepingItems(@Param("userId") Long userId, @Param("days") int days);

    @Select("SELECT COUNT(*) FROM item WHERE user_id = #{userId} AND deleted = 0")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM item WHERE user_id = #{userId} AND category_id = #{categoryId} AND deleted = 0")
    Long countByUserIdAndCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Select("SELECT colors FROM item WHERE user_id = #{userId} AND deleted = 0")
    List<String> findAllColorsByUserId(@Param("userId") Long userId);

    List<Item> findListWithFilters(Page<Item> page, @Param("userId") Long userId,
                                   @Param("categoryId") Long categoryId,
                                   @Param("styles") List<String> styles,
                                   @Param("seasons") List<String> seasons,
                                   @Param("occasions") List<String> occasions,
                                   @Param("colors") List<String> colors,
                                   @Param("status") Integer status,
                                   @Param("keyword") String keyword,
                                   @Param("sortBy") String sortBy,
                                   @Param("sortOrder") String sortOrder);
}
