package org.example.regulator;

import java.util.List;

public interface Regulator {
    /**
     * Метод управления регулятором
     * @param operation операция, что требуется сделать. Действия задаются через биты двоичного числа
     * @param inData значение температуры, которое нужно записать
     * @param outData список, в которых требуется записать выходные данные
     * @param offsetOut смещение индекса в списке значений температуры, чтобы получать не весь список
     * @return код результата
     */
    int adjustTemp(byte operation, float inData, List<Float> outData, int offsetOut);
}
