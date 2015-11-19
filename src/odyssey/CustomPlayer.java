package odyssey;

import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import org.tritonus.share.sampled.TAudioFormat;

import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 *
 * @author jaam
 */
public class CustomPlayer {

    public BasicPlayer player;
    public static float[] Eq = new float[32];
    public static int[] EqP = new int[10];
    public static float Balance;
    public static int ItemEc;
    public double Volume;
    public int Duration;
    public String SongName, Artist, Album,Genre,Anno;
    public int lengthInBytes;    
    public BasicController control;

    /**
     *
     */
    public CustomPlayer() {
        this.Volume = 1;
        player = new BasicPlayer();
        control = (BasicController) player;
    }
    
    public void open2000(URL pUrl) {
        try {
            control.open(pUrl);
        } catch (BasicPlayerException ex) {
            Logger.getLogger(CustomPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param pInput
     * @param pStreamSize
     * @return
     */
    public boolean openSong(InputStream pInput,Mp3File pMetaFile) {
        //lengthInBytes = pStreamSize;
        BufferedInputStream bis = new BufferedInputStream(pInput);
        try {
            cleanMetadata();
            //getMetaSong(bis, pStreamSize,pMetaFile);
            getMetaSong3000(pMetaFile);
            //player.open(bis);
            System.out.println("End1");
            stop();
            control.open(bis);
            System.out.println("End2");
            return true;
        } catch (BasicPlayerException ex) {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public boolean play() {
        try {
            if (player.getStatus() == 1) {
                //player.resume();
                control.resume();
            } else {
                control.play();
                //player.play();
            }
            changeVolume(Volume);
            return true;
        } catch (BasicPlayerException ex) {
            return false;
        }
    }
    
    /**
     *
     * @return
     */
    public boolean pause() {
        try {
            if (player.getStatus() == 0) {
                //player.pause();
                control.pause();
            }
            return true;
        } catch (BasicPlayerException ex) {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public boolean stop() {
        try {
            if (player.getStatus() == 1 || player.getStatus() == 0) {
                //player.stop();
                control.stop();
            }
            return true;
        } catch (BasicPlayerException ex) {
            return false;
        }
    }

    /**
     *
     * @param pInput
     * @param pStreamSize
     * @param pPlaying
     * @return
     */
    public boolean changeSongBy(InputStream pInput,Mp3File pMetaFile, boolean pPlaying) {
        stop();
        if (openSong(pInput,pMetaFile)) {
            if (pPlaying) {
                play();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param pPosition
     * @return
     */
    public boolean setLocation(long pPosition) {
        try {
            boolean SeReproduce = false;
            if (player.getStatus() == 0) {
                SeReproduce = true;
            }
            stop();
            //player.seek(pPosition);
            control.seek(pPosition);
            if (SeReproduce) {
                play();
            }
            return true;
        } catch (BasicPlayerException ex) {
            return false;
        }

    }

    /**
     *
     * @return
     */
    public int getStatus() {
        return player.getStatus();
    }

    /**
     *
     * @param pVolume
     * @return
     */
    public boolean changeVolume(double pVolume) {
        try {
            Volume = pVolume;
            //player.setGain(pVolume);
            control.setGain(pVolume);
        } catch (BasicPlayerException ex) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param pBalance
     * @return
     */
    public boolean setBalance(float pBalance) {
        try {
            //player.setPan(pBalance);
            control.setPan(pBalance);
        } catch (BasicPlayerException ex) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public void cleanMetadata() {
        SongName = "";
        Artist = "";
        Album = "";
        Anno = "";
        Duration = 0;
    }
    
    public void getMetaSong3000(Mp3File pMetaFile) {        
        SongName = pMetaFile.getName();
        Artist = pMetaFile.getArtist();
        Album = pMetaFile.getAlbum();
        Anno = pMetaFile.getAnno();
        Duration = pMetaFile.getDuration();
        lengthInBytes = pMetaFile.getLengthBytes();
    }
    
    public void getMetaSong2000(InputStream pInput, byte[] pByteArray, Mp3File pMetaFile) {
        AudioFileFormat baseFileFormat = null;

        File tempFile = null;
        try {
            tempFile = File.createTempFile("musir", ".mp3");
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempFile);
            //byte[] bytes = ByteStreams.toByteArray(pInput);
            fos.write(pByteArray);

            String path = tempFile.getAbsolutePath();

            //System.out.println("Temp file : " + tempFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(CustomPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //File mp3Archivo = new File(pRuta);
        //AudioFileFormat baseFileFormat = null;
        try {
            baseFileFormat = AudioSystem.getAudioFileFormat(tempFile);
        } catch (UnsupportedAudioFileException | IOException ex) {
            Logger.getLogger(CustomPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (baseFileFormat instanceof TAudioFileFormat) {
            Map properties = ((TAudioFileFormat) baseFileFormat).properties();
            Duration = (int) properties.get("duration") / 1000000;

            System.out.println(Long.toString(Duration));
        }
    
        SongName = pMetaFile.getName();
        Artist = pMetaFile.getArtist();
        Album = pMetaFile.getAlbum();
        Anno = pMetaFile.getAnno();
    }

    /**
     *
     * @param pInput
     * @param pStreamSize
     */
    public void getMetaSong(InputStream pInput, int pStreamSize,Mp3File pMetaFile) {
        AudioFileFormat baseFileFormat = null;
        AudioFormat baseFormat = null;

        try {
            baseFileFormat = AudioSystem.getAudioFileFormat(pInput);
            baseFormat = baseFileFormat.getFormat();
        } catch (UnsupportedAudioFileException | IOException ex) {
            Logger.getLogger(CustomPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (baseFileFormat instanceof TAudioFileFormat) {
            Map properties = ((TAudioFileFormat) baseFileFormat).properties();
            
            if (properties.containsKey("duration")) {
                Duration = (int) properties.get("duration") / 1000000;
            } else {
                Integer bitrate = 0;
                if (baseFormat instanceof TAudioFormat) {
                    Map propertiesAux = ((TAudioFormat) baseFormat).properties();
                    String key = "bitrate";
                    bitrate = (Integer) propertiesAux.get(key);
                    
                }              
                long dur = (long)(pStreamSize / bitrate);
                Duration = (int) (dur * 8.13);
            }
        }
        
        SongName = pMetaFile.getName();
        Artist = pMetaFile.getArtist();
        Album = pMetaFile.getAlbum();
        Anno = pMetaFile.getAnno();
    }
    
    /*
    public boolean Abrir(String Ruta){
        File Archivo=new File(Ruta);
        try {
            cleanMetadata();
            ObternerDatos(Ruta);
            
            InputStream is = new FileInputStream(Archivo);
            byte[] bytes = ByteStreams.toByteArray(is);
            
            System.out.println("Length:");
            System.out.println(Integer.toString(bytes.length));
            
            //player.open(Archivo);
            control.open(Archivo);
            
            
            return true;
        } catch (BasicPlayerException ex) {
            return false;
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
    */
    
    /*
    public void ObternerDatos(String pRuta){
        File mp3Archivo = new File(pRuta);
        AudioFileFormat baseFileFormat = null;
        try {
            baseFileFormat = AudioSystem.getAudioFileFormat(mp3Archivo);
        } catch (UnsupportedAudioFileException | IOException ex) {
            Logger.getLogger(CustomPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(baseFileFormat instanceof TAudioFileFormat){
            Map properties = ((TAudioFileFormat)baseFileFormat).properties();
            SongName=(String)properties.get("title");
            Artist=(String)properties.get("author");
            Album=(String)properties.get("album");
            Anno=(String)properties.get("date");
            Duration=(long)properties.get("duration")/1000000;
            
            System.out.println(Long.toString(Duration));
        }
    }*/
 
}