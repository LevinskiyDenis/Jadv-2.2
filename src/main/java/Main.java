public class Main {

    public static void main(String[] args) {

        Restaurant restaurant = new Restaurant();
        Runnable orderFood = restaurant::visitorToDo;
        Runnable bringFood = restaurant::waiterToDo;
        Runnable cookFood = restaurant::cookToDo;

        new Thread(null, cookFood, "Повар 1").start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(null, bringFood, "Официант 1").start();
        new Thread(null, bringFood, "Официант 2").start();
        new Thread(null, bringFood, "Официант 3").start();

        for (int i = 0; i < 7; i++) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Thread(null, orderFood, "Посетитель " + (i + 1)).start();
        }
    }
}
