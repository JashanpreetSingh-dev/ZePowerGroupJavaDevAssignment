import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BankOfCanadaApiClient {

    public static final String API_URL = "https://www.bankofcanada.ca/valet/observations/";
    public String seriesName = "";

    public String getExchangeRate(String[] seriesName, String startDate, String endDate) throws IOException {

        for (String s : seriesName) {
            this.seriesName = this.seriesName.concat(s + ",");
        }
        this.seriesName = this.seriesName.substring(0, this.seriesName.length() - 1);

        URL url = new URL(API_URL + this.seriesName + "/json?start_date=" + startDate + "&end_date=" + endDate);
        return getString(url);
    }

    public String getSeriesGroup() throws IOException {
        URL url = new URL(API_URL + "group/FX_RATES_DAILY/json");
        return getString(url);
    }

    /**
     * Get string from URL
     * @param url URL
     * @return String
     * @throws IOException if an I/O error occurs
     */
    private String getString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String output;
        StringBuilder response = new StringBuilder();
        while ((output = br.readLine()) != null) {
            response.append(output);
        }
        connection.disconnect();
        return response.toString();
    }

}