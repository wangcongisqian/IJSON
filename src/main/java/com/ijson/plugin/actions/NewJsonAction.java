package com.ijson.plugin.actions;

import com.ijson.plugin.ui.NewJsonDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 新建 / 生成 JSON 动作
 */
public class NewJsonAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        NewJsonDialog dialog = new NewJsonDialog(project);
        if (dialog.showAndGet()) {
            String json = dialog.getGeneratedJson();
            if (json == null || json.trim().isEmpty()) {
                Messages.showWarningDialog(project, "未生成有效 JSON", "IJSON");
                return;
            }

            // 选项：插入到当前编辑器 或 新建临时文件
            String[] options = {"插入到当前编辑器", "打开为新文件", "仅复制到剪贴板"};
            int choice = Messages.showDialog(
                    project,
                    "请选择如何使用生成的 JSON：",
                    "新建 JSON 成功",
                    options,
                    0,
                    Messages.getInformationIcon()
            );

            if (choice == 0) {
                Editor editor = e.getData(CommonDataKeys.EDITOR);
                if (editor != null) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        int offset = editor.getCaretModel().getOffset();
                        editor.getDocument().insertString(offset, json);
                    });
                } else {
                    openAsNewFile(project, json);
                }
            } else if (choice == 1) {
                openAsNewFile(project, json);
            } else if (choice == 2) {
                com.intellij.openapi.ide.CopyPasteManager.getInstance()
                        .setContents(new java.awt.datatransfer.StringSelection(json));
                Messages.showInfoMessage(project, "已复制到剪贴板", "IJSON");
            }
        }
    }

    private void openAsNewFile(Project project, String json) {
        LightVirtualFile file = new LightVirtualFile("generated.json", json);
        FileEditorManager.getInstance(project).openFile(file, true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
