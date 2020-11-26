import java.util.concurrent.locks.*;

public class Restaurant {

    final Lock lock = new ReentrantLock(true);

    final Condition visitorsWantToOrder = lock.newCondition();
    final Condition ordersToCook = lock.newCondition();
    final Condition ordersToBring = lock.newCondition();
    final Condition ordersDelivered = lock.newCondition();

    // переменные для работы с Condition
    int numberOfVisitorsWantToOrder;
    int numberOfOrdersToCook;
    int numberOfOrdersToBring;
    int numberOfOrdersDelivered;

    // переменные для подсчета посетителей по ролям
    int numberOfVisitorsForWaiter;
    int numberOfVisitorsForCook;
    int numberOfVisitorsForVisitors;

    final int VISITORSTIMETOCHOOSE = 5000;
    final int COOKSTIMETODO = 7000;
    final int VISITORSTIMETOEAT = 3000;

    public void waiterToDo() {

        try {
            lock.lock();
            System.out.printf("%s на работе\n", Thread.currentThread().getName());

            while (numberOfVisitorsForWaiter < 5) {

                while (numberOfVisitorsWantToOrder == 0) {
                    visitorsWantToOrder.await();
                }
                numberOfVisitorsForWaiter += 1;
                System.out.printf("%s принял заказ\n", Thread.currentThread().getName());
                numberOfVisitorsWantToOrder -= 1;
                numberOfOrdersToCook += 1;
                ordersToCook.signal();

                while (numberOfOrdersToBring == 0) {
                    ordersToBring.await();
                }

                System.out.printf("%s принес заказ\n", Thread.currentThread().getName());
                numberOfOrdersDelivered += 1;
                numberOfOrdersToBring -= 1;
                ordersDelivered.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void cookToDo() {
        try {
            lock.lock();
            System.out.printf("%s на работе\n", Thread.currentThread().getName());

            while (numberOfVisitorsForCook < 5) {

                while (numberOfOrdersToCook == 0) {
                    ordersToCook.await();
                }

                numberOfVisitorsForCook += 1;
                System.out.printf("%s готовит заказ\n", Thread.currentThread().getName());
                Thread.sleep(COOKSTIMETODO);
                System.out.printf("%s приготовил заказ\n", Thread.currentThread().getName());
                numberOfOrdersToCook -= 1;
                numberOfOrdersToBring += 1;
                ordersToBring.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    public void visitorToDo() {
        try {
            lock.lock();
            numberOfVisitorsForVisitors += 1;

            if (numberOfVisitorsForVisitors > 5) {
                System.out.println("Ресторан обслужил 5 посетителей и закрыт для новых гостей");
                return;
            }
            System.out.printf("%s зашел в ресторан\n", Thread.currentThread().getName());
            Thread.sleep(VISITORSTIMETOCHOOSE);
            numberOfVisitorsWantToOrder += 1;
            visitorsWantToOrder.signal();

            System.out.printf("%s ЖДЕТ\n", Thread.currentThread().getName());
            while (numberOfOrdersDelivered == 0) {
                ordersDelivered.await();
            }

            System.out.printf("%s приступил к еде\n", Thread.currentThread().getName());
            Thread.sleep(VISITORSTIMETOEAT);
            System.out.printf("%s вышел из ресторана\n", Thread.currentThread().getName());
            numberOfOrdersDelivered -= 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


}
