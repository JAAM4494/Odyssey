/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import odyssey.Constants;
import odyssey.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jaam
 */
public class LoginFrame extends javax.swing.JFrame {

    /**
     * Creates new form loginFrame
     */
    public LoginFrame() {
        initComponents();

        this.setResizable(false);
        this.setLocationRelativeTo(null);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        signInBtn = new javax.swing.JButton();
        userEntry = new javax.swing.JTextField();
        passEntry = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(null);

        signInBtn.setBackground(java.awt.Color.blue);
        signInBtn.setFont(new java.awt.Font("Ubuntu", 3, 15)); // NOI18N
        signInBtn.setForeground(java.awt.Color.white);
        signInBtn.setText("Sign in");
        signInBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signInBtnMouseClicked(evt);
            }
        });
        jPanel1.add(signInBtn);
        signInBtn.setBounds(262, 290, 110, 30);
        jPanel1.add(userEntry);
        userEntry.setBounds(70, 200, 260, 27);
        jPanel1.add(passEntry);
        passEntry.setBounds(70, 255, 260, 27);

        jLabel2.setFont(new java.awt.Font("Ubuntu", 3, 15)); // NOI18N
        jLabel2.setForeground(java.awt.Color.white);
        jLabel2.setText("Username");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(70, 180, 100, 18);

        jLabel3.setFont(new java.awt.Font("Ubuntu", 3, 15)); // NOI18N
        jLabel3.setForeground(java.awt.Color.white);
        jLabel3.setText("Password");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(70, 235, 90, 17);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png"))); // NOI18N
        jPanel1.add(jLabel4);
        jLabel4.setBounds(15, 20, 360, 130);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bg0.jpg"))); // NOI18N
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 390, 330);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void signInBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInBtnMouseClicked
        ArrayList valores = new ArrayList();
        ArrayList llaves = new ArrayList();
        valores.add(userEntry.getText());
        valores.add(new String(passEntry.getPassword()));
        llaves.add("user");
        llaves.add("password");
        HttpRequest request = new HttpRequest();
        String response = request.postRequest(Constants.loginUrl, valores, llaves);

        if (response.equals("-1")) {
            JOptionPane.showMessageDialog(this, "Service Unavaible", "Bad request", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println(response);
            try {
                JSONObject jsonInput = new JSONObject(response);
                String returnCode = jsonInput.getString("result");

                if (returnCode.equals("0")) {
                    JOptionPane.showMessageDialog(this, "Wrong password/user exist", "Bad error", JOptionPane.ERROR_MESSAGE);
                } else {
                    this.dispose();
                    Constants.userName = userEntry.getText();
                    OdysseyFrame ventanaOdyssey = new OdysseyFrame();
                    ventanaOdyssey.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                            ArrayList tagNames = new ArrayList();
                            ArrayList values = new ArrayList();
                            tagNames.add("user");
                            values.add(Constants.userName);

                            HttpRequest request = new HttpRequest();
                            String rr = request.postRequest(Constants.logoutUrl, values, tagNames);

                            System.exit(0);
                        }
                    });
                    ventanaOdyssey.setVisible(true);
                }
            } catch (JSONException ex) {
                Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_signInBtnMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField passEntry;
    private javax.swing.JButton signInBtn;
    private javax.swing.JTextField userEntry;
    // End of variables declaration//GEN-END:variables
}
