package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.kxaca.dartdatatoolkit.generators.BarrelFileGenerator
import java.io.File

class GenerateBarrelFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(LangDataKeys.IDE_VIEW)?.directories?.firstOrNull()?.virtualFile
            ?: return

        val baseVDir = if (file.isDirectory) file else file.parent ?: return
        val baseDir = File(baseVDir.path)

        val dialog = GenerateBarrelDialog(baseDir)
        if (!dialog.showAndGet()) return

        val selectedFiles = dialog.getSelectedFiles()
        val fileName = dialog.getFileName()
        val filesToExport = selectedFiles.filter { it.name != fileName }

        if (filesToExport.isEmpty()) {
            Messages.showErrorDialog(project, "No files selected.", "Error")
            return
        }

        generateBarrel(project, baseVDir, baseDir, filesToExport, fileName)
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
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Failed to create barrel file: ${ex.message}", "Error")
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val hasView = e.getData(LangDataKeys.IDE_VIEW)?.directories?.isNotEmpty() == true
        e.presentation.isEnabled = hasView || (file != null && (file.isDirectory || file.name.endsWith(".dart")))
        e.presentation.isVisible = true
    }
}
