package com.ijson.plugin.ui;

import com.ijson.plugin.IJSONBundle;
import com.ijson.plugin.utils.JsonUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 新建 / 生成 JSON 对话框
 */
public class NewJsonDialog extends DialogWrapper {

    private final JTextArea inputArea;
    private final JTextArea resultArea;
    private final JRadioButton sampleRadio;
    private final JRadioButton fromInputRadio;
    private final JRadioButton emptyRadio;

    public NewJsonDialog(@Nullable Project project) {
        super(project);
        setTitle(IJSONBundle.message("json.dialog.title"));
        setSize(700, 550);

        inputArea = new JTextArea(8, 50);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setText("name=Alice\nage=30\nactive=true\nrole=admin");

        resultArea = new JTextArea(12, 50);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setEditable(false);

        sampleRadio = new JRadioButton(IJSONBundle.message("json.dialog.option.sample"), true);
        fromInputRadio = new JRadioButton(IJSONBundle.message("json.dialog.option.from-input"));
        emptyRadio = new JRadioButton(IJSONBundle.message("json.dialog.option.empty-object"));

        ButtonGroup group = new ButtonGroup();
        group.add(sampleRadio);
        group.add(fromInputRadio);
        group.add(emptyRadio);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(JBUI.Borders.empty(10));

        // 选项
        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(BorderFactory.createTitledBorder(IJSONBundle.message("json.dialog.section.generation-method")));
        options.add(sampleRadio);
        options.add(fromInputRadio);
        options.add(emptyRadio);

        // 输入
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                IJSONBundle.message("json.dialog.section.input")));
        inputPanel.add(new JBScrollPane(inputArea), BorderLayout.CENTER);

        // 结果
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder(IJSONBundle.message("json.dialog.section.result")));
        resultPanel.add(new JBScrollPane(resultArea), BorderLayout.CENTER);

        JButton generateBtn = new JButton(IJSONBundle.message("json.dialog.button.generate"));
        generateBtn.addActionListener(e -> doGenerate());

        JPanel top = new JPanel(new BorderLayout());
        top.add(options, BorderLayout.NORTH);
        top.add(inputPanel, BorderLayout.CENTER);
        top.add(generateBtn, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(resultPanel, BorderLayout.CENTER);

        // 初始生成一次
        doGenerate();

        return panel;
    }

    private void doGenerate() {
        String result;
        if (sampleRadio.isSelected()) {
            result = JsonUtils.generateSampleJson();
        } else if (emptyRadio.isSelected()) {
            result = "{\n}";
        } else {
            result = JsonUtils.generateFromInput(inputArea.getText());
        }
        resultArea.setText(result);
    }

    public String getGeneratedJson() {
        return resultArea.getText();
    }

    @Override
    protected void doOKAction() {
        if (resultArea.getText().trim().isEmpty()) {
            doGenerate();
        }
        super.doOKAction();
    }
}
