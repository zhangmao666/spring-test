package com.example.springboottest.modules.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 任务Repository
 */
@Mapper
public interface TaskRepository extends BaseMapper<Task> {

    @Select("SELECT * FROM tasks WHERE task_no = #{taskNo}")
    Task selectByTaskNo(String taskNo);

    default Optional<Task> findByTaskNo(String taskNo) {
        return Optional.ofNullable(selectByTaskNo(taskNo));
    }

    @Select("SELECT task_no FROM tasks WHERE task_no LIKE CONCAT(#{prefix}, '%') ORDER BY task_no DESC LIMIT 1")
    String getMaxTaskNoByPrefix(@Param("prefix") String prefix);

    @Select("SELECT * FROM tasks WHERE creator_id = #{creatorId} ORDER BY created_at DESC")
    IPage<Task> selectPageByCreatorId(Page<Task> page, @Param("creatorId") Long creatorId);

    @Select("SELECT * FROM tasks WHERE status = #{status} ORDER BY created_at DESC")
    IPage<Task> selectPageByStatus(Page<Task> page, @Param("status") String status);

    @Select("SELECT DISTINCT t.* FROM tasks t " +
            "INNER JOIN task_approval_records r ON t.id = r.task_id " +
            "WHERE r.approver_id = #{userId} AND r.result = 'PENDING' " +
            "AND t.status IN ('PENDING', 'IN_PROGRESS') ORDER BY t.created_at DESC")
    IPage<Task> selectPendingTasksByApproverId(Page<Task> page, @Param("userId") Long userId);

    @Select("SELECT DISTINCT t.* FROM tasks t " +
            "INNER JOIN task_approval_records r ON t.id = r.task_id " +
            "WHERE r.approver_id = #{userId} AND r.result IN ('APPROVED', 'REJECTED') " +
            "ORDER BY r.approval_time DESC")
    IPage<Task> selectApprovedTasksByApproverId(Page<Task> page, @Param("userId") Long userId);
}
