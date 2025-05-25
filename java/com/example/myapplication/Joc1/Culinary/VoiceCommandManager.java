package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manager pentru comenzi vocale în ghidul pas cu pas de gătit
 */
public class VoiceCommandManager {

    private final Context context;
    private final CommandListener listener;
    private final TextView statusText;
    
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;
    
    // Mapare între comenzi vocale și acțiuni
    private final Map<String, List<String>> commandPhrases = new HashMap<>();
    
    /**
     * Interfață pentru ascultarea comenzilor vocale recunoscute
     */
    public interface CommandListener {
        void onNextStepCommand();
        void onPreviousStepCommand();
        void onCompleteStepCommand();
        void onStartTimerCommand();
        void onStopTimerCommand();
        void onResetTimerCommand();
        void onPlayVideoCommand();
        void onVoiceCommandNotRecognized(String command);
    }
    
    public VoiceCommandManager(Context context, CommandListener listener, TextView statusText) {
        this.context = context;
        this.listener = listener;
        this.statusText = statusText;
        
        initializeCommandPhrases();
    }
    
    /**
     * Inițializează expresiile pentru comenzi vocale
     */
    private void initializeCommandPhrases() {
        // Comanda "următorul pas"
        commandPhrases.put("next", Arrays.asList(
                "următorul pas", "pas următor", "înainte", "următorul", "next", "continuă", "mai departe"
        ));
        
        // Comanda "pasul anterior"
        commandPhrases.put("previous", Arrays.asList(
                "pasul anterior", "pas înapoi", "înapoi", "anterior", "previous", "back"
        ));
        
        // Comanda "finalizează pasul"
        commandPhrases.put("complete", Arrays.asList(
                "finalizează pasul", "pas terminat", "pas finalizat", "complete", "gata", "am terminat", "marcare finalizat"
        ));
        
        // Comanda "pornește cronometrul"
        commandPhrases.put("start_timer", Arrays.asList(
                "pornește cronometrul", "start cronometru", "start timer", "pornește timer", "start", "pornește"
        ));
        
        // Comanda "oprește cronometrul"
        commandPhrases.put("stop_timer", Arrays.asList(
                "oprește cronometrul", "stop cronometru", "stop timer", "oprește timer", "stop", "oprește", "pauză"
        ));
        
        // Comanda "resetează cronometrul"
        commandPhrases.put("reset_timer", Arrays.asList(
                "resetează cronometrul", "reset cronometru", "reset timer", "resetează timer", "reset", "resetează", "de la început"
        ));
        
        // Comanda "arată video"
        commandPhrases.put("play_video", Arrays.asList(
                "arată video", "redă video", "video tutorial", "deschide video", "vreau să văd", "arată-mi cum"
        ));
    }
    
    /**
     * Inițializează și configurează recunoașterea vocală
     */
    public void initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            setStatus("Recunoașterea vocală nu este disponibilă pe acest dispozitiv");
            return;
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                setStatus("Ascultă comenzi...");
            }

            @Override
            public void onBeginningOfSpeech() {
                setStatus("Se procesează comanda...");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Actualizare nivel audio (opțional)
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Buffer audio primit
            }

            @Override
            public void onEndOfSpeech() {
                // S-a terminat de vorbit, așteptăm rezultatul
            }

            @Override
            public void onError(int error) {
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "Eroare de rețea";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "Timeout rețea";
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Eroare audio";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorMessage = "Eroare server";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "Eroare client";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "Nu s-a detectat voce";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "Nu s-a recunoscut comanda";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "Serviciul este ocupat";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "Permisiuni insuficiente";
                        break;
                    default:
                        errorMessage = "Eroare necunoscută";
                        break;
                }
                setStatus("Eroare: " + errorMessage);
                
                // Reluăm ascultarea dacă este activă
                if (isListening) {
                    startListening();
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0).toLowerCase();
                    processCommand(command);
                }
                
                // Reluăm ascultarea dacă este activă
                if (isListening) {
                    startListening();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Rezultate parțiale (opțional)
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Evenimente (opțional)
            }
        });
    }
    
    /**
     * Procesează comanda vocală recunoscută
     */
    private void processCommand(String command) {
        setStatus("Comandă: " + command);
        
        // Verifică fiecare grup de comenzi
        for (Map.Entry<String, List<String>> entry : commandPhrases.entrySet()) {
            String commandType = entry.getKey();
            List<String> phrases = entry.getValue();
            
            // Verifică dacă comanda conține vreuna din expresiile acceptate
            for (String phrase : phrases) {
                if (command.contains(phrase)) {
                    executeCommand(commandType);
                    return;
                }
            }
        }
        
        // Comanda nu a fost recunoscută
        setStatus("Comandă nerecunoscută. Încearcă din nou.");
        if (listener != null) {
            listener.onVoiceCommandNotRecognized(command);
        }
    }
    
    /**
     * Execută acțiunea corespunzătoare comenzii
     */
    private void executeCommand(String commandType) {
        if (listener == null) return;
        
        switch (commandType) {
            case "next":
                listener.onNextStepCommand();
                break;
            case "previous":
                listener.onPreviousStepCommand();
                break;
            case "complete":
                listener.onCompleteStepCommand();
                break;
            case "start_timer":
                listener.onStartTimerCommand();
                break;
            case "stop_timer":
                listener.onStopTimerCommand();
                break;
            case "reset_timer":
                listener.onResetTimerCommand();
                break;
            case "play_video":
                listener.onPlayVideoCommand();
                break;
        }
    }
    
    /**
     * Pornește sau oprește ascultarea comenzilor vocale
     */
    public void toggleListening(boolean start) {
        if (speechRecognizer == null) {
            initialize();
            if (speechRecognizer == null) return; // Inițializarea a eșuat
        }
        
        if (start && !isListening) {
            startListening();
        } else if (!start && isListening) {
            stopListening();
        }
    }
    
    /**
     * Pornește ascultarea comenzilor vocale
     */
    private void startListening() {
        try {
            speechRecognizer.startListening(speechRecognizerIntent);
            isListening = true;
            setStatus("Ascultă comenzi...");
        } catch (Exception e) {
            setStatus("Eroare la pornirea recunoașterii vocale");
            isListening = false;
        }
    }
    
    /**
     * Oprește ascultarea comenzilor vocale
     */
    private void stopListening() {
        try {
            speechRecognizer.stopListening();
            isListening = false;
            setStatus("Recunoaștere vocală dezactivată");
        } catch (Exception e) {
            setStatus("Eroare la oprirea recunoașterii vocale");
        }
    }
    
    /**
     * Actualizează textul de status al comenzilor vocale
     */
    private void setStatus(String status) {
        if (statusText != null) {
            statusText.setText(status);
        }
    }
    
    /**
     * Eliberează resursele
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        isListening = false;
    }
} 