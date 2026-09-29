package com.ijson.plugin.ui;

import com.ijson.plugin.IJSONBundle;
import com.ijson.plugin.utils.JsonUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * 工具窗口主面板：输入区 + 树编辑区 + 操作按钮
 */
public class JsonToolWindowPanel extends JPanel {

    private final Project project;
    private final JTextArea inputArea;
    private final JsonTreePanel treePanel;

    public JsonToolWindowPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        setBorder(JBUI.Borders.empty(8));

        // 输入区
        inputArea = new JTextArea(6, 40);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setText("{\n  \"hello\": \"IJSON\"\n}");
        JBScrollPane inputScroll = new JBScrollPane(inputArea);
        //JSON输入/编辑
        inputScroll.setBorder(BorderFactory.createTitledBorder(IJSONBundle.message("inputArea.title")));

        // 树面板
        treePanel = new JsonTreePanel();

        // 按钮栏
        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
        //校验、美化并加载到树、从树导出到输入、复制美化结果、复制压缩结果、生成示例、根据输入生成
        toolBarPanel.add(iconButton(IJSONBundle.message("action.validate"), AllIcons.Actions.Execute, this::validateJson));
        toolBarPanel.add(iconButton(IJSONBundle.message("action.beautifyAndLoad"), AllIcons.Actions.Edit, this::beautifyAndLoad));
        toolBarPanel.add(iconButton(IJSONBundle.message("action.exportFromTree"), AllIcons.Nodes.Folder, this::exportFromTree));
        toolBarPanel.add(iconButton(IJSONBundle.message("action.copyPretty"), AllIcons.Actions.Copy, this::copyPretty));
        toolBarPanel.add(iconButton(IJSONBundle.message("action.copyCompact"),AllIcons.General.CopyHovered, this::copyCompact));
        toolBarPanel.add(iconButton(IJSONBundle.message("action.generateSample"), AllIcons.General.Add, this::generateSample));
        toolBarPanel.add(iconButton(IJSONBundle.message("action.generateFromInput"), AllIcons.General.Export, this::generateFromInput));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScroll, treePanel);
        split.setResizeWeight(0.3);
        split.setBorder(null);

        add(toolBarPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    /** Icon 工具按钮 */
    private static JButton iconButton(String tooltip, Icon icon, Runnable action) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setMargin(JBUI.insets(2));
        btn.setBorder(JBUI.Borders.empty(2));
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void validateJson() {
        String text = inputArea.getText();
        JsonUtils.ValidationResult result = JsonUtils.validate(text);
        if (result.isValid()) {
            //JSON校验成功
            Messages.showInfoMessage(project, result.getMessage(), IJSONBundle.message("action.validate.success"));
        } else {
            //JSON校验失败
            Messages.showErrorDialog(project, result.getMessage(), IJSONBundle.message("action.validate.fail"));
        }
    }

    private void beautifyAndLoad() {
        String text = inputArea.getText();
        JsonUtils.ValidationResult result = JsonUtils.validate(text);
        if (!result.isValid()) {
            //JSON不合法，无法美化
            Messages.showErrorDialog(project, result.getMessage(), IJSONBundle.message("action.validate.fail"));
            return;
        }
        try {
            String pretty = JsonUtils.beautify(text);
            inputArea.setText(pretty);
            treePanel.setJson(pretty);
        } catch (Exception e) {
            Messages.showErrorDialog(project, e.getMessage(), IJSONBundle.message("action.validate.fail"));
        }
    }

    private void exportFromTree() {
        String json = treePanel.getJsonString();
        inputArea.setText(json);
    }

    private void copyPretty() {
        String json = treePanel.getJsonString();
        CopyPasteManager.getInstance().setContents(new StringSelection(json));
        //已复制美化后的 JSON 到剪贴板
        Messages.showInfoMessage(project, IJSONBundle.message("action.copy.beautify.success"), "IJSON");
    }

    private void copyCompact() {
        String json = treePanel.getCompactJsonString();
        CopyPasteManager.getInstance().setContents(new StringSelection(json));
        //已复制压缩后的 JSON 到剪贴板
        Messages.showInfoMessage(project, IJSONBundle.message("action.copy.compact.success"), "IJSON");
    }

    private void generateSample() {
        String sample = JsonUtils.generateSampleJson();
        inputArea.setText(sample);
        treePanel.setJson(sample);
    }

    private void generateFromInput() {
        String input = inputArea.getText();
        String generated = JsonUtils.generateFromInput(input);
        inputArea.setText(generated);
        treePanel.setJson(generated);
    }

    /**
     * 外部调用：加载指定 JSON 到工具窗口
     */
    public void loadJson(String json) {
        inputArea.setText(json);
        try {
            treePanel.setJson(json);
        } catch (Exception ignored) {
        }
    }
}
