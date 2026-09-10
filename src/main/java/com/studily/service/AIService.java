package com.studily.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.studily.model.StudyKit;
import com.studily.util.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Groq API client. Sends study notes, receives a strict JSON document
 * (summary, flashcards, MCQs), parses it with Gson, and retries once on
 * transient failures.
 */
public final class AIService {

    private static final String API_URL = AppConfig.get("groq.url");
    private static final String API_KEY = AppConfig.get("groq.api.key");
    private static final String MODEL = AppConfig.get("groq.model", "llama-3.3-70b-versatile");
    private static final int TIMEOUT_SECONDS = AppConfig.getInt("groq.timeout.seconds", 45);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final Gson GSON = new Gson();

    private static final String SYSTEM_PROMPT = """
            You are Studily's study-material generator. You read student notes and return study material.
            Respond with ONLY a valid JSON object, no markdown fences, no commentary, matching exactly this schema:
            {
              "summary": {
                "summary": "one-paragraph concise summary of the notes",
                "key_concepts": ["concept", "..."],
                "definitions": ["term: meaning", "..."],
                "exam_tips": ["tip", "..."]
              },
              "flashcards": [ { "question": "...", "answer": "..." } ],
              "mcqs": [ {
                 "question": "...",
                 "option_a": "...", "option_b": "...", "option_c": "...", "option_d": "...",
                 "correct_answer": "A" | "B" | "C" | "D",
                 "explanation": "why this answer is correct",
                 "difficulty": "easy" | "medium" | "hard"
              } ]
            }
            Rules: 5-8 flashcards, 5-10 MCQs, all content grounded ONLY in the provided notes,
            all strings plain text (no markdown), every JSON string escaped properly.
            """;

    private AIService() {
    }

    /**
     * Calls Groq and returns the parsed structured payload.
     *
     * @throws AIServiceException on invalid key, rate limit, timeout, or malformed JSON
     */
    public static StudyKit.AIPayload generateStudyKit(String noteText) throws AIServiceException {
        String userPrompt = buildUserPrompt(noteText);
        String content = null;

        // Retry mechanism: one transient-failure retry
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                content = callGroq(userPrompt);
                break;
            } catch (IOException | InterruptedException e) {
                if (attempt == 2) {
                    Thread.currentThread().interrupt();
                    throw new AIServiceException("The AI service is unreachable right now. Please try again.");
                }
            }
        }

        return parsePayload(content);
    }

    private static String callGroq(String userPrompt) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);

        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", SYSTEM_PROMPT);
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userPrompt);
        messages.add(system);
        messages.add(user);

        body.add("messages", messages);
        body.addProperty("temperature", 0.4);
        body.addProperty("max_tokens", 4096);
        body.add("response_format", createJsonMode());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Invalid Groq API key.");
        }
        if (response.statusCode() == 429) {
            throw new IllegalStateException("AI rate limit reached. Please wait a moment and retry.");
        }
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Groq API error " + response.statusCode() + ".");
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        return root.getAsJsonArray("choices").get(0).getAsJsonObject()
                   .getAsJsonObject("message").get("content").getAsString();
    }

    private static JsonObject createJsonMode() {
        JsonObject jsonMode = new JsonObject();
        jsonMode.addProperty("type", "json_object");
        return jsonMode;
    }

    private static String buildUserPrompt(String noteText) {
        String clipped = noteText.length() > 12000
                ? noteText.substring(0, 12000) + "\n\n[Notes truncated for length]"
                : noteText;
        return """
                Generate study material from the following study notes.

                NOTES:
                %s

                Return ONLY the JSON object described in the system instructions.
                """.formatted(clipped);
    }

    /**
     * Parses the model output; tolerates accidental markdown fences and
     * trailing text by locating the outermost JSON object.
     */
    static StudyKit.AIPayload parsePayload(String content) throws AIServiceException {
        if (content == null || content.isBlank()) {
            throw new AIServiceException("The AI returned an empty response. Please retry.");
        }
        String json = stripFences(content);
        try {
            StudyKit.AIPayload payload = GSON.fromJson(json, StudyKit.AIPayload.class);
            if (payload == null || payload.getSummary() == null
                    || payload.getSummary().getSummary() == null
                    || payload.getFlashcards() == null || payload.getMcqs() == null
                    || payload.getFlashcards().isEmpty() || payload.getMcqs().isEmpty()) {
                throw new AIServiceException("The AI response was incomplete. Please retry.");
            }
            return payload;
        } catch (JsonSyntaxException e) {
            throw new AIServiceException("The AI returned malformed data. Please retry.");
        }
    }

    private static String stripFences(String content) {
        String s = content.trim();
        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            int lastFence = s.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                s = s.substring(firstNewline + 1, lastFence);
            }
        }
        int brace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (brace >= 0 && lastBrace > brace) {
            s = s.substring(brace, lastBrace + 1);
        }
        return s;
    }

    /** Domain exception surfaced to users as a friendly flash message. */
    public static class AIServiceException extends Exception {
        public AIServiceException(String message) {
            super(message);
        }
    }

    // ==================== v2.0 capabilities ====================

    /**
     * NotebookLM-style chat: answer strictly from the provided notes.
     * history alternates user/assistant turns (already oldest-first).
     */
    public static String chat(int userId, String noteTitle, String noteText,
                              List<String[]> history, String question) throws AIServiceException {
        String system = """
                You are Studily, an AI study tutor answering questions about a student's uploaded notes.
                Note title: "%s"
                RULES:
                - Answer ONLY from the NOTES below. If the answer is not in the notes, say so briefly and
                  offer the closest related concept that IS in the notes.
                - Be concise and clear; use short paragraphs or bullet points.
                - Plain text only: no markdown syntax, no asterisks, no heading marks.
                NOTES:
                %s
                """.formatted(noteTitle, clip(noteText, 9000));

        StringBuilder convo = new StringBuilder();
        for (String[] turn : history) {
            convo.append(turn[0].equals("user") ? "Student: " : "Tutor: ")
                 .append(clip(turn[1], 700)).append("\n");
        }
        convo.append("Student: ").append(clip(question, 1200));

        return complete(system, convo.toString());
    }

    /**
     * Generates a hierarchical mind map of the note.
     * Returns raw JSON: { "label": "...", "children": [ { "label": "...", "children": [] } ] }
     */
    public static String generateMindMap(String noteText) throws AIServiceException {
        String system = """
                You convert study notes into a hierarchical mind map. Respond with ONLY a JSON object:
                { "label": "central topic", "children": [ { "label": "branch", "children": [ { "label": "sub", "children": [] } ] } ] }
                Rules: 3-6 top-level branches, at most 2 levels deep, labels max 4 words, plain text,
                content grounded ONLY in the provided notes.
                NOTES:
                %s
                """.formatted(clip(noteText, 9000));
        return complete(system, "Generate the mind map JSON now.");
    }

    /**
     * Weak-topic detection: analyze wrong quiz answers, output JSON array of
     * { "topic": "...", "reason": "...", "noteId": 123 } entries (max 4).
     */
    public static String detectWeakTopics(String wrongAnswerDigest) throws AIServiceException {
        String system = """
                You analyze a student's wrong quiz answers to find weak topics. Respond with ONLY a JSON array:
                [ { "topic": "topic name", "reason": "one short sentence on what to review", "noteId": <the related note id as number> } ]
                Rules: max 4 items, most important first, topic = the specific concept (not the note title),
                if the list is empty return [].
                WRONG ANSWERS (question | chosen | correct | noteId):
                %s
                """.formatted(clip(wrongAnswerDigest, 6000));
        return complete(system, "Analyze now.");
    }

    /**
     * Personalized wrong-answer explanation: why THIS student fell for the
     * trap they chose, not just what the right answer is.
     */
    public static String explainMistake(String noteTitle, String noteText, String question,
                                        String chosenLetter, String chosenText, String correctLetter,
                                        String correctText, String aiExplanation) throws AIServiceException {
        String system = """
                You are a study coach. The student answered a quiz question wrong. Explain the mistake
                personally and briefly (2-4 sentences): why the option they picked is tempting, what
                misconception it reveals, and the key fact that gives the right answer.
                Address the student as "you". Plain text, no markdown.
                Question: %s
                Student chose: %s) %s
                Correct answer: %s) %s
                Reference explanation: %s
                NOTES (%s):
                %s
                """.formatted(clip(question, 400), chosenLetter, clip(chosenText, 200),
                              correctLetter, clip(correctText, 200), clip(aiExplanation, 300),
                              noteTitle, clip(noteText, 4000));
        return complete(system, "Explain this student's mistake now.");
    }

    /** Single-turn completion helper shared by the v2 features. */
    private static String complete(String systemPrompt, String userPrompt) throws AIServiceException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", MODEL);
            com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", systemPrompt);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", userPrompt);
            messages.add(system);
            messages.add(user);
            body.add("messages", messages);
            body.addProperty("temperature", 0.4);
            body.addProperty("max_tokens", 2048);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) throw new IllegalStateException("Invalid Groq API key.");
            if (response.statusCode() == 429) throw new IllegalStateException("AI rate limit reached. Please wait a moment and retry.");
            if (response.statusCode() >= 400) throw new IllegalStateException("Groq API error " + response.statusCode() + ".");

            return JsonParser.parseString(response.body()).getAsJsonObject()
                    .getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new AIServiceException("The AI service is unreachable right now. Please try again.");
        } catch (IllegalStateException e) {
            throw new AIServiceException(e.getMessage());
        }
    }

    private static String clip(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
