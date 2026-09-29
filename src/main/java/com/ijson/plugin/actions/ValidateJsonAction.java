package com.ijson.plugin.actions;

import com.ijson.plugin.utils.JsonUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 校验 JSON 动作
 */
public class ValidateJsonAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        String text = null;
        if (editor != null) {
            SelectionModel selection = editor.getSelectionModel();
            if (selection.hasSelection()) {
                text = selection.getSelectedText();
            } else {
                text = editor.getDocument().getText();
            }
        }

        if (text == null || text.trim().isEmpty()) {
            Messages.showWarningDialog(project, "请先选中 JSON 文本，或打开一个包含 JSON 的文件", "校验 JSON");
            return;
        }

        JsonUtils.ValidationResult result = JsonUtils.validate(text);
        if (result.isValid()) {
            Messages.showInfoMessage(project, result.getMessage(), "JSON 校验成功");
        } else {
            Messages.showErrorDialog(project, result.getMessage(), "JSON 校验失败");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
