import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class SearchTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testSingleFileSingleOccurrence() = runBlocking {
        val file = tempDir.resolve("a.txt")

        Files.writeString(file, "hello world\nthis is a test\n")

        val results = searchForTextOccurrences("test", tempDir).toList()

        assertEquals(1, results.size)

        val occurrence = results[0]

        assertEquals(file, occurrence.file)
        assertEquals(2, occurrence.line)
        assertEquals(10, occurrence.offset)
    }

    @Test
    fun testMultipleOccurrencesInSameLine() = runBlocking {
        val file = tempDir.resolve("b.txt")

        Files.writeString(file, "aaa aaa aaa\n")

        val results = searchForTextOccurrences("aa", tempDir).toList()

        assertTrue(results.isNotEmpty())

        val offsets = results.map { it.offset }.sorted()

        assertTrue(offsets.contains(0))
        assertTrue(offsets.contains(1))
        assertTrue(offsets.contains(4))
        assertTrue(offsets.contains(5))
        assertTrue(offsets.contains(8))
        assertTrue(offsets.contains(9))
    }

    @Test
    fun testSearchInSubdirectories() = runBlocking {
        val dir = tempDir.resolve("sub")

        Files.createDirectories(dir)

        val file1 = dir.resolve("x.txt")
        val file2 = tempDir.resolve("y.txt")

        Files.writeString(file1, "test\n")
        Files.writeString(file2, "nothing\ntest at second line\n")

        val results = searchForTextOccurrences("test", tempDir).toList()

        assertEquals(2, results.size)

        val paths = results.map { it.file }.toSet()

        assertTrue(paths.contains(file1))
        assertTrue(paths.contains(file2))
    }

    @Test
    fun testEmptyDirectoryProducesEmptyFlow() = runBlocking {
        val results = searchForTextOccurrences("test", tempDir).toList()

        assertTrue(results.isEmpty())
    }
    @Test
    fun testEmptySearchStringProducesNoResults() = runBlocking {
        val file = tempDir.resolve("c.txt")

        Files.writeString(file, "test\n")

        val results = searchForTextOccurrences("", tempDir).toList()

        assertTrue(results.isEmpty())
    }

    @Test
    fun testUnreadableFileIsSkipped() = runBlocking {
        val file = tempDir.resolve("d.txt")

        Files.writeString(file, "testtesttest\n")

        file.toFile().setReadable(false)

        val results = searchForTextOccurrences("test", tempDir).toList()

        assertTrue(results.size <= 1)
    }
}
