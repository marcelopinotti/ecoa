package com.marcelodev.ecoa.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.ai.chat.client.ChatClient;
import com.marcelodev.ecoa.expense.ExpenseTools;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AudioConversationService {

    private static final Set<String> SUPPORTED_AUDIO_TYPES = Set.of(
            "audio/aac", "audio/flac", "audio/mpeg", "audio/mp4", "audio/ogg", "audio/wav", "audio/x-wav");

    private static final String INSTRUCTION = """
            Você é Ecoa, assistente financeiro por voz. Ouça o áudio em português brasileiro.
            Para um novo gasto, chame createExpenseDraft com valor, descrição e categoria, depois peça confirmação.
            Só chame confirmLatestExpense quando usuário confirmar claramente gasto pendente.
            Para consultas por categoria, chame listExpensesByCategory.
            Nunca invente valor ou confirme gasto sem pedido explícito.
            Responda de forma curta, clara e em português brasileiro.
            """;

    private final ChatClient chatClient;
    private final MediaProperties mediaProperties;

    public AudioConversationService(ChatClient.Builder chatClientBuilder, MediaProperties mediaProperties,
            ExpenseTools expenseTools) {
        this.chatClient = chatClientBuilder.defaultSystem(INSTRUCTION).defaultTools(expenseTools).build();
        this.mediaProperties = mediaProperties;
    }

    public Path answer(MultipartFile file) {
        var contentType = validate(file);

        try {
            var input = store(file, "entrada", extension(contentType));
            var reply = chatClient.prompt()
                    .user(user -> user.text("Analise este áudio seguindo as instruções do sistema.")
                            .media(MimeType.valueOf(contentType), new FileSystemResource(input)))
                    .call()
                    .content();

            if (!StringUtils.hasText(reply)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini não retornou resposta");
            }

            var output = mediaProperties.directory().resolve("saida").resolve(UUID.randomUUID() + ".wav");
            Files.createDirectories(output.getParent());
            synthesize(reply, output);
            return output;
        }
        catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar áudio", exception);
        }
    }

    private String validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo de áudio");
        }

        var contentType = file.getContentType();
        if (!SUPPORTED_AUDIO_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de áudio não suportado");
        }
        return contentType;
    }

    private Path store(MultipartFile file, String folder, String extension) throws IOException {
        var directory = mediaProperties.directory().resolve(folder);
        Files.createDirectories(directory);
        var target = directory.resolve(UUID.randomUUID() + extension);

        try (var input = file.getInputStream()) {
            Files.copy(input, target);
        }

        return target;
    }

    private void synthesize(String text, Path output) throws IOException {
        Process process;
        try {
            process = new ProcessBuilder(mediaProperties.espeakCommand(), "-v", "pt-br", "--stdout", text)
                    .redirectOutput(output.toFile())
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        }
        catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "espeak-ng não encontrado; instale-o e deixe-o no PATH", exception);
        }

        try {
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "espeak-ng demorou demais");
            }
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Geração de voz interrompida", exception);
        }

        if (process.exitValue() != 0 || Files.size(output) == 0) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "espeak-ng não gerou áudio");
        }
    }

    private static String extension(String contentType) {
        return switch (contentType) {
            case "audio/aac" -> ".aac";
            case "audio/flac" -> ".flac";
            case "audio/mpeg" -> ".mp3";
            case "audio/mp4" -> ".m4a";
            case "audio/ogg" -> ".ogg";
            default -> ".wav";
        };
    }
}
