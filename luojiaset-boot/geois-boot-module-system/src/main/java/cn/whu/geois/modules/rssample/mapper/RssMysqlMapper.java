package cn.whu.geois.modules.rssample.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/7/13 17:56
 */
public interface RssMysqlMapper {
    @Select("SELECT role_code FROM sys_user_role LEFT JOIN sys_role on sys_role.id=sys_user_role.role_id WHERE user_id=#{userId};")
    String getUserRole(@Param("userId") String userId);
}
