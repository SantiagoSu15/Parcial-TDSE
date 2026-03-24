package org.example;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class proxyService {

    private final Verificador verificador;

    public proxyService(Verificador verificador) {
        this.verificador = verificador;
    }


    public String llamarActivo(String path) {
        String baseUrl = verificador.getMasterUrl();
        String base = baseUrl.split(":8080")[0] + ":8080";
        String targetUrl = base + path;

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
