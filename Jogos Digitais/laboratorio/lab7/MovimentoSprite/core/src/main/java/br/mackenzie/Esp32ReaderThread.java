package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;

public class Esp32ReaderThread extends Thread {
    
    private volatile String lastData = "0"; 
    private SerialPort esp32Port;

    public Esp32ReaderThread(String portName) {
        this.esp32Port = SerialPort.getCommPort(portName);
    }

    @Override
    public void run() {
        if (!esp32Port.openPort()) {
            System.err.println("Erro: Não foi possível abrir a porta serial " + esp32Port.getSystemPortName());
            return;
        }

        esp32Port.setBaudRate(9600);
        esp32Port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0); 
        
        InputStream in = esp32Port.getInputStream();
        StringBuilder currentLine = new StringBuilder();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (in.available() > 0) {
                    char character = (char) in.read();
                    currentLine.append(character);

                    if (character == '\n') {
                        lastData = currentLine.toString().trim();
                        currentLine.setLength(0);
                    }
                }
                Thread.sleep(1);
            }
        } catch (Exception e) {
            System.err.println("Erro na leitura serial: " + e.getMessage());
        } finally {
            if (esp32Port.isOpen()) {
                esp32Port.closePort();
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