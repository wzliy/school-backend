package com.zlwang.school.infrastructure.persistence.log;

import com.zlwang.school.modules.log.repository.CreateLoginLog;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginLogMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM sys_login_log
        WHERE deleted = 0
        <if test='username != null and username != ""'>
          AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test='loginStatus != null'>
          AND login_status = #{loginStatus}
        </if>
        <if test='startTime != null'>
          AND created_at &gt;= #{startTime}
        </if>
        <if test='endTime != null'>
          AND created_at &lt;= #{endTime}
        </if>
        </script>
        """)
    long countLogs(
        @Param("username") String username,
        @Param("loginStatus") String loginStatus,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Select("""
        <script>
        SELECT id, user_id, username, login_ip, user_agent, login_status,
               failure_reason, created_at
        FROM sys_login_log
        WHERE deleted = 0
        <if test='username != null and username != ""'>
          AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test='loginStatus != null'>
          AND login_status = #{loginStatus}
        </if>
        <if test='startTime != null'>
          AND created_at &gt;= #{startTime}
        </if>
        <if test='endTime != null'>
          AND created_at &lt;= #{endTime}
        </if>
        ORDER BY created_at DESC, id DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<LoginLogRow> findLogs(
        @Param("username") String username,
        @Param("loginStatus") String loginStatus,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Insert("""
        INSERT INTO sys_login_log (
          user_id, username, login_ip, user_agent, login_status, failure_reason,
          created_by, updated_by, deleted
        ) VALUES (
          #{row.userId}, #{row.username}, #{row.loginIp}, #{row.userAgent},
          #{loginStatus}, #{row.failureReason}, #{row.userId}, #{row.userId}, 0
        )
        """)
    int insert(@Param("row") CreateLoginLog row, @Param("loginStatus") String loginStatus);
}
