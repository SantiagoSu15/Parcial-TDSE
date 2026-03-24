package org.example;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class proxyService {

    private final Verificador verificador;

    public proxyService(Verificador verificador) {
        this.verificador = verificador;
    }

    public String llamarActivo(String path) {
        String baseUrl = verificador.getMasterUrl();
        String targetUrl = baseUrl + path;

        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream())
                );

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                return response.toString();
            } else {
                return "Error: " + responseCode;
            }

        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
}

