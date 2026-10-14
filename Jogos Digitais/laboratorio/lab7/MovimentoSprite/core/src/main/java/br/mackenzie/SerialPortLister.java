package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortLister {
    public static void main(String[] args) {
        listSerialPorts();
    }

    /**
     * Lista todas as portas seriais disponíveis no sistema.
     */
    public static void listSerialPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("Nenhuma porta serial encontrada.");
        } else {
            System.out.println("Portas seriais disponíveis:");
            for (SerialPort port : ports) {
                System.out.println("- " + port.getSystemPortName() + " (" + port.getDescriptivePortName() + ")");
            }
        }
    }
}
