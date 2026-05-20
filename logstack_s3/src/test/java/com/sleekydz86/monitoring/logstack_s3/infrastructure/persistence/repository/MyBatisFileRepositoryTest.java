package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFileSummary;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.mapper.StoredFileMapper;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileProcedureParam;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("MyBatisFileRepository 테스트")
class MyBatisFileRepositoryTest {

    @Mock
    private StoredFileMapper mapper;

    private MyBatisFileRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MyBatisFileRepository(mapper);
        ReflectionTestUtils.setField(repository, "fileIdPrefix", "lky");
        ReflectionTestUtils.setField(repository, "defaultBucketId", 1L);
    }

    @Test
    @DisplayName("성공 - 저장 시 순번 조회 후 ID 부여")
    void save_success() {
        // given
        var draft = StoredFile.draft(
                "a.png", "uploads/a.png", "thumbnails/a.jpg", "image/png", 100L);
        draft = new StoredFile(
                draft.id(), draft.originalFilename(), draft.objectKey(),
                draft.thumbnailKey(), draft.contentType(), draft.size(),
                TestFileFixtures.FIXED_TIME);
        org.mockito.Mockito.doAnswer(invocation -> {
            StoredFileProcedureParam param = invocation.getArgument(0);
            if ("S".equals(param.getOperation())) {
                return 6L;
            }
            return null;
        }).when(mapper).callManage(any(StoredFileProcedureParam.class));

        // when
        StoredFile saved = repository.save(draft);

        // then
        assertThat(saved.id()).endsWith("_0006");
        assertThat(saved.id()).startsWith("lky_20260520_1430_");
        var inOrder = inOrder(mapper);
        inOrder.verify(mapper).callManage(org.mockito.ArgumentMatchers.argThat(
                p -> "S".equals(p.getOperation()) && "lky_20260520_1430".equals(p.getDateTimePrefix())));
        inOrder.verify(mapper).callManage(org.mockito.ArgumentMatchers.argThat(
                p -> "C".equals(p.getOperation())));
        verify(mapper, times(2)).callManage(any(StoredFileProcedureParam.class));
    }

    @Test
    @DisplayName("실패 - 수정 시 id 없음")
    void update_noId_fail() {
        // given
        var file = StoredFile.draft("a", "k", "t", "image/png", 1L);

        // when & then
        assertThatThrownBy(() -> repository.update(file))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage(DomainMessages.ID_REQUIRED_FOR_UPDATE);
    }

    @Test
    @DisplayName("성공 - 단건 조회")
    void findById_success() {
        // given
        String id = "lky_20260520_1430_0001";
        given(mapper.selectById(id)).willReturn(TestFileFixtures.storedFileRow());

        // when
        Optional<StoredFile> found = repository.findById(id);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().originalFilename()).isEqualTo("sample.png");
    }

    @Test
    @DisplayName("성공 - 뷰 목록 조회")
    void search_success() {
        // given
        given(mapper.countFromView(null)).willReturn(1L);
        given(mapper.selectPageFromView(eq(null), eq(0), eq(12)))
                .willReturn(List.of(TestFileFixtures.storedFileListRow()));

        // when
        PageResult<StoredFileSummary> page = repository.search(Optional.empty(), 0, 12);

        // then
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content().getFirst().bucketDisplayName()).isEqualTo("ERP LocalStack Bucket");
    }

    @Test
    @DisplayName("성공 - 삭제 프로시저 호출")
    void delete_success() {
        // given
        String id = "lky_20260520_1430_0001";

        // when
        repository.delete(id);

        // then
        ArgumentCaptor<StoredFileProcedureParam> captor = ArgumentCaptor.forClass(StoredFileProcedureParam.class);
        verify(mapper).callManage(captor.capture());
        assertThat(captor.getValue().getOperation()).isEqualTo("D");
        assertThat(captor.getValue().getId()).isEqualTo(id);
    }
}
