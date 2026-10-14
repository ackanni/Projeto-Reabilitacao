package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;

public class ArduinoReaderThread extends Thread {
    
    // A palavra-chave 'volatile' garante que o valor seja visível
    // para todas as threads, garantindo a comunicação segura.
    private volatile String lastData = "Nenhum dado";
    private SerialPort arduinoPort;

    public ArduinoReaderThread(String portName) {
        this.arduinoPort = SerialPort.getCommPort(portName);
    }

    @Override
    public void run() {
        if (!arduinoPort.openPort()) {
            System.err.println("Erro: Não foi possível abrir a porta serial.");
            return;
        }

        arduinoPort.setBaudRate(9600);
        InputStream in = arduinoPort.getInputStream();
        StringBuilder currentLine = new StringBuilder();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (in.available() > 0) {
                    char character = (char) in.read();
                    currentLine.append(character);

                    if (character == '\n') {
                        // Armazena a linha completa no shared variable
                        lastData = currentLine.toString().trim();
                        currentLine.setLength(0);
                    }
                }
                Thread.sleep(1);
            }
        } catch (Exception e) {
            System.err.println("Erro na leitura serial: " + e.getMessage());
        } finally {
            if (arduinoPort.isOpen()) {
                arduinoPort.closePort();
                System.out.println("Porta serial fechada.");
            }
        }
    }

    public String getLastData() {
        return lastData;
    }

    public void stopReading() {
        this.interrupt();
    }
}