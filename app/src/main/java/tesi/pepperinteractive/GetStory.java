package tesi.pepperinteractive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiException;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetStory extends RobotActivity implements RobotLifecycleCallbacks {

    private ImageView imageView;
    private ImageButton nextParagraph;
    private PlayerView videoView;
    private PlayerView audioView;
    private SimpleExoPlayer simpleAudioExoPlayer; //per l'audio
    private SimpleExoPlayer simpleVideoExoPlayer; //per il video
    private ArrayList<Bitmap> imageList = new ArrayList<>();
    private ArrayList<String> story = new ArrayList<>();
    private ArrayList<String> color = new ArrayList<>();
    private ArrayList<String> videoName = new ArrayList<>();
    private ArrayList<String> audioName = new ArrayList<>();
    private String moral;
    private int index = 0;
    //Variabili per le domande
    protected HashMap<Integer, QuestionData> questionDataMap = new HashMap<>();
    private LinearLayout buttonContainer;
    private Button answerButton1, answerButton2, answerButton3, answerButton4;
    private boolean isButtonPressed;
    //Costanti per specificare cosa dice Pepper se la risposta è corretta o sbagliata
    private static final String RISPOSTA_CORRETTA = "La risposta è corretta";
    private static final String RISPOSTA_SBAGLIATA = "La risposta è sbagliata";
    //Costanti per la nuova loading bar
    private RelativeLayout loadingLayout;
    private TextView loadedResource;
    private TextView correctAnswersCount;
    private int countResources = 0;
    private int corrAnswers = 0, totAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.get_story);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        audioView = findViewById(R.id.audioView);
        nextParagraph = findViewById(R.id.nextParagraph);

        //Link alle componenti XML
        buttonContainer = findViewById(R.id.answerButtonLayout);
        answerButton1 = findViewById(R.id.answerButton1);
        answerButton2 = findViewById(R.id.answerButton2);
        answerButton3 = findViewById(R.id.answerButton3);
        answerButton4 = findViewById(R.id.answerButton4);
        correctAnswersCount = findViewById(R.id.corrAnswerCount);

        //Link al layout della loading bar
        loadingLayout = findViewById(R.id.loadingLayout);
        loadedResource = findViewById(R.id.progressResource);

        getStory();
    }

    private void getStory() {

        class GetParagraphs extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String table = params[0];
                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/Cartella%20temporanea%20GETTERS/get_paragraph.php");
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);
                    // Richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject ob = jsonArray.getJSONObject(i);
                            story.add(i, ob.get("Testo").toString());
                        }
                        return responseBody;
                    } else {
                        Log.e("get-story", "Errore nella risposta in GetParagraph: " + response.code() + " " + response.message());
                    }
                } catch (IOException | JSONException e) {
                    Log.e("get-story", "Errore durante la richiesta HTTP in GetParagraph: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String b) {
                super.onPostExecute(b);
                for (int i=0; i < story.size(); i++ ) {
                    Log.d("get-story","\nParagrafo : "+(i+1)+ " -  "+story.get(i));
                    getImage(i);
                    getQuestion(i);
                }
                getColor();
                getVideoName();
                getAudioName();
                getMoral();
            }

        }

        GetParagraphs getParagraphs = new GetParagraphs();
        getParagraphs.execute(""+PepperStory.storyTitle);
    }

    private void getImage(int id) {

        class GetImage extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(String... params) {
                String table = params[0];
                String id = params[1];
                Bitmap image = null;
                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/Cartella%20temporanea%20GETTERS/get_image.php");
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8") +
                            "&" + URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);
                    //Richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                        image = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        imageList.add(Integer.parseInt(id), image);
                    } else {
                        Log.e("get-story", "Errore nella risposta in GetImage: " + response.code() + " " + response.message());
                    }
                } catch (IOException e) {
                    Log.e("get-story", "Errore durante la richiesta HTTP in GetImage: " + e.getMessage());
                    e.printStackTrace();
                }
                return image;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                if (b!=null){
                    countResources+=1;
                    loadedResource.setText("Risorse scaricate : "+countResources+" / "+story.size());
                }
            }
        }

        GetImage getImage = new GetImage();
        getImage.execute(""+PepperStory.storyTitle, ""+id);

    }

    private void getColor() {

        class GetColor extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String table = params[0];
                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/Cartella%20temporanea%20GETTERS/get_color.php");
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);
                    //Richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = Objects.requireNonNull(response.body()).string();
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject ob = jsonArray.getJSONObject(i);
                            color.add(ob.getString("Colore"));
                        }
                        return responseBody;
                    } else {
                        Log.e("get-story", "Errore nella risposta in GetColor: " + response.code() + " " + response.message());
                    }
                } catch (IOException | JSONException e) {
                    Log.e("get-story", "Errore durante la richiesta HTTP in GetColor: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String b) {
                super.onPostExecute(b);
            }

        }

        GetColor getColor = new GetColor();
        getColor.execute(""+PepperStory.storyTitle);
    }


    private void getVideoName() {

        class GetVideoName extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String table = params[0];
                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/get_video_name.php");
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);
                    //Richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        try {
                            String responseBodyVideo = Objects.requireNonNull(response.body()).string();
                            // Tentativo di parsing della risposta come JSON
                            JSONArray jsonArray;
                            jsonArray = new JSONArray(responseBodyVideo);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject ob = jsonArray.getJSONObject(i);
                                videoName.add(ob.getString("NomeVideo"));
                            }
                            return responseBodyVideo;
                        } catch (JSONException e) {
                            Log.d("get-story", "Il parsing del JSON in get_video_name.php non ha prodotto risultati o contiene errori : " + e.getMessage());
                            return null;
                        }
                    } else {
                        Log.e("get-story", "Errore nella risposta in GetVideoName: " + response.code() + " " + response.message());
                    }
                } catch (IOException e) {
                    Log.e("get-story", "Errore durante la richiesta HTTP in GetVideoName: " + e.getMessage());
                    e.printStackTrace();
                }

                return null;
            }


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String b) {
                super.onPostExecute(b);
            }

        }

        GetVideoName getVideoName = new GetVideoName();
        getVideoName.execute(""+PepperStory.storyTitle);
    }


    private void getAudioName() {

        class GetAudioName extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String table = params[0];
                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/get_audio_name.php");
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);
                    //Richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        try {
                            String responseBodyAudio = Objects.requireNonNull(response.body()).string();
                            // Tentativo di parsing della risposta come JSON
                            JSONArray jsonArray;
                            jsonArray = new JSONArray(responseBodyAudio);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject ob = jsonArray.getJSONObject(i);
                                videoName.add(ob.getString("NomeAudio"));
                            }
                            return responseBodyAudio;
                        } catch (JSONException e) {
                            Log.d("get-story", "Il parsing del JSON in get_audio_name.php non ha prodotto risultati o contiene errori : " + e.getMessage());
                            return null; // Ritorno null indicando un errore
                        }
                    } else {
                        Log.e("get-story", "Errore nella risposta in GetAudioName: " + response.code() + " " + response.message());
                    }
                } catch (IOException e) {
                    Log.e("get-story", "Errore durante la richiesta HTTP in GetAudioName: " + e.getMessage());
                    e.printStackTrace();
                }

                return null;
            }


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String b) {
                super.onPostExecute(b);
            }

        }

        GetAudioName getAudioName = new GetAudioName();
        getAudioName.execute(""+PepperStory.storyTitle);
    }


    private void getMoral() {

        class GetMoral extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String table = params[0];
                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/Cartella%20temporanea%20GETTERS/get_moral.php");

                    // Costruisci il corpo della richiesta POST
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);

                    // Costruisci la richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    // Esegui la richiesta e gestisci la risposta
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBodyMoral = response.body().string();
                        // Controllo preliminare sulla risposta
                        if (responseBodyMoral.isEmpty() || responseBodyMoral.equals("[]")) {
                            Log.d("get-story", "La query in get_moral.php non ha prodotto risultati");
                            return null;
                        }else{
                            Log.d("get-story", "La morale vale : "+ responseBodyMoral);
                            moral= responseBodyMoral;
                        }
                        return responseBodyMoral;
                    } else {
                        Log.e("get-story", "Errore nella risposta in GetMoral: " + response.code() + " " + response.message());
                    }
                } catch (IOException e) {
                    Log.e("get-story", "Errore durante la richiesta HTTP in GetMoral: " + e.getMessage());
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String b) {
                super.onPostExecute(b);
                getParagraph();
            }

        }

        GetMoral getMoral = new GetMoral();
        getMoral.execute(""+PepperStory.storyTitle);
    }

    //INIZIA CARICA DATI DOMANDA
    private void getQuestion(int id) {

        class GetQuestion extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String table = params[0];
                String id = params[1];
                int paragraphID;
                String domanda;
                String risposta1;
                String risposta2;
                String risposta3;
                String risposta4;
                int idParDestinazione1;
                int idParDestinazione2;
                int idParDestinazione3;
                int idParDestinazione4;
                int esitoRisp1;
                int esitoRisp2;
                int esitoRisp3;
                int esitoRisp4;

                OkHttpClient client = RequestOkHttpClient.getOkHttpClient();

                try {
                    URL url = new URL("https://pepper4storytelling.altervista.org/Cartella%20temporanea%20GETTERS/get_question.php");
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String sendData = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode(table, "UTF-8") +
                            "&" + URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    RequestBody body = RequestBody.create(mediaType, sendData);
                    // Richiesta POST con OkHttp
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBodyQuestion = response.body().string();
                        Log.d("question-logic", "\nID : "+(Integer.parseInt(id)+1)+"      Risultato query: " + responseBodyQuestion);

                        try {
                            JSONObject jsonObject = new JSONObject(responseBodyQuestion);
                            if (jsonObject.has("false")) {
                                return null;
                            }
                            else {
                                totAnswers += 1;
                                Log.d("count-answer","Tot Answer : "+totAnswers);
                                paragraphID = Integer.parseInt(jsonObject.getString("IdParagrafo"));
                                domanda = jsonObject.getString("Domanda");
                                risposta1 = jsonObject.getString("Risposta1");
                                risposta2 = jsonObject.getString("Risposta2");
                                risposta3 = jsonObject.getString("Risposta3");
                                risposta4 = jsonObject.getString("Risposta4");
                                idParDestinazione1 = jsonObject.optInt("idParDestinazione1");
                                idParDestinazione2 = jsonObject.optInt("idParDestinazione2");
                                idParDestinazione3 = jsonObject.optInt("idParDestinazione3");
                                idParDestinazione4 = jsonObject.optInt("idParDestinazione4");
                                esitoRisp1 = jsonObject.optInt("esitoRisp1");
                                esitoRisp2 = jsonObject.optInt("esitoRisp2");
                                esitoRisp3 = jsonObject.optInt("esitoRisp3");
                                esitoRisp4 = jsonObject.optInt("esitoRisp4");

                                QuestionData questionData = new QuestionData(domanda, risposta1, risposta2, risposta3, risposta4,
                                        idParDestinazione1, idParDestinazione2, idParDestinazione3, idParDestinazione4, esitoRisp1, esitoRisp2, esitoRisp3, esitoRisp4);
                                questionDataMap.put(paragraphID, questionData);
                                return responseBodyQuestion;
                            }
                        } catch (JSONException e) {
                            Log.d("question-logic", "Il paragrafo non prevede domande");
                        }

                        return responseBodyQuestion;
                    } else {
                        Log.e("question-logic", "Errore nella risposta in GetQuestion: " + response.code() + " " + response.message());
                    }
                } catch (IOException e) {
                    Log.e("question-logic", "Errore durante la richiesta HTTP in GetQuestion: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String b) { super.onPostExecute(b); }
        }
        GetQuestion getQuestion = new GetQuestion();
        Log.d("question-logic","Nome storia : "+PepperStory.storyTitle+ "   ID paragrafo : "+(id+1));
        getQuestion.execute(""+PepperStory.storyTitle, ""+id);
    }
    //FINE CARICA DATI DOMANDA

    private void getParagraph() {

        Log.d("prova", "valore array paragrafi: " +story.size());
        Log.d("prova", "valore array immagini: " +imageList.size());
        Log.d("prova", "prova bitmap: " + imageList.get(index));
        if (imageList.get(index) != null) {
            imageView.setBackgroundColor(255);
            imageView.setImageBitmap(imageList.get(index));
        } else if (!videoName.isEmpty() && !videoName.get(index).isEmpty()) { // SE IL NOMEVIDEO NELLA COLONNA DEL DATABASE E' PRESENTE
            // Controlla se il nome del video è diverso da "null" (stringa) e "NULL" (stringa), e non null (oggetto)
            if (videoName.get(index) != null && !videoName.get(index).equalsIgnoreCase("null")) {
                String storyTableNoSpace = PepperStory.storyTitle;
                storyTableNoSpace = storyTableNoSpace.replaceAll(" ", "%20");
                Log.d("conn-video", "prova stringa storyTableNoSpace: " + storyTableNoSpace);
                String string = "https://pepper4storytelling.altervista.org/get_video2.php?table=" + storyTableNoSpace + "&id=" + index;
                Log.d("conn-video", "prova stringa connessione: " + string);
                //String string = "https://pepper4storytelling.altervista.org/get_video2.php?table=" + PepperStory.storyTitle + "&id=" + index;
                //Log.d("prova video", "prova stringa connessione: " + string);
                imageView.setVisibility(imageView.INVISIBLE);
                simpleVideoExoPlayer = new SimpleExoPlayer.Builder(this).build();
                videoView.setPlayer(simpleVideoExoPlayer);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
                MediaSource dataSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(string));
                videoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                videoView.setControllerHideOnTouch(true);
                simpleVideoExoPlayer.prepare(dataSource);
                simpleVideoExoPlayer.setPlayWhenReady(true);
                simpleVideoExoPlayer.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if(playbackState == Player.STATE_ENDED) {
                            Log.d("conn-video", "IS PLAYING: " +simpleVideoExoPlayer.isPlaying());
                            Log.d("conn-video", "SONO NEL LISTENER STATO FINITO");
                            if(simpleAudioExoPlayer == null || simpleAudioExoPlayer.isPlaying() == false) {
                                Log.d("conn-video", "sono nel getParagraph dell'if dell'end simpleVideoPlayer");
                                nextParagraph.setVisibility(nextParagraph.VISIBLE);
                                imageView.setBackgroundColor(255);
                                videoView.setLayoutParams(new FrameLayout.LayoutParams(1, 1));
                                index = index +1;
                                imageView.setVisibility(imageView.VISIBLE);
                                nextParagraph.setVisibility(nextParagraph.INVISIBLE);
                                getParagraph();
                            }
                        }
                    }
                });
            }else{
                Log.d("conn-video","Sono nell'else");
                imageView.setImageBitmap(null);
                imageView.setBackgroundColor(Color.parseColor(color.get(index)));
            }
        } else {
            imageView.setImageBitmap(null);
            imageView.setBackgroundColor(Color.parseColor(color.get(index)));
        }
        if (!audioName.isEmpty() && !audioName.get(index).isEmpty()) { //SE IL NOMEAUDIO NELLA COLONNA DEL DATABASE E' PRESENTE
            //SE NON SOSTITUISCO GLI SPAZI CON I CARATTERI %20, L'AUDIO NON VIENE RIPRODOTTO
            if(!audioName.get(index).equals("NULL")||!audioName.get(index).equals("null")){
                String storyTableNoSpace = PepperStory.storyTitle;
                storyTableNoSpace = storyTableNoSpace.replaceAll(" ", "%20");
                Log.d("prova video", "prova stringa storyTableNoSpace: " + storyTableNoSpace);
                String string = "https://pepper4storytelling.altervista.org/get_audio.php?table=" + storyTableNoSpace + "&id=" + index;
                Log.d("prova video", "prova stringa connessione: " + string);
                //String string = "https://pepper4storytelling.altervista.org/get_audio.php?table=" + PepperStory.storyTitle + "&id=" + index;
                //Log.d("prova audio", "prova stringa connessione: " + string);
                simpleAudioExoPlayer = new SimpleExoPlayer.Builder(this).build();
                audioView.setPlayer(simpleAudioExoPlayer);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
                MediaSource dataSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(string));
                //audioView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                audioView.setControllerHideOnTouch(true);
                simpleAudioExoPlayer.prepare(dataSource);
                simpleAudioExoPlayer.setPlayWhenReady(true);
                simpleAudioExoPlayer.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if (playbackState == Player.STATE_ENDED) {
                            Log.d("prova audio", "IS PLAYING: " + simpleAudioExoPlayer.isPlaying());
                            Log.d("prova audio", "SONO NEL LISTENER STATO FINITO");
                            if (simpleVideoExoPlayer == null || simpleVideoExoPlayer.isPlaying() == false) {
                                Log.d("flusso", "sono nel getParagraph dell'if dell'end simpleAudioPlayer");
                                nextParagraph.setVisibility(nextParagraph.VISIBLE);
                                index = index +1;
                                //imageView.setVisibility(imageView.VISIBLE); INUTILE?
                                nextParagraph.setVisibility(nextParagraph.INVISIBLE);
                                getParagraph();
                            }
                        }
                    }
                });
            }
        }
        Log.d("CARICAMENTO", "\n\nFine caricamento");
        loadingLayout.setVisibility(View.GONE);
        correctAnswersCount.setText("Risposte corrette : "+corrAnswers+" di "+totAnswers);
        correctAnswersCount.setVisibility(View.VISIBLE);
        startTalk();
    }

    public void startTalk() {
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);
        QiSDK.register(this, this);
    }

    @Override
    public void onBackPressed() {
        if(simpleAudioExoPlayer != null) {
            simpleAudioExoPlayer.stop();
            simpleAudioExoPlayer.release();
        }
        if(simpleVideoExoPlayer != null) {
            simpleVideoExoPlayer.stop();
            simpleVideoExoPlayer.release();
        }
        startActivity(new Intent(GetStory.this, PepperStory.class));
        finish();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        int q_index;
        Log.d("prova", "valore array paragrafi: " +story.size());
        Log.d("prova", "valore array immagini: " +imageList.size());

        Phrase paragrafo = new Phrase("\\rspd=85\\\\wait=9\\" + story.get(index));
        Say say = SayBuilder.with(qiContext).withPhrase(paragrafo).build();
        say.run();
        if (index + 1 < story.size()) {
            //INIZIO AGGIUNTA DOMANDE
            q_index=index+1;
            if (questionDataMap.containsKey(q_index)) {
                isButtonPressed = false;
                // Ottieni l'oggetto QuestionData corrispondente all'idParagrafoDesiderato
                QuestionData questionData = questionDataMap.get(q_index);
                Log.d("speech-logic-focus","Sto per entrare in askQuestionAndListen - index:"+index);
                assert questionData != null;
                //Mando la domanda al metodo per la gestione delle domande
                askQuestionAndListen(qiContext, questionData);

            } else {
                Log.d("question-logic-focus", "Nessuna informazione trovata per l'IdParagrafo " + q_index);
                if(simpleVideoExoPlayer == null && simpleAudioExoPlayer == null) {
                    Log.d("question-logic-focus", "AUDIO NULL E VIDEO NULL");
                    index = index +1;
                    runOnUiThread(() -> imageView.setVisibility(View.VISIBLE));
                    runOnUiThread(() -> nextParagraph.setVisibility(View.INVISIBLE));
                    runOnUiThread(this::getParagraph);
                } else if (simpleAudioExoPlayer != null && simpleVideoExoPlayer == null) {
                    if (!simpleAudioExoPlayer.isPlaying()) {
                        Log.d("question-logic-focus", "AUDIO IS PLAYING FALSE E VIDEO NULL");
                        index = index +1;
                        runOnUiThread(() -> imageView.setVisibility(View.VISIBLE));
                        runOnUiThread(() -> nextParagraph.setVisibility(View.INVISIBLE));
                        runOnUiThread(this::getParagraph);
                    }
                } else if (simpleAudioExoPlayer == null) {
                    if (!simpleVideoExoPlayer.isPlaying()) {
                        Log.d("question-logic-focus", "VIDEO IS PLAYING FALSE E AUDIO NULL");
                        index = index +1;
                        runOnUiThread(() -> imageView.setVisibility(View.VISIBLE));
                        runOnUiThread(() -> nextParagraph.setVisibility(View.INVISIBLE));
                        runOnUiThread(this::getParagraph);
                    }
                } else if (!simpleAudioExoPlayer.isPlaying() && !simpleVideoExoPlayer.isPlaying()) {
                    Log.d("question-logic-focus", "AUDIO E VIDEO IS PLAYING FALSE");
                    index = index +1;
                    runOnUiThread(() -> imageView.setVisibility(View.VISIBLE));
                    runOnUiThread(() -> nextParagraph.setVisibility(View.INVISIBLE));
                    runOnUiThread(this::getParagraph);
                }
            }
            //FINE AGGIUNTA DOMANDE


        } else if (index + 1==story.size()){
            //SE SIAMO ALL'ULTIMO PARAGRAFO
            Log.d("get-story", "sono nell'else ultimo paragrafo : "+story.size());
            if (questionDataMap.containsKey(story.size())) {
                // Ottieni l'oggetto QuestionData corrispondente all'idParagrafoDesiderato
                QuestionData questionData = questionDataMap.get(story.size());
                // Ottieni i valori dai campi di QuestionData
                assert questionData != null;
                //Mando la domanda al metodo per la gestione delle domande
                askQuestionAndListen(qiContext, questionData);
            } else {
                Log.d("question-logic-focus", "Nessuna informazione trovata per l'IdParagrafo " + story.size());
            }

            runOnUiThread(() -> nextParagraph.setVisibility(View.INVISIBLE));

            //RACCONTO MORALE
            if (moral!=null &&!moral.isEmpty()) {
                Log.d("get-story", "sono nell'if della morale");
                Phrase morale = new Phrase("\\rspd=85\\\\wait=9\\La morale della storia è: " + moral);
                Say say1 = SayBuilder.with(qiContext).withPhrase(morale).build();
                say1.run();
                Log.d("get-story", "\nTesto morale: " + moral);
            }else{
                Log.d("get-story", "La storia non prevede morale");
            }

            Animation animation = AnimationBuilder.with(qiContext).withResources(R.raw.hello_a002).build(); // Build the animation.
            Animate animate;
            // Create an animate action.
            animate = AnimateBuilder.with(qiContext).withAnimation(animation).build(); // Build the animate action.
            // Run the animate action asynchronously.
            Future<Void> animateFuture = animate.async().run();
            String endStoryPhrase="La storia è terminata. Grazie a tutti per l'attenzione.";
            if (!questionDataMap.isEmpty())
                endStoryPhrase="Grazie per la partecipazione, le risposte corrette sono "+corrAnswers+" su un totale di "+totAnswers;
            Phrase endStory = new Phrase(endStoryPhrase);
            Say say2 = SayBuilder.with(qiContext).withPhrase(endStory).build();
            say2.run();

            startActivity(new Intent(GetStory.this, PepperStory.class));
            finish();
        }


        nextParagraph.setOnClickListener(view -> {
            Log.d("question-logic-focus", "SONO IN NEXTPARAGRAPH");
            index = index +1;
            runOnUiThread(() -> imageView.setVisibility(View.VISIBLE));
            runOnUiThread(() -> nextParagraph.setVisibility(View.INVISIBLE));
            getParagraph();
        });
    }


    private Future<ListenResult> listenFuture; // Variabile per tracciare il Future del processo di ascolto
    private boolean preventSpam = false;
    private void handleAnswerFromButton(int idParagraphDestination, int answerType, QiContext qiContext) {
        int newIndex;
        String responseText = "";
        newIndex = idParagraphDestination;
        Log.d("question-logic-focus", "Sono in HandleAnswer questo è il nuovo id di destinazione : " + newIndex);
        if (newIndex != -1) {
            isButtonPressed = true;
            index = newIndex - 1;
            if (answerType == 1) {
                Log.d("question-logic-focus", RISPOSTA_CORRETTA);
                responseText = RISPOSTA_CORRETTA;

                if(!preventSpam){
                    corrAnswers+=1;
                    correctAnswersCount.setText("Risposte corrette : "+corrAnswers+" di "+totAnswers);
                    preventSpam = true;
                }

            } else if (answerType == 2) {
                Log.d("question-logic-focus", RISPOSTA_SBAGLIATA);
                responseText = RISPOSTA_SBAGLIATA;
            }
            // Cancella il processo di ascolto corrente, se presente
            if (listenFuture != null && !listenFuture.isDone()) {
                listenFuture.requestCancellation();
            }
            Log.d("question-logic-focus", "Sono dopo il metodo vocale");
            String finalResponseText = responseText;
            SayBuilder.with(qiContext).withPhrase(new Phrase("\\rspd=85\\\\wait=9\\" + finalResponseText)).buildAsync().thenConsume(sayAnswerFuture -> {
                if (sayAnswerFuture.isSuccess()) {
                    Say sayAnswer = sayAnswerFuture.get();
                    sayAnswer.async().run().andThenConsume(gotonext -> {
                        runOnUiThread(() -> buttonContainer.setVisibility(View.GONE));
                        runOnUiThread(() -> answerButton3.setVisibility(View.GONE));
                        runOnUiThread(() -> answerButton4.setVisibility(View.GONE));
                        preventSpam = false;
                        runOnUiThread(this::getParagraph);
                    });
                } else {
                    Log.e("question-logic-focus", "Failed to build Say action", sayAnswerFuture.getError());
                }
            });
        }
    }

    private void askQuestionAndListen(QiContext qiContext, QuestionData questionData) {
        Log.d("question-logic-focus", "Sto per farti una domanda: " + questionData.getDomanda());
        String build_phrase_with_answers = questionData.getDomanda() + "\\wait=9\\" + questionData.getRisposta1() + "\\wait=9\\" + questionData.getRisposta2();
        if (!questionData.getRisposta3().equals("null")) {
            Log.d("question-logic-focus", "QUANTO VALE: "+questionData.getRisposta3());
            build_phrase_with_answers += "\\wait=7\\" + questionData.getRisposta3();
            if (!questionData.getRisposta4().equals("null")) {
                Log.d("question-logic-focus", "QUANTO VALE: "+questionData.getRisposta4());
                build_phrase_with_answers += "\\wait=7\\" + questionData.getRisposta4();
            }
        }
        Phrase questionPhrase = new Phrase("\\rspd=85\\\\wait=9\\"+build_phrase_with_answers);
        Say sayQuestion = SayBuilder.with(qiContext).withPhrase(questionPhrase).build();
        runOnUiThread(() -> {
            buttonContainer.setVisibility(View.VISIBLE);
            answerButton1.setText(questionData.getRisposta1());
            answerButton2.setText(questionData.getRisposta2());
            answerButton3.setText(questionData.getRisposta3());
            answerButton4.setText(questionData.getRisposta4());
            answerButton1.setEnabled(false);
            answerButton2.setEnabled(false);
            answerButton3.setEnabled(false);
            answerButton4.setEnabled(false);
            if (!questionData.getRisposta3().equals("null")) {
                answerButton3.setVisibility(View.VISIBLE);
            }
            if (!questionData.getRisposta4().equals("null")) {
                answerButton4.setVisibility(View.VISIBLE);
            }
        });

        // Metodo di interazione 1: pressione sul bottone
        answerButton1.setOnClickListener(view -> handleAnswerFromButton(questionData.getIdParDestinazione1(), questionData.getEsitoRisp1(), qiContext));
        answerButton2.setOnClickListener(view -> handleAnswerFromButton(questionData.getIdParDestinazione2(), questionData.getEsitoRisp2(), qiContext));
        answerButton3.setOnClickListener(view -> handleAnswerFromButton(questionData.getIdParDestinazione3(), questionData.getEsitoRisp3(), qiContext));
        answerButton4.setOnClickListener(view -> handleAnswerFromButton(questionData.getIdParDestinazione4(), questionData.getEsitoRisp4(), qiContext));

        // Metodo di interazione 2: risposta a voce
        if (!isButtonPressed) {
            sayQuestion.async().run().andThenConsume(ignore -> {
                Log.d("question-logic-focus", "Le risposte sono "+
                        "\nA : " + questionData.getRisposta1() +" La risposta è : "+questionData.getEsitoRisp1()+
                        "\nB : " + questionData.getRisposta2() +" La risposta è : "+questionData.getEsitoRisp2()+
                        "\nC : " + questionData.getRisposta3() +" La risposta è : "+questionData.getEsitoRisp3()+
                        "\nD : " + questionData.getRisposta4() +" La risposta è : "+questionData.getEsitoRisp4());
                runOnUiThread(() -> {
                        answerButton1.setEnabled(true);
                        answerButton2.setEnabled(true);
                        answerButton3.setEnabled(true);
                        answerButton4.setEnabled(true);
                });
                PhraseSet phraseSet1 = PhraseSetBuilder.with(qiContext)
                        .withTexts(questionData.getRisposta1()).build();
                PhraseSet phraseSet2 = PhraseSetBuilder.with(qiContext)
                        .withTexts(questionData.getRisposta2()).build();
                PhraseSet phraseSet3 = PhraseSetBuilder.with(qiContext)
                        .withTexts(questionData.getRisposta3()).build();
                PhraseSet phraseSet4 = PhraseSetBuilder.with(qiContext)
                        .withTexts(questionData.getRisposta4()).build();

                Listen listenAnswer = ListenBuilder.with(qiContext)
                        .withPhraseSets(phraseSet1, phraseSet2, phraseSet3, phraseSet4)
                        .build();

                // Salva il Future del processo di ascolto
                listenFuture = listenAnswer.async().run();
                listenFuture.andThenConsume(listenResult -> {
                    String heardPhrase = listenResult.getHeardPhrase().getText();
                    Log.d("question-logic-focus", "Heard phrase: " + heardPhrase);

                    String responseText="";

                    if (heardPhrase.equalsIgnoreCase(questionData.getRisposta1())) {
                        if (questionData.getEsitoRisp1() == 1) {
                            Log.d("question-logic-focus", RISPOSTA_CORRETTA);
                            responseText = RISPOSTA_CORRETTA;
                            corrAnswers+=1;
                        } else if (questionData.getEsitoRisp1() == 2) {
                            Log.d("question-logic-focus", RISPOSTA_SBAGLIATA);
                            responseText = RISPOSTA_SBAGLIATA;
                        }
                        index = questionData.getIdParDestinazione1() - 1; // -1 perché index è zero-based
                    } else if (heardPhrase.equalsIgnoreCase(questionData.getRisposta2())) {
                        if (questionData.getEsitoRisp2() == 1) {
                            Log.d("question-logic-focus", RISPOSTA_CORRETTA);
                            responseText = RISPOSTA_CORRETTA;
                            corrAnswers+=1;
                        } else if (questionData.getEsitoRisp2() == 2) {
                            Log.d("question-logic-focus", RISPOSTA_SBAGLIATA);
                            responseText = RISPOSTA_SBAGLIATA;
                        }
                        index = questionData.getIdParDestinazione2() - 1;
                    } else if (heardPhrase.equalsIgnoreCase(questionData.getRisposta3())) {
                        if (questionData.getEsitoRisp3() == 1) {
                            Log.d("question-logic-focus", RISPOSTA_CORRETTA);
                            responseText = RISPOSTA_CORRETTA;
                            corrAnswers+=1;
                        } else if (questionData.getEsitoRisp3() == 2) {
                            Log.d("question-logic-focus", RISPOSTA_SBAGLIATA);
                            responseText = RISPOSTA_SBAGLIATA;
                        }
                        index = questionData.getIdParDestinazione3() - 1;
                    } else if (heardPhrase.equalsIgnoreCase(questionData.getRisposta4())) {
                        if (questionData.getEsitoRisp4() == 1) {
                            Log.d("question-logic-focus", RISPOSTA_CORRETTA);
                            responseText = RISPOSTA_CORRETTA;
                            corrAnswers+=1;
                        } else if (questionData.getEsitoRisp4() == 2) {
                            Log.d("question-logic-focus", RISPOSTA_SBAGLIATA);
                            responseText = RISPOSTA_SBAGLIATA;
                        }
                        index = questionData.getIdParDestinazione4() - 1;
                    }


                    Log.d("question-logic-focus","Sono dopo il metodo vocale");
                    Phrase answerType = new Phrase("\\rspd=85\\\\wait=9\\"+responseText);
                    Say sayAnswer = SayBuilder.with(qiContext).withPhrase(answerType).build();
                    sayAnswer.async().run().andThenConsume(gotonext -> {
                        runOnUiThread(() -> buttonContainer.setVisibility(View.GONE));
                        runOnUiThread(() -> answerButton3.setVisibility(View.GONE));
                        runOnUiThread(() -> answerButton4.setVisibility(View.GONE));
                        runOnUiThread(() -> correctAnswersCount.setText("Risposte corrette : "+corrAnswers+" di "+totAnswers));
                        runOnUiThread(this::getParagraph);
                    });
                });
            });
        }
    }
// TODO : 17/06/2024 Aggiungere una considerazione finale in base al numero di risposte corrette? - far ripartire subito la storia?

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

}
