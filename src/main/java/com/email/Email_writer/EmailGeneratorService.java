package com.email.Email_writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${geminiApiUrl}")
    private String geminiApiUrl;
    @Value("${geminiApiKey}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientbuilder) {
        this.webClient = webClientbuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest){
        // build a prompt
        //craft a request
        //Do request and Response
        //return response

        //1)build a request
        String prompt = buildPrompt(emailRequest);

        //2)Craft Request
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of(
                                "parts", new Object[] {
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        //3)Do request and response
        String response= webClient.post()
                .uri(geminiApiUrl+geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractResponseContent(response);


    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper objectMapper=new ObjectMapper();

            JsonNode rootNode= objectMapper.readTree(response);

            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();


        } catch (Exception e) {
            return "Error Processing request : "+ e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email-content , you just need to give the reply in the email format , also do what is said , dont say here is the reponse i have generated , just give me the final mail reply");

        if(emailRequest.getTone()!= null && !emailRequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone");
        }

        prompt.append("/n Give me the perfect and a crisp reply/n");

        prompt.append("/n Original-Email/n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
