import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BankOfCanadaExchangeRate {

    private static final String[] SERIES_NAMES = { "FXCADUSD", "FXAUDCAD"};
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Main method
     * @param args Command line arguments
     * @throws IOException if an I/O error occurs
     * @throws CsvValidationException if an error occurs while parsing CSV
     * @throws ParseException if an error occurs while parsing date
     */
    public static void main(String[] args) throws IOException, CsvValidationException, ParseException {

        // Get formatted command line arguments

        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
        List<String> seriesNames = new ArrayList<>();

        if (args.length > 0){
            startDate = LocalDate.parse(args[0], DateTimeFormatter.ofPattern(DATE_FORMAT));
            if (args.length > 1){
                endDate = LocalDate.parse(args[1], DateTimeFormatter.ofPattern(DATE_FORMAT));
            }
            if (args.length > 2){
                for (int i = 2; i < args.length; i++){
                    if (seriesNames.size() >= 4){
                        System.err.println("Only 4 series names are allowed");
                        System.exit(1);
                    }
                    seriesNames.add(args[i]);
                }
            }
        } else {
            // Use default values
            startDate = endDate.minusDays(7);
            seriesNames.addAll(List.of(SERIES_NAMES));
        }

        BankOfCanadaApiClient apiClient = new BankOfCanadaApiClient();
        String response = apiClient.getExchangeRate(seriesNames.toArray(new String[0]), startDate.toString(), endDate.toString());

//        parsing Data
        ExchangeRate exchangeRate = new ExchangeRate();
        Map<String, List<Map<String, String>>> groupedData = exchangeRate.parseData(response);
        Map<String, List<List<String>>> formattedData = exchangeRate.convertToRows(groupedData);
        for (String seriesName : seriesNames) {
            System.out.println(seriesName);
            for (List<String> row : formattedData.get(seriesName)) {
                System.out.println(String.join(", ", row));
            }
        }
        exchangeRate.writeDataToCSV(formattedData, "exchange_rate.csv");

    }
}
