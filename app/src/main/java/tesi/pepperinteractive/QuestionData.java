package tesi.pepperinteractive;

class QuestionData {
    private String domanda;
    private String risposta1;
    private String risposta2;
    private String risposta3;
    private String risposta4;
    private int idParDestinazione1;
    private int idParDestinazione2;
    private int idParDestinazione3;
    private int idParDestinazione4;
    private int esitoRisp1;
    private int esitoRisp2;
    private int esitoRisp3;
    private int esitoRisp4;

    // Costruttore
    public QuestionData(String domanda, String risposta1, String risposta2, String risposta3, String risposta4,
                        int idParDestinazione1, int idParDestinazione2, int idParDestinazione3, int idParDestinazione4,
                        int esitoRisp1, int esitoRisp2, int esitoRisp3, int esitoRisp4) {
        this.domanda = domanda;
        this.risposta1 = risposta1;
        this.risposta2 = risposta2;
        this.risposta3 = risposta3;
        this.risposta4 = risposta4;
        this.idParDestinazione1 = idParDestinazione1;
        this.idParDestinazione2 = idParDestinazione2;
        this.idParDestinazione3 = idParDestinazione3;
        this.idParDestinazione4 = idParDestinazione4;
        this.esitoRisp1 = esitoRisp1;
        this.esitoRisp2 = esitoRisp2;
        this.esitoRisp3 = esitoRisp3;
        this.esitoRisp4 = esitoRisp4;
    }

    // Metodi getter per accedere ai dati
    public String getDomanda() {
        return domanda;
    }

    public String getRisposta1() {
        return risposta1;
    }

    public String getRisposta2() {
        return risposta2;
    }

    public String getRisposta3() {
        return risposta3;
    }

    public String getRisposta4() {
        return risposta4;
    }

    public int getIdParDestinazione1() {
        return idParDestinazione1;
    }

    public int getIdParDestinazione2() {
        return idParDestinazione2;
    }

    public int getIdParDestinazione3() {
        return idParDestinazione3;
    }

    public int getIdParDestinazione4() {
        return idParDestinazione4;
    }

    public int getEsitoRisp1() {
        return esitoRisp1;
    }

    public int getEsitoRisp2() {
        return esitoRisp2;
    }

    public int getEsitoRisp3() {
        return esitoRisp3;
    }

    public int getEsitoRisp4() {
        return esitoRisp4;
    }
}