
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

import locks.ALock;
import locks.BackoffLock;
import locks.CLHLock;
import locks.CompositeFastPathLock;
import locks.CompositeLock;
import locks.MCSLock;
import locks.SemLock;
import locks.TASLock;
import locks.TTASLock;

class Main {
        static Lock lock;
        static int L , N, REPEAT, D;
        static long start, end;

        static Thread thread(int n) {
                Thread t = new Thread(() -> {
                        for (int i = 0; i < L; i++) {
                                lock.lock();
                                /// Adicionar algum processamento (cpu bound) >> NÃO SLEEP
                                if (D != 0)
                                        bubbleSort();
                                lock.unlock();
                        }
                });
                t.start();
                return t;
        }

        // Consolidado
        static long testThreads(int TH) {
                start = System.nanoTime();
                Thread[] threads = new Thread[TH];
                for (int i = 0; i < TH; i++)
                        threads[i] = thread(i);
                try {
                        for (int i = 0; i < TH; i++) {
                                threads[i].join();
                        }
                } catch (InterruptedException e) {
                        return 0;
                } finally {
                        end = System.nanoTime();
                }
                return (end - start);
        }

        // Consolidado (CPU BOUND)
        static void bubbleSort() {
                int temp, arraysize = D;
                int[] array = new int[arraysize];

                for (int i = 0; i < arraysize; i++) {
                        array[i] = i;
                }

                for (int i = 0; i < (arraysize - 1); i++) {
                        for (int j = 0; j < arraysize - i - 1; j++) {
                                if (array[j] < array[j + 1]) {
                                        temp = array[j];
                                        array[j] = array[j + 1];
                                        array[j + 1] = temp;
                                }
                        }
                }
        }

        static long returnMedia(int thread) {
                if (REPEAT == 1) {
                        return TimeUnit.MILLISECONDS.convert(testThreads(thread), TimeUnit.NANOSECONDS);
                } else if (REPEAT == 2) {
                        long a = testThreads(thread);
                        long b = testThreads(thread);
                        return TimeUnit.MILLISECONDS.convert((a + b) / 2, TimeUnit.NANOSECONDS);
                }
                long[] result = new long[REPEAT];
                long media = 0;
                for (int i = 0; i < REPEAT; i++) {
                        result[i] = testThreads(thread);
                }
                Arrays.sort(result);
                for (int i = 1; i < REPEAT - 1; i++) {
                        media += result[i];
                }
                media = media / (REPEAT - 2);
                media = TimeUnit.MILLISECONDS.convert(media, TimeUnit.NANOSECONDS);
                System.out.println(" time >> " + media + " >> n threads  " + thread);
                return media;
        }

        public static void main(String[] args) throws InterruptedException {
                if (args.length != 5) {
                        System.out.println(
                                        "Usage: java numThreads numIterations numRepetitions sizeBubbleSort fileName");
                } else {
                        System.out.println("Executando experimentos");
                        L = Integer.parseInt(args[1]);
                        N = Integer.parseInt(args[0]);
                        REPEAT = Integer.parseInt(args[2]);
                        D = Integer.parseInt(args[3]);

                        /// Repetir o exp e fazer média
                        /// Repetir exp por n vezes, remover maior e menor resultado, e usar média
                        PrintWriter writer;
                        try {
                                writer = new PrintWriter(args[4] + ".csv", "UTF-8");
                                for (int i = 2; i <= N; i = i + 2) {
                                        writer.print(i + ",");
                                        // System.out.print(">> TASLock >>");
                                        lock = new TASLock();
                                        writer.print(returnMedia(i) + ",");
                                        // System.out.print(">> TTASLock >>");
                                        lock = new TTASLock();
                                        writer.print(returnMedia(i) + ",");

                                        // System.out.print(">> BackoffLock >>");
                                        lock = new BackoffLock();
                                        writer.print(returnMedia(i) + ",");

                                        // System.out.print(">> SemLock >>");
                                        lock = new SemLock();
                                        writer.print(returnMedia(i) + ",");

                                        // System.out.print(">> ALock >>");
                                        // Verificar motivo de travar ao executar com maior número de threads
                                        lock = new ALock(i);
                                        writer.print(returnMedia(i) + ",");

                                        // System.out.print(">> MCSLock >>");
                                        lock = new MCSLock();
                                        writer.print(returnMedia(i) + ",");

                                        // System.out.print(">> CLHLock >>");
                                        lock = new CLHLock();
                                        writer.print(returnMedia(i) + ",");

                                        lock = new CompositeFastPathLock();
                                        writer.print(returnMedia(i) + ",");

                                        lock = new CompositeLock();
                                        writer.println(returnMedia(i));

                                }

                                writer.close();

                        } catch (FileNotFoundException | UnsupportedEncodingException e) {
                                e.printStackTrace();
                        }
                }
        }

}