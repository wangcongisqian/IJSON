package com.ijson.plugin.actions;

import com.ijson.plugin.ui.JsonTreePanel;
import com.ijson.plugin.utils.JsonUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 美化 JSON 并打开可交互树编辑对话框 / 工具窗口
 */
public class BeautifyJsonAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        String text = null;
        boolean hasSelection = false;
        if (editor != null) {
            SelectionModel selection = editor.getSelectionModel();
            if (selection.hasSelection()) {
                text = selection.getSelectedText();
                hasSelection = true;
            } else {
                text = editor.getDocument().getText();
            }
        }

        if (text == null || text.trim().isEmpty()) {
            Messages.showWarningDialog(project, "请先选中 JSON 文本，或打开一个包含 JSON 的文件", "美化 JSON");
            return;
        }

        JsonUtils.ValidationResult result = JsonUtils.validate(text);
        if (!result.isValid()) {
            Messages.showErrorDialog(project, result.getMessage(), "无法美化 - JSON 不合法");
            return;
        }

        try {
            String pretty = JsonUtils.beautify(text);

            // 弹出交互式树对话框
            BeautifyDialog dialog = new BeautifyDialog(project, pretty);
            if (dialog.showAndGet()) {
                String finalJson = dialog.getResultJson();
                // 如果用户确认，写回编辑器
                if (finalJson != null) {
                    final boolean sel = hasSelection;
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        if (sel) {
                            editor.getDocument().replaceString(
                                    editor.getSelectionModel().getSelectionStart(),
                                    editor.getSelectionModel().getSelectionEnd(),
                                    finalJson
                            );
                        } else {
                            editor.getDocument().setText(finalJson);
                        }
                    });
                }
            }

            // 同时尝试打开工具窗口并加载
            openToolWindowWithJson(project, pretty);

        } catch (Exception ex) {
            Messages.showErrorDialog(project, "美化失败: " + ex.getMessage(), "IJSON");
        }
    }

    private void openToolWindowWithJson(Project project, String json) {
        if (project == null) return;
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("IJSON");
        if (toolWindow != null) {
            toolWindow.show(() -> {
                Content content = toolWindow.getContentManager().getContent(0);
                if (content != null && content.getComponent() instanceof com.ijson.plugin.ui.JsonToolWindowPanel) {
                    ((com.ijson.plugin.ui.JsonToolWindowPanel) content.getComponent()).loadJson(json);
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    /**
     * 美化 + 树编辑对话框
     */
    private static class BeautifyDialog extends DialogWrapper {
        private final JsonTreePanel treePanel;
        private String resultJson;

        public BeautifyDialog(@Nullable Project project, String json) {
            super(project);
            setTitle("美化 JSON - 交互式树编辑 (可收缩/展开、增删节点)");
            setSize(800, 600);
            treePanel = new JsonTreePanel();
            treePanel.setJson(json);
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(treePanel, BorderLayout.CENTER);
            JLabel tip = new JLabel("提示：右键节点可增删/编辑，双击叶子节点可修改值，工具栏支持展开/收缩");
            tip.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panel.add(tip, BorderLayout.SOUTH);
            return panel;
        }

        @Override
        protected void doOKAction() {
            resultJson = treePanel.getJsonString();
            super.doOKAction();
        }

        public String getResultJson() {
            return resultJson;
        }
    }
}
