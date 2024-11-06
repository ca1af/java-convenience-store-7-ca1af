package store.infra.loader;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import store.infra.InfraErrorMessage;

class FileLoaderTest {
    private static final String VALID_FILE_PATH = "src/test/resources/test_data.md";
    private static final String INVALID_FILE_PATH = "src/test/resources/nonexistent.md";

    static class StubFileLoader extends FileLoader<String> {

        @Override
        protected List<String> parseLines(List<String> lines) {
            return lines;
        }

        @Override
        protected List<String> load(String filePath) {
            List<String> lines = readFileLines(filePath);
            return parseLines(lines);
        }
    }

    private static StubFileLoader fileLoader;

    @BeforeAll
    static void setUp() {
        fileLoader = new StubFileLoader();
    }

    @DisplayName("파일 라인을 불러오는 데 성공한다.")
    @Test
    void shouldReadLinesFromValidFile() {
        List<String> lines = fileLoader.readFileLines(VALID_FILE_PATH);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(lines).hasSize(2);
            softly.assertThat(lines.get(0)).isEqualTo("field1,field2,field3");
            softly.assertThat(lines.get(1)).isEqualTo("data1,data2,data3");
        });
    }

    @DisplayName("존재하지 않는 파일을 읽으려고 하면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        Assertions.assertThatThrownBy(() -> fileLoader.readFileLines(INVALID_FILE_PATH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InfraErrorMessage.FILE_NOT_FOUND.getMessage());
    }

    @DisplayName("유효한 정수 문자열을 올바르게 파싱한다.")
    @ParameterizedTest
    @CsvSource({
            "123, 123",
            "-456, -456",
            "0, 0"
    })
    void shouldParseIntegerSuccessfully(String input, int expected) {
        int result = fileLoader.parseInteger(input);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @DisplayName("잘못된 정수 형식을 파싱할 때 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"abc", "12.34", ""})
    void shouldThrowExceptionForInvalidInteger(String input) {
        Assertions.assertThatThrownBy(() -> fileLoader.parseInteger(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InfraErrorMessage.INVALID_INTEGER.getMessage());
    }

    @DisplayName("유효한 날짜 문자열을 올바르게 파싱한다.")
    @ParameterizedTest
    @CsvSource({
            "2024-01-01, 2024-01-01",
            "2023-12-31, 2023-12-31"
    })
    void shouldParseDateSuccessfully(String input, String expectedDate) {
        LocalDate date = fileLoader.parseDate(input);
        Assertions.assertThat(date).isEqualTo(LocalDate.parse(expectedDate));
    }

    @DisplayName("잘못된 날짜 형식을 파싱할 때 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"01-01-2024", "2024/01/01", ""})
    void shouldThrowExceptionForInvalidDateFormat(String input) {
        Assertions.assertThatThrownBy(() -> fileLoader.parseDate(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InfraErrorMessage.INVALID_DATE_FORMAT.getMessage());
    }

    @DisplayName("올바른 필드 개수를 검증한다.")
    @ParameterizedTest
    @CsvSource(delimiter = ':', value = {"data1,data2,data3 : 3", "data1,data2 : 2"})
    void shouldValidateFieldCountCorrectly(String line, int expectedCount) {
        String[] fields = fileLoader.getDeclaredFields(line, expectedCount);
        Assertions.assertThat(fields).hasSize(expectedCount);
    }

    @DisplayName("라인 안에 필드 개수가 일치하지 않을 때 예외가 발생한다.")
    @Test
    void shouldThrowExceptionForInvalidFieldCount() {
        String line = "data1,data2";
        Assertions.assertThatThrownBy(() -> fileLoader.getDeclaredFields(line, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InfraErrorMessage.INVALID_PROMOTION_FORMAT.getMessage());
    }
}
