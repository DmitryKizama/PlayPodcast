package stdmitry.playerpodcast.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import stdmitry.playerpodcast.database.Podcast;

public class DownloadPodcustService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URLPATH = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.vogella.android.service.receiver";
    private static final String LOG_TAG = "onDownloadPodcast";
    private boolean bItem = false;
    private boolean btitle = false;
    private boolean benclosure = false;
    private boolean bauthor = false;
    private boolean bpubdate = false;
    private boolean bsummary = false;

    private StringBuilder response;
    private Podcast podcast;
    private List<Podcast> list;

    public DownloadPodcustService() {
        super("DownloadPodcustService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        SystemClock.sleep(5000);
        list = new ArrayList();
        String urlPath = intent.getStringExtra(URLPATH);
        String fileName = intent.getStringExtra(FILENAME);
        response = new StringBuilder();
        InputStream stream = null;

        try {
            String str;
            URL url = new URL(urlPath);
            stream = url.openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            do {
                str = reader.readLine();
                response.append(str).append("\n");
            } while (str != null);

//            Log.d(LOG_TAG, "word = " + response);

            result = Activity.RESULT_OK;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        parseXml();
        saveToDataBase();
        publishResults(FILEPATH, result);
    }

    private void saveToDataBase() {
        int i = 0;
        if (list.size() == 0) {
            return;
        }
        for (Podcast podcast : list) {
            podcast = list.get(i);
//            Podcast pod = Podcast.selectByMP3(podcast.getMp3());
            Log.d("select", "select mp3 = " + podcast.getMp3());
            podcast.save();
            ++i;
        }
    }

    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    private void parseXml() {
        String tmp = "";
        try {
            XmlPullParser xpp = prepareXpp();
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        if (!bItem) {
//                            podcast = Podcast.create();
                            Log.d(LOG_TAG, "-----------------------------NEXT--------------------------");
                        }
                        getTipeOfName(xpp.getName(), 0);
//                        tmp = "";
//                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
//                            tmp = tmp + xpp.getAttributeName(i) + " = "
//                                    + xpp.getAttributeValue(i) + ", ";
//                        }
                        if (benclosure) {
                            Log.d(LOG_TAG, "ENCLOSURE: " + xpp.getAttributeValue(0));
                            podcast.setMp3(xpp.getAttributeValue(0));
                            benclosure = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        getTipeOfName(xpp.getName(), 1);
                        break;

                    case XmlPullParser.TEXT:
                        if (bItem && btitle) {
                            Log.d(LOG_TAG, "TITLE = " + xpp.getText());
                            podcast.setTitle(xpp.getText());
                        }
                        if (bItem && bpubdate) {
                            Log.d(LOG_TAG, "PUBLISH DATE = " + xpp.getText());
                            podcast.setPubdate(xpp.getText());
                        }
                        if (bItem && bauthor) {
                            Log.d(LOG_TAG, "AUTHOR = " + xpp.getText());
                            podcast.setAuthor(xpp.getText());
                        }
                        if (bItem && bsummary) {
                            Log.d(LOG_TAG, "DESCRIPTION = " + xpp.getText());
                            podcast.setDescription(xpp.getText());
                        }
                        break;

                    default:
                        break;
                }

                xpp.next();
            }
            Log.d(LOG_TAG, "END_DOCUMENT");

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTipeOfName(String s, int num) {
        switch (s) {
            case Podcast.ITEM:
                if (num == 0) {
                    if (podcast != null) {
                        list.add(podcast);
                    }
                    podcast = Podcast.create();
                    bItem = true;
                } else
                    bItem = false;
                break;
            case Podcast.TITLE:
                if (num == 0)
                    btitle = true;
                else
                    btitle = false;
                break;
            case Podcast.ENCLOSURE:
                benclosure = true;
                break;
            case Podcast.AUTHOR:
                if (num == 0)
                    bauthor = true;
                else
                    bauthor = false;
                break;
            case Podcast.PUBDATE:
                if (num == 0)
                    bpubdate = true;
                else
                    bpubdate = false;
                break;
            case Podcast.SUMMARY:
                if (num == 0)
                    bsummary = true;
                else
                    bsummary = false;
                break;
        }
    }

    XmlPullParser prepareXpp() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(response.toString()));
        return xpp;
    }

}
