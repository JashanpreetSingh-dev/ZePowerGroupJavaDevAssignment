import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.time.LocalDate;
import java.util.*;


public class ExchangeRate {

    /**
     * Parse JSON data
     * @param jsonData JSON data
     * @return Map of series name and list of data
     */
    public Map<String, List<Map<String, String>>> parseData(String jsonData) {
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

        Map<String, List<Map<String, String>>> groupedData = new LinkedHashMap<>();

        jsonObject.getAsJsonArray("observations").forEach(observation -> {
            String date = observation.getAsJsonObject().get("d").getAsString();
            observation.getAsJsonObject().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("FX"))
                    .forEach(entry -> {
                        String seriesName = entry.getKey();
                        JsonObject seriesDetail = jsonObject.getAsJsonObject("seriesDetail").getAsJsonObject(seriesName);
                        String seriesLabel = seriesDetail.get("label").getAsString();
                        String seriesDescription = seriesDetail.get("description").getAsString();
                        String exchangeRate = entry.getValue().getAsJsonObject().get("v").getAsString();

                        Map<String, String> data = new LinkedHashMap<>();
                        data.put("Date", date);
                        data.put("Series Name", seriesName);
                        data.put("Label", seriesLabel);
                        data.put("Description", seriesDescription);
                        data.put("Value", exchangeRate);

                        groupedData.computeIfAbsent(seriesName, k -> new ArrayList<>()).add(data);
                    });
        });

        return groupedData;
    }

    /**
     * Convert parsed data to rows
     * @param parsedData Parsed data
     * @return Map of series name and list of rows
     */
    public Map<String, List<List<String>>> convertToRows(Map<String, List<Map<String, String>>> parsedData) {
        Map<String, List<List<String>>> rowsData = new LinkedHashMap<>();

        parsedData.forEach((seriesName, seriesData) -> {
            List<List<String>> rows = new ArrayList<>();
            seriesData.forEach(data -> {
                List<String> row = new ArrayList<>();
                row.add(data.get("Date"));
                row.add(data.get("Value"));
                row.add(data.get("Label"));
                row.add(data.get("Description"));
                row.add(data.get("Series Name"));
                rows.add(row);
            });
            rowsData.put(seriesName, rows);
        });

        return rowsData;
    }

    /**
     * Format data
     * @param formattedData Rows data
     * @param fileName File name
     * @return writes data to csv file
     */
    public void writeDataToCSV(Map<String, List<List<String>>> formattedData, String fileName) {
        try {
            // Check if file already exists
            File file = new File(fileName);
            boolean fileExists = file.exists();

            // Create FileWriter object with append mode true

            FileWriter csvWriter = new FileWriter(fileName, true);

            // Write headers if file doesn't exist
            if (!fileExists) {
                csvWriter.append("Date,Value,Label,Description,SeriesName,Change\n");
            }

            // Loop through each series
            for (Map.Entry<String, List<List<String>>> entry : formattedData.entrySet()) {
                String seriesName = entry.getKey();
                List<List<String>> rows = entry.getValue();

                // Sort rows in descending order by date
                rows.sort(Comparator.comparing((List<String> row) -> LocalDate.parse(row.get(0))).reversed());

                // Loop through each row in the series
                for (int i = 0; i < rows.size(); i++) {
                    List<String> row = rows.get(i);

                    // Calculate change column
                    if (i == 0) {
                        row.add("0.00%");
                    } else {
                        double prevValue = Double.parseDouble(rows.get(i - 1).get(1));
                        double currValue = Double.parseDouble(row.get(1));
                        double change = (currValue - prevValue) / prevValue * 100;
                        row.add(String.format("%.2f%%", change));
                    }

                    // Check if row already exists in file
                    if (fileExists) {
                        BufferedReader reader = new BufferedReader(new FileReader(fileName));
                        String line;
                        boolean rowExists = false;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith(row.get(0) + "," + row.get(1) + "," + row.get(2) + "," + row.get(3) + "," + row.get(4))) {
                                rowExists = true;
                                break;
                            }
                        }
                        reader.close();
                        if (rowExists) {
                            continue;
                        }
                    }

                    // Write row to file
                    csvWriter.append(row.get(0)).append(",").append(row.get(1)).append(",").append(row.get(2)).append(",").append(row.get(3)).append(",").append(row.get(4)).append(",").append(row.get(5)).append("\n");
                }
            }

            // Close FileWriter object
            csvWriter.flush();
            csvWriter.close();

            System.out.println("Data has been written to CSV file.");
            System.out.println("File path: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("An error occurred while writing data to CSV file: " + e.getMessage());
        }
    }

}


