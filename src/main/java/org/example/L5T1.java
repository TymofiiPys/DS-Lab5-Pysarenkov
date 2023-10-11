package org.example;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class L5T1 {
    private final int totalRecruitAmount;
    private final int numThreads;
    private int[] recruitPositions; // 0 - направлений вліво, 1 - вправо

    private boolean stopOrdering;

    public L5T1() {
        totalRecruitAmount = 225;
        numThreads = 3;
        recruitPositions = new int[totalRecruitAmount];
        stopOrdering = false;
        fillWithRandoms();
    }

    private void fillWithRandoms() {
        for (int i = 0; i < totalRecruitAmount; i++) {
            recruitPositions[i] = new Random().nextInt(2);
        }
    }

    class BarAction implements Runnable {
        @Override
        public void run() {
            System.out.println("Стрій новобранців:");
            System.out.println(Arrays.toString(recruitPositions));
            for (int i = 1; i < totalRecruitAmount; i++) {
                if (recruitPositions[i] != recruitPositions[i - 1]) {
                    return;
                }
            }
            stopOrdering = true;
        }
    }

    class Orderer implements Runnable {
        CyclicBarrier cb;
        int left;
        int right;

        public Orderer(CyclicBarrier cb, int threadNumber) {
            this.cb = cb;
            left = threadNumber * (totalRecruitAmount / numThreads);
            right = (threadNumber + 1) * (totalRecruitAmount / numThreads);
            new Thread(this, "Orderer"+threadNumber).start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                if(stopOrdering){
                    break;
                }
                for (int i = left; i < right; i++) {
                    recruitPositions[i] = new Random().nextInt(2);
                    switch (recruitPositions[i]) {
                        case 0 -> {
                            if (i != 0 && recruitPositions[i] != recruitPositions[i - 1]) {
                                recruitPositions[i] = 1 - recruitPositions[i];
                                recruitPositions[i - 1] = 1 - recruitPositions[i - 1];
                            }
                        }
                        case 1 -> {
                            if (i != totalRecruitAmount - 1 && recruitPositions[i] != recruitPositions[i + 1]) {
                                recruitPositions[i] = 1 - recruitPositions[i];
                                recruitPositions[i + 1] = 1 - recruitPositions[i + 1];
                            }
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void startOrder() {
        CyclicBarrier cb = new CyclicBarrier(numThreads, new BarAction());
        for (int i = 0; i < numThreads; i++) {
            new Orderer(cb, i);
        }
    }

    public static void main(String[] args) {
        L5T1 l = new L5T1();
        l.startOrder();
    }
}