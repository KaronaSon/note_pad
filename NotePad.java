package database_java;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

class Note {
    int id;
    String text;

    public Note(int id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

public class NotePad extends JFrame {
    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> list = new JList<>(model);
    Connection connection;
    Statement statement;
    static ArrayList<Note> notes = new ArrayList<>();

    public NotePad() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        SimpleButton btAdd = new SimpleButton("Add note");
        SimpleButton btUpdate = new SimpleButton("Update note");
        SimpleButton btDelete = new SimpleButton("Delete Note");

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menu.add(btAdd);
        menu.add(btUpdate);
        menu.add(btDelete);

        add(menu, BorderLayout.NORTH);
        add(new JScrollPane(list));
        setVisible(true);

        customizeList();
        initNotes();

        btAdd.addActionListener(e -> addNote());

        btDelete.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index != -1) {
                deleteNote(index);
            }
        });

        btUpdate.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index != -1) {
                updateNote(index);
            }
        });
    }

    void customizeList() {
        list.setCellRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends String> list, String value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JTextArea textArea = new JTextArea(value);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(true);
                textArea.setMargin(new Insets(5, 15, 5, 5));

                if (isSelected) {
                    textArea.setBackground(list.getSelectionBackground());
                    textArea.setForeground(list.getSelectionForeground());
                } else {
                    textArea.setBackground(list.getBackground());
                    textArea.setForeground(list.getForeground());
                }
                return textArea;
            }
        });
    }

    void initNotes() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/noted",
                    "root", "");
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM note_tbl");

            while (resultSet.next()) {
                notes.add(new Note(
                        resultSet.getInt("id"),
                        resultSet.getString("text"))); // Use correct column name
            }

            for (Note note : notes) {
                model.addElement(note.toString());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    void addNote() {
        SimpleButton btSave = new SimpleButton("Add note");
        JTextArea textArea = new JTextArea();

        JDialog addDialog = new JDialog();
        addDialog.setLayout(new BorderLayout());
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        savePanel.add(btSave);

        textArea.setMargin(new Insets(5, 5, 5, 0));

        addDialog.add(savePanel, BorderLayout.NORTH);
        addDialog.add(textArea);
        addDialog.setVisible(true);
        textArea.requestFocus();
        addDialog.setSize(400, 200);
        addDialog.setLocationRelativeTo(null);

        btSave.addActionListener(e -> {
            String text = textArea.getText();
            if (!text.isEmpty()) {
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(
                            "INSERT INTO note_tbl(title, text) VALUES (current_timestamp(), ?)");
                    preparedStatement.setString(1, text);
                    preparedStatement.executeUpdate();

                    ResultSet rs = statement.executeQuery("SELECT last_insert_id()");
                    int id = 0;
                    while (rs.next()) {
                        id = rs.getInt(1);
                    }

                    notes.add(new Note(id, text));
                    model.addElement(text);
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
            addDialog.dispose();
        });
    }

    void deleteNote(int index) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM note_tbl WHERE id=?");
            preparedStatement.setInt(1, notes.get(index).id);
            preparedStatement.executeUpdate();

            notes.remove(index);
            model.removeElementAt(index);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void updateNote(int index) {
        JButton btUpdate = new JButton("update");
        String text = notes.get(index).text;
        JTextArea textArea = new JTextArea(text);

        JDialog updateDialog = new JDialog();
        updateDialog.setLayout(new BorderLayout());
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updatePanel.add(btUpdate);

        btUpdate.setOpaque(false);
        btUpdate.setContentAreaFilled(false);
        btUpdate.setBorderPainted(false);
        textArea.setMargin(new Insets(5, 5, 5, 0));

        updateDialog.add(updatePanel, BorderLayout.NORTH);
        updateDialog.add(textArea);

        updateDialog.setVisible(true);
        textArea.requestFocus();
        updateDialog.setSize(400, 200);
        updateDialog.setLocationRelativeTo(null);

        btUpdate.addActionListener(e -> {
            String newText = textArea.getText();
            if (!newText.isEmpty()) {
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(
                            "UPDATE note_tbl SET text=? WHERE id=?"); // Fixed column name
                    preparedStatement.setString(1, newText);
                    preparedStatement.setInt(2, notes.get(index).id);

                    preparedStatement.executeUpdate();
                    model.setElementAt(newText, index);
                    Note note = new Note(notes.get(index).id, newText);
                    notes.set(index, note);
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
            updateDialog.dispose();
        });
    }

    public static void main(String[] args) {
        new NotePad();
    }
}
