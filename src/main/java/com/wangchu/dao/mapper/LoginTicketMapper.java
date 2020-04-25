package com.wangchu.dao.mapper;

import com.wangchu.dal.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    @Insert({"insert login_ticket(user_id,ticket,status,expired) ",
    "values(#{userId},#{ticket},#{status},#{expired}) "})
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({"select id,user_id,ticket,status,expired from login_ticket ",
            "where ticket = #{ticket}"})
    LoginTicket selectLoginTicketByTicket(String ticket);

    @Update({"update login_ticket set status = #{status} where ticket = #{ticket}"})
    int updateLoginTicketStatus(@Param(value = "ticket") String ticket,@Param(value = "status") int status);
}