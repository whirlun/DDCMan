package ddcMan

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.last
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.sql.Clob
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode

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
    tree.addTreeSelectionListener {
        val elem = it.path.lastPathComponent
        if (elem is DefaultMutableTreeNode && elem.userObject is CollectionNode) {
            tabs.insertTab((elem.userObject as CollectionNode).name,
                null,
                collectionView(elem.userObject as CollectionNode),
                null,
                tabs.tabCount - 1)
            tabs.selectedIndex = tabs.tabCount - 1
        }
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
    constraint.weightx = 0.0
    constraint.weighty = 0.0
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
        listOf(model.getValueAt(it, 0)?.toString(), model.getValueAt(it, 1)?.toString())
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
            listOf(model.getValueAt(it, 0)?.toString(), model.getValueAt(it, 1)?.toString())
        }, {
            Document.put(tab, "req_body", Document.get<List<List<String?>>>(tab, "req_body")!!
                .associateBy({it[0]}, {it[1]}))
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
                model.getValueAt(it, 2).toString()
            )
        },
        {
            Document.put(tab, "req_body", Document.get<List<List<String?>>>(tab, "req_body")!!
                .associateBy({it[0]}, {it[1]}))
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

fun httpTestView(tab: String): JSplitPane {
    val methodCombobox = JComboBox(arrayOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"))
    methodCombobox.preferredSize = Dimension(100, 30)
    methodCombobox.maximumSize = Dimension(100, 30)
    val sendButton = JButton("Send")
    val saveButton = JButton("Save")
    val uriInput = Document.get<JTextField>(tab, "uri_input")
    sendButton.addActionListener {
        val client = HttpClient()
        val bodyType = Document.get<BodyType>(tab, "body_type")!!
        val response = when (methodCombobox.selectedItem) {
            "GET" -> client.get(uriInput!!.text, mapOf())
            "POST" -> Document.get<Map<String, String>>(tab, "req_body")?.let { it1 ->
                client.post(uriInput!!.text, mapOf(), it1, bodyType)
            }
            "PUT" -> Document.get<Map<String, String>>(tab, "req_body")?.let { it1 ->
                client.put(uriInput!!.text, mapOf(), it1, bodyType)
            }
            "PATCH" -> Document.get<Map<String, String>>(tab, "req_body")?.let { it1 ->
                client.patch(uriInput!!.text, mapOf(), it1, bodyType)
            }
            "DELETE" -> Document.get<Map<String, String>>(tab, "req_body")?.let { it1 ->
                client.delete(uriInput!!.text, mapOf(), it1, bodyType)
            }
            "HEAD" -> client.head(uriInput!!.text, mapOf())
            "OPTIONS" -> client.options(uriInput!!.text, mapOf())
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
        panel.insertTab("Untitled$i", null, protocolSelectView("Untitled$i"), null, panel.tabCount - 1)
        panel.selectedIndex = panel.tabCount - 2
    }
    //panel.addTab("Untitled", protocolSelectView("Untitled"))
    panel.addTab("new", JPanel())
    panel.setTabComponentAt(0, newTabButton)
    panel.setEnabledAt(0, false)
    return panel
}