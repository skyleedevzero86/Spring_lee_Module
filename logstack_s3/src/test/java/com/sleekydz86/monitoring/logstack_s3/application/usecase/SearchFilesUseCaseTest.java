package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.FileViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.query.SearchFilesQuery;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileListItemView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchFilesUseCase 테스트")
class SearchFilesUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileViewAssembler assembler;

    @InjectMocks
    private SearchFilesUseCase useCase;

    @Test
    @DisplayName("성공 - 목록 조회")
    void apply_success() {
        // given
        var summary = TestFileFixtures.storedFileSummary();
        var page = PageResult.of(List.of(summary), 0, 12, 1);
        var item = new FileListItemView(
                summary.id(), summary.originalFilename(), summary.contentType(), summary.size(),
                summary.createdAt(), "thumb-url", summary.bucketDisplayName(), summary.region(),
                summary.sizeLabel(), summary.mediaType()
        );
        given(fileRepository.search(Optional.of("sample"), 0, 12)).willReturn(page);
        given(assembler.toListItem(summary)).willReturn(item);

        // when
        var result = useCase.apply(new SearchFilesQuery("sample", 0, 12));

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().originalFilename()).isEqualTo("sample.png");
    }

    @Test
    @DisplayName("성공 - 결과 없음")
    void apply_empty_success() {
        // given
        given(fileRepository.search(Optional.empty(), 0, 12))
                .willReturn(PageResult.of(List.of(), 0, 12, 0));

        // when
        var result = useCase.apply(new SearchFilesQuery(null, 0, 12));

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
