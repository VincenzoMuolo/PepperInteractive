package tesi.pepperinteractive;

import org.json.JSONException;
import org.json.JSONObject;

class Esito {
    private String storyTitle;
    private int idParagrafo;
    private String esito;

    // Costruttore
    public Esito(String storyTitle, int idParagrafo, String esito) {
        this.storyTitle = storyTitle;
        this.idParagrafo = idParagrafo;
        this.esito = esito;
    }
    // Metodo per convertire l'oggetto in JSON
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("storyTitle", storyTitle);
            jsonObject.put("idParagrafo", idParagrafo);
            jsonObject.put("esito", esito);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
