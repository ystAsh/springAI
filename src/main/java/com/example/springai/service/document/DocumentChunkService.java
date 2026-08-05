package com.example.springai.service.document;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunkService {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 100;
    private static final int MAX_CHUNK_COUNT = 500;

    /**
     * 긴 문서 텍스트를 중복 영역을 가진 Chunk 목록으로 분할한다.
     */
    public List<String> split(
            String text
    ) {
        if (text == null
                || text.isBlank()) {

            throw new IllegalArgumentException(
                    "분할할 문서 텍스트가 없습니다."
            );
        }

        List<String> chunks =
                new ArrayList<>();

        int start = 0;

        while (start < text.length()) {
            int end =
                    Math.min(
                            start + CHUNK_SIZE,
                            text.length()
                    );

            if (end < text.length()) {
                end =
                        findBoundary(
                                text,
                                start,
                                end
                        );
            }

            String chunk =
                    text.substring(
                            start,
                            end
                    ).trim();

            if (!chunk.isBlank()) {
                chunks.add(
                        chunk
                );
            }

            if (chunks.size() > MAX_CHUNK_COUNT) {
                throw new IllegalArgumentException(
                        "문서 Chunk 수가 허용 범위를 초과했습니다. 최대 "
                                + MAX_CHUNK_COUNT
                                + "개까지 가능합니다."
                );
            }

            if (end >= text.length()) {
                break;
            }

            int nextStart =
                    end - CHUNK_OVERLAP;

            /*
             * 다음 시작 위치가 반드시 앞으로 진행되도록 보장한다.
             */
            start =
                    Math.max(
                            nextStart,
                            start + 1
                    );
        }

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "문서 Chunk를 생성하지 못했습니다."
            );
        }

        return List.copyOf(
                chunks
        );
    }

    private int findBoundary(
            String text,
            int start,
            int end
    ) {
        int newlineIndex =
                text.lastIndexOf(
                        '\n',
                        end - 1
                );

        int periodIndex =
                text.lastIndexOf(
                        '.',
                        end - 1
                );

        int koreanPeriodIndex =
                text.lastIndexOf(
                        '。',
                        end - 1
                );

        int questionIndex =
                text.lastIndexOf(
                        '?',
                        end - 1
                );

        int exclamationIndex =
                text.lastIndexOf(
                        '!',
                        end - 1
                );

        int boundary =
                Math.max(
                        Math.max(
                                newlineIndex,
                                periodIndex
                        ),
                        Math.max(
                                Math.max(
                                        koreanPeriodIndex,
                                        questionIndex
                                ),
                                exclamationIndex
                        )
                );

        int minimumBoundary =
                start + (CHUNK_SIZE / 2);

        if (boundary >= minimumBoundary) {
            return boundary + 1;
        }

        return end;
    }
}