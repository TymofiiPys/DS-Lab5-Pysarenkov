package org.example;

import java.util.HashMap;
import java.util.Random;

public class L5T2 {
    private String[] threadStrings;
    private boolean stopCondition;
    public L5T2(){
        threadStrings = new String[4];
        stopCondition = false;
        for (int i = 0; i < 4; i++) {
            threadStrings[i] = "";
            for (int j = 0; j < 10; j++) {
                threadStrings[i] += (char)(new Random().nextInt(4) + 65);
            }
            System.out.println("Рядок " + i + " = " + threadStrings[i]);
        }
    }

    class Barrier{
        int threadsWaiting;
        int totalThreadsAmount = 4;
        private void BarAction(){
            int[] ABs = new int[4];
            HashMap<Integer, Integer> equalABs = new HashMap<>();
            System.out.println("===============================================");
            for (int k = 0; k < 4; k++) {
                System.out.print(threadStrings[k]);
                for (int i = 0; i < threadStrings[k].length(); i++) {
                    if(threadStrings[k].charAt(i) == 'A' || threadStrings[k].charAt(i) == 'B'){
                        ABs[k]++;
                    }
                }
                System.out.println(", ABs = " + ABs[k]);
                if(equalABs.containsKey(ABs[k])){
                    equalABs.put(ABs[k], equalABs.get(ABs[k]) + 1);
                }
                else {
                    equalABs.put(ABs[k], 1);
                }
            }
            for (Integer i:
                 equalABs.keySet()) {
                if(equalABs.get(i) >= 3){
                    stopCondition = true;
                    break;
                }
            }
        }

        synchronized void enterBarrier(){
            threadsWaiting++;
            System.out.println("Чекає уже " + threadsWaiting + " потоків");
            if(threadsWaiting == totalThreadsAmount){
                BarAction();
                notifyAll();
            }
        }

        synchronized void waitTillFinished(){
            if(threadsWaiting != 4){
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
//            threadsWaiting--;
        }

        synchronized void leaveBarrier(){
            threadsWaiting--;
        }
        public void awaitAndExecuteBarAction(){
            enterBarrier();
            waitTillFinished();
            leaveBarrier();
        }
    }
    class StringManipulator implements Runnable{
        int num;
        Barrier b;
        public StringManipulator(int num, Barrier b){
            this.num = num;
            this.b = b;
            new Thread(this, "SM"+num).start();
        }
        @Override
        public void run() {
            while (true) {
                b.awaitAndExecuteBarAction();
                if(stopCondition){
                    break;
                }
                String s = "";
                for (int i = 0; i < threadStrings[num].length(); i++) {
                    int changeOrNot = new Random().nextInt(2);
                    switch (changeOrNot) {
                        case 0 -> s += threadStrings[num].charAt(i);
                        case 1 -> {
                            switch (threadStrings[num].charAt(i)) {
                                case 'A' -> s += 'C';
                                case 'B' -> s += 'D';
                                case 'C' -> s += 'A';
                                case 'D' -> s += 'B';
                            }
                        }
                    }
                }
                threadStrings[num] = s;
//                System.out.println(threadStrings[num]);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void start(){
        Barrier b = new Barrier();
        new StringManipulator(0, b);
        new StringManipulator(1, b);
        new StringManipulator(2, b);
        new StringManipulator(3, b);
    }

    public static void main(String[] args) {
        L5T2 l = new L5T2();
        l.start();
    }
}
