package com.zolegus.qchecker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author oleg.zherebkin
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Try `qchecker --help' or `qchecker --usage' for more information.");
            return;
        }
        if (args[0].equals("--help") || args[0].equals("--usage") ) {
            //TODO Нужно перенести и выводить из ресурсов
            System.out.println("Синтаксис: qchecker ПУТЬ {ПАРАМЕТРЫ}\n" +
                    "ПУТЬ\t\tОбязательный параметр, определяет расположение хранилище котировок\n" +
                    "ПАРАМЕТРЫ\t:\n\n" +
                    " -param\t text description");
            return;
        }

        if (args[0].equals("--version")) {
            //TODO Нужно перенести и выводить из ресурсов
            System.out.println("qchecker 1.0-SNAPSHOT");
            return;
        }

        String BASE_PATH = args[0];
        File folder = new File(BASE_PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Не верно указан путь к хранилищу котировок");
            System.out.println("Try `qchecker --help' or `qchecker --usage' for more information.");
            return;
        }

        // Составляем список каталогов по указанному пути
        File directory = new File(BASE_PATH);
        File[] rootDirectoryList = directory.listFiles();
        StringBuilder sb = new StringBuilder(1024);
        BufferedWriter reportWriter = null;
        Path pathToFile = Paths.get("./ticker-report.csv");
        try {
            if (!Files.exists(pathToFile))
                Files.createFile(pathToFile);
            reportWriter = Files.newBufferedWriter(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < rootDirectoryList.length; i++) {
            if (rootDirectoryList[i].isDirectory()) {

                System.out.println(rootDirectoryList[i].getName());
                // Читаем всю структуру файлов
                ArrayList<File> ticksDataPathFiles = new ArrayList<>();
                listf(rootDirectoryList[i].getAbsolutePath(), ticksDataPathFiles);
                // Сортируем на возростание
                Collections.sort(ticksDataPathFiles);
                boolean zeroflag = false;
                LocalDate lastDate = null;
                for (int f = 0; f < ticksDataPathFiles.size(); f++) {
                    File file = ticksDataPathFiles.get(f);
                    int day = Integer.parseInt(file.getName().substring(0, 2));
                    int month = Integer.parseInt(file.getParentFile().getName());
                    int year = Integer.parseInt(file.getParentFile().getParentFile().getName());
//                    System.out.printf(" %d-%02d-%02d ", year, month, day);
                    lastDate = LocalDate.of(year, month, day);
                    if (file.length() == 0 && (lastDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                                                lastDate.getDayOfWeek() != DayOfWeek.SUNDAY &&
                                                lastDate.compareTo(LocalDate.of(2015,4,3)) != 0 &&
                                                lastDate.compareTo(LocalDate.of(2015,5,25)) != 0 &&
                                                lastDate.compareTo(LocalDate.of(2015,7,3)) != 0 &&
                                                lastDate.compareTo(LocalDate.of(2015,9,7)) != 0)
                            ) {
                        zeroflag = true;
                        break;
                    }
                }
                // Сохраняем информацию в файл
                sb.setLength(0);
                sb.append(rootDirectoryList[i].getName()).append(";");
                if (zeroflag) {
                    sb.append("ERROR DATA");
                    if (lastDate != null)
                        sb.append(" in ").append(lastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else
                    sb.append(lastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                sb.append("\n");
                try {
                    reportWriter.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            reportWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }
}
