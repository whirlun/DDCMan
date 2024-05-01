package ddcMan

import java.awt.GridBagConstraints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane

class TabCloseButton(title: String, val id: Int, val tabs: JTabbedPane, val type: String): JPanel() {
    init {
        val closeButton = JButton("x")
        val label = JLabel(title)
        closeButton.apply {
            isBorderPainted = false
            isContentAreaFilled = false
            isOpaque = false
            addMouseListener(object: MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    for (i in 0 ..< tabs.tabCount) {
                        val tab = tabs.getTabComponentAt(i) as TabCloseButton
                        if (tab.id == id && tab.type == type) {
                            tabs.removeTabAt(i)
                            Document.store.remove(tab.id.toString())
                            break
                        }
                    }
                }
            })
        }
        this.isOpaque = false
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0
        this.add(label, gbc)
        gbc.gridx++
        gbc.weightx = 0.0
        this.add(closeButton, gbc)
    }
}