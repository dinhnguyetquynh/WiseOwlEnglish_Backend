package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.ContentType;
import com.iuh.WiseOwlEnglish_Backend.model.TestOption;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TestOptionRepository extends JpaRepository<TestOption, Long> {

    @Query("SELECT o FROM TestOption o WHERE o.question.id = :questionId ORDER BY o.order ASC")
    List<TestOption> findByQuestionIdOrderByOrder(@Param("questionId") Long questionId);
    List<TestOption> findByQuestionIdIn(Collection<Long> questionIds);

    @Query(value = """
        SELECT t.content_ref_id
        FROM test_option t
        WHERE t.id = ANY(:ids)
          AND t.content_ref_id IS NOT NULL
        ORDER BY array_position(:ids, t.id)
    """, nativeQuery = true)
    List<Long> findContentRefIdsOrderByInput(@Param("ids") Long[] ids);

    // Kiểm tra xem Vocab/Sentence có đang được dùng làm Option (đáp án) không
    boolean existsByContentTypeAndContentRefId(ContentType contentType, Long contentRefId);
}
