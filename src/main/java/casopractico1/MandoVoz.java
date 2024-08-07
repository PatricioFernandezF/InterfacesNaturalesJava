package casopractico1;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.LogLevel;

import javax.sound.sampled.*;
import java.io.IOException;

public class MandoVoz {

    private Recognizer recognizer;
    private Model model; // Keep a reference to the Model
    private MandoTelevision mando; // Instanciar MandoTelevision aquí
    private String lastCommand = ""; // Guardar el último comando procesado

    public MandoVoz(Model model) throws IOException {
        this.model = model; // Initialize the model
        this.recognizer = new Recognizer(model, 16000);
        this.mando = new MandoTelevision(); // Inicializar aquí
    }

    public void procesarAudio() throws IOException, LineUnavailableException {
        // Configura el micrófono
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        TargetDataLine microphone;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Micrófono no soportado");
            return;
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        // Buffer para leer el audio
        byte[] buffer = new byte[4096];
        int bytesRead;

        // Procesa el audio en tiempo real
        try {
            System.out.println("Comenzando a procesar audio...");
            while (true) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    recognizer.acceptWaveForm(buffer, bytesRead);
                    String resultado = recognizer.getPartialResult();

                    // Imprime el resultado en la consola
                    //System.out.println("Resultado parcial: " + resultado);

                    // Procesa el resultado en busca de comandos
                    if (resultado.contains("subir volumen")) {
                        System.out.println("Comando recibido: Subir volumen");
                        mando.subirVolumen(); // Usar la instancia existente
                        System.out.println("Volumen actual: " + mando.obtenerVolumen());
                        lastCommand = "subir volumen"; // Actualizar el último comando procesado
                        resetRecognizer(); // Limpiar el buffer
                    } else if (resultado.contains("bajar volumen") ) {
                        System.out.println("Comando recibido: Bajar volumen");
                        mando.bajarVolumen(); // Usar la instancia existente
                        System.out.println("Volumen actual: " + mando.obtenerVolumen());
                        lastCommand = "bajar volumen"; // Actualizar el último comando procesado
                        resetRecognizer(); // Limpiar el buffer
                    } else if (resultado.isEmpty()) {
                        // Si el resultado está vacío, limpiar el último comando procesado
                        lastCommand = "";
                    }
                }
            }
        } finally {
            microphone.stop();
            microphone.close();
        }
    }

    private void resetRecognizer() throws IOException {
        // Reinicia el reconocedor para limpiar el buffer
        recognizer = new Recognizer(model, 16000);
        // Optional: Sleep a bit to ensure buffer clearing
        try {
            Thread.sleep(500); // Adjust the sleep time if needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws LineUnavailableException {
        // Inicializa la biblioteca de Vosk
        LibVosk.setLogLevel(LogLevel.INFO);

        // Especifica la ruta del modelo
        String modeloRuta = "C:\\Users\\Patricio\\Downloads\\vosk-model-small-es-0.42\\vosk-model-small-es-0.42"; // Cambia esta ruta a la ubicación del modelo en tu sistema

        try (Model model = new Model(modeloRuta)) {
        	MandoVoz transcripcion = new MandoVoz(model);
            transcripcion.procesarAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
