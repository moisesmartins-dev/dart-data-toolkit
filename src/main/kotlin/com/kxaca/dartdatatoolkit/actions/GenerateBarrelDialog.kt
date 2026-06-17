package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBCheckBox
import com.kxaca.dartdatatoolkit.generators.BarrelFileAnalyzer
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.io.File
import javax.swing.*

class GenerateBarrelDialog(private val baseDir: File) : DialogWrapper(true) {

    private val fileNameField = JTextField(baseDir.name + ".dart", 22)
    private val recursiveCheckBox = JBCheckBox("Include subdirectories", false)
    private val checkBoxList = CheckBoxList<File>()

    init {
        title = "Generate Barrel File"
        init()
        loadFiles()
        recursiveCheckBox.addActionListener { loadFiles() }
    }

    private fun loadFiles() {
        checkBoxList.clear()
        val files = BarrelFileAnalyzer()
            .findDartFiles(baseDir, recursive = recursiveCheckBox.isSelected)
            .sortedBy { it.name }
        files.forEach { file ->
            val label = file.relativeTo(baseDir).path.replace("\\", "/")
            checkBoxList.addItem(file, label, true)
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 8))
        panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)

        // Nome do arquivo
        val namePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        namePanel.add(JLabel("File name:  "))
        namePanel.add(fileNameField)
        panel.add(namePanel, BorderLayout.NORTH)

        // Lista de arquivos
        val listPanel = JPanel(BorderLayout(0, 4))
        listPanel.border = BorderFactory.createTitledBorder("Files to export")

        val btnPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0))
        btnPanel.add(recursiveCheckBox)

        val selectAllBtn = JButton("Select all")
        val deselectAllBtn = JButton("Deselect all")
        selectAllBtn.addActionListener { setAllSelected(true) }
        deselectAllBtn.addActionListener { setAllSelected(false) }
        btnPanel.add(Box.createHorizontalStrut(12))
        btnPanel.add(selectAllBtn)
        btnPanel.add(deselectAllBtn)

        listPanel.add(btnPanel, BorderLayout.NORTH)
        listPanel.add(JBScrollPane(checkBoxList), BorderLayout.CENTER)

        panel.add(listPanel, BorderLayout.CENTER)
        panel.preferredSize = java.awt.Dimension(520, 460)

        return panel
    }

    fun getSelectedFiles(): List<File> {
        val selected = mutableListOf<File>()
        for (i in 0 until checkBoxList.itemsCount) {
            if (checkBoxList.isItemSelected(i)) {
                checkBoxList.getItemAt(i)?.let { selected.add(it) }
            }
        }
        return selected
    }

    fun getFileName(): String {
        val name = fileNameField.text.trim().ifEmpty { baseDir.name }
        return if (name.endsWith(".dart")) name else "$name.dart"
    }

    private fun setAllSelected(selected: Boolean) {
        for (i in 0 until checkBoxList.itemsCount) {
            checkBoxList.getItemAt(i)?.let { checkBoxList.setItemSelected(it, selected) }
        }
        checkBoxList.repaint()
    }
}
