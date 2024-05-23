package tesi.pepperinteractive;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetTitles extends AsyncTask<Void, Void, String> {
    Context context;
    protected static ArrayList<String> titleList = new ArrayList<>();

    public GetTitles(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        OkHttpClient client = RequestOkHttpClient.getOkHttpClient();
        Request request = new Request.Builder()
                .url("https://pepper4storytelling.altervista.org/Cartella%20temporanea%20GETTERS/story_list.php")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                JSONArray jsonArray = new JSONArray(responseBody);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject ob = jsonArray.getJSONObject(i);
                    titleList.add(ob.getString("Titolo"));
                    Log.d("gettitles","JSON titoli storie :\n"+ob.getString("Titolo"));
                }
                return responseBody;
            } else {
                Log.e("gettitles", "Errore nella risposta : " + response.code() + " " + response.message());
            }
        } catch (IOException | JSONException e) {
            Log.e("gettitles", "Errore durante la richiesta HTTP : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
