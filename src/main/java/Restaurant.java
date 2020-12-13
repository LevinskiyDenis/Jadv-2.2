import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class Restaurant {

    final Lock lockvwto = new ReentrantLock(true);
    final Lock lockotc = new ReentrantLock(true);
    final Lock lockotb = new ReentrantLock(true);
    final Lock lockod = new ReentrantLock(true);

    final Condition visitorsWantToOrder = lockvwto.newCondition();
    final Condition ordersToCook = lockotc.newCondition();
    final Condition ordersToBring = lockotb.newCondition();
    final Condition ordersDelivered = lockod.newCondition();

    // переменные для работы с Condition
    int numberOfVisitorsWantToOrder;
    int numberOfOrdersToCook;
    int numberOfOrdersToBring;
    int numberOfOrdersDelivered;

    // переменные для подсчета посетителей по ролям
    int totalNumberOfOrdersAccepted;
    int totalNumberOfOrdersDelivered;
    int totalNumberOfOrdersToBring;
    AtomicInteger totalNumberOfVisitors = new AtomicInteger(0);

    final int VISITORSTIMETOCHOOSE = 5000;
    final int COOKSTIMETODO = 7000;
    final int VISITORSTIMETOEAT = 3000;
    final int LIMIT = 5;

    public void waiterToDo() {

        System.out.printf("%s на работе\n", Thread.currentThread().getName());

        while (totalNumberOfOrdersDelivered < LIMIT) {

            try {
                lockvwto.lock();

                if (totalNumberOfOrdersAccepted >= LIMIT) {
                    System.out.printf("%s закончил работать\n", Thread.currentThread().getName());
                    return;
                }

                while (numberOfVisitorsWantToOrder == 0) {
                    System.out.println(Thread.currentThread().getName() + " totalNumberOfVisitorsWantToOrder " + totalNumberOfOrdersAccepted);
                    System.out.println(Thread.currentThread().getName() + " totalNumberOfOrdersDelivered " + totalNumberOfOrdersDelivered);
                    System.out.println(Thread.currentThread().getName() + " ждет заказов посетителей");
                    visitorsWantToOrder.await();
                }

                System.out.printf("%s принял заказ\n", Thread.currentThread().getName());
                numberOfVisitorsWantToOrder -= 1;
                totalNumberOfOrdersAccepted += 1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lockvwto.unlock();
            }

            try {
                lockotc.lock();
                numberOfOrdersToCook += 1;
                ordersToCook.signal();
            } finally {
                lockotc.unlock();
            }

            try {
                lockotb.lock();
                while (numberOfOrdersToBring == 0) {
                    ordersToBring.await();
                }

                System.out.printf("%s принес заказ\n", Thread.currentThread().getName());
                numberOfOrdersToBring -= 1;
            } catch (
                    InterruptedException e) {
                e.printStackTrace();
            } finally {
                lockotb.unlock();
            }

            try {
                lockod.lock();
                numberOfOrdersDelivered += 1;
                totalNumberOfOrdersDelivered += 1;
                ordersDelivered.signal();
            } finally {
                lockod.unlock();
            }
        }

        System.out.printf("%s закончил работать\n", Thread.currentThread().getName());
    }

    public void cookToDo() {

        System.out.printf("%s на работе\n", Thread.currentThread().getName());

        while (totalNumberOfOrdersToBring < 5) {

            try {
                lockotc.lock();
                while (numberOfOrdersToCook == 0) {
                    ordersToCook.await();
                }
                System.out.printf("%s готовит заказ\n", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lockotc.unlock();
            }

            try {
                Thread.sleep(COOKSTIMETODO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                lockotc.lock();
                System.out.printf("%s приготовил заказ\n", Thread.currentThread().getName());
                numberOfOrdersToCook -= 1;
            } finally {
                lockotc.unlock();
            }

            try {
                lockotb.lock();
                numberOfOrdersToBring += 1;
                totalNumberOfOrdersToBring += 1;
                ordersToBring.signal();
            } finally {
                lockotb.unlock();
            }
        }

        System.out.printf("%s закончил работать\n", Thread.currentThread().getName());
    }

    public void visitorToDo() {

        totalNumberOfVisitors.getAndIncrement();

        if (totalNumberOfVisitors.get() > LIMIT) {
            System.out.printf("%s, ресторан закрыт\n", Thread.currentThread().getName());
            return;
        } else {
            System.out.printf("%s зашел в ресторан\n", Thread.currentThread().getName());
        }

        try {
            Thread.sleep(VISITORSTIMETOCHOOSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            lockvwto.lock();
            numberOfVisitorsWantToOrder += 1;
            System.out.printf("%s хочет сделать заказ\n", Thread.currentThread().getName());
            visitorsWantToOrder.signal();
        } finally {
            lockvwto.unlock();
        }

        try {
            lockod.lock();
            while (numberOfOrdersDelivered == 0) {
                ordersDelivered.await();
            }
            numberOfOrdersDelivered -= 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockod.unlock();
        }

        System.out.printf("%s приступил к еде\n", Thread.currentThread().getName());

        try {
            Thread.sleep(VISITORSTIMETOEAT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.printf("%s вышел из ресторана\n", Thread.currentThread().getName());
    }
}
