package ddcMan

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.sql.Clob
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import javax.print.Doc
import javax.swing.event.DocumentEvent


fun sidebarTree(tabs: JTabbedPane): JSplitPane {
    val toolbar = JPanel()
    toolbar.layout = BoxLayout(toolbar, BoxLayout.X_AXIS)
    val root = DefaultMutableTreeNode("Root")
    val treeModel = DefaultTreeModel(root)
    val tree = JTree(treeModel)
    Store
        .db
        .from(CollectionTable)
        .select()
        .map { row -> CollectionTable.createEntity(row) }
        .forEach { root.add(DefaultMutableTreeNode(CollectionNode(it))) }
    treeModel.reload()

    val newCollectionButton = JButton("+")
    newCollectionButton.apply {
        horizontalAlignment = SwingConstants.CENTER
        verticalAlignment = SwingConstants.CENTER
        isBorderPainted = false
        isContentAreaFilled = false
        isOpaque = false
        addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                Store.db.useConnection {
                    val collection = Collections {
                        name = "Untitled"
                        description = it.createClob().apply { setString(1, "") }
                    }
                    Store.collections.add(collection)
                }
                val newCollection = Store.collections.last()
                root.add(DefaultMutableTreeNode(CollectionNode(newCollection)))
                treeModel.reload()
            }

            override fun mouseEntered(e: MouseEvent?) {
                isContentAreaFilled = true
            }
            override fun mouseExited(e: MouseEvent?) {
                isContentAreaFilled = false
            }
        })
    }
    toolbar.apply {
        add(newCollectionButton)
    }
    tree.preferredSize = Dimension(150, 450)
    tree.minimumSize = Dimension(150, 450)
    tree.maximumSize = Dimension(150, 4500)
    tree.isRootVisible = false
    val tabIndex = tabs.tabCount - 1
    tree.addTreeSelectionListener {
        val elem = it.path.lastPathComponent
        if (elem is DefaultMutableTreeNode && elem.userObject is CollectionNode) {
            val userObject = elem.userObject as CollectionNode
            if (!Document.hasTab(userObject.id.toString())) {
                tabs.insertTab(
                    userObject.name,
                    null,
                    collectionView(userObject),
                    null,
                    tabIndex
                )
                Document.addTab(userObject.id.toString())
                tabs.setTabComponentAt(tabIndex, TabCloseButton(userObject.name, userObject.id, tabs, "collection"))
                tabs.selectedIndex = tabIndex
            }
        }
    }
    Store.rxSubject.subscribe {
        println("received signal $it")
        if (it.first.startsWith("CollectionName")) {
            val collectionId = it.first.split("|").last().toInt()
            for (node in root.children()) {
                if (node is DefaultMutableTreeNode
                    && node.userObject is CollectionNode
                    && (node.userObject as CollectionNode).id == collectionId) {
                    (node.userObject as CollectionNode).name = it.second
                    for (i in 0 ..< tabs.tabCount) {
                        val tab = tabs.getTabComponentAt(i) as TabCloseButton
                        if (tab.id == collectionId && tab.type == "collection") {
                            tabs.setTabComponentAt(i, TabCloseButton(it.second, collectionId, tabs, "collection"))
                            break
                        }
                    }
                }
            }
        } else if (it.first.startsWith("InsertRequest")) {
            val requestId = it.first.split("|").last().toInt()
            val collectionId = it.second.toInt()
            for (treeNode in root.breadthFirstEnumeration()) {
                if (treeNode is DefaultMutableTreeNode) {
                    if (treeNode.userObject is String) {
                        continue
                    }
                    if (treeNode.userObject !is CollectionNode) {
                        break
                    }
                    val collectionNode = treeNode.userObject as CollectionNode
                    if (collectionNode.id == collectionId) {
                        print(treeNode)
                        val request = Store.requests.find { r -> r.id eq requestId }
                        treeNode.add(DefaultMutableTreeNode(RequestNode(request!!)))
                    }
                }
            }
        }
        treeModel.reload()
    }
    val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, toolbar, tree)
    splitPane.dividerSize = 0
    splitPane.isEnabled = false
    return splitPane
}

fun collectionView(collection: CollectionNode): JPanel {
    val panel = JPanel()
    val gridbag = GridBagLayout()
    val constraint = GridBagConstraints()
    panel.layout = gridbag
    constraint.insets = Insets(10, 10, 10, 10)
    constraint.anchor = GridBagConstraints.NORTHWEST
    constraint.gridx = 0
    constraint.gridy = 0
    constraint.gridwidth = 1
    constraint.gridheight = GridBagConstraints.RELATIVE
    val emptyPanel1 = JPanel()
    emptyPanel1.preferredSize = Dimension(100, 100)
    panel.add(emptyPanel1, constraint)
    constraint.gridx = 1
    constraint.gridheight = 1
    val nameInput = JTextField(collection.name)
    nameInput.preferredSize = Dimension(200, 30)
    panel.add(nameInput, constraint)
    constraint.fill = GridBagConstraints.BOTH
    constraint.gridy = 1
    constraint.weightx = 1.0
    constraint.weighty = 1.0
    val descriptionInput = themedSyntaxTextArea()
    descriptionInput.apply {
        syntaxEditingStyle = RSyntaxTextArea.SYNTAX_STYLE_MARKDOWN
        text = collection.description
        preferredSize = Dimension(400, 200)
        maximumSize = Dimension(400, 200)
    }
    panel.add(RTextScrollPane(descriptionInput), constraint)
    constraint.fill = GridBagConstraints.NONE
    constraint.gridx = 2
    constraint.gridy = 0
    constraint.weightx = 0.0
    constraint.weighty = 0.0
    constraint.gridheight = GridBagConstraints.RELATIVE
    val emptyPanel2 = JPanel()
    emptyPanel2.preferredSize = Dimension(100, 100)
    panel.add(emptyPanel2, constraint)
    constraint.gridx = 0
    constraint.gridy = 2
    constraint.fill = GridBagConstraints.BOTH
    constraint.weightx = 1.0
    constraint.weighty = 1.0
    constraint.gridheight = GridBagConstraints.REMAINDER
    constraint.gridwidth = GridBagConstraints.REMAINDER
    val emptyPanel3 = JPanel()
    emptyPanel3.preferredSize = Dimension(200, (panel.height * 0.6).toInt())
    panel.add(emptyPanel3, constraint)
    nameInput.addFocusListener(object: FocusListener {
        override fun focusGained(e: FocusEvent?) {}

        override fun focusLost(e: FocusEvent?) {
            UpdateWorker(collection, nameInput.text).execute()
        }
    })
    return panel
}

fun themedSyntaxTextArea(): RSyntaxTextArea {
    val textArea = RSyntaxTextArea()
    val theme = Theme.load(object {}.javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"))
    theme.apply(textArea)
    return textArea
}

fun sidebarCard(tabs: JTabbedPane): JPanel {
    val cardLayout = CardLayout()
    val cardPanel = JPanel(cardLayout)
    cardPanel.add(sidebarTree(tabs), "Collection")
    cardPanel.add(JLabel("Environment"), "Environment")
    cardPanel.preferredSize = Dimension(150, 450)
    cardPanel.minimumSize = Dimension(150, 450)
    cardPanel.maximumSize = Dimension(150, 4500)
    return cardPanel
}

fun sidebarToolbar(cards: JPanel): JToolBar {
    val collectionButton = JButton()
    val envButton = JButton()
    val svgTranscoder = SVGTranscoder()
    collectionButton.icon = ImageIcon(svgTranscoder.readImage(
        object {}.javaClass.getResourceAsStream("/icons/folder-light.svg") ?: null, 20, 20))
    envButton.icon = ImageIcon(svgTranscoder.readImage(
        object {}.javaClass.getResourceAsStream("/icons/layers-light.svg") ?: null, 20, 20))
    collectionButton.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
            val layout = cards.layout as CardLayout
            layout.show(cards, "Collection")
        }
    })
    envButton.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
            val layout = cards.layout as CardLayout
            layout.show(cards, "Environment")
        }
    })
    val sidebar = JToolBar()
    sidebar.orientation = SwingConstants.VERTICAL
    sidebar.add(collectionButton)
    sidebar.add(envButton)
    sidebar.preferredSize = Dimension(50, 450)
    sidebar.minimumSize = Dimension(50, 450)
    sidebar.maximumSize = Dimension(50, 4500)
    return sidebar
}

fun httpParaView(tab: String): JScrollPane {
    val model = object: DefaultTableModel(arrayOf("Key", "Value", "Description"), 1) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return true
        }
    }
    registerTableEvent(model, tab, "param_uri_assoc", arrayOf("", "", ""),
        {
        listOf(model.getValueAt(it, 0)?.toString(), model.getValueAt(it, 1)?.toString(), model.getValueAt(it, 2)?.toString())
        }, {
            syncUriParams(tab, "params_table")
        }
        )
    val table = JTable(model)
    IntRange(0, table.columnModel.columnCount - 1).map {
        val defaultEditor = table.getDefaultEditor(table.getColumnClass(it)) as DefaultCellEditor
        defaultEditor.clickCountToStart = 1
    }
    return JScrollPane(table)
}

fun xWWWFormView(tab: String): JScrollPane {
    val model = object: DefaultTableModel(arrayOf("Key", "Value", "Description"), 1) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return true
        }
    }
    model.addRow(arrayOf("", "", ""))
    registerTableEvent(model, tab, "req_body", arrayOf("", "", ""),
        {
            listOf(model.getValueAt(it, 0)?.toString(), model.getValueAt(it, 1)?.toString(), model.getValueAt(it, 2)?.toString())
        }, {
            println(Document.get<List<List<String>>>(tab, "req_body"))

        }
    )
    val table = JTable(model)
    IntRange(0, table.columnModel.columnCount - 1).map {
        val defaultEditor = table.getDefaultEditor(table.getColumnClass(it)) as DefaultCellEditor
        defaultEditor.clickCountToStart = 1
    }
    return JScrollPane(table)
}

fun formDataBodyView(tab: String): JScrollPane {
    val model = object: DefaultTableModel(arrayOf("Key", "Type", "Value", "Description"), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return true
        }
    }
    val table = JTable(model)
    val typeSelector = JComboBox(arrayOf("Text", "File"))
    val typeColumn = table.columnModel.getColumn(1)
    typeColumn.cellEditor = DefaultCellEditor(typeSelector)
    typeColumn.maxWidth = 50
    model.addRow(arrayOf("", "Text", "", ""))
    registerTableEvent(model, tab, "req_body", arrayOf("", "Text", "", ""),
        {
            listOf(
                model.getValueAt(it, 0).toString() + ":;" + model.getValueAt(it, 1).toString(),
                model.getValueAt(it, 2).toString(),
                model.getValueAt(it, 3).toString()
            )
        },
        {
//            Document.put(tab, "req_body", Document.get<List<List<String?>>>(tab, "req_body")!!
//                .associateBy({it[0]}, {it[1]}))
            println(Document.get<List<List<String>>>(tab, "req_body"))

        }
    )
    IntRange(0, table.columnModel.columnCount - 1).map {
        val defaultEditor = table.getDefaultEditor(table.getColumnClass(it)) as DefaultCellEditor
        defaultEditor.clickCountToStart = 1
    }
    return JScrollPane(table)
}

fun scriptEditorView(tab: String, area: String): RTextScrollPane {
    val textArea = Document.get<RSyntaxTextArea>(tab, area)
    textArea?.apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
        isCodeFoldingEnabled = true
    }
    return RTextScrollPane(textArea)
}

fun repResultView(tab: String): RTextScrollPane {
    val textArea = Document.get<RSyntaxTextArea>(tab, "result_text_area")
    textArea?.apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
        isCodeFoldingEnabled = true
        isEditable = false
    }
    return RTextScrollPane(textArea)
}

fun rawResultView(tab: String): RTextScrollPane {
    val textArea = Document.get<RSyntaxTextArea>(tab, "raw_text_area")
    textArea?.apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_NONE
        isEditable = false
    }
    return RTextScrollPane(textArea)
}

fun resultView(tab: String): JTabbedPane {
    val tabPane = JTabbedPane()
    tabPane.apply {
        addTab("Pretty", repResultView(tab))
        add("Raw", rawResultView(tab))
        add("Cookies", JLabel("Cookies"))
    }
    return tabPane
}

fun bodyView(tab: String): JSplitPane {
    val bodyType = JComboBox(arrayOf("none", "form-data", "x-www-form-url-encoded", "raw", "binary"))
    val bodyCard = JPanel(CardLayout())
    bodyCard.apply {
        add(JLabel("This request doesn't have a body"), "none")
        add(formDataBodyView(tab), "form-data")
        add(xWWWFormView(tab), "x-www-form-urlencoded")
    }
    bodyType.addItemListener {
        val cl = bodyCard.layout as CardLayout
        cl.show(bodyCard, it.item as String)
        Document.put(tab, "body_type", BodyType.valueOfBodyType(it.item as String))
    }
    val splitPanel = JSplitPane(JSplitPane.VERTICAL_SPLIT, bodyType, bodyCard)
    splitPanel.isEnabled = false
    splitPanel.dividerSize = 0
    return splitPanel
}

fun httpTestInnerView(tab: String): JSplitPane {
    val tabs = JTabbedPane()
    tabs.apply {
        add("Parameters", httpParaView(tab))
        add("Body", bodyView(tab))
        add("Headers", JLabel("Headers"))
        add("Authorization", JLabel("Authorization"))
        add("Pre-request Script", scriptEditorView(tab, "pre_script_text_area"))
    }
    val splitPanel = JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, resultView(tab))
    return splitPanel
}

fun showSaveOptionPane(tab: String) {
    val dialog = JPanel()
    val layout = BoxLayout(dialog, BoxLayout.Y_AXIS)
    dialog.layout = layout
    val nameInput = JTextField()
    dialog.add(JLabel("Name"))
    dialog.add(nameInput)
    val collections = Store
        .db
        .from(CollectionTable)
        .select()
        .map { CollectionNode(CollectionTable.createEntity(it)) }
        .toTypedArray()
    val collectionInput = JComboBox(collections)
    dialog.add(JLabel("Collection"))
    dialog.add(collectionInput)
    val result = JOptionPane.showConfirmDialog(null, dialog, "Save", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
    if (result == JOptionPane.OK_OPTION) {

        InsertWorker(tab, nameInput.text, (collectionInput.selectedItem as CollectionNode).id).execute()
        Store.rxSubject.subscribe {
            if (it.first.startsWith("InsertRequest")) {
                val requestId = it.first.split("|")[1].toInt()
                Document.put(tab, "id", requestId)
            }
        }
    }
}

fun httpTestView(tab: String): JSplitPane {
    val methodCombobox = JComboBox(arrayOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"))
    methodCombobox.preferredSize = Dimension(100, 30)
    methodCombobox.maximumSize = Dimension(100, 30)
    methodCombobox.addActionListener {
        Document.put(tab, "method", HTTPMETHOD.valueOf(methodCombobox.selectedItem!!.toString()))
    }
    val sendButton = JButton("Send")
    val saveButton = JButton("Save")
    saveButton.addActionListener { showSaveOptionPane(tab) }
    val uriInput = Document.get<JTextField>(tab, "uri_input")
    sendButton.addActionListener {
        val client = HttpClient()
        val bodyType = Document.get<BodyType>(tab, "body_type")!!
        val response = when (Document.get<HTTPMETHOD>(tab, "method")) {
            HTTPMETHOD.GET -> client.get(uriInput!!.text, mapOf())
            HTTPMETHOD.POST -> Document.get<List<List<String>>>(tab, "req_body")?.let { it1 ->
                client.post(uriInput!!.text, mapOf(), it1.associateBy({it2 -> it2[0]}, {it2 -> it2[1]}), bodyType)
            }
            HTTPMETHOD.PUT -> Document.get<List<List<String>>>(tab, "req_body")?.let { it1 ->
                client.put(uriInput!!.text, mapOf(), it1.associateBy({it2 -> it2[0]}, {it2 -> it2[1]}), bodyType)
            }
            HTTPMETHOD.PATCH -> Document.get<List<List<String>>>(tab, "req_body")?.let { it1 ->
                client.patch(uriInput!!.text, mapOf(), it1.associateBy({it2 -> it2[0]}, {it2 -> it2[1]}), bodyType)
            }
            HTTPMETHOD.DELETE -> Document.get<List<List<String>>>(tab, "req_body")?.let { it1 ->
                client.delete(uriInput!!.text, mapOf(), it1.associateBy({it2 -> it2[0]}, {it2 -> it2[1]}), bodyType)
            }
            HTTPMETHOD.HEAD -> client.head(uriInput!!.text, mapOf())
            HTTPMETHOD.OPTIONS -> client.options(uriInput!!.text, mapOf())
            else -> null
        }
        val resultTextArea = Document.get<RSyntaxTextArea>(tab, "result_text_area")
        resultTextArea!!.text = JSUtils.jsonBeautify(response?.first ?: "")
        val rawTextArea = Document.get<RSyntaxTextArea>(tab, "raw_text_area")
        rawTextArea!!.text = response?.first ?: ""

        val status = Document.get<JLabel>(tab, "status")
        status!!.text = buildString {
            append(response?.second.toString() + " ")
            append(response?.third.toString() + "ms ")
            append(response?.fourth?.get("content-length")?.plus("bytes") ?: "")
        }
    }
    val upperPanel = JPanel()
    upperPanel.layout = BoxLayout(upperPanel, BoxLayout.X_AXIS)
    upperPanel.apply {
        add(methodCombobox)
        add(Box.createHorizontalStrut(10))
        add(uriInput)
        add(Box.createHorizontalStrut(10))
        add(sendButton)
        add(Box.createHorizontalStrut(10))
        add(saveButton)
    }
    val splitPanel = JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel, httpTestInnerView(tab))
    splitPanel.isEnabled = false
    splitPanel.dividerSize = 0
    return splitPanel
}

fun protocolSelectView(tab: String): JSplitPane {
    val protocolCombobox = JComboBox(arrayOf("REST", "GRPC"))
    protocolCombobox.preferredSize = Dimension(100, 30)
    protocolCombobox.maximumSize = Dimension(100, 30)
    val urlPanel = JPanel()
    urlPanel.layout = BoxLayout(urlPanel, BoxLayout.X_AXIS)
    urlPanel.apply {
        add(Box.createHorizontalStrut(10))
        add(JLabel(tab))
        add(Box.createHorizontalGlue())
        add(Document.get<JLabel>(tab, "status"))
        add(Box.createHorizontalStrut(20))
    }
    val panel = JSplitPane(JSplitPane.VERTICAL_SPLIT, urlPanel, httpTestView(tab))
    panel.dividerSize = 0
    panel.isEnabled = false
    panel.dividerLocation = 50
    return panel
}

fun tabView(): JTabbedPane {
    val newTabButton = JButton("+")
    newTabButton.margin = Insets(0, 0, 3, 0)
    newTabButton.horizontalAlignment = SwingConstants.CENTER
    newTabButton.verticalAlignment = SwingConstants.CENTER
    val panel = JTabbedPane()
    newTabButton.addActionListener {
        var i = 0
        while (Document.hasTab("Untitled$i")) {
            i++
        }
        Document.addDocument("Untitled$i")
        val index = panel.tabCount - 1
        panel.insertTab("Untitled$i", null, protocolSelectView("Untitled$i"), null, panel.tabCount - 1)
        panel.setTabComponentAt(index, TabCloseButton("Untitled$i", i, panel, "request"))
        panel.selectedIndex = index
    }
    //panel.addTab("Untitled", protocolSelectView("Untitled"))
    panel.addTab("new", JPanel())
    panel.setTabComponentAt(0, newTabButton)
    panel.setEnabledAt(0, false)

    return panel
}