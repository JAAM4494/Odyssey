/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

/**
 *
 * @author jaam
 */
public class Mp3File {
    
    private String Name;
    private String Artist;
    private String Album;
    private String Genre;
    private String Anno;
    private String ID;
    private int Duration;
    private int LengthBytes;
    
    private String NameBackup;
    private String ArtistBackup;
    private String AlbumBackup;
    private String GenreBackup;
    private String AnnoBackup;
    
    public Mp3File() {
        Name = "";
        Artist = "";
        Album = "";
        Genre = "";
        Anno = "";
        Duration = 1;
        LengthBytes = 1;
        
        NameBackup = "";
        ArtistBackup = "";
        AlbumBackup = "";
        GenreBackup = "";
        AnnoBackup = "";
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String Artist) {
        this.Artist = Artist;
    }

    public String getAlbum() {
        return Album;
    }

    public void setAlbum(String Album) {
        this.Album = Album;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String Genre) {
        this.Genre = Genre;
    }

    public String getAnno() {
        return Anno;
    }

    public void setAnno(String Anno) {
        this.Anno = Anno;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int Duration) {
        this.Duration = Duration;
    }

    public int getLengthBytes() {
        return LengthBytes;
    }

    public void setLengthBytes(int LengthBytes) {
        this.LengthBytes = LengthBytes;
    }

    public String getNameBackup() {
        return NameBackup;
    }

    public void setNameBackup(String NameBackup) {
        this.NameBackup = NameBackup;
    }

    public String getArtistBackup() {
        return ArtistBackup;
    }

    public void setArtistBackup(String ArtistBackup) {
        this.ArtistBackup = ArtistBackup;
    }

    public String getAlbumBackup() {
        return AlbumBackup;
    }

    public void setAlbumBackup(String AlbumBackup) {
        this.AlbumBackup = AlbumBackup;
    }

    public String getGenreBackup() {
        return GenreBackup;
    }

    public void setGenreBackup(String GenreBackup) {
        this.GenreBackup = GenreBackup;
    }

    public String getAnnoBackup() {
        return AnnoBackup;
    }

    public void setAnnoBackup(String AnnoBackup) {
        this.AnnoBackup = AnnoBackup;
    }
}
