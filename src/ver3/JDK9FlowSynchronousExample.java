package ver3;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class JDK9FlowSynchronousExample {

    public static void main(String[] args) {

        // Create publisher
        Publisher<Integer> publisherSync = new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {

                // Publish 100 numbers
                for (int i = 0; i < 100; i++) {
                    System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                    // Publish or emit a value.
                    subscriber.onNext(i);
                }
                // When all values or emitted, call complete.
                subscriber.onComplete();
            }
        };

        // Create subscriber
        Subscriber<Integer> subscriberSync = new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(Integer item) {
                // Process received value.
                System.out.println(Thread.currentThread().getName() + " | Received = " + item);
                // 100 mills delay to simulate slow subscriber
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        };

        // Subscriber subscribing to publisher
        publisherSync.subscribe(subscriberSync);
    }
}
// Subscriber takes 100 ms to complete processing. You can see that, when
// subscriber takes 100 ms to process, publisher is blocked during that time.
// Next value will be published only when subscriber processing is finished for
// earlier value. You can also see that both subscribing & publishing happens on
// same thread i.e. main thread.
