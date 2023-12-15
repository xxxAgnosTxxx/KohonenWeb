import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Mapper {
    private static final File WEIGHTS_CONTAINER = new File("weightsMap.csv");
    private static float[][] arrWeights = new float[10][100];
    private static int[][] map = new int[10][10];
    private static float[] inputData;

    public Mapper() {
/*
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(WEIGHTS_CONTAINER));
            for (int i = 0; i < 10; i++) {
                String[] stringWeights = new String[100];
                for (int j = 0; j < 100; j++) {
                    stringWeights[j] = String.valueOf(Math.random() * 0.1 + 0.1);
                }
                writer.writeNext(stringWeights);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("File not found.");
        }
*/
        try {
            CSVReader reader = new CSVReader(new FileReader(WEIGHTS_CONTAINER));
            for (int i = 0; i < 10; i++) {
                String[] stringWeights = reader.readNext();
                for (int j = 0; j < 100; j++) {
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

    private void normalizeInputData(int[] input) {
        inputData = new float[input.length];
        int[][] normalData = DataExamples.getNormalizeData();
        for (int i = 0; i < input.length; i++) {
            inputData[i] = normalize(input[i], normalData[i][1], normalData[i][0]);
        }
    }

    private float normalize(float x, float max, float min) {
        return (x - min) / (max - min);
    }

    public void start(int[] input) {
        normalizeInputData(input);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                float sum = 0;
                for (int k = 0; k < 10; k++) {
                    sum += arrWeights[k][i * 10 + j] * input[k];
                }
                map[i][j] = (int) sum;
            }
        }
        //printMap();
    }

    private float getDistance(float weight) {
        float dist = 0;
        for (float inputDatum : inputData) {
            dist += Math.pow(inputDatum - weight, 2);
        }
        return (float) Math.sqrt(dist);
    }

    private float getRadius(int winX, int x, int winY, int y) {
        double r = (Math.pow(x - winX, 2) + Math.pow(y - winY, 2));
        return (float) Math.sqrt(r);
    }

    public void learn(int[][] input) {
        for (int[] ints : input) {
            learn(ints);
        }
        printMap();
        editImg();
    }

    private void learn(int[] input) {
        int iteration = 1;
        start(input);

        while (iteration < 10) {
            int[] winIx = getWinnerIndexes();
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    float radius = getRadius(winIx[0], i, winIx[1], j);
                    float sigma = getSigma(iteration, j, i);
                    if (radius < sigma) {
                        for (int i1 = 0; i1 < inputData.length; i1++) {
                            arrWeights[i1][i * 10 + j] += getSpeed(iteration, j, i) * getRo(radius, sigma) * (input[i1] - map[i][j]);
                        }
                    }
                }
            }
            iteration++;
        }

        writeWeightsToCSV();
    }

    private float getSpeed(int iteration, int j, int i) {
        return (float) (0.3 * Math.exp(-iteration / getLambda(iteration, j, i)));
    }

    private float getRo(float radius, float sigma) {
        return (float) (-Math.pow(radius, 2) / (2 * Math.pow(sigma, 2)));
    }

    private int[] getWinnerIndexes() {
        float minD = getDistance(map[0][0]);
        int[] ixes = new int[]{0, 0};
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (minD > getDistance(map[i][j])) {
                    ixes[0] = i;
                    ixes[1] = j;
                }
            }
        }
        return ixes;
    }

    private float getSigma(int iteration, int j, int i) {
        return (float) (getSigmaZero(j, i) * Math.pow(Math.E, -iteration / getLambda(iteration, j, i)));
    }

    private float getLambda(int iteration, int j, int i) {
        return (float) (iteration / Math.log(getSigmaZero(j, i)));
    }

    private float getSigmaZero(int width, int height) {
        float sigma = Math.max(10 * width, 10 * height);
        return sigma / 2;
    }


    private void editImg() {
        File file = new File("E:/Java/KohonenWeb/img.jpg");
        try {
            BufferedImage img = ImageIO.read(file);
            BufferedImage i2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    int mapI = i / 100;
                    int mapJ = j / 100;
                    Color newColor = new Color(255 - map[mapI][mapJ] * 30, 255 - map[mapI][mapJ] * 30, 50);
                    i2.setRGB(i, j, newColor.getRGB());
                }
            }
            File out = new File("E:/Java/KohonenWeb/out" + System.currentTimeMillis() + ".jpg");
            ImageIO.write(i2, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printMap() {
        System.out.println("Карта Коххонена:");
        for (int[] arr : map) {
            Arrays.sort(arr);
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] < 5) arr[i] *= 0;
                else arr[i] -= 5;
            }
        }

        for (int[] floats : map) {
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < floats.length; i++) {
                temp.append(floats[i]).append(" ");/*
                if (i < 9 && floats[i] != floats[i + 1]) {
                    temp.append("|| ");
                }*/
            }
            System.out.println(temp);
        }
    }

    //запись в файл
    private void writeWeightsToCSV() {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(WEIGHTS_CONTAINER));
            for (int i = 0; i < 10; i++) {
                String[] strings = new String[100];
                for (int j = 0; j < 100; j++) {
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