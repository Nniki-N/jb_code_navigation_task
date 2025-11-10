import java.nio.file.Path

interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}
