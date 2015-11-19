/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey.ui;

import javax.swing.JDialog;
import odyssey.LibrariesComm;
import odyssey.Mp3File;

/**
 *
 * @author jaam
 */
public class MetadataFrame extends JDialog {
    
    private Mp3File fileToUpdate;

    /**
     * Creates new form metadataFrame
     */
    public MetadataFrame() {
        initComponents();
        
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        fileToUpdate = new Mp3File();
    }
    
    public void setFileToUpdate(Mp3File pMp3File) {
        fileToUpdate = pMp3File;
    }
    
    public void runVisible() {
        this.actualNameTV.setText(fileToUpdate.getName());
        this.actualArtistTV.setText(fileToUpdate.getArtist());
        this.actualAlbumTV.setText(fileToUpdate.getAlbum());
        this.actualGenreTV.setText(fileToUpdate.getGenre());
        this.actualYearTV.setText(fileToUpdate.getAnno());
        
        this.setVisible(true);
    }
    
    private void updateMetadata() {
        
        // setting values
        if(!newNameTV.getText().equals(""))
            fileToUpdate.setName(newNameTV.getText());
        if(!newAlbumTV.getText().equals(""))
            fileToUpdate.setAlbum(newAlbumTV.getText());
        if(!newArtistTV.getText().equals(""))
            fileToUpdate.setArtist(newArtistTV.getText());
        if(!newGenreTV.getText().equals(""))
            fileToUpdate.setGenre(newGenreTV.getText());
        if(!newYearTV.getText().equals(""))
            fileToUpdate.setAnno(newYearTV.getText());
        ///
        LibrariesComm communication = new LibrariesComm();
        communication.updateMp3File(fileToUpdate);
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
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        actualYearTV = new javax.swing.JTextField();
        actualGenreTV = new javax.swing.JTextField();
        actualAlbumTV = new javax.swing.JTextField();
        actualArtistTV = new javax.swing.JTextField();
        actualNameTV = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        newNameTV = new javax.swing.JTextField();
        newArtistTV = new javax.swing.JTextField();
        newAlbumTV = new javax.swing.JTextField();
        newGenreTV = new javax.swing.JTextField();
        newYearTV = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        jPanel1.setLayout(null);

        jButton1.setBackground(java.awt.Color.green);
        jButton1.setFont(new java.awt.Font("Ubuntu", 3, 15)); // NOI18N
        jButton1.setForeground(java.awt.Color.white);
        jButton1.setText("Modify");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);
        jButton1.setBounds(450, 290, 110, 30);

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel2.setForeground(java.awt.Color.white);
        jLabel2.setText("Actual Metadata Version");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(40, 10, 230, 21);

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel3.setForeground(java.awt.Color.white);
        jLabel3.setText("New Metadata Version");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(330, 10, 210, 21);

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel4.setForeground(java.awt.Color.white);
        jLabel4.setText("Name:");
        jPanel1.add(jLabel4);
        jLabel4.setBounds(10, 60, 80, 18);

        jLabel5.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel5.setForeground(java.awt.Color.white);
        jLabel5.setText("Artist:");
        jPanel1.add(jLabel5);
        jLabel5.setBounds(10, 100, 80, 18);

        jLabel6.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel6.setForeground(java.awt.Color.white);
        jLabel6.setText("Album:");
        jPanel1.add(jLabel6);
        jLabel6.setBounds(10, 140, 80, 18);

        jLabel7.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel7.setForeground(java.awt.Color.white);
        jLabel7.setText("Genre:");
        jPanel1.add(jLabel7);
        jLabel7.setBounds(10, 180, 80, 18);

        jLabel8.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel8.setForeground(java.awt.Color.white);
        jLabel8.setText("Pub. Year:");
        jPanel1.add(jLabel8);
        jLabel8.setBounds(10, 220, 80, 18);

        actualYearTV.setEditable(false);
        jPanel1.add(actualYearTV);
        actualYearTV.setBounds(100, 220, 170, 27);

        actualGenreTV.setEditable(false);
        jPanel1.add(actualGenreTV);
        actualGenreTV.setBounds(100, 180, 170, 27);

        actualAlbumTV.setEditable(false);
        jPanel1.add(actualAlbumTV);
        actualAlbumTV.setBounds(100, 140, 170, 27);

        actualArtistTV.setEditable(false);
        jPanel1.add(actualArtistTV);
        actualArtistTV.setBounds(100, 100, 170, 27);

        actualNameTV.setEditable(false);
        jPanel1.add(actualNameTV);
        actualNameTV.setBounds(100, 60, 170, 27);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(290, 40, 10, 230);
        jPanel1.add(newNameTV);
        newNameTV.setBounds(340, 60, 170, 27);
        jPanel1.add(newArtistTV);
        newArtistTV.setBounds(340, 100, 170, 27);
        jPanel1.add(newAlbumTV);
        newAlbumTV.setBounds(340, 140, 170, 27);
        jPanel1.add(newGenreTV);
        newGenreTV.setBounds(340, 180, 170, 27);
        jPanel1.add(newYearTV);
        newYearTV.setBounds(340, 220, 170, 27);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bg2.jpg"))); // NOI18N
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 570, 330);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // actualizar metadata en la base de datos ya sea local o en cloud
        updateMetadata();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField actualAlbumTV;
    private javax.swing.JTextField actualArtistTV;
    private javax.swing.JTextField actualGenreTV;
    private javax.swing.JTextField actualNameTV;
    private javax.swing.JTextField actualYearTV;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField newAlbumTV;
    private javax.swing.JTextField newArtistTV;
    private javax.swing.JTextField newGenreTV;
    private javax.swing.JTextField newNameTV;
    private javax.swing.JTextField newYearTV;
    // End of variables declaration//GEN-END:variables
}