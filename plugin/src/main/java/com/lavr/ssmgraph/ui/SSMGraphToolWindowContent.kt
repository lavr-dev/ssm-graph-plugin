package com.lavr.ssmgraph.ui

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.lavr.ssmgraph.editor.DotPreviewEditor
import com.lavr.ssmgraph.service.CheckBoxService
import com.lavr.ssmgraph.support.ColorTableSupport
import com.lavr.ssmgraph.support.ComboBoxSupport
import com.lavr.ssmgraph.support.JTextAreaSupport
import com.lavr.ssmgraph.support.UpdateButtonSupport
import javax.swing.*
import javax.swing.table.DefaultTableModel

class SSMGraphToolWindowContent {

    fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val statesComboBoxModel = DefaultComboBoxModel<String>()
        val exclusionStatesComboBox = ComboBox(statesComboBoxModel)
        val colorStatesComboBox = ComboBox(statesComboBoxModel)

        val selectedStatesArea = createSelectedStatesArea()

        val colorTableModel = createColorTableModel()
        val colorTable = createColorTable(colorTableModel, statesComboBoxModel)
        val tablePanel = createTablePanel(table = colorTable, colorStatesComboBox = colorStatesComboBox, colorTableModel = colorTableModel)

        var selectedDotFile: VirtualFile? = null

        val panel = panel {
            row {
                label("Spring state machine graph")
                    .bold()
                    .align(Align.CENTER)
            }.bottomGap(BottomGap.MEDIUM)

            row {
                checkBox("Catch the SSM launch and draw graph")
                    .applyToComponent {
                        val checkBoxService = project.service<CheckBoxService>()
                        val disposeListener = checkBoxService.addCatchSsmLaunchListener { enabled ->
                            SwingUtilities.invokeLater {
                                if (isSelected != enabled) {
                                    isSelected = enabled
                                }
                            }
                        }
                        Disposer.register(toolWindow.disposable) { disposeListener.run() }
                    }
                    .onChanged { event ->
                        project.service<CheckBoxService>().setCatchSsmLaunchEnabled(event.isSelected)
                    }
            }

            row {
                button("Sync Dot") {
                    val dotFile = ReadAction.computeBlocking<VirtualFile?, Exception> {
                        val currentFile = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
                        if (currentFile?.extension.equals("dot", ignoreCase = true)) {
                            selectedDotFile = currentFile
                        }
                        selectedDotFile
                    }

                    if (dotFile == null) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Please open a .dot file in the editor first",
                            "SSM Graph",
                            JOptionPane.WARNING_MESSAGE
                        )
                        return@button
                    }

                    val documentData = ReadAction.computeBlocking<Pair<Document, String>?, Exception> {
                        val doc = FileDocumentManager.getInstance().getDocument(dotFile)
                        if (doc != null) Pair(doc, doc.text) else null
                    }

                    if (documentData == null) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Cannot load selected .dot file document",
                            "SSM Graph",
                            JOptionPane.ERROR_MESSAGE
                        )
                        return@button
                    }

                    val (document, documentText) = documentData

                    val updatedDot = UpdateButtonSupport.updateDot(
                        documentText,
                        selectedStatesArea.text,
                        ColorTableSupport.getStateColors(colorTableModel)
                    )

                    WriteCommandAction.runWriteCommandAction(project) {
                        document.setText(updatedDot)
                        FileDocumentManager.getInstance().saveDocument(document)
                    }

                    ReadAction.runBlocking<Exception> {
                        ComboBoxSupport.updateStatesFromDotFile(dotFile, statesComboBoxModel, exclusionStatesComboBox, colorStatesComboBox)
                    }

                    val editors = ReadAction.computeBlocking<Array<FileEditor>, Exception> {
                        FileEditorManager.getInstance(project).getAllEditors(dotFile)
                    }

                    editors.forEach { editor ->
                        when (editor) {
//                            is DotPreviewEditor -> editor.refreshPreview()
                            is TextEditorWithPreview -> {
                                val previewEditor = editor.previewEditor
                                if (previewEditor is DotPreviewEditor) {
                                    previewEditor.refreshPreview()
                                }
                            }
                        }
                    }
                }
            }

            group("Exclusions") {
                row {
                    cell(exclusionStatesComboBox).label("State")
                }
                row {
                    button("Add") {
                        JTextAreaSupport.addStateToSelection(exclusionStatesComboBox.selectedItem?.toString(), selectedStatesArea)
                    }
                }
                row {
                    cell(JScrollPane(selectedStatesArea))
                        .align(Align.FILL)
                        .resizableColumn()
                }
            }

            group("Colors") {
                row {
                    cell(colorStatesComboBox).label("State")
                }
                row {
                    cell(JScrollPane(tablePanel))
                        .align(Align.FILL)
                }
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
    }

    private fun createSelectedStatesArea(): JTextArea {
        return JTextArea(6, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }
    }

    private fun createColorTableModel(): DefaultTableModel {
        return object : DefaultTableModel(arrayOf("Status", "Color"), 0) {
            override fun isCellEditable(row: Int, column: Int) = column == 0 || column == 1
        }
    }

    private fun createColorTable(model: DefaultTableModel, statesComboBoxModel: DefaultComboBoxModel<String>): JTable {
        return JBTable(model).apply {
            val stateEditor = DefaultCellEditor(ComboBox(statesComboBoxModel).apply { isEditable = true })
            columnModel.getColumn(0).cellEditor = stateEditor

            val colorOptions = arrayOf("White", "Red", "Yellow", "Green", "Gray", "Orange", "Pink", "lightBlue", "lightGreen", "lightSalmon")
            val colorEditor = DefaultCellEditor(JComboBox(colorOptions))
            columnModel.getColumn(1).cellEditor = colorEditor
        }
    }

    private fun createTablePanel(table: JTable,
                                 colorStatesComboBox: ComboBox<String>,
                                 colorTableModel: DefaultTableModel): JPanel {
        return ToolbarDecorator.createDecorator(table)
            .setAddAction { _ ->
                ColorTableSupport.addState(colorStatesComboBox.selectedItem?.toString(), colorTableModel)
            }
            .setRemoveAction { _ ->
                ColorTableSupport.removeSelectedRows(table, colorTableModel)
            }
            .createPanel()
    }
}