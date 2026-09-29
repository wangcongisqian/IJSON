package com.ijson.plugin.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.ijson.plugin.IJSONBundle;
import com.ijson.plugin.model.JsonTreeNode;
import com.ijson.plugin.utils.JsonUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Interactive JSON tree panel with node expansion, editing, addition, and removal.
 */
public class JsonTreePanel extends JPanel {

    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private JsonTreeNode rootNode;
    private final JTextArea previewArea;
    private final JLabel statusLabel;

    public JsonTreePanel() {
        super(new BorderLayout());
        setBorder(JBUI.Borders.empty(5));

        // Tree
        rootNode = new JsonTreeNode(null, JsonTreeNode.NodeType.ROOT, null);
        treeModel = new DefaultTreeModel(rootNode);
        tree = new Tree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new JsonTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Context menu
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        tree.setSelectionPath(path);
                        showPopupMenu(e.getX(), e.getY(), path);
                    }
                }
            }
        });

        // Double-click to edit leaf nodes
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getSelectionPath();
                    if (path != null) {
                        JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                        if (node.isLeafType()) {
                            editNodeValue(node);
                        }
                    }
                }
            }
        });

        JBScrollPane treeScroll = new JBScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(400, 300));

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(createButton(IJSONBundle.message("action.expand.all"), AllIcons.Actions.Expandall, this::expandAll));
        toolBar.add(createButton(IJSONBundle.message("action.contract.all"), AllIcons.Actions.Collapseall, this::collapseAll));
        toolBar.addSeparator();
        toolBar.add(createButton(IJSONBundle.message("action.add.nodes"), AllIcons.General.Add, this::addNode));
        toolBar.add(createButton(IJSONBundle.message("action.delete.nodes"), AllIcons.General.Remove, this::removeNode));
        toolBar.addSeparator();
        toolBar.add(createButton(IJSONBundle.message("action.refresh.preview"), AllIcons.Actions.Refresh, this::updatePreview));

        // Preview
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        previewArea.setBackground(JBColor.background());
        JBScrollPane previewScroll = new JBScrollPane(previewArea);
        previewScroll.setPreferredSize(new Dimension(400, 200));

        statusLabel = new JLabel(IJSONBundle.message("json.tree.status.ready"));
        statusLabel.setBorder(JBUI.Borders.empty(2, 5));

        // Toolbar and tree above, preview below
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolBar, BorderLayout.NORTH);
        topPanel.add(treeScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, previewScroll);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JButton createButton(String tooltip, Icon icon, Runnable action) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public void setJson(String json) {
        try {
            JsonNode node = JsonUtils.parse(json);
            rootNode = JsonTreeNode.fromJsonNode(node);
            // Ensure the root node has the ROOT type
            if (rootNode.getType() == JsonTreeNode.NodeType.OBJECT) {
                rootNode.setType(JsonTreeNode.NodeType.ROOT);
            }
            treeModel.setRoot(rootNode);
            treeModel.reload();
            expandAll();
            updatePreview();
            statusLabel.setText(IJSONBundle.message("json.tree.status.loaded"));
        } catch (Exception e) {
            statusLabel.setText(IJSONBundle.message("json.tree.status.load.failed", e.getMessage()));
            Messages.showErrorDialog(IJSONBundle.message("json.tree.error.parse", e.getMessage()), "IJSON");
        }
    }

    public void setJsonNode(JsonNode node) {
        rootNode = JsonTreeNode.fromJsonNode(node);
        if (rootNode.getType() == JsonTreeNode.NodeType.OBJECT) {
            rootNode.setType(JsonTreeNode.NodeType.ROOT);
        }
        treeModel.setRoot(rootNode);
        treeModel.reload();
        expandAll();
        updatePreview();
    }

    public String getJsonString() {
        return JsonUtils.toPrettyString(rootNode.toJsonNode());
    }

    public String getCompactJsonString() {
        return JsonUtils.toCompactString(rootNode.toJsonNode());
    }

    public JsonNode getJsonNode() {
        return rootNode.toJsonNode();
    }


    /**
     * 展开到指定层级（根节点为第 0 级）。
     * level <= 0 时只保证根节点可见；level 越大展开越深。
     */
    private void expandToLevel(int level) {
        if (rootNode == null) return;
        expandNodesToLevel(new TreePath(rootNode), 0, level);
    }

    private void expandNodesToLevel(TreePath path, int currentLevel, int maxLevel) {
        if (currentLevel >= maxLevel) {
            return;
        }
        tree.expandPath(path);
        JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
        for (int i = 0; i < node.getChildCount(); i++) {
            TreePath childPath = path.pathByAddingChild(node.getChildAt(i));
            expandNodesToLevel(childPath, currentLevel + 1, maxLevel);
        }
    }

    /**
     * 折叠超过指定层级的节点（根节点为第 0 级）。
     * 先把目标层级以内的节点展开，再把更深的节点全部折叠。
     */
    private void collapseToLevel(int level) {
        if (rootNode == null) return;
        // 先展开到目标层级，保证可见性
        expandToLevel(level);
        // 再从深到浅折叠超过目标层级的节点
        collapseNodesBeyondLevel(new TreePath(rootNode), 0, level);
    }

    private void collapseNodesBeyondLevel(TreePath path, int currentLevel, int maxLevel) {
        JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
        // 先处理子节点（后序，避免路径失效）
        for (int i = 0; i < node.getChildCount(); i++) {
            TreePath childPath = path.pathByAddingChild(node.getChildAt(i));
            collapseNodesBeyondLevel(childPath, currentLevel + 1, maxLevel);
        }
        if (currentLevel > maxLevel) {
            tree.collapsePath(path);
        }
    }

    /** 工具栏“全部展开”按钮：弹出输入框指定层级后展开 */
    private void expandAll() {
        Integer level = promptForLevel(
                IJSONBundle.message("json.tree.prompt.expand-level"),
                IJSONBundle.message("json.tree.dialog.expand-level"),
                2   // 默认展开到第 2 级
        );
        if (level == null) return;
        expandToLevel(level);
        statusLabel.setText(IJSONBundle.message("json.tree.status.expanded-to-level", level));
    }

    /** 工具栏“全部折叠”按钮：弹出输入框指定层级后折叠 */
    private void collapseAll() {
        Integer level = promptForLevel(
                IJSONBundle.message("json.tree.prompt.collapse-level"),
                IJSONBundle.message("json.tree.dialog.collapse-level"),
                0   // 默认折叠到根
        );
        if (level == null) return;
        collapseToLevel(level);
        statusLabel.setText(IJSONBundle.message("json.tree.status.collapsed-to-level", level));
    }

    /**
     * 弹出输入框，获取用户输入的层级数字。
     * @return 合法非负整数；用户取消或输入非法时返回 null
     */
    private Integer promptForLevel(String message, String title, int defaultValue) {
        String input = Messages.showInputDialog(
                message,
                title,
                null,
                String.valueOf(defaultValue),
                null
        );
        if (input == null) return null;
        input = input.trim();
        try {
            int level = Integer.parseInt(input);
            if (level < 0) {
                Messages.showErrorDialog(
                        IJSONBundle.message("json.tree.error.invalid-level"),
                        "IJSON"
                );
                return null;
            }
            return level;
        } catch (NumberFormatException e) {
            Messages.showErrorDialog(
                    IJSONBundle.message("json.tree.error.invalid-level"),
                    "IJSON"
            );
            return null;
        }
    }

    private void addNode() {
        TreePath path = tree.getSelectionPath();
        JsonTreeNode parent;
        if (path == null) {
            parent = rootNode;
        } else {
            parent = (JsonTreeNode) path.getLastPathComponent();
            if (!parent.isContainer()) {
                parent = (JsonTreeNode) parent.getParent();
                if (parent == null) parent = rootNode;
            }
        }

        String[] options = {"Object", "Array", "String", "Number", "Boolean", "Null"};
        int choice = Messages.showChooseDialog(
                IJSONBundle.message("json.tree.prompt.node-type"),
                IJSONBundle.message("json.tree.dialog.add-node"),
                options,
                options[2],
                null
        );
        if (choice < 0) return;

        JsonTreeNode.NodeType type = switch (choice) {
            case 0 -> JsonTreeNode.NodeType.OBJECT;
            case 1 -> JsonTreeNode.NodeType.ARRAY;
            case 2 -> JsonTreeNode.NodeType.STRING;
            case 3 -> JsonTreeNode.NodeType.NUMBER;
            case 4 -> JsonTreeNode.NodeType.BOOLEAN;
            default -> JsonTreeNode.NodeType.NULL;
        };

        String key = "newKey";
        if (parent.getType() == JsonTreeNode.NodeType.ARRAY) {
            key = String.valueOf(parent.getChildCount());
        } else {
            String input = Messages.showInputDialog(
                    IJSONBundle.message("json.tree.prompt.field-name"),
                    IJSONBundle.message("json.tree.dialog.add-node"), null, key, null);
            if (input == null || input.trim().isEmpty()) return;
            key = input.trim();
        }

        Object value = null;
        if (type == JsonTreeNode.NodeType.STRING) {
            value = Messages.showInputDialog(
                    IJSONBundle.message("json.tree.prompt.string-value"),
                    IJSONBundle.message("json.tree.dialog.add-node"), null, "", null);
            if (value == null) value = "";
        } else if (type == JsonTreeNode.NodeType.NUMBER) {
            String num = Messages.showInputDialog(
                    IJSONBundle.message("json.tree.prompt.number"),
                    IJSONBundle.message("json.tree.dialog.add-node"), null, "0", null);
            if (num == null) return;
            try {
                if (num.contains(".")) {
                    value = Double.parseDouble(num);
                } else {
                    value = Long.parseLong(num);
                }
            } catch (NumberFormatException e) {
                Messages.showErrorDialog(IJSONBundle.message("json.tree.error.invalid-number"), "IJSON");
                return;
            }
        } else if (type == JsonTreeNode.NodeType.BOOLEAN) {
            int b = Messages.showYesNoDialog(
                    IJSONBundle.message("json.tree.prompt.boolean"),
                    IJSONBundle.message("json.tree.dialog.add-node"), "true", "false", null);
            value = b == Messages.YES;
        }

        JsonTreeNode newNode = new JsonTreeNode(key, type, value);
        treeModel.insertNodeInto(newNode, parent, parent.getChildCount());
        tree.expandPath(new TreePath(parent.getPath()));
        updatePreview();
        statusLabel.setText(IJSONBundle.message("json.tree.status.node-added", key));
    }

    private void removeNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            Messages.showInfoMessage(IJSONBundle.message("json.tree.info.select-node-to-delete"), "IJSON");
            return;
        }
        JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
        if (node == rootNode) {
            Messages.showWarningDialog(IJSONBundle.message("json.tree.error.delete-root"), "IJSON");
            return;
        }
        int confirm = Messages.showYesNoDialog(
                IJSONBundle.message("json.tree.confirm.delete-node", node),
                IJSONBundle.message("json.tree.dialog.delete-node"), null);
        if (confirm != Messages.YES) return;

        treeModel.removeNodeFromParent(node);
        // Reindex children if the parent is an array
        JsonTreeNode parent = (JsonTreeNode) node.getParent();
        if (parent != null && parent.getType() == JsonTreeNode.NodeType.ARRAY) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                JsonTreeNode child = (JsonTreeNode) parent.getChildAt(i);
                child.setKey(String.valueOf(i));
                treeModel.nodeChanged(child);
            }
        }
        updatePreview();
        statusLabel.setText(IJSONBundle.message("json.tree.status.node-deleted"));
    }

    private void editNodeValue(JsonTreeNode node) {
        if (!node.isLeafType()) return;

        String current = node.getValue() != null ? node.getValue().toString() : "";
        String newValue = Messages.showInputDialog(
                IJSONBundle.message("json.tree.prompt.edit-value", node.getType()),
                IJSONBundle.message("json.tree.dialog.edit-node"),
                null,
                current,
                null
        );
        if (newValue == null) return;

        switch (node.getType()) {
            case STRING:
                node.setValue(newValue);
                break;
            case NUMBER:
                try {
                    if (newValue.contains(".")) {
                        node.setValue(Double.parseDouble(newValue));
                    } else {
                        node.setValue(Long.parseLong(newValue));
                    }
                } catch (NumberFormatException e) {
                    Messages.showErrorDialog(IJSONBundle.message("json.tree.error.invalid-number"), "IJSON");
                    return;
                }
                break;
            case BOOLEAN:
                node.setValue(Boolean.parseBoolean(newValue));
                break;
            case NULL:
                // Allow changing to another type
                break;
            default:
                break;
        }
        treeModel.nodeChanged(node);
        updatePreview();
        statusLabel.setText(IJSONBundle.message("json.tree.status.node-updated"));
    }

    private void showPopupMenu(int x, int y, TreePath path) {
        JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
        JPopupMenu menu = new JPopupMenu();

        JMenuItem expandItem = new JMenuItem(IJSONBundle.message("json.tree.menu.expand"));
        expandItem.addActionListener(e -> tree.expandPath(path));
        menu.add(expandItem);

        JMenuItem collapseItem = new JMenuItem(IJSONBundle.message("json.tree.menu.collapse"));
        collapseItem.addActionListener(e -> tree.collapsePath(path));
        menu.add(collapseItem);

        menu.addSeparator();

        if (node.isContainer() || node == rootNode) {
            JMenuItem addItem = new JMenuItem(IJSONBundle.message("json.tree.menu.add-child"));
            addItem.addActionListener(e -> {
                tree.setSelectionPath(path);
                addNode();
            });
            menu.add(addItem);
        }

        if (node.isLeafType()) {
            JMenuItem editItem = new JMenuItem(IJSONBundle.message("json.tree.menu.edit-value"));
            editItem.addActionListener(e -> editNodeValue(node));
            menu.add(editItem);
        }

        if (node != rootNode) {
            JMenuItem removeItem = new JMenuItem(IJSONBundle.message("json.tree.menu.delete-node"));
            removeItem.addActionListener(e -> {
                tree.setSelectionPath(path);
                removeNode();
            });
            menu.add(removeItem);
        }

        menu.show(tree, x, y);
    }

    private void updatePreview() {
        previewArea.setText(getJsonString());
        previewArea.setCaretPosition(0);
    }

    /**
     * Custom tree cell renderer with type-specific colors and icons.
     */
    private static class JsonTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof JsonTreeNode) {
                JsonTreeNode node = (JsonTreeNode) value;
                switch (node.getType()) {
                    case OBJECT:
                    case ROOT:
                        setIcon(AllIcons.Nodes.Property);
                        break;
                    case ARRAY:
                        setIcon(AllIcons.Nodes.Folder);
                        break;
                    case STRING:
                        setIcon(AllIcons.Nodes.Parameter);
                        break;
                    case NUMBER:
                        setIcon(AllIcons.Debugger.Db_primitive);
                        break;
                    case BOOLEAN:
                        setIcon(AllIcons.Debugger.Db_primitive);
                        break;
                    case NULL:
                        setIcon(AllIcons.General.Add);
                        break;
                }
            }
            return this;
        }
    }
}
