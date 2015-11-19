/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey.ui;

import com.google.common.io.ByteStreams;
import com.sun.javafx.font.freetype.HBGlyphLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import odyssey.Constants;
import odyssey.CustomPlayer;
import odyssey.HttpRequest;
import odyssey.LibrariesComm;
import odyssey.Metodos;
import odyssey.Mp3File;
import odyssey.LocalSyncThread;
import odyssey.UserDetails;

/**
 *
 * @author jaam
 */
public class OdysseyFrame extends JFrame implements BasicPlayerListener, ActionListener,Observer, Runnable {

    private boolean PararBarra = false, Aleatorio = false, EnSilencio = false, Mostrar = false;
    public static boolean CambioEnEcualizador = false;
    private CustomPlayer myPlayer;
    private ArrayList mp3FilesArray;
    private ArrayList usersThatShareMeArray;
    private float[] ecualizador;
    private Timer myTimer;
    private TimerTask myTask;
    private String selectedFile;
    private PopupMenu popupMiniature;
    private Metodos metodos = new Metodos();
    
    private LocalSyncThread HiloSincronizacion;

    private int ItemActual, TamanoEnBytes, PrimeroDeAleatorio, Repetir = 0;

    /**
     * Creates new form OdysseyFrame
     */
    public OdysseyFrame() {
        initComponents();

        this.setLocationRelativeTo(null);
        this.setResizable(false);

        selectedFile = "-1";

        myPlayer = new CustomPlayer();
        mp3FilesArray = new ArrayList();
        usersThatShareMeArray = new ArrayList();
        myTimer = null;
        popupMiniature = new PopupMenu();
        HiloSincronizacion = new LocalSyncThread("MainSyncThread");

        progresSlider.setEnabled(false);

        myPlayer.player.addBasicPlayerListener(this);
        abrirInfo();
        
        initLibraries();
        HiloSincronizacion.start();
        HiloSincronizacion.addObserver(this);
    }
    
    private void initLibraries() {
        LibrariesComm communication = new LibrariesComm();
        usersThatShareMeArray = communication.getSharedLibs();

        libListModel.clear();
        libListModel.addElement("MyOdyssey-Lib");

        for (int i = 0; i < usersThatShareMeArray.size(); i++) {
            libListModel.addElement("SharedLib-" + (i + 1) + "(" + ((UserDetails) usersThatShareMeArray.get(i)).getUserName() + ")");
        }
    }

    private void fillMusicTable() {
        DefaultTableModel model = (DefaultTableModel) filesTable.getModel();
        //DefaultTableModel model = new DefaultTableModel();

        int number = 1;
        for (int i = 0; i < mp3FilesArray.size(); i++) {
            Mp3File temp = (Mp3File) mp3FilesArray.get(i);
            model.setValueAt(number, i, 0);
            model.setValueAt(temp.getName(), i, 1);
            model.setValueAt(temp.getArtist(), i, 2);
            model.setValueAt(temp.getAlbum(), i, 3);
            model.setValueAt(temp.getGenre(), i, 4);
            model.setValueAt(temp.getAnno(), i, 5);
            number++;
        }
    }

    private void clearFilesTable() {
        DefaultTableModel model = (DefaultTableModel) filesTable.getModel();

        for (int i = 0; i < mp3FilesArray.size(); i++) {
            model.setValueAt("", i, 0);
            model.setValueAt("", i, 1);
            model.setValueAt("", i, 2);
            model.setValueAt("", i, 3);
            model.setValueAt("", i, 4);
            model.setValueAt("", i, 5);
        }
    }

    private void loadFiles() {
        JFileChooser loadFileChooser = new JFileChooser();
        loadFileChooser.setMultiSelectionEnabled(true);
        //loadFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".mp3", "mp3"));
        loadFileChooser.setFileFilter(new FileNameExtensionFilter(".mp3", "mp3"));
        loadFileChooser.setApproveButtonText("Importar");
        loadFileChooser.setDialogTitle("Import Mp3 Files");
        int returnVal = loadFileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = loadFileChooser.getSelectedFiles();

            ArrayList filesPath = new ArrayList();
            ArrayList filesName = new ArrayList();
            for (int i = 0; i < selectedFiles.length; i++) {
                //System.out.println(selectedFiles[i].getAbsolutePath());
                filesPath.add(selectedFiles[i].getAbsolutePath());
                //System.out.println(selectedFiles[i].getName().split(".mp3")[0]);
                filesName.add(selectedFiles[i].getName().split(".mp3")[0]);
            }

            LibrariesComm comunication = new LibrariesComm();
            comunication.addMp3Blobs(filesPath, filesName);
        }
    }

    private void changeMetadataFile() {
        MetadataFrame ventanaMeta = new MetadataFrame();
        if (!mp3FilesArray.isEmpty()) {
            ventanaMeta.setFileToUpdate((Mp3File) mp3FilesArray.get(Integer.parseInt(selectedFile) - 1));
        }
        ventanaMeta.setModal(true);
        ventanaMeta.runVisible();
    }

    private void syncWithCloud() {
        LibrariesComm comunication = new LibrariesComm();
        comunication.syncMp3FilesWithCloud();
    }

    private void abrirInfo() {
        ObjectInputStream Leer = null;
        try {
            if (new File(Metodos.Ruta() + "/Ecualizador.dat").exists()) {
                Leer = new ObjectInputStream(new FileInputStream(Metodos.Ruta() + "/Ecualizador.dat"));
                CustomPlayer.Eq[0] = Leer.readFloat();
                CustomPlayer.Eq[1] = Leer.readFloat();
                CustomPlayer.Eq[2] = Leer.readFloat();
                CustomPlayer.Eq[3] = Leer.readFloat();
                CustomPlayer.Eq[4] = Leer.readFloat();
                CustomPlayer.Eq[5] = Leer.readFloat();
                CustomPlayer.Eq[6] = Leer.readFloat();
                CustomPlayer.Eq[7] = Leer.readFloat();
                CustomPlayer.Eq[8] = Leer.readFloat();
                CustomPlayer.Eq[9] = Leer.readFloat();
                CustomPlayer.EqP[0] = Leer.readInt();
                CustomPlayer.EqP[1] = Leer.readInt();
                CustomPlayer.EqP[2] = Leer.readInt();
                CustomPlayer.EqP[3] = Leer.readInt();
                CustomPlayer.EqP[4] = Leer.readInt();
                CustomPlayer.EqP[5] = Leer.readInt();
                CustomPlayer.EqP[6] = Leer.readInt();
                CustomPlayer.EqP[7] = Leer.readInt();
                CustomPlayer.EqP[8] = Leer.readInt();
                CustomPlayer.EqP[9] = Leer.readInt();
                CustomPlayer.Balance = Leer.readFloat();
                CustomPlayer.ItemEc = Leer.readInt();
                Leer.close();
            }
            if (new File(Metodos.Ruta() + "/Volumen.dat").exists()) {
                Leer = new ObjectInputStream(new FileInputStream(Metodos.Ruta() + "/Volumen.dat"));
                myPlayer.Volume = Leer.readDouble();
                volumeSlider.setValue((int) (myPlayer.Volume * volumeSlider.getMaximum()));
                Leer.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Leer = null;
        }
    }

    private void corregirRuidos() {
        //Este metodo siempre debe llamarse de ultimo, sino causara efectos no deseados
        try {
            myPlayer.player.wait(10);
        } catch (InterruptedException ex) {
            //si hay error;
        }
    }

    private void mostrarMsjError() {
        pararGiro();
        myTimer = null;
        progressLbl.setText("00:00/00:00");
        animationLbl1.setText("No pudimos abrir esta cancion :(");
        animationLbl2.setText("No pudimos abrir esta cancion :(");
    }

    public void pasarGarbageCollector() {
        Runtime garbage = Runtime.getRuntime();
        garbage.gc();
    }

    private void hacerGirar(int TiempoInicial, int Frecuencia) {
        myTask = new TimerTask() {
            @Override
            public void run() {
                girarTextoInfo();
            }
        };
        myTimer = new Timer();
        myTimer.schedule(myTask, TiempoInicial, Frecuencia);
    }

    private void pararGiro() {
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
            myTask = null;
            animationLbl1.setBounds(10, 480, 200, 20);
            animationLbl2.setBounds(200, 480, 200, 20);
            ubicacion = 10;
        }
        pasarGarbageCollector();//limpiar memoria
    }

    int ubicacion = 10;

    private void girarTextoInfo() {
        animationLbl1.setBounds(ubicacion, 480, 200, 20);
        animationLbl2.setBounds(ubicacion + 200, 480, 200, 20);
        ubicacion--;
        if (ubicacion == -200) {
            ubicacion = 10;
        }
    }

    private void silenciarVolumen() {
        if (myPlayer.getStatus() != -1 && myPlayer.getStatus() != 3) {
            if (!myPlayer.changeVolume(0)) {
                JOptionPane.showMessageDialog(this, "Ups, algo salio mal", "Aviso", 0);
            }
        } else {
            myPlayer.Volume = 0;
        }
    }

    private void volumenActual() {
        if (myPlayer.getStatus() != -1 && myPlayer.getStatus() != 3) {
            if (!myPlayer.changeVolume((double) volumeSlider.getValue() / (double) volumeSlider.getMaximum())) {
                JOptionPane.showMessageDialog(this, "Ups, algo salio mal", "Aviso", 0);
            }
        } else {
            myPlayer.Volume = (double) volumeSlider.getValue() / (double) volumeSlider.getMaximum();
        }
    }

    private void cambiarVolumen() {
        if (!EnSilencio) {
            volumeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/volume2.jpg")));
            EnSilencio = true;
            silenciarVolumen();
        } else {
            volumeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/volume1.jpg")));
            EnSilencio = false;
            volumenActual();
        }
    }

    private void seleccionarCancionActual() {
        filesTable.setRowSelectionInterval(ItemActual, ItemActual);
    }

    /*
     private boolean pasarCancion(boolean Adelante) {
     boolean Abre = false;
     if (mp3FilesArray.size() > 0) {
     if (myPlayer.getStatus() != 0) {
     Abre = myPlayer.changeSongBy(Canciones.get(ItemActual), false);
     } else {
     Abre = myPlayer.changeSongBy(Canciones.get(ItemActual), true);
     }
     //seleccionarCancionActual();
     }
     return Abre;
     }*/
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        changeMetaPopUp = new javax.swing.JMenuItem();
        commentPopUp = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        omniFinderEntry = new javax.swing.JTextField();
        libsLbl = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        filesTable = new javax.swing.JTable();
        findBtn = new javax.swing.JButton();
        gestureBtn = new javax.swing.JButton();
        likeBtn = new javax.swing.JButton();
        dislikeBtn = new javax.swing.JButton();
        progresSlider = new javax.swing.JSlider();
        progressLbl = new javax.swing.JLabel();
        animationLbl1 = new javax.swing.JLabel();
        animationLbl2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        previousBtn = new javax.swing.JButton();
        playBtn = new javax.swing.JButton();
        pauseBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        volumeBtn = new javax.swing.JButton();
        nextBtn = new javax.swing.JButton();
        volumeSlider = new javax.swing.JSlider();
        eqBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        libListModel = new DefaultListModel();
        musicLibList = new javax.swing.JList();
        auxBtn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        friendsList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        wallLbl = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        addMp3Menu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        closeOdyMenu = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        changeMetaMenu = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        setupLiGesMenu = new javax.swing.JMenuItem();
        setupDisGesMenu = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        syncCloudMenu = new javax.swing.JMenuItem();
        downLibMenu = new javax.swing.JMenuItem();
        getMetaMenu = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        shareLibMenu = new javax.swing.JMenuItem();
        addPeopleMenu = new javax.swing.JMenuItem();
        recommSongsMenu = new javax.swing.JMenuItem();
        recommPeopleMenu = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        contentsMenu = new javax.swing.JMenuItem();
        gestureSupMenu = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        aboutOdyMenu = new javax.swing.JMenuItem();

        changeMetaPopUp.setText("Change Metadata");
        changeMetaPopUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeMetaPopUpActionPerformed(evt);
            }
        });
        popupMenu.add(changeMetaPopUp);

        commentPopUp.setText("Comment Song");
        popupMenu.add(commentPopUp);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setLayout(null);

        omniFinderEntry.setToolTipText("Omnifinder");
        jPanel1.add(omniFinderEntry);
        omniFinderEntry.setBounds(10, 20, 200, 27);

        libsLbl.setFont(new java.awt.Font("Ubuntu", 3, 18)); // NOI18N
        libsLbl.setForeground(java.awt.Color.white);
        libsLbl.setText("Music Libraries");
        jPanel1.add(libsLbl);
        libsLbl.setBounds(10, 65, 160, 21);

        filesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "#", "Name", "Artist", "Album", "Genre", "Pub. Year"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        filesTable.getTableHeader().setReorderingAllowed(false);
        filesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                filesTableMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filesTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(filesTable);
        if (filesTable.getColumnModel().getColumnCount() > 0) {
            filesTable.getColumnModel().getColumn(0).setResizable(false);
            filesTable.getColumnModel().getColumn(0).setPreferredWidth(8);
            filesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            filesTable.getColumnModel().getColumn(2).setPreferredWidth(70);
            filesTable.getColumnModel().getColumn(3).setPreferredWidth(70);
            filesTable.getColumnModel().getColumn(4).setPreferredWidth(70);
            filesTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        }

        jPanel1.add(jScrollPane2);
        jScrollPane2.setBounds(220, 60, 640, 540);

        findBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/search.png"))); // NOI18N
        findBtn.setToolTipText("Find");
        jPanel1.add(findBtn);
        findBtn.setBounds(220, 20, 30, 30);

        gestureBtn.setBackground(java.awt.Color.green);
        gestureBtn.setFont(new java.awt.Font("Ubuntu", 3, 18)); // NOI18N
        gestureBtn.setForeground(java.awt.Color.white);
        gestureBtn.setText("Gesture");
        gestureBtn.setToolTipText("Gesture Recognition");
        gestureBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gestureBtnMouseClicked(evt);
            }
        });
        jPanel1.add(gestureBtn);
        gestureBtn.setBounds(610, 20, 120, 29);

        likeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/like.png"))); // NOI18N
        likeBtn.setToolTipText("Like");
        jPanel1.add(likeBtn);
        likeBtn.setBounds(740, 20, 30, 30);

        dislikeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dislike.png"))); // NOI18N
        dislikeBtn.setToolTipText("DIslike");
        jPanel1.add(dislikeBtn);
        dislikeBtn.setBounds(780, 20, 80, 30);

        progresSlider.setBackground(java.awt.Color.black);
        progresSlider.setToolTipText("Progress");
        progresSlider.setValue(0);
        progresSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                progresSliderStateChanged(evt);
            }
        });
        progresSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                progresSliderMouseClicked(evt);
            }
        });
        jPanel1.add(progresSlider);
        progresSlider.setBounds(10, 620, 850, 20);

        progressLbl.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        progressLbl.setForeground(new java.awt.Color(51, 153, 255));
        progressLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        progressLbl.setText("00:00/00:00");
        jPanel1.add(progressLbl);
        progressLbl.setBounds(450, 600, 120, 20);

        animationLbl1.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        animationLbl1.setForeground(new java.awt.Color(51, 153, 255));
        animationLbl1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        animationLbl1.setText("Odyssey Song");
        animationLbl1.setDoubleBuffered(true);
        jPanel1.add(animationLbl1);
        animationLbl1.setBounds(10, 480, 200, 20);

        animationLbl2.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        animationLbl2.setForeground(new java.awt.Color(51, 153, 255));
        animationLbl2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        animationLbl2.setText("Odyssey Song");
        animationLbl2.setDoubleBuffered(true);
        jPanel1.add(animationLbl2);
        animationLbl2.setBounds(10, 480, 200, 20);

        jPanel2.setBackground(java.awt.Color.black);

        previousBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/prev1.jpg"))); // NOI18N
        previousBtn.setToolTipText("Previous");

        playBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play1.jpg"))); // NOI18N
        playBtn.setToolTipText("Play");
        playBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playBtnMouseClicked(evt);
            }
        });
        playBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playBtnActionPerformed(evt);
            }
        });

        pauseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pause1.jpg"))); // NOI18N
        pauseBtn.setToolTipText("Pause");
        pauseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseBtnActionPerformed(evt);
            }
        });

        stopBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop1.jpg"))); // NOI18N
        stopBtn.setToolTipText("Stop");
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        volumeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/volume1.jpg"))); // NOI18N
        volumeBtn.setToolTipText("Mute");
        volumeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volumeBtnActionPerformed(evt);
            }
        });

        nextBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/next1.jpg"))); // NOI18N
        nextBtn.setToolTipText("Next");

        volumeSlider.setBackground(java.awt.Color.black);
        volumeSlider.setToolTipText("Volume");
        volumeSlider.setValue(100);
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        eqBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/eq1.jpg"))); // NOI18N
        eqBtn.setToolTipText("Equalizer");
        eqBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eqBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(volumeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(eqBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(volumeSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(previousBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(playBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pauseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(24, 24, 24))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previousBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pauseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nextBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(volumeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eqBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2);
        jPanel2.setBounds(10, 510, 200, 110);

        musicLibList.setModel(libListModel
        );
        musicLibList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                musicLibListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(musicLibList);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(10, 90, 200, 380);

        auxBtn.setText("Auxiliar");
        auxBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auxBtnActionPerformed(evt);
            }
        });
        jPanel1.add(auxBtn);
        auxBtn.setBounds(390, 20, 120, 29);

        friendsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(friendsList);

        jPanel1.add(jScrollPane3);
        jScrollPane3.setBounds(870, 530, 220, 110);

        jLabel1.setFont(new java.awt.Font("Ubuntu", 3, 18)); // NOI18N
        jLabel1.setForeground(java.awt.Color.white);
        jLabel1.setText("Friends");
        jPanel1.add(jLabel1);
        jLabel1.setBounds(870, 510, 80, 20);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane4.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane4);
        jScrollPane4.setBounds(870, 170, 220, 320);

        jLabel2.setFont(new java.awt.Font("Ubuntu", 3, 18)); // NOI18N
        jLabel2.setForeground(java.awt.Color.white);
        jLabel2.setText("Song Letter");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(870, 150, 110, 17);

        wallLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bg1.jpg"))); // NOI18N
        jPanel1.add(wallLbl);
        wallLbl.setBounds(0, 0, 1100, 650);

        jMenu1.setText("File");

        addMp3Menu.setText("Add MP3 Files");
        addMp3Menu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addMp3MenuMousePressed(evt);
            }
        });
        jMenu1.add(addMp3Menu);
        jMenu1.add(jSeparator1);

        closeOdyMenu.setText("Close Odyssey");
        closeOdyMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeOdyMenuMousePressed(evt);
            }
        });
        jMenu1.add(closeOdyMenu);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        changeMetaMenu.setText("Change MP3 Metadata");
        changeMetaMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeMetaMenuActionPerformed(evt);
            }
        });
        jMenu2.add(changeMetaMenu);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Tools");

        setupLiGesMenu.setText("Setup Like Gesture");
        setupLiGesMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                setupLiGesMenuMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setupLiGesMenuMouseClicked(evt);
            }
        });
        jMenu3.add(setupLiGesMenu);

        setupDisGesMenu.setText("Setup Dislike Gesture");
        setupDisGesMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                setupDisGesMenuMousePressed(evt);
            }
        });
        jMenu3.add(setupDisGesMenu);
        jMenu3.add(jSeparator2);

        syncCloudMenu.setText("Sync with Odyssey-Cloud");
        syncCloudMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                syncCloudMenuMousePressed(evt);
            }
        });
        jMenu3.add(syncCloudMenu);

        downLibMenu.setText("Download MyOdyssey-Lib");
        jMenu3.add(downLibMenu);

        getMetaMenu.setText("Sync with external metadata API");
        jMenu3.add(getMetaMenu);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Social");

        shareLibMenu.setText("Share MyOdyssey-Lib");
        shareLibMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shareLibMenuActionPerformed(evt);
            }
        });
        jMenu5.add(shareLibMenu);

        addPeopleMenu.setText("Add People");
        jMenu5.add(addPeopleMenu);

        recommSongsMenu.setText("Recommended Songs");
        jMenu5.add(recommSongsMenu);

        recommPeopleMenu.setText("Recommended People");
        jMenu5.add(recommPeopleMenu);

        jMenuBar1.add(jMenu5);

        jMenu4.setText("Help");

        contentsMenu.setText("Contents");
        jMenu4.add(contentsMenu);

        gestureSupMenu.setText("Gesture Support");
        jMenu4.add(gestureSupMenu);
        jMenu4.add(jSeparator3);

        aboutOdyMenu.setText("About Odyssey");
        jMenu4.add(aboutOdyMenu);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void gestureBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gestureBtnMouseClicked
        Process recognitionService;
        try {
            recognitionService = Runtime.getRuntime().exec("./gestureRecognitionService 0");
            recognitionService.waitFor();
            System.out.println(recognitionService.exitValue());
        } catch (IOException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_gestureBtnMouseClicked

    private void setupLiGesMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setupLiGesMenuMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_setupLiGesMenuMouseClicked

    private void setupLiGesMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setupLiGesMenuMousePressed
        Process recognitionService;
        try {
            recognitionService = Runtime.getRuntime().exec("./gestureRecognitionService 1");
            recognitionService.waitFor();
            System.out.println(recognitionService.exitValue());
        } catch (IOException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_setupLiGesMenuMousePressed

    private void setupDisGesMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setupDisGesMenuMousePressed
        Process recognitionService;
        try {
            recognitionService = Runtime.getRuntime().exec("./gestureRecognitionService 2");
            recognitionService.waitFor();
            System.out.println(recognitionService.exitValue());
        } catch (IOException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_setupDisGesMenuMousePressed

    private void closeOdyMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeOdyMenuMousePressed
        this.dispose();
        System.exit(0);
    }//GEN-LAST:event_closeOdyMenuMousePressed

    private void addMp3MenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addMp3MenuMousePressed
        loadFiles();
    }//GEN-LAST:event_addMp3MenuMousePressed

    private void syncCloudMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_syncCloudMenuMousePressed
        syncWithCloud();
    }//GEN-LAST:event_syncCloudMenuMousePressed

    private void playBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playBtnMouseClicked

        /*
         try {
            
         //////////////////////
         LocalLibComm communication = new LocalLibComm();
            
         byte[] bytes = communication.getMp3ToPlay("G1uMBHGonmp6.128");
            
         InputStream nn = new ByteArrayInputStream(bytes);
            
         BufferedInputStream bis = new BufferedInputStream(nn);
            
         Player player = new Player(bis);
         player.play();
            
         System.out.println(Integer.toString(player.getPosition()));
            
         /////////////////////////

         //String mp3Url = "http://ia600402.us.archive.org/6/items/Stockfinster.-DeadLinesutemos025/01_Push_Push.mp3";
            
         String mp3Url = "http://odyssey.mybluemix.net/song/G1uMBHGonmp6.128/";
         URL url = new URL(mp3Url);
         URLConnection conn = url.openConnection();
         InputStream is = conn.getInputStream();
         BufferedInputStream bis = new BufferedInputStream(is);

         Player player = new Player(bis);
         //player.open(bis);
         player.play();
            
      

         } catch (JavaLayerException ex) {
         Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
         } catch (MalformedURLException ex) {
         Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
         Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }//GEN-LAST:event_playBtnMouseClicked

    private void playBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playBtnActionPerformed
        if (mp3FilesArray.size() > 0) {

            /*progressLbl.setText("00:00/" + metodos.FormatoReloj(myPlayer.Duration));
             if (myPlayer.SongName == null || myPlayer.SongName.trim().isEmpty()) {
             animationLbl1.setText("Unknow - Unknow");
             animationLbl2.setText("Unknow - Unknow");
             //jLabel3.setText(myPlayer.NombreArchivo());
             //jLabel4.setText(myPlayer.NombreArchivo());
             } else {
             animationLbl1.setText(myPlayer.SongName + " - " + myPlayer.Artist);
             animationLbl2.setText(myPlayer.SongName + " - " + myPlayer.Artist);
             }
             pararGiro();
             hacerGirar(1500, 15);
             Mostrar = true;*/
            myPlayer.play();
        }
    }//GEN-LAST:event_playBtnActionPerformed

    private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
        if (EnSilencio) {
            cambiarVolumen();
        } else {
            volumenActual();
        }
        Metodos.CrearCarpeta();
        ObjectOutputStream Escribir = null;
        try {
            Escribir = new ObjectOutputStream(new FileOutputStream(Metodos.Ruta() + "/Volumen.dat"));
            Escribir.writeDouble(myPlayer.Volume);
            Escribir.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EqFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EqFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Escribir = null;
        }
    }//GEN-LAST:event_volumeSliderStateChanged

    private void volumeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volumeBtnActionPerformed
        cambiarVolumen();
    }//GEN-LAST:event_volumeBtnActionPerformed

    private void pauseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseBtnActionPerformed
        //if(Canciones.size()>0){
        myPlayer.pause();
        //}
    }//GEN-LAST:event_pauseBtnActionPerformed

    private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
        if (mp3FilesArray.size() > 0) {
            myPlayer.stop();
            progressLbl.setText("00:00/" + metodos.FormatoReloj(myPlayer.Duration));
            progresSlider.setValue(1);
        }
    }//GEN-LAST:event_stopBtnActionPerformed

    private void progresSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_progresSliderStateChanged
        /*if (progresSlider.getMaximum() == progresSlider.getValue()) {
         PararBarra = true;
         progresSlider.setValue(1); // esto evita que pase dos canciones a la vez

         while (!pasarCancion(true)) {
         mostrarMsjError();
         try {
         Thread.sleep(3000);
         } catch (InterruptedException ex) {
         Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
         if (myPlayer.getStatus() != 0) {
         myPlayer.play(); //en caso de q no reprodusca automaticamente
         }

         PararBarra = false;
         corregirRuidos(); //siempre debe llamarse de ultimo
         }*/
    }//GEN-LAST:event_progresSliderStateChanged

    private void progresSliderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_progresSliderMouseClicked
        //if(Canciones.size()>0) {

        int Clic = progresSlider.getMousePosition().x;
        int LargoDeBarra = progresSlider.getSize().width;
        int finalx = (progresSlider.getMaximum() * Clic) / LargoDeBarra;
        myPlayer.setLocation(finalx * 1024);
        Mostrar = false;
        if (myPlayer.getStatus() != 1) {
            progresSlider.setValue(finalx);
            long LlevaEnSegundos = (myPlayer.Duration * progresSlider.getValue()) / TamanoEnBytes;
            progressLbl.setText(metodos.FormatoReloj(LlevaEnSegundos) + "/" + metodos.FormatoReloj(myPlayer.Duration));
        }
         //}


    }//GEN-LAST:event_progresSliderMouseClicked

    private void eqBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eqBtnActionPerformed
        new EqFrame(this).setVisible(true);
    }//GEN-LAST:event_eqBtnActionPerformed

    private void musicLibListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_musicLibListMouseClicked

        LibrariesComm communication = new LibrariesComm();

        if (evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1) {
            Constants.selectedLib = (String) musicLibList.getSelectedValue();
            clearFilesTable();
            if (Constants.selectedLib.equals("MyOdyssey-Lib")) {
                mp3FilesArray = communication.getLibraryContent(0, "LocalUser");
            } else {
                int selectedIndex = musicLibList.getSelectedIndex();
                //System.out.println(musicLibList.getSelectedIndex());
                mp3FilesArray = communication.getLibraryContent(1, ((UserDetails) usersThatShareMeArray.get(selectedIndex - 1)).getUserName());
            }
            fillMusicTable();
        }
    }//GEN-LAST:event_musicLibListMouseClicked

    private void filesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filesTableMouseClicked
        if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON1) {
            if (filesTable.getValueAt(filesTable.getSelectedRow(), 0) != null) {
                String songID = Integer.toString((int) filesTable.getValueAt(filesTable.getSelectedRow(), 0));
                if (!songID.equals(selectedFile)) {
                    selectedFile = songID;

                    if (Constants.selectedLib.equals("MyOdyssey-Lib")) {
                        LibrariesComm communication = new LibrariesComm();
                        //System.out.println("ID:" + Integer.parseInt(songID));
                        byte[] mp3ByteArray = communication.getMp3ToPlay(Integer.parseInt(songID));
                        InputStream mp3Stream = new ByteArrayInputStream(mp3ByteArray);

                        if (myPlayer.getStatus() != 0) {
                            myPlayer.changeSongBy(mp3Stream, (Mp3File) mp3FilesArray.get(Integer.parseInt(songID) - 1), false);
                        } else {
                            myPlayer.changeSongBy(mp3Stream, (Mp3File) mp3FilesArray.get(Integer.parseInt(songID) - 1), true);
                        }
                    } else {
                        URL url;
                        try {
                            String tmpURL = Constants.streamingUrl + 
                                    ( (UserDetails)usersThatShareMeArray.get(musicLibList.getSelectedIndex()) ).getUserID() + 
                                    "/" + selectedFile;
                            url = new URL(tmpURL);
                            
                            URLConnection conn = url.openConnection();
                            InputStream is = conn.getInputStream();
                            BufferedInputStream bis = new BufferedInputStream(is);
                            //myPlayer.open2000(url);
                            if (myPlayer.getStatus() != 0) {
                                myPlayer.changeSongBy(bis, (Mp3File) mp3FilesArray.get(0), false);
                            }

                        } catch (MalformedURLException ex) {
                            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(OdysseyFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } else {
                selectedFile = "-1";
                //System.out.println(selectedFile);
            }
        }
    }//GEN-LAST:event_filesTableMouseClicked

    private void auxBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auxBtnActionPerformed
        //myPlayer.Abrir("/home/jaam/Música/Musica/Reggae/RedemptionSong.mp3");
    }//GEN-LAST:event_auxBtnActionPerformed

    private void changeMetaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeMetaMenuActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_changeMetaMenuActionPerformed

    private void filesTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filesTableMouseReleased
        if (evt.getButton() == MouseEvent.BUTTON3) {
            //PosicionMouse=evt.getY()/16;
            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_filesTableMouseReleased

    private void changeMetaPopUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeMetaPopUpActionPerformed
        changeMetadataFile();
    }//GEN-LAST:event_changeMetaPopUpActionPerformed

    private void shareLibMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shareLibMenuActionPerformed
        // TODO add your handling code here:
        String tmpUser = "-1";
        tmpUser = JOptionPane.showInputDialog(this, "Type userName that you want to share your Odyssey-Lib");

        if (tmpUser != null) {
            LibrariesComm communication = new LibrariesComm();
            communication.setShareLib(tmpUser);
        }
    }//GEN-LAST:event_shareLibMenuActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutOdyMenu;
    private javax.swing.JMenuItem addMp3Menu;
    private javax.swing.JMenuItem addPeopleMenu;
    private javax.swing.JLabel animationLbl1;
    private javax.swing.JLabel animationLbl2;
    private javax.swing.JButton auxBtn;
    private javax.swing.JMenuItem changeMetaMenu;
    private javax.swing.JMenuItem changeMetaPopUp;
    private javax.swing.JMenuItem closeOdyMenu;
    private javax.swing.JMenuItem commentPopUp;
    private javax.swing.JMenuItem contentsMenu;
    private javax.swing.JButton dislikeBtn;
    private javax.swing.JMenuItem downLibMenu;
    private javax.swing.JButton eqBtn;
    private javax.swing.JTable filesTable;
    private javax.swing.JButton findBtn;
    private javax.swing.JList friendsList;
    private javax.swing.JButton gestureBtn;
    private javax.swing.JMenuItem gestureSupMenu;
    private javax.swing.JMenuItem getMetaMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel libsLbl;
    private javax.swing.JButton likeBtn;
    private javax.swing.JList musicLibList;
    private DefaultListModel libListModel;
    private javax.swing.JButton nextBtn;
    private javax.swing.JTextField omniFinderEntry;
    private javax.swing.JButton pauseBtn;
    private javax.swing.JButton playBtn;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JButton previousBtn;
    private javax.swing.JSlider progresSlider;
    private javax.swing.JLabel progressLbl;
    private javax.swing.JMenuItem recommPeopleMenu;
    private javax.swing.JMenuItem recommSongsMenu;
    private javax.swing.JMenuItem setupDisGesMenu;
    private javax.swing.JMenuItem setupLiGesMenu;
    private javax.swing.JMenuItem shareLibMenu;
    private javax.swing.JButton stopBtn;
    private javax.swing.JMenuItem syncCloudMenu;
    private javax.swing.JButton volumeBtn;
    private javax.swing.JSlider volumeSlider;
    private javax.swing.JLabel wallLbl;
    // End of variables declaration//GEN-END:variables

    @Override
    public void opened(Object o, Map map) {
        System.out.println("RRRRR");
        if (map.containsKey("audio.length.bytes")) {
            TamanoEnBytes = (int) (Double.parseDouble(map.get("audio.length.bytes").toString()) / 1024);
        } else {
            TamanoEnBytes = (int) (myPlayer.lengthInBytes / 1024);
        }
        System.out.println(Integer.toString(TamanoEnBytes));

        progresSlider.setMaximum(TamanoEnBytes);
        progresSlider.setValue(1);
        CambioEnEcualizador = true;

        progressLbl.setText("00:00/" + metodos.FormatoReloj(myPlayer.Duration));
        if (myPlayer.SongName == null || myPlayer.SongName.trim().isEmpty()) {
            animationLbl1.setText("Unknow - Unknow");
            animationLbl2.setText("Unknow - Unknow");
            //jLabel3.setText(myPlayer.NombreArchivo());
            //jLabel4.setText(myPlayer.NombreArchivo());
        } else {
            animationLbl1.setText(myPlayer.SongName + " - " + myPlayer.Artist);
            animationLbl2.setText(myPlayer.SongName + " - " + myPlayer.Artist);
        }
        pararGiro();
        hacerGirar(1500, 15);
        Mostrar = true;

    }

    PopUpFrame MostrarLaMusica = null;

    @Override
    public void progress(int i, long l, byte[] bytes, Map map) {
        System.out.println("playing..");
        System.out.println(map.toString());
        if (!PararBarra) {
            System.out.println("Entrando...." + Integer.toString(i));
            float progressUpdate = (float) (i * 1.0f / TamanoEnBytes * 1.0f);
            int progressNow = (int) (TamanoEnBytes * progressUpdate) / 1024;
            System.out.println("ProgressNow: " + Integer.toString(progressNow));

            long pp = (long) map.get("mp3.position.microseconds");
            long ppaux = (pp / 1000000);
            float factor = ((float) progresSlider.getMaximum()) / myPlayer.Duration;
            int rpp = (int) (ppaux * factor);
            System.out.printf("ProgressNow22: %d\n", rpp);
            //System.out.println("ProgressNow22: " + factor);

            progresSlider.setValue(rpp);
            if (CambioEnEcualizador) {
                ecualizador = (float[]) map.get("mp3.equalizer");
                System.arraycopy(CustomPlayer.Eq, 0, ecualizador, 0, ecualizador.length);
                myPlayer.setBalance(CustomPlayer.Balance);
                CambioEnEcualizador = false;
            }
            //long LlevaEnSegundos = (myPlayer.Duration * rpp) / TamanoEnBytes;
            long LlevaEnSegundos = (pp / 1000000);
            progressLbl.setText(metodos.FormatoReloj(LlevaEnSegundos) + "/" + metodos.FormatoReloj(myPlayer.Duration));
        }
        if (Mostrar) {
            if (MostrarLaMusica != null) {
                MostrarLaMusica.dispose();
            }
            if (myPlayer.SongName == null || myPlayer.SongName.trim().isEmpty()) {
                MostrarLaMusica = new PopUpFrame(new java.awt.Frame(), false, "Unknow", "");
                MostrarLaMusica.setVisible(true);
            } else {
                MostrarLaMusica = new PopUpFrame(new java.awt.Frame(), false, myPlayer.SongName, myPlayer.Artist);
                MostrarLaMusica.setVisible(true);
            }
            Mostrar = false;
        }
    }

    @Override
    public void stateUpdated(BasicPlayerEvent bpe) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setController(BasicController bc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(Observable o, Object arg) {
        LibrariesComm communication = new LibrariesComm();
        mp3FilesArray = communication.getLibraryContent(0, "LocalUser");
        fillMusicTable();
    }

    @Override
    public void run() {
        this.setVisible(true);
    }
}
