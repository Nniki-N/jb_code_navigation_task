import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

/**
 * Searches for all appearances of [stringToSearch] in all files of the [directory] and its subdirectories.
 *
 * Runs concurrently: opens a coroutine to read each file.
 *
 * If there are to many files, limits number of simultaneously working coroutines to a set value of 16.
 */
fun searchForTextOccurrences(
    stringToSearch: String,
    directory: Path
): Flow<Occurrence> {
    if (stringToSearch.isEmpty()) {
        return emptyFlow()
    }

    val maxParallelFiles = 16

    return channelFlow {
        val files = try {
            withContext(Dispatchers.IO) {
                Files.walk(directory).use { stream ->
                    stream.filter { Files.isRegularFile(it) }.asSequence().toList()
                }
            }
        } catch (ex: IOException) {
            close(ex)
            return@channelFlow
        }

        if (files.isEmpty()) return@channelFlow

        val semaphore = Semaphore(maxParallelFiles)

        for (file in files) {
            if (!isActive) break

            semaphore.acquire()

            launch(Dispatchers.IO) {
                try {
                    Files.newBufferedReader(file, StandardCharsets.UTF_8).use { reader ->
                        var lineNumber = 1
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            val text = line!!
                            var idx = text.indexOf(stringToSearch)

                            while (idx >= 0) {
                                send(OccurrenceImpl(file, lineNumber, idx))
                                idx = text.indexOf(stringToSearch, idx + 1)
                            }

                            lineNumber++
                        }
                    }
                } catch (_: IOException) {
                    // Skip if exception with a file happened
                } finally {
                    semaphore.release()
                }
            }
        }
    }
}
