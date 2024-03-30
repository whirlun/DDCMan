package ddcMan

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme
import com.formdev.flatlaf.util.SystemInfo
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.BoxLayout
import javax.swing.UIManager
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.WindowConstants
import javax.swing.border.EmptyBorder
import javax.swing.plaf.FontUIResource

fun setGlobalFont() {
    val defaultFont = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getAvailableFontFamilyNames(null)[0]
    val fontResource = FontUIResource(defaultFont, Font.PLAIN, 20)
    val uiKeys = UIManager.getDefaults()
    uiKeys.forEach {
        if (it.value is FontUIResource) {
            UIManager.put(it.key, it.value)
        }
    }
}

fun main() {
    UIManager.setLookAndFeel(FlatMonokaiProIJTheme())
    JFrame.setDefaultLookAndFeelDecorated(false)
    UIManager.put("Label.foreground", "#FFFFFF")
    setGlobalFont()
    extractScriptToDataFolder()
    //Document.addDocument("Untitled")
    val frame = JFrame("")
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.size = Dimension(1000, 600)
    val titlePanel = JPanel()
    titlePanel.layout = BoxLayout(titlePanel, BoxLayout.X_AXIS)
    val title = JLabel("ddcMan")
    title.border = EmptyBorder(0, 100, 0, 0)
    titlePanel.add(title)
    val bodyPanel = JPanel()
    val tabs = tabView()
    val cards = sidebarCard(tabs)
    bodyPanel.layout = BoxLayout(bodyPanel,BoxLayout.X_AXIS)
    bodyPanel.add(sidebarToolbar(cards))
    bodyPanel.add(cards)
    bodyPanel.add(tabs)
    val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, titlePanel, bodyPanel)
    splitPane.dividerSize = 0
    splitPane.isEnabled = false
    splitPane.dividerLocation = 40
    if (SystemInfo.isMacFullWindowContentSupported) {
        frame.rootPane.putClientProperty("apple.awt.fullWindowContent",true)
        frame.rootPane.putClientProperty("apple.awt.transparentTitleBar",true)
    }
    frame.contentPane.add(splitPane, BorderLayout.CENTER)
    frame.isVisible = true
    Store.db
    JSUtils.jsonBeautify("")
}