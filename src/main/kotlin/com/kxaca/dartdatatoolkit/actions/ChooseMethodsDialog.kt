package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*

class ChooseMethodsDialog : DialogWrapper(true) {

    private val copyWithCheckBox = JCheckBox("copyWith()", true)
    private val toJsonCheckBox = JCheckBox("toJson()", true)
    private val fromJsonCheckBox = JCheckBox("fromJson()", true)
    private val toStringCheckBox = JCheckBox("toString()", true)

    init {
        title = "Generate Dart Methods"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val titleLabel = JLabel("Select which methods to generate:")
        titleLabel.font = titleLabel.font.deriveFont(14f)
        panel.add(titleLabel)
        panel.add(Box.createVerticalStrut(15))

        val checkboxPanel = JPanel()
        checkboxPanel.layout = BoxLayout(checkboxPanel, BoxLayout.Y_AXIS)

        copyWithCheckBox.toolTipText = "Creates a copy of the class with some fields modified"
        checkboxPanel.add(copyWithCheckBox)
        checkboxPanel.add(Box.createVerticalStrut(8))

        toJsonCheckBox.toolTipText = "Converts the class to a JSON map"
        checkboxPanel.add(toJsonCheckBox)
        checkboxPanel.add(Box.createVerticalStrut(8))

        fromJsonCheckBox.toolTipText = "Creates a class instance from a JSON map"
        checkboxPanel.add(fromJsonCheckBox)
        checkboxPanel.add(Box.createVerticalStrut(8))

        toStringCheckBox.toolTipText = "String representation of the class with all fields"
        checkboxPanel.add(toStringCheckBox)

        panel.add(checkboxPanel)
        panel.add(Box.createVerticalStrut(10))

        val infoLabel = JLabel("(All selected by default)")
        infoLabel.font = infoLabel.font.deriveFont(11f)
        panel.add(infoLabel)

        panel.preferredSize = java.awt.Dimension(400, 250)

        return panel
    }

    fun getSelectedMethods(): Set<String> {
        val methods = mutableSetOf<String>()
        if (copyWithCheckBox.isSelected) methods.add("copyWith")
        if (toJsonCheckBox.isSelected) methods.add("toJson")
        if (fromJsonCheckBox.isSelected) methods.add("fromJson")
        if (toStringCheckBox.isSelected) methods.add("toString")
        return methods
    }
}
