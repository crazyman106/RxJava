package atomic_demo;

public class Test {

    public static void main(String args[]) {
        final AtomicCount atomicCount = new AtomicCount();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    atomicCount.add();
                    if (i == 999)
                        System.out.println(atomicCount.get());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    atomicCount.add();
                    if (i == 999)
                        System.out.println(atomicCount.get());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    atomicCount.add();
                    if (i == 999)
                        System.out.println(atomicCount.get());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    atomicCount.add();
                    if (i == 999)
                        System.out.println(atomicCount.get());
                }
            }
        }).start();
    }
}
