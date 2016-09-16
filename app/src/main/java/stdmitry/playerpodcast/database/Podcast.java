package stdmitry.playerpodcast.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "PodcastModel")
public class Podcast extends Model {

    public static final String ITEM = "item";
    public static final String TITLE = "title";
    public static final String PUBDATE = "pubDate";
    public static final String ENCLOSURE = "enclosure";
    public static final String SUMMARY = "summary";
    public static final String AUTHOR = "author";

    @Column(name = TITLE)
    private String title;

    @Column(name = PUBDATE)
    private String pubdate;

    @Column(name = ENCLOSURE, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private String mp3;

    @Column(name = SUMMARY)
    private String description;

    @Column(name = AUTHOR)
    private String author;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getPubdate() {
        return pubdate;
    }

    public String getMp3() {
        return mp3;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }


    public Podcast() {
        super();
    }

    public static Podcast create() {
        Podcast podcast = new Podcast();
        return podcast;
    }

    public static List<Podcast> getAll() {
        return new Select().from(Podcast.class).execute();
    }

    public static Podcast selectByMP3(String mp3) {
        if (mp3 == null) {
            return null;
        }
        return new Select().from(Podcast.class).where(Podcast.ENCLOSURE + " = ?", mp3).executeSingle();
    }

}
