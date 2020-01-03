package ru.gadjini.reminder.generator;

import java.io.PrintWriter;

public class TestDataGenerator {

    public static void main(String[] args) throws Exception {
        generateUsers();
    }

    private static void generateUsers() throws Exception {
        try (PrintWriter printWriter = new PrintWriter("C:\\Users\\GadzhievSA\\Work\\SCM\\tg-bot\\reminder\\test\\Users config.csv")) {
            for (int i = 1; i <= 100000; ++i) {
                printWriter.println("test" + i + ",test" + i + ",test" + i + "," + i);
            }
        }
    }
}
