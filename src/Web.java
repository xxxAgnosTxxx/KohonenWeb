import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Web {
    private static final int INPUT_FACTS_COUNT = 10;
    private static final int OUTPUT_NEURONS_COUNT = 5;
    private static final File WEIGHTS_CONTAINER = new File("weights.csv");
    private static float[][] arrWeights = new float[INPUT_FACTS_COUNT][OUTPUT_NEURONS_COUNT];
    //входные показатели
    private static float[] inputFacts;
    //константы для скорости обучения
    private static final float A = (float) 0.03;
    private static final float B = (float) 0.001;
    //хранит индекс последнего нейрона-победителя
    private static int indexWinnerNeuron = 0;
    //потенциалы нейронов
    private static final float START_POTENTIAL = (float) 1 / OUTPUT_NEURONS_COUNT;
    private static float[] potentialsNeuron = new float[]{
            START_POTENTIAL, START_POTENTIAL, START_POTENTIAL, START_POTENTIAL, START_POTENTIAL
    };

    public Web() {
        //рандомные значения весов
/*
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(WEIGHTS_CONTAINER));
            for (int i = 0; i < INPUT_FACTS_COUNT; i++) {
                String[] stringWeights = new String[OUTPUT_NEURONS_COUNT];
                for (int j = 0; j < OUTPUT_NEURONS_COUNT; j++) {
                    stringWeights[j] = String.valueOf(Math.random()*0.1+0.1);
                }
                writer.writeNext(stringWeights);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("File not found.");
        }*/
        //инициализация массива весов
        try {
            CSVReader reader = new CSVReader(new FileReader(WEIGHTS_CONTAINER));
            for (int i = 0; i < INPUT_FACTS_COUNT; i++) {
                String[] stringWeights = reader.readNext();
                for (int j = 0; j < OUTPUT_NEURONS_COUNT; j++) {
                    arrWeights[i][j] = Float.parseFloat(stringWeights[j]);
                }
            }
            reader.close();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File not found.");
        }
    }

    //нормализация входных значений и запись в массив
    private void normalizeInputData(int[] input) {
        inputFacts = new float[input.length];
        int[][] normalData = DataExamples.getNormalizeData();
        for (int i = 0; i < input.length; i++) {
            inputFacts[i] = normalize(input[i], normalData[i][1], normalData[i][0]);
        }
    }

    //подсчет расстояний
    private float[] countDistances() {
        float[] distArr = new float[OUTPUT_NEURONS_COUNT];
        for (int i = 0; i < OUTPUT_NEURONS_COUNT; i++) {
            distArr[i] = getDistance(i);
        }
        return distArr;
    }

    public int start(int[] input) {
        normalizeInputData(input);
        return getMinDistanceIndex(countDistances());
    }

    //нормализация входных значений
    private float normalize(float x, float max, float min) {
        return (x - min) / (max - min);
    }

    //определение расстояния
    private float getDistance(/*номер нейрона*/int n) {
        float dist = 0;
        for (int i = 0; i < INPUT_FACTS_COUNT; i++) {
            dist += Math.pow(inputFacts[i] - arrWeights[i][n], 2);
        }
        dist = (float) Math.sqrt(dist);
        return dist;
    }

    //получение индекса мнимального расстояния
    private int getMinDistanceIndex(float[] distances) {
        int maxIndex = 0;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < distances[maxIndex]) maxIndex = i;
        }
        return maxIndex;
    }

    //Гауссово значение
    private float getGaussCount(int i, float dist) {
        return (float) Math.pow(Math.E, -dist / 2 * 1 * i);
    }

    //получение коэффициента скорости обучения
    private float getCoefficientSpeed(int i, float dist) {
        return getGaussCount(i, dist) * A / (i + B);
    }

    //корректировка веса по Кохонену
    private float setWeight(int input, float weight, int i, float dist) {
        return weight + getCoefficientSpeed(i, dist) * (input - weight);
    }

    public void learn(int[][] input) {
        for (int i = 0; i < input.length; i++) {
            learn(input[i]);
        }
    }

    private void learn(int[] input) {
        normalizeInputData(input);
        float[] dist = countDistances();
        float radius = (float) 0.1;
        indexWinnerNeuron = getMinDistanceIndex(dist);
        //проверка победителя на утомляемость
        float winPotential = getPotentialValue(indexWinnerNeuron);
        if (winPotential < getMinPotential()) {
            indexWinnerNeuron = getReplaceWinner(indexWinnerNeuron);
        }
        //постепенное уменьшение радиуса
        while (radius >= 0) {
            //проход по каждому нейрону
            for (int i = 0; i < OUTPUT_NEURONS_COUNT; i++) {
                //если расстояние до нейрона-победителя менее радиуса
                if (dist[indexWinnerNeuron] + radius >= dist[i] && potentialsNeuron[i] >= getMinPotential()) {
                    //проход по весам нейрона
                    for (int j = 0; j < INPUT_FACTS_COUNT; j++) {
                        //цикл обучения
                        for (int k = 1; k < 11; k++) {
                            //корректировка веса
                            arrWeights[j][i] = setWeight(input[j], arrWeights[j][i], k, dist[i]);
                        }
                    }
                }
            }
            radius -= 0.02;
        }
        replacePotentialValue(indexWinnerNeuron);
        writeWeightsToCSV();
    }

    //пересчет потенциалов
    private void replacePotentialValue(int ixWinner) {
        float minPotential = getMinPotential();
        for (int i = 0; i < potentialsNeuron.length; i++) {
            float pNeuronTemp = potentialsNeuron[i];
            if (ixWinner == i) {
                pNeuronTemp -= minPotential;
            } else {
                pNeuronTemp += START_POTENTIAL;
            }

            if (pNeuronTemp > 1) potentialsNeuron[i] = 1;
            else if (pNeuronTemp < 0) potentialsNeuron[i] = 0;
        }
    }

    private float getPotentialValue(int ix) {
        return potentialsNeuron[ix];
    }

    private float getMinPotential() {
        float minP = potentialsNeuron[0];
        for (int i = 0; i < potentialsNeuron.length; i++) {
            if (potentialsNeuron[i] < minP) minP = potentialsNeuron[i];
        }
        return minP;
    }

    //перерасчет победителя
    private int getReplaceWinner(int lastWinnerIndex) {
        float[] distances = countDistances();
        int ixWin = lastWinnerIndex;
        //массив расстояний без учета расстояния бывшего победителя
        for (int i = 0; i < distances.length; i++) {
            if (i == lastWinnerIndex) continue;
            if (ixWin == lastWinnerIndex) ixWin = i;
            if (distances[i] < distances[ixWin]) ixWin = i;
        }
        return ixWin;
    }

    //запись в файл
    private void writeWeightsToCSV() {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(WEIGHTS_CONTAINER));
            for (int i = 0; i < INPUT_FACTS_COUNT; i++) {
                String[] strings = new String[OUTPUT_NEURONS_COUNT];
                for (int j = 0; j < OUTPUT_NEURONS_COUNT; j++) {
                    strings[j] = String.valueOf(arrWeights[i][j]);
                }
                writer.writeNext(strings);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("File not found.");
        }
    }
}
