package org.example.task3

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class TextSearchToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = TextSearchPanel()
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel.mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
