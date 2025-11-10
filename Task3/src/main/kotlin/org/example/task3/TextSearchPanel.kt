package org.example.task3

import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import searchForTextOccurrences
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class TextSearchPanel {
    val mainPanel: JPanel = JPanel(BorderLayout())
    private val directoryField = JTextField()
    private val searchField = JTextField()
    private val startButton = JButton("Start Search")
    private val cancelButton = JButton("Cancel Search")
    private val resultArea = JTextArea()
    private var searchJob: Job? = null

    init {
        resultArea.isEditable = false
        resultArea.caret.isVisible = false
        resultArea.isFocusable = false
        resultArea.border = JBUI.Borders.empty(10)

        val inputPanel = JPanel()
        inputPanel.layout = BoxLayout(inputPanel, BoxLayout.Y_AXIS)
        inputPanel.border = JBUI.Borders.empty(10)

        addLabelAndField(inputPanel,"Directory Path:", directoryField)
        addLabelAndField(inputPanel, "String to Search:", searchField)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonPanel.add(startButton)
        buttonPanel.add(cancelButton)

        inputPanel.add(buttonPanel)

        mainPanel.add(inputPanel, BorderLayout.NORTH)
        mainPanel.add(JScrollPane(resultArea), BorderLayout.CENTER)

        setupActions()
    }


    private fun addLabelAndField(inputPanel: JPanel, labelText: String, textField: JTextField) {
        val labelPanel = JPanel()
        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.X_AXIS)

        val label = JLabel(labelText)
        labelPanel.add(label)
        labelPanel.add(Box.createHorizontalGlue())
        inputPanel.add(labelPanel)
        inputPanel.add(Box.createVerticalStrut(2))

        textField.maximumSize = Dimension(Int.MAX_VALUE, textField.preferredSize.height)
        inputPanel.add(textField)
        inputPanel.add(Box.createVerticalStrut(10))
    }

    private fun setupActions() {
        startButton.addActionListener {
            val dir = directoryField.text.trim()
            val text = searchField.text.trim()

            if (dir.isEmpty() || text.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "Please fill both fields!")
                return@addActionListener
            }

            resultArea.text = "Searching...\n"
            startButton.isEnabled = false
            cancelButton.isEnabled = true

            val directory = java.nio.file.Path.of(dir)

            searchJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    searchForTextOccurrences(text, directory).collect { occurrence ->
                        withContext(Dispatchers.Main) {
                            resultArea.append("${occurrence.file}: ${occurrence.line}:${occurrence.offset}\n")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        JOptionPane.showMessageDialog(mainPanel, "Error: ${e.message}")
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        startButton.isEnabled = true
                        cancelButton.isEnabled = false
                        resultArea.append("Search finished")
                    }
                }
            }
        }

        cancelButton.addActionListener {
            searchJob?.cancel()
            startButton.isEnabled = true
            cancelButton.isEnabled = false
            resultArea.append("Search cancelled\n")
        }
    }
}
