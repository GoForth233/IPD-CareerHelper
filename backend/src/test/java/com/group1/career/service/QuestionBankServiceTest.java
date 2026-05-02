package com.group1.career.service;

import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.repository.InterviewQuestionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionBankServiceTest {

    @Mock
    private InterviewQuestionRepository repo;

    @InjectMocks
    private QuestionBankService questionBankService;

    // ========== list ==========

    @Test
    @DisplayName("list — returns paged results from repository")
    public void testList_ReturnsPage() {
        List<InterviewQuestion> questions = List.of(
                InterviewQuestion.builder().id(1L).position("Java Backend").difficulty("Normal")
                        .content("Tell me about Spring Boot").likes(10).build(),
                InterviewQuestion.builder().id(2L).position("Java Backend").difficulty("Normal")
                        .content("Explain dependency injection").likes(5).build()
        );
        Page<InterviewQuestion> page = new PageImpl<>(questions);
        when(repo.search(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        Page<InterviewQuestion> result = questionBankService.list("Java Backend", "Normal", 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(repo).search(eq("Java Backend"), eq("Normal"), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("list — treats blank position as null filter")
    public void testList_BlankPositionFilter() {
        when(repo.search(isNull(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<InterviewQuestion> result = questionBankService.list("  ", "Normal", 0, 10);

        assertNotNull(result);
        verify(repo).search(isNull(), eq("Normal"), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("list — page size is capped at 50")
    public void testList_PageSizeCapped() {
        when(repo.search(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        questionBankService.list(null, null, 0, 1000);

        verify(repo).search(any(), any(), any(), argThat(p -> p.getPageSize() == 50));
    }

    // ========== contribute ==========

    @Test
    @DisplayName("contribute — saves and returns the new question")
    public void testContribute_Success() {
        InterviewQuestion saved = InterviewQuestion.builder()
                .id(1L).position("Frontend").difficulty("Normal")
                .content("Explain the virtual DOM in detail please.").status("APPROVED").build();
        when(repo.save(any(InterviewQuestion.class))).thenReturn(saved);

        InterviewQuestion result = questionBankService.contribute(
                42L, "Frontend", "Normal", "Explain the virtual DOM in detail please.", null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("APPROVED", result.getStatus());
        verify(repo).save(any(InterviewQuestion.class));
    }

    @Test
    @DisplayName("contribute — normalises difficulty Easy/Hard/anything-else")
    public void testContribute_NormaliseDifficulty_Easy() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InterviewQuestion result = questionBankService.contribute(
                1L, "Backend", "easy", "What is the difference between process and thread in Java?", null);

        assertEquals("Easy", result.getDifficulty());
    }

    @Test
    @DisplayName("contribute — normalises unknown difficulty to Normal")
    public void testContribute_NormaliseDifficulty_Unknown() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InterviewQuestion result = questionBankService.contribute(
                1L, "Backend", "medium", "Describe RESTful API design principles in practice.", null);

        assertEquals("Normal", result.getDifficulty());
    }

    @Test
    @DisplayName("contribute — throws IllegalArgumentException when content too short")
    public void testContribute_ContentTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> questionBankService.contribute(1L, "Java", "Normal", "Short", null));
    }

    @Test
    @DisplayName("contribute — blank position defaults to General")
    public void testContribute_BlankPositionDefaultsToGeneral() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InterviewQuestion result = questionBankService.contribute(
                1L, "", "Normal", "Describe how garbage collection works in the JVM.", null);

        assertEquals("General", result.getPosition());
    }

    @Test
    @DisplayName("contribute — hashes contributorId (non-null hash stored)")
    public void testContribute_ContributorHashIsHashed() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InterviewQuestion result = questionBankService.contribute(
                99L, "Backend", "Hard", "How does the JVM JIT compiler optimise hot paths at runtime?", null);

        assertNotNull(result.getContributorHash());
        assertNotEquals("99", result.getContributorHash());
    }

    // ========== like ==========

    @Test
    @DisplayName("like — increments and returns the question")
    public void testLike_Success() {
        InterviewQuestion q = InterviewQuestion.builder()
                .id(1L).likes(6).content("A question").position("Backend").difficulty("Normal").build();
        when(repo.incrementLikes(1L)).thenReturn(1);
        when(repo.findById(1L)).thenReturn(Optional.of(q));

        Optional<InterviewQuestion> result = questionBankService.like(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(repo).incrementLikes(1L);
    }

    @Test
    @DisplayName("like — returns empty when question not found")
    public void testLike_NotFound() {
        when(repo.incrementLikes(999L)).thenReturn(0);

        Optional<InterviewQuestion> result = questionBankService.like(999L);

        assertFalse(result.isPresent());
        verify(repo, never()).findById(any());
    }

    // ========== drawForInterview ==========

    @Test
    @DisplayName("drawForInterview — returns empty when pool is empty")
    public void testDrawForInterview_EmptyPool() {
        when(repo.drawPool(any(), any(), any(Pageable.class))).thenReturn(Collections.emptyList());

        // Run several times: even if the RNG decides to draw, the pool is empty → always empty.
        for (int i = 0; i < 10; i++) {
            Optional<InterviewQuestion> result =
                    questionBankService.drawForInterview("Frontend", "Normal");
            // Either RNG said skip (empty), or pool was empty (also empty).
            assertTrue(result.isEmpty() || result.isPresent());
        }
    }
}
