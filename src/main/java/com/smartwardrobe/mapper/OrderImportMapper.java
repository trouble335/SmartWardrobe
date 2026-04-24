package com.smartwardrobe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobe.entity.OrderImport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderImportMapper extends BaseMapper<OrderImport> {

    @Select("SELECT * FROM order_import WHERE id = #{id} AND user_id = #{userId}")
    OrderImport findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE order_import SET status = #{status}, item_id = #{itemId} WHERE id = #{id}")
    int updateStatusAndItemId(@Param("id") Long id, @Param("status") Integer status, @Param("itemId") Long itemId);
}
