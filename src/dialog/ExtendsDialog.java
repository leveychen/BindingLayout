package dialog;

import javax.swing.*;
import java.awt.event.*;

public class ExtendsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox extendsBox;
    private JButton settingsButton;
    private JPanel btnEbo;
    private JCheckBox serializableBox;
    private JPanel btnSerializable;
    private boolean isCancel = false;

    public ExtendsDialog(boolean hasExtends,boolean hasImplements) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        btnEbo.setVisible(!hasExtends);
        btnSerializable.setVisible(!hasImplements);
        setTitle("Binding Layout");

        btnEbo.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                extendsBox.setSelected(!extendsBox.isSelected());
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        btnSerializable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                serializableBox.setSelected(!serializableBox.isSelected());
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });




        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        isCancel = false;
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        isCancel = true;
        dispose();
    }

    public boolean isCancel(){
        return isCancel;
    }

//    public static void main(String[] args) {
//        ExtendsDialog dialog = new ExtendsDialog();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    public boolean isExtendsChecked(){
        return extendsBox.isSelected();
    }


    public boolean isImplementsChecked(){
        return serializableBox.isSelected();
    }

}
