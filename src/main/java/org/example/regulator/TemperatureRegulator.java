package org.example.regulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Регулятор температуры
 */
public class TemperatureRegulator implements Regulator {

    private static TemperatureRegulator instance = null;

    private static final List<Float> temperatures = new ArrayList<>();

    private static final int MAX_COUNT_FOR_GET = 4;
    private static final int MAX_NUMBER = 3; // Максимальное количество значений, которое можно задать
    // между последним значением температуры и заданным значением


    private final Random random = new Random();

    private TemperatureRegulator() {
    }

    public static synchronized Regulator getInstance() {
        if (instance == null) {
            instance = new TemperatureRegulator();
            return instance;
        }

        return instance;
    }

    public static synchronized void removeInstance() {
        instance = null;
    }

    @Override
    public int adjustTemp(byte operation, float inData, List<Float> outData, int offsetOut) {
        if (operation == 0) {
            return 3; //Некорректна указана операция
        }
        if (outData == null) {
            return 2; // Передан неинициализированный список
        }

        byte operationWithReservedBit = (byte) (operation | (byte) 1);

        if (operationWithReservedBit != operation) {
            return 1; //8-ой бит зарезервирован и должен быть всегда 1
        }

        String operationStr = Integer.toBinaryString(operation & 0xFF);
        operationStr = adjustOperation(operationStr);

        byte count = Byte.parseByte(operationStr.substring(3, 7), 2); // Количество запрашиваемых данных
        // Вычитаем минус 1 потому что 16 занимает 6 бит, а не 4 бита
        if (count > (Math.pow(2, MAX_COUNT_FOR_GET) - 1)) {
            return 4; //Превышено значения количества данных, которые требуется получить в параметре outData
        }

        char[] bits = operationStr.toCharArray();

        // Если 1-ый бит равен 1, то следует очистить список
        if (bits[0] == '1') {
            temperatures.clear();
        }
        // Если 2-ой бит равен 1, то следует добавить значение температуры в список
        if (bits[1] == '1') {
            addRandomValues(inData);

            temperatures.add(inData);
        }
        // Если 3-ий бит равен 1, то получать температуру обратно
        if (bits[2] == '1') {
            if (count > 0) {
                outData.addAll(temperatures.subList(offsetOut, temperatures.size()));
            }
            if (count == 0) {
                outData.addAll(temperatures);
            }

            // Если количество получаемых значений не задано и смещение не задано,
            // то получаем последнее значение температуры
            if (count == 0 && offsetOut == -1) {
                outData.add(temperatures.getLast());
            }


        }

        return 0;
    }

    /**
     * Метод для добавления случайных значений между последним значением и заданным значением температуры
     *
     * @param inData заданное значение температуры
     */
    private void addRandomValues(float inData) {
        int i = 0;
        while (i < MAX_NUMBER) {
            temperatures.add((float) (random.nextInt(10) + i));

            i++;
        }
    }

    /**
     * Метод для корректировки двоичного числа.
     * Если двоичное число поступило с размером меньше 8 бит, то добавить в начале нулей
     *
     * @param operation операция описанная в двоичной системе
     */
    private String adjustOperation(String operation) {
        if (operation.length() < 8) {
            int length = operation.length();
            StringBuilder operationBuilder = new StringBuilder(operation);
            while (length != 8) {
                operationBuilder.insert(0, "0");

                length++;
            }
            return operationBuilder.toString();
        }

        return operation;
    }
}
