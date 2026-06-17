package com.kxaca.dartdatatoolkit.actions

import com.google.gson.*
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.ActionEvent
import javax.swing.*

class JsonToDartDialog : DialogWrapper(true) {

    private val classNameField = JTextField("MyClass", 22)

    private val copyWithBox  = JBCheckBox("copyWith()",  true)
    private val toJsonBox    = JBCheckBox("toJson()",    true)
    private val fromJsonBox  = JBCheckBox("fromJson()",  true)
    private val toStringBox  = JBCheckBox("toString()",  true)

    private val jsonArea = JTextArea(16, 42).apply {
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        lineWrap = false
        tabSize = 2
    }

    init {
        title = "Generate Dart Class from JSON"
        init()
        // Override Ctrl+V to auto-format on paste
        jsonArea.actionMap.put("paste", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) = pasteAndFormat()
        })
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout(0, 10))
        root.border = BorderFactory.createEmptyBorder(12, 12, 4, 12)

        root.add(buildTopPanel(), BorderLayout.NORTH)
        root.add(buildJsonPanel(), BorderLayout.CENTER)
        root.preferredSize = Dimension(580, 560)
        return root
    }

    private fun buildTopPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val nameRow = JPanel(FlowLayout(FlowLayout.LEFT, 6, 0))
        nameRow.add(JLabel("Class name:  "))
        nameRow.add(classNameField)
        panel.add(nameRow)
        panel.add(Box.createVerticalStrut(10))

        val methodsRow = JPanel(FlowLayout(FlowLayout.LEFT, 6, 0))
        val methodsLabel = JLabel("Methods to generate:")
        methodsRow.add(methodsLabel)
        panel.add(methodsRow)

        val checkRow = JPanel(FlowLayout(FlowLayout.LEFT, 6, 0))
        checkRow.add(copyWithBox)
        checkRow.add(toJsonBox)
        checkRow.add(fromJsonBox)
        checkRow.add(toStringBox)
        panel.add(checkRow)

        val selectRow = JPanel(FlowLayout(FlowLayout.LEFT, 6, 4))
        val selectAll   = JButton("Select all")
        val deselectAll = JButton("Deselect all")
        selectAll.addActionListener   { setAllMethods(true)  }
        deselectAll.addActionListener { setAllMethods(false) }
        selectRow.add(selectAll)
        selectRow.add(deselectAll)
        panel.add(selectRow)

        return panel
    }

    private fun buildJsonPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 4))
        panel.border = BorderFactory.createTitledBorder("JSON")
        panel.add(JBScrollPane(jsonArea), BorderLayout.CENTER)

        val pastePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 4))
        val pasteBtn = JButton("Paste from clipboard")
        pasteBtn.addActionListener { pasteAndFormat() }
        pastePanel.add(pasteBtn)
        panel.add(pastePanel, BorderLayout.SOUTH)

        return panel
    }

    private fun pasteAndFormat() {
        try {
            val cb = Toolkit.getDefaultToolkit().systemClipboard
            val text = cb.getData(DataFlavor.stringFlavor) as? String ?: return
            jsonArea.text = formatJson(text)
            jsonArea.caretPosition = 0
        } catch (_: Exception) {}
    }

    private fun formatJson(raw: String): String = try {
        val el = JsonParser.parseString(raw.trim())
        GsonBuilder().setPrettyPrinting().create().toJson(el)
    } catch (_: Exception) { raw }

    private fun setAllMethods(selected: Boolean) {
        copyWithBox.isSelected  = selected
        toJsonBox.isSelected    = selected
        fromJsonBox.isSelected  = selected
        toStringBox.isSelected  = selected
    }

    fun getClassName(): String =
        classNameField.text.trim().ifEmpty { "MyClass" }.replaceFirstChar { it.uppercase() }

    fun getJsonText(): String = jsonArea.text.trim()

    fun getSelectedMethods(): Set<String> {
        val set = mutableSetOf<String>()
        if (copyWithBox.isSelected)  set.add("copyWith")
        if (toJsonBox.isSelected)    set.add("toJson")
        if (fromJsonBox.isSelected)  set.add("fromJson")
        if (toStringBox.isSelected)  set.add("toString")
        return set
    }
}
