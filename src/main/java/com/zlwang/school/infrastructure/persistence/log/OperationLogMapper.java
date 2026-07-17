package com.zlwang.school.infrastructure.persistence.log;

import com.zlwang.school.modules.log.repository.CreateOperationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OperationLogMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM sys_operation_log
        WHERE deleted = 0
        <if test='username != null and username != ""'>
          AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test='moduleName != null'>
          AND module_name = #{moduleName}
        </if>
        <if test='operationType != null'>
          AND operation_type = #{operationType}
        </if>
        <if test='resultStatus != null'>
          AND result_status = #{resultStatus}
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
        @Param("moduleName") String moduleName,
        @Param("operationType") String operationType,
        @Param("resultStatus") String resultStatus,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Select("""
        <script>
        SELECT id, user_id, username, module_name, operation_type, request_method,
               request_uri, request_ip, request_params, result_status, error_message,
               cost_ms, created_at
        FROM sys_operation_log
        WHERE deleted = 0
        <if test='username != null and username != ""'>
          AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test='moduleName != null'>
          AND module_name = #{moduleName}
        </if>
        <if test='operationType != null'>
          AND operation_type = #{operationType}
        </if>
        <if test='resultStatus != null'>
          AND result_status = #{resultStatus}
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
    List<OperationLogRow> findLogs(
        @Param("username") String username,
        @Param("moduleName") String moduleName,
        @Param("operationType") String operationType,
        @Param("resultStatus") String resultStatus,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Insert("""
        INSERT INTO sys_operation_log (
          user_id, username, module_name, operation_type, request_method,
          request_uri, request_ip, request_params, result_status, error_message,
          cost_ms, created_by, updated_by, deleted
        ) VALUES (
          #{row.userId}, #{row.username}, #{moduleName}, #{operationType},
          #{row.requestMethod}, #{row.requestUri}, #{row.requestIp},
          #{row.requestParams}, #{resultStatus}, #{row.errorMessage},
          #{row.costMs}, #{row.userId}, #{row.userId}, 0
        )
        """)
    int insert(
        @Param("row") CreateOperationLog row,
        @Param("moduleName") String moduleName,
        @Param("operationType") String operationType,
        @Param("resultStatus") String resultStatus
    );
}
