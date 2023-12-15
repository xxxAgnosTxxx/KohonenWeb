public class Main {
    public static void main(String[] args) {
        Web web = new Web();
        int[][] examples = DataExamples.getExamples();

        printStatistic(web, examples);

        Mapper mapper = new Mapper();
        mapper.learn(examples);
    }

    private static void printResult(int ix) {
        if (ix == 0) {
            System.out.println("Скорее всего - это птица.");
        } else if (ix == 1) {
            System.out.println("Скорее всего - это млекопитающее.");
        } else if (ix == 2) {
            System.out.println("Скорее всего - это рыба.");
        } else if (ix == 3) {
            System.out.println("Скорее всего - это рептилия.");
        } else if (ix == 4) {
            System.out.println("Скорее всего - это земноводное.");
        }
    }

    private static void printStatistic(Web web, int[][] examples){
        System.out.println("Кластеризация:");
        int[] statistics = new int[5];
        for (int[] example : examples) {
            int ix = web.start(example);
            if (ix == 0) {
                statistics[0]++;
            } else if (ix == 1) {
                statistics[1]++;
            } else if (ix == 2) {
                statistics[2]++;
            } else if (ix == 3) {
                statistics[3]++;
            } else if (ix == 4) {
                statistics[4]++;
            }
        }
        System.out.println("Кластер 'птицы': " + statistics[0]);
        System.out.println("Кластер 'млекопитающие': " + statistics[1]);
        System.out.println("Кластер 'рыбы': " + statistics[2]);
        System.out.println("Кластер 'рептилии': " + statistics[3]);
        System.out.println("Кластер 'земноводные': " + statistics[4]+"\n");
    }
}
