package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.kxaca.dartdatatoolkit.generators.BarrelFileGenerator
import java.io.File

class GenerateBarrelFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val baseVDir = if (file.isDirectory) file else file.parent ?: return
        val baseDir = File(baseVDir.path)

        val dialog = GenerateBarrelDialog(baseDir)
        if (!dialog.showAndGet()) return

        val selectedFiles = dialog.getSelectedFiles()
        if (selectedFiles.isEmpty()) {
            Messages.showErrorDialog(project, "No files selected.", "Error")
            return
        }

        val fileName = dialog.getFileName()
        generateBarrel(project, baseVDir, baseDir, selectedFiles, fileName)
    }

    private fun generateBarrel(
        project: Project,
        baseVDir: VirtualFile,
        baseDir: File,
        files: List<File>,
        fileName: String
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val content = BarrelFileGenerator().generateBarrel(
                files.sortedBy { it.name },
                baseDir,
                withFileComments = false
            )

            try {
                File(baseDir, fileName).writeText(content)
                baseVDir.refresh(false, true)
                Messages.showInfoMessage(
                    project,
                    "'$fileName' generated with ${files.size} file(s) exported.",
                    "Success"
                )
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Failed to create barrel file: ${ex.message}", "Error")
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabled = file != null && (file.isDirectory || file.name.endsWith(".dart"))
        e.presentation.isVisible = true
    }
}
