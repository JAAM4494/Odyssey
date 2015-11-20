package odyssey;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

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
    public String SongName, Artist, Album, Genre, Anno;
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
    public boolean openSong(InputStream pInput, Mp3File pMetaFile) {
        BufferedInputStream bis = new BufferedInputStream(pInput);
        try {
            cleanMetadata();
            getMetaSong(pMetaFile);
            //player.open(bis);
            stop();
            control.open(bis);
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
    public boolean changeSongBy(InputStream pInput, Mp3File pMetaFile, boolean pPlaying) {
        stop();
        if (openSong(pInput, pMetaFile)) {
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

    public void getMetaSong(Mp3File pMetaFile) {
        SongName = pMetaFile.getName();
        Artist = pMetaFile.getArtist();
        Album = pMetaFile.getAlbum();
        Anno = pMetaFile.getAnno();
        Duration = pMetaFile.getDuration();
        lengthInBytes = pMetaFile.getLengthBytes();
    }
}
